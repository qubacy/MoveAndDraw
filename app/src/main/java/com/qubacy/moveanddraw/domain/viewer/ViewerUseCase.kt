package com.qubacy.moveanddraw.domain.viewer

import android.net.Uri
import com.qubacy.moveanddraw._common.exception.error.MADErrorException
import com.qubacy.moveanddraw.data.drawing.model.toDrawing
import com.qubacy.moveanddraw.data.drawing.repository.DrawingDataRepository
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain._common.usecase.UseCase
import com.qubacy.moveanddraw.domain.viewer.result.LoadDrawingResult
import kotlinx.coroutines.launch
import javax.inject.Inject

open class ViewerUseCase @Inject constructor(
    errorDataRepository: ErrorDataRepository,
    private val mDrawingDataRepository: DrawingDataRepository
) : UseCase(
    errorDataRepository
) {
    open fun loadDrawing(drawingUri: Uri) {
        mCoroutineScope.launch(mCoroutineDispatcher) {
            try {
                val loadedDrawing = mDrawingDataRepository.loadDrawing(drawingUri) // todo: can be suspend;

                mResultFlow.emit(LoadDrawingResult(loadedDrawing.toDrawing()))

            } catch (e: MADErrorException) {
                onErrorCaught(e.errorId)

            } catch (e: Exception) {
                e.printStackTrace()

                throw e
            }
        }
    }
}