package pg.eti.project.polishbanknotes.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.navigation.Navigation
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import pg.eti.project.polishbanknotes.R

class SettingsFragment : PreferenceFragmentCompat() {

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
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val manageTorch = sp.getString("manage_torch", "")
        Log.d("SETTINGS", "TORCH: $manageTorch")
    }
}