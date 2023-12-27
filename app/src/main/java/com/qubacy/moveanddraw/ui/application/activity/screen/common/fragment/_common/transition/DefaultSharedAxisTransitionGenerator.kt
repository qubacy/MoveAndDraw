package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.transition

import android.content.Context
import android.view.animation.AccelerateDecelerateInterpolator
import com.google.android.material.transition.MaterialSharedAxis
import com.google.android.material.transition.MaterialSharedAxis.Axis
import com.qubacy.moveanddraw.R

object DefaultSharedAxisTransitionGenerator {
    fun generate(context: Context, @Axis axis: Int, forward: Boolean): MaterialSharedAxis {
        return MaterialSharedAxis(
            axis,
            forward
        ).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = context.resources
                .getInteger(R.integer.default_transition_animation_duration).toLong()
        }
    }
}