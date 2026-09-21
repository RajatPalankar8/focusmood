package com.proto.focusonwork.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [BlockedAppEntity::class, SessionLogEntity::class], version = 2, exportSchema = false)
abstract class FocusDatabase : RoomDatabase() {
    abstract fun focusDao(): FocusDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS session_logs (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, startedAtMillis INTEGER NOT NULL, endedAtMillis INTEGER NOT NULL, durationMinutes INTEGER NOT NULL, wasCompleted INTEGER NOT NULL, emergencyUnlock INTEGER NOT NULL DEFAULT 0, blockedAppCount INTEGER NOT NULL DEFAULT 0)")
            }
        }

        fun create(context: Context): FocusDatabase =
            Room.databaseBuilder(context, FocusDatabase::class.java, "focus_on_work.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
