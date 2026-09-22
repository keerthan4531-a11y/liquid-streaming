package com.cybersec.liquidstream.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.cybersec.liquidstream.data.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class WatchlistRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _watchlistFlow = MutableStateFlow<List<Movie>>(loadWatchlist())
    val watchlistFlow: StateFlow<List<Movie>> = _watchlistFlow.asStateFlow()

    companion object {
        private const val PREFS_NAME = "liquid_watchlist_v1"
        private const val KEY_WATCHLIST = "watchlist_movies"

        @Volatile
        private var instance: WatchlistRepository? = null

        fun getInstance(context: Context): WatchlistRepository {
            return instance ?: synchronized(this) {
                instance ?: WatchlistRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    fun isBookmarked(movieId: String): Boolean {
        return _watchlistFlow.value.any { it.id == movieId }
    }

    @Synchronized
    fun toggleBookmark(movie: Movie): Boolean {
        val current = _watchlistFlow.value.toMutableList()
        val exists = current.any { it.id == movie.id }
        if (exists) {
            current.removeAll { it.id == movie.id }
        } else {
            current.add(0, movie)
        }
        _watchlistFlow.value = current
        persistWatchlist(current)
        return !exists // returns true if now bookmarked
    }

    fun getWatchlistCount(): Int {
        return _watchlistFlow.value.size
    }

    private fun loadWatchlist(): List<Movie> {
        val jsonStr = prefs.getString(KEY_WATCHLIST, null) ?: return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<Movie>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Movie(
                        id = obj.optString("id", ""),
                        title = obj.optString("title", ""),
                        posterUrl = obj.optString("posterUrl", ""),
                        detailUrl = obj.optString("detailUrl", ""),
                        rating = obj.optString("rating", "8.5"),
                        badge = obj.optString("badge", "NEW"),
                        year = obj.optString("year", "2026"),
                        category = obj.optString("category", "Tamil"),
                        genre = obj.optString("genre", "Drama")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun persistWatchlist(list: List<Movie>) {
        try {
            val arr = JSONArray()
            for (m in list) {
                val obj = JSONObject().apply {
                    put("id", m.id)
                    put("title", m.title)
                    put("posterUrl", m.posterUrl)
                    put("detailUrl", m.detailUrl)
                    put("rating", m.rating)
                    put("badge", m.badge)
                    put("year", m.year)
                    put("category", m.category)
                    put("genre", m.genre)
                }
                arr.put(obj)
            }
            prefs.edit().putString(KEY_WATCHLIST, arr.toString()).apply()
        } catch (_: Exception) {}
    }
}
