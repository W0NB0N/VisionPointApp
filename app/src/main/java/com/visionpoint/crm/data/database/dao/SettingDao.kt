package com.visionpoint.crm.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.visionpoint.crm.data.model.Setting
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(setting: Setting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(settings: List<Setting>)

    @Query("SELECT * FROM settings")
    fun getAll(): Flow<List<Setting>>

    @Query("SELECT value FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun getValue(key: String): String?

    @Query("SELECT * FROM settings WHERE `key` = :key LIMIT 1")
    fun getByKey(key: String): Flow<Setting?>
}
