/*
 * Copyright (c) 2026 Nafis Kabbo
 *
 * SPDX-License-Identifier: MIT
 */

package com.hemanthraj.fluttercompass.util

import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Lightweight sensor smoothing and a last-resort heading estimate when
 * [android.hardware.SensorManager.getRotationMatrix] is unavailable.
 */
object CompassHelper {
    /** 0 ≤ ALPHA ≤ 1 — smaller is smoother but slower to react. */
    private const val ALPHA = 0.15f

    fun calculateHeading(accelerometerReading: FloatArray, magnetometerReading: FloatArray): Float {
        var ax = accelerometerReading[0]
        var ay = accelerometerReading[1]
        var az = accelerometerReading[2]
        val ex = magnetometerReading[0]
        val ey = magnetometerReading[1]
        val ez = magnetometerReading[2]

        var hx = ey * az - ez * ay
        var hy = ez * ax - ex * az
        var hz = ex * ay - ey * ax

        val invH = 1.0f / sqrt((hx * hx + hy * hy + hz * hz).toDouble()).toFloat()
        hx *= invH
        hy *= invH
        hz *= invH

        val invA = 1.0f / sqrt((ax * ax + ay * ay + az * az).toDouble()).toFloat()
        ax *= invA
        ay *= invA
        az *= invA

        val my = az * hx - ax * hz
        return atan2(hy.toDouble(), my.toDouble()).toFloat()
    }

    fun convertRadtoDeg(rad: Float): Float = (rad / Math.PI).toFloat() * 180f

    fun map180to360(angle: Float): Float = (angle + 360f) % 360f

    fun lowPassFilter(input: FloatArray, output: FloatArray?): FloatArray {
        if (output == null) return input
        for (i in input.indices) {
            output[i] = output[i] + ALPHA * (input[i] - output[i])
        }
        return output
    }
}
