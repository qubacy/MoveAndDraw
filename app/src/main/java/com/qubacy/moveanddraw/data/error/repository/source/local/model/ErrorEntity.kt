package com.qubacy.moveanddraw.data.error.repository.source.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.qubacy.moveanddraw._common.error.Error

@Entity(
    tableName = ErrorEntity.TABLE_NAME
)
data class ErrorEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = ID_PROP_NAME) val id: Long,
    val message: String,
    @ColumnInfo(
        name = "is_critical",
        defaultValue = IS_CRITICAL_DEFAULT_VALUE
    ) val isCritical: Boolean
) {
    companion object {
        const val TABLE_NAME = "Error"

        const val ID_PROP_NAME = "id"

        const val IS_CRITICAL_DEFAULT_VALUE = "0"
    }
}

fun ErrorEntity.toError(): Error {
    return Error(id, message, isCritical)
}