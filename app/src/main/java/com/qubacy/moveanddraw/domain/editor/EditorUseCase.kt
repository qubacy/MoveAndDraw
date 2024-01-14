package com.qubacy.moveanddraw.domain.editor

import android.net.Uri
import com.qubacy.moveanddraw._common.exception.error.MADErrorException
import com.qubacy.moveanddraw.data.drawing.model.toDrawing
import com.qubacy.moveanddraw.data.drawing.repository.DrawingDataRepository
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain._common.model.drawing.Drawing
import com.qubacy.moveanddraw.domain._common.model.drawing.toDataDrawing
import com.qubacy.moveanddraw.domain._common.usecase.drawing.DrawingUseCase
import com.qubacy.moveanddraw.domain._common.usecase.drawing.result.LoadDrawingResult
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
}