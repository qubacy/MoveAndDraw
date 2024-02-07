package com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment._common.model.business

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.test.platform.app.InstrumentationRegistry
import com.qubacy.moveanddraw.data._common.repository._common.source.local.TestDatabase
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.UiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model.business.BusinessViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model.business.BusinessViewModelModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito

@Module
@TestInstallIn(
    components = [ActivityRetainedComponent::class],
    replaces = [BusinessViewModelModule::class]
)
object FakeBusinessViewModelModule {
    abstract class BusinessViewModelFactory<UiStateType : UiState>(

    ) : AbstractSavedStateViewModelFactory() {
        override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            val viewModelMock = Mockito.mock(modelClass) as BusinessViewModel<UiStateType>

            Mockito.`when`(viewModelMock.uiState).thenReturn(MutableLiveData<UiStateType>())

            val mSavedStateHandleFieldReflection = BusinessViewModel::class.java
                .getDeclaredField("mSavedStateHandle")
                .apply { isAccessible = true }

            mSavedStateHandleFieldReflection.set(viewModelMock, SavedStateHandle())

            return viewModelMock as T
        }
    }

    @Provides
    fun provideErrorDataRepository(): ErrorDataRepository {
        val db = TestDatabase.getDatabase(InstrumentationRegistry.getInstrumentation().targetContext)

        return ErrorDataRepository(db.errorDao())
    }
}