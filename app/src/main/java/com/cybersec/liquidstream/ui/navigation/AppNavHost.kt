package com.cybersec.liquidstream.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cybersec.liquidstream.core.glass.rememberLiquidGlassState
import com.cybersec.liquidstream.data.model.Movie
import com.cybersec.liquidstream.data.repository.AiChatRepository
import com.cybersec.liquidstream.data.repository.DownloadRepository
import android.content.Context
import com.cybersec.liquidstream.data.repository.MovieRepository
import com.cybersec.liquidstream.ui.components.CustomizationSheet
import com.cybersec.liquidstream.ui.components.LiquidGlassNavBar
import com.cybersec.liquidstream.ui.components.NavItem
import com.cybersec.liquidstream.ui.components.OnboardingWelcomeDialog
import com.cybersec.liquidstream.ui.screens.about.AboutScreen
import com.cybersec.liquidstream.ui.screens.aichat.AiChatScreen
import com.cybersec.liquidstream.ui.screens.aichat.AiChatViewModel
import com.cybersec.liquidstream.ui.screens.category.CategoryDetailScreen
import com.cybersec.liquidstream.ui.screens.categories.CategoriesScreen
import com.cybersec.liquidstream.ui.screens.categories.CategoriesViewModel
import com.cybersec.liquidstream.ui.screens.detail.MovieDetailScreen
import com.cybersec.liquidstream.ui.screens.detail.MovieDetailViewModel
import com.cybersec.liquidstream.ui.screens.downloads.DownloadsScreen
import com.cybersec.liquidstream.ui.screens.downloads.DownloadsViewModel
import com.cybersec.liquidstream.ui.screens.home.HomeScreen
import com.cybersec.liquidstream.ui.screens.home.HomeViewModel
import com.cybersec.liquidstream.ui.screens.player.PlayerScreen
import com.cybersec.liquidstream.ui.screens.player.PlayerViewModel
import com.cybersec.liquidstream.ui.screens.profile.ProfileScreen
import com.cybersec.liquidstream.ui.screens.search.SearchScreen
import com.cybersec.liquidstream.ui.screens.search.SearchViewModel
import com.cybersec.liquidstream.ui.screens.settings.SettingsScreen
import com.cybersec.liquidstream.ui.screens.splash.SplashScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    incomingIntent: android.content.Intent? = null
) {
    val context = LocalContext.current
    val glassState = rememberLiquidGlassState()

    val movieRepository = remember { MovieRepository(context) }
    val downloadRepository = remember { DownloadRepository(context) }
    val watchHistoryRepository = remember { com.cybersec.liquidstream.data.repository.WatchHistoryRepository.getInstance(context) }
    val watchlistRepository = remember { com.cybersec.liquidstream.data.repository.WatchlistRepository.getInstance(context) }
    val aiChatRepository = remember { AiChatRepository() }

    val homeViewModel: HomeViewModel = viewModel { HomeViewModel(movieRepository, watchHistoryRepository) }
    val downloadsViewModel: DownloadsViewModel = viewModel { DownloadsViewModel(downloadRepository) }
    val searchViewModel: SearchViewModel = viewModel { SearchViewModel(movieRepository) }
    val categoriesViewModel: CategoriesViewModel = viewModel { CategoriesViewModel(movieRepository) }
    val aiChatViewModel: AiChatViewModel = viewModel { AiChatViewModel(aiChatRepository) }

    val activity = context as? android.app.Activity
    val startIntentTarget = activity?.intent?.getStringExtra("navigate_to")
    val categoryKeyExtra = activity?.intent?.getStringExtra("category_key") ?: "action"
    val categoryTitleExtra = activity?.intent?.getStringExtra("category_title") ?: "Action Movies"

    val prefs = remember { context.getSharedPreferences("liquid_prefs", Context.MODE_PRIVATE) }
    var showWelcome by remember {
        mutableStateOf(!prefs.getBoolean("has_seen_welcome_v3", false))
    }

    var showSplash by remember { mutableStateOf(startIntentTarget == null) }
    var currentScreen by remember {
        mutableStateOf<Screen>(
            when (startIntentTarget) {
                "category" -> Screen.CategoryDetail(categoryTitleExtra, categoryKeyExtra)
                "categories" -> Screen.Categories
                "player" -> Screen.Player(
                    com.cybersec.liquidstream.data.model.StreamSource(
                        title = "Bethlehem Kudumba Unit (2026)",
                        directStreamUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
                        directDownloadUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
                        watchOnlinePageUrl = "",
                        fileName = "bethlehem_kudumba_unit_2026.mp4",
                        fileSize = "1.2 GB",
                        videoResolution = "1080p HD",
                        duration = "2h 18m"
                    )
                )
                "profile" -> Screen.Profile
                "settings" -> Screen.Settings
                "about" -> Screen.About
                else -> Screen.Home
            }
        )
    }
    var selectedNavItem by remember {
        mutableStateOf(
            when (startIntentTarget) {
                "profile", "settings", "about" -> NavItem.PROFILE
                "categories" -> NavItem.CATEGORIES
                "search" -> NavItem.SEARCH
                "downloads" -> NavItem.DOWNLOADS
                else -> NavItem.HOME
            }
        )
    }
    var isStudioOpen by remember { mutableStateOf(false) }

    val homeUiState by homeViewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(incomingIntent, activity?.intent) {
        val effectiveIntent = incomingIntent ?: activity?.intent
        val target = effectiveIntent?.getStringExtra("navigate_to") ?: return@LaunchedEffect
        if (target == "category") {
            val key = effectiveIntent.getStringExtra("category_key") ?: "action"
            val title = effectiveIntent.getStringExtra("category_title") ?: "Action Movies"
            currentScreen = Screen.CategoryDetail(title, key)
            showSplash = false
        } else if (target == "categories") {
            selectedNavItem = NavItem.CATEGORIES
            currentScreen = Screen.Categories
            showSplash = false
            showWelcome = false
        } else if (target == "player") {
            val playerTitle = effectiveIntent.getStringExtra("player_title") ?: "Bethlehem Kudumba Unit (2026)"
            val streamUrl = effectiveIntent.getStringExtra("stream_url") ?: "https://www.w3schools.com/html/mov_bbb.mp4"
            val watchPage = effectiveIntent.getStringExtra("watch_page") ?: ""
            val headersMap = mutableMapOf<String, String>()
            val referer = effectiveIntent.getStringExtra("stream_referer")
            if (!referer.isNullOrBlank()) {
                headersMap["Referer"] = referer
            }
            currentScreen = Screen.Player(
                com.cybersec.liquidstream.data.model.StreamSource(
                    title = playerTitle,
                    directStreamUrl = streamUrl,
                    directDownloadUrl = streamUrl,
                    watchOnlinePageUrl = watchPage,
                    fileName = "${playerTitle.replace(" ", "_")}.mp4",
                    fileSize = "1.2 GB",
                    videoResolution = "1080p HD",
                    duration = "2h 18m",
                    streamHeaders = headersMap
                )
            )
            showSplash = false
        } else if (target == "detail") {
            val movieId = effectiveIntent.getStringExtra("movie_id") ?: "mvl_deadpool_wolverine_2024"
            val movieTitle = effectiveIntent.getStringExtra("movie_title") ?: "Deadpool & Wolverine (2024)"
            val movieUrl = effectiveIntent.getStringExtra("movie_url") ?: "https://isaidub.green/movie/deadpool-and-wolverine-2024-tamil-dubbed-movie/"
            val moviePoster = effectiveIntent.getStringExtra("movie_poster") ?: "https://image.tmdb.org/t/p/w500/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg"
            val testMovie = Movie(
                id = movieId,
                title = movieTitle,
                posterUrl = moviePoster,
                detailUrl = movieUrl,
                rating = "8.1",
                year = "2024",
                category = "Hollywood"
            )
            currentScreen = Screen.Detail(testMovie)
            showSplash = false
            showWelcome = false
        } else if (target == "profile") {
            selectedNavItem = NavItem.PROFILE
            currentScreen = Screen.Profile
            showSplash = false
            showWelcome = false
        } else if (target == "settings") {
            selectedNavItem = NavItem.PROFILE
            currentScreen = Screen.Settings
            showSplash = false
            showWelcome = false
        } else if (target == "about") {
            selectedNavItem = NavItem.PROFILE
            currentScreen = Screen.About
            showSplash = false
            showWelcome = false
        } else if (target == "search") {
            selectedNavItem = NavItem.SEARCH
            currentScreen = Screen.Search
            val query = effectiveIntent.getStringExtra("search_query")
            if (!query.isNullOrBlank()) {
                searchViewModel.onQueryChange(query)
            }
            val catSlug = effectiveIntent.getStringExtra("category_slug")
            if (!catSlug.isNullOrBlank()) {
                val cat = movieRepository.availableYears.find { it.slug == catSlug }
                if (cat != null) {
                    searchViewModel.selectCategory(cat)
                }
            }
            showSplash = false
            showWelcome = false
        }
    }

    androidx.compose.runtime.LaunchedEffect(
        homeUiState.recommendedMovies,
        homeUiState.actionMovies,
        homeUiState.horrorMovies,
        homeUiState.comedyMovies
    ) {
        val all = homeUiState.recommendedMovies + homeUiState.actionMovies + homeUiState.horrorMovies + homeUiState.comedyMovies
        if (all.isNotEmpty()) {
            searchViewModel.seedInitialMovies(all)
        }
    }

    // Handle Hardware Back Button
    BackHandler(enabled = currentScreen !is Screen.Home) {
        when (val screen = currentScreen) {
            is Screen.Detail -> currentScreen = Screen.Home
            is Screen.Player -> currentScreen = Screen.Home
            is Screen.CategoryDetail -> {
                if (screen.categoryKey.startsWith("hollywood")) {
                    selectedNavItem = NavItem.CATEGORIES
                    currentScreen = Screen.Categories
                } else {
                    selectedNavItem = NavItem.HOME
                    currentScreen = Screen.Home
                }
            }
            Screen.AiChat -> currentScreen = Screen.Home
            Screen.About -> {
                selectedNavItem = NavItem.PROFILE
                currentScreen = Screen.Profile
            }
            Screen.Settings -> {
                selectedNavItem = NavItem.PROFILE
                currentScreen = Screen.Profile
            }
            Screen.Categories, Screen.Profile, Screen.Search, Screen.Downloads -> {
                selectedNavItem = NavItem.HOME
                currentScreen = Screen.Home
            }
            Screen.Home -> {}
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val screen = currentScreen) {
            is Screen.Home -> {
                HomeScreen(
                    viewModel = homeViewModel,
                    state = glassState,
                    onMovieSelected = { movie -> currentScreen = Screen.Detail(movie) },
                    onWatchOnline = { movie ->
                        currentScreen = Screen.Detail(movie)
                    },
                    onDownload = { movie ->
                        currentScreen = Screen.Detail(movie)
                    },
                    onOpenSearch = {
                        selectedNavItem = NavItem.SEARCH
                        currentScreen = Screen.Search
                    },
                    onOpenStudio = { isStudioOpen = true },
                    onOpenAiChat = { currentScreen = Screen.AiChat },
                    onOpenProfile = {
                        selectedNavItem = NavItem.PROFILE
                        currentScreen = Screen.Profile
                    },
                    onSeeAllClick = { title, key ->
                        currentScreen = Screen.CategoryDetail(title, key)
                    }
                )
            }
            is Screen.Categories -> {
                CategoriesScreen(
                    viewModel = categoriesViewModel,
                    state = glassState,
                    onMovieSelected = { movie -> currentScreen = Screen.Detail(movie) },
                    onWatchOnline = { movie -> currentScreen = Screen.Detail(movie) },
                    onDownload = { movie -> currentScreen = Screen.Detail(movie) },
                    onOpenSearch = {
                        selectedNavItem = NavItem.SEARCH
                        currentScreen = Screen.Search
                    },
                    onSeeAllClick = { title, key ->
                        currentScreen = Screen.CategoryDetail(title, key)
                    }
                )
            }
            is Screen.AiChat -> {
                AiChatScreen(
                    viewModel = aiChatViewModel,
                    state = glassState,
                    onBack = { currentScreen = Screen.Home }
                )
            }
            is Screen.CategoryDetail -> {
                CategoryDetailScreen(
                    title = screen.title,
                    categoryKey = screen.categoryKey,
                    movieRepository = movieRepository,
                    state = glassState,
                    onBack = {
                        if (screen.categoryKey.startsWith("hollywood")) {
                            selectedNavItem = NavItem.CATEGORIES
                            currentScreen = Screen.Categories
                        } else {
                            selectedNavItem = NavItem.HOME
                            currentScreen = Screen.Home
                        }
                    },
                    onMovieSelected = { movie -> currentScreen = Screen.Detail(movie) }
                )
            }
            is Screen.Search -> {
                SearchScreen(
                    viewModel = searchViewModel,
                    state = glassState,
                    onMovieSelected = { movie -> currentScreen = Screen.Detail(movie) }
                )
            }
            is Screen.Downloads -> {
                DownloadsScreen(
                    viewModel = downloadsViewModel,
                    state = glassState
                )
            }
            is Screen.Profile -> {
                ProfileScreen(
                    state = glassState,
                    onNavigateToSettings = { currentScreen = Screen.Settings },
                    onNavigateToAbout = { currentScreen = Screen.About },
                    onNavigateToDownloads = {
                        selectedNavItem = NavItem.DOWNLOADS
                        currentScreen = Screen.Downloads
                    }
                )
            }
            is Screen.Settings -> {
                SettingsScreen(
                    state = glassState,
                    onBack = {
                        selectedNavItem = NavItem.PROFILE
                        currentScreen = Screen.Profile
                    }
                )
            }
            is Screen.About -> {
                AboutScreen(
                    state = glassState,
                    onBack = {
                        selectedNavItem = NavItem.PROFILE
                        currentScreen = Screen.Profile
                    }
                )
            }
            is Screen.Detail -> {
                val detailViewModel: MovieDetailViewModel = viewModel(key = screen.movie.id) {
                    MovieDetailViewModel(movieRepository, downloadRepository)
                }
                androidx.compose.runtime.LaunchedEffect(screen.movie.id) {
                    detailViewModel.loadMovie(screen.movie)
                }

                MovieDetailScreen(
                    viewModel = detailViewModel,
                    state = glassState,
                    onBack = { currentScreen = Screen.Home },
                    onNavigateToPlayer = { stream ->
                        currentScreen = Screen.Player(stream)
                    },
                    onDownloadSuccess = {
                        selectedNavItem = NavItem.DOWNLOADS
                        currentScreen = Screen.Downloads
                    }
                )
            }
            is Screen.Player -> {
                val playerViewModel: PlayerViewModel = viewModel { PlayerViewModel() }
                PlayerScreen(
                    streamSource = screen.streamSource,
                    viewModel = playerViewModel,
                    state = glassState,
                    onClose = { currentScreen = Screen.Home }
                )
            }
        }

        // Floating Liquid Glass Navigation Bar (visible on root tabs)
        if (currentScreen is Screen.Home || currentScreen is Screen.Categories || currentScreen is Screen.Search ||
            currentScreen is Screen.Downloads || currentScreen is Screen.Profile
        ) {
            LiquidGlassNavBar(
                selectedItem = selectedNavItem,
                onItemSelected = { item ->
                    selectedNavItem = item
                    currentScreen = item.toScreen()
                },
                state = glassState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Quick Customization Bottom Sheet
        if (isStudioOpen) {
            CustomizationSheet(
                state = glassState,
                onDismiss = { isStudioOpen = false }
            )
        }

        // App Launch Animated Splash Screen with AI Transparent Logo
        if (showSplash) {
            SplashScreen(
                state = glassState,
                onSplashFinished = { showSplash = false }
            )
        }

        // First-Time Launch Onboarding Dialog
        if (!showSplash && showWelcome) {
            OnboardingWelcomeDialog(
                state = glassState,
                onDismiss = {
                    prefs.edit().putBoolean("has_seen_welcome_v3", true).apply()
                    showWelcome = false
                }
            )
        }
    }
}
