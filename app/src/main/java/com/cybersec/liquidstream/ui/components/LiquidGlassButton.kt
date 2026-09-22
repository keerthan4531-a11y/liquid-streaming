package com.cybersec.liquidstream.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybersec.liquidstream.core.glass.LiquidGlassState
import com.cybersec.liquidstream.core.glass.liquidGlassAccent
import com.cybersec.liquidstream.core.glass.liquidGlassSurface

/**
 * CINEVA-style glass button.
 * Primary: accent gradient glass (violet). Secondary: dark glass with border.
 */
@Composable
fun LiquidGlassButton(
    text: String,
    icon: ImageVector,
    state: LiquidGlassState,
    isPrimary: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = modifier
            .height(48.dp)
            .then(
                if (isPrimary) {
                    Modifier.liquidGlassAccent(
                        state = state,
                        accentColor = state.themePreset.primaryAccent,
                        shape = shape
                    )
                } else {
                    Modifier.liquidGlassSurface(
                        state = state,
                        shape = shape,
                        tintOverride = Color(0xD81A1436),
                        elevated = true
                    )
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = if (isPrimary) FontWeight.ExtraBold else FontWeight.Bold
        )
    }
}
