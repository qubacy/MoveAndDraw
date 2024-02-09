package com.qubacy.moveanddraw.domain.editor

import android.net.Uri
import com.qubacy.moveanddraw._common.exception.error.MADErrorException
import com.qubacy.moveanddraw.data.drawing.repository.DrawingDataRepository
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.domain._common.model.drawing._common.toDataDrawing
import com.qubacy.moveanddraw.domain._common.model.drawing.util.DrawingUtil
import com.qubacy.moveanddraw.domain._common.usecase.drawing.DrawingUseCase
import com.qubacy.moveanddraw.domain.editor.result.face.add.AddNewFaceToDrawingResult
import com.qubacy.moveanddraw.domain.editor.result.face.remove.RemoveLastFaceFromDrawingResult
import com.qubacy.moveanddraw.domain.editor.result.save.SaveDrawingResult
import kotlinx.coroutines.launch
import javax.inject.Inject

class EditorUseCase @Inject constructor(
    errorDataRepository: ErrorDataRepository,
    drawingDataRepository: DrawingDataRepository
) : DrawingUseCase(
    errorDataRepository,
    drawingDataRepository
) {
    open fun saveDrawing(drawing: Drawing, drawingUri: Uri? = null, filename: String? = null) {
        mCoroutineScope.launch(mCoroutineDispatcher) {
            try {
                val dataDrawing = drawing.toDataDrawing()

                val savedDrawingResult =
                    if (drawingUri != null)
                        mDrawingDataRepository.saveDrawing(dataDrawing, drawingUri) // todo: can be suspend;
                    else
                        mDrawingDataRepository.saveNewDrawing(dataDrawing, filename!!)

                val finalDrawing = Drawing(
                    savedDrawingResult.uri,
                    drawing.vertexArray,
                    drawing.normalArray,
                    drawing.textureArray,
                    drawing.faceArray
                )

                mResultFlow.emit(SaveDrawingResult(finalDrawing, savedDrawingResult.filePath))

            } catch (e: MADErrorException) {
                onErrorCaught(e.errorId)

            } catch (e: Exception) {
                e.printStackTrace()

                throw e
            }
        }
    }

    open fun removeLastFaceFromDrawing(drawing: Drawing) {
        mCoroutineScope.launch(mCoroutineDispatcher) {
            val faces = drawing.faceArray.sliceArray(0 until drawing.faceArray.size - 1)
            val verticesFacesPair = DrawingUtil.filterVertexArrayWithFaces(drawing.vertexArray, faces)

            val editedDrawing = Drawing(
                drawing.uri,
                verticesFacesPair.first,
                drawing.normalArray,
                drawing.textureArray,
                verticesFacesPair.second
            )

            mResultFlow.emit(RemoveLastFaceFromDrawingResult(editedDrawing))
        }
    }

    open fun addNewFaceToDrawing(
        drawing: Drawing? = null,
        faceVertexTripleArray: Array<Triple<Float, Float, Float>>,
        face: Array<Triple<Int, Int?, Int?>>
    ) {
        mCoroutineScope.launch(mCoroutineDispatcher) {
            val vertexIndexShift = drawing?.vertexArray?.size ?: 0
            val shiftedFace =
                face.map {
                    Triple((it.first + vertexIndexShift), it.second, it.third)
                }.toTypedArray()

            val finalVertexArray = drawing?.vertexArray?.plus(faceVertexTripleArray)
                ?: faceVertexTripleArray
            val finalFaceArray = drawing?.faceArray?.plus(shiftedFace) ?: arrayOf(face)

            val renewedDrawing = Drawing(
                drawing?.uri,
                finalVertexArray,
                drawing?.normalArray ?: floatArrayOf(),
                drawing?.textureArray ?: floatArrayOf(),
                finalFaceArray
            )

            mResultFlow.emit(AddNewFaceToDrawingResult(renewedDrawing))
        }
    }
}