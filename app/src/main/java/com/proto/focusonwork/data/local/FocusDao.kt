package com.proto.focusonwork.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {
    @Query("SELECT * FROM blocked_apps ORDER BY displayName")
    fun observeBlockedApps(): Flow<List<BlockedAppEntity>>

    @Upsert
    suspend fun upsertBlockedApp(app: BlockedAppEntity)

    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun deleteBlockedApp(packageName: String)
}
