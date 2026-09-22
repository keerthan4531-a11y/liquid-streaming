package com.cybersec.liquidstream.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import android.widget.Toast
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.cybersec.liquidstream.data.model.Movie
import com.cybersec.liquidstream.data.repository.WatchlistRepository
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.rounded.Movie
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.cybersec.liquidstream.ui.components.MovieDetailSkeleton
import com.cybersec.liquidstream.core.glass.LiquidGlassColors
import com.cybersec.liquidstream.core.glass.LiquidGlassState
import com.cybersec.liquidstream.data.model.StreamSource
import com.cybersec.liquidstream.ui.components.GlassCard
import com.cybersec.liquidstream.ui.components.LiquidGlassButton

/**
 * CINEVA-style Movie Detail Screen — V2 with enhanced glass effects.
 * Backdrop poster, glass back button, quality selector, action buttons, and security card.
 */
@Composable
fun MovieDetailScreen(
    viewModel: MovieDetailViewModel,
    state: LiquidGlassState,
    onBack: () -> Unit,
    onNavigateToPlayer: (StreamSource) -> Unit,
    onDownloadSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val watchlistRepo = remember { WatchlistRepository.getInstance(context) }
    val watchlist by watchlistRepo.watchlistFlow.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidGlassColors.Background)
    ) {
        val detail = uiState.movieDetail

        if (uiState.isLoading || detail == null) {
            MovieDetailSkeleton(state = state)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 60.dp)
            ) {
                // Header Backdrop
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                ) {
                    // Ambient background gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        state.themePreset.primaryAccent.copy(alpha = 0.35f),
                                        Color(0xFF16102B),
                                        LiquidGlassColors.Background
                                    )
                                )
                            )
                    )

                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(detail.posterUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = detail.title,
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                state.themePreset.primaryAccent.copy(alpha = 0.35f),
                                                Color(0xFF16102B),
                                                LiquidGlassColors.Background
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = state.themePreset.primaryAccent,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                state.themePreset.primaryAccent.copy(alpha = 0.40f),
                                                Color(0xFF22133D),
                                                LiquidGlassColors.Background
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Movie,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.35f),
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        LiquidGlassColors.Background.copy(alpha = 0.75f),
                                        LiquidGlassColors.Background
                                    )
                                )
                            )
                    )

                    // Floating Glass Back Button (Library-backed glass card)
                    Box(
                        modifier = Modifier
                            .padding(top = 40.dp, start = 20.dp)
                            .align(Alignment.TopStart)
                    ) {
                        GlassCard(
                            state = state,
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            IconButton(onClick = onBack, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Floating Glass Bookmark Button on Top Right
                    val isBookmarked = watchlist.any { it.id == detail.id || it.title.equals(detail.title, ignoreCase = true) }
                    Box(
                        modifier = Modifier
                            .padding(top = 40.dp, end = 20.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        GlassCard(
                            state = state,
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    val movieToSave = Movie(
                                        id = detail.id,
                                        title = detail.title,
                                        posterUrl = detail.posterUrl,
                                        detailUrl = detail.detailUrl,
                                        rating = detail.rating,
                                        year = detail.year,
                                        genre = detail.genres
                                    )
                                    val saved = watchlistRepo.toggleBookmark(movieToSave)
                                    Toast.makeText(
                                        context,
                                        if (saved) "Added to Watchlist" else "Removed from Watchlist",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                    contentDescription = "Watchlist",
                                    tint = if (isBookmarked) state.themePreset.primaryAccent else Color.White
                                )
                            }
                        }
                    }
                }

                // Title Section with Elevated Poster Thumbnail Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Floating High-Res Poster Thumbnail Card
                    Box(
                        modifier = Modifier
                            .width(105.dp)
                            .height(152.dp)
                            .offset(y = (-36).dp)
                            .shadow(16.dp, RoundedCornerShape(14.dp), spotColor = state.themePreset.primaryAccent)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF16102B))
                            .border(1.2.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(detail.posterUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = detail.title,
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        color = state.themePreset.primaryAccent,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            error = {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Movie,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.35f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = detail.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xEE120E22))
                                    .border(0.8.dp, Color(0x50FFD700), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "★ ${detail.rating}",
                                    color = Color(0xFFFFD700),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(detail.year, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(detail.language, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                            Text(
                                detail.qualityBadge.ifEmpty { "HQ PreDVD" },
                                color = state.themePreset.primaryAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .offset(y = (-18).dp)
                ) {

                    if (detail.genres.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "🎭 ${detail.genres}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (detail.starring.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⭐ Starring: ${detail.starring}",
                            color = Color.White.copy(alpha = 0.70f),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }

                    if (detail.director.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🎬 Director: ${detail.director}",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    }

                    if (detail.lastUpdated.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🕒 Last Updated: ${detail.lastUpdated}",
                            color = Color.White.copy(alpha = 0.50f),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = detail.synopsis,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.80f),
                        lineHeight = 21.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Resolution & Quality Selector
                    Text(
                        text = "SELECT VIDEO QUALITY",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = state.themePreset.primaryAccent
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    detail.availableQualities.forEach { quality ->
                        val isSelected = uiState.selectedQuality == quality
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) state.themePreset.primaryAccent.copy(alpha = 0.2f) else Color(0x1AFFFFFF))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) state.themePreset.primaryAccent else Color(0x22FFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.selectQuality(quality) }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = quality.resolutionName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Estimated size: ${quality.sampleFileSize} • MP4 H.264",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = state.themePreset.primaryAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        LiquidGlassButton(
                            text = if (uiState.isResolvingStream) "Resolving..." else "Watch Online",
                            icon = Icons.Rounded.PlayArrow,
                            state = state,
                            isPrimary = true,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.resolveStreamAndPlay { stream ->
                                    onNavigateToPlayer(stream)
                                }
                            }
                        )

                        LiquidGlassButton(
                            text = if (uiState.downloadStarted) "Downloading" else "Download",
                            icon = Icons.Rounded.Download,
                            state = state,
                            isPrimary = false,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.startDownload {
                                    onDownloadSuccess()
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Cyber Security & Reverse Engineering Inspector Card
                    GlassCard(
                        state = state,
                        shape = RoundedCornerShape(14.dp),
                        tintOverride = Color(0xF0120E22)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Security,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "REVERSE ENGINEERING PIPELINE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• Multi-hop bypass: moviessda.com → gotopage.top → moviezda.com → downloadpage.xyz → justdownload.xyz\n" +
                                        "• Protocol: HTTP 206 Partial Content enabled (?stream=1)\n" +
                                        "• Security Status: 100% Ad-blockers & Popunder scripts bypassed via headless OkHttp engine.",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
