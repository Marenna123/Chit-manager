package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupManager
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AdminEntity
import com.example.data.local.entity.ChittyEntity
import com.example.data.local.entity.MemberEntity
import com.example.data.local.entity.SettingsEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BackupRestoreTest {

    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testExportAndRestoreBackup() = runBlocking {
        // Seed initial data
        db.adminAndSettingsDao().saveAdmin(
            AdminEntity(id = 1L, name = "Gopal Krishna", mobileNumber = "9876543210")
        )
        db.adminAndSettingsDao().saveSettings(
            SettingsEntity(id = 1L, adminName = "Gopal Krishna", mobileNumber = "9876543210")
        )
        val memberId = db.memberDao().insertMember(
            MemberEntity(id = 1L, memberCode = "M001", name = "Ravi Kumar", mobileNumber = "9123456789")
        )
        val chittyId = db.chittyDao().insertChitty(
            ChittyEntity(
                id = 1L,
                chittyCode = "C001",
                name = "₹1,00,000 Chitty",
                totalAmount = 100000.0,
                durationMonths = 20,
                monthlyInstallment = 5000.0,
                startDate = "2026-01-01"
            )
        )

        // 1. Export database to JSON
        val exportedJson = BackupManager.exportDatabaseToJson(db)
        assertNotNull(exportedJson)
        assertTrue(exportedJson.contains("\"app\": \"Cheeti\""))
        assertTrue(exportedJson.contains("Ravi Kumar"))
        assertTrue(exportedJson.contains("Gopal Krishna"))

        // 2. Inspect backup
        val inspection = BackupManager.inspectBackup(exportedJson)
        assertEquals(1, inspection.membersCount)
        assertEquals(1, inspection.chittiesCount)

        // 3. Clear database to simulate data loss
        db.memberDao().deleteAllMembers()
        db.chittyDao().deleteAllChitties()
        assertEquals(0, db.memberDao().getAllMembersDirect().size)
        assertEquals(0, db.chittyDao().getAllChittiesDirect().size)

        // 4. Restore database from JSON
        val restoreResult = BackupManager.restoreDatabaseFromJson(db, exportedJson)
        assertEquals(1, restoreResult.membersCount)
        assertEquals(1, restoreResult.chittiesCount)

        // Verify data was restored accurately
        val restoredMembers = db.memberDao().getAllMembersDirect()
        val restoredChitties = db.chittyDao().getAllChittiesDirect()
        assertEquals(1, restoredMembers.size)
        assertEquals("Ravi Kumar", restoredMembers[0].name)
        assertEquals(1, restoredChitties.size)
        assertEquals("₹1,00,000 Chitty", restoredChitties[0].name)

        val restoredAdmin = db.adminAndSettingsDao().getAdminDirect()
        assertEquals("Gopal Krishna", restoredAdmin?.name)
    }
}
