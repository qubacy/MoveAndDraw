package com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment._common.model.business

import androidx.test.platform.app.InstrumentationRegistry
import com.qubacy.moveanddraw.data._common.repository._common.source.local.TestDatabase
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model.business.BusinessViewModelModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [ActivityRetainedComponent::class],
    replaces = [BusinessViewModelModule::class]
)
object FakeBusinessViewModelModule {
    @Provides
    fun provideErrorDataRepository(): ErrorDataRepository {
        val db = TestDatabase.getDatabase(InstrumentationRegistry.getInstrumentation().targetContext)

        return ErrorDataRepository(db.errorDao())
    }
}