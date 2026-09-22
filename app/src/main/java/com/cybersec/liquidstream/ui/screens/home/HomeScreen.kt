package com.cybersec.liquidstream.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybersec.liquidstream.R
import com.cybersec.liquidstream.core.glass.LiquidGlassColors
import com.cybersec.liquidstream.core.glass.LiquidGlassState
import com.cybersec.liquidstream.core.glass.physics.liquidGelPress
import com.cybersec.liquidstream.core.glass.prism.PrismDispersionBrush
import com.cybersec.liquidstream.data.model.Movie
import com.cybersec.liquidstream.ui.components.ContinueWatchingCard
import com.cybersec.liquidstream.ui.components.HeroMovieBanner
import com.cybersec.liquidstream.ui.components.HomeScreenSkeleton
import com.cybersec.liquidstream.ui.components.MovieCard
import com.cybersec.liquidstream.ui.components.MovieRow
import com.cybersec.liquidstream.ui.components.TopTab
import com.cybersec.liquidstream.ui.components.TopTabBar

/**
 * 2026 Liquid Glass 3.0 Home Screen.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    state: LiquidGlassState,
    onMovieSelected: (Movie) -> Unit,
    onWatchOnline: (Movie) -> Unit,
    onDownload: (Movie) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenStudio: () -> Unit,
    onOpenAiChat: () -> Unit,
    onOpenProfile: () -> Unit = {},
    onSeeAllClick: (title: String, categoryKey: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val topTabs = remember {
        listOf(
            TopTab("Home", "home"),
            TopTab("Explore", "explore"),
            TopTab("My List", "mylist"),
            TopTab("Profile", "profile")
        )
    }
    var selectedTabId by remember { mutableStateOf("home") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidGlassColors.Background)
    ) {
        if (uiState.isLoading) {
            HomeScreenSkeleton(state = state)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 100.dp)
            ) {
                // 1. Brand Header (LiquidStream AI Logo + Name + Cast + Avatar)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 48.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "LiquidStream AI Logo",
                            modifier = Modifier
                                .size(38.dp)
                                .padding(end = 8.dp)
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "LIQUID",
                                    color = state.themePreset.primaryAccent,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "STREAM",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Text(
                                text = "STREAM YOUR WORLD",
                                color = Color.White.copy(alpha = 0.40f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Cast,
                                contentDescription = "Cast",
                                tint = Color.White.copy(alpha = 0.70f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // AI Assistant Header Quick Launch
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E1E2C).copy(alpha = 0.85f))
                                .border(
                                    width = 1.dp,
                                    brush = PrismDispersionBrush.chromaticBorderBrush(
                                        intensity = 0.70f,
                                        primaryAccent = state.themePreset.primaryAccent
                                    ),
                                    shape = CircleShape
                                )
                                .liquidGelPress(onClick = onOpenAiChat),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = "Ask CineAI",
                                tint = state.themePreset.primaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Profile avatar circle
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            state.themePreset.primaryAccent,
                                            state.themePreset.secondaryAccent
                                        )
                                    )
                                )
                                .clickable { onOpenProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // 2. Top Tab Bar (CINEVA glass pills)
                TopTabBar(
                    tabs = topTabs,
                    selectedTabId = selectedTabId,
                    onTabSelected = { tab ->
                        selectedTabId = tab.id
                        when (tab.id) {
                            "explore" -> onOpenSearch()
                            "profile" -> onOpenProfile()
                        }
                    },
                    state = state
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Hero Movie Banner (Auto-Swiping Carousel)
                HeroMovieBanner(
                    movies = uiState.heroMovies,
                    state = state,
                    onWatchOnline = { onWatchOnline(it) },
                    onDownload = { onDownload(it) },
                    onInfoClick = { onMovieSelected(it) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Continue Watching Section (CINEVA style)
                if (uiState.continueWatching.isNotEmpty()) {
                    SectionHeader(title = "Continue Watching")

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.continueWatching) { item ->
                            ContinueWatchingCard(
                                movie = item.movie,
                                progress = item.progress,
                                remainingTime = item.remainingTimeText,
                                state = state,
                                onClick = { onMovieSelected(item.movie) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 1. Recommended Movies Row
                if (uiState.recommendedMovies.isNotEmpty()) {
                    MovieRow(
                        title = "Recommended Movies",
                        movies = uiState.recommendedMovies,
                        state = state,
                        onMovieClick = onMovieSelected,
                        onSeeAllClick = { onSeeAllClick("Recommended Movies", "recommended") }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 2. Action Movies Row
                if (uiState.actionMovies.isNotEmpty()) {
                    MovieRow(
                        title = "Action Movies",
                        movies = uiState.actionMovies,
                        state = state,
                        onMovieClick = onMovieSelected,
                        onSeeAllClick = { onSeeAllClick("Action Movies", "action") }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 3. Horror Movies Row
                if (uiState.horrorMovies.isNotEmpty()) {
                    MovieRow(
                        title = "Horror Movies",
                        movies = uiState.horrorMovies,
                        state = state,
                        onMovieClick = onMovieSelected,
                        onSeeAllClick = { onSeeAllClick("Horror Movies", "horror") }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 4. Comedy Movies Row
                if (uiState.comedyMovies.isNotEmpty()) {
                    MovieRow(
                        title = "Comedy Movies",
                        movies = uiState.comedyMovies,
                        state = state,
                        onMovieClick = onMovieSelected,
                        onSeeAllClick = { onSeeAllClick("Comedy Movies", "comedy") }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 5. Kids & Animation Row
                if (uiState.kidsMovies.isNotEmpty()) {
                    MovieRow(
                        title = "Kids & Animation",
                        movies = uiState.kidsMovies,
                        state = state,
                        onMovieClick = onMovieSelected,
                        onSeeAllClick = { onSeeAllClick("Kids Movies", "kids") }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 6. Web Series Row
                if (uiState.seriesMovies.isNotEmpty()) {
                    MovieRow(
                        title = "Web Series",
                        movies = uiState.seriesMovies,
                        state = state,
                        onMovieClick = onMovieSelected,
                        onSeeAllClick = { onSeeAllClick("Web Series", "series") }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 7. Love Stories Row
                if (uiState.loveMovies.isNotEmpty()) {
                    MovieRow(
                        title = "Love Stories",
                        movies = uiState.loveMovies,
                        state = state,
                        onMovieClick = onMovieSelected,
                        onSeeAllClick = { onSeeAllClick("Love Stories", "love") }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 8. Romance Hits Row
                if (uiState.romanceMovies.isNotEmpty()) {
                    MovieRow(
                        title = "Romance Hits",
                        movies = uiState.romanceMovies,
                        state = state,
                        onMovieClick = onMovieSelected,
                        onSeeAllClick = { onSeeAllClick("Romance Movies", "romance") }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Floating Liquid Glass AI Orb (Quick access to CineAI / MiniTool AI)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 86.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = state.themePreset.primaryAccent.copy(alpha = 0.50f),
                    spotColor = state.themePreset.primaryAccent.copy(alpha = 0.85f)
                )
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            state.themePreset.primaryAccent,
                            state.themePreset.secondaryAccent
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = PrismDispersionBrush.chromaticBorderBrush(
                        intensity = 0.85f,
                        primaryAccent = Color.White
                    ),
                    shape = CircleShape
                )
                .liquidGelPress(onClick = onOpenAiChat),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = "CineAI Assistant",
                tint = Color.Black,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

/**
 * Reusable CINEVA-style section header: "Title" + "See All >"
 */
@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "See All >",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
