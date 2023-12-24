package com.qubacy.moveanddraw.ui._common.util.view.action.drag

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class DragViewAction(
    private val mEndX: Float,
    private val mEndY: Float,
) : ViewAction {
    override fun getDescription(): String = String()

    override fun getConstraints(): Matcher<View> = Matchers.any(View::class.java)

    override fun perform(uiController: UiController?, view: View?) {
        DragViewActionUtil.drag(uiController!!, view!!, mEndX, mEndY)
    }
}