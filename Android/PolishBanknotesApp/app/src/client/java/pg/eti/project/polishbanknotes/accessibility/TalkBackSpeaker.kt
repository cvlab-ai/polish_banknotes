package pg.eti.project.polishbanknotes.accessibility

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.preference.PreferenceManager
import java.util.*

class TalkBackSpeaker(context: Context) {
    private lateinit var textToSpeech: TextToSpeech
    private var isActive = true

    init {
        // Setting TextToSpeech (TalkBack)
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED
                ) { // Set polish language as a backup language
                    val polishLocale = Locale("pl", "PL")
                    val polishResult = textToSpeech.setLanguage(polishLocale)
                    if (polishResult == TextToSpeech.LANG_MISSING_DATA
                        || polishResult == TextToSpeech.LANG_NOT_SUPPORTED
                    ) {
//                        Log.e("TTS", "Language not supported")
                    } else {
//                        Log.i("TTS", "Language set: $englishLocale (backup)")
                    }
                } else {
//                    Log.i("TTS", "Language set: ${Locale.getDefault()}")
                }
            } else {
//                Log.e("TTS", "Initialization failed")
            }
        }
    }

    fun checkIfEnable(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val speakerOnOff = "manage_speaker"

        isActive = sharedPreferences.getBoolean(speakerOnOff, true)
    }

    fun getIsActive(): Boolean {
        return isActive
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