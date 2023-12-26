package com.qubacy.moveanddraw.data.preview.repository

import android.net.Uri
import com.qubacy.moveanddraw.data._common.repository._common.DataRepository
import com.qubacy.moveanddraw.data.preview.repository.source.local.LocalPreviewDataSource
import javax.inject.Inject

open class PreviewDataRepository @Inject constructor(
    private val mLocalPreviewDataSource: LocalPreviewDataSource
) : DataRepository() {
    open fun getExamplePreviews(): List<Uri> {
        return mLocalPreviewDataSource.getExamplePreviews() // todo: can be suspend;
    }
}