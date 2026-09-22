package com.cybersec.liquidstream.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.cybersec.liquidstream.data.model.Movie
import com.cybersec.liquidstream.ui.screens.home.ContinueWatchingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class WatchHistoryEntry(
    val movieId: String,
    val title: String,
    val posterUrl: String,
    val detailUrl: String,
    val positionMs: Long,
    val durationMs: Long,
    val timestamp: Long
) {
    val progress: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val remainingTimeText: String
        get() {
            val remainingMs = (durationMs - positionMs).coerceAtLeast(0L)
            val totalMinutes = (remainingMs / 1000) / 60
            val hours = totalMinutes / 60
            val mins = totalMinutes % 60
            return when {
                hours > 0 -> "${hours}h ${mins}m left"
                mins > 0 -> "${mins}m left"
                else -> "1m left"
            }
        }

    fun toMovie(): Movie {
        return Movie(
            id = movieId,
            title = title,
            posterUrl = posterUrl,
            detailUrl = detailUrl
        )
    }

    fun toContinueWatchingItem(): ContinueWatchingItem {
        return ContinueWatchingItem(
            movie = toMovie(),
            progress = progress,
            remainingTimeText = remainingTimeText,
            positionMs = positionMs,
            durationMs = durationMs
        )
    }
}

class WatchHistoryRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _historyFlow = MutableStateFlow<List<WatchHistoryEntry>>(loadHistory())
    val historyFlow: StateFlow<List<WatchHistoryEntry>> = _historyFlow.asStateFlow()

    companion object {
        private const val PREFS_NAME = "liquid_watch_history_v1"
        private const val KEY_HISTORY = "history_entries"

        @Volatile
        private var instance: WatchHistoryRepository? = null

        fun getInstance(context: Context): WatchHistoryRepository {
            return instance ?: synchronized(this) {
                instance ?: WatchHistoryRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    @Synchronized
    fun saveProgress(
        movieId: String,
        title: String,
        posterUrl: String,
        detailUrl: String,
        positionMs: Long,
        durationMs: Long
    ) {
        if (movieId.isBlank() || durationMs <= 0 || positionMs <= 1000) return

        val currentList = _historyFlow.value.toMutableList()
        currentList.removeAll { it.movieId == movieId }

        // If watched more than 95%, treat as watched/completed
        val isCompleted = positionMs >= (durationMs * 0.95f)

        val entry = WatchHistoryEntry(
            movieId = movieId,
            title = title,
            posterUrl = posterUrl,
            detailUrl = detailUrl,
            positionMs = positionMs,
            durationMs = durationMs,
            timestamp = System.currentTimeMillis()
        )

        currentList.add(0, entry)
        val trimmed = currentList.take(30) // keep latest 30
        _historyFlow.value = trimmed
        persistHistory(trimmed)
    }

    fun getSavedPosition(movieId: String): Long {
        val entry = _historyFlow.value.find { it.movieId == movieId } ?: return 0L
        // Don't resume if watched > 95%
        if (entry.durationMs > 0 && entry.positionMs >= (entry.durationMs * 0.95f)) {
            return 0L
        }
        return entry.positionMs
    }

    fun getContinueWatchingList(): List<ContinueWatchingItem> {
        return _historyFlow.value
            .filter { entry ->
                // Filter out nearly finished movies
                entry.durationMs > 0 && entry.positionMs < (entry.durationMs * 0.95f)
            }
            .map { it.toContinueWatchingItem() }
    }

    fun getTotalWatchedCount(): Int {
        return _historyFlow.value.size
    }

    fun getTotalWatchTimeMinutes(): Long {
        val totalMs = _historyFlow.value.sumOf { it.positionMs }
        return (totalMs / 1000) / 60
    }

    fun clearHistory() {
        _historyFlow.value = emptyList()
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun loadHistory(): List<WatchHistoryEntry> {
        val jsonStr = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<WatchHistoryEntry>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    WatchHistoryEntry(
                        movieId = obj.optString("movieId", ""),
                        title = obj.optString("title", ""),
                        posterUrl = obj.optString("posterUrl", ""),
                        detailUrl = obj.optString("detailUrl", ""),
                        positionMs = obj.optLong("positionMs", 0L),
                        durationMs = obj.optLong("durationMs", 0L),
                        timestamp = obj.optLong("timestamp", 0L)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun persistHistory(list: List<WatchHistoryEntry>) {
        try {
            val jsonArray = JSONArray()
            for (item in list) {
                val obj = JSONObject().apply {
                    put("movieId", item.movieId)
                    put("title", item.title)
                    put("posterUrl", item.posterUrl)
                    put("detailUrl", item.detailUrl)
                    put("positionMs", item.positionMs)
                    put("durationMs", item.durationMs)
                    put("timestamp", item.timestamp)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
        } catch (_: Exception) {}
    }
}
