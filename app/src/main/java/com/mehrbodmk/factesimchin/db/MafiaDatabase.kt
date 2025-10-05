package com.mehrbodmk.factesimchin.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mehrbodmk.factesimchin.models.PlayerPresence
import java.util.concurrent.Executors


@Database(
    entities = [
        PlayerPresence::class
    ], version = 1
)
abstract class MafiaDatabase : RoomDatabase() {

    abstract fun daoBase() : DaoBase

    companion object {

        const val DATABASE_NAME = "mafia_db"

        @Volatile
        private var INSTANCE: MafiaDatabase? = null

        fun getInstance(context: Context, testMode: Boolean = false): MafiaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context, testMode).also {
                    INSTANCE = it
                }
            }
        }

        private fun buildDatabase(context: Context, testMode: Boolean): MafiaDatabase {
            return if (testMode) {
                Room.inMemoryDatabaseBuilder(context, MafiaDatabase::class.java)
                    .fallbackToDestructiveMigration( )
                    .allowMainThreadQueries()
            } else {
                @Suppress("SpreadOperator")
                Room.databaseBuilder(context, MafiaDatabase::class.java, DATABASE_NAME)
                    .setQueryExecutor(Executors.newSingleThreadExecutor())
                    .setTransactionExecutor(Executors.newSingleThreadExecutor())
                    .enableMultiInstanceInvalidation()
            }.build()
        }

    }
}