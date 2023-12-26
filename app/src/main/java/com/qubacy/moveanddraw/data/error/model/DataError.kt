package com.qubacy.moveanddraw.data.error.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = DataError.TABLE_NAME
)
data class DataError(
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