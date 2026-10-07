package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AdminEntity
import com.example.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminAndSettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SettingsEntity)

    @Query("SELECT * FROM admins WHERE id = 1 LIMIT 1")
    fun getAdmin(): Flow<AdminEntity?>

    @Query("SELECT * FROM admins WHERE id = 1 LIMIT 1")
    suspend fun getAdminDirect(): AdminEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAdmin(admin: AdminEntity)

    @Query("DELETE FROM admins")
    suspend fun deleteAllAdmins()

    @Query("DELETE FROM settings")
    suspend fun deleteAllSettings()
}
