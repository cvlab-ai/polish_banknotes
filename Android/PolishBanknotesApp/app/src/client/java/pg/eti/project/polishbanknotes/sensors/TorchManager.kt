package pg.eti.project.polishbanknotes.sensors

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.camera.core.Camera
import androidx.core.graphics.*
import androidx.preference.PreferenceManager
import pg.eti.project.polishbanknotes.settings_management.DEFAULT_PREFERENCES_FLAG
import kotlin.math.sqrt

const val MILLIS_TO_CHECK_TORCH = 2000L

class TorchManager {
    private var isActive = true

    companion object {
        private const val LUMINANCE_THRESHOLD = 0.3  // Relative luminance has values in range 0.0 (pure black) - 1.0 (pure white)
    }

    fun checkIfEnable(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val manageTorchKey = "manage_torch"

        Log.e("TEST", sharedPreferences.getBoolean(manageTorchKey, true).toString());
        isActive = sharedPreferences.getBoolean(manageTorchKey, true)
    }

    fun disableTorch(camera: Camera?){
        camera?.cameraControl?.enableTorch(false)
    }

    fun enableTorch(camera: Camera?){
        camera?.cameraControl?.enableTorch(true)
    }

    fun calculateBrightness(image: Bitmap, camera: Camera?, inferenceMillisCounter: Long){
        if (inferenceMillisCounter >= MILLIS_TO_CHECK_TORCH && isActive) {
            var r = 0.0
            var g = 0.0
            var b = 0.0
            var pixelCounter = 0
            for (h in 0 until image.height) {
                for (w in 0 until image.width) {
                    r += image[w, h].red
                    g += image[w, h].green
                    b += image[w, h].blue
                    pixelCounter += 1
                }
            }

            r /= pixelCounter
            g /= pixelCounter
            b /= pixelCounter

            val imageBrightness = sqrt(0.299 * (r * r) + 0.587 * (g * g) + 0.114 * (b * b))

            if (imageBrightness < LUMINANCE_THRESHOLD) {
                enableTorch(camera)
            } else {
                disableTorch(camera)
            }
        }
    }


}