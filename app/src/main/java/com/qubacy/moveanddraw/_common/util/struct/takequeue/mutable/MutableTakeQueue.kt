package com.qubacy.moveanddraw._common.util.struct.takequeue.mutable

import com.qubacy.moveanddraw._common.util.struct.takequeue._common.TakeQueue

open class MutableTakeQueue<ItemType>(

) : TakeQueue<ItemType>() {
    open suspend fun put(item: ItemType) {
        mMutex.lock()

        mItemQueue.add(item)

        mMutex.unlock()
    }
}