package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.MonthlyInstallmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallmentDao {
    @Query("SELECT * FROM monthly_installments ORDER BY monthNumber ASC")
    fun getAllInstallments(): Flow<List<MonthlyInstallmentEntity>>

    @Query("SELECT * FROM monthly_installments")
    suspend fun getAllInstallmentsDirect(): List<MonthlyInstallmentEntity>

    @Query("SELECT * FROM monthly_installments WHERE chittyId = :chittyId ORDER BY monthNumber ASC, id ASC")
    fun getInstallmentsForChitty(chittyId: Long): Flow<List<MonthlyInstallmentEntity>>

    @Query("SELECT * FROM monthly_installments WHERE memberId = :memberId ORDER BY chittyId ASC, monthNumber ASC")
    fun getInstallmentsForMember(memberId: Long): Flow<List<MonthlyInstallmentEntity>>

    @Query("SELECT * FROM monthly_installments WHERE memberId = :memberId ORDER BY chittyId ASC, monthNumber ASC")
    suspend fun getInstallmentsForMemberDirect(memberId: Long): List<MonthlyInstallmentEntity>

    @Query("SELECT * FROM monthly_installments WHERE chittyId = :chittyId AND memberId = :memberId ORDER BY monthNumber ASC")
    fun getInstallmentsForChittyMember(chittyId: Long, memberId: Long): Flow<List<MonthlyInstallmentEntity>>

    @Query("SELECT * FROM monthly_installments WHERE chittyId = :chittyId AND memberId = :memberId ORDER BY monthNumber ASC")
    suspend fun getInstallmentsForChittyMemberDirect(chittyId: Long, memberId: Long): List<MonthlyInstallmentEntity>

    @Query("SELECT * FROM monthly_installments WHERE chittyId = :chittyId AND memberId = :memberId AND status != 'PAID' ORDER BY monthNumber ASC")
    suspend fun getUnpaidInstallmentsForChittyMember(chittyId: Long, memberId: Long): List<MonthlyInstallmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallments(installments: List<MonthlyInstallmentEntity>)

    @Update
    suspend fun updateInstallment(installment: MonthlyInstallmentEntity)

    @Update
    suspend fun updateInstallments(installments: List<MonthlyInstallmentEntity>)

    @Query("DELETE FROM monthly_installments WHERE chittyId = :chittyId AND memberId = :memberId")
    suspend fun deleteInstallmentsForChittyMember(chittyId: Long, memberId: Long)

    @Query("DELETE FROM monthly_installments WHERE chittyId = :chittyId")
    suspend fun deleteInstallmentsForChitty(chittyId: Long)

    @Query("DELETE FROM monthly_installments")
    suspend fun deleteAllInstallments()
}
