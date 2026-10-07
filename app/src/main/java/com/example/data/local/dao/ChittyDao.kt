package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ChittyEntity
import com.example.data.local.entity.ChittyMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChittyDao {
    @Query("SELECT * FROM chitties ORDER BY id DESC")
    fun getAllChitties(): Flow<List<ChittyEntity>>

    @Query("SELECT * FROM chitties WHERE id = :id LIMIT 1")
    fun getChittyById(id: Long): Flow<ChittyEntity?>

    @Query("SELECT * FROM chitties WHERE id = :id LIMIT 1")
    suspend fun getChittyByIdDirect(id: Long): ChittyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChitty(chitty: ChittyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllChitties(chitties: List<ChittyEntity>)

    @Update
    suspend fun updateChitty(chitty: ChittyEntity)

    @Delete
    suspend fun deleteChitty(chitty: ChittyEntity)

    @Query("SELECT * FROM chitties ORDER BY id ASC")
    suspend fun getAllChittiesDirect(): List<ChittyEntity>

    @Query("DELETE FROM chitties")
    suspend fun deleteAllChitties()

    @Query("SELECT * FROM chitty_members ORDER BY id ASC")
    suspend fun getAllChittyMembersDirect(): List<ChittyMemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllChittyMembers(members: List<ChittyMemberEntity>)

    @Query("DELETE FROM chitty_members")
    suspend fun deleteAllChittyMembers()

    @Query("SELECT COUNT(*) FROM chitties")
    fun getTotalChittiesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM chitties WHERE status = 'ACTIVE'")
    fun getActiveChittiesCount(): Flow<Int>

    // Chitty Members queries
    @Query("SELECT * FROM chitty_members WHERE chittyId = :chittyId ORDER BY id ASC")
    fun getChittyMembers(chittyId: Long): Flow<List<ChittyMemberEntity>>

    @Query("SELECT * FROM chitty_members WHERE chittyId = :chittyId")
    suspend fun getChittyMembersDirect(chittyId: Long): List<ChittyMemberEntity>

    @Query("SELECT * FROM chitty_members WHERE memberId = :memberId")
    fun getChittyMembershipsForMember(memberId: Long): Flow<List<ChittyMemberEntity>>

    @Query("SELECT * FROM chitty_members WHERE memberId = :memberId")
    suspend fun getChittyMembershipsForMemberDirect(memberId: Long): List<ChittyMemberEntity>

    @Query("SELECT * FROM chitty_members WHERE chittyId = :chittyId AND memberId = :memberId LIMIT 1")
    suspend fun getChittyMember(chittyId: Long, memberId: Long): ChittyMemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChittyMember(chittyMember: ChittyMemberEntity): Long

    @Update
    suspend fun updateChittyMember(chittyMember: ChittyMemberEntity)

    @Delete
    suspend fun deleteChittyMember(chittyMember: ChittyMemberEntity)

    @Query("DELETE FROM chitty_members WHERE chittyId = :chittyId AND memberId = :memberId")
    suspend fun removeMemberFromChitty(chittyId: Long, memberId: Long)
}
