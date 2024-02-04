package com.qubacy.moveanddraw.ui._common._test.view.util.matcher.button.floating.icon.drawable

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageButton
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.hamcrest.BaseMatcher
import org.hamcrest.Description

class FABIconDrawableViewMatcher(
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
        val expectedBitmap = Bitmap.createScaledBitmap(
            expectedDrawable.toBitmap(), gottenBitmap.width, gottenBitmap.height, false)

        return gottenBitmap.sameAs(expectedBitmap)
    }
}