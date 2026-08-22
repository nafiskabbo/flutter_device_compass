/*
 * Copyright (c) 2026 Nafis Kabbo
 *
 * SPDX-License-Identifier: MIT
 */

package com.hemanthraj.fluttercompass.heading

/**
 * Truncated rotation-vector sample (x, y, z) from [android.hardware.Sensor.TYPE_ROTATION_VECTOR].
 */
data class SensorAttitude(
    val x: Float,
    val y: Float,
    val z: Float,
) {
    init {
        require(x.isFinite() && y.isFinite() && z.isFinite()) {
            "SensorAttitude components must be finite (got x=$x y=$y z=$z)"
        }
    }

    fun toRotationVectorArray(): FloatArray = floatArrayOf(x, y, z)
}
