package com.qubacy.moveanddraw.ui.application.activity.screen.initial.component.chooser.view

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("componentOptionChooserOptionTitles")
fun setComponentOptionChooserOptionTitles(view: View, optionsTitles: Array<String>) {
    (view as OptionChooserComponent).apply {
        setComponentOptionChooserOptionTitles(optionsTitles)
    }
}

@BindingAdapter("componentOptionChooserSwipeActivationPercent")
fun setComponentOptionChooserSwipeActivationPercent(view: View, activationPercent: Float) {
    (view as OptionChooserComponent).apply {
        setComponentOptionChooserSwipeActivationPercent(activationPercent)
    }
}

@BindingAdapter("componentOptionChooserOptionPreviewPercent")
fun setComponentOptionChooserOptionPreviewPercent(view: View, optionPreviewPercent: Float) {
    (view as OptionChooserComponent).apply {
        setComponentOptionChooserOptionPreviewPercent(optionPreviewPercent)
    }
}