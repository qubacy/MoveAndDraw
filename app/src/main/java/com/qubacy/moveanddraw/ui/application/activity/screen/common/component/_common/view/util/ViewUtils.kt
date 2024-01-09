package com.qubacy.moveanddraw.ui.application.activity.screen.common.component._common.view.util

import android.os.Build
import android.view.View
import androidx.annotation.ColorRes

fun View.getColorByResId(@ColorRes colorResId: Int): Int {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        resources.getColor(colorResId)
    else
        resources.getColor(colorResId, context.theme)
}