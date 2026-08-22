package com.hemanthraj.fluttercompass.heading

import android.view.Surface
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CompassBearingTest {
    @Test
    fun `normalizes degrees into 0 until 360`() {
        assertEquals(0f, CompassBearing.fromDegrees(0f).degrees)
        assertEquals(90f, CompassBearing.fromDegrees(90f).degrees)
        assertEquals(0f, CompassBearing.fromDegrees(360f).degrees)
        assertEquals(10f, CompassBearing.fromDegrees(370f).degrees)
        assertEquals(270f, CompassBearing.fromDegrees(-90f).degrees)
        assertEquals(350f, CompassBearing.fromDegrees(-10f).degrees)
    }

    @Test
    fun `rejects non-finite degrees`() {
        assertThrows<IllegalArgumentException> { CompassBearing.fromDegrees(Float.NaN) }
        assertThrows<IllegalArgumentException> {
            CompassBearing.fromDegrees(Float.POSITIVE_INFINITY)
        }
    }
}

class SensorAttitudeTest {
    @Test
    fun `rejects non-finite components`() {
        assertThrows<IllegalArgumentException> {
            SensorAttitude(Float.NaN, 0f, 0f)
        }
    }

    @Test
    fun `exposes rotation vector array`() {
        val attitude = SensorAttitude(0.1f, 0.2f, 0.3f)
        assertEquals(listOf(0.1f, 0.2f, 0.3f), attitude.toRotationVectorArray().toList())
    }
}

class ScreenOrientationTest {
    @Test
    fun `maps surface rotation constants`() {
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
}
