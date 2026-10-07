package com.example.util

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

enum class SearchCategory(val prefKey: String) {
    MEMBERS("pref_recent_searches_members"),
    CHITTIES("pref_recent_searches_chitties")
}

class SearchHistoryManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "cheeti_search_history_prefs"
        private const val MAX_HISTORY_ITEMS = 10
    }

    @Synchronized
    fun getHistory(category: SearchCategory): List<String> {
        val jsonString = prefs.getString(category.prefKey, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.optString(i)?.trim()
                if (!item.isNullOrBlank() && !list.contains(item)) {
                    list.add(item)
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun addQuery(category: SearchCategory, rawQuery: String): List<String> {
        val clean = rawQuery.trim()
        if (clean.isBlank()) return getHistory(category)

        val current = getHistory(category).toMutableList()
        // Remove duplicate regardless of case
        current.removeAll { it.equals(clean, ignoreCase = true) }
        // Insert at beginning
        current.add(0, clean)

        // Cap to MAX_HISTORY_ITEMS
        val trimmed = if (current.size > MAX_HISTORY_ITEMS) {
            current.subList(0, MAX_HISTORY_ITEMS)
        } else {
            current
        }

        saveHistory(category, trimmed)
        return trimmed
    }

    @Synchronized
    fun removeQuery(category: SearchCategory, queryToRemove: String): List<String> {
        val current = getHistory(category).toMutableList()
        current.removeAll { it.equals(queryToRemove.trim(), ignoreCase = true) }
        saveHistory(category, current)
        return current
    }

    @Synchronized
    fun clearHistory(category: SearchCategory): List<String> {
        prefs.edit().remove(category.prefKey).apply()
        return emptyList()
    }

    private fun saveHistory(category: SearchCategory, list: List<String>) {
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it) }
        prefs.edit().putString(category.prefKey, jsonArray.toString()).apply()
    }
}
