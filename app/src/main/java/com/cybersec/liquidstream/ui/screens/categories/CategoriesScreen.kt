package com.cybersec.liquidstream.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybersec.liquidstream.core.glass.LiquidGlassColors
import com.cybersec.liquidstream.core.glass.LiquidGlassState
import com.cybersec.liquidstream.data.model.Movie
import com.cybersec.liquidstream.ui.components.GlassCard
import com.cybersec.liquidstream.ui.components.HeroMovieBanner
import com.cybersec.liquidstream.ui.components.HomeScreenSkeleton
import com.cybersec.liquidstream.ui.components.MovieCard
import com.cybersec.liquidstream.ui.components.MovieRow

@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    state: LiquidGlassState,
    onMovieSelected: (Movie) -> Unit,
    onWatchOnline: (Movie) -> Unit,
    onDownload: (Movie) -> Unit,
    onOpenSearch: () -> Unit,
    onSeeAllClick: (title: String, categoryKey: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val quickChips = remember {
        listOf(
            "All" to "hollywood_all",
            "Marvel & DC" to "hollywood_marvel",
            "Action" to "hollywood_action",
            "Sci-Fi" to "hollywood_scifi",
            "Horror" to "hollywood_horror",
            "Web Series" to "hollywood_series",
            "Romance" to "hollywood_romance",
            "2026 Latest" to "hollywood_2026"
        )
    }
    var selectedChipKey by remember { mutableStateOf("hollywood_all") }

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
                // Header: Hollywood Tamil Dubbed Hub Brand Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 48.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            state.themePreset.primaryAccent,
                                            Color(0xFFFF007F)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Movie,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "HOLLYWOOD",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(state.themePreset.primaryAccent.copy(alpha = 0.25f))
                                        .border(0.8.dp, state.themePreset.primaryAccent.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "TAMIL DUB",
                                        color = state.themePreset.primaryAccent,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                            Text(
                                text = "4K HDR & 1080P DUAL AUDIO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // Global Search Button
                    GlassCard(
                        state = state,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = onOpenSearch, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Global Search",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Dedicated Hollywood Search Bar
                GlassCard(
                    state = state,
                    shape = RoundedCornerShape(18.dp),
                    tintOverride = Color(0xF2140E24),
                    elevated = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .height(52.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search Hollywood",
                            tint = state.themePreset.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )

                        BasicTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(state.themePreset.primaryAccent),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp),
                            decorationBox = { innerTextField ->
                                if (uiState.searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search Hollywood Movies (Deadpool, Marvel, Horror...)",
                                        color = Color.White.copy(alpha = 0.40f),
                                        fontSize = 13.sp
                                    )
                                }
                                innerTextField()
                            }
                        )

                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.clearSearch() },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Branch between Hollywood Search Results or Curated Categories Hub
                if (uiState.isSearching && uiState.searchQuery.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Found ${uiState.searchResults.size} Hollywood Movies",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Clear",
                                color = state.themePreset.primaryAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { viewModel.clearSearch() }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (uiState.searchResults.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Rounded.Movie,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No Hollywood movies found matching \"${uiState.searchQuery}\"",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            val chunked = uiState.searchResults.chunked(2)
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                for (pair in chunked) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        for (movie in pair) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                MovieCard(
                                                    movie = movie,
                                                    state = state,
                                                    onClick = { onMovieSelected(movie) }
                                                )
                                            }
                                        }
                                        if (pair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Regular Curated Hollywood Hub View
                    // Hero Carousel Banner (Top Hollywood releases)
                    if (uiState.heroMovies.isNotEmpty()) {
                        HeroMovieBanner(
                            movies = uiState.heroMovies,
                            state = state,
                            onWatchOnline = onWatchOnline,
                            onDownload = onDownload,
                            onInfoClick = onMovieSelected
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Category Filter Pills
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickChips) { (label, key) ->
                            val isSelected = selectedChipKey == key
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) state.themePreset.primaryAccent
                                        else Color(0x22FFFFFF)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) state.themePreset.primaryAccent
                                        else Color(0x33FFFFFF),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        selectedChipKey = key
                                        if (key != "hollywood_all") {
                                            onSeeAllClick(label, key)
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 1. Marvel & DC Universe Row
                    if (uiState.marvelMovies.isNotEmpty()) {
                        MovieRow(
                            title = "🦸 Marvel & DC Universe",
                            movies = uiState.marvelMovies,
                            state = state,
                            onMovieClick = onMovieSelected,
                            onSeeAllClick = { onSeeAllClick("Marvel & DC Universe", "hollywood_marvel") }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // 2. Hollywood Action Tamil Dubbed
                    if (uiState.actionMovies.isNotEmpty()) {
                        MovieRow(
                            title = "💥 Hollywood Action (Tamil Dub)",
                            movies = uiState.actionMovies,
                            state = state,
                            onMovieClick = onMovieSelected,
                            onSeeAllClick = { onSeeAllClick("Hollywood Action Dubbed", "hollywood_action") }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // 3. Sci-Fi & Cyberpunk
                    if (uiState.sciFiMovies.isNotEmpty()) {
                        MovieRow(
                            title = "🛸 Sci-Fi & Cyberpunk",
                            movies = uiState.sciFiMovies,
                            state = state,
                            onMovieClick = onMovieSelected,
                            onSeeAllClick = { onSeeAllClick("Sci-Fi Tamil Dubbed", "hollywood_scifi") }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // 4. Hollywood Horror & Supernatural
                    if (uiState.horrorMovies.isNotEmpty()) {
                        MovieRow(
                            title = "👻 Hollywood Horror",
                            movies = uiState.horrorMovies,
                            state = state,
                            onMovieClick = onMovieSelected,
                            onSeeAllClick = { onSeeAllClick("Hollywood Horror Dubbed", "hollywood_horror") }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // 5. Hollywood Web Series (Tamil)
                    if (uiState.seriesMovies.isNotEmpty()) {
                        MovieRow(
                            title = "📺 Hollywood Web Series",
                            movies = uiState.seriesMovies,
                            state = state,
                            onMovieClick = onMovieSelected,
                            onSeeAllClick = { onSeeAllClick("Hollywood Web Series (Tamil)", "hollywood_series") }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // 6. Romance & Drama
                    if (uiState.romanceMovies.isNotEmpty()) {
                        MovieRow(
                            title = "💖 Love & Romance",
                            movies = uiState.romanceMovies,
                            state = state,
                            onMovieClick = onMovieSelected,
                            onSeeAllClick = { onSeeAllClick("Hollywood Romance Dubbed", "hollywood_romance") }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // 7. Latest 2026 Releases
                    if (uiState.latest2026Movies.isNotEmpty()) {
                        MovieRow(
                            title = "🌟 2026 Latest Hollywood Dubs",
                            movies = uiState.latest2026Movies,
                            state = state,
                            onMovieClick = onMovieSelected,
                            onSeeAllClick = { onSeeAllClick("2026 Hollywood Tamil Dubs", "hollywood_2026") }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
