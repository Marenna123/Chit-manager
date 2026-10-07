package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val memberCode: String,
    val name: String,
    val mobileNumber: String,
    val address: String = "",
    val status: String = "ACTIVE", // ACTIVE, INACTIVE
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chitties")
data class ChittyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val chittyCode: String,
    val name: String,
    val totalAmount: Double,
    val durationMonths: Int,
    val monthlyInstallment: Double, // Entered by admin, not auto-calculated
    val startDate: String, // e.g. "2026-01-01"
    val dueDayOfMonth: Int = 10,
    val totalMembers: Int = 20,
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, DRAFT
    val adminId: Long = 1L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chitty_members"
)
data class ChittyMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val chittyId: Long,
    val memberId: Long,
    val shareNumber: Int = 1,
    val joinedDate: String = "",
    val advanceAmount: Double = 0.0, // Carry-over credit for extra payments
    val status: String = "ACTIVE", // ACTIVE, EXITED
    val createdAt: Long = System.currentTimeMillis()
)
