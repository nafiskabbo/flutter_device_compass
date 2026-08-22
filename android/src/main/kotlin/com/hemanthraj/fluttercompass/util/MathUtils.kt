/*
 * This file is part of Compass.
 * Copyright (C) 2023 Philipp Bobek <philipp.bobek@mailbox.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Compass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.hemanthraj.fluttercompass.util

import android.hardware.GeomagneticField
import android.hardware.SensorManager
import android.location.Location
import com.hemanthraj.fluttercompass.model.Azimuth
import com.hemanthraj.fluttercompass.model.DisplayRotation
import com.hemanthraj.fluttercompass.model.RotationVector
import kotlin.math.roundToInt

private const val AZIMUTH = 0
private const val AXIS_SIZE = 3
private const val ROTATION_MATRIX_SIZE = 9

object MathUtils {

    @JvmStatic
    fun calculateAzimuth(rotationVector: RotationVector, displayRotation: DisplayRotation): Azimuth {
        val rotationMatrix = getRotationMatrixFromVector(rotationVector)
        return azimuthFromMatrix(remapForDisplay(rotationMatrix, displayRotation))
    }

    /**
     * Heading out the back of the device (rear camera / AR), per Android docs:
     * `remapCoordinateSystem(inR, AXIS_X, AXIS_Z, outR)`.
     */
    @JvmStatic
    fun calculateCameraAzimuth(rotationVector: RotationVector): Azimuth {
        val rotationMatrix = getRotationMatrixFromVector(rotationVector)
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

    private fun getRotationMatrixFromVector(rotationVector: RotationVector): FloatArray {
        val rotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector.toArray())
        return rotationMatrix
    }

    private fun azimuthFromMatrix(rotationMatrix: FloatArray): Azimuth {
        val orientationInRadians = SensorManager.getOrientation(rotationMatrix, FloatArray(AXIS_SIZE))
        val azimuthInDegrees = Math.toDegrees(orientationInRadians[AZIMUTH].toDouble()).toFloat()
        return Azimuth(azimuthInDegrees)
    }

    private fun remapForDisplay(rotationMatrix: FloatArray, displayRotation: DisplayRotation): FloatArray {
        return when (displayRotation) {
            DisplayRotation.ROTATION_0 -> remapRotationMatrix(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y)
            DisplayRotation.ROTATION_90 -> remapRotationMatrix(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X)
            DisplayRotation.ROTATION_180 -> remapRotationMatrix(rotationMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y)
            DisplayRotation.ROTATION_270 -> remapRotationMatrix(rotationMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X)
        }
    }

    /** Remap so getOrientation azimuth is the rear-camera pointing direction. */
    private fun remapForCamera(rotationMatrix: FloatArray): FloatArray {
        return remapRotationMatrix(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z)
    }

    private fun remapRotationMatrix(rotationMatrix: FloatArray, newX: Int, newY: Int): FloatArray {
        val remappedRotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)
        SensorManager.remapCoordinateSystem(rotationMatrix, newX, newY, remappedRotationMatrix)
        return remappedRotationMatrix
    }

    @JvmStatic
    fun getMagneticDeclination(location: Location): Float {
        val latitude = location.latitude.toFloat()
        val longitude = location.longitude.toFloat()
        val altitude = location.altitude.toFloat()
        val time = location.time
        val geomagneticField = GeomagneticField(latitude, longitude, altitude, time)
        return geomagneticField.declination
    }

    fun getClosestNumberFromInterval(number: Float, interval: Float): Float =
        (number / interval).roundToInt() * interval

    /**
     * @see <a href="https://math.stackexchange.com/questions/2275439/check-if-point-on-circle-is-between-two-other-points-on-circle">Stackexchange</a>
     */
    fun isAzimuthBetweenTwoPoints(azimuth: Azimuth, pointA: Azimuth, pointB: Azimuth): Boolean {
        val aToB = (pointB.degrees - pointA.degrees + 360f) % 360f
        val aToAzimuth = (azimuth.degrees - pointA.degrees + 360f) % 360f
        return aToB <= 180f != aToAzimuth > aToB
    }
}
