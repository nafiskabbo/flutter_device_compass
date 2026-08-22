package com.hemanthraj.fluttercompass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.os.SystemClock
import android.util.Log
import android.view.Display
import android.view.Surface
import com.hemanthraj.fluttercompass.model.DisplayRotation
import com.hemanthraj.fluttercompass.model.RotationVector
import com.hemanthraj.fluttercompass.util.CompassHelper
import com.hemanthraj.fluttercompass.util.CompassHelper.calculateHeading
import com.hemanthraj.fluttercompass.util.CompassHelper.convertRadtoDeg
import com.hemanthraj.fluttercompass.util.CompassHelper.map180to360
import com.hemanthraj.fluttercompass.util.HeadingUtils
import com.hemanthraj.fluttercompass.util.MathUtils
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class FlutterCompassPlugin : FlutterPlugin, EventChannel.StreamHandler, MethodChannel.MethodCallHandler {
    private var compassSensorEventListener: SensorEventListener? = null
    private var display: Display? = null
    private var sensorManager: SensorManager? = null
    private var rotationSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var magneticFieldSensor: Sensor? = null
    private val truncatedRotationVectorValue = FloatArray(4)
    private var lastAccuracySensorStatus = 0
    private var compassUpdateNextTimestamp: Long = 0
    private var accelerometerReading = FloatArray(3)
    private var magneticReading = FloatArray(3)
    private var channel: EventChannel? = null
    private var methodChannel: MethodChannel? = null

    private val isCompassSensorAvailable: Boolean
        get() = rotationSensor != null

    // New Plugin APIs
    override fun onAttachedToEngine(binding: FlutterPluginBinding) {
        channel = EventChannel(binding.binaryMessenger, EVENT_NAME)
        methodChannel = MethodChannel(binding.binaryMessenger, METHOD_NAME)
        getSensors(binding.applicationContext)
        channel?.setStreamHandler(this)
        methodChannel?.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
        unregisterListener()
        cleanSensors()
        channel?.setStreamHandler(null)
        methodChannel?.setMethodCallHandler(null)
        channel = null
        methodChannel = null
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "hasSensors" -> {
                val available = rotationSensor != null ||
                        (accelerometerSensor != null && magneticFieldSensor != null)
                result.success(available)
            }
            else -> result.notImplemented()
        }
    }

    override fun onListen(arguments: Any?, events: EventSink?) {
        events?.let {
            unregisterListener()
            compassSensorEventListener = CompassSensorEventListener(events)
            registerListener()
        }
    }

    override fun onCancel(arguments: Any?) {
        unregisterListener()
        compassSensorEventListener = null
    }

    private fun getSensors(context: Context) {
        display = (context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager)
            ?.getDisplay(Display.DEFAULT_DISPLAY)
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationSensor == null) {
            Log.d(
                TAG, "Rotation vector sensor not supported on device, "
                        + "falling back to accelerometer and magnetic field."
            )
            accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magneticFieldSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        }
    }

    private fun cleanSensors() {
        sensorManager = null
        display = null
        rotationSensor = null
        accelerometerSensor = null
        magneticFieldSensor = null
    }

    private fun registerListener() {
        val sm = sensorManager ?: return
        val listener = compassSensorEventListener ?: return
        if (isCompassSensorAvailable) {
            sm.registerListener(listener, rotationSensor, SENSOR_DELAY_MICROS)
        } else {
            sm.registerListener(listener, accelerometerSensor, SENSOR_DELAY_MICROS)
            sm.registerListener(listener, magneticFieldSensor, SENSOR_DELAY_MICROS)
        }
    }

    private fun unregisterListener() {
        val sm = sensorManager ?: return
        val listener = compassSensorEventListener ?: return
        sm.unregisterListener(listener)
    }

    private inner class CompassSensorEventListener(val eventSink: EventSink) : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (lastAccuracySensorStatus == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                Log.d(TAG, "Compass sensor is unreliable, device calibration is needed.")
            }
            when {
                event.sensor.type == Sensor.TYPE_ROTATION_VECTOR -> {
                    val rotationVectorReading = getRotationVectorFromSensorEvent(event)
                    updateRotationCompass(rotationVectorReading)
                    return
                }

                event.sensor.type == Sensor.TYPE_ACCELEROMETER && !isCompassSensorAvailable -> {
                    CompassHelper.lowPassFilter(event.values.clone(), accelerometerReading)
                    updateHeading()
                }

                event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD && !isCompassSensorAvailable -> {
                    CompassHelper.lowPassFilter(event.values.clone(), magneticReading)
                    updateHeading()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            when (sensor.type) {
                Sensor.TYPE_MAGNETIC_FIELD -> if (lastAccuracySensorStatus != accuracy) {
                    lastAccuracySensorStatus = accuracy
                }

                Sensor.TYPE_ROTATION_VECTOR -> {
                    if (lastAccuracySensorStatus != accuracy) {
                        lastAccuracySensorStatus = accuracy
                    }
                    Log.v(TAG, "Received rotation vector sensor accuracy $accuracy")
                }

                else -> Log.w(TAG, "Unexpected accuracy changed event of type ${sensor.type}")
            }
        }

        private fun updateHeading() {
            val displayRotation = getDisplayRotation()
            val azimuths = HeadingUtils.calculateAzimuthsFromAccelMag(
                accelerometerReading,
                magneticReading,
                displayRotation,
            )
            if (azimuths != null) {
                notifyCompassChangeListeners(
                    azimuths.first.degrees.toDouble(),
                    azimuths.second.degrees.toDouble(),
                )
                return
            }

            // Fallback if getRotationMatrix fails: UI heading only; camera matches UI.
            var heading = calculateHeading(accelerometerReading, magneticReading)
            heading = convertRadtoDeg(heading)
            heading = map180to360(heading)
            notifyCompassChangeListeners(heading.toDouble(), heading.toDouble())
        }

        private fun updateRotationCompass(rotationVectorValue: FloatArray) {
            // Some devices occasionally deliver NaN rotation-vector samples.
            // Azimuth's init block throws IllegalArgumentException for
            // non-finite degrees, which escapes onSensorChanged on the main
            // thread and crashes the host app. The NaN check further down in
            // notifyCompassChangeListeners runs too late to prevent the
            // throw, so skip non-finite samples before computing the azimuth.
            if (!rotationVectorValue[0].isFinite() ||
                !rotationVectorValue[1].isFinite() ||
                !rotationVectorValue[2].isFinite()
            ) {
                return
            }
            val rotationVector = RotationVector(rotationVectorValue[0], rotationVectorValue[1], rotationVectorValue[2])
            val displayRotation = getDisplayRotation()
            val uiAzimuth = MathUtils.calculateAzimuth(rotationVector, displayRotation)
            val cameraAzimuth = HeadingUtils.calculateCameraAzimuth(rotationVector)

            notifyCompassChangeListeners(
                uiAzimuth.degrees.toDouble(),
                cameraAzimuth.degrees.toDouble(),
            )
        }

        private fun getDisplayRotation(): DisplayRotation {
            val rotation = display?.rotation ?: Surface.ROTATION_0
            return when (rotation) {
                Surface.ROTATION_90 -> DisplayRotation.ROTATION_90
                Surface.ROTATION_180 -> DisplayRotation.ROTATION_180
                Surface.ROTATION_270 -> DisplayRotation.ROTATION_270
                else -> DisplayRotation.ROTATION_0
            }
        }

        private fun notifyCompassChangeListeners(degrees: Double, cameraDegrees: Double) {
            if (degrees.isNaN() || cameraDegrees.isNaN()) {
                return
            }

            val now = SystemClock.elapsedRealtime()
            if (now < compassUpdateNextTimestamp) {
                return
            }
            compassUpdateNextTimestamp = now + COMPASS_UPDATE_INTERVAL_MS

            val data = DoubleArray(3)
            data[0] = degrees
            data[1] = cameraDegrees
            data[2] = accuracy.toDouble()

            eventSink.success(data)
        }

        private val accuracy: Int
            get() = when (lastAccuracySensorStatus) {
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> 15
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> 30
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> 45
                else -> -1
            }

        /**
         * Pulls out the rotation vector from a SensorEvent, with a maximum length
         * vector of four elements to avoid potential compatibility issues.
         *
         * @param event the sensor event
         * @return the events rotation vector, potentially truncated
         */
        private fun getRotationVectorFromSensorEvent(event: SensorEvent): FloatArray {
            return if (event.values.size > 4) {
                // On some Samsung devices SensorManager.getRotationMatrixFromVector
                // appears to throw an exception if rotation vector has length > 4.
                // For the purposes of this class the first 4 values of the
                // rotation vector are sufficient (see crbug.com/335298 for details).
                // Only affects Android 4.3
                System.arraycopy(event.values, 0, truncatedRotationVectorValue, 0, 4)
                truncatedRotationVectorValue
            } else {
                event.values
            }
        }
    }

    companion object {
        private const val TAG = "FlutterCompass"
        private const val EVENT_NAME = "hemanthraj/flutter_compass"
        private const val METHOD_NAME = "hemanthraj/flutter_compass/methods"

        // Hint to the system; actual delivery may be faster or slower.
        // ~30 Hz. Throttled again in notifyCompassChangeListeners.
        private const val SENSOR_DELAY_MICROS = 30 * 1000
        private const val COMPASS_UPDATE_INTERVAL_MS = 33L
    }
}
