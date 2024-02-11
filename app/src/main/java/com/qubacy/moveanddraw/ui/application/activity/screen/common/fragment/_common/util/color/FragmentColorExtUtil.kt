package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.util.color

import android.os.Build
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment

fun Fragment.getColorInt(@ColorRes colorRes: Int): Int {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        resources.getColor(colorRes)
    } else {
        resources.getColor(colorRes, requireContext().theme)
    }
}

fun Fragment.getColorByAttr(@AttrRes attrRes: Int): Int {
    val resultTypedValue = TypedValue()

    if (!requireContext().theme.resolveAttribute(attrRes, resultTypedValue, true))
        throw IllegalStateException()

    return resultTypedValue.data
}