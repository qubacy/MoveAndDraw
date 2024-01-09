package com.qubacy.moveanddraw.ui.application.activity.screen.initial.component.chooser.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.textview.MaterialTextView
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw.ui.application.activity.screen.common.component.button.draggable.view.DraggableButton
import com.qubacy.moveanddraw.ui.application.activity.screen.common.component.button.draggable.view.OnSwipeMoveCallback
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.view.CanvasView
import com.qubacy.moveanddraw.ui.application.activity.screen.initial.component.chooser.view.OptionChooserComponentCallback.SwipeOption
import kotlinx.coroutines.runBlocking
import kotlin.math.abs

class OptionChooserComponent(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs), OnSwipeMoveCallback {
    companion object {
        const val TAG = "OPTION_CHOOSER_COMP"

        const val DEFAULT_SWIPE_PREVIEW_PERCENT = 0.3f
        const val DEFAULT_SWIPE_ACTIVATION_PERCENT = 0.5f

        const val DEFAULT_ATTR_FLOAT = -1f
    }

    private var mHeader: MaterialTextView? = null
    private var mSwipeButton: DraggableButton? = null

    private var mCallback: OptionChooserComponentCallback? = null

    private var mComponentOptionChooserOptionTitles: Array<String> = arrayOf()
    val componentOptionChooserOptionTitles get() = mComponentOptionChooserOptionTitles

    private var mComponentOptionChooserOptionPreviewPercent = DEFAULT_SWIPE_PREVIEW_PERCENT
    val componentOptionChooserOptionPreviewPercent get() = mComponentOptionChooserOptionPreviewPercent

    private var mComponentOptionChooserSwipeActivationPercent = DEFAULT_SWIPE_ACTIVATION_PERCENT
    val componentOptionChooserSwipeActivationPercent get() =
        mComponentOptionChooserSwipeActivationPercent

    init {
        initCustomAttrs(attrs)
    }

    @SuppressLint("Recycle", "ResourceType")
    private fun initCustomAttrs(attrs: AttributeSet) {
        val attrsTypedArray = context.obtainStyledAttributes(
            attrs,
            intArrayOf(
                R.attr.optionChooserActivationPercent,
                R.attr.optionChooserPreviewPercent,
                R.attr.optionChooserTitles
            )
        )

        attrsTypedArray.getFloat(0, -1f).apply {
            if (this == DEFAULT_ATTR_FLOAT) return@apply

            setComponentOptionChooserSwipeActivationPercent(this)
        }
        attrsTypedArray.getFloat(1, -1f).apply {
            if (this == DEFAULT_ATTR_FLOAT) return@apply

            setComponentOptionChooserOptionPreviewPercent(this)
        }
        attrsTypedArray.getTextArray(2).apply {
            val titlesArray = this.map { it -> it.toString() }.toTypedArray()

            setComponentOptionChooserOptionTitles(titlesArray)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        mHeader = findViewById<MaterialTextView>(R.id.component_option_chooser_header)
        mSwipeButton = findViewById<DraggableButton>(R.id.component_option_chooser_swipe_button)
            .apply { setOnSwipeMoveCallback(this@OptionChooserComponent) }

        rootView.setOnClickListener { mCallback?.onScrimClicked() }
    }

    fun setComponentOptionChooserOptionTitles(optionTitles: Array<String>) {
        mComponentOptionChooserOptionTitles = optionTitles
    }

    fun setComponentOptionChooserOptionPreviewPercent(optionPreviewPercent: Float) {
        mComponentOptionChooserOptionPreviewPercent = optionPreviewPercent
    }

    fun setComponentOptionChooserSwipeActivationPercent(activationPercent: Float) {
        mComponentOptionChooserSwipeActivationPercent = activationPercent
    }

    fun setSwipeOptionChosenCallback(callback: OptionChooserComponentCallback) {
        mCallback = callback
    }

    override fun onSwipeMove(transitionX: Int, transitionY: Int) {
        if (mCallback == null) return

        val swipeFraction = getSwipeFraction(transitionX)
        val swipeDirection = getSwipeDirection(transitionX)

        changeTitleWithSwipeFraction(swipeFraction, swipeDirection)
    }

    private fun getSwipeFraction(transitionX: Int): Float {
        val swipePathLength = rootView.measuredWidth / 2

        return abs(transitionX) / swipePathLength.toFloat()
    }

    private fun getSwipeDirection(transitionX: Int): SwipeOption {
        return if (transitionX < 0) SwipeOption.LEFT else SwipeOption.RIGHT
    }

    override fun onSwipeEnded(lastTransitionX: Int, lastTransitionY: Int) {
        val swipeFraction = getSwipeFraction(lastTransitionX)
        val swipeDirection = getSwipeDirection(lastTransitionX)

        if (swipeFraction >= mComponentOptionChooserSwipeActivationPercent)
            mCallback!!.onSwipeOptionChosen(swipeDirection) { changeTitleWithSwipeFraction() }
        else
            changeTitleWithSwipeFraction()
    }

    private fun changeTitleWithSwipeFraction(
        swipeFraction: Float = 0f,
        swipeOption: SwipeOption = SwipeOption.UNSET
    ) {
        val title =
            if (swipeFraction < mComponentOptionChooserOptionPreviewPercent
                || swipeOption == SwipeOption.UNSET
            ) {
                context.getString(R.string.component_option_chooser_header_text)
            } else if (swipeOption == SwipeOption.RIGHT)
                mComponentOptionChooserOptionTitles.last()
            else
                mComponentOptionChooserOptionTitles.first()

        mHeader?.text = title
    }
}