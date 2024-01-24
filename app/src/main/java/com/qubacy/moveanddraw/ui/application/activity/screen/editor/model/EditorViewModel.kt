package com.qubacy.moveanddraw.ui.application.activity.screen.editor.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qubacy.moveanddraw._common.util.struct.takequeue._common.TakeQueue
import com.qubacy.moveanddraw.data.drawing.repository.DrawingDataRepository
import com.qubacy.moveanddraw.data.drawing.repository.source.local.LocalDrawingDataSource
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.domain._common.usecase._common.result._common.Result
import com.qubacy.moveanddraw.domain.editor.EditorUseCase
import com.qubacy.moveanddraw.domain.editor.result.AddNewFaceToDrawingResult
import com.qubacy.moveanddraw.domain.editor.result.RemoveLastFaceFromDrawingResult
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.DrawingViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.data.FaceSketch
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.EditorUiState
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.operation.face.added.NewFaceAddedToDrawingUiOperation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Qualifier

@HiltViewModel
open class EditorViewModel @Inject constructor(
    private val mEditorUseCase: EditorUseCase
) : DrawingViewModel<EditorUiState>(mEditorUseCase) {
    companion object {
        const val TAG = "EDITOR_VIEW_MODEL"
    }

    override fun generateDrawingUiState(
        drawing: Drawing?,
        isLoading: Boolean,
        pendingOperations: TakeQueue<UiOperation>
    ): EditorUiState {
        return EditorUiState(
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
            AddNewFaceToDrawingResult::class ->
                processAddNewFaceToDrawingResult(result as AddNewFaceToDrawingResult)
            else -> null
        }
    }

    private fun processRemoveLastFaceResult(result: RemoveLastFaceFromDrawingResult): EditorUiState {
        return generateDrawingUiState(drawing = result.drawing, isLoading = false)
    }

    private fun processAddNewFaceToDrawingResult(result: AddNewFaceToDrawingResult): EditorUiState {
        return generateDrawingUiState(drawing = result.drawing, isLoading = false,
            pendingOperations = TakeQueue(NewFaceAddedToDrawingUiOperation()))
    }

    fun removeLastFace() {
        if (mUiState.value?.drawing == null) return

        mUiState.value = generateDrawingUiState(isLoading = true)

        mEditorUseCase.removeLastFaceFromDrawing(mUiState.value!!.drawing!!)
    }

    fun saveFaceSketch(faceSketch: FaceSketch) {
        mUiState.value = generateDrawingUiState(isLoading = true)

        mEditorUseCase.addNewFaceToDrawing(
            mUiState.value!!.drawing, faceSketch.vertexArray, faceSketch.face)
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
