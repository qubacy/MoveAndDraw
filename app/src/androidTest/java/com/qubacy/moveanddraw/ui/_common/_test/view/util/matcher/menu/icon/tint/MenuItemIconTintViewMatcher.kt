package com.qubacy.moveanddraw.ui._common._test.view.util.matcher.menu.icon.tint

import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.VectorDrawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.view.menu.ActionMenuItemView
import org.hamcrest.BaseMatcher
import org.hamcrest.Description

class MenuItemIconTintViewMatcher(
    @ColorInt val iconTint: Int
) : BaseMatcher<View>() {
    override fun describeTo(description: Description?) { }

    override fun matches(item: Any?): Boolean {
        if (item == null) return false
        if (!item::class.java.isAssignableFrom(ActionMenuItemView::class.java)) return false

        item as ActionMenuItemView

        val mTintFilterFieldReflection = VectorDrawable::class.java.getDeclaredField("mTintFilter")
            .apply { isAccessible = true }
        val tintFilter = mTintFilterFieldReflection.get(item.itemData.icon) as PorterDuffColorFilter

        val mColorFieldReflection = PorterDuffColorFilter::class.java.getDeclaredField("mColor")
            .apply { isAccessible = true }
        val tintColor = mColorFieldReflection.get(tintFilter) as Int

        return (tintColor == iconTint)
    }
}