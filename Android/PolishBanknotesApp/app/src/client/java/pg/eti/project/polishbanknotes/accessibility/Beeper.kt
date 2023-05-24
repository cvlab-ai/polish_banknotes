package pg.eti.project.polishbanknotes.accessibility

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import androidx.preference.PreferenceManager

const val VOLUME_MULTIPLICITY = 200

class Beeper(var context: Context) {
    private var am: AudioManager?
//    private var volumeLevel: Int
    private var toneGen: ToneGenerator
    private var isActive = true

    init {
        am = context.getSystemService(AUDIO_SERVICE) as AudioManager?
//        volumeLevel = am?.getStreamVolume(AudioManager.STREAM_MUSIC)!!
        toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, Int.MAX_VALUE)
    }

    fun checkIfEnable(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val speakerBeepOnOff = "manage_speaker_beep"

        isActive = sharedPreferences.getBoolean(speakerBeepOnOff, false)
    }

    fun getIsActive(): Boolean {
        return isActive
    }

    fun beep() {
        toneGen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 125)
        // TONE_CDMA_EMERGENCY_RINGBACK - slightly lower
        // TONE_CDMA_SOFT_ERROR_LITE - slightly higher (diff. init)
    }

    fun doubleBeep() {
        toneGen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 260)
    }
}