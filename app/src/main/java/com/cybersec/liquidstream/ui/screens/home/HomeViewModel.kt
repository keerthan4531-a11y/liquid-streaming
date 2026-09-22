package com.cybersec.liquidstream.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cybersec.liquidstream.data.model.Movie
import com.cybersec.liquidstream.data.repository.MovieRepository
import com.cybersec.liquidstream.data.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContinueWatchingItem(
    val movie: Movie,
    val progress: Float, // 0.0 to 1.0
    val remainingTimeText: String = "",
    val positionMs: Long = 0L,
    val durationMs: Long = 0L
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val heroMovies: List<Movie> = emptyList(),
    val continueWatching: List<ContinueWatchingItem> = emptyList(),
    val recommendedMovies: List<Movie> = emptyList(),
    val actionMovies: List<Movie> = emptyList(),
    val horrorMovies: List<Movie> = emptyList(),
    val comedyMovies: List<Movie> = emptyList(),
    val kidsMovies: List<Movie> = emptyList(),
    val seriesMovies: List<Movie> = emptyList(),
    val loveMovies: List<Movie> = emptyList(),
    val romanceMovies: List<Movie> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(
    val repository: MovieRepository = MovieRepository(),
    private val watchHistoryRepository: WatchHistoryRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeFeed()
        observeWatchHistory()
    }

    private fun observeWatchHistory() {
        val repo = watchHistoryRepository ?: return
        viewModelScope.launch {
            repo.historyFlow.collect {
                val realContinue = repo.getContinueWatchingList()
                _uiState.value = _uiState.value.copy(continueWatching = realContinue)
            }
        }
    }

    fun refreshContinueWatching() {
        watchHistoryRepository?.let { repo ->
            val realContinue = repo.getContinueWatchingList()
            _uiState.value = _uiState.value.copy(continueWatching = realContinue)
        }
    }

    fun loadHomeFeed() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val heroes = repository.getFeaturedHeroMovies()
            val recommended = repository.getRecommendedMovies().take(20)
            val action = repository.getMoviesByGenre("Action").take(20)
            val horror = repository.getMoviesByGenre("Horror").take(20)
            val comedy = repository.getMoviesByGenre("Comedy").take(20)
            val kids = repository.getMoviesByGenre("Kids").take(20)
            val series = repository.getMoviesByGenre("Web Series").take(20)
            val love = repository.getMoviesByGenre("Love").take(20)
            val romance = repository.getMoviesByGenre("Romance").take(20)

            // Real Continue watching from persistence
            val realContinue = watchHistoryRepository?.getContinueWatchingList() ?: emptyList()

            _uiState.value = HomeUiState(
                isLoading = false,
                heroMovies = if (heroes.isNotEmpty()) heroes else recommended.take(5),
                continueWatching = realContinue,
                recommendedMovies = recommended,
                actionMovies = action,
                horrorMovies = horror,
                comedyMovies = comedy,
                kidsMovies = kids,
                seriesMovies = series,
                loveMovies = love,
                romanceMovies = romance
            )
        }
    }
}
