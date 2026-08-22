package com.hemanthraj.fluttercompass.util

import android.hardware.SensorManager
import com.hemanthraj.fluttercompass.model.Azimuth
import com.hemanthraj.fluttercompass.model.DisplayRotation
import com.hemanthraj.fluttercompass.model.RotationVector

/**
 * Additional heading helpers (camera / AR remapping and accel+mag matrices).
 *
 * Kept separate from [MathUtils] so that file stays unchanged.
 */
object HeadingUtils {
    private const val AZIMUTH = 0
    private const val AXIS_SIZE = 3
    private const val ROTATION_MATRIX_SIZE = 9

    /**
     * Heading out the back of the device (rear camera / AR), per Android docs:
     * `remapCoordinateSystem(inR, AXIS_X, AXIS_Z, outR)`.
     */
    @JvmStatic
    fun calculateCameraAzimuth(rotationVector: RotationVector): Azimuth {
        val rotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector.toArray())
        return azimuthFromMatrix(remapForCamera(rotationMatrix))
    }

    /**
     * UI + camera headings from accelerometer + magnetometer when rotation vector
     * is unavailable. Returns null if [SensorManager.getRotationMatrix] fails.
     */
    @JvmStatic
    fun calculateAzimuthsFromAccelMag(
        accelerometerReading: FloatArray,
        magnetometerReading: FloatArray,
        displayRotation: DisplayRotation,
    ): Pair<Azimuth, Azimuth>? {
        val rotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)
        val inclinationMatrix = FloatArray(ROTATION_MATRIX_SIZE)
        val ok = SensorManager.getRotationMatrix(
            rotationMatrix,
            inclinationMatrix,
            accelerometerReading,
            magnetometerReading,
        )
        if (!ok) return null

        val ui = azimuthFromMatrix(remapForDisplay(rotationMatrix, displayRotation))
        val camera = azimuthFromMatrix(remapForCamera(rotationMatrix))
        return ui to camera
    }

    private fun azimuthFromMatrix(rotationMatrix: FloatArray): Azimuth {
        val orientationInRadians = SensorManager.getOrientation(rotationMatrix, FloatArray(AXIS_SIZE))
        val azimuthInDegrees = Math.toDegrees(orientationInRadians[AZIMUTH].toDouble()).toFloat()
        return Azimuth(azimuthInDegrees)
    }

    private fun remapForDisplay(rotationMatrix: FloatArray, displayRotation: DisplayRotation): FloatArray {
        return when (displayRotation) {
            DisplayRotation.ROTATION_0 ->
                remap(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y)
            DisplayRotation.ROTATION_90 ->
                remap(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X)
            DisplayRotation.ROTATION_180 ->
                remap(rotationMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y)
            DisplayRotation.ROTATION_270 ->
                remap(rotationMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X)
        }
    }

    private fun remapForCamera(rotationMatrix: FloatArray): FloatArray =
        remap(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z)

    private fun remap(rotationMatrix: FloatArray, newX: Int, newY: Int): FloatArray {
        val out = FloatArray(ROTATION_MATRIX_SIZE)
        SensorManager.remapCoordinateSystem(rotationMatrix, newX, newY, out)
        return out
    }
}
