/*
 * Copyright (c) 2026 Nafis Kabbo
 *
 * SPDX-License-Identifier: MIT
 *
 * Heading math based on Android platform SensorManager documentation:
 * - getRotationMatrixFromVector / getRotationMatrix / getOrientation
 * - remapCoordinateSystem for display rotation
 * - remapCoordinateSystem(inR, AXIS_X, AXIS_Z, outR) for camera / AR heading
 */

package com.hemanthraj.fluttercompass.heading

import android.hardware.SensorManager

/**
 * Computes UI and camera compass bearings from device sensors.
 *
 * Uses only public Android [SensorManager] APIs (not vendor “default” orientation
 * helpers), so remapping stays explicit and consistent across devices.
 */
object BearingCalculator {
    private const val AZIMUTH_INDEX = 0
    private const val ORIENTATION_SIZE = 3
    private const val MATRIX_SIZE = 9

    /**
     * Bearings from a rotation-vector sensor sample
     * (`Sensor.TYPE_ROTATION_VECTOR`).
     */
    fun fromRotationVector(
        attitude: SensorAttitude,
        orientation: ScreenOrientation,
    ): DualBearing {
        val matrix = FloatArray(MATRIX_SIZE)
        SensorManager.getRotationMatrixFromVector(matrix, attitude.toRotationVectorArray())
        return dualFromWorldMatrix(matrix, orientation)
    }

    /**
     * Bearings from accelerometer + magnetometer when rotation vector is unavailable.
     * Returns null if [SensorManager.getRotationMatrix] fails.
     */
    fun fromAccelAndMag(
        accelerometer: FloatArray,
        magnetometer: FloatArray,
        orientation: ScreenOrientation,
    ): DualBearing? {
        val matrix = FloatArray(MATRIX_SIZE)
        val inclination = FloatArray(MATRIX_SIZE)
        val ok = SensorManager.getRotationMatrix(
            matrix,
            inclination,
            accelerometer,
            magnetometer,
        )
        if (!ok) return null
        return dualFromWorldMatrix(matrix, orientation)
    }

    private fun dualFromWorldMatrix(
        worldMatrix: FloatArray,
        orientation: ScreenOrientation,
    ): DualBearing {
        val ui = bearingFromMatrix(remapForUi(worldMatrix, orientation))
        val camera = bearingFromMatrix(remapForCamera(worldMatrix))
        return DualBearing(forUi = ui, forCamera = camera)
    }

    private fun bearingFromMatrix(rotationMatrix: FloatArray): CompassBearing {
        val orientationRadians = FloatArray(ORIENTATION_SIZE)
        SensorManager.getOrientation(rotationMatrix, orientationRadians)
        val degrees = Math.toDegrees(orientationRadians[AZIMUTH_INDEX].toDouble()).toFloat()
        return CompassBearing.fromDegrees(degrees)
    }

    /**
     * Display-aware remap so [SensorManager.getOrientation] azimuth matches the
     * top edge of the screen (UI compass). Axis pairs follow Android docs for
     * [Surface] rotation.
     */
    private fun remapForUi(
        rotationMatrix: FloatArray,
        orientation: ScreenOrientation,
    ): FloatArray = when (orientation) {
        ScreenOrientation.Natural ->
            remap(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y)
        ScreenOrientation.Rotated90 ->
            remap(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X)
        ScreenOrientation.Rotated180 ->
            remap(rotationMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y)
        ScreenOrientation.Rotated270 ->
            remap(rotationMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X)
    }

    /**
     * Camera / AR remap: heading of the direction out the back of the device.
     * Android docs example: `remapCoordinateSystem(inR, AXIS_X, AXIS_Z, outR)`.
     */
    private fun remapForCamera(rotationMatrix: FloatArray): FloatArray =
        remap(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z)

    private fun remap(rotationMatrix: FloatArray, newX: Int, newY: Int): FloatArray {
        val out = FloatArray(MATRIX_SIZE)
        SensorManager.remapCoordinateSystem(rotationMatrix, newX, newY, out)
        return out
    }
}
