package com.qubacy.moveanddraw.ui._common._test.view.util.matcher.menu.icon.drawable

import android.graphics.Bitmap
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.core.graphics.drawable.toBitmap
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.BaseMatcher
import org.hamcrest.Description

class MenuItemIconDrawableViewMatcher (
    @DrawableRes val drawableResId: Int
) : BaseMatcher<View>() {
    override fun describeTo(description: Description?) { }

    override fun matches(item: Any?): Boolean {
        if (item == null) return false
        if (!item::class.java.isAssignableFrom(ActionMenuItemView::class.java)) return false

        item as ActionMenuItemView

        val gottenIconDrawable = item.itemData.icon!!.apply {
            mutate()
            setTint(0)
        }
        val expectedDrawable = InstrumentationRegistry.getInstrumentation()
            .targetContext.getDrawable(drawableResId)!!.apply {
                mutate()
                setTint(0)
            }

        val gottenBitmap = gottenIconDrawable.toBitmap()
        val expectedBitmap = Bitmap.createScaledBitmap(
            expectedDrawable.toBitmap(), gottenBitmap.width, gottenBitmap.height, false)

        return gottenBitmap.sameAs(expectedBitmap)
    }
}