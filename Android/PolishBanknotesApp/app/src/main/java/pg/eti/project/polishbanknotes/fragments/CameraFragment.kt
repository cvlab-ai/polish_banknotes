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
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager

import org.tensorflow.lite.task.vision.classifier.Classifications
import pg.eti.project.polishbanknotes.ImageClassifierHelper
import pg.eti.project.polishbanknotes.MainActivity
import pg.eti.project.polishbanknotes.R
import pg.eti.project.polishbanknotes.UiManager
import pg.eti.project.polishbanknotes.databinding.FragmentCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * I want device to vibrate every 1s, so if I assume that older device is inferencing
 * ~200ms so 5 of them will give me about 1s. The accuracy is not that important.
 */
const val INFERENCE_COUNTER_FOR_OLDER_DEVICES = 5

/**
 * Number of last results that will be considered for choosing final label
 * (most occurrences in last NUMBER_OF_LAST_RESULTS)
 */
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
    private var wasHaptizerActive = false
    private var inferenceCounter: Int = 0
    private var lastLabels = mutableListOf<String?>()
    private var torchStatus = false
    private lateinit var uiManager: UiManager

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    override fun onResume() {
        super.onResume()

        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(CameraFragmentDirections.actionCameraToPermissions())
        }

        torchStatus = (activity as MainActivity?)!!.torchManager.getTorchStatus()
        if(torchStatus)
            camera!!.cameraControl.enableTorch(true)

    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        // Shut down our background executor
        cameraExecutor.shutdown()
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

        // Prepare UI manager for mode switch.
        uiManager = UiManager(fragmentCameraBinding)

        imageClassifierHelper =
            ImageClassifierHelper(
                context = requireContext(),
                imageClassifierListener = this,
                uiManager = uiManager,
                activity = requireActivity()
            )

        with(fragmentCameraBinding.recyclerviewResults) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = classificationResultsAdapter
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        fragmentCameraBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera()
        }

        /**
         * Turning on classification by clicking on camera.
         * It needs to work even when it is in dev mode, because someone
         * could turn the dev mode back on and not turn the classification.
         */
        fragmentCameraBinding.viewFinder.setOnClickListener {
            classificationActive = true
            haptizerActive = true
        }

        // Attach listeners to UI control widgets
        initBottomSheetControls()

        // Attach listeners to UI components to manage it.
        initModeSwitchListeners()
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

    private fun initBottomSheetControls() {
        // When clicked, lower classification score threshold floor
        fragmentCameraBinding.bottomSheetLayout.thresholdMinus.setOnClickListener {
            if (imageClassifierHelper.threshold >= 0.1) {
                imageClassifierHelper.threshold -= 0.1f
                updateControlsUi()
            }
        }

        // When clicked, raise classification score threshold floor
        fragmentCameraBinding.bottomSheetLayout.thresholdPlus.setOnClickListener {
            if (imageClassifierHelper.threshold < 0.9) {
                imageClassifierHelper.threshold += 0.1f
                updateControlsUi()
            }
        }

        // When clicked, reduce the number of objects that can be classified at a time
        fragmentCameraBinding.bottomSheetLayout.maxResultsMinus.setOnClickListener {
            if (imageClassifierHelper.maxResults > 1) {
                imageClassifierHelper.maxResults--
                updateControlsUi()
                classificationResultsAdapter.updateAdapterSize(size = imageClassifierHelper.maxResults)
            }
        }

        // When clicked, increase the number of objects that can be classified at a time
        fragmentCameraBinding.bottomSheetLayout.maxResultsPlus.setOnClickListener {
            if (imageClassifierHelper.maxResults < 7) {
                imageClassifierHelper.maxResults++
                updateControlsUi()
                classificationResultsAdapter.updateAdapterSize(size = imageClassifierHelper.maxResults)
            }
        }

        // When clicked, decrease the number of threads used for classification
        fragmentCameraBinding.bottomSheetLayout.threadsMinus.setOnClickListener {
            if (imageClassifierHelper.numThreads > 1) {
                imageClassifierHelper.numThreads--
                updateControlsUi()
            }
        }

        // When clicked, increase the number of threads used for classification
        fragmentCameraBinding.bottomSheetLayout.threadsPlus.setOnClickListener {
            if (imageClassifierHelper.numThreads < 4) {
                imageClassifierHelper.numThreads++
                updateControlsUi()
            }
        }

        // When clicked, change the underlying hardware used for inference. Current options are CPU
        // GPU, and NNAPI
        fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.setSelection(0, false)
        fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    imageClassifierHelper.currentDelegate = position
                    updateControlsUi()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    /* no op */
                }
            }

        // When clicked, change the underlying model used for object classification
        fragmentCameraBinding.bottomSheetLayout.spinnerModel.setSelection(0, false)
        fragmentCameraBinding.bottomSheetLayout.spinnerModel.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    imageClassifierHelper.currentModel = position
                    updateControlsUi()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    /* no op */
                }
            }
    }

    private fun initModeSwitchListeners() {
        // Toggle dev and user mode by clicking the TFL logo.
        fragmentCameraBinding.toolbar.setOnClickListener {
            if (fragmentCameraBinding.recyclerviewResults.visibility == View.GONE) {
                // Show bottom sheet controls.
                // Every UI change must be done on UI thread.
                activity?.runOnUiThread {
                    uiManager.showBottomSheetControls()
                }
            } else if (fragmentCameraBinding.recyclerviewResults.visibility == View.VISIBLE) {
                // Hide bottom sheet controls.
                // Every UI change must be done on UI thread.
                activity?.runOnUiThread {
                    uiManager.hideBottomSheetControls()
                }
            }
            // TODO else exceptions here, any test?
        }
    }

    // Update the values displayed in the bottom sheet. Reset classifier.
    private fun updateControlsUi() {
        fragmentCameraBinding.bottomSheetLayout.maxResultsValue.text =
            imageClassifierHelper.maxResults.toString()

        fragmentCameraBinding.bottomSheetLayout.thresholdValue.text =
            String.format("%.2f", imageClassifierHelper.threshold)
        fragmentCameraBinding.bottomSheetLayout.threadsValue.text =
            imageClassifierHelper.numThreads.toString()
        // Needs to be cleared instead of reinitialized because the GPU
        // delegate needs to be initialized on the thread using it when applicable
        imageClassifierHelper.clearImageClassifier()
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
        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }

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
            fragmentCameraBinding.bottomSheetLayout.inferenceTimeVal.text =
                String.format("%d ms", inferenceTime)

        // Say the label.
        // TODO: any test, exception, else?
        // TODO: test use cases
        // TODO PERFORMANCE: is this not slow?

            //
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

            if(label == null)
                label = "None"

            // Snippet to have active/inactive classification
            if (label != "None"
                && fragmentCameraBinding.recyclerviewResults.visibility == View.GONE && lastLabels.size == NUMBER_OF_LAST_RESULTS) {
                // Stop classifying only when in blind-user mode.
                (activity as MainActivity?)!!.talkBackSpeaker.speak(label)
                classificationActive = false
                haptizerActive = false
                lastLabels.clear()

//              I dont know if torch should be turned off after recognition
//                torchStatus = false
            } else if (label != "None" && label != lastResultLabel && lastLabels.size == NUMBER_OF_LAST_RESULTS) {
                // Speak on changed banknote.
                // (dev mode + maybe in user mode because of USE-CASE #1)
                // TODO CONTRARY USE-CASE #1: If someone will fastly put other (the same value)
                //  banknote instead of previous, the app won't speak. Is this possible?
                (activity as MainActivity?)!!.talkBackSpeaker.speak(label)
                // TODO TOO DIRECT: accessing val from here; maybe create abstract class
                //  for accessibility functionalities?
                // TODO LEARN: how are fragments run, there is no call anywhere.
                lastLabels.clear()

//              I dont know if torch should be turned off after recognition
//                torchStatus = false
            } else if (label == "None") {
                // Start vibrating when the label is "None".
                haptizerActive = true
            }
            lastResultLabel = label

            /**
             * Run, pause haptizer service.
             * It needs to be controlled by two variables because it will be run every inference
             * thus it would start haptizer every time.
             */
            // TODO TEST ON DEVICE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                /**
                 * Haptizer tested on: Samsung S21 5G
                 */
                if (haptizerActive && !wasHaptizerActive) {
                    (activity as MainActivity?)!!.haptizer.startSnowCone()
                    wasHaptizerActive = true
                } else if (!haptizerActive && wasHaptizerActive) {
                    (activity as MainActivity?)!!.haptizer.stop()
                    wasHaptizerActive = false
                }
            } else {
                /**
                 * When haptizer is not active it won't count inferences,
                 * so there is no need to stop it.
                 * TESTED ON: Maxcom MS457
                 */
                if (haptizerActive)
                    if (inferenceCounter % INFERENCE_COUNTER_FOR_OLDER_DEVICES == 0)
                        (activity as MainActivity?)!!.haptizer.vibrateShot()
            }
            if (inferenceCounter % INFERENCE_COUNTER_FOR_OLDER_DEVICES == 0)
                enableTorch()
                
            inferenceCounter++ 
        }
    }

    private fun enableTorch(){
        if (torchStatus == (activity as MainActivity?)!!.torchManager.getTorchStatus())
            return

        torchStatus = (activity as MainActivity?)!!.torchManager.getTorchStatus()

        if (torchStatus){
            camera!!.cameraControl.enableTorch(true)
        }else{
            camera!!.cameraControl.enableTorch(false)
        }
    }
}
