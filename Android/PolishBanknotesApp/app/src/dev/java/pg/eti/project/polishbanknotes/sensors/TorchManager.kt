package pg.eti.project.polishbanknotes.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.camera.core.Camera

// Value of illuminance (in lux) at which torch is turning on/off
// TODO Perform tests and select the best value
const val LIGHT_VALUE = 190

class TorchManager(context: Context) : SensorEventListener {
    private var sensorManager: SensorManager
    private var camera: Camera? = null
    private var lightSensor: Sensor? = null

    private var lx: Float? = null


    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        sensorManager.registerListener(this,
            lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL,
            SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        return
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_LIGHT){
            lx = event.values[0]
        }
    }

    fun registerSensorListener() {
        lightSensor?.also { light ->
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun unregisterSensorListener() {
        sensorManager.unregisterListener(this)
    }

    fun disableTorch(){
        if(camera != null)
            camera!!.cameraControl.enableTorch(false)
    }

    fun enableTorchBasedOnSensor(){
        if(camera != null)
            camera!!.cameraControl.enableTorch(lx!! < LIGHT_VALUE)
    }

    fun setCamera(cam: Camera){
        this.camera = cam
    }
}