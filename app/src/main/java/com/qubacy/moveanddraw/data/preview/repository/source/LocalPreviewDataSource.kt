package com.qubacy.moveanddraw.data.preview.repository.source

import android.content.Context
import android.net.Uri
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw._common.util.resourceUri
import com.qubacy.moveanddraw.data._common.repository._common.source.DataSource
import javax.inject.Inject

class LocalPreviewDataSource @Inject constructor(
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

    fun getExamplePreviews(): List<Uri> {
        return EXAMPLE_PREVIEW_RESOURCE_ID_LIST.map { mContext.resourceUri(it) }
    }
}