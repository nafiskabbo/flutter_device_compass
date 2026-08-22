package com.hemanthraj.fluttercompass.util

import com.hemanthraj.fluttercompass.model.Azimuth
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MathUtilsTest {

    @Test
    fun `test getClosestNumberFromInterval rounds to nearest step`() {
        assertEquals(10f, MathUtils.getClosestNumberFromInterval(11.2f, 5f))
        assertEquals(15f, MathUtils.getClosestNumberFromInterval(13.5f, 5f))
        assertEquals(0f, MathUtils.getClosestNumberFromInterval(0.4f, 1f))
        assertEquals(1f, MathUtils.getClosestNumberFromInterval(0.6f, 1f))
    }

    @Test
    fun `test isAzimuthBetweenTwoPoints on normal range`() {
        val pointA = Azimuth(10f)
        val pointB = Azimuth(50f)

        assertTrue(MathUtils.isAzimuthBetweenTwoPoints(Azimuth(30f), pointA, pointB))
        assertFalse(MathUtils.isAzimuthBetweenTwoPoints(Azimuth(60f), pointA, pointB))
        assertFalse(MathUtils.isAzimuthBetweenTwoPoints(Azimuth(5f), pointA, pointB))
    }

    @Test
    fun `test isAzimuthBetweenTwoPoints wrapping across 0 degrees boundary`() {
        val pointA = Azimuth(350f)
        val pointB = Azimuth(20f)

        assertTrue(MathUtils.isAzimuthBetweenTwoPoints(Azimuth(355f), pointA, pointB))
        assertTrue(MathUtils.isAzimuthBetweenTwoPoints(Azimuth(0f), pointA, pointB))
        assertTrue(MathUtils.isAzimuthBetweenTwoPoints(Azimuth(10f), pointA, pointB))
        assertFalse(MathUtils.isAzimuthBetweenTwoPoints(Azimuth(180f), pointA, pointB))
        assertFalse(MathUtils.isAzimuthBetweenTwoPoints(Azimuth(340f), pointA, pointB))
    }
}
