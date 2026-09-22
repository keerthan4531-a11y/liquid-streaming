package com.cybersec.liquidstream.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cybersec.liquidstream.core.glass.LiquidGlassState
import com.cybersec.liquidstream.core.glass.liquidGlassSurface

@Composable
fun GlassCard(
    state: LiquidGlassState,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderWidth: Dp = 1.2.dp,
    tintOverride: Color? = null,
    elevated: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.liquidGlassSurface(
            state = state,
            shape = shape,
            borderWidth = borderWidth,
            tintOverride = tintOverride,
            elevated = elevated
        ),
        content = content
    )
}
