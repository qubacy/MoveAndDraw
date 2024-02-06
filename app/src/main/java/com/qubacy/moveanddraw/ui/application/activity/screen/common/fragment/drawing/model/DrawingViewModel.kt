package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.qubacy.moveanddraw._common.util.struct.takequeue._common.TakeQueue
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.domain._common.usecase._common.result._common.Result
import com.qubacy.moveanddraw.domain._common.usecase._common.result.error.ErrorResult
import com.qubacy.moveanddraw.domain._common.usecase.drawing.DrawingUseCase
import com.qubacy.moveanddraw.domain._common.usecase.drawing.result.LoadDrawingResult
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation.error.ShowErrorUiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model.business.BusinessViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.DrawingUiState

abstract class DrawingViewModel<UiStateType : DrawingUiState>(
    savedStateHandle: SavedStateHandle,
    protected val mDrawingUseCase: DrawingUseCase
) : BusinessViewModel<UiStateType>(savedStateHandle, mDrawingUseCase) {
    enum class DrawingFileExtension(
        val ext: String
    ) {
        OBJ("obj");
    }

    companion object {
        const val TAG = "DRAWING_VIEW_MODEL"

        const val DRAWING_MIME_TYPE = "model/obj"
    }

    fun isDrawingFileExtensionValid(ext: String): Boolean {
        return DrawingFileExtension.values().find { it.ext == ext } != null
    }

    fun loadDrawing(drawingUri: Uri) {
        mUiState.value = generateDrawingUiState(isLoading = true)

        mDrawingUseCase.loadDrawing(drawingUri)
    }

    override fun processResult(result: Result): UiStateType? {
        return when (result::class) {
            LoadDrawingResult::class -> { processLoadDrawingResult(result as LoadDrawingResult) }
            else -> throw IllegalArgumentException()
        }
    }

    private fun processLoadDrawingResult(result: LoadDrawingResult): UiStateType {
        return generateDrawingUiState(drawing = result.drawing, isLoading = false)
    }

    override fun processErrorResult(errorResult: ErrorResult): UiStateType {
        return generateDrawingUiState(
            drawing = mUiState.value?.drawing,
            isLoading = false,
            pendingOperations = TakeQueue(ShowErrorUiOperation(errorResult.error))
        )
    }

    // todo: it should be common for both Drawing & Init view models:
    override fun resetUiState() {
        super.resetUiState()

        mDrawingUseCase.resetFlow()
    }

    abstract fun generateDrawingUiState(
        drawing: Drawing? = null,
        isLoading: Boolean = false,
        pendingOperations : TakeQueue<UiOperation> = TakeQueue()
    ): UiStateType

    override fun getUiStateWithUiOperation(uiOperation: UiOperation): UiStateType {
        return generateDrawingUiState(
            drawing = mUiState.value?.drawing,
            pendingOperations = TakeQueue(uiOperation)
        )
    }
}
