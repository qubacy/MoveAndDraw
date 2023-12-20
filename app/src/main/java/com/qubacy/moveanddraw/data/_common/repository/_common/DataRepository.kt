package com.qubacy.moveanddraw.data._common.repository._common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

abstract class DataRepository(
    protected var mCoroutineScope: CoroutineScope = GlobalScope,
    protected var mCoroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun setCoroutineScope(coroutineScope: CoroutineScope) {
        mCoroutineScope = coroutineScope
    }

    fun setCoroutineDispatcher(coroutineDispatcher: CoroutineDispatcher) {
        mCoroutineDispatcher = coroutineDispatcher
    }
}