package com.example.data.model

import com.example.data.local.entity.ChittyEntity
import com.example.data.local.entity.MemberEntity
import com.example.data.local.entity.MonthlyInstallmentEntity

data class AdminDashboardMetrics(
    val totalChitties: Int = 0,
    val activeChitties: Int = 0,
    val totalMembers: Int = 0,
    val thisMonthExpectedCollection: Double = 0.0,
    val thisMonthCollected: Double = 0.0,
    val currentMonthOutstanding: Double = 0.0,
    val previousPending: Double = 0.0,
    val totalOutstanding: Double = 0.0
)

data class MemberDashboardSummary(
    val member: MemberEntity,
    val totalChitties: Int,
    val currentMonthDue: Double,
    val currentMonthPaid: Double = 0.0,
    val currentMonthPending: Double = 0.0,
    val previousOutstanding: Double,
    val totalDue: Double = currentMonthPending + previousOutstanding,
    val totalOutstanding: Double,
    val totalPaid: Double,
    val advanceCredit: Double,
    val chittySummaries: List<MemberChittySummary>
)

data class MemberChittySummary(
    val chitty: ChittyEntity,
    val shareNumber: Int,
    val monthlyInstallment: Double,
    val currentMonthDue: Double,
    val currentMonthPaid: Double = 0.0,
    val currentMonthPending: Double = 0.0,
    val previousOutstanding: Double,
    val totalDue: Double = currentMonthPending + previousOutstanding,
    val totalOutstanding: Double,
    val totalPaid: Double,
    val advanceAmount: Double,
    val hasReceived: Boolean,
    val receivedMonth: Int? = null,
    val receivedAmount: Double? = null,
    val installments: List<MonthlyInstallmentEntity> = emptyList()
)

data class MemberDuesOverview(
    val memberId: Long,
    val memberName: String,
    val memberCode: String,
    val mobileNumber: String,
    val enrolledChittiesCount: Int,
    val currentMonthPaying: Double,
    val currentMonthPaid: Double,
    val currentMonthPending: Double,
    val previousPending: Double,
    val totalDue: Double,
    val chittyBreakdowns: List<MemberChittyDuesItem> = emptyList()
)

data class MemberChittyDuesItem(
    val chittyId: Long,
    val chittyName: String,
    val monthlyInstallment: Double,
    val currentMonthDue: Double,
    val currentMonthPending: Double,
    val oldPending: Double,
    val totalDue: Double
)

data class ChittyAllocationInput(
    val chittyId: Long,
    val chittyName: String,
    val monthlyInstallment: Double,
    val currentDue: Double,
    val totalOutstanding: Double,
    var allocatedAmount: Double = 0.0,
    val targetMonth: Int? = null
)
