package com.qubacy.moveanddraw.data.error.repository.source.local

import androidx.room.Dao
import androidx.room.Query
import com.qubacy.moveanddraw.data._common.repository._common.source._common.DataSource
import com.qubacy.moveanddraw.data.error.model.DataError

@Dao
interface LocalErrorDataSource : DataSource {
    @Query("SELECT * FROM ${DataError.TABLE_NAME} WHERE ${DataError.ID_PROP_NAME} = :id")
    fun getErrorById(id: Long): DataError?
}