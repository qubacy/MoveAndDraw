package com.qubacy.moveanddraw.domain.initial

import com.qubacy.moveanddraw._common.exception.error.MADErrorException
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.data.preview.repository.PreviewDataRepository
import com.qubacy.moveanddraw.domain._common.usecase._common.UseCase
import com.qubacy.moveanddraw.domain.initial.result.GetExamplePreviewsResult
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

open class InitialUseCase @Inject constructor(
    errorDataRepository: ErrorDataRepository,
    private val mPreviewDataRepository: PreviewDataRepository
) : UseCase(errorDataRepository) {
    open fun getExamplePreviews() {
        mCoroutineScope.launch(mCoroutineDispatcher) {
            try {
                val examplePreviews = mPreviewDataRepository.getExamplePreviews() // todo: can be suspend;

                mResultFlow.emit(GetExamplePreviewsResult(examplePreviews))

            } catch (e: MADErrorException) {
                onErrorCaught(e.errorId)

            } catch (e: Exception) {
                e.printStackTrace()

                throw e
            }
        }
    }
}