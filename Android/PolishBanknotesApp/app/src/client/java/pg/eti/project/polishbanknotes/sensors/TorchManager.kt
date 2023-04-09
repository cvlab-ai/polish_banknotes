package pg.eti.project.polishbanknotes.sensors

import android.graphics.Bitmap
import androidx.camera.core.Camera
import androidx.core.graphics.get
import androidx.core.graphics.luminance

const val MILLIS_TO_CHECK_TORCH = 2000L

class TorchManager() {

    companion object {
        private const val LUMINANCE_THRESHOLD = 0.3  // Relative luminance has values in range 0.0 (pure black) - 1.0 (pure white)
    }

    fun disableTorch(camera: Camera?){
        camera?.cameraControl?.enableTorch(false)
    }

    fun enableTorch(camera: Camera?){
        camera?.cameraControl?.enableTorch(true)
    }

    fun calculateBrightness(image: Bitmap, camera: Camera?, inferenceMillisCounter: Long){
        if (inferenceMillisCounter >= MILLIS_TO_CHECK_TORCH) {
            var brightness = 0.0
            for (h in 0 until image.height) {
                for (w in 0 until image.width) {
                    brightness += image[w, h].luminance
                }
            }

            val imageBrightness = brightness / (image.width * image.height)

            if (imageBrightness < LUMINANCE_THRESHOLD) {
                enableTorch(camera)
            } else {
                disableTorch(camera)
            }
        }
    }


}