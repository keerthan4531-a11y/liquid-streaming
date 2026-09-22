package com.cybersec.liquidstream.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cybersec.liquidstream.core.network.NetworkResult
import com.cybersec.liquidstream.data.model.Movie
import com.cybersec.liquidstream.data.repository.MovieRepository
import com.cybersec.liquidstream.data.repository.YearCategoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val selectedCategory: YearCategoryItem,
    val availableYears: List<YearCategoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val displayedMovies: List<Movie> = emptyList(),
    val activeCategoryMovies: List<Movie> = emptyList()
)

class SearchViewModel(
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {

    private val cache = mutableMapOf<String, List<Movie>>()
    private val availableYears = repository.availableYears

    private val _uiState = MutableStateFlow(
        SearchUiState(
            selectedCategory = availableYears.first(),
            availableYears = availableYears
        )
    )
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        // Pre-load default (2026 or All)
        selectCategory(availableYears.first())
    }

    fun seedInitialMovies(initial: List<Movie>) {
        if (initial.isNotEmpty() && !cache.containsKey("all")) {
            cache["all"] = initial
            if (_uiState.value.selectedCategory.slug == "all" && _uiState.value.activeCategoryMovies.isEmpty()) {
                applyFilter(initial, _uiState.value.query)
            }
        }
    }

    fun selectCategory(category: YearCategoryItem) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)

        val cached = cache[category.slug]
        if (cached != null && cached.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                activeCategoryMovies = cached,
                isLoading = false
            )
            applyFilter(cached, _uiState.value.query)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.getMoviesBySlug(category.slug)
            val movies = if (result is NetworkResult.Success) result.data else emptyList()

            cache[category.slug] = movies
            _uiState.value = _uiState.value.copy(
                activeCategoryMovies = movies,
                isLoading = false
            )
            applyFilter(movies, _uiState.value.query)
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
        applyFilter(_uiState.value.activeCategoryMovies, newQuery)
    }

    private fun applyFilter(source: List<Movie>, q: String) {
        val filtered = if (q.isBlank()) {
            source
        } else {
            repository.searchAllMovies(q)
        }
        _uiState.value = _uiState.value.copy(displayedMovies = filtered)
    }
}
