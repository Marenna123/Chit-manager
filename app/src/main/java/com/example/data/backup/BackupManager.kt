package com.example.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupSummary(
    val exportDate: String,
    val membersCount: Int,
    val chittiesCount: Int,
    val paymentsCount: Int,
    val installmentsCount: Int,
    val recipientsCount: Int,
    val receiptsCount: Int
)

object BackupManager {

    private const val APP_IDENTIFIER = "Cheeti"
    private const val BACKUP_VERSION = 1

    suspend fun exportDatabaseToJson(db: AppDatabase): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", APP_IDENTIFIER)
        root.put("version", BACKUP_VERSION)
        val timestamp = System.currentTimeMillis()
        root.put("exportTimestamp", timestamp)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        root.put("exportDate", dateFormat.format(Date(timestamp)))

        // Fetch records
        val admin = db.adminAndSettingsDao().getAdminDirect()
        val settings = db.adminAndSettingsDao().getSettingsDirect()
        val members = db.memberDao().getAllMembersDirect()
        val chitties = db.chittyDao().getAllChittiesDirect()
        val chittyMembers = db.chittyDao().getAllChittyMembersDirect()
        val installments = db.installmentDao().getAllInstallmentsDirect()
        val payments = db.paymentDao().getAllPaymentsDirect()
        val allocations = db.paymentDao().getAllAllocationsDirect()
        val recipients = db.recipientDao().getAllRecipientsDirect()
        val receipts = db.paymentDao().getAllReceiptsDirect()

        // Summary
        val summary = JSONObject().apply {
            put("membersCount", members.size)
            put("chittiesCount", chitties.size)
            put("paymentsCount", payments.size)
            put("installmentsCount", installments.size)
            put("recipientsCount", recipients.size)
            put("receiptsCount", receipts.size)
        }
        root.put("summary", summary)

        val data = JSONObject()

        // Admin
        if (admin != null) {
            val adminObj = JSONObject().apply {
                put("id", admin.id)
                put("name", admin.name)
                put("mobileNumber", admin.mobileNumber)
                put("createdAt", admin.createdAt)
            }
            data.put("admin", adminObj)
        }

        // Settings
        if (settings != null) {
            val settingsObj = JSONObject().apply {
                put("id", settings.id)
                put("adminName", settings.adminName)
                put("mobileNumber", settings.mobileNumber)
                put("isFirstTimeSetupCompleted", settings.isFirstTimeSetupCompleted)
                put("reminderHeader", settings.reminderHeader)
                put("customNotes", settings.customNotes)
                put("currencySymbol", settings.currencySymbol)
                put("themeMode", settings.themeMode)
                put("updatedAt", settings.updatedAt)
            }
            data.put("settings", settingsObj)
        }

        // Members
        val membersArray = JSONArray()
        members.forEach { m ->
            membersArray.put(JSONObject().apply {
                put("id", m.id)
                put("memberCode", m.memberCode)
                put("name", m.name)
                put("mobileNumber", m.mobileNumber)
                put("address", m.address)
                put("status", m.status)
                put("createdAt", m.createdAt)
            })
        }
        data.put("members", membersArray)

        // Chitties
        val chittiesArray = JSONArray()
        chitties.forEach { c ->
            chittiesArray.put(JSONObject().apply {
                put("id", c.id)
                put("chittyCode", c.chittyCode)
                put("name", c.name)
                put("totalAmount", c.totalAmount)
                put("durationMonths", c.durationMonths)
                put("monthlyInstallment", c.monthlyInstallment)
                put("startDate", c.startDate)
                put("dueDayOfMonth", c.dueDayOfMonth)
                put("totalMembers", c.totalMembers)
                put("status", c.status)
                put("adminId", c.adminId)
                put("createdAt", c.createdAt)
            })
        }
        data.put("chitties", chittiesArray)

