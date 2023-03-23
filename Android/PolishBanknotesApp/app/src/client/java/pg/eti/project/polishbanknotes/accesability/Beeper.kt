package pg.eti.project.polishbanknotes.accesability

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.media.ToneGenerator


class Beeper(var context: Context) {
    private var am: AudioManager?
    private var volumeLevel: Int
    private var toneGen: ToneGenerator

    init {
        am = context.getSystemService(AUDIO_SERVICE) as AudioManager?
        volumeLevel = am?.getStreamVolume(AudioManager.STREAM_MUSIC)!!
        toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, volumeLevel)
    }

    fun beep() {
        volumeLevel = am?.getStreamVolume(AudioManager.STREAM_MUSIC)!!
        toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, volumeLevel)
        toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 300)
    }
}