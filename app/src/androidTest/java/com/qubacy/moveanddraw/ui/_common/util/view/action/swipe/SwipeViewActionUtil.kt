package com.qubacy.moveanddraw.ui._common.util.view.action.swipe

import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe

object SwipeViewActionUtil {
    fun generateSwipeViewAction(endX: Float, endY: Float): ViewAction {
        return GeneralSwipeAction(
            Swipe.SLOW, GeneralLocation.CENTER, { floatArrayOf(endX, endY) }, Press.FINGER
        )
    }
}