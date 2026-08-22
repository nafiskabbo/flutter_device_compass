/*
 * Copyright (c) 2026 Nafis Kabbo
 *
 * SPDX-License-Identifier: MIT
 */

package com.hemanthraj.fluttercompass.heading

/**
 * Compass bearing in degrees, normalized to `[0, 360)`.
 */
@JvmInline
value class CompassBearing private constructor(val degrees: Float) {
    companion object {
        fun fromDegrees(rawDegrees: Float): CompassBearing {
            require(rawDegrees.isFinite()) {
                "Bearing must be finite but was '$rawDegrees'"
            }
            return CompassBearing(normalizeDegrees(rawDegrees))
        }

        fun normalizeDegrees(angle: Float): Float {
            var value = angle % 360f
            if (value < 0f) value += 360f
            return value
        }
    }
}

/**
 * UI (screen-up) bearing and camera (out-the-back) bearing.
 */
data class DualBearing(
    val forUi: CompassBearing,
    val forCamera: CompassBearing,
)
