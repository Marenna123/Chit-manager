package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.MemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    fun getMemberById(id: Long): Flow<MemberEntity?>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    suspend fun getMemberByIdDirect(id: Long): MemberEntity?

    @Query("SELECT * FROM members WHERE name LIKE '%' || :query || '%' OR mobileNumber LIKE '%' || :query || '%' OR memberCode LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchMembers(query: String): Flow<List<MemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMembers(members: List<MemberEntity>)

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Delete
    suspend fun deleteMember(member: MemberEntity)

    @Query("SELECT * FROM members ORDER BY id ASC")
    suspend fun getAllMembersDirect(): List<MemberEntity>

    @Query("DELETE FROM members")
    suspend fun deleteAllMembers()

    @Query("SELECT COUNT(*) FROM members WHERE status = 'ACTIVE'")
    fun getActiveMembersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM members")
    fun getTotalMembersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM members")
    suspend fun getMembersCountDirect(): Int
}
