package com.qubacy.moveanddraw.data._common.repository.flowable

import com.qubacy.moveanddraw.data._common.repository._common.DataRepository
import com.qubacy.moveanddraw.data._common.repository.flowable.state.State
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow

abstract class FlowableDataRepository<StateType : State>(
    coroutineScope: CoroutineScope,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DataRepository(
    coroutineScope,
    coroutineDispatcher
) {
    protected val mStateFlow = MutableStateFlow<StateType?>(null)
    val stateFlow = mStateFlow
}