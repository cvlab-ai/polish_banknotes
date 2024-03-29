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
    private val haptizerTimings = longArrayOf(1000, 35)

    init {
        // Setting the haptizer.
        haptizer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val haptizerManager =
                context.getSystemService(VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            haptizerManager!!.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    /**
     * Function to start cyclic vibrations with the Snow Cone (12.0: api 31) or higher android
     * version.
     */
    fun startSnowCone() {
        // TODO API: change after testing on my MAXCOM
        haptizer.let {
            it.vibrate(VibrationEffect.createWaveform(haptizerTimings, 0))
        }
    }

    /**
     * Function purpose id to make single vibration for below Snow Cone (12.0: api 31) android
     * version.
     */
    fun vibrateShot() {
        haptizer.let {
            @Suppress("DEPRECATION")
            it.vibrate(40)
        }
    }

    // Function to stop TextToSpeech service working.
    fun stop() {
        haptizer.cancel()
    }
}