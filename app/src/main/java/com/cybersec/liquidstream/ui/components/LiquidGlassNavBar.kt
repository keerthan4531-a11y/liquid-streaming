package com.cybersec.liquidstream.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybersec.liquidstream.core.glass.LiquidGlassState
import com.cybersec.liquidstream.core.glass.liquidGlassNavigation
import com.cybersec.liquidstream.core.glass.physics.liquidGelPress
import com.cybersec.liquidstream.core.glass.prism.PrismDispersionBrush
import com.cybersec.liquidstream.ui.navigation.Screen
import kotlin.math.abs

/**
 * 5-tab CINEVA-style frosted glass bottom navigation bar.
 * Uses real Liquid Glass blur (liquidGlassNavigation modifier) for the bar surface.
 * Tabs: Home, Search, Categories, Downloads, More
 */
enum class NavItem(
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    HOME("Home", Icons.Rounded.Home, Icons.Outlined.Home),
    SEARCH("Search", Icons.Rounded.Search, Icons.Outlined.Search),
    CATEGORIES("Categories", Icons.Rounded.Category, Icons.Outlined.Category),
    DOWNLOADS("Downloads", Icons.Rounded.FileDownload, Icons.Outlined.FileDownload),
    PROFILE("Profile", Icons.Rounded.Person, Icons.Outlined.Person);

    fun toScreen(): Screen = when (this) {
        HOME -> Screen.Home
        SEARCH -> Screen.Search
        CATEGORIES -> Screen.Categories
        DOWNLOADS -> Screen.Downloads
        PROFILE -> Screen.Profile
    }
}

/**
 * 2026 Magnetic Liquid Glide Bottom Navigation Bar.
 *
 * Features:
 * - Real hardware AGSL shader blur for navigation chrome per Apple HIG
 * - Magnetic Liquid Glow Pill: Stretches and glides smoothly across tabs like liquid mercury
 * - Chromatic prism reflection on the floating indicator
 * - 3D fluid tactile response on tab selection
 */
@Composable
fun LiquidGlassNavBar(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    state: LiquidGlassState,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val pillShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .liquidGlassNavigation(state = state, shape = shape)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp)
        ) {
            val totalTabs = NavItem.entries.size
            val tabWidth = maxWidth / totalTabs
            val selectedIndex = NavItem.entries.indexOf(selectedItem).coerceAtLeast(0)

            // Calculate magnetic pill target offset (centered in tab)
            val pillBaseWidth = (tabWidth * 0.76f).coerceIn(48.dp, 68.dp)
            val targetLeftOffset = tabWidth * selectedIndex + (tabWidth - pillBaseWidth) / 2

            // Magnetic fluid spring physics
            val animatedLeftOffset by animateDpAsState(
                targetValue = targetLeftOffset,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "magnetic_pill_offset"
            )

            // Dynamic fluid elongation: Pill physically stretches slightly while in transit
            val distance = abs((animatedLeftOffset - targetLeftOffset).value)
            val dynamicStretch = (distance * 0.18f).coerceAtMost(16f).dp
            val dynamicPillWidth = pillBaseWidth + dynamicStretch

            // Magnetic Liquid Glow Pill (Active Background Indicator)
            Box(
                modifier = Modifier
                    .offset(x = animatedLeftOffset - dynamicStretch / 2, y = 2.dp)
                    .width(dynamicPillWidth)
                    .height(44.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = pillShape,
                        clip = false,
                        ambientColor = state.themePreset.primaryAccent.copy(alpha = 0.50f),
                        spotColor = state.themePreset.primaryAccent.copy(alpha = 0.70f)
                    )
                    .clip(pillShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                state.themePreset.primaryAccent.copy(alpha = 0.28f),
                                state.themePreset.primaryAccent.copy(alpha = 0.12f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = PrismDispersionBrush.chromaticBorderBrush(
                            intensity = 0.85f,
                            primaryAccent = state.themePreset.primaryAccent
                        ),
                        shape = pillShape
                    )
            )

            // Foreground Tab Icons and Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem.entries.forEach { item ->
                    val isSelected = item == selectedItem

                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.45f),
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "navIconColor"
                    )
                    val labelColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.40f),
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "navLabelColor"
                    )

                    Column(
                        modifier = Modifier
                            .width(tabWidth)
                            .liquidGelPress(onClick = { onItemSelected(item) })
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            color = labelColor,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
