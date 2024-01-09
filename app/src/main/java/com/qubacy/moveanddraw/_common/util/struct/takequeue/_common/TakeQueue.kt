package com.qubacy.moveanddraw._common.util.struct.takequeue._common

import kotlinx.coroutines.sync.Mutex
import java.util.LinkedList
import java.util.Queue

open class TakeQueue<ItemType> {
    protected var mItemQueue: Queue<ItemType>
    protected val mMutex = Mutex(false)

    constructor(vararg items: ItemType) {
        mItemQueue = LinkedList<ItemType>().apply { addAll(items) }
    }

    constructor(takeQueue: TakeQueue<ItemType>, vararg items: ItemType) : this() {
        mItemQueue = takeQueue.mItemQueue.apply { addAll(items) }
    }

    open suspend fun take(): ItemType? {
        mMutex.lock()

        if (mItemQueue.isEmpty()) {
            mMutex.unlock()

            return null
        }

        val removedItem = mItemQueue.remove()

        mMutex.unlock()

        return removedItem
    }
}