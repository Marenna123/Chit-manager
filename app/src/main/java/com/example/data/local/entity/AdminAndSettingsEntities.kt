package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admins")
data class AdminEntity(
    @PrimaryKey val id: Long = 1L,
    val name: String,
    val mobileNumber: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Long = 1L,
    val adminName: String = "",
    val mobileNumber: String = "",
    val isFirstTimeSetupCompleted: Boolean = false,
    val reminderHeader: String = "🔔 Cheeti Payment Reminder",
    val customNotes: String = "Please make the payment by the due date.",
    val currencySymbol: String = "₹",
    val themeMode: String = "SYSTEM", // "LIGHT", "DARK", "SYSTEM"
    val updatedAt: Long = System.currentTimeMillis()
)
