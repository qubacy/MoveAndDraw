package com.qubacy.moveanddraw.data.preview.repository.source.local

import android.content.Context
import android.net.Uri
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw._common.util.context.resourceUri
import com.qubacy.moveanddraw.data._common.repository._common.source._common.DataSource
import javax.inject.Inject

open class LocalPreviewDataSource @Inject constructor(
    private val mContext: Context
) : DataSource {
    companion object {
        val EXAMPLE_PREVIEW_RESOURCE_ID_LIST = listOf<Int>(
            R.drawable.example_drawing_1,
            R.drawable.example_drawing_1,
            R.drawable.example_drawing_1,
            R.drawable.example_drawing_1,
            R.drawable.example_drawing_1,
            R.drawable.example_drawing_1
        )
    }

    open fun getExamplePreviews(): List<Uri> {
        return EXAMPLE_PREVIEW_RESOURCE_ID_LIST.map { mContext.resourceUri(it) }
    }
}