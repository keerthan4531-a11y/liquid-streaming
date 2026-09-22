package com.cybersec.liquidstream.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.cybersec.liquidstream.core.glass.physics.liquidGelPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cybersec.liquidstream.core.glass.LiquidGlassColors
import com.cybersec.liquidstream.core.glass.LiquidGlassState
import com.cybersec.liquidstream.core.glass.liquidGlassBorder
import com.cybersec.liquidstream.core.glass.liquidGlassFrost
import com.cybersec.liquidstream.data.model.Movie
import kotlinx.coroutines.delay

/**
 * CINEVA-Style Netflix Hero Banner — V2 Exact Screenshot Match.
 *
 * Auto-swiping carousel with cinematic poster, glass buttons, page dots.
 * Exact layout match from screenshot:
 * "✨ New Release" → Giant Title → Year • Genre • Duration → ★ Rating • Age → Synopsis → Play + More Info
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HeroMovieBanner(
    movies: List<Movie>,
    state: LiquidGlassState,
    modifier: Modifier = Modifier,
    onWatchOnline: (Movie) -> Unit,
    onDownload: (Movie) -> Unit,
    onInfoClick: (Movie) -> Unit
) {
    if (movies.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { movies.size })

    // Auto-swipe every 4.5 seconds
    LaunchedEffect(pagerState, movies.size) {
        if (movies.size > 1) {
            while (true) {
                delay(4500)
                try {
                    val next = (pagerState.currentPage + 1) % movies.size
                    pagerState.animateScrollToPage(
                        page = next,
                        animationSpec = tween(durationMillis = 700)
                    )
                } catch (_: Exception) { }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(520.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val movie = movies[page]
            Box(modifier = Modifier.fillMaxSize()) {
                // Hero Poster Backdrop
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(movie.posterUrl)
                        .crossfade(400)
                        .build(),
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Multi-stop Cinematic Gradient Mask (CINEVA purple fade)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.15f to Color.Transparent,
                                0.40f to LiquidGlassColors.Background.copy(alpha = 0.50f),
                                0.65f to LiquidGlassColors.Background.copy(alpha = 0.88f),
                                1.0f to LiquidGlassColors.Background
                            )
                        )
                )

                // Content Overlay — Exact screenshot layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 40.dp, top = 100.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.Start
                ) {
                    // "✨ New Release" Badge (matches screenshot pill)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (page == 0) "✨ New Release" else "⭐ #${page + 1} Top Featured",
                            color = Color.White.copy(alpha = 0.90f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Giant Movie Title — CINEVA style: uppercase, black weight, dramatic shadow
                    Text(
                        text = movie.title.uppercase(),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 36.sp,
                        maxLines = 2,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.90f),
                                offset = Offset(0f, 4f),
                                blurRadius = 16f
                            )
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Meta Info Row 1: "2024 • Sci-Fi • 2h 18m" (matches screenshot)
                    Text(
                        text = "${movie.year} • ${movie.genre} • ${movie.duration}",
                        color = Color.White.copy(alpha = 0.70f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Meta Info Row 2: "★ 4.8 • U/A 16+" (matches screenshot)
                    Text(
                        text = "★ ${movie.rating} • ${movie.ageRating}",
                        color = Color.White.copy(alpha = 0.60f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Synopsis (matches screenshot italic description)
                    Text(
                        text = movie.synopsis,
                        color = Color.White.copy(alpha = 0.50f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action Buttons Row — CINEVA: ▶ Play = frosted white glass, ⓘ More Info = border glass
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ▶ Play Button (frosted white glass + liquid gel physics)
                        Row(
                            modifier = Modifier
                                .liquidGelPress(onClick = { onWatchOnline(movie) })
                                .liquidGlassFrost(state = state, shape = RoundedCornerShape(28.dp))
                                .height(42.dp)
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Play", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        // ⓘ More Info Button (border-only glass + liquid gel physics)
                        Row(
                            modifier = Modifier
                                .liquidGelPress(onClick = { onInfoClick(movie) })
                                .liquidGlassBorder(state = state, shape = RoundedCornerShape(28.dp))
                                .height(42.dp)
                                .padding(horizontal = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Rounded.Info, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("More Info", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // Animated Page Indicator Dots (bottom center — matches screenshot)
        if (movies.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(movies.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 20.dp else 6.dp,
                        animationSpec = tween(300),
                        label = "indicatorWidth"
                    )

                    Box(
                        modifier = Modifier
                            .height(5.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White
                                else Color.White.copy(alpha = 0.30f)
                            )
                    )
                }
            }
        }
    }
}
