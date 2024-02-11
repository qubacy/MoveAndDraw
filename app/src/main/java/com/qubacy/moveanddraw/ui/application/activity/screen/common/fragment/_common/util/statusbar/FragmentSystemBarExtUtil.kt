package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.util.statusbar

import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.qubacy.moveanddraw.ui.application.activity.MainActivity
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.util.color.getColorInt

fun Fragment.setStatusBarBackgroundColorByColorRes(@ColorRes colorRes: Int) {
    val colorInt = getColorInt(colorRes)

    setStatusBarBackgroundColorByColorInt(colorInt)
}

fun Fragment.setStatusBarBackgroundColorByColorInt(@ColorInt colorInt: Int) {
    (requireActivity() as MainActivity).setStatusBarBackgroundColor(colorInt)
}

@RequiresApi(Build.VERSION_CODES.O)
fun Fragment.setNavigationBarBackgroundColorByColorRes(@ColorRes colorRes: Int) {
    val colorInt = getColorInt(colorRes)

    setNavigationBarBackgroundColorByColorInt(colorInt)
}

@RequiresApi(Build.VERSION_CODES.O)
fun Fragment.setNavigationBarBackgroundColorByColorInt(@ColorInt colorInt: Int) {
    (requireActivity() as MainActivity).setNavigationBarBackgroundColor(colorInt)
}

@RequiresApi(Build.VERSION_CODES.M)
fun Fragment.setLightSystemBarIconColor() {
    (requireActivity() as MainActivity).setLightSystemBarIconColor()
}
