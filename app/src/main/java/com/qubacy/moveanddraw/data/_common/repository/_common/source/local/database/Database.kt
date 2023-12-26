package com.qubacy.moveanddraw.data._common.repository._common.source.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.qubacy.moveanddraw.data.error.model.DataError
import com.qubacy.moveanddraw.data.error.repository.source.local.LocalErrorDataSource

@Database(entities = [DataError::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun errorDao(): LocalErrorDataSource
}