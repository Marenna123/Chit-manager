package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PaymentAllocationEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ReceiptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE isDeleted = 0 ORDER BY id DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE isDeleted = 0 ORDER BY id DESC")
    suspend fun getAllPaymentsDirect(): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE id = :id LIMIT 1")
    fun getPaymentById(id: Long): Flow<PaymentEntity?>

    @Query("SELECT * FROM payments WHERE id = :id LIMIT 1")
    suspend fun getPaymentByIdDirect(id: Long): PaymentEntity?

    @Query("SELECT * FROM payments WHERE memberId = :memberId AND isDeleted = 0 ORDER BY id DESC")
    fun getPaymentsForMember(memberId: Long): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPayments(payments: List<PaymentEntity>)

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Query("DELETE FROM payments")
    suspend fun deleteAllPayments()

    // Allocations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocations(allocations: List<PaymentAllocationEntity>)

    @Query("DELETE FROM payment_allocations")
    suspend fun deleteAllAllocations()

    @Query("SELECT * FROM payment_allocations WHERE paymentId = :paymentId")
    fun getAllocationsForPayment(paymentId: Long): Flow<List<PaymentAllocationEntity>>

    @Query("SELECT * FROM payment_allocations WHERE paymentId = :paymentId")
    suspend fun getAllocationsForPaymentDirect(paymentId: Long): List<PaymentAllocationEntity>

    @Query("SELECT * FROM payment_allocations")
    fun getAllAllocations(): Flow<List<PaymentAllocationEntity>>

    @Query("SELECT * FROM payment_allocations")
    suspend fun getAllAllocationsDirect(): List<PaymentAllocationEntity>

    // Receipts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReceipts(receipts: List<ReceiptEntity>)

    @Query("DELETE FROM receipts")
    suspend fun deleteAllReceipts()

    @Query("SELECT * FROM receipts ORDER BY id DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts ORDER BY id ASC")
    suspend fun getAllReceiptsDirect(): List<ReceiptEntity>

    @Query("SELECT * FROM receipts WHERE paymentId = :paymentId LIMIT 1")
    fun getReceiptByPaymentId(paymentId: Long): Flow<ReceiptEntity?>

    @Query("SELECT * FROM receipts WHERE receiptNumber = :receiptNumber LIMIT 1")
    suspend fun getReceiptByNumberDirect(receiptNumber: String): ReceiptEntity?

    @Query("SELECT COUNT(*) FROM receipts")
    suspend fun getReceiptCountDirect(): Int
}
