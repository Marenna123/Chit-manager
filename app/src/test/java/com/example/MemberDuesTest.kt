package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.ChittiRepository
import kotlinx.coroutines.flow.first
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
class MemberDuesTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: ChittiRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ChittiRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testMemberInMultipleChittiesCurrentMonthAndOldBalance() = runBlocking {
        // Create Member
        val memberId = repository.addMember("Ravi Kumar", "9876543210", "Bangalore")

        // Create 2 Chitties
        // Chitty 1: ₹1,00,000, 20 months, ₹5,000/month
        val chitty1Id = repository.createChitty(
            name = "Chitty 100K",
            totalAmount = 100000.0,
            durationMonths = 20,
            monthlyInstallment = 5000.0,
            startDate = "2026-10-01",
            dueDayOfMonth = 10,
            totalMembers = 20
        )

        // Chitty 2: ₹50,000, 12 months, ₹3,000/month
        val chitty2Id = repository.createChitty(
            name = "Chitty 50K",
            totalAmount = 50000.0,
            durationMonths = 12,
            monthlyInstallment = 3000.0,
            startDate = "2026-10-01",
            dueDayOfMonth = 10,
            totalMembers = 12
        )

        // Join member to both chitties
        repository.addMemberToChitty(chitty1Id, memberId, shareNumber = 1)
        repository.addMemberToChitty(chitty2Id, memberId, shareNumber = 1)

        // Verify summary before any payment
        val summaryBefore = repository.getMemberDashboardSummary(memberId).first()
        assertNotNull(summaryBefore)
        assertEquals(2, summaryBefore!!.totalChitties)
        // Current Month Paying = ₹5,000 + ₹3,000 = ₹8,000
        assertEquals(8000.0, summaryBefore.currentMonthDue, 0.01)
        assertEquals(8000.0, summaryBefore.currentMonthPending, 0.01)
        assertEquals(0.0, summaryBefore.previousOutstanding, 0.01)
        assertEquals(8000.0, summaryBefore.totalDue, 0.01)

        // Now record payment for Chitty 1 (₹5,000 fully paid)
        repository.recordPayment(
            memberId = memberId,
            totalAmount = 5000.0,
            paymentDate = "2026-10-08",
            paymentMethod = "Cash",
            notes = "Chitty 1 payment",
            allocations = listOf(Pair(chitty1Id, 5000.0))
        )

        // Verify summary after payment
        val summaryAfter = repository.getMemberDashboardSummary(memberId).first()
        assertNotNull(summaryAfter)
        assertEquals(8000.0, summaryAfter!!.currentMonthDue, 0.01)
        assertEquals(5000.0, summaryAfter.currentMonthPaid, 0.01)
        // Still pending for current month = ₹3,000 (from Chitty 2)
        assertEquals(3000.0, summaryAfter.currentMonthPending, 0.01)
        assertEquals(3000.0, summaryAfter.totalDue, 0.01)

        // Verify dashboard metrics:
        // Expected collection this month = ₹8,000
        // Collected = ₹5,000
        // Current Month Outstanding = ₹3,000 (NOT ₹1,45,000!)
        val metrics = repository.getAdminDashboardMetrics().first()
        assertEquals(8000.0, metrics.thisMonthExpectedCollection, 0.01)
        assertEquals(5000.0, metrics.thisMonthCollected, 0.01)
        assertEquals(3000.0, metrics.currentMonthOutstanding, 0.01)
        assertEquals(3000.0, metrics.totalOutstanding, 0.01)

        // Verify WhatsApp reminder message shows both chitties and clear old and current balance
        val whatsappMsg = repository.generateWhatsAppMessage(memberId)
        assertTrue(whatsappMsg.contains("Chitty 100K"))
        assertTrue(whatsappMsg.contains("Chitty 50K"))
        assertTrue(whatsappMsg.contains("Current Month Paying: ₹3000"))
        assertTrue(whatsappMsg.contains("TOTAL BALANCE DUE: ₹3000"))
    }
}