        // Chitty Members
        val cmArray = JSONArray()
        chittyMembers.forEach { cm ->
            cmArray.put(JSONObject().apply {
                put("id", cm.id)
                put("chittyId", cm.chittyId)
                put("memberId", cm.memberId)
                put("shareNumber", cm.shareNumber)
                put("joinedDate", cm.joinedDate)
                put("advanceAmount", cm.advanceAmount)
                put("status", cm.status)
                put("createdAt", cm.createdAt)
            })
        }
        data.put("chittyMembers", cmArray)

        // Monthly Installments
        val instArray = JSONArray()
        installments.forEach { inst ->
            instArray.put(JSONObject().apply {
                put("id", inst.id)
                put("chittyId", inst.chittyId)
                put("memberId", inst.memberId)
                put("monthNumber", inst.monthNumber)
                put("monthLabel", inst.monthLabel)
                put("dueDate", inst.dueDate)
                put("dueAmount", inst.dueAmount)
                put("paidAmount", inst.paidAmount)
                put("outstandingAmount", inst.outstandingAmount)
                put("status", inst.status)
                if (inst.paymentDate != null) put("paymentDate", inst.paymentDate)
                put("notes", inst.notes)
            })
        }
        data.put("installments", instArray)

        // Payments
        val paymentsArray = JSONArray()
        payments.forEach { p ->
            paymentsArray.put(JSONObject().apply {
                put("id", p.id)
                put("receiptNumber", p.receiptNumber)
                put("memberId", p.memberId)
                put("totalAmount", p.totalAmount)
                put("paymentDate", p.paymentDate)
                put("paymentMethod", p.paymentMethod)
                put("notes", p.notes)
                put("isCombined", p.isCombined)
                put("isDeleted", p.isDeleted)
                put("createdAt", p.createdAt)
            })
        }
        data.put("payments", paymentsArray)

        // Allocations
        val allocationsArray = JSONArray()
        allocations.forEach { a ->
            allocationsArray.put(JSONObject().apply {
                put("id", a.id)
                put("paymentId", a.paymentId)
                put("chittyId", a.chittyId)
                put("memberId", a.memberId)
                if (a.installmentId != null) put("installmentId", a.installmentId)
                if (a.monthNumber != null) put("monthNumber", a.monthNumber)
                put("allocatedAmount", a.allocatedAmount)
                put("notes", a.notes)
            })
        }
        data.put("allocations", allocationsArray)

        // Recipients
        val recipientsArray = JSONArray()
        recipients.forEach { r ->
            recipientsArray.put(JSONObject().apply {
                put("id", r.id)
                put("chittyId", r.chittyId)
                put("monthNumber", r.monthNumber)
                put("memberId", r.memberId)
                put("amountReceived", r.amountReceived)
                put("receivedDate", r.receivedDate)
                put("notes", r.notes)
                put("createdAt", r.createdAt)
            })
        }
        data.put("recipients", recipientsArray)

        // Receipts
        val receiptsArray = JSONArray()
        receipts.forEach { rc ->
            receiptsArray.put(JSONObject().apply {
                put("id", rc.id)
                put("paymentId", rc.paymentId)
                put("receiptNumber", rc.receiptNumber)
                put("memberId", rc.memberId)
                put("memberName", rc.memberName)
                put("memberMobile", rc.memberMobile)
                put("amount", rc.amount)
                put("date", rc.date)
                put("paymentMethod", rc.paymentMethod)
                put("allocationSummary", rc.allocationSummary)
                put("adminName", rc.adminName)
                put("createdAt", rc.createdAt)
            })
        }
        data.put("receipts", receiptsArray)

