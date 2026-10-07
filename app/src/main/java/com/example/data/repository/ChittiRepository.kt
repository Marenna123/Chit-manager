package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.AdminEntity
import com.example.data.local.entity.ChittyEntity
import com.example.data.local.entity.ChittyMemberEntity
import com.example.data.local.entity.ChittyRecipientEntity
import com.example.data.local.entity.MemberEntity
import com.example.data.local.entity.MonthlyInstallmentEntity
import com.example.data.local.entity.PaymentAllocationEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ReceiptEntity
import com.example.data.local.entity.SettingsEntity
import com.example.data.model.AdminDashboardMetrics
import com.example.data.model.ChittyAllocationInput
import com.example.data.model.MemberChittySummary
import com.example.data.model.MemberDashboardSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChittiRepository(private val database: AppDatabase) {

    private val adminDao = database.adminAndSettingsDao()
    private val memberDao = database.memberDao()
    private val chittyDao = database.chittyDao()
    private val installmentDao = database.installmentDao()
    private val paymentDao = database.paymentDao()
    private val recipientDao = database.recipientDao()

    // Settings & Admin
    val settings: Flow<SettingsEntity?> = adminDao.getSettings()
    val admin: Flow<AdminEntity?> = adminDao.getAdmin()

    suspend fun saveAdminSetup(name: String, mobileNumber: String) {
        val admin = AdminEntity(id = 1L, name = name.trim(), mobileNumber = mobileNumber.trim())
        adminDao.saveAdmin(admin)

        val currentSettings = adminDao.getSettingsDirect() ?: SettingsEntity()
        adminDao.saveSettings(
            currentSettings.copy(
                adminName = name.trim(),
                mobileNumber = mobileNumber.trim(),
                isFirstTimeSetupCompleted = true,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateSettings(
        adminName: String,
        mobileNumber: String,
        reminderHeader: String,
        customNotes: String
    ) {
        adminDao.saveAdmin(AdminEntity(id = 1L, name = adminName.trim(), mobileNumber = mobileNumber.trim()))
        val current = adminDao.getSettingsDirect() ?: SettingsEntity()
        adminDao.saveSettings(
            current.copy(
                adminName = adminName.trim(),
                mobileNumber = mobileNumber.trim(),
                reminderHeader = reminderHeader.trim(),
                customNotes = customNotes.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateThemeMode(themeMode: String) {
        val current = adminDao.getSettingsDirect() ?: SettingsEntity()
        adminDao.saveSettings(
            current.copy(
                themeMode = themeMode,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // Backup & Restore
    suspend fun exportDatabaseToJson(): String {
        return com.example.data.backup.BackupManager.exportDatabaseToJson(database)
    }

    fun inspectBackup(jsonString: String): com.example.data.backup.BackupSummary {
        return com.example.data.backup.BackupManager.inspectBackup(jsonString)
    }

    suspend fun restoreDatabaseFromJson(jsonString: String): com.example.data.backup.BackupSummary {
        return com.example.data.backup.BackupManager.restoreDatabaseFromJson(database, jsonString)
    }

    // Members
    val allMembers: Flow<List<MemberEntity>> = memberDao.getAllMembers()

    fun searchMembers(query: String): Flow<List<MemberEntity>> =
        if (query.isBlank()) memberDao.getAllMembers() else memberDao.searchMembers(query.trim())

    fun getMemberById(id: Long): Flow<MemberEntity?> = memberDao.getMemberById(id)

    suspend fun addMember(name: String, mobileNumber: String, address: String): Long {
        val totalMembers = memberDao.getMembersCountDirect()
        val code = "M%03d".format(totalMembers + 1)
        val member = MemberEntity(
            memberCode = code,
            name = name.trim(),
            mobileNumber = mobileNumber.trim(),
            address = address.trim(),
            status = "ACTIVE"
        )
        return memberDao.insertMember(member)
    }

    suspend fun updateMember(member: MemberEntity) {
        memberDao.updateMember(member)
    }

    suspend fun toggleMemberStatus(memberId: Long) {
        val member = memberDao.getMemberByIdDirect(memberId) ?: return
        val newStatus = if (member.status == "ACTIVE") "INACTIVE" else "ACTIVE"
        memberDao.updateMember(member.copy(status = newStatus))
    }

    // Chitties
    val allChitties: Flow<List<ChittyEntity>> = chittyDao.getAllChitties()

    fun getChittyById(id: Long): Flow<ChittyEntity?> = chittyDao.getChittyById(id)

    suspend fun createChitty(
        name: String,
        totalAmount: Double,
        durationMonths: Int,
        monthlyInstallment: Double, // Entered by admin, not auto-calculated
        startDate: String,
        dueDayOfMonth: Int,
        totalMembers: Int
    ): Long {
        val count = chittyDao.getTotalChittiesCount().first()
        val code = "CHT-%03d".format(count + 1)
        val chitty = ChittyEntity(
            chittyCode = code,
            name = name.trim(),
            totalAmount = totalAmount,
            durationMonths = durationMonths,
            monthlyInstallment = monthlyInstallment,
            startDate = startDate,
            dueDayOfMonth = dueDayOfMonth,
            totalMembers = totalMembers,
            status = "ACTIVE"
        )
        return chittyDao.insertChitty(chitty)
    }

    suspend fun updateChitty(chitty: ChittyEntity) {
        chittyDao.updateChitty(chitty)
    }

    fun getChittyMembers(chittyId: Long): Flow<List<ChittyMemberEntity>> =
        chittyDao.getChittyMembers(chittyId)

    suspend fun getChittyMembershipsDirect(memberId: Long): List<ChittyMemberEntity> =
        chittyDao.getChittyMembershipsForMemberDirect(memberId)

    suspend fun addMemberToChitty(chittyId: Long, memberId: Long, shareNumber: Int = 1) {
        val existing = chittyDao.getChittyMember(chittyId, memberId)
        if (existing != null) return

        val chitty = chittyDao.getChittyByIdDirect(chittyId) ?: return
        val joinedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

        val chittyMember = ChittyMemberEntity(
            chittyId = chittyId,
            memberId = memberId,
            shareNumber = shareNumber,
            joinedDate = joinedDate
        )
        chittyDao.insertChittyMember(chittyMember)

        // Generate monthly installments for this member in this chitty
        val installments = mutableListOf<MonthlyInstallmentEntity>()
        val startCal = Calendar.getInstance()
        // Try parsing startDate, fallback to now
        try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(chitty.startDate)
                ?: SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(chitty.startDate)
            if (parsed != null) startCal.time = parsed
        } catch (_: Exception) {
            startCal.time = Date()
        }

        val monthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val dueDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        for (m in 1..chitty.durationMonths) {
            val dueCal = Calendar.getInstance().apply {
                time = startCal.time
                add(Calendar.MONTH, m - 1)
                set(Calendar.DAY_OF_MONTH, chitty.dueDayOfMonth.coerceAtMost(getActualMaximum(Calendar.DAY_OF_MONTH)))
            }

            val monthLabel = "Month $m (${monthFormat.format(dueCal.time)})"
            val dueDateStr = dueDateFormat.format(dueCal.time)

            installments.add(
                MonthlyInstallmentEntity(
                    chittyId = chittyId,
                    memberId = memberId,
                    monthNumber = m,
                    monthLabel = monthLabel,
                    dueDate = dueDateStr,
                    dueAmount = chitty.monthlyInstallment,
                    paidAmount = 0.0,
                    outstandingAmount = chitty.monthlyInstallment,
                    status = "UNPAID"
                )
            )
        }
        installmentDao.insertInstallments(installments)
    }

    suspend fun removeMemberFromChitty(chittyId: Long, memberId: Long) {
        chittyDao.removeMemberFromChitty(chittyId, memberId)
        installmentDao.deleteInstallmentsForChittyMember(chittyId, memberId)
    }

    // Installments
    fun getInstallmentsForChitty(chittyId: Long): Flow<List<MonthlyInstallmentEntity>> =
        installmentDao.getInstallmentsForChitty(chittyId)

    fun getInstallmentsForMember(memberId: Long): Flow<List<MonthlyInstallmentEntity>> =
        installmentDao.getInstallmentsForMember(memberId)

    fun getInstallmentsForChittyMember(chittyId: Long, memberId: Long): Flow<List<MonthlyInstallmentEntity>> =
        installmentDao.getInstallmentsForChittyMember(chittyId, memberId)

    // Payments
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()
    val allReceipts: Flow<List<ReceiptEntity>> = paymentDao.getAllReceipts()

    fun getPaymentsForMember(memberId: Long): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsForMember(memberId)

    fun getReceiptByPaymentId(paymentId: Long): Flow<ReceiptEntity?> =
        paymentDao.getReceiptByPaymentId(paymentId)

    /**
     * Record payment (Single or Combined across multiple chitties)
     */
    suspend fun recordPayment(
        memberId: Long,
        totalAmount: Double,
        paymentDate: String,
        paymentMethod: String,
        notes: String,
        allocations: List<Pair<Long, Double>> // Pair(chittyId, amountAllocated)
    ): Long {
        val member = memberDao.getMemberByIdDirect(memberId) ?: return 0L
        val receiptCount = paymentDao.getReceiptCountDirect()
        val datePrefix = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date())
        val receiptNumber = "RCP-$datePrefix-%04d".format(receiptCount + 1)

        val payment = PaymentEntity(
            receiptNumber = receiptNumber,
            memberId = memberId,
            totalAmount = totalAmount,
            paymentDate = paymentDate,
            paymentMethod = paymentMethod,
            notes = notes,
            isCombined = allocations.size > 1
        )
        val paymentId = paymentDao.insertPayment(payment)

        val allocationEntities = mutableListOf<PaymentAllocationEntity>()
        val allocationSummaryLines = mutableListOf<String>()

        for ((chittyId, allocatedAmount) in allocations) {
            if (allocatedAmount <= 0) continue
            val chitty = chittyDao.getChittyByIdDirect(chittyId) ?: continue

            allocationEntities.add(
                PaymentAllocationEntity(
                    paymentId = paymentId,
                    chittyId = chittyId,
                    memberId = memberId,
                    allocatedAmount = allocatedAmount,
                    notes = notes
                )
            )
            allocationSummaryLines.add("${chitty.name}: ₹${allocatedAmount.toLong()}")

            // Apply payment to installments sequentially for this chitty
            applyPaymentToInstallments(chittyId, memberId, allocatedAmount, paymentDate)
        }

        paymentDao.insertAllocations(allocationEntities)

        // Save receipt
        val currentAdmin = adminDao.getAdminDirect()?.name ?: "Management"
        val receipt = ReceiptEntity(
            paymentId = paymentId,
            receiptNumber = receiptNumber,
            memberId = memberId,
            memberName = member.name,
            memberMobile = member.mobileNumber,
            amount = totalAmount,
            date = paymentDate,
            paymentMethod = paymentMethod,
            allocationSummary = allocationSummaryLines.joinToString("\n"),
            adminName = currentAdmin
        )
        paymentDao.insertReceipt(receipt)

        return paymentId
    }

    /**
     * Sequential installment allocation logic:
     * - Handles Missed Payments carry-forward (pays oldest unpaid first)
     * - Handles Partial Payments (status -> PARTIALLY_PAID, remainder carries forward)
     * - Handles Advance Payments (if remaining > all due, stored in chitty_members.advanceAmount)
     */
    private suspend fun applyPaymentToInstallments(
        chittyId: Long,
        memberId: Long,
        amountToApply: Double,
        paymentDate: String
    ) {
        val unpaidInstallments = installmentDao.getUnpaidInstallmentsForChittyMember(chittyId, memberId)
        var remainingPayment = amountToApply

        for (inst in unpaidInstallments) {
            if (remainingPayment <= 0.0) break

            val remainingDueOnThis = inst.dueAmount - inst.paidAmount
            if (remainingPayment >= remainingDueOnThis) {
                // Fully pays this installment
                val updated = inst.copy(
                    paidAmount = inst.dueAmount,
                    outstandingAmount = 0.0,
                    status = "PAID",
                    paymentDate = paymentDate
                )
                installmentDao.updateInstallment(updated)
                remainingPayment -= remainingDueOnThis
            } else {
                // Partial payment
                val newPaid = inst.paidAmount + remainingPayment
                val newOutstanding = inst.dueAmount - newPaid
                val updated = inst.copy(
                    paidAmount = newPaid,
                    outstandingAmount = newOutstanding,
                    status = "PARTIALLY_PAID",
                    paymentDate = paymentDate
                )
                installmentDao.updateInstallment(updated)
                remainingPayment = 0.0
            }
        }

        // Advance payment handling:
        // If money still left after paying all due installments, store as Advance/Credit
        if (remainingPayment > 0.0) {
            val chittyMember = chittyDao.getChittyMember(chittyId, memberId)
            if (chittyMember != null) {
                val updatedAdvance = chittyMember.advanceAmount + remainingPayment
                chittyDao.updateChittyMember(chittyMember.copy(advanceAmount = updatedAdvance))
            }
        }
    }

    // Recipients
    val allRecipients: Flow<List<ChittyRecipientEntity>> = recipientDao.getAllRecipients()

    fun getRecipientsForChitty(chittyId: Long): Flow<List<ChittyRecipientEntity>> =
        recipientDao.getRecipientsForChitty(chittyId)

    suspend fun isMemberAlreadyRecipient(chittyId: Long, memberId: Long): Boolean {
        return recipientDao.getRecipientByChittyAndMember(chittyId, memberId) != null
    }

    suspend fun recordRecipient(
        chittyId: Long,
        monthNumber: Int,
        memberId: Long,
        amountReceived: Double,
        receivedDate: String,
        notes: String
    ): Long {
        val recipient = ChittyRecipientEntity(
            chittyId = chittyId,
            monthNumber = monthNumber,
            memberId = memberId,
            amountReceived = amountReceived,
            receivedDate = receivedDate,
            notes = notes
        )
        return recipientDao.insertRecipient(recipient)
    }

    // Dashboard Metrics
    fun getAdminDashboardMetrics(): Flow<AdminDashboardMetrics> = combine(
        chittyDao.getAllChitties(),
        memberDao.getAllMembers(),
        installmentDao.getAllInstallments()
    ) { chitties, members, installments ->
        val totalChitties = chitties.size
        val activeChitties = chitties.count { it.status == "ACTIVE" }
        val totalMembers = members.size

        // Month 1 is treated as the current active cycle for this month calculations
        val currentMonthInstallments = installments.filter { it.monthNumber == 1 }
        val thisMonthExpected = currentMonthInstallments.sumOf { it.dueAmount }
        val thisMonthCollected = currentMonthInstallments.sumOf { it.paidAmount }

        // Total Outstanding across all installments
        val totalOutstanding = installments.sumOf { it.outstandingAmount }

        AdminDashboardMetrics(
            totalChitties = totalChitties,
            activeChitties = activeChitties,
            totalMembers = totalMembers,
            thisMonthExpectedCollection = thisMonthExpected,
            thisMonthCollected = thisMonthCollected,
            totalOutstanding = totalOutstanding
        )
    }

    // Member Dashboard Summary
    fun getMemberDashboardSummary(memberId: Long): Flow<MemberDashboardSummary?> = flow {
        val member = memberDao.getMemberByIdDirect(memberId)
        if (member == null) {
            emit(null)
            return@flow
        }

        val memberships = chittyDao.getChittyMembershipsForMemberDirect(memberId)
        val recipients = recipientDao.getRecipientsForMemberDirect(memberId)
        val allMemberInstallments = installmentDao.getInstallmentsForMemberDirect(memberId)

        val chittySummaries = mutableListOf<MemberChittySummary>()
        var overallCurrentMonthDue = 0.0
        var overallPreviousOutstanding = 0.0
        var overallTotalOutstanding = 0.0
        var overallTotalPaid = 0.0
        var overallAdvanceCredit = 0.0

        for (membership in memberships) {
            val chitty = chittyDao.getChittyByIdDirect(membership.chittyId) ?: continue
            val chittyInstallments = allMemberInstallments.filter { it.chittyId == chitty.id }

            // Current Month installment (Month 1 in current cycle)
            val currentInst = chittyInstallments.firstOrNull { it.monthNumber == 1 }
            val currentMonthDue = currentInst?.dueAmount ?: chitty.monthlyInstallment

            // Historical previous installments (Month > 1 if any past due or previous unpaid)
            val previousUnpaid = chittyInstallments.filter { it.monthNumber > 1 && it.status != "PAID" }.sumOf { it.outstandingAmount }

            val totalOutstandingForChitty = chittyInstallments.sumOf { it.outstandingAmount }
            val totalPaidForChitty = chittyInstallments.sumOf { it.paidAmount }

            val recipientRecord = recipients.firstOrNull { it.chittyId == chitty.id }

            chittySummaries.add(
                MemberChittySummary(
                    chitty = chitty,
                    shareNumber = membership.shareNumber,
                    monthlyInstallment = chitty.monthlyInstallment,
                    currentMonthDue = currentMonthDue,
                    previousOutstanding = previousUnpaid,
                    totalDue = currentMonthDue + previousUnpaid,
                    totalOutstanding = totalOutstandingForChitty,
                    totalPaid = totalPaidForChitty,
                    advanceAmount = membership.advanceAmount,
                    hasReceived = recipientRecord != null,
                    receivedMonth = recipientRecord?.monthNumber,
                    receivedAmount = recipientRecord?.amountReceived,
                    installments = chittyInstallments
                )
            )

            overallCurrentMonthDue += currentMonthDue
            overallPreviousOutstanding += previousUnpaid
            overallTotalOutstanding += totalOutstandingForChitty
            overallTotalPaid += totalPaidForChitty
            overallAdvanceCredit += membership.advanceAmount
        }

        emit(
            MemberDashboardSummary(
                member = member,
                totalChitties = memberships.size,
                currentMonthDue = overallCurrentMonthDue,
                previousOutstanding = overallPreviousOutstanding,
                totalOutstanding = overallTotalOutstanding,
                totalPaid = overallTotalPaid,
                advanceCredit = overallAdvanceCredit,
                chittySummaries = chittySummaries
            )
        )
    }

    /**
     * Generate Combined WhatsApp message for a Member as strictly specified in Requirement 13
     */
    suspend fun generateWhatsAppMessage(memberId: Long): String {
        val member = memberDao.getMemberByIdDirect(memberId) ?: return ""
        val memberships = chittyDao.getChittyMembershipsForMemberDirect(memberId)
        val adminSettings = adminDao.getSettingsDirect()
        val adminName = adminSettings?.adminName?.ifBlank { "Management" } ?: "Management"

        val lines = mutableListOf<String>()
        var currentMonthDueSum = 0.0
        var previousOutstandingSum = 0.0

        for (membership in memberships) {
            val chitty = chittyDao.getChittyByIdDirect(membership.chittyId) ?: continue
            val installments = installmentDao.getInstallmentsForChittyMemberDirect(chitty.id, memberId)

            val currentInst = installments.firstOrNull { it.monthNumber == 1 }
            val currentDue = currentInst?.dueAmount ?: chitty.monthlyInstallment

            val prevOutstanding = installments.filter { it.monthNumber > 1 && it.status != "PAID" }.sumOf { it.outstandingAmount }

            lines.add("• ${chitty.name} — ₹${currentDue.toLong()}")
            currentMonthDueSum += currentDue
            previousOutstandingSum += prevOutstanding
        }

        val totalAmountDue = currentMonthDueSum + previousOutstandingSum

        val message = buildString {
            append("${adminSettings?.reminderHeader?.ifBlank { "🔔 Cheeti Payment Reminder" } ?: "🔔 Cheeti Payment Reminder"}\n\n")
            append("Dear ${member.name},\n\n")
            append("Your payments for this month:\n\n")
            lines.forEach { append("$it\n") }
            append("\n")
            append("Current Month Due: ₹${currentMonthDueSum.toLong()}\n")
            append("Previous Outstanding: ₹${previousOutstandingSum.toLong()}\n")
            append("Total Amount Due: ₹${totalAmountDue.toLong()}\n\n")
            append("${adminSettings?.customNotes?.ifBlank { "Please make the payment by the due date." } ?: "Please make the payment by the due date."}\n\n")
            append("Thank you\n")
            append(adminName)
        }

        return message
    }

    /**
     * Seed Sample / Test Data as strictly specified in Requirement 17:
     * Admin: Gopal Krishna
     * Chitty 1: ₹1,00,000, 20 months, ₹5,000/month
     * Chitty 2: ₹50,000, 12 months, ₹3,000/month
     * Member: Ravi Kumar joined to both chitties
     * Test state: Chitty 1 -> Paid ₹5,000; Chitty 2 -> Not Paid
     * Dashboard: Current Month Due = ₹8,000, Paid = ₹5,000, Outstanding = ₹3,000
     */
    suspend fun seedTestData() {
        // Save Admin
        saveAdminSetup(name = "Gopal Krishna", mobileNumber = "9876543210")

        // Create Member Ravi Kumar
        val memberId = addMember(
            name = "Ravi Kumar",
            mobileNumber = "9876543210",
            address = "No. 42, Temple Road, Bengaluru"
        )

        // Create Chitty 1
        val chitty1Id = createChitty(
            name = "₹1,00,000 Chitty",
            totalAmount = 100000.0,
            durationMonths = 20,
            monthlyInstallment = 5000.0,
            startDate = "2026-10-01",
            dueDayOfMonth = 10,
            totalMembers = 20
        )

        // Create Chitty 2
        val chitty2Id = createChitty(
            name = "₹50,000 Chitty",
            totalAmount = 50000.0,
            durationMonths = 12,
            monthlyInstallment = 3000.0,
            startDate = "2026-10-01",
            dueDayOfMonth = 10,
            totalMembers = 12
        )

        // Join Ravi to both chitties
        addMemberToChitty(chitty1Id, memberId, shareNumber = 1)
        addMemberToChitty(chitty2Id, memberId, shareNumber = 1)

        // Test payment: Chitty 1 -> Paid ₹5,000; Chitty 2 -> Not Paid
        recordPayment(
            memberId = memberId,
            totalAmount = 5000.0,
            paymentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
            paymentMethod = "UPI",
            notes = "Test Initial Payment for Chitty 1",
            allocations = listOf(Pair(chitty1Id, 5000.0))
        )
    }
}
