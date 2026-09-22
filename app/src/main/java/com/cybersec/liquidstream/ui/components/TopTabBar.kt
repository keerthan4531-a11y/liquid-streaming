package com.cybersec.liquidstream.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybersec.liquidstream.core.glass.LiquidGlassState

/**
 * CINEVA-style top tab bar with glass pill selection indicator.
 * Matches the "Home | Explore | My List | Profile" pills in the screenshot.
 * Selected pill gets frosted glass surface with specular border.
 */
data class TopTab(val label: String, val id: String)

@Composable
fun TopTabBar(
    tabs: List<TopTab>,
    selectedTabId: String,
    onTabSelected: (TopTab) -> Unit,
    state: LiquidGlassState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            val isSelected = tab.id == selectedTabId
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) Color.White.copy(alpha = 0.14f) else Color.Transparent,
                animationSpec = spring(),
                label = "tabBg"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) Color.White.copy(alpha = 0.30f) else Color.Transparent,
                animationSpec = spring(),
                label = "tabBorder"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.55f),
                animationSpec = spring(),
                label = "tabText"
            )

            Box(
                modifier = Modifier
                    .height(34.dp)
                    .then(
                        if (isSelected) {
                            Modifier
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    clip = false,
                                    ambientColor = Color.White.copy(alpha = 0.08f),
                                    spotColor = Color.White.copy(alpha = 0.10f)
                                )
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.18f),
                                            Color.White.copy(alpha = 0.10f)
                                        )
                                    ),
                                    RoundedCornerShape(20.dp)
                                )
                                .border(0.8.dp, borderColor, RoundedCornerShape(20.dp))
                        } else {
                            Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(bgColor, RoundedCornerShape(20.dp))
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onTabSelected(tab) }
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.label,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
