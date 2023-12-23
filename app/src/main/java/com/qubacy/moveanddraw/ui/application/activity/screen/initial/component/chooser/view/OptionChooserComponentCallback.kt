package com.qubacy.moveanddraw.ui.application.activity.screen.initial.component.chooser.view

interface OptionChooserComponentCallback {
    enum class SwipeOption {
        RIGHT(), LEFT(), UNSET();
    }

    fun onSwipeOptionChosen(swipeOption: SwipeOption, endAction: (() -> Unit)? = null)
    fun onScrimClicked()
}