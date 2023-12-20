package com.qubacy.moveanddraw.ui.application.activity.screen.initial.model

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qubacy.moveanddraw.data.preview.repository.PreviewDataRepository
import com.qubacy.moveanddraw.data.preview.repository.source.LocalPreviewDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class InitialViewModel @Inject constructor(
    private val mPreviewDataRepository: PreviewDataRepository
) : ViewModel() {
    init {
        mPreviewDataRepository.setCoroutineScope(viewModelScope)
    }

    fun getExampleDrawingPreviews(): LiveData<List<Uri>> {
        return mPreviewDataRepository.getExamplePreviews()
    }
}

@Module
@InstallIn(ViewModelComponent::class)
object InitialViewModelModule {
    @Provides
    fun providePreviewDataRepository(
        context: Context
    ): PreviewDataRepository {
        return PreviewDataRepository(LocalPreviewDataSource(context))
    }

    @Provides
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
}

