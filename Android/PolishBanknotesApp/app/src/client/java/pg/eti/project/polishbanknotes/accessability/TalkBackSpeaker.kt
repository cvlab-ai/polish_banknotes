package pg.eti.project.polishbanknotes.accessability

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class TalkBackSpeaker(context: Context) {
    private lateinit var textToSpeech: TextToSpeech

    init {
        // Setting TextToSpeech (TalkBack)
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    // TODO set english language as a backup language
                    Log.e("TTS", "Language not supported")
                } else {
                    Log.i("TTS", "Language set: ${Locale.getDefault()}")
                }
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }
    }

    // Function responsible for speaking the banknotes values.
    fun speak(text: String) {
        // QUEUE_FLUSH means that speaker won't wait for the previous sentence to end.
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // Function to stop TextToSpeech service working.
    fun stop() {
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}