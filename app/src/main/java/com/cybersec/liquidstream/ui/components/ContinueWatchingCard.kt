package com.cybersec.liquidstream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
 * 2026 Liquid Glass 3.0 "Continue Watching" landscape card.
 *
 * Features:
 * - Liquid Gel Press Response
 * - Dynamic Ambient Halo glow
 * - Chromatic Prism Edge Refraction
 * - Touch-Reactive Specular Highlight
 * - Glowing progress track
 */
@Composable
fun ContinueWatchingCard(
    movie: Movie,
    progress: Float, // 0.0 to 1.0
    state: LiquidGlassState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    remainingTime: String = ""
) {
    val cardShape = RoundedCornerShape(14.dp)
    val haloColor = AmbientHaloEngine.resolveMovieHaloColor(movie, state.themePreset.primaryAccent)

    Column(
        modifier = modifier
            .width(175.dp)
            .liquidGelPress(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .width(175.dp)
                .aspectRatio(16f / 10f)
                .ambientPosterHalo(
                    state = state,
                    haloColor = haloColor,
                    shape = cardShape,
                    elevation = 10.dp,
                    intensity = 0.55f
                )
                .clip(cardShape)
                .background(LiquidGlassColors.CardSurface)
                .prismDispersionBorder(state = state, shape = cardShape, borderWidth = 1.dp)
                .microFrostedGrain(state = state)
                .touchReactiveSpecular(state = state, shape = cardShape)
        ) {
            // Poster
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.posterUrl)
                    .crossfade(300)
                    .build(),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Cinematic gradient overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.40f to Color.Transparent,
                            0.72f to Color.Black.copy(alpha = 0.65f),
                            1.0f to Color.Black.copy(alpha = 0.95f)
                        )
                    )
            )

            // Horizontal Optical Prism Sheen at top
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PrismDispersionBrush.horizontalPrismSheen(intensity = 0.75f))
            )

            // Frosted Play icon circle with specular border
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, bottom = 22.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f), CircleShape)
                    .border(
                        0.8.dp,
                        PrismDispersionBrush.chromaticBorderBrush(
                            intensity = 0.70f,
                            primaryAccent = haloColor
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Title at bottom (CINEVA style — uppercase)
            Text(
                text = movie.title.uppercase(),
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, bottom = 8.dp, end = 40.dp)
            )

            // Progress & Remaining Time
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (remainingTime.isNotBlank()) {
                    Text(
                        text = remainingTime,
                        color = haloColor,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Progress bar at very bottom with accent glow
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter),
                color = haloColor,
                trackColor = Color.White.copy(alpha = 0.15f)
            )
        }
    }
}
