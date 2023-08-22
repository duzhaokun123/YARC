package io.github.duzhaokun123.yarc

import android.graphics.PixelFormat
import android.view.WindowManager

class RotationParam(orientation: Int) : WindowManager.LayoutParams(
    0, 0, TYPE_APPLICATION_OVERLAY, FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
) {
    init {
        screenOrientation = orientation
    }
}