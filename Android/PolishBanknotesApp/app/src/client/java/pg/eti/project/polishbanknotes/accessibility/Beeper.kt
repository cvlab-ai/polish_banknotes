package pg.eti.project.polishbanknotes.accessibility

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.media.ToneGenerator
import android.media.ToneGenerator.MAX_VOLUME
import androidx.preference.PreferenceManager


class Beeper(var context: Context) {
    private var am: AudioManager?
    private var volumeLevel: Int
    private var toneGen: ToneGenerator
    private var isActive = true

    init {
        am = context.getSystemService(AUDIO_SERVICE) as AudioManager?
        volumeLevel = am?.getStreamVolume(AudioManager.STREAM_MUSIC)!!
        toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, MAX_VOLUME)
    }

    fun checkIfEnable(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val speakerBeepOnOff = "manage_speaker_beep"

        isActive = sharedPreferences.getBoolean(speakerBeepOnOff, true)
    }

    fun getIsActive(): Boolean {
        return isActive
    }

    fun beep() {
        toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
    }

    fun doubleBeep() {
        toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 350)
    }
}