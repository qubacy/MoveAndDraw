package com.qubacy.moveanddraw.ui.application.activity.screen.common.component.button.draggable.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.AccelerateDecelerateInterpolator
import com.google.android.material.button.MaterialButton


class DraggableButton(
    context: Context,
    attrs: AttributeSet
) : MaterialButton(context, attrs), OnTouchListener {
    companion object {
        const val TAG = "DRAGGABLE_BUTTON"

        const val DEFAULT_BACK_ANIMATION_DURATION = 300L
        const val DEFAULT_DRAGGING_ICON_SIZE_SCALE = 3f
    }

    private var mOriginalIconSize = 0

    private var mDY = 0f
    private var mDX = 0f

    private var mCallback: OnSwipeMoveCallback? = null

    override fun onFinishInflate() {
        super.onFinishInflate()

        mOriginalIconSize = iconSize

        setOnTouchListener(this)
    }

    override fun onTouch(v: View, event: MotionEvent?): Boolean {
        when (event!!.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mDX = v.translationX - event.rawX
                mDY = v.translationY - event.rawY

                iconSize = (DEFAULT_DRAGGING_ICON_SIZE_SCALE * mOriginalIconSize).toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                v.apply {
                    translationX = event.rawX + mDX
                    translationY = event.rawY + mDY

                    mCallback?.onSwipeMove(translationX.toInt(), translationY.toInt())
                }
            }
            MotionEvent.ACTION_UP -> {
                iconSize = mOriginalIconSize

                val lastTransitionX = translationX.toInt()
                val lastTransitionY = translationY.toInt()

                animateIconBackwardsTransition {
                    mCallback?.onSwipeEnded(lastTransitionX, lastTransitionY)
                }
            }
            else -> return false
        }

        return true
    }

    fun setOnSwipeMoveCallback(callback: OnSwipeMoveCallback) {
        mCallback = callback
    }

    private fun animateIconBackwardsTransition(startAction: () -> Unit) {
        animate()
            .translationX(0f)
            .translationY(0f)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(DEFAULT_BACK_ANIMATION_DURATION)
            .setUpdateListener {
                val maxIconSize = DEFAULT_DRAGGING_ICON_SIZE_SCALE * mOriginalIconSize

                iconSize = (maxIconSize - (maxIconSize - mOriginalIconSize) * it.animatedFraction).toInt()
            }
            .withStartAction(startAction)
            .start()
    }
}