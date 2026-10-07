package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ChittyRecipientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipientDao {
    @Query("SELECT * FROM chitty_recipients ORDER BY id DESC")
    fun getAllRecipients(): Flow<List<ChittyRecipientEntity>>

    @Query("SELECT * FROM chitty_recipients")
    suspend fun getAllRecipientsDirect(): List<ChittyRecipientEntity>

    @Query("SELECT * FROM chitty_recipients WHERE chittyId = :chittyId ORDER BY monthNumber ASC")
    fun getRecipientsForChitty(chittyId: Long): Flow<List<ChittyRecipientEntity>>

    @Query("SELECT * FROM chitty_recipients WHERE chittyId = :chittyId ORDER BY monthNumber ASC")
    suspend fun getRecipientsForChittyDirect(chittyId: Long): List<ChittyRecipientEntity>

    @Query("SELECT * FROM chitty_recipients WHERE memberId = :memberId")
    fun getRecipientsForMember(memberId: Long): Flow<List<ChittyRecipientEntity>>

    @Query("SELECT * FROM chitty_recipients WHERE memberId = :memberId")
    suspend fun getRecipientsForMemberDirect(memberId: Long): List<ChittyRecipientEntity>

    @Query("SELECT * FROM chitty_recipients WHERE chittyId = :chittyId AND memberId = :memberId LIMIT 1")
    suspend fun getRecipientByChittyAndMember(chittyId: Long, memberId: Long): ChittyRecipientEntity?

    @Query("SELECT * FROM chitty_recipients WHERE chittyId = :chittyId AND monthNumber = :monthNumber LIMIT 1")
    suspend fun getRecipientByChittyAndMonth(chittyId: Long, monthNumber: Int): ChittyRecipientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipient(recipient: ChittyRecipientEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRecipients(recipients: List<ChittyRecipientEntity>)

    @Update
    suspend fun updateRecipient(recipient: ChittyRecipientEntity)

    @Delete
    suspend fun deleteRecipient(recipient: ChittyRecipientEntity)

    @Query("DELETE FROM chitty_recipients")
    suspend fun deleteAllRecipients()
}
