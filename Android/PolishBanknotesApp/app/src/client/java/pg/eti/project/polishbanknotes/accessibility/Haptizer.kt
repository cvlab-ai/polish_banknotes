package pg.eti.project.polishbanknotes.accessibility

import android.content.Context
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager

const val MILLIS_TO_HAPTIZE = 2000L

class Haptizer(context: Context) {
    private var haptizer: Vibrator
    private val haptizerTimings = longArrayOf(MILLIS_TO_HAPTIZE, 35)
    private var isActive = true

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

    fun checkIfEnable(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val manageHaptizerKey = "manage_haptizer"

        isActive = sharedPreferences.getBoolean(manageHaptizerKey, true)
    }

    fun getIsActive(): Boolean {
        return isActive
    }

    /**
     * Function to start cyclic vibrations with the Snow Cone (12.0: api 31) or higher android
     * version.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun startSnowCone() {
        // TODO API: change after testing on my MAXCOM
        haptizer.let {
            it.vibrate(VibrationEffect.createWaveform(haptizerTimings, 0))
        }
    }

    /**
     * Function purpose id to make single vibration for below Snow Cone (12.0: api 31) android
     * version.
     *
     * @return Number of milliseconds to start adding from.
     */
    fun vibrateShot(inferenceMillisSum: Long): Long {
        var millisSum = inferenceMillisSum
        if (millisSum >= MILLIS_TO_HAPTIZE){
            haptizer.let {
                @Suppress("DEPRECATION")
                it.vibrate(40)
            }

            millisSum = 0L
        }

        return millisSum
    }

    // Function to stop TextToSpeech service working.
    fun stop() {
        haptizer.cancel()
    }
}