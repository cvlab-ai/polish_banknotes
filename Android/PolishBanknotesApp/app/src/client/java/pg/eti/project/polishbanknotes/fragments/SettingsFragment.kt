package pg.eti.project.polishbanknotes.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainer
import androidx.fragment.app.findFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import pg.eti.project.polishbanknotes.R
import java.util.*


class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var showTimeSeekBar: SeekBarPreference
    private lateinit var outlineWidthSeekBar: SeekBarPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSettings()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // Set listener to seekbars.
        showTimeSeekBar = findPreference("label_show_time")!!
        val sbpLabelShowTimeMin = resources.getInteger(R.integer.label_show_millis_min)
        showTimeSeekBar.min = sbpLabelShowTimeMin
        showTimeSeekBar.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {
                preference, newValue ->
            if (preference is SeekBarPreference && preference.key == "label_show_time") {
                // Handle the change in SeekBarPreference here
                val progress = newValue as Int
                val floatValue = progress / 1000.0f // Convert to float, assuming it is in millis
//                Log.d("VALUE", "progress: $progress, floatValue: $floatValue")

                // Update the preference summary with the float value
                var summary = String.format(Locale.US, "%.1f", floatValue) + " s"
                preference.summary = summary

                // Return true to indicate that the preference change should be persisted
                return@OnPreferenceChangeListener true
            }

            // Return false to indicate that the preference change should not be persisted
            false
        }

        outlineWidthSeekBar = findPreference("label_outline_size")!!
        val sbpLabelOutlineWidth = resources.getInteger(R.integer.label_show_millis_min)
        outlineWidthSeekBar.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {
                preference, newValue ->
            if (preference is SeekBarPreference && preference.key == "label_outline_size") {

                // Update the preference summary with the float value
                preference.summary = newValue.toString()

                // Return true to indicate that the preference change should be persisted
                return@OnPreferenceChangeListener true
            }

            // Return false to indicate that the preference change should not be persisted
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_fragment_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu){
        super.onPrepareOptionsMenu(menu)
        val item = menu.findItem(R.id.action_settings)
        item.isVisible = false
    }

    private fun loadSettings() {
        // Prepare to read from SharedPreferences
        val sp = PreferenceManager.getDefaultSharedPreferences(context)

        // Read label status and set switch.
        var keyString: String = getString(R.string.label_key)
        var isLabelActive = sp.getBoolean(keyString, true)
        var spcLabelOnOff = findPreference<SwitchPreferenceCompat>(keyString)
        spcLabelOnOff!!.isChecked = isLabelActive
//        Log.d("SETTINGS", "isLabelActive: $isLabelActive")

        // Prepare seek bar for label show time - summary.
        keyString = "label_show_time"
        var labelShowTimeMillis = sp.getInt(keyString, 1200).toLong()
        var sbpLabelShowTime = findPreference<SeekBarPreference>(keyString)
        sbpLabelShowTime!!.value = labelShowTimeMillis.toInt()
        val floatValue = labelShowTimeMillis / 1000.0f

        // Update the preference summary with the float value
        var summary = String.format(Locale.US, "%.1f", floatValue) + " s"
        sbpLabelShowTime.summary = summary

        // Prepare seek bar for label outline size - summary.
        keyString = "label_outline_size"
        var labelOutlineSize = sp.getInt(keyString, 10)
        var sbpLabelOutlineSize = findPreference<SeekBarPreference>(keyString)

        sbpLabelOutlineSize!!.summary = labelOutlineSize.toString()
    }
}