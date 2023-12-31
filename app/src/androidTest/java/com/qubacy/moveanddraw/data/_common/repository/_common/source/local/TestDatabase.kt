package com.qubacy.moveanddraw.data._common.repository._common.source.local

import android.content.Context
import androidx.room.Room
import com.qubacy.moveanddraw.data._common.repository._common.source.local.database.Database

object TestDatabase {
    private var mDB: Database? = null

    fun getDatabase(context: Context): Database {
        if (mDB == null) {
            mDB = Room.databaseBuilder(
                context,
                Database::class.java,
                Database.DATABASE_NAME
            ).createFromAsset(Database.DATABASE_NAME)
                .fallbackToDestructiveMigration().build()
        }

        return mDB as Database
    }
}