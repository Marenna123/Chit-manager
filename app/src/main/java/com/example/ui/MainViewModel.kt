package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AdminEntity
import com.example.data.local.entity.ChittyEntity
import com.example.data.local.entity.ChittyMemberEntity
import com.example.data.local.entity.ChittyRecipientEntity
import com.example.data.local.entity.MemberEntity
import com.example.data.local.entity.MonthlyInstallmentEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ReceiptEntity
import com.example.data.local.entity.SettingsEntity
import com.example.data.model.AdminDashboardMetrics
import com.example.data.model.MemberDashboardSummary
import com.example.data.repository.ChittiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val repository = ChittiRepository(database)

    val settings: StateFlow<SettingsEntity?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val admin: StateFlow<AdminEntity?> = repository.admin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val metrics: StateFlow<AdminDashboardMetrics> = repository.getAdminDashboardMetrics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminDashboardMetrics())

    val chitties: StateFlow<List<ChittyEntity>> = repository.allChitties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val members: StateFlow<List<MemberEntity>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val receipts: StateFlow<List<ReceiptEntity>> = repository.allReceipts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recipients: StateFlow<List<ChittyRecipientEntity>> = repository.allRecipients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active member summary for Member Dashboard
    private val _selectedMemberSummary = MutableStateFlow<MemberDashboardSummary?>(null)
    val selectedMemberSummary: StateFlow<MemberDashboardSummary?> = _selectedMemberSummary.asStateFlow()

    // WhatsApp Preview state
    private val _whatsAppPreview = MutableStateFlow<Pair<MemberEntity, String>?>(null)
    val whatsAppPreview: StateFlow<Pair<MemberEntity, String>?> = _whatsAppPreview.asStateFlow()

    // Receipt Dialog state
    private val _activeReceipt = MutableStateFlow<ReceiptEntity?>(null)
    val activeReceipt: StateFlow<ReceiptEntity?> = _activeReceipt.asStateFlow()

    // Search History Management
    private val searchHistoryManager = com.example.util.SearchHistoryManager(application)

    private val _memberSearchHistory = MutableStateFlow<List<String>>(
        searchHistoryManager.getHistory(com.example.util.SearchCategory.MEMBERS)
    )
    val memberSearchHistory: StateFlow<List<String>> = _memberSearchHistory.asStateFlow()

    private val _chittySearchHistory = MutableStateFlow<List<String>>(
        searchHistoryManager.getHistory(com.example.util.SearchCategory.CHITTIES)
    )
    val chittySearchHistory: StateFlow<List<String>> = _chittySearchHistory.asStateFlow()

    fun addMemberSearch(query: String) {
        if (query.isNotBlank()) {
            _memberSearchHistory.value = searchHistoryManager.addQuery(com.example.util.SearchCategory.MEMBERS, query)
        }
    }

    fun removeMemberSearch(query: String) {
        _memberSearchHistory.value = searchHistoryManager.removeQuery(com.example.util.SearchCategory.MEMBERS, query)
    }

    fun clearMemberSearchHistory() {
        _memberSearchHistory.value = searchHistoryManager.clearHistory(com.example.util.SearchCategory.MEMBERS)
    }

    fun addChittySearch(query: String) {
        if (query.isNotBlank()) {
            _chittySearchHistory.value = searchHistoryManager.addQuery(com.example.util.SearchCategory.CHITTIES, query)
        }
    }

    fun removeChittySearch(query: String) {
        _chittySearchHistory.value = searchHistoryManager.removeQuery(com.example.util.SearchCategory.CHITTIES, query)
    }

    fun clearChittySearchHistory() {
        _chittySearchHistory.value = searchHistoryManager.clearHistory(com.example.util.SearchCategory.CHITTIES)
    }

    // Status message for feedback
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun completeFirstTimeSetup(name: String, mobile: String) {
        viewModelScope.launch {
            repository.saveAdminSetup(name, mobile)
            _snackbarMessage.value = "Welcome, $name! Setup completed."
        }
    }

    fun updateSettings(adminName: String, mobileNumber: String, header: String, notes: String) {
        viewModelScope.launch {
            repository.updateSettings(adminName, mobileNumber, header, notes)
            _snackbarMessage.value = "Settings updated successfully."
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.updateThemeMode(mode)
            val modeName = when (mode) {
                "LIGHT" -> "Light mode"
                "DARK" -> "Dark mode"
                else -> "System default theme"
            }
            _snackbarMessage.value = "$modeName enabled"
        }
    }

    // Local Database Backup & Restore
    suspend fun exportDatabaseBackup(): String {
        return repository.exportDatabaseToJson()
    }

    fun inspectBackup(jsonString: String): Result<com.example.data.backup.BackupSummary> {
        return try {
            Result.success(repository.inspectBackup(jsonString))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun restoreDatabase(jsonString: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val summary = repository.restoreDatabaseFromJson(jsonString)
                _snackbarMessage.value = "Backup restored: ${summary.membersCount} members, ${summary.chittiesCount} chitties"
                onComplete(true, "Successfully restored ${summary.membersCount} members, ${summary.chittiesCount} chitties, ${summary.paymentsCount} payments.")
            } catch (e: Exception) {
                _snackbarMessage.value = "Restore failed: ${e.message}"
                onComplete(false, e.message ?: "Failed to restore backup")
            }
        }
    }

    fun addMember(name: String, mobile: String, address: String, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.addMember(name, mobile, address)
            _snackbarMessage.value = "Member $name added successfully."
            onComplete(id)
        }
    }

    fun updateMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.updateMember(member)
            _snackbarMessage.value = "Member updated."
        }
    }

    fun toggleMemberStatus(memberId: Long) {
        viewModelScope.launch {
            repository.toggleMemberStatus(memberId)
        }
    }

    fun createChitty(
        name: String,
        totalAmount: Double,
        durationMonths: Int,
        monthlyInstallment: Double,
        startDate: String,
        dueDayOfMonth: Int,
        totalMembers: Int,
        onComplete: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val id = repository.createChitty(
                name = name,
                totalAmount = totalAmount,
                durationMonths = durationMonths,
                monthlyInstallment = monthlyInstallment,
                startDate = startDate,
                dueDayOfMonth = dueDayOfMonth,
                totalMembers = totalMembers
            )
            _snackbarMessage.value = "Chitty $name created."
            onComplete(id)
        }
    }

    fun addMemberToChitty(chittyId: Long, memberId: Long, shareNumber: Int = 1) {
        viewModelScope.launch {
            repository.addMemberToChitty(chittyId, memberId, shareNumber)
            _snackbarMessage.value = "Member enrolled in Chitty."
        }
    }

    fun removeMemberFromChitty(chittyId: Long, memberId: Long) {
        viewModelScope.launch {
            repository.removeMemberFromChitty(chittyId, memberId)
            _snackbarMessage.value = "Member removed from Chitty."
        }
    }

    fun recordPayment(
        memberId: Long,
        totalAmount: Double,
        paymentDate: String,
        paymentMethod: String,
        notes: String,
        allocations: List<Pair<Long, Double>>,
        onReceiptCreated: (ReceiptEntity) -> Unit = {}
    ) {
        viewModelScope.launch {
            val paymentId = repository.recordPayment(
                memberId = memberId,
                totalAmount = totalAmount,
                paymentDate = paymentDate,
                paymentMethod = paymentMethod,
                notes = notes,
                allocations = allocations
            )
            val receipt = repository.getReceiptByPaymentId(paymentId)
            receipt.collect { r ->
                if (r != null) {
                    _activeReceipt.value = r
                    onReceiptCreated(r)
                }
            }
        }
    }

    fun recordRecipient(
        chittyId: Long,
        monthNumber: Int,
        memberId: Long,
        amountReceived: Double,
        receivedDate: String,
        notes: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val alreadyWon = repository.isMemberAlreadyRecipient(chittyId, memberId)
            if (alreadyWon) {
                onError("This member has already received the chitty payout for this Chitty!")
                return@launch
            }
            repository.recordRecipient(
                chittyId = chittyId,
                monthNumber = monthNumber,
                memberId = memberId,
                amountReceived = amountReceived,
                receivedDate = receivedDate,
                notes = notes
            )
            _snackbarMessage.value = "Chitty recipient recorded successfully."
            onSuccess()
        }
    }

    fun loadMemberDashboard(memberId: Long) {
        viewModelScope.launch {
            repository.getMemberDashboardSummary(memberId).collect { summary ->
                _selectedMemberSummary.value = summary
            }
        }
    }

    fun prepareWhatsAppReminder(member: MemberEntity) {
        viewModelScope.launch {
            val msg = repository.generateWhatsAppMessage(member.id)
            _whatsAppPreview.value = Pair(member, msg)
        }
    }

    fun closeWhatsAppPreview() {
        _whatsAppPreview.value = null
    }

    fun showReceipt(receipt: ReceiptEntity) {
        _activeReceipt.value = receipt
    }

    fun closeReceipt() {
        _activeReceipt.value = null
    }

    fun seedSampleTestData() {
        viewModelScope.launch {
            repository.seedTestData()
            _snackbarMessage.value = "Test data seeded (Admin: Gopal Krishna, Member: Ravi Kumar, Chitties: ₹1L & ₹50K)."
        }
    }
}
