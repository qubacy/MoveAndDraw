package com.qubacy.moveanddraw.data.preview.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.qubacy.moveanddraw.data._common.repository._common.DataRepository
import com.qubacy.moveanddraw.data.preview.repository.source.LocalPreviewDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class PreviewDataRepository @Inject constructor(
    private val mLocalPreviewDataSource: LocalPreviewDataSource
) : DataRepository() {
    fun getExamplePreviews(): LiveData<List<Uri>> {
        val result = MutableLiveData<List<Uri>>()

        mCoroutineScope.launch(mCoroutineDispatcher) {
            val examplePreviews = mLocalPreviewDataSource.getExamplePreviews() // todo: can be suspend;

            mCoroutineScope.launch(Dispatchers.Main) {
                result.value = examplePreviews
            }
        }

        return result
    }
}