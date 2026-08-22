/*
 * Copyright (c) 2026 Nafis Kabbo
 *
 * SPDX-License-Identifier: MIT
 */

package com.hemanthraj.fluttercompass.heading

import android.view.Surface

/**
 * Physical display rotation used when remapping the device rotation matrix
 * for on-screen (UI) compass heading.
 *
 * Derived from [android.view.Display.getRotation] / [Surface] constants.
 */
sealed class ScreenOrientation {
    data object Natural : ScreenOrientation()
    data object Rotated90 : ScreenOrientation()
    data object Rotated180 : ScreenOrientation()
    data object Rotated270 : ScreenOrientation()

    companion object {
        fun fromSurfaceRotation(rotation: Int): ScreenOrientation = when (rotation) {
            Surface.ROTATION_90 -> Rotated90
            Surface.ROTATION_180 -> Rotated180
            Surface.ROTATION_270 -> Rotated270
            else -> Natural
        }
    }
}