        root.put("data", data)
        root.toString(2)
    }

    fun inspectBackup(jsonString: String): BackupSummary {
        val root = JSONObject(jsonString)
        val app = root.optString("app", "")
        if (app != APP_IDENTIFIER) {
            throw IllegalArgumentException("Invalid backup file: Not a recognized $APP_IDENTIFIER backup.")
        }

        val exportDate = root.optString("exportDate", "Unknown date")
        val data = root.optJSONObject("data") ?: throw IllegalArgumentException("Backup data is empty or corrupted.")

        val membersArray = data.optJSONArray("members") ?: JSONArray()
        val chittiesArray = data.optJSONArray("chitties") ?: JSONArray()
        val paymentsArray = data.optJSONArray("payments") ?: JSONArray()
        val installmentsArray = data.optJSONArray("installments") ?: JSONArray()
        val recipientsArray = data.optJSONArray("recipients") ?: JSONArray()
        val receiptsArray = data.optJSONArray("receipts") ?: JSONArray()

        return BackupSummary(
            exportDate = exportDate,
            membersCount = membersArray.length(),
            chittiesCount = chittiesArray.length(),
            paymentsCount = paymentsArray.length(),
            installmentsCount = installmentsArray.length(),
            recipientsCount = recipientsArray.length(),
            receiptsCount = receiptsArray.length()
        )
    }

    suspend fun restoreDatabaseFromJson(db: AppDatabase, jsonString: String): BackupSummary = withContext(Dispatchers.IO) {
        val root = JSONObject(jsonString)
        val app = root.optString("app", "")
        if (app != APP_IDENTIFIER) {
            throw IllegalArgumentException("Invalid backup file: Not a recognized $APP_IDENTIFIER backup.")
        }

        val data = root.optJSONObject("data") ?: throw IllegalArgumentException("Missing data object in backup.")
        val exportDate = root.optString("exportDate", "Unknown date")

        // Parse Admin
        val adminObj = data.optJSONObject("admin")
        val admin = adminObj?.let {
            AdminEntity(
                id = it.optLong("id", 1L),
                name = it.optString("name", "Management"),
                mobileNumber = it.optString("mobileNumber", ""),
                createdAt = it.optLong("createdAt", System.currentTimeMillis())
            )
        }

        // Parse Settings
        val settingsObj = data.optJSONObject("settings")
        val settings = settingsObj?.let {
            SettingsEntity(
                id = it.optLong("id", 1L),
                adminName = it.optString("adminName", ""),
                mobileNumber = it.optString("mobileNumber", ""),
                isFirstTimeSetupCompleted = it.optBoolean("isFirstTimeSetupCompleted", true),
                reminderHeader = it.optString("reminderHeader", "🔔 Cheeti Payment Reminder"),
                customNotes = it.optString("customNotes", "Please make the payment by the due date."),
                currencySymbol = it.optString("currencySymbol", "₹"),
                themeMode = it.optString("themeMode", "SYSTEM"),
                updatedAt = it.optLong("updatedAt", System.currentTimeMillis())
            )
        }

        // Parse Members
        val membersList = mutableListOf<MemberEntity>()
        val membersArray = data.optJSONArray("members") ?: JSONArray()
        for (i in 0 until membersArray.length()) {
            val obj = membersArray.getJSONObject(i)
            membersList.add(
                MemberEntity(
                    id = obj.optLong("id", 0L),
                    memberCode = obj.optString("memberCode", "M%03d".format(i + 1)),
                    name = obj.optString("name", ""),
                    mobileNumber = obj.optString("mobileNumber", ""),
                    address = obj.optString("address", ""),
                    status = obj.optString("status", "ACTIVE"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // Parse Chitties
        val chittiesList = mutableListOf<ChittyEntity>()
        val chittiesArray = data.optJSONArray("chitties") ?: JSONArray()
        for (i in 0 until chittiesArray.length()) {
            val obj = chittiesArray.getJSONObject(i)
            chittiesList.add(
                ChittyEntity(
                    id = obj.optLong("id", 0L),
                    chittyCode = obj.optString("chittyCode", "C%03d".format(i + 1)),
                    name = obj.optString("name", ""),
                    totalAmount = obj.optDouble("totalAmount", 0.0),
                    durationMonths = obj.optInt("durationMonths", 12),
                    monthlyInstallment = obj.optDouble("monthlyInstallment", 0.0),
                    startDate = obj.optString("startDate", ""),
                    dueDayOfMonth = obj.optInt("dueDayOfMonth", 10),
                    totalMembers = obj.optInt("totalMembers", 20),
                    status = obj.optString("status", "ACTIVE"),
                    adminId = obj.optLong("adminId", 1L),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // Parse Chitty Members
        val chittyMembersList = mutableListOf<ChittyMemberEntity>()
        val cmArray = data.optJSONArray("chittyMembers") ?: JSONArray()
        for (i in 0 until cmArray.length()) {
            val obj = cmArray.getJSONObject(i)
            chittyMembersList.add(
                ChittyMemberEntity(
                    id = obj.optLong("id", 0L),
                    chittyId = obj.optLong("chittyId", 0L),
                    memberId = obj.optLong("memberId", 0L),
                    shareNumber = obj.optInt("shareNumber", 1),
                    joinedDate = obj.optString("joinedDate", ""),
                    advanceAmount = obj.optDouble("advanceAmount", 0.0),
                    status = obj.optString("status", "ACTIVE"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // Parse Installments
        val installmentsList = mutableListOf<MonthlyInstallmentEntity>()
        val instArray = data.optJSONArray("installments") ?: JSONArray()
        for (i in 0 until instArray.length()) {
            val obj = instArray.getJSONObject(i)
            installmentsList.add(
                MonthlyInstallmentEntity(
                    id = obj.optLong("id", 0L),
                    chittyId = obj.optLong("chittyId", 0L),
                    memberId = obj.optLong("memberId", 0L),
                    monthNumber = obj.optInt("monthNumber", 1),
                    monthLabel = obj.optString("monthLabel", "Month ${i + 1}"),
                    dueDate = obj.optString("dueDate", ""),
                    dueAmount = obj.optDouble("dueAmount", 0.0),
                    paidAmount = obj.optDouble("paidAmount", 0.0),
                    outstandingAmount = obj.optDouble("outstandingAmount", 0.0),
                    status = obj.optString("status", "UNPAID"),
                    paymentDate = if (obj.has("paymentDate") && !obj.isNull("paymentDate")) obj.optString("paymentDate") else null,
                    notes = obj.optString("notes", "")
                )
            )
        }

        // Parse Payments
        val paymentsList = mutableListOf<PaymentEntity>()
        val paymentsArray = data.optJSONArray("payments") ?: JSONArray()
        for (i in 0 until paymentsArray.length()) {
            val obj = paymentsArray.getJSONObject(i)
            paymentsList.add(
                PaymentEntity(
                    id = obj.optLong("id", 0L),
                    receiptNumber = obj.optString("receiptNumber", "REC-$i"),
                    memberId = obj.optLong("memberId", 0L),
                    totalAmount = obj.optDouble("totalAmount", 0.0),
                    paymentDate = obj.optString("paymentDate", ""),
                    paymentMethod = obj.optString("paymentMethod", "Cash"),
                    notes = obj.optString("notes", ""),
                    isCombined = obj.optBoolean("isCombined", false),
                    isDeleted = obj.optBoolean("isDeleted", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // Parse Allocations
        val allocationsList = mutableListOf<PaymentAllocationEntity>()
        val allocationsArray = data.optJSONArray("allocations") ?: JSONArray()
        for (i in 0 until allocationsArray.length()) {
            val obj = allocationsArray.getJSONObject(i)
            val instId = if (obj.has("installmentId") && !obj.isNull("installmentId")) obj.optLong("installmentId") else null
            val mNum = if (obj.has("monthNumber") && !obj.isNull("monthNumber")) obj.optInt("monthNumber") else null
            allocationsList.add(
                PaymentAllocationEntity(
                    id = obj.optLong("id", 0L),
                    paymentId = obj.optLong("paymentId", 0L),
                    chittyId = obj.optLong("chittyId", 0L),
                    memberId = obj.optLong("memberId", 0L),
                    installmentId = instId,
                    monthNumber = mNum,
                    allocatedAmount = obj.optDouble("allocatedAmount", 0.0),
                    notes = obj.optString("notes", "")
                )
            )
        }

        // Parse Recipients
        val recipientsList = mutableListOf<ChittyRecipientEntity>()
        val recipientsArray = data.optJSONArray("recipients") ?: JSONArray()
        for (i in 0 until recipientsArray.length()) {
            val obj = recipientsArray.getJSONObject(i)
            recipientsList.add(
                ChittyRecipientEntity(
                    id = obj.optLong("id", 0L),
                    chittyId = obj.optLong("chittyId", 0L),
                    monthNumber = obj.optInt("monthNumber", 1),
                    memberId = obj.optLong("memberId", 0L),
                    amountReceived = obj.optDouble("amountReceived", 0.0),
                    receivedDate = obj.optString("receivedDate", ""),
                    notes = obj.optString("notes", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // Parse Receipts
        val receiptsList = mutableListOf<ReceiptEntity>()
        val receiptsArray = data.optJSONArray("receipts") ?: JSONArray()
        for (i in 0 until receiptsArray.length()) {
            val obj = receiptsArray.getJSONObject(i)
            receiptsList.add(
                ReceiptEntity(
                    id = obj.optLong("id", 0L),
                    paymentId = obj.optLong("paymentId", 0L),
                    receiptNumber = obj.optString("receiptNumber", "REC-$i"),
                    memberId = obj.optLong("memberId", 0L),
                    memberName = obj.optString("memberName", ""),
                    memberMobile = obj.optString("memberMobile", ""),
                    amount = obj.optDouble("amount", 0.0),
                    date = obj.optString("date", ""),
                    paymentMethod = obj.optString("paymentMethod", "Cash"),
                    allocationSummary = obj.optString("allocationSummary", ""),
                    adminName = obj.optString("adminName", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // Atomic Transaction: Clear and Replace
        db.recipientDao().deleteAllRecipients()
        db.paymentDao().deleteAllReceipts()
        db.paymentDao().deleteAllAllocations()
        db.paymentDao().deleteAllPayments()
        db.installmentDao().deleteAllInstallments()
        db.chittyDao().deleteAllChittyMembers()
        db.chittyDao().deleteAllChitties()
        db.memberDao().deleteAllMembers()
        db.adminAndSettingsDao().deleteAllAdmins()
        db.adminAndSettingsDao().deleteAllSettings()

        // Insert new records
        if (admin != null) db.adminAndSettingsDao().saveAdmin(admin)
        if (settings != null) db.adminAndSettingsDao().saveSettings(settings)
        if (membersList.isNotEmpty()) db.memberDao().insertAllMembers(membersList)
        if (chittiesList.isNotEmpty()) db.chittyDao().insertAllChitties(chittiesList)
        if (chittyMembersList.isNotEmpty()) db.chittyDao().insertAllChittyMembers(chittyMembersList)
        if (installmentsList.isNotEmpty()) db.installmentDao().insertInstallments(installmentsList)
        if (paymentsList.isNotEmpty()) db.paymentDao().insertAllPayments(paymentsList)
        if (allocationsList.isNotEmpty()) db.paymentDao().insertAllocations(allocationsList)
        if (recipientsList.isNotEmpty()) db.recipientDao().insertAllRecipients(recipientsList)
        if (receiptsList.isNotEmpty()) db.paymentDao().insertAllReceipts(receiptsList)

        BackupSummary(
            exportDate = exportDate,
            membersCount = membersList.size,
            chittiesCount = chittiesList.size,
            paymentsCount = paymentsList.size,
            installmentsCount = installmentsList.size,
            recipientsCount = recipientsList.size,
            receiptsCount = receiptsList.size
        )
    }

    fun readJsonFromUri(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        } ?: throw IllegalArgumentException("Could not read from selected backup file.")
    }

    fun writeJsonToUri(context: Context, uri: Uri, jsonString: String) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
            outputStream.flush()
        } ?: throw IllegalArgumentException("Could not write backup to selected destination.")
    }

    fun shareBackup(context: Context, jsonString: String, filename: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$APP_IDENTIFIER Database Backup ($filename)")
            putExtra(Intent.EXTRA_TEXT, jsonString)
        }
        val chooser = Intent.createChooser(shareIntent, "Share or Save Cheeti Backup")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
