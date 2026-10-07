package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AdminAndSettingsDao
import com.example.data.local.dao.ChittyDao
import com.example.data.local.dao.InstallmentDao
import com.example.data.local.dao.MemberDao
import com.example.data.local.dao.PaymentDao
import com.example.data.local.dao.RecipientDao
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

@Database(
    entities = [
        AdminEntity::class,
        SettingsEntity::class,
        MemberEntity::class,
        ChittyEntity::class,
        ChittyMemberEntity::class,
        MonthlyInstallmentEntity::class,
        PaymentEntity::class,
        PaymentAllocationEntity::class,
        ChittyRecipientEntity::class,
        ReceiptEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun adminAndSettingsDao(): AdminAndSettingsDao
    abstract fun memberDao(): MemberDao
    abstract fun chittyDao(): ChittyDao
    abstract fun installmentDao(): InstallmentDao
    abstract fun paymentDao(): PaymentDao
    abstract fun recipientDao(): RecipientDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chitti_management.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
