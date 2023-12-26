package com.qubacy.moveanddraw.ui.application.activity.screen.initial.model

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.qubacy.moveanddraw.data._common.repository._common.source.local.TestDatabase
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.data.preview.repository.PreviewDataRepository
import com.qubacy.moveanddraw.data.preview.repository.source.local.LocalPreviewDataSource
import com.qubacy.moveanddraw.domain.initial.InitialUseCase
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
    fun provideInitialUseCase(
        context: Context
    ): InitialUseCase {
        val db = TestDatabase.getDatabase(InstrumentationRegistry.getInstrumentation().targetContext)

        val errorDataRepository = ErrorDataRepository(db.errorDao())
        val previewDataRepository = PreviewDataRepository(LocalPreviewDataSource(context))

        return InitialUseCase(errorDataRepository, previewDataRepository)
    }

    @Provides
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
}
