package com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qubacy.moveanddraw._common.util.struct.takequeue.TakeQueue
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain.viewer.ViewerUseCase
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model.business.BusinessViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model.state.ViewerUiState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Qualifier

@HiltViewModel
open class ViewerViewModel @Inject constructor(
    private val mViewerUseCase: ViewerUseCase
) : BusinessViewModel<ViewerUiState>(mViewerUseCase) {


    override fun getUiStateWithUiOperation(uiOperation: UiOperation): ViewerUiState {
        return ViewerUiState(TakeQueue(uiOperation))
    }
}

class ViewerViewModelFactory(
    private val mViewerUseCase: ViewerUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(ViewerViewModel::class.java))
            throw IllegalArgumentException()

        return ViewerViewModel(mViewerUseCase) as T
    }
}

@Qualifier
annotation class ViewerViewModelFactoryQualifier

@Module
@InstallIn(ActivityRetainedComponent::class)
object ViewerViewModelFactoryModule {
    @Provides
    @ViewerViewModelFactoryQualifier
    fun provideViewerViewModelFactory(
        viewerUseCase: ViewerUseCase
    ): ViewModelProvider.Factory {
        return ViewerViewModelFactory(viewerUseCase)
    }

    @Provides
    fun provideViewerUseCase(
        errorDataRepository: ErrorDataRepository,
        @ApplicationContext context: Context
    ): ViewerUseCase {
        return ViewerUseCase(errorDataRepository)
    }
}
