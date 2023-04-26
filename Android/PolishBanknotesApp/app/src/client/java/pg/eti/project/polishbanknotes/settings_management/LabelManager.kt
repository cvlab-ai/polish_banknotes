package pg.eti.project.polishbanknotes.settings_management

import android.app.Activity
import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View.LAYER_TYPE_SOFTWARE
import android.view.WindowMetrics
import android.view.accessibility.AccessibilityManager
import androidx.preference.PreferenceManager
import pg.eti.project.polishbanknotes.databinding.FragmentCameraBinding


const val DEFAULT_PREFERENCES_FLAG = "default_preferences_flag"
const val SMALL_LABEL_SIZE_DIVISOR = 3F
const val NORMAL_LABEL_SIZE_DIVISOR = 2.5F
const val BIGGEST_LABEL_SIZE_DIVISOR = 1.8F
const val SHOW_LABEL_MILLIS_DEFAULT = 1200L

class LabelManager(activity: Activity) {
    private var isActive = true
    private var showLabelMillis: Long = SHOW_LABEL_MILLIS_DEFAULT
    private var screenWidthSp = 0F

    init {
        val displayMetrics = Resources.getSystem().displayMetrics
        val screenWidthPx: Int
        val density: Float
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
            screenWidthPx = windowMetrics.bounds.width()
        } else {
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            screenWidthPx = displayMetrics.widthPixels
        }

        val spValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, screenWidthPx.toFloat(), displayMetrics)
        screenWidthSp = spValue / displayMetrics.density
    }

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
//                Log.d("LABEL_MANAGER", "TalkBack ON")
            } else {
                sharedPreferencesEditor.putBoolean(labelOnOffKey, true)
//                Log.d("LABEL_MANAGER", "TalkBack OFF")
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
            "100" -> fcb.labelTextView.textSize =
                (screenWidthSp / SMALL_LABEL_SIZE_DIVISOR)
            "150" -> fcb.labelTextView.textSize =
                (screenWidthSp / NORMAL_LABEL_SIZE_DIVISOR)
            "200" -> fcb.labelTextView.textSize =
                (screenWidthSp / BIGGEST_LABEL_SIZE_DIVISOR)
            else -> fcb.labelTextView.textSize =
                (screenWidthSp / NORMAL_LABEL_SIZE_DIVISOR)
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