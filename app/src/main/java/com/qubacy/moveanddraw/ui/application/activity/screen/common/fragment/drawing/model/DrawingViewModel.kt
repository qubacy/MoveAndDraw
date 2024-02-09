package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.qubacy.moveanddraw._common.util.struct.takequeue._common.TakeQueue
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.domain._common.usecase._common.result._common.Result
import com.qubacy.moveanddraw.domain._common.usecase._common.result.error.ErrorResult
import com.qubacy.moveanddraw.domain._common.usecase.drawing.DrawingUseCase
import com.qubacy.moveanddraw.domain._common.usecase.drawing.result._common.SetDrawingResult
import com.qubacy.moveanddraw.domain._common.usecase.drawing.result.load.LoadDrawingResult
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation.error.ShowErrorUiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model.business.BusinessViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.DrawingUiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.operation.loaded.DrawingLoadedUiOperation

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

        const val DRAWING_KEY = "drawing"
    }

    private var mDrawing: Drawing? = null
    val drawing get() = mDrawing

    init {
        mDrawing = mSavedStateHandle[DRAWING_KEY]
    }

    fun isDrawingFileExtensionValid(ext: String): Boolean {
        return DrawingFileExtension.values().find { it.ext == ext } != null
    }

    fun loadDrawing(drawingUri: Uri) {
        mUiState.value = generateDrawingUiState(isLoading = true)

        mDrawingUseCase.loadDrawing(drawingUri)
    }

    override fun processResult(result: Result): UiStateType? {
        return if (result is SetDrawingResult) {
//            LoadDrawingResult::class -> { processLoadDrawingResult(result as LoadDrawingResult) }
            processSetDrawingResult(result)
        } else null
    }

    open fun processSetDrawingResult(result: SetDrawingResult): UiStateType? {
        mDrawing = result.drawing
        mSavedStateHandle[DRAWING_KEY] = result.drawing // todo: is this OK?

        return when (result::class) {
            LoadDrawingResult::class -> { processLoadDrawingResult(result as LoadDrawingResult) }
            else -> null
        }
    }

    private fun processLoadDrawingResult(result: LoadDrawingResult): UiStateType {
        return generateDrawingUiState(
            //drawing = result.drawing,
            isLoading = false,
            pendingOperations = TakeQueue(DrawingLoadedUiOperation(mDrawing!!))
        )
    }

    override fun processErrorResult(errorResult: ErrorResult): UiStateType {
        return generateDrawingUiState(
            //drawing = mUiState.value?.drawing,
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
        //drawing: Drawing? = null,
        isLoading: Boolean = false,
        pendingOperations : TakeQueue<UiOperation> = TakeQueue()
    ): UiStateType

    override fun getUiStateWithUiOperation(uiOperation: UiOperation): UiStateType {
        return generateDrawingUiState(
            //drawing = mUiState.value?.drawing,
            pendingOperations = TakeQueue(uiOperation)
        )
    }
}
