package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model

import android.net.Uri
import android.util.Log
import com.qubacy.moveanddraw._common.util.struct.takequeue._common.TakeQueue
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.domain._common.usecase._common.result._common.Result
import com.qubacy.moveanddraw.domain._common.usecase._common.result.error.ErrorResult
import com.qubacy.moveanddraw.domain._common.usecase.drawing.DrawingUseCase
import com.qubacy.moveanddraw.domain._common.usecase.drawing.result.LoadDrawingResult
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation.error.ShowErrorUiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model.business.BusinessViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.camera._common.CameraData
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.DrawingUiState

abstract class DrawingViewModel<UiStateType : DrawingUiState>(
    protected val mDrawingUseCase: DrawingUseCase
) : BusinessViewModel<UiStateType>(mDrawingUseCase) {
    companion object {
        const val TAG = "DRAWING_VIEW_MODEL"

        const val DRAWING_MIME_TYPE = "model/obj"
        val DRAWING_FILE_EXTENSIONS = arrayOf("obj")
    }

    protected var mLastCameraData: CameraData? = null
    val lastCameraData get() = mLastCameraData

    fun setLastCameraData(cameraData: CameraData) {
        Log.d(TAG, "setLastCameraData(): cameraData.pos = ${cameraData.position.joinToString()}")

        mLastCameraData = cameraData
    }

    fun isDrawingFileExtensionValid(ext: String): Boolean {
        return DRAWING_FILE_EXTENSIONS.contains(ext)
    }

    fun loadDrawing(drawingUri: Uri) {
        mUiState.value = generateDrawingUiState(isLoading = true)

        mDrawingUseCase.loadDrawing(drawingUri)
    }

    override fun processResult(result: Result): UiStateType? {
        return when (result::class) {
            LoadDrawingResult::class -> { processLoadDrawingResult(result as LoadDrawingResult) }
            else -> null
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
