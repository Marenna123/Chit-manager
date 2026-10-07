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
    val totalOutstanding: Double = 0.0
)

data class MemberDashboardSummary(
    val member: MemberEntity,
    val totalChitties: Int,
    val currentMonthDue: Double,
    val previousOutstanding: Double,
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
    val previousOutstanding: Double,
    val totalDue: Double,
    val totalOutstanding: Double,
    val totalPaid: Double,
    val advanceAmount: Double,
    val hasReceived: Boolean,
    val receivedMonth: Int? = null,
    val receivedAmount: Double? = null,
    val installments: List<MonthlyInstallmentEntity> = emptyList()
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
