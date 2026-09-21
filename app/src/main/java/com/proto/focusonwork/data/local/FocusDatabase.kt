package com.proto.focusonwork.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BlockedAppEntity::class], version = 1, exportSchema = false)
abstract class FocusDatabase : RoomDatabase() {
    abstract fun focusDao(): FocusDao

    companion object {
        fun create(context: Context): FocusDatabase =
            Room.databaseBuilder(context, FocusDatabase::class.java, "focus_on_work.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
