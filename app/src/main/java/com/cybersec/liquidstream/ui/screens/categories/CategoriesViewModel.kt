package com.cybersec.liquidstream.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cybersec.liquidstream.data.model.Movie
import com.cybersec.liquidstream.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val searchResults: List<Movie> = emptyList(),
    val isSearching: Boolean = false,
    val heroMovies: List<Movie> = emptyList(),
    val marvelMovies: List<Movie> = emptyList(),
    val actionMovies: List<Movie> = emptyList(),
    val sciFiMovies: List<Movie> = emptyList(),
    val horrorMovies: List<Movie> = emptyList(),
    val seriesMovies: List<Movie> = emptyList(),
    val romanceMovies: List<Movie> = emptyList(),
    val latest2026Movies: List<Movie> = emptyList(),
    val allHollywoodMovies: List<Movie> = emptyList(),
    val errorMessage: String? = null
)

class CategoriesViewModel(
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        loadHollywoodHub()
    }

    fun loadHollywoodHub() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val allHollywood = repository.getHollywoodMovies()
            val heroes = repository.getHollywoodFeaturedHero()
            val marvel = repository.getHollywoodMoviesByGenre("Marvel")
            val action = repository.getHollywoodMoviesByGenre("Action")
            val scifi = repository.getHollywoodMoviesByGenre("Sci-Fi")
            val horror = repository.getHollywoodMoviesByGenre("Horror")
            val series = repository.getHollywoodMoviesByGenre("Web Series")
            val romance = repository.getHollywoodMoviesByGenre("Romance")
            val latest2026 = allHollywood.filter { it.year == "2026" }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                allHollywoodMovies = allHollywood,
                heroMovies = if (heroes.isNotEmpty()) heroes else marvel.take(5),
                marvelMovies = marvel,
                actionMovies = action,
                sciFiMovies = scifi,
                horrorMovies = horror,
                seriesMovies = series,
                romanceMovies = romance,
                latest2026Movies = latest2026
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                searchQuery = query,
                searchResults = emptyList(),
                isSearching = false
            )
        } else {
            val results = repository.searchHollywoodMovies(trimmed)
            _uiState.value = _uiState.value.copy(
                searchQuery = query,
                searchResults = results,
                isSearching = true
            )
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            searchResults = emptyList(),
            isSearching = false
        )
    }
}
