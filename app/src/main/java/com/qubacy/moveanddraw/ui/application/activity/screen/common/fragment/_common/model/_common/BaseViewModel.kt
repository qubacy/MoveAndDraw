package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.UiState

abstract class BaseViewModel<UiStateType : UiState>(
) : ViewModel() {
    protected abstract val mUiState: MutableLiveData<UiStateType?>
    val uiState: LiveData<UiStateType?> get() = mUiState
}