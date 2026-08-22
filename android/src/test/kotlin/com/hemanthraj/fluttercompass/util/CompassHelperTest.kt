package com.hemanthraj.fluttercompass.util

import org.junit.jupiter.api.Test
import kotlin.math.PI
import kotlin.test.assertEquals

class CompassHelperTest {

    @Test
    fun `test convertRadtoDeg conversion accuracy`() {
        assertEquals(0f, CompassHelper.convertRadtoDeg(0f), 0.0001f)
        assertEquals(180f, CompassHelper.convertRadtoDeg(PI.toFloat()), 0.001f)
        assertEquals(90f, CompassHelper.convertRadtoDeg((PI / 2).toFloat()), 0.001f)
        assertEquals(-90f, CompassHelper.convertRadtoDeg((-PI / 2).toFloat()), 0.001f)
    }

    @Test
    fun `test map180to360 maps angles properly`() {
        assertEquals(0f, CompassHelper.map180to360(0f))
        assertEquals(90f, CompassHelper.map180to360(90f))
        assertEquals(180f, CompassHelper.map180to360(180f))
        assertEquals(270f, CompassHelper.map180to360(-90f))
        assertEquals(180f, CompassHelper.map180to360(-180f))
        assertEquals(350f, CompassHelper.map180to360(-10f))
    }

    @Test
    fun `test lowPassFilter with null output returns input directly`() {
        val input = floatArrayOf(1f, 2f, 3f)
        val result = CompassHelper.lowPassFilter(input, null)
        assertEquals(input, result)
    }

    @Test
    fun `test lowPassFilter applies smoothing correctly`() {
        val input = floatArrayOf(10f, 20f, 30f)
        val output = floatArrayOf(0f, 0f, 0f)
        val filtered = CompassHelper.lowPassFilter(input, output)

        // ALPHA is 0.15f -> output + 0.15 * (10 - 0) = 1.5
        assertEquals(1.5f, filtered[0], 0.001f)
        assertEquals(3.0f, filtered[1], 0.001f)
        assertEquals(4.5f, filtered[2], 0.001f)
    }
}
