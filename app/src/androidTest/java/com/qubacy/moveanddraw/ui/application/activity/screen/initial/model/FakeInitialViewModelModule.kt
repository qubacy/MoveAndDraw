package com.qubacy.moveanddraw.ui.application.activity.screen.initial.model

import android.content.Context
import com.qubacy.moveanddraw.data.preview.repository.PreviewDataRepository
import com.qubacy.moveanddraw.data.preview.repository.source.local.LocalPreviewDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [ViewModelComponent::class],
    replaces = [InitialViewModelModule::class]
)
object FakeInitialViewModelModule {
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
