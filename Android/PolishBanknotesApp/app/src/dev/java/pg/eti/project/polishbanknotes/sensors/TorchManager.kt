package pg.eti.project.polishbanknotes.sensors

import android.content.Context
import androidx.camera.core.Camera

class TorchManager(context: Context) {

    fun disableTorch(camera: Camera?){
        camera?.cameraControl?.enableTorch(false)
    }

    fun enableTorch(camera: Camera?){
        camera?.cameraControl?.enableTorch(true)
    }
}