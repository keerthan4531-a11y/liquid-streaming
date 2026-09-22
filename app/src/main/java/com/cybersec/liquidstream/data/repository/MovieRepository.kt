package com.cybersec.liquidstream.data.repository

import android.content.Context
import android.util.Log
import com.cybersec.liquidstream.core.network.NetworkResult
import com.cybersec.liquidstream.data.model.Movie
import com.cybersec.liquidstream.data.model.MovieDetail
import com.cybersec.liquidstream.data.model.MovieQualityOption
import com.cybersec.liquidstream.data.model.StreamSource
import com.cybersec.liquidstream.data.parser.MoviesdaScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.InputStreamReader

data class YearCategoryItem(
    val id: String,
    val title: String,
    val slug: String,
    val yearTag: String
)

class MovieRepository(
    private val context: Context? = null
) {
    companion object {
        private const val TAG = "MovieRepository"
    }

    private val resolvedContext: Context?
        get() = context ?: try {
            com.cybersec.liquidstream.LiquidStreamApp.instance
        } catch (_: Exception) {
            null
        }

    val availableYears: List<YearCategoryItem> = listOf(
        YearCategoryItem("all", "All Years (3,805+)", "all", "All"),
        YearCategoryItem("hollywood", "🎬 Hollywood Dubbed (87)", "hollywood-dubbed", "Hollywood"),
        YearCategoryItem("2026", "2026 Movies (258)", "tamil-2026-movies", "2026"),
        YearCategoryItem("2025", "2025 Movies (489)", "tamil-2025-movies", "2025"),
        YearCategoryItem("2024", "2024 Movies (472)", "tamil-2024-movies", "2024"),
        YearCategoryItem("2023", "2023 Movies (381)", "tamil-2023-movies", "2023"),
        YearCategoryItem("2022", "2022 Movies (388)", "tamil-2022-movies", "2022"),
        YearCategoryItem("2021", "2021 Movies (381)", "tamil-2021-movies", "2021"),
        YearCategoryItem("2020", "2020 Movies (218)", "tamil-2020-movies", "2020"),
        YearCategoryItem("2019", "2019 Movies (201)", "tamil-2019-movies", "2019"),
        YearCategoryItem("2018", "2018 Movies (177)", "tamil-2018-movies", "2018"),
        YearCategoryItem("2017", "2017 Movies (187)", "tamil-2017-movies", "2017"),
        YearCategoryItem("2016", "2016 Movies (152)", "tamil-2016-movies", "2016"),
        YearCategoryItem("2015", "2015 Movies (91)", "tamil-2015-movies", "2015"),
        YearCategoryItem("2012", "2012 Movies (13)", "tamil-2012-movies", "2012"),
        YearCategoryItem("series", "Web Series (146)", "tamil-web-series-download", "Series"),
        YearCategoryItem("dubbed", "Tamil Dubbed (72)", "tamil-dubbed-movies", "Dubbed"),
        YearCategoryItem("collections", "Collections (41)", "tamil-movies-collection", "HQ"),
        YearCategoryItem("hd_mobile", "HD Mobile (42)", "tamil-hd-movies", "Mobile"),
        YearCategoryItem("single_parts", "Single Parts (16)", "moviesda-tamil-collections", "MP4")
    )

    // Master database of 3,350+ movies loaded from assets
    val databaseMovies: List<Movie> by lazy {
        loadDatabaseFromAssets()
    }

    // Hollywood Tamil Dubbed database loaded from assets
    private val hollywoodMoviesList: List<Movie> by lazy {
        loadHollywoodDatabaseFromAssets()
    }

    private fun loadHollywoodDatabaseFromAssets(): List<Movie> {
        val ctx = resolvedContext ?: return emptyList()
        return try {
            ctx.assets.open("hollywood_database.json").use { stream ->
                val reader = InputStreamReader(stream)
                val jsonText = reader.readText()
                val jsonArray = JSONArray(jsonText)
                val list = ArrayList<Movie>(jsonArray.length())
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        Movie(
                            id = obj.optString("id"),
                            title = obj.optString("title"),
                            posterUrl = obj.optString("posterUrl"),
                            detailUrl = obj.optString("detailUrl"),
                            rating = obj.optString("rating", "8.5"),
                            badge = obj.optString("badge", "HD"),
                            year = obj.optString("year", "2025"),
                            category = obj.optString("category", "Hollywood"),
                            genre = obj.optString("genre", "Action"),
                            duration = obj.optString("duration", "2h 15m"),
                            synopsis = obj.optString("synopsis", "Stream in Tamil Dubbed HD.")
                        )
                    )
                }
                Log.d(TAG, "Successfully loaded ${list.size} Hollywood movies from assets")
                list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading hollywood_database.json", e)
            emptyList()
        }
    }

    fun getHollywoodMovies(): List<Movie> = hollywoodMoviesList

    fun getHollywoodFeaturedHero(): List<Movie> = hollywoodMoviesList.take(6)

    fun getHollywoodMoviesByGenre(genre: String): List<Movie> {
        val target = genre.trim().lowercase()
        return hollywoodMoviesList.filter {
            it.genre.lowercase().contains(target) || it.title.lowercase().contains(target)
        }
    }

    fun searchHollywoodMovies(query: String): List<Movie> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return hollywoodMoviesList

        val words = q.split(" ").filter { it.isNotBlank() }
        return hollywoodMoviesList.filter { movie ->
            val title = movie.title.lowercase()
            val id = movie.id.lowercase()
            val year = movie.year.lowercase()
            val genre = movie.genre.lowercase()
            val category = movie.category.lowercase()
            val synopsis = movie.synopsis.lowercase()
            words.all { w ->
                title.contains(w) || id.contains(w) || year.contains(w) ||
                        genre.contains(w) || category.contains(w) || synopsis.contains(w)
            }
        }
    }

    private fun loadDatabaseFromAssets(): List<Movie> {
        val ctx = resolvedContext ?: return emptyList()
        return try {
            ctx.assets.open("movies_database.json").use { stream ->
                val reader = InputStreamReader(stream)
                val jsonText = reader.readText()
                val jsonArray = JSONArray(jsonText)
                val list = ArrayList<Movie>(jsonArray.length())
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optString("id")
                    val title = obj.optString("title")
                    val poster = obj.optString("posterUrl")
                    val detail = obj.optString("detailUrl")
                    val rating = obj.optString("rating", "8.5")
                    val badge = obj.optString("badge", "HD")
                    val year = obj.optString("year", "2026")
                    val category = obj.optString("category", "Tamil")
                    val genre = obj.optString("genre", if (year == "Series") "Web Series" else "Action / Drama")
                    list.add(
                        Movie(
                            id = id,
                            title = title,
                            posterUrl = poster,
                            detailUrl = detail,
                            rating = rating,
                            badge = badge,
                            year = year,
                            category = category,
                            genre = genre,
                            duration = if (genre == "Web Series" || year == "Series") "8 Episodes" else "2h 15m",
                            synopsis = "$title ($year) - Stream now in HD with direct multi-hop reverse-engineered pipeline from Moviesda."
                        )
                    )
                }
                Log.d(TAG, "Successfully loaded ${list.size} movies from assets database")
                list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading movies_database.json", e)
            emptyList()
        }
    }

    /**
     * Recommended movies: top-rated blockbusters from 2026 and 2025.
     */
    fun getRecommendedMovies(): List<Movie> {
        return databaseMovies
            .filter { it.year == "2026" || it.year == "2025" }
            .sortedByDescending { it.rating.toFloatOrNull() ?: 8.0f }
    }

    /**
     * Filter movies by explicit genre.
     */
    fun getMoviesByGenre(genre: String): List<Movie> {
        val target = genre.trim().lowercase()
        return databaseMovies.filter {
            it.genre.lowercase() == target || it.category.lowercase().contains(target)
        }
    }

    /**
     * Retrieve full category list for 'See All' pages.
     */
    fun getMoviesByCategoryKey(key: String): List<Movie> {
        return when (key.trim().lowercase()) {
            "recommended" -> getRecommendedMovies()
            "action" -> getMoviesByGenre("Action")
            "horror" -> getMoviesByGenre("Horror")
            "comedy" -> getMoviesByGenre("Comedy")
            "kids" -> getMoviesByGenre("Kids")
            "series", "web series", "websiries" -> getMoviesByGenre("Web Series")
            "love" -> getMoviesByGenre("Love")
            "romance" -> getMoviesByGenre("Romance")
            "hollywood_marvel", "marvel" -> getHollywoodMoviesByGenre("Marvel")
            "hollywood_action" -> getHollywoodMoviesByGenre("Action")
            "hollywood_scifi", "scifi", "sci-fi" -> getHollywoodMoviesByGenre("Sci-Fi")
            "hollywood_horror" -> getHollywoodMoviesByGenre("Horror")
            "hollywood_series" -> getHollywoodMoviesByGenre("Web Series")
            "hollywood_romance" -> getHollywoodMoviesByGenre("Romance")
            "hollywood_2026", "2026_hollywood" -> getHollywoodMovies().filter { it.year == "2026" }
            "hollywood_all", "hollywood" -> getHollywoodMovies()
            else -> (databaseMovies + hollywoodMoviesList).filter { it.year.equals(key, true) || it.category.contains(key, true) }
        }
    }

    /**
     * Searches across the ENTIRE movie database (Tamil + Hollywood) for old and new movies.
     */
    fun searchAllMovies(query: String): List<Movie> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return databaseMovies.take(30) + hollywoodMoviesList.take(20)

        val words = q.split(" ").filter { it.isNotBlank() }
        val combined = databaseMovies + hollywoodMoviesList
        return combined.filter { movie ->
            val title = movie.title.lowercase()
            val id = movie.id.lowercase()
            val year = movie.year.lowercase()
            val genre = movie.genre.lowercase()
            val category = movie.category.lowercase()
            words.all { w -> title.contains(w) || id.contains(w) || year.contains(w) || genre.contains(w) || category.contains(w) }
        }
    }

    suspend fun getFeaturedHeroMovies(): List<Movie> = withContext(Dispatchers.IO) {
        val list2026 = get2026Movies(page = 1)
        val combined = mutableListOf<Movie>()
        if (list2026 is NetworkResult.Success && list2026.data.isNotEmpty()) {
            combined.addAll(list2026.data.take(6))
        } else {
            val db2026 = databaseMovies.filter { it.year == "2026" }
            if (db2026.isNotEmpty()) combined.addAll(db2026.take(6))
        }

        if (combined.isNotEmpty()) combined else getFallback2026Movies()
    }

    suspend fun getMoviesBySlug(slug: String, page: Int = 1): NetworkResult<List<Movie>> = withContext(Dispatchers.IO) {
        try {
            if (slug == "all") {
                val combined = (databaseMovies + hollywoodMoviesList).distinctBy { it.id }
                if (combined.isNotEmpty()) {
                    return@withContext NetworkResult.Success(combined)
                }
            }

            if (slug.contains("hollywood")) {
                if (hollywoodMoviesList.isNotEmpty()) {
                    return@withContext NetworkResult.Success(hollywoodMoviesList)
                }
            }

            // Check if available in local master database first for instant loading
            val matchedFromDb = when {
                slug.contains("hollywood") -> hollywoodMoviesList
                slug.contains("2026") -> databaseMovies.filter { it.year == "2026" }
                slug.contains("2025") -> databaseMovies.filter { it.year == "2025" }
                slug.contains("2024") -> databaseMovies.filter { it.year == "2024" }
                slug.contains("2023") -> databaseMovies.filter { it.year == "2023" }
                slug.contains("2022") -> databaseMovies.filter { it.year == "2022" }
                slug.contains("2021") -> databaseMovies.filter { it.year == "2021" }
                slug.contains("2020") -> databaseMovies.filter { it.year == "2020" }
                slug.contains("2019") -> databaseMovies.filter { it.year == "2019" }
                slug.contains("2018") -> databaseMovies.filter { it.year == "2018" }
                slug.contains("2017") -> databaseMovies.filter { it.year == "2017" }
                slug.contains("2016") -> databaseMovies.filter { it.year == "2016" }
                slug.contains("2015") -> databaseMovies.filter { it.year == "2015" }
                slug.contains("2012") -> databaseMovies.filter { it.year == "2012" }
                slug.contains("series") -> databaseMovies.filter { it.year == "Series" || it.category.contains("Series", true) }
                slug.contains("dubbed") -> databaseMovies.filter { it.year == "Dubbed" || it.category.contains("Dubbed", true) }
                slug.contains("collection") -> databaseMovies.filter { it.year == "Collections" || it.category.contains("Collection", true) }
                slug.contains("single") -> databaseMovies.filter { it.year == "Single Parts" }
                slug.contains("hd-movies") || slug.contains("hd_mobile") -> databaseMovies.filter { it.year == "HD Mobile" || it.category.contains("HD Mobile", true) }
                else -> emptyList()
            }

            // Also attempt live online fetch to keep fresh
            val online = try {
                MoviesdaScraper.fetchCatalog(slug, page)
            } catch (e: Exception) {
                emptyList()
            }

            val finalMovies = (online + matchedFromDb).distinctBy { it.id }
            if (finalMovies.isNotEmpty()) {
                NetworkResult.Success(finalMovies)
            } else {
                NetworkResult.Success(getFallbackBySlug(slug))
            }
        } catch (e: Exception) {
            NetworkResult.Success(getFallbackBySlug(slug))
        }
    }

    suspend fun get2026Movies(page: Int = 1): NetworkResult<List<Movie>> =
        getMoviesBySlug("tamil-2026-movies", page)

    suspend fun get2025Movies(page: Int = 1): NetworkResult<List<Movie>> =
        getMoviesBySlug("tamil-2025-movies", page)

    suspend fun get2024Movies(page: Int = 1): NetworkResult<List<Movie>> =
        getMoviesBySlug("tamil-2024-movies", page)

    suspend fun getMovieDetails(movieUrl: String): NetworkResult<MovieDetail> = withContext(Dispatchers.IO) {
        try {
            // Try live scrape from Moviesda
            val detail = MoviesdaScraper.fetchDetails(movieUrl)
            NetworkResult.Success(detail)
        } catch (e: Exception) {
            Log.w(TAG, "Live detail scrape error: ${e.localizedMessage}, using cached metadata")
            // Fallback from master database
            val id = movieUrl.trimEnd('/').substringAfterLast('/')
            val cached = databaseMovies.find { it.id == id || it.detailUrl == movieUrl } 
                ?: hollywoodMoviesList.find { it.id == id || it.detailUrl == movieUrl }
            val fallback = MovieDetail(
                id = id,
                title = cached?.title ?: id.replace("-", " ").capitalizeWords(),
                posterUrl = cached?.posterUrl ?: "https://moviezda.com/uploads/posters/$id.jpg",
                detailUrl = movieUrl,
                rating = cached?.rating ?: "8.8",
                year = cached?.year ?: "2026",
                synopsis = "${cached?.title ?: "Movie"} (${cached?.year ?: "2026"}) - High definition stream with multi-hop download support.",
                availableQualities = listOf(
                    MovieQualityOption("1080p HD HQ", movieUrl, "2.4 GB"),
                    MovieQualityOption("720p HD", movieUrl, "1.2 GB"),
                    MovieQualityOption("480p HQ MP4", movieUrl, "450 MB")
                )
            )
            NetworkResult.Success(fallback)
        }
    }

    suspend fun resolveStream(qualityUrl: String): NetworkResult<StreamSource> = withContext(Dispatchers.IO) {
        try {
            val source = MoviesdaScraper.resolveStreamSource(qualityUrl)
            NetworkResult.Success(source)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to resolve stream link: ${e.localizedMessage}", e)
        }
    }

    private fun getFallbackBySlug(slug: String): List<Movie> {
        val filtered = databaseMovies.filter {
            slug.contains(it.year)
        }
        if (filtered.isNotEmpty()) return filtered

        return when {
            slug.contains("2026") -> getFallback2026Movies()
            slug.contains("2025") -> databaseMovies.filter { it.year == "2025" }.ifEmpty { getFallback2026Movies() }
            else -> databaseMovies.take(20)
        }
    }

    private fun getFallback2026Movies(): List<Movie> = listOf(
        Movie(
            id = "bethlehem-kudumba-unit-2026-tamil-movie",
            title = "Bethlehem Kudumba Unit",
            posterUrl = "https://moviezda.com/uploads/posters/bethlehem-kudumba-unit-2026.jpg",
            detailUrl = "https://moviezda.com/bethlehem-kudumba-unit-2026-tamil-movie/",
            rating = "9.5",
            badge = "#1 TRENDING",
            year = "2026"
        ),
        Movie(
            id = "bigg-boss-2026-tamil-web-series",
            title = "Bigg Boss Tamil (2026)",
            posterUrl = "https://moviezda.com/uploads/posters/bigg-boss-2026.jpg",
            detailUrl = "https://moviezda.com/bigg-boss-2026-tamil-web-series/",
            rating = "8.8",
            badge = "HOT",
            year = "2026"
        ),
        Movie(
            id = "immortal-2026-tamil-movie",
            title = "Immortal",
            posterUrl = "https://moviezda.com/uploads/posters/immortal-2026.jpg",
            detailUrl = "https://moviezda.com/immortal-2026-tamil-movie/",
            rating = "8.9",
            badge = "NEW",
            year = "2026"
        ),
        Movie(
            id = "the-yellow-elephant-2026-tamil-movie",
            title = "The Yellow Elephant",
            posterUrl = "https://moviezda.com/uploads/posters/the-yellow-elephant-2026.jpg",
            detailUrl = "https://moviezda.com/the-yellow-elephant-2026-tamil-movie/",
            rating = "8.4",
            badge = "HD",
            year = "2026"
        ),
        Movie(
            id = "im-game-2026-tamil-movie",
            title = "Im Game",
            posterUrl = "https://moviezda.com/uploads/posters/im-game-2026.jpg",
            detailUrl = "https://moviezda.com/im-game-2026-tamil-movie/",
            rating = "8.7",
            badge = "HQ",
            year = "2026"
        )
    )

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}
