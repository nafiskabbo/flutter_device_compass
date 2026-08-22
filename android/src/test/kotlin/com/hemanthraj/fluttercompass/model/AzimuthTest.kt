package com.hemanthraj.fluttercompass.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AzimuthTest {

    @Test
    fun `test angle normalization within 0 to 360`() {
        assertEquals(0f, Azimuth(0f).degrees)
        assertEquals(90f, Azimuth(90f).degrees)
        assertEquals(180f, Azimuth(180f).degrees)
        assertEquals(270f, Azimuth(270f).degrees)
        assertEquals(0f, Azimuth(360f).degrees)
        assertEquals(10f, Azimuth(370f).degrees)
        assertEquals(0f, Azimuth(720f).degrees)
    }

    @Test
    fun `test negative angle normalization`() {
        assertEquals(270f, Azimuth(-90f).degrees)
        assertEquals(180f, Azimuth(-180f).degrees)
        assertEquals(350f, Azimuth(-10f).degrees)
        assertEquals(0f, Azimuth(-360f).degrees)
    }

    @Test
    fun `test non-finite values throw IllegalArgumentException`() {
        assertThrows<IllegalArgumentException> {
            Azimuth(Float.NaN)
        }
        assertThrows<IllegalArgumentException> {
            Azimuth(Float.POSITIVE_INFINITY)
        }
        assertThrows<IllegalArgumentException> {
            Azimuth(Float.NEGATIVE_INFINITY)
        }
    }

    @Test
    fun `test roundedDegrees rounding`() {
        assertEquals(45, Azimuth(45.4f).roundedDegrees)
        assertEquals(46, Azimuth(45.6f).roundedDegrees)
        assertEquals(0, Azimuth(359.8f).roundedDegrees)
        assertEquals(0, Azimuth(0.2f).roundedDegrees)
    }

    @Test
    fun `test cardinal direction mapping`() {
        assertEquals(CardinalDirection.NORTH, Azimuth(0f).cardinalDirection)
        assertEquals(CardinalDirection.NORTH, Azimuth(22.4f).cardinalDirection)
        assertEquals(CardinalDirection.NORTHEAST, Azimuth(22.5f).cardinalDirection)
        assertEquals(CardinalDirection.NORTHEAST, Azimuth(45f).cardinalDirection)
        assertEquals(CardinalDirection.NORTHEAST, Azimuth(67.4f).cardinalDirection)
        assertEquals(CardinalDirection.EAST, Azimuth(67.5f).cardinalDirection)
        assertEquals(CardinalDirection.EAST, Azimuth(90f).cardinalDirection)
        assertEquals(CardinalDirection.EAST, Azimuth(112.4f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTHEAST, Azimuth(112.5f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTHEAST, Azimuth(135f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTHEAST, Azimuth(157.4f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTH, Azimuth(157.5f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTH, Azimuth(180f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTH, Azimuth(202.4f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTHWEST, Azimuth(202.5f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTHWEST, Azimuth(225f).cardinalDirection)
        assertEquals(CardinalDirection.SOUTHWEST, Azimuth(247.4f).cardinalDirection)
        assertEquals(CardinalDirection.WEST, Azimuth(247.5f).cardinalDirection)
        assertEquals(CardinalDirection.WEST, Azimuth(270f).cardinalDirection)
        assertEquals(CardinalDirection.WEST, Azimuth(292.4f).cardinalDirection)
        assertEquals(CardinalDirection.NORTHWEST, Azimuth(292.5f).cardinalDirection)
        assertEquals(CardinalDirection.NORTHWEST, Azimuth(315f).cardinalDirection)
        assertEquals(CardinalDirection.NORTHWEST, Azimuth(337.4f).cardinalDirection)
        assertEquals(CardinalDirection.NORTH, Azimuth(337.5f).cardinalDirection)
        assertEquals(CardinalDirection.NORTH, Azimuth(359.9f).cardinalDirection)
    }

    @Test
    fun `test plus and minus operators`() {
        assertEquals(Azimuth(150f), Azimuth(100f) + 50f)
        assertEquals(Azimuth(350f), Azimuth(10f) - 20f)
        assertEquals(Azimuth(10f), Azimuth(350f) + 20f)
    }

    @Test
    fun `test equals and hashCode`() {
        val a1 = Azimuth(10f)
        val a2 = Azimuth(370f)
        val a3 = Azimuth(20f)

        assertEquals(a1, a2)
        assertEquals(a1.hashCode(), a2.hashCode())
        assertNotEquals(a1, a3)
    }

    @Test
    fun `test comparison`() {
        assertTrue(Azimuth(10f) < Azimuth(20f))
        assertTrue(Azimuth(350f) > Azimuth(10f))
    }

    @Test
    fun `test toString format`() {
        assertEquals("Azimuth(degrees=45.0)", Azimuth(45f).toString())
    }
}
