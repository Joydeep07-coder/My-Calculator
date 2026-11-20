package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)
abstract class OmniCalcDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: OmniCalcDatabase? = null

        fun getInstance(context: Context): OmniCalcDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    OmniCalcDatabase::class.java,
                    "omni_calc_database",
                ).build().also { INSTANCE = it }
            }
        }
    }
}
