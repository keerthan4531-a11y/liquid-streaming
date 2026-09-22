package com.cybersec.liquidstream.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cybersec.liquidstream.core.glass.LiquidGlassColors
import com.cybersec.liquidstream.core.glass.LiquidGlassState

/**
 * 2026 Shimmer linear gradient brush with glowing cyan & deep purple highlights.
 */
@Composable
fun rememberShimmerBrush(
    accentColor: Color = Color(0xFF00E5FF)
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = -800f,
        targetValue = 1600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    return Brush.linearGradient(
        colors = listOf(
            Color(0xFF141324),
            Color(0xFF221F3A),
            accentColor.copy(alpha = 0.35f),
            Color(0xFF221F3A),
            Color(0xFF141324)
        ),
        start = Offset(translateAnim - 400f, translateAnim - 400f),
        end = Offset(translateAnim + 400f, translateAnim + 400f)
    )
}

/**
 * Single movie card shimmer placeholder.
 */
@Composable
fun MovieCardSkeleton(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(128.dp)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(shimmerBrush)
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(11.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush)
        )
    }
}

/**
 * Shimmer placeholder for horizontal movie rows.
 */
@Composable
fun MovieRowSkeleton(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(shimmerBrush)
            )
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(4) {
                MovieCardSkeleton(shimmerBrush = shimmerBrush)
            }
        }
    }
}

/**
 * Shimmer placeholder for Hero Movie Banner carousel.
 */
@Composable
fun HeroBannerSkeleton(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(shimmerBrush)
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(shimmerBrush)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(shimmerBrush)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(shimmerBrush)
                )
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(shimmerBrush)
                )
            }
        }
    }
}

/**
 * Full Home Screen shimmering skeleton layout.
 */
@Composable
fun HomeScreenSkeleton(state: LiquidGlassState) {
    val shimmerBrush = rememberShimmerBrush(state.themePreset.primaryAccent)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LiquidGlassColors.Background)
            .padding(top = 44.dp)
    ) {
        // Brand Header Skeleton
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(shimmerBrush)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(shimmerBrush))
                Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(shimmerBrush))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HeroBannerSkeleton(shimmerBrush = shimmerBrush)
        Spacer(modifier = Modifier.height(22.dp))
        MovieRowSkeleton(shimmerBrush = shimmerBrush)
        Spacer(modifier = Modifier.height(22.dp))
        MovieRowSkeleton(shimmerBrush = shimmerBrush)
    }
}

/**
 * Movie Detail Screen shimmering skeleton layout.
 */
@Composable
fun MovieDetailSkeleton(state: LiquidGlassState) {
    val shimmerBrush = rememberShimmerBrush(state.themePreset.primaryAccent)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LiquidGlassColors.Background)
    ) {
        // Large Backdrop Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .background(shimmerBrush)
        )

        Column(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(shimmerBrush)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(shimmerBrush)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(shimmerBrush)
            )
            Spacer(modifier = Modifier.height(20.dp))
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (it == 3) 0.5f else 1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * 2-column Grid Shimmer skeleton for Search and Category Detail screens.
 */
@Composable
fun MovieGridSkeleton(
    state: LiquidGlassState,
    modifier: Modifier = Modifier
) {
    val shimmerBrush = rememberShimmerBrush(state.themePreset.primaryAccent)

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(6) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(shimmerBrush)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(15.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
            }
        }
    }
}
