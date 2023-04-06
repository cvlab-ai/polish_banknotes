/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pg.eti.project.polishbanknotes.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import java.lang.Exception
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.material.appbar.AppBarLayout
import org.tensorflow.lite.task.vision.classifier.Classifications
import pg.eti.project.polishbanknotes.ImageClassifierHelper
import pg.eti.project.polishbanknotes.MainActivity
import pg.eti.project.polishbanknotes.R
import pg.eti.project.polishbanknotes.accessability.Beeper
import pg.eti.project.polishbanknotes.accessability.Haptizer
import pg.eti.project.polishbanknotes.accessability.TalkBackSpeaker
import pg.eti.project.polishbanknotes.databinding.FragmentCameraBinding
import pg.eti.project.polishbanknotes.settings_management.LabelManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// TODO remove: when torch will be refactored
const val MILLIS_TO_HAPTIZE = 2000L

/**
 * Number of last results that will be considered for choosing final label
 * (most occurrences in last NUMBER_OF_LAST_RESULTS)
 */
// TODO optimization: auto select inference counter for older devices.
//  on Xiaomi Redmi 6A the app is slow and inference is giving message even if not
//  pointing on banknote.
// TODO question: is it needed?
// TODO CRASH: fast switching with settings crashes because binding not ready
const val NUMBER_OF_LAST_RESULTS = 5

class CameraFragment : Fragment(), ImageClassifierHelper.ClassifierListener {

    companion object {
        private const val TAG = "Image Classifier"
    }

    private var _fragmentCameraBinding: FragmentCameraBinding? = null
    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private lateinit var bitmapBuffer: Bitmap
    private val classificationResultsAdapter by lazy {
        ClassificationResultsAdapter().apply {
            updateAdapterSize(imageClassifierHelper.maxResults)
        }
    }
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lastResultLabel = ""
    private var classificationActive = true
    private var haptizerActive = true
    private var inferenceMillisCounter: Long = 0L
    private var lastLabels = mutableListOf<String?>()
    private var torchStatus = false
    private lateinit var beeper: Beeper
    private lateinit var haptizer: Haptizer
    private lateinit var talkBackSpeaker: TalkBackSpeaker
    private lateinit var labelManager: LabelManager
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    override fun onResume() {
        super.onResume()

        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(CameraFragmentDirections.actionCameraToPermissions())
        }

        // TODO test: if everything else works after returning to app.
        // TODO crash
        // NOTE (03.04.2023): these lines crashes the return from settings
//        torchStatus = (activity as MainActivity?)!!.torchManager.getTorchStatus()
//        if(torchStatus)
//            camera!!.cameraControl.enableTorch(true)


        checkSettingsManagement()
    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        // Shut down our background executor
        cameraExecutor.shutdown()

        // Stopping the haptizer service.
        haptizer.stop()

