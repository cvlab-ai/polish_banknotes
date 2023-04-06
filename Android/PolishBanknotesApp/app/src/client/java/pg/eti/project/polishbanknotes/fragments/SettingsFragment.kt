package pg.eti.project.polishbanknotes.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.FragmentContainer
import androidx.fragment.app.findFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import pg.eti.project.polishbanknotes.R


class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSettings()
//        Log.d("PRESENT", "inicjalizacja")
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
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
        Log.d("SETTINGS", "isLabelActive: $isLabelActive")
    }
}