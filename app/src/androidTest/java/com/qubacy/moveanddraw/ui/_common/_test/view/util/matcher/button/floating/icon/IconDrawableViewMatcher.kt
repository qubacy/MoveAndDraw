package com.qubacy.moveanddraw.ui._common._test.view.util.matcher.button.floating.icon

import android.view.View
import android.widget.ImageButton
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.hamcrest.BaseMatcher
import org.hamcrest.Description


class IconDrawableViewMatcher(
    @DrawableRes val drawableResId: Int,
    @ColorInt val iconTint: Int
) : BaseMatcher<View>() {
    override fun describeTo(description: Description?) {}

    override fun matches(item: Any?): Boolean {
        if (item == null) return false
        if (!item::class.java.isAssignableFrom(FloatingActionButton::class.java)) return false

        item as ImageButton

        val expectedDrawable = InstrumentationRegistry.getInstrumentation()
            .targetContext.getDrawable(drawableResId)!!.apply {
                mutate()
                setTint(iconTint)
            }

        val gottenBitmap = item.drawable.toBitmap()
        val expectedBitmap = expectedDrawable.toBitmap()

        return gottenBitmap.sameAs(expectedBitmap)
    }
}