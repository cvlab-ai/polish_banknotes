package pg.eti.project.polishbanknotes.accesability

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.media.ToneGenerator
import android.media.ToneGenerator.MAX_VOLUME


class Beeper(var context: Context) {
    private var am: AudioManager?
    private var volumeLevel: Int
    private var toneGen: ToneGenerator

    init {
        am = context.getSystemService(AUDIO_SERVICE) as AudioManager?
        volumeLevel = am?.getStreamVolume(AudioManager.STREAM_MUSIC)!!
        toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, MAX_VOLUME)
    }

    fun beep() {
        toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
    }

    fun doubleBeep() {
        toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 350)
    }
}