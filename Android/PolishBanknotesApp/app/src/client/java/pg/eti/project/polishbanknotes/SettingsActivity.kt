package pg.eti.project.polishbanknotes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import pg.eti.project.polishbanknotes.databinding.ActivitySettingsBinding


class SettingsActivity : Activity() {
    private lateinit var activitySettingsBinding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflation.
        activitySettingsBinding = ActivitySettingsBinding.inflate(layoutInflater)
        val viewSettings = activitySettingsBinding.root
        setContentView(viewSettings)

        activitySettingsBinding.goBackImageView.setOnClickListener {
            finish()
        }
    }
}