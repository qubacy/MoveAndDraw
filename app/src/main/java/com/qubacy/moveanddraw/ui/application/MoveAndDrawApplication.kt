package com.qubacy.moveanddraw.ui.application

import android.app.Application
import androidx.room.Room
import com.qubacy.moveanddraw.data._common.repository._common.source.local.database.Database
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MoveAndDrawApplication : Application() {
    private lateinit var mDB: Database
    val db get() = mDB

    override fun onCreate() {
        super.onCreate()

        mDB = Room.databaseBuilder(
            this, Database::class.java, Database.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .createFromAsset(Database.DATABASE_NAME)
            .build()
    }
}