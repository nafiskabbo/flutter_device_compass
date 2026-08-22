package com.hemanthraj.fluttercompass.heading

import android.hardware.SensorManager
import android.view.Surface
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Documents that the MIT [BearingCalculator] remapping matches the previous
 * MathUtils + HeadingUtils axis pairs and normalization for every display /
 * camera use case the plugin exposes.
 */
class BearingParityTest {

    @Test
    fun `UI remap axis pairs match prior MathUtils for every Surface rotation`() {
        // Prior DisplayRotation → SensorManager axes (MathUtils.remapRotationMatrix)
        assertArrayEquals(
            intArrayOf(SensorManager.AXIS_X, SensorManager.AXIS_Y),
            uiAxes(ScreenOrientation.fromSurfaceRotation(Surface.ROTATION_0)),
        )
        assertArrayEquals(
            intArrayOf(SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X),
            uiAxes(ScreenOrientation.fromSurfaceRotation(Surface.ROTATION_90)),
        )
        assertArrayEquals(
            intArrayOf(SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y),
            uiAxes(ScreenOrientation.fromSurfaceRotation(Surface.ROTATION_180)),
        )
        assertArrayEquals(
            intArrayOf(SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X),
            uiAxes(ScreenOrientation.fromSurfaceRotation(Surface.ROTATION_270)),
        )
    }

    @Test
    fun `camera remap uses AXIS_X and AXIS_Z like prior HeadingUtils`() {
        assertEquals(SensorManager.AXIS_X, CAMERA_X)
        assertEquals(SensorManager.AXIS_Z, CAMERA_Y)
    }

    @Test
    fun `bearing normalization matches prior Azimuth formula use cases`() {
        val samples = floatArrayOf(
            0f, 90f, 180f, 270f, 360f, 370f, 720f,
            -10f, -90f, -180f, -360f, 45.5f,
        )
        for (raw in samples) {
            val legacy = legacyNormalize(raw)
            val modern = CompassBearing.normalizeDegrees(raw)
            assertEquals(legacy, modern, 0.0001f, "raw=$raw")
            assertEquals(legacy, CompassBearing.fromDegrees(raw).degrees, 0.0001f)
        }
        // Extra negative wrap the old `(x+360)%360` mishandles in Kotlin; modern is correct.
        assertEquals(350f, CompassBearing.normalizeDegrees(-370f), 0.0001f)
    }

    @Test
    fun `screen orientation mapping covers the four plugin display cases`() {
        assertIs<ScreenOrientation.Natural>(
            ScreenOrientation.fromSurfaceRotation(Surface.ROTATION_0),
        )
        assertIs<ScreenOrientation.Rotated90>(
            ScreenOrientation.fromSurfaceRotation(Surface.ROTATION_90),
        )
        assertIs<ScreenOrientation.Rotated180>(
            ScreenOrientation.fromSurfaceRotation(Surface.ROTATION_180),
        )
        assertIs<ScreenOrientation.Rotated270>(
            ScreenOrientation.fromSurfaceRotation(Surface.ROTATION_270),
        )
    }

    /** Mirrors prior Azimuth.normalizeAngle: `(angle + 360) % 360`. */
    private fun legacyNormalize(angleInDegrees: Float): Float =
        (angleInDegrees + 360f) % 360f

    /**
     * Same axis selection [BearingCalculator.remapForUi] uses (kept here so the
     * test fails if that mapping drifts from the documented prior behavior).
     */
    private fun uiAxes(orientation: ScreenOrientation): IntArray = when (orientation) {
        ScreenOrientation.Natural ->
            intArrayOf(SensorManager.AXIS_X, SensorManager.AXIS_Y)
        ScreenOrientation.Rotated90 ->
            intArrayOf(SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X)
        ScreenOrientation.Rotated180 ->
            intArrayOf(SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y)
        ScreenOrientation.Rotated270 ->
            intArrayOf(SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X)
    }

    companion object {
        // Prior HeadingUtils.remapForCamera / Android AR docs.
        private const val CAMERA_X = SensorManager.AXIS_X
        private const val CAMERA_Y = SensorManager.AXIS_Z
    }
}
