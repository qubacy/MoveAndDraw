package com.qubacy.moveanddraw.ui.application.activity.screen.editor.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qubacy.moveanddraw._common.util.struct.takequeue._common.TakeQueue
import com.qubacy.moveanddraw.data.drawing.repository.DrawingDataRepository
import com.qubacy.moveanddraw.data.drawing.repository.source.local.LocalDrawingDataSource
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain._common.model.drawing.Drawing
import com.qubacy.moveanddraw.domain._common.usecase._common.result._common.Result
import com.qubacy.moveanddraw.domain.editor.EditorUseCase
import com.qubacy.moveanddraw.domain.editor.result.RemoveLastFaceFromDrawingResult
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.model._common.AccelerometerStateHolder
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.model.simple.SimpleAccelerometerStateHolder
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.DrawingViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.EditorUiState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Qualifier
import kotlin.math.abs

@HiltViewModel
open class EditorViewModel @Inject constructor(
    private val mEditorUseCase: EditorUseCase
) : DrawingViewModel<EditorUiState>(mEditorUseCase),
    AccelerometerStateHolder by SimpleAccelerometerStateHolder() {
    companion object {
        const val TAG = "EDITOR_VIEW_MODEL"

        val DEFAULT_DEVICE_POS = floatArrayOf(0f, 0f, 0f)
        const val DEFAULT_CONST_OFFSET = 0.3f
    }

    private var mXConstOffset: Float = DEFAULT_CONST_OFFSET
    private var mYConstOffset: Float = DEFAULT_CONST_OFFSET
    private var mZConstOffset: Float = DEFAULT_CONST_OFFSET

    private var mOffsetScaleFactor: Float = 1f

    override fun generateDrawingUiState(
        drawing: Drawing?,
        isLoading: Boolean,
        pendingOperations: TakeQueue<UiOperation>
    ): EditorUiState {
        return EditorUiState(
            devicePos = mUiState.value?.devicePos ?: DEFAULT_DEVICE_POS,
            drawing = drawing ?: mUiState.value?.drawing,
            isLoading = isLoading,
            pendingOperations = pendingOperations
        )
    }

    fun saveCurrentDrawingToNewFile(drawing: Drawing, filename: String) {
        mUiState.value = generateDrawingUiState(drawing = drawing, isLoading = true)

        mEditorUseCase.saveDrawing(drawing, filename = filename)
    }

    fun saveCurrentDrawingChanges(drawing: Drawing) {
        mUiState.value = generateDrawingUiState(drawing = drawing, isLoading = true)

        mEditorUseCase.saveDrawing(drawing, drawingUri = drawing.uri)
    }

    override fun processResult(result: Result): EditorUiState? {
        val state = super.processResult(result)

        if (state != null) return state

        return when (result::class) {
            RemoveLastFaceFromDrawingResult::class ->
                processRemoveLastFaceResult(result as RemoveLastFaceFromDrawingResult)
            else -> null
        }
    }

    private fun processRemoveLastFaceResult(result: RemoveLastFaceFromDrawingResult): EditorUiState {
        return generateDrawingUiState(drawing = result.drawing, isLoading = false)
    }

    fun removeLastFace() {
        if (mUiState.value?.drawing == null) return

        mUiState.value = generateDrawingUiState(isLoading = true)

        mEditorUseCase.removeLastFaceFromDrawing(mUiState.value!!.drawing!!)
    }

    fun setConstantOffsets(xOffset: Float, yOffset: Float, zOffset: Float) {
        mXConstOffset = xOffset
        mYConstOffset = yOffset
        mZConstOffset = zOffset
    }

    override fun applyAccelerations(
        xAcceleration: Float, yAcceleration: Float, zAcceleration: Float
    ) {
        val offsets = getOffsetsByAccelerations(xAcceleration, yAcceleration, zAcceleration)

        val curDevicePos = mUiState.value?.devicePos ?: DEFAULT_DEVICE_POS

        val newDevicePosX =
            if (abs(offsets[0]) >= abs(mXConstOffset)) curDevicePos[0] + offsets[0] * mOffsetScaleFactor
            else curDevicePos[0]
        val newDevicePosY =
            if (abs(offsets[1]) >= abs(mYConstOffset)) curDevicePos[1] + offsets[1] * mOffsetScaleFactor
            else curDevicePos[1]
        val newDevicePosZ =
            if (abs(offsets[2]) >= abs(mZConstOffset)) curDevicePos[2] + offsets[2] * mOffsetScaleFactor
            else curDevicePos[2]

        val newDevicePos = floatArrayOf(newDevicePosX, newDevicePosY, newDevicePosZ)

        Log.d(TAG, "applyAccelerations(): newDevicePos = ${newDevicePos.joinToString()}")

        changeDevicePos(newDevicePos)
    }

    private fun changeDevicePos(devicePos: FloatArray) {
        mUiState.value = EditorUiState(
            devicePos = devicePos,
            drawing = mUiState.value?.drawing,
            isLoading = mUiState.value?.isLoading ?: false,
            pendingOperations = mUiState.value?.pendingOperations ?: TakeQueue()
        )
    }
}

class EditorViewModelFactory(
    private val mEditorUseCase: EditorUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(EditorViewModel::class.java))
            throw IllegalArgumentException()

        return EditorViewModel(mEditorUseCase) as T
    }
}

@Qualifier
annotation class EditorViewModelFactoryQualifier

@Module
@InstallIn(ActivityRetainedComponent::class)
object EditorViewModelFactoryModule {
    @Provides
    @EditorViewModelFactoryQualifier
    fun provideEditorViewModelFactory(
        editorUseCase: EditorUseCase
    ): ViewModelProvider.Factory {
        return EditorViewModelFactory(editorUseCase)
    }

    @Provides
    fun provideEditorUseCase(
        errorDataRepository: ErrorDataRepository,
        @ApplicationContext context: Context
    ): EditorUseCase {
        val drawingDataRepository = DrawingDataRepository(LocalDrawingDataSource(context))

        return EditorUseCase(errorDataRepository, drawingDataRepository)
    }
}
