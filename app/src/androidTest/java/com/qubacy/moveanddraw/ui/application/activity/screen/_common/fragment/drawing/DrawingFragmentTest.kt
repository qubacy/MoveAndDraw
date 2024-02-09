package com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment.drawing

import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment._common.StatefulFragmentTest
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.DrawingFragment
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.view.CanvasView
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.DrawingViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.DrawingUiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.operation._common.SetDrawingUiOperation
import java.lang.reflect.Field

abstract class DrawingFragmentTest<
        UiStateType : DrawingUiState,
        ViewModelType : DrawingViewModel<UiStateType>,
        CanvasViewType : CanvasView,
        FragmentType : DrawingFragment<UiStateType, ViewModelType, CanvasViewType>
>() : StatefulFragmentTest<UiStateType, ViewModelType, FragmentType>() {
    private lateinit var mDrawingFieldReflection: Field

    protected open fun setModelDrawingBySetDrawingStateOperation(drawing: Drawing) {
        setModelDrawing(drawing)

        val state = generateUiStateWithUiOperation(SetDrawingUiOperation(drawing))

        setState(state)
    }

    protected open fun setModelDrawing(drawing: Drawing) {
        mDrawingFieldReflection.set(mModel, drawing)
    }

    override fun mockViewModel() {
        super.mockViewModel()

        mDrawingFieldReflection = DrawingViewModel::class.java.getDeclaredField("mDrawing")
            .apply { isAccessible = true }
    }
}