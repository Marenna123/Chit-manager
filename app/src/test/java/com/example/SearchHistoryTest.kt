package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.SearchCategory
import com.example.util.SearchHistoryManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SearchHistoryTest {

    private lateinit var context: Context
    private lateinit var searchHistoryManager: SearchHistoryManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        searchHistoryManager = SearchHistoryManager(context)
        searchHistoryManager.clearHistory(SearchCategory.MEMBERS)
        searchHistoryManager.clearHistory(SearchCategory.CHITTIES)
    }

    @Test
    fun testAddAndRetrieveMemberSearchHistory() {
        var history = searchHistoryManager.getHistory(SearchCategory.MEMBERS)
        assertTrue(history.isEmpty())

        history = searchHistoryManager.addQuery(SearchCategory.MEMBERS, "Ravi")
        assertEquals(listOf("Ravi"), history)

        history = searchHistoryManager.addQuery(SearchCategory.MEMBERS, "Gopal")
        assertEquals(listOf("Gopal", "Ravi"), history)

        // Adding duplicate moves it to the front
        history = searchHistoryManager.addQuery(SearchCategory.MEMBERS, "ravi")
        assertEquals(listOf("ravi", "Gopal"), history)
    }

    @Test
    fun testRemoveAndClearSearchHistory() {
        searchHistoryManager.addQuery(SearchCategory.CHITTIES, "Gold Scheme")
        searchHistoryManager.addQuery(SearchCategory.CHITTIES, "Silver 50K")
        searchHistoryManager.addQuery(SearchCategory.CHITTIES, "Diamond 1L")

        var history = searchHistoryManager.getHistory(SearchCategory.CHITTIES)
        assertEquals(3, history.size)
        assertEquals("Diamond 1L", history[0])

        history = searchHistoryManager.removeQuery(SearchCategory.CHITTIES, "Silver 50K")
        assertEquals(2, history.size)
        assertTrue(!history.contains("Silver 50K"))

        history = searchHistoryManager.clearHistory(SearchCategory.CHITTIES)
        assertTrue(history.isEmpty())
    }

    @Test
    fun testMaxHistoryItemsCapacity() {
        for (i in 1..15) {
            searchHistoryManager.addQuery(SearchCategory.MEMBERS, "Member $i")
        }
        val history = searchHistoryManager.getHistory(SearchCategory.MEMBERS)
        // Max capacity is 10
        assertEquals(10, history.size)
        assertEquals("Member 15", history[0])
    }

    @Test
    fun testBlankQueryIgnored() {
        searchHistoryManager.addQuery(SearchCategory.MEMBERS, "   ")
        val history = searchHistoryManager.getHistory(SearchCategory.MEMBERS)
        assertTrue(history.isEmpty())
    }
}
