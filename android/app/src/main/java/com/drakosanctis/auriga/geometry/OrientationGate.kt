package com.drakosanctis.auriga.geometry

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.sqrt

enum class CoarseOrientation {
    UPRIGHT,
    SIDEWAYS,
    UPSIDE_DOWN,
    INDETERMINATE,
    UNAVAILABLE
}

/**
 * Uses accelerometer gravity only to reject clearly sideways or inverted
 * device poses. It deliberately does not estimate camera pitch.
 */
class OrientationGate(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    @Volatile
    var current: CoarseOrientation = CoarseOrientation.UNAVAILABLE
        private set

    fun start(): Boolean {
        val manager = sensorManager ?: return false
        val sensor = accelerometer ?: return false
        current = CoarseOrientation.INDETERMINATE
        return manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        current = CoarseOrientation.UNAVAILABLE
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER || event.values.size < 3) return
        current = classify(event.values[0], event.values[1], event.values[2])
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        fun classify(x: Float, y: Float, z: Float): CoarseOrientation {
            val magnitude = sqrt(x * x + y * y + z * z)
            if (!magnitude.isFinite() || magnitude !in 7f..12.5f) return CoarseOrientation.INDETERMINATE
            if (abs(x) / magnitude >= 0.7f) return CoarseOrientation.SIDEWAYS
            if (y / magnitude >= 0.7f) return CoarseOrientation.UPRIGHT
            if (y / magnitude <= -0.7f) return CoarseOrientation.UPSIDE_DOWN
            return CoarseOrientation.INDETERMINATE
        }
    }
}
