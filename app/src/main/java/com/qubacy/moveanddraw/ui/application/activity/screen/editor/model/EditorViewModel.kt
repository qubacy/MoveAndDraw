package com.qubacy.moveanddraw.ui.application.activity.screen.editor.model

import android.content.Context
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
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
import com.qubacy.moveanddraw.domain.editor.result.SaveDrawingResult
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.Dot2D
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.GLContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.DrawingViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.data.face.FaceSketch
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.EditorUiState
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.operation.face.added.NewFaceAddedToDrawingUiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.operation.saved.DrawingSavedUiOperation
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
    savedStateHandle: SavedStateHandle,
    private val mEditorUseCase: EditorUseCase
) : DrawingViewModel<EditorUiState>(savedStateHandle, mEditorUseCase) {
    companion object {
        const val TAG = "EDITOR_VIEW_MODEL"

        const val FACE_SKETCH_DOT_BUFFER_KEY = "faceSketchDotBuffer"
        const val IS_SHARING_PENDING_KEY = "isSharingPending"
    }

    private var mFaceSketchDotBuffer: List<Dot2D>? = null
    val faceSketchDotBuffer get() = mFaceSketchDotBuffer

    init {
        mSavedStateHandle.get<FloatArray>(FACE_SKETCH_DOT_BUFFER_KEY)?.also {
            mFaceSketchDotBuffer = GLContext.floatArrayToDot2DList(it)
        }
    }

    fun getIsSharingPending(): Boolean {
        return mSavedStateHandle[IS_SHARING_PENDING_KEY] ?: false
    }

    fun setIsSharingPending(isSharingPending: Boolean) {
        mSavedStateHandle[IS_SHARING_PENDING_KEY] = isSharingPending
    }

    fun setFaceSketchDotBuffer(faceSketchDotBuffer: List<Dot2D>) {
        mFaceSketchDotBuffer = faceSketchDotBuffer

        val preparedFaceSketchDotBuffer =
            faceSketchDotBuffer.flatMap { it.toList() }.toFloatArray()

        mSavedStateHandle[FACE_SKETCH_DOT_BUFFER_KEY] = preparedFaceSketchDotBuffer
    }

    override fun generateDrawingUiState(
        drawing: Drawing?,
        isLoading: Boolean,
        pendingOperations: TakeQueue<UiOperation>
    ): EditorUiState {
        return EditorUiState(
            drawing = drawing, //?: mUiState.value?.drawing,
            isLoading = isLoading,
            pendingOperations = pendingOperations
        )
    }

    fun checkNewFileFilenameValidity(filename: String): Boolean {
        return Regex("^\\S+$").matches(filename)
    }

    fun checkDrawingValidity(drawing: Drawing): Boolean {
        return (drawing.vertexArray.isNotEmpty() && drawing.faceArray.isNotEmpty())
    }

    open fun saveCurrentDrawingToNewFile(drawing: Drawing, filename: String) {
        mUiState.value = generateDrawingUiState(drawing = drawing, isLoading = true)

        // todo: only .obj for now:
        val filenameWithExtension = filename + '.' + DrawingFileExtension.OBJ.ext

        mEditorUseCase.saveDrawing(drawing, filename = filenameWithExtension)
    }

    open fun saveCurrentDrawingChanges(drawing: Drawing) {
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
            SaveDrawingResult::class ->
                processSaveDrawingResult(result as SaveDrawingResult)
            else -> throw IllegalArgumentException()
        }
    }

    private fun processRemoveLastFaceResult(result: RemoveLastFaceFromDrawingResult): EditorUiState {
        return generateDrawingUiState(drawing = result.drawing, isLoading = false)
    }

    private fun processAddNewFaceToDrawingResult(result: AddNewFaceToDrawingResult): EditorUiState {
        return generateDrawingUiState(drawing = result.drawing, isLoading = false,
            pendingOperations = TakeQueue(NewFaceAddedToDrawingUiOperation()))
    }

    private fun processSaveDrawingResult(result: SaveDrawingResult): EditorUiState {
        return generateDrawingUiState(drawing = result.drawing, isLoading = false,
            pendingOperations = TakeQueue(DrawingSavedUiOperation(result.filePath)))
    }

    fun removeLastFace(drawing: Drawing) {
        mUiState.value = generateDrawingUiState(isLoading = true)

        mEditorUseCase.removeLastFaceFromDrawing(drawing)
    }

    fun saveFaceSketch(drawing: Drawing?, faceSketch: FaceSketch) {
        mUiState.value = generateDrawingUiState(isLoading = true)

        mEditorUseCase.addNewFaceToDrawing(drawing, faceSketch.vertexArray, faceSketch.face)
    }
}

class EditorViewModelFactory(
    private val mEditorUseCase: EditorUseCase
) : AbstractSavedStateViewModelFactory() {
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (!modelClass.isAssignableFrom(EditorViewModel::class.java))
            throw IllegalArgumentException()

        return EditorViewModel(handle, mEditorUseCase) as T
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
