package com.hemanthraj.fluttercompass.model

import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class RotationVectorTest {

    @Test
    fun `test toArray converts fields accurately`() {
        val vector = RotationVector(0.1f, -0.5f, 0.9f)
        assertEquals(0.1f, vector.x)
        assertEquals(-0.5f, vector.y)
        assertEquals(0.9f, vector.z)

        val array = vector.toArray()
        assertContentEquals(floatArrayOf(0.1f, -0.5f, 0.9f), array)
    }

    @Test
    fun `test data class equality and copy`() {
        val v1 = RotationVector(1f, 2f, 3f)
        val v2 = RotationVector(1f, 2f, 3f)
        val v3 = v1.copy(z = 4f)

        assertEquals(v1, v2)
        assertEquals(v1.hashCode(), v2.hashCode())
        assertEquals(4f, v3.z)
    }
}
