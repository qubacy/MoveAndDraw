package com.qubacy.moveanddraw.domain.editor

import android.net.Uri
import com.qubacy.moveanddraw._common.exception.error.MADErrorException
import com.qubacy.moveanddraw.data.drawing.repository.DrawingDataRepository
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.domain._common.model.drawing._common.toDataDrawing
import com.qubacy.moveanddraw.domain._common.usecase.drawing.DrawingUseCase
import com.qubacy.moveanddraw.domain.editor.result.AddNewFaceToDrawingResult
import com.qubacy.moveanddraw.domain.editor.result.RemoveLastFaceFromDrawingResult
import com.qubacy.moveanddraw.domain.editor.result.SaveDrawingResult
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

                val savedDrawingPath =
                    if (drawingUri != null)
                        mDrawingDataRepository.saveDrawing(dataDrawing, drawingUri) // todo: can be suspend;
                    else
                        mDrawingDataRepository.saveNewDrawing(dataDrawing, filename!!)

                mResultFlow.emit(SaveDrawingResult(savedDrawingPath))

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
            val faces = drawing.faceArray.filterIndexed {
                index, face -> index != drawing.faceArray.size - 1
            }
            val editedDrawing = Drawing(
                drawing.uri,
                drawing.vertexArray,
                drawing.normalArray,
                drawing.textureArray,
                faces.toTypedArray()
            )

            mResultFlow.emit(RemoveLastFaceFromDrawingResult(editedDrawing))
        }
    }

    open fun addNewFaceToDrawing(
        drawing: Drawing? = null,
        faceVertexTripleArray: Array<Triple<Float, Float, Float>>,
        face: Array<Triple<Short, Short?, Short?>>
    ) {
        mCoroutineScope.launch(mCoroutineDispatcher) {
            val vertexIndexShift = drawing?.vertexArray?.size
            val shiftedFace = face.map {
                Triple((it.first + vertexIndexShift!!).toShort(), it.second, it.third)
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