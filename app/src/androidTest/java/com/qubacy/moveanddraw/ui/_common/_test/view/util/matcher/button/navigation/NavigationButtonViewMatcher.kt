package com.qubacy.moveanddraw.ui._common._test.view.util.matcher.button.navigation

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.annotation.DrawableRes
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import org.hamcrest.BaseMatcher
import org.hamcrest.Description

class NavigationButtonViewMatcher(
    @DrawableRes mDrawableResId: Int,
    private val mContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
) : BaseMatcher<View>() {
    companion object {
        const val CHILDREN_INDEX = 1
    }

    private val mDrawable: Drawable

    init {
        Dispatchers.Main.run {
            mDrawable = mContext.getDrawable(mDrawableResId)!!
        }
    }

    override fun describeTo(description: Description?) {}

    override fun matches(item: Any?): Boolean {
        if (item !is ImageButton) return false

        val imageButton = item as ImageButton
        val parent = (imageButton.parent as ViewGroup)

        if (parent !is Toolbar) return false

        val childIndex = parent.indexOfChild(imageButton)

        return (childIndex == CHILDREN_INDEX)
    }

    override fun describeMismatch(item: Any?, mismatchDescription: Description?) {

    }
}