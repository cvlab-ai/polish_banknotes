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

import android.os.*
import androidx.appcompat.app.AppCompatActivity
import pg.eti.project.polishbanknotes.accesability.Haptizer
import pg.eti.project.polishbanknotes.accesability.TalkBackSpeaker
import pg.eti.project.polishbanknotes.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding

    // TODO SCOPE?
    lateinit var talkBackSpeaker: TalkBackSpeaker
    lateinit var haptizer: Haptizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Creating file to show the app folder in storage.
        val directoryToStore: File? = baseContext.getExternalFilesDir("MlModelsFolder")
        if (!directoryToStore!!.exists()) {
            if (directoryToStore!!.mkdir());
        }

        // Main inflation.
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        val viewMain = activityMainBinding.root
        setContentView(viewMain)

        // Accessibility features initialization.
        talkBackSpeaker = TalkBackSpeaker(this)
        haptizer = Haptizer(this)
    }

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
        // TextToSpeech service must be stopped before closing the app.
        talkBackSpeaker.stop()

        // Stopping the haptizer service.
        haptizer.stop()

        super.onDestroy()
    }
}