        // TextToSpeech service must be stopped before closing the app.
        talkBackSpeaker.stop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)

        return fragmentCameraBinding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageClassifierHelper =
            ImageClassifierHelper(
                context = requireContext(),
                imageClassifierListener = this
            )

        cameraExecutor = Executors.newSingleThreadExecutor()

        beeper = Beeper(
            context = requireContext()
        )

        fragmentCameraBinding.viewFinder.post {
            // Set up the camera and its use cases.
            // Also turn on Torch.
            setUpCamera()
            enableTorch()
        }

        // Initialize services.
        talkBackSpeaker = TalkBackSpeaker(requireContext())
        haptizer = Haptizer(requireContext())

        // Init SharedPreferences.
        sharedPreferences = requireContext().getSharedPreferences(
            "pg.eti.project.polishbanknotes",
            MODE_PRIVATE
            )
        sharedPreferencesEditor = sharedPreferences.edit()

        // Init settings management.
        labelManager = LabelManager()
        checkSettingsManagement()
    }

    private fun checkSettingsManagement(){
        labelManager.checkIfEnable(requireContext())
        labelManager.updateAppearance(requireContext(), fragmentCameraBinding)
    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation = fragmentCameraBinding.viewFinder.display.rotation
    }

    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector - makes assumption that we're only using the back camera
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview =
            Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(cameraExecutor) { image ->
                        if (!::bitmapBuffer.isInitialized) {
                            // The image rotation and RGB image buffer are initialized only once
                            // the analyzer has started running
                            bitmapBuffer = Bitmap.createBitmap(
                                image.width,
                                image.height,
                                Bitmap.Config.ARGB_8888
                            )
                        }

                        classifyImage(image)
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun getScreenOrientation() : Int {
        val outMetrics = DisplayMetrics()

        val display: Display?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display = requireActivity().display
            //display?.getRealMetrics(outMetrics)
        } else {
            @Suppress("DEPRECATION")
            display = requireActivity().windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(outMetrics)
        }

        return display?.rotation ?: 0
    }

    private fun classifyImage(image: ImageProxy) {
        // Copy out RGB bits to the shared bitmap buffer
        // TODO CRASH: if the infer time is ~300 ms and pause app, bitmaps won't load and the whole
        //  app will crash...

        try {
            image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }
        } catch (e: java.lang.RuntimeException) {
            Log.e("BUFF_FULL", "ROME parse error: $e")
        } catch (e2: Error) {
            Log.e("BUFF_FULL", "ROME parse error2: $e2")
        }


        if (classificationActive) {
            // Pass Bitmap and rotation to the image classifier helper for processing and classification
            imageClassifierHelper.classify(bitmapBuffer, getScreenOrientation())
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            classificationResultsAdapter.updateResults(null)
            classificationResultsAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResults(
        results: List<Classifications>?,
        inferenceTime: Long
    ) {
        activity?.runOnUiThread {
            // Show result on bottom sheet
            classificationResultsAdapter.updateResults(results)
            classificationResultsAdapter.notifyDataSetChanged()

            // TODO: any test, exception, else?
            // TODO: test use cases
            // TODO PERFORMANCE: is this not slow?

            val result: String? = if(results?.isEmpty() == true || results!![0].categories.isEmpty()){
                null
            }else{
                results[0].categories[0].label
            }

            if(lastLabels.size >= NUMBER_OF_LAST_RESULTS){
                lastLabels.removeLast()
            }
            lastLabels.add(0, result)

            // label is the result that have the most occurrences in lastLabels list
            var label = lastLabels.groupingBy { it }.eachCount().toList()
                .maxByOrNull { (_, value) -> value }!!.first
            // If the user changed the banknote at the end of inference
            // and most of labels was from the one before.
            if(label == null || label != result)
                label = "None"

            if (label != "None" && lastLabels.size == NUMBER_OF_LAST_RESULTS) {
                // TODO TOO DIRECT: accessing val from here; maybe create abstract class
                // TODO CONTRARY USE-CASE #1: If someone will fastly put other (the same value)
                //  banknote instead of previous, the app won't speak. Is this possible?

                // We want only to beep at highest denominations.
                when (label) {
                    "200" -> beeper.beep()
                    "500" -> beeper.doubleBeep()
                    else -> talkBackSpeaker.speak(label)
                }

                // Show the label in textView.
                // TODO: if all the time on one banknote then it will say label,
                //  even if we are not pointing at it.
                // TODO: give option to turn on TalkBackSpeaker if TalkBack is not turned on.

                if (labelManager.getIsActive()) {
                    fragmentCameraBinding.labelTextView.text = label
                    classificationActive = false
                    val resetLabelTextView = Runnable {
                        fragmentCameraBinding.labelTextView.text = ""
                        // To minimize bug of pointing at another banknote
                        // and saying the previous one.
                        // Slightly slower working, but still good.
                        val turnOnClassification = Runnable {
                            classificationActive = true
                        }
                        Handler(Looper.getMainLooper()).postDelayed(turnOnClassification, 500)
                    }
                    Handler(Looper.getMainLooper()).postDelayed(resetLabelTextView, 1200)
                }

                lastLabels.clear()
            }

            lastResultLabel = label

            if (haptizerActive) {
                if (inferenceMillisCounter >= MILLIS_TO_HAPTIZE)
                    // Check if torch is needed.
                    enableTorch()
                    inferenceMillisCounter = haptizer.vibrateShot(inferenceMillisCounter)
            }

            inferenceMillisCounter += inferenceTime
             Log.d("MILLIS", "$inferenceMillisCounter")
        }
    }

    private fun enableTorch() {
        if (torchStatus == (activity as MainActivity?)!!.torchManager.getTorchStatus())
            return

        torchStatus = (activity as MainActivity?)!!.torchManager.getTorchStatus()

        try {
            if (torchStatus) {
                camera!!.cameraControl.enableTorch(true)
            } else {
                camera!!.cameraControl.enableTorch(false)
            }
        } catch (e: NullPointerException) {
            Log.e("NULL_TORCH", "NPE error: $e")
        }

    }
}
