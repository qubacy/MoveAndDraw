package com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment._common

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.qubacy.moveanddraw._common.error.Error
import com.qubacy.moveanddraw._common.util.struct.takequeue._common.TakeQueue
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.UiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation.error.ShowErrorUiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model.business.BusinessViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.base.BaseFragment
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.Before
import org.junit.Rule
import org.mockito.Mockito
import java.lang.reflect.Field

abstract class StatefulFragmentTest<
        UiStateType : UiState,
        ViewModelType : BusinessViewModel<UiStateType>,
        FragmentType : BaseFragment<UiStateType, ViewModelType>>(

) {
    companion object {
        val TEST_ERROR = Error(0, "test message!", false)
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    protected lateinit var mFragment: FragmentType

    protected lateinit var mModel: ViewModelType
    protected lateinit var mUiState: MutableLiveData<UiStateType>
    protected lateinit var mSavedStateHandle: SavedStateHandle

    protected lateinit var mNavController: TestNavHostController

    protected abstract fun retrieveModelFieldReflection(): Field
    protected abstract fun initFragment(modelFieldReflection: Field)

    @Before
    open fun setup() {
        mNavController = TestNavHostController(ApplicationProvider.getApplicationContext())

        val mModelFieldReflection = retrieveModelFieldReflection()

        initFragment(mModelFieldReflection)
        mockViewModel()
    }

    protected open fun setState(uiState: UiStateType) {
        mUiState.postValue(uiState)
    }

    protected open fun mockViewModel() {
        mUiState = mModel.uiState as MutableLiveData<UiStateType>
    }

    abstract fun generateUiStateWithUiOperation(operation: UiOperation): UiStateType

    protected open fun mockRetrieveError() {
        Mockito.`when`(mModel.retrieveError(Mockito.anyLong()))
            .thenAnswer {
                mUiState.value = generateUiStateWithUiOperation(
                    ShowErrorUiOperation(TEST_ERROR))

                Unit
            }
    }
}