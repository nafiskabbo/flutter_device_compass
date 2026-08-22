package com.hemanthraj.fluttercompass.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CardinalDirectionTest {

    @Test
    fun `test cardinal direction labels match expectations`() {
        assertEquals("N", CardinalDirection.NORTH.label)
        assertEquals("NE", CardinalDirection.NORTHEAST.label)
        assertEquals("E", CardinalDirection.EAST.label)
        assertEquals("SE", CardinalDirection.SOUTHEAST.label)
        assertEquals("S", CardinalDirection.SOUTH.label)
        assertEquals("SW", CardinalDirection.SOUTHWEST.label)
        assertEquals("W", CardinalDirection.WEST.label)
        assertEquals("NW", CardinalDirection.NORTHWEST.label)
    }

    @Test
    fun `test all enum values count is 8`() {
        assertEquals(8, CardinalDirection.values().size)
    }
}
