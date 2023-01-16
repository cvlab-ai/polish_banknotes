package pg.eti.project.polishbanknotes.accesability

import android.content.Context
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat.getSystemService

class Haptizer(context: Context) {
    private var haptizer: Vibrator
    private val haptizerTimings = arrayOf<Long>(0, 1000).toLongArray()

    init {
        // Setting the haptizer.
        // TODO DEVICE SPECS: check if the device has needed options for haptic feedback.
        // TODO EXCEPTION: Do sth with this "ClassNullException".
        haptizer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val haptizerManager =
                context.getSystemService(VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            haptizerManager!!.defaultVibrator
            // TODO EXCEPTION?
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    /**
     * Function to start cyclic vibrations with the Oreo (8.0: api 26) or higher android version.
     */
    fun startOreo() {
        // TODO EXCEPTION?: does it need test, or else, or exception etc.
        haptizer.let {
            it.vibrate(VibrationEffect.createWaveform(haptizerTimings, 0))
        }
    }

    /**
     * Function purpose id to make single vibration for below Oreo (8.0: api 26) android version.
     */
    fun vibrateShot() {
        // TODO EXCEPTION?: does it need test, or else, or exception etc.
        haptizer.let {
            @Suppress("DEPRECATION")
            it.vibrate(40)
        }
    }

    // Function to stop TextToSpeech service working.
    fun stop() {
        haptizer.cancel()
        // TODO: Any return value? Catches?
    }
}