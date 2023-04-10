package pg.eti.project.polishbanknotes.settings_management

import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.graphics.Color
import android.util.Log
import android.view.View.LAYER_TYPE_SOFTWARE
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import pg.eti.project.polishbanknotes.R
import pg.eti.project.polishbanknotes.databinding.FragmentCameraBinding

const val DEFAULT_PREFERENCES_FLAG = "default_preferences_flag"
const val SMALL_LABEL_SIZE = 100F
const val NORMAL_LABEL_SIZE = 150F
const val BIGGEST_LABEL_SIZE = 200F
const val SHOW_LABEL_MILLIS_DEFAULT = 1200L

class LabelManager {
    private var isActive = true
    private var showLabelMillis: Long = SHOW_LABEL_MILLIS_DEFAULT

    /**
     * If device has enabled TalkBack we can assume that it is used by blind person, so label
     * should be turned off at start.
     */
    fun checkIfEnable(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val labelOnOffKey = "label_on_off"

        // USE-CASE #1: If user changed the option in settings.
        isActive = sharedPreferences.getBoolean(labelOnOffKey, true)

        // USE-CASE #2: On first start of the app check if TalkBack is enabled.
        val defaultPreferencesFlag = sharedPreferences.getBoolean(DEFAULT_PREFERENCES_FLAG, false)

        // If default preferences have not been set, set then
        if (!defaultPreferencesFlag) {
            val am = context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager?

            // Set your default preferences here using SharedPreferences.Editor
            val sharedPreferencesEditor = sharedPreferences.edit()
            if (am!!.isTouchExplorationEnabled) {
                isActive = false
                sharedPreferencesEditor.putBoolean(labelOnOffKey, false)
                Log.d("LABEL_MANAGER", "TalkBack ON")
            } else {
                sharedPreferencesEditor.putBoolean(labelOnOffKey, true)
                Log.d("LABEL_MANAGER", "TalkBack OFF")
            }

            // Update the flag to indicate that default preferences have been set
            sharedPreferencesEditor.putBoolean(DEFAULT_PREFERENCES_FLAG, true)

            sharedPreferencesEditor.apply()
        }
    }

    fun getIsActive(): Boolean {
        return isActive
    }

    fun getsShowLabelMillis(): Long {
        return showLabelMillis
    }

    fun updateAppearance(context: Context, fcb: FragmentCameraBinding) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        // Label text size.
        when (sharedPreferences.getString("label_size", "150")) {
            "100" -> fcb.labelTextView.textSize = SMALL_LABEL_SIZE
            "150" -> fcb.labelTextView.textSize = NORMAL_LABEL_SIZE
            "200" -> fcb.labelTextView.textSize = BIGGEST_LABEL_SIZE
            else -> fcb.labelTextView.textSize = NORMAL_LABEL_SIZE
        }


        // Label outline color and size.
        val labelOutlineColorInt = sharedPreferences.getInt(
                "label_outline_color",
                Color.parseColor("#FFFFFFFF")
            )
        val labelOutlineColorString = Integer.toHexString(labelOutlineColorInt)

        val labelOutlineSize = sharedPreferences.getInt("label_outline_size", 10).toFloat()

        val shadowColor = Color.parseColor("#$labelOutlineColorString")
        val shadowRadius = labelOutlineSize
        val shadowDx = labelOutlineSize
        val shadowDy = labelOutlineSize

        // Set the shadow layer using setLayerType() method
        // Enable software layer for TextView
        fcb.labelTextView.setLayerType(LAYER_TYPE_SOFTWARE, null)
        fcb.labelTextView.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor)

        // Set the text color
        val labelTextColorInt = sharedPreferences.getInt("label_stroke_color", 0)
        fcb.labelTextView.setTextColor(labelTextColorInt)

        // Set show label time.
        showLabelMillis = sharedPreferences.getInt(
            "label_show_time",
            SHOW_LABEL_MILLIS_DEFAULT.toInt()
        ).toLong()
    }
}