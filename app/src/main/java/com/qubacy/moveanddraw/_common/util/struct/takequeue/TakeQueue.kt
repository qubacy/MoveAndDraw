package com.qubacy.moveanddraw._common.util.struct.takequeue

import java.util.LinkedList
import java.util.Queue

open class TakeQueue<ItemType>(
    vararg items: ItemType
) {
    private var mItemQueue: Queue<ItemType> = LinkedList<ItemType>().apply { addAll(items) }

    constructor(takeQueue: TakeQueue<ItemType>, vararg items: ItemType) : this() {
        mItemQueue = takeQueue.mItemQueue.apply { addAll(items) }
    }

    open fun take(): ItemType? {
        if (mItemQueue.isEmpty()) return null

        return mItemQueue.remove()
    }


}