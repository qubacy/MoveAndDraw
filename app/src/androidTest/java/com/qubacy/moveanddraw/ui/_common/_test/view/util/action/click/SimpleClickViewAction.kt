package com.qubacy.moveanddraw.ui._common._test.view.util.action.click

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class SimpleClickViewAction : ViewAction {
    override fun getDescription(): String = String()

    override fun getConstraints(): Matcher<View> = Matchers.allOf(ViewMatchers.isClickable())

    override fun perform(uiController: UiController?, view: View?) {
        view!!.performClick()
    }
}