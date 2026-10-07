package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_installments")
data class MonthlyInstallmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val chittyId: Long,
    val memberId: Long,
    val monthNumber: Int, // 1 to durationMonths
    val monthLabel: String, // e.g. "Month 1 (Oct 2026)"
    val dueDate: String,
    val dueAmount: Double,
    val paidAmount: Double = 0.0,
    val outstandingAmount: Double, // Initialized to dueAmount, reduced as payments come
    val status: String = "UNPAID", // UNPAID, PARTIALLY_PAID, PAID
    val paymentDate: String? = null,
    val notes: String = ""
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val receiptNumber: String,
    val memberId: Long,
    val totalAmount: Double,
    val paymentDate: String,
    val paymentMethod: String = "Cash", // Cash, UPI, Bank Transfer, Cheque, Other
    val notes: String = "",
    val isCombined: Boolean = false,
    val isDeleted: Boolean = false, // Never permanently delete transactions
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payment_allocations")
data class PaymentAllocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val paymentId: Long,
    val chittyId: Long,
    val memberId: Long,
    val installmentId: Long? = null,
    val monthNumber: Int? = null,
    val allocatedAmount: Double,
    val notes: String = ""
)

@Entity(tableName = "chitty_recipients")
data class ChittyRecipientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val chittyId: Long,
    val monthNumber: Int,
    val memberId: Long,
    val amountReceived: Double,
    val receivedDate: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val paymentId: Long,
    val receiptNumber: String,
    val memberId: Long,
    val memberName: String,
    val memberMobile: String,
    val amount: Double,
    val date: String,
    val paymentMethod: String,
    val allocationSummary: String, // Text summary of chitty distributions
    val adminName: String,
    val createdAt: Long = System.currentTimeMillis()
)
