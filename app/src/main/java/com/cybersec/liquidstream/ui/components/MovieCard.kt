package com.cybersec.liquidstream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.cybersec.liquidstream.core.glass.LiquidGlassColors
import com.cybersec.liquidstream.core.glass.LiquidGlassState
import com.cybersec.liquidstream.core.glass.halo.AmbientHaloEngine
import com.cybersec.liquidstream.core.glass.halo.ambientPosterHalo
import com.cybersec.liquidstream.core.glass.physics.liquidGelPress
import com.cybersec.liquidstream.core.glass.physics.touchReactiveSpecular
import com.cybersec.liquidstream.core.glass.prism.PrismDispersionBrush
import com.cybersec.liquidstream.core.glass.prism.prismDispersionBorder
import com.cybersec.liquidstream.core.glass.texture.microFrostedGrain
import com.cybersec.liquidstream.data.model.Movie

/**
 * 2026 Liquid Glass 3.0 Movie Card.
 *
 * Features:
 * - Liquid Gel Spring Physics (tactile 3D press response)
 * - Dynamic Poster Color Absorption (Atmospheric Ambient Halo)
 * - Chromatic Prism Edge Dispersion (Optical light splitting)
 * - Touch-Reactive Specular Reflection (Light angle follows touch)
 * - Micro-Frosted Optical Glass Grain Texture
 */
@Composable
fun MovieCard(
    movie: Movie,
    state: LiquidGlassState,
    modifier: Modifier = Modifier,
    showAddButton: Boolean = false,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(14.dp)
    val haloColor = AmbientHaloEngine.resolveMovieHaloColor(movie, state.themePreset.primaryAccent)

    val effectiveModifier = if (modifier == Modifier) Modifier.width(135.dp) else modifier

    Column(
        modifier = effectiveModifier
            .clickable(onClick = onClick)
            .liquidGelPress(onClick = onClick)
    ) {
        // Poster Box with 2026 Living Glass Suite
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(195.dp)
                .clickable(onClick = onClick)
                .ambientPosterHalo(
                    state = state,
                    haloColor = haloColor,
                    shape = cardShape,
                    elevation = 12.dp,
                    intensity = 0.65f
                )
                .clip(cardShape)
                .background(LiquidGlassColors.CardSurface)
                .prismDispersionBorder(state = state, shape = cardShape, borderWidth = 1.dp)
                .microFrostedGrain(state = state)
                .touchReactiveSpecular(state = state, shape = cardShape)
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.posterUrl)
                    .crossfade(300)
                    .build(),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E1638), Color(0xFF0E0B1A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Movie,
                            contentDescription = null,
                            tint = Color(0x50FFFFFF),
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
                                    listOf(Color(0xFF2A1840), Color(0xFF140E24), Color(0xFF0A0816))
                                )
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Movie,
                                contentDescription = null,
                                tint = haloColor.copy(alpha = 0.85f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = movie.title,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            )

            // Cinematic gradient overlay at bottom for title readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.50f to Color.Transparent,
                            0.82f to Color.Black.copy(alpha = 0.60f),
                            1.0f to Color.Black.copy(alpha = 0.85f)
                        )
                    )
            )

            // Horizontal Optical Prism Sheen at top
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PrismDispersionBrush.horizontalPrismSheen(intensity = 0.80f))
            )

            // "+" Add button (CINEVA style) — bottom left
            if (showAddButton) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.20f), CircleShape)
                        .border(0.8.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Rating Chip — Top Right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xEE0A0816))
                    .border(0.8.dp, Color(0x60FFD700), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "★ ${movie.rating}",
                    color = Color(0xFFFFD700),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Badge — Top Left
            if (movie.badge.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(haloColor.copy(alpha = 0.95f), haloColor.copy(alpha = 0.75f))
                            )
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = movie.badge,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Title inside poster at bottom (CINEVA style)
            Text(
                text = movie.title.uppercase(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 8.dp, bottom = 8.dp, end = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Year subtitle below card
        Text(
            text = "${movie.year} • ${movie.category}",
            color = Color(0xFF9CA3AF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
