package com.qubacy.moveanddraw.ui._common._test.view.util.action.drag

import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.test.espresso.UiController


object DragViewActionUtil {
    private const val DEFAULT_STEP_COUNT = 100

    fun drag(
        uiController: UiController,
        view: View,
        toX: Float,
        toY: Float,
        stepCount: Int = DEFAULT_STEP_COUNT,
        endAction: (() -> Unit)? = null
    ) {
        val downTime = SystemClock.uptimeMillis()
        var eventTime = SystemClock.uptimeMillis()

        var y = view.y
        var x = view.x

        val yStep = (toY - y) / stepCount
        val xStep = (toX - x) / stepCount

        var event = MotionEvent.obtain(
            downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0)

        view.dispatchTouchEvent(event)

        for (i in 0 until stepCount) {
            y += yStep
            x += xStep

            eventTime = SystemClock.uptimeMillis()
            event = MotionEvent.obtain(
                downTime, eventTime, MotionEvent.ACTION_MOVE, x, y, 0)

            view.dispatchTouchEvent(event)
            uiController.loopMainThreadForAtLeast(20)
        }

        eventTime = SystemClock.uptimeMillis()
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0)

        endAction?.invoke()
        view.dispatchTouchEvent(event)
    }
}