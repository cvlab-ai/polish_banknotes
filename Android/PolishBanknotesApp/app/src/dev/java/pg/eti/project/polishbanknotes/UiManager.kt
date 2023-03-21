package pg.eti.project.polishbanknotes

import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.viewbinding.ViewBinding
import pg.eti.project.polishbanknotes.databinding.FragmentCameraBinding

/**
 * Class collects the functions that change UI significantly.
 * The class don't include data updates. Could be used to
 * switch between user and dev mode.
 * Every change should be run on UI thread (runOnUiThread)
 * in the place of call.
 */
class UiManager(private val binding: FragmentCameraBinding) {

    fun hideBottomSheetControls() {
        binding.recyclerviewResults.visibility = View.GONE
        binding.bottomSheetLayout.bottomSheetLayout.visibility = View.GONE
        binding.wrapContent.visibility = View.GONE
    }

    fun showBottomSheetControls() {
        binding.recyclerviewResults.visibility = View.VISIBLE
        binding.bottomSheetLayout.bottomSheetLayout.visibility = View.VISIBLE
        binding.wrapContent.visibility = View.VISIBLE
    }

    fun hideOtherModelsOption() {
        binding.bottomSheetLayout.spinnerModel.visibility = View.GONE // SDK could be 23
        binding.bottomSheetLayout.tvMlMode.text = "You are using latest_model.tflite!"
    }
}