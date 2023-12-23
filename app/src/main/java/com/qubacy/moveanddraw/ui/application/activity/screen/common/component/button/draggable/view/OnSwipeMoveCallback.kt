package com.qubacy.moveanddraw.ui.application.activity.screen.common.component.button.draggable.view

interface OnSwipeMoveCallback {
    fun onSwipeMove(transitionX: Int, transitionY: Int)
    fun onSwipeEnded(lastTransitionX: Int, lastTransitionY: Int)
}