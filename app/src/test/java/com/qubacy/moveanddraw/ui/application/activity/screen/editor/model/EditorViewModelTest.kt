package com.qubacy.moveanddraw.ui.application.activity.screen.editor.model

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.qubacy.moveanddraw._common._test.data.InitData
import com.qubacy.moveanddraw._common._test.util.mock.AnyMockUtil
import com.qubacy.moveanddraw._common._test.util.mock.UriMockUtil
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.domain._common.model.drawing._test.util.DrawingGeneratorUtil
import com.qubacy.moveanddraw.domain.editor.EditorUseCase
import com.qubacy.moveanddraw.domain.editor.result.AddNewFaceToDrawingResult
import com.qubacy.moveanddraw.domain.editor.result.RemoveLastFaceFromDrawingResult
import com.qubacy.moveanddraw.domain.editor.result.SaveDrawingResult
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment.drawing.model.DrawingViewModelTest
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.data.face.FaceSketch
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model._test.data.EditorUseCaseMockInitData
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.EditorUiState
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.operation.saved.DrawingSavedUiOperation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

class EditorViewModelTest(

) : DrawingViewModelTest<EditorUiState, EditorUseCase, EditorViewModel>()  {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun mockUseCase(initData: InitData?): EditorUseCase {
        val editorUseCaseMock = Mockito.mock(EditorUseCase::class.java)

        if (initData != null && initData::class == EditorUseCaseMockInitData::class) {
            initData as EditorUseCaseMockInitData

            Mockito.`when`(editorUseCaseMock.saveDrawing(
                AnyMockUtil.anyObject<Drawing>(),
                AnyMockUtil.anyObject<Uri?>(),
                AnyMockUtil.anyObject<String?>()
            )).thenAnswer {
                mainCoroutineRule.launch {
                    mResultFlow.emit(initData.saveDrawingResult)
                }
            }
            Mockito.`when`(editorUseCaseMock.addNewFaceToDrawing(
                AnyMockUtil.anyObject<Drawing>(),
                AnyMockUtil.anyObject<Array<Triple<Float, Float, Float>>>(),
                AnyMockUtil.anyObject<Array<Triple<Int, Int?, Int?>>>()
            )).thenAnswer {
                mainCoroutineRule.launch {
                    mResultFlow.emit(initData.addNewFaceToDrawingResult)
                }
            }
            Mockito.`when`(editorUseCaseMock.removeLastFaceFromDrawing(
                AnyMockUtil.anyObject<Drawing>()
            )).thenAnswer {
                mainCoroutineRule.launch {
                    mResultFlow.emit(initData.removeLastFaceFromDrawingResult)
                }
            }
        }

        return editorUseCaseMock
    }

    override fun createViewModel(
        savedStateHandleMock: SavedStateHandle,
        useCaseMock: EditorUseCase
    ): EditorViewModel {
        return EditorViewModel(savedStateHandleMock, useCaseMock)
    }

    @Test
    fun saveFaceSketchTest() = runTest {
        val drawing = DrawingGeneratorUtil.generateDrawingByVerticesFaces(
            vertices = arrayOf(
                Triple(0f, 0f, 0f),
                Triple(0f, 1f, 0f),
                Triple(1f, 1f, 0f),
                Triple(1f, 0f, 0f)
            ),
            faces = arrayOf(
                arrayOf(
                    Triple(0, null, null),
                    Triple(1, null, null),
                    Triple(2, null, null),
                    Triple(3, null, null)
                )
            )
        )
        val faceSketch = FaceSketch(
            drawing.vertexArray,
            drawing.faceArray.first()
        )
        val modifiedDrawing = DrawingGeneratorUtil.generateDrawingByVerticesFaces(
            vertices = drawing.vertexArray.plus(faceSketch.vertexArray),
            faces = drawing.faceArray.plus(
                faceSketch.face.map {
                    Triple((it.first + drawing.vertexArray.size), it.second, it.third)
                }.toTypedArray()
            )
        )

        val initData = EditorUseCaseMockInitData(
            addNewFaceToDrawingResult = AddNewFaceToDrawingResult(modifiedDrawing)
        )

        initViewModel(useCaseMockInitData = initData)

        mViewModel.uiStateFlow.test {
            mViewModel.saveFaceSketch(faceSketch)

            val uiState = awaitItem()!!

            Assert.assertEquals(modifiedDrawing, uiState.drawing)
        }
    }

    @Test
    fun setDrawingThenRemoveLastFaceTest() = runTest {
        val drawing = DrawingGeneratorUtil.generateDrawingByVerticesFaces(
            vertices = arrayOf(
                Triple(0f, 0f, 0f),
                Triple(0f, 1f, 0f),
                Triple(1f, 1f, 0f),
                Triple(1f, 0f, 0f)
            ),
            faces = arrayOf(
                arrayOf(
                    Triple(0, null, null),
                    Triple(1, null, null),
                    Triple(2, null, null),
                    Triple(3, null, null)
                )
            )
        )
        val modifiedDrawing = DrawingGeneratorUtil.generateDrawingByVerticesFaces(
            vertices = drawing.vertexArray.sliceArray(0 until drawing.vertexArray.size - 1),
            faces = drawing.faceArray.sliceArray(0 until drawing.faceArray.size - 1)
        )

        val initData = EditorUseCaseMockInitData(
            removeLastFaceFromDrawingResult = RemoveLastFaceFromDrawingResult(modifiedDrawing)
        )

        initViewModel(useCaseMockInitData = initData)

        mViewModel.uiStateFlow.test {
            mViewModel.removeLastFace(drawing)

            val uiState = awaitItem()!!

            Assert.assertEquals(modifiedDrawing, uiState.drawing)
        }
    }

    private data class SaveCurrentDrawingTestInitData(
        val drawing: Drawing,
        val savedDrawingFilepath: String
    )

    private fun initSaveCurrentDrawingTestData(
        drawingFilename: String? = null,
        drawingUri: Uri? = null
    ): SaveCurrentDrawingTestInitData {
        val drawing = DrawingGeneratorUtil.generateDrawingByVerticesFaces(
            uri = drawingUri,
            vertices = arrayOf(
                Triple(0f, 0f, 0f),
                Triple(0f, 1f, 0f),
                Triple(1f, 1f, 0f),
                Triple(1f, 0f, 0f)
            ),
            faces = arrayOf(
                arrayOf(
                    Triple(0, null, null),
                    Triple(1, null, null),
                    Triple(2, null, null),
                    Triple(3, null, null)
                )
            )
        )

        val savedDrawingFilepath = "path"

        val initData = EditorUseCaseMockInitData(
            saveDrawingResult = SaveDrawingResult(drawing, savedDrawingFilepath)
        )

        initViewModel(useCaseMockInitData = initData)

        return SaveCurrentDrawingTestInitData(drawing, savedDrawingFilepath)
    }

    @Test
    fun saveCurrentDrawingToNewFileTest() = runTest {
        val drawingFilename = String()
        val testData = initSaveCurrentDrawingTestData(drawingFilename = drawingFilename)

        mViewModel.uiStateFlow.test {
            mViewModel.saveCurrentDrawingToNewFile(testData.drawing, drawingFilename)

            val uiState = awaitItem()!!
            val operation = uiState.pendingOperations.take()!! as DrawingSavedUiOperation

            Assert.assertEquals(testData.savedDrawingFilepath, operation.filePath)
        }
    }

    @Test
    fun saveCurrentDrawingChangesTest() = runTest {
        val drawingUri = UriMockUtil.getMockedUri()
        val testData = initSaveCurrentDrawingTestData(drawingUri = drawingUri)

        mViewModel.uiStateFlow.test {
            mViewModel.saveCurrentDrawingChanges(testData.drawing)

            val uiState = awaitItem()!!
            val operation = uiState.pendingOperations.take()!! as DrawingSavedUiOperation

            Assert.assertEquals(testData.savedDrawingFilepath, operation.filePath)
        }
    }

    data class CheckNewFileFilenameValidityTestCase(
        val filename: String,
        val isValid: Boolean
    )

    @Test
    fun checkNewFileFilenameValidityTest() {
        val testCases = arrayOf(
            CheckNewFileFilenameValidityTestCase("", false),
            CheckNewFileFilenameValidityTestCase(" ", false),
            CheckNewFileFilenameValidityTestCase("something", true)
        )

        initViewModel()

        for (testCase in testCases) {
            val gottenValidity = mViewModel.checkNewFileFilenameValidity(testCase.filename)

            Assert.assertEquals(testCase.isValid, gottenValidity)
        }
    }

    data class CheckDrawingValidityTestTestCase(
        val drawing: Drawing,
        val isValid: Boolean
    )

    @Test
    fun checkDrawingValidityTest() {
        val testCases = arrayOf(
            CheckDrawingValidityTestTestCase(
                DrawingGeneratorUtil.generateDrawingByVerticesFaces(
                    vertices = arrayOf(
                        Triple(0f, 0f, 0f),
                        Triple(0f, 1f, 0f),
                        Triple(1f, 1f, 0f)
                    ),
                    faces = arrayOf()
                ),
                false
            ),
            CheckDrawingValidityTestTestCase(
                DrawingGeneratorUtil.generateDrawingByVerticesFaces(
                    vertices = arrayOf(),
                    faces = arrayOf(
                        arrayOf(
                            Triple(0, null, null),
                            Triple(1, null, null),
                            Triple(2, null, null)
                        )
                    )
                ),
                false
            ),
            CheckDrawingValidityTestTestCase(
                DrawingGeneratorUtil.generateDrawingByVerticesFaces(
                    vertices = arrayOf(
                        Triple(0f, 0f, 0f),
                        Triple(0f, 1f, 0f),
                        Triple(1f, 1f, 0f)
                    ),
                    faces = arrayOf(
                        arrayOf(
                            Triple(0, null, null),
                            Triple(1, null, null),
                            Triple(2, null, null)
                        )
                    )
                ),
                true
            )
        )

        initViewModel()

        for (testCase in testCases) {
            val gottenValidity = mViewModel.checkDrawingValidity(testCase.drawing)

            Assert.assertEquals(testCase.isValid, gottenValidity)
        }
    }
}