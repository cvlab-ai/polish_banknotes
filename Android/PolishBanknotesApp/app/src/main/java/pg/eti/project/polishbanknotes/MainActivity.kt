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

package pg.eti.project.polishbanknotes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.*
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.view.get
import androidx.fragment.app.findFragment
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_camera.view.*
import pg.eti.project.polishbanknotes.accesability.TalkBackSpeaker
import pg.eti.project.polishbanknotes.databinding.ActivityMainBinding
import pg.eti.project.polishbanknotes.fragments.CameraFragment
import java.io.File
import java.util.*

// TODO improve modularity: separate TTS and so on to separate classes (it is commmon?)
class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    lateinit var talkBackSpeaker: TalkBackSpeaker
//    private lateinit var vib: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setting the vibrator.
        // TODO check if the device has needed options to vibrate.
        // TODO Do sth with this "ClassNullExcpetion".
//        vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val vibratorManager =
//                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
//            vibratorManager!!.defaultVibrator
//            // TODO catch exceptions
//        } else {
//            @Suppress("DEPRECATION")
//            getSystemService(VIBRATOR_SERVICE) as Vibrator
//        }
//        @Suppress("DEPRECATION")
//        vib = getSystemService(VIBRATOR_SERVICE) as Vibrator



        // Creating file to show the app folder in storage.
        val directoryToStore: File? = baseContext.getExternalFilesDir("MlModelsFolder")
        if (!directoryToStore!!.exists()) {
            if (directoryToStore!!.mkdir());
        }

        // Main inflation
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        val viewMain = activityMainBinding.root
        setContentView(viewMain)

        talkBackSpeaker = TalkBackSpeaker(this)

        // TODO MODULARITY
        // Toggle dev and user mode by clicking the TFL logo.
        viewMain.toolbar.setOnClickListener {
            if (viewMain.fragment_container.recyclerview_results.visibility == View.GONE) {
                viewMain.fragment_container.recyclerview_results.visibility = View.VISIBLE
                viewMain.fragment_container.bottom_sheet_layout.visibility = View.VISIBLE
                viewMain.fragment_container.wrap_content.visibility = View.VISIBLE


            } else if (viewMain.fragment_container.
                recyclerview_results.visibility == View.VISIBLE) {
                viewMain.fragment_container.recyclerview_results.visibility = View.GONE
                viewMain.fragment_container.bottom_sheet_layout.visibility = View.GONE
                viewMain.fragment_container.wrap_content.visibility = View.GONE
            }
            // TODO else exceptions here, any test?
        }
    }

    // TODO Finish on good smartphone.
//    fun vibrateDevice() {
//        // TODO does it need test, or else, or exception etc.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            vib.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.EFFECT_DOUBLE_CLICK))
//        } else {
//            @Suppress("DEPRECATION")
//            vib.vibrate(25)
//        }
//    }

    // Vibrates the device for 100 milliseconds.
//    fun vibrateDevice(context: Context) {
//        val vibrator = getSystemService(context, Vibrator::class.java)
//        vibrator?.let {
//            if (Build.VERSION.SDK_INT >= 26) {
//                it.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
//            } else {
//                @Suppress("DEPRECATION")
//                it.vibrate(100)
//            }
//        }
//    }


    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // Workaround for Android Q memory leak issue in IRequestFinishCallback$Stub.
            // (https://issuetracker.google.com/issues/139738913)
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        // TextToSpeech must be stopped before closing the app.
        talkBackSpeaker.stop()

        super.onDestroy()
    }
}
