package com.cybersec.liquidstream.ui.screens.about

import android.content.Intent
import android.net.Uri
import android.os.Build
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybersec.liquidstream.R
import com.cybersec.liquidstream.core.glass.LiquidGlassColors
import com.cybersec.liquidstream.core.glass.LiquidGlassState
import com.cybersec.liquidstream.ui.components.GlassCard
import kotlinx.coroutines.delay

@Composable
fun AboutScreen(
    state: LiquidGlassState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val accent = state.themePreset.primaryAccent
    val secondaryAccent = state.themePreset.secondaryAccent

    // High Animation Staggered Visibility Flags
    var showHeader by remember { mutableStateOf(false) }
    var showAppCard by remember { mutableStateOf(false) }
    var showCreatorCard by remember { mutableStateOf(false) }
    var showInstaCard by remember { mutableStateOf(false) }
    var showTechStack by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showHeader = true
        delay(120)
        showAppCard = true
        delay(150)
        showCreatorCard = true
        delay(150)
        showInstaCard = true
        delay(150)
        showTechStack = true
    }

    // Cyber Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "cyber_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val glowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing)
        ),
        label = "glow_rotation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidGlassColors.Background)
            .padding(top = 40.dp, bottom = 40.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            // Top Bar with Back Button
            AnimatedVisibility(
                visible = showHeader,
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { -it }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x25FFFFFF))
                            .border(1.dp, accent.copy(alpha = 0.5f), CircleShape)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "SYSTEM ARCHITECTURE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = accent
                        )
                        Text(
                            text = "About LiquidStream",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            // 1. App Identity Card (Cyber Hero)
            AnimatedVisibility(
                visible = showAppCard,
                enter = fadeIn(tween(400)) + scaleIn(initialScale = 0.92f, animationSpec = tween(400))
            ) {
                GlassCard(
                    state = state,
                    shape = RoundedCornerShape(22.dp),
                    tintOverride = Color(0xF0130E26)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // App Logo with glowing cyber halo
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        listOf(accent, Color(0xFF00F2FE), secondaryAccent, accent)
                                    )
                                )
                                .padding(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color(0xFF0A0718)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_app_logo),
                                    contentDescription = "LiquidStream Logo",
                                    modifier = Modifier.size(54.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "LIQUIDSTREAM",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = Color.White
                        )

                        Text(
                            text = "v3.0.0 ULTRA CYBERNETIC // 2026",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = accent
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x18FFFFFF))
                                .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Zero-Ad Next-Gen P2P Streaming Architecture",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Engineered with real-time Android Graphics Shading Language (AGSL) liquid glass optics, military-grade anti-popunder shield, and hardware-accelerated ExoPlayer video pipeline.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. CREATOR SECTION: mokka coding
            AnimatedVisibility(
                visible = showCreatorCard,
                enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 2 }
            ) {
                GlassCard(
                    state = state,
                    shape = RoundedCornerShape(22.dp),
                    tintOverride = Color(0xF0100C22)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Section Header Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF00F2FE).copy(alpha = 0.18f))
                                    .border(1.dp, Color(0xFF00F2FE).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Code,
                                        contentDescription = null,
                                        tint = Color(0xFF00F2FE),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "ARCHITECT & CREATOR",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp,
                                        color = Color(0xFF00F2FE)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Creator Profile Row: Custom DP avatar + Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // High-Tech Circular DP Avatar
                            Box(
                                modifier = Modifier
                                    .size(86.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.sweepGradient(
                                            listOf(
                                                Color(0xFF00F2FE),
                                                accent,
                                                secondaryAccent,
                                                Color(0xFF00FF9D),
                                                Color(0xFF00F2FE)
                                            )
                                        )
                                    )
                                    .padding(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color(0xFF080512))
                                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                ) {
                                    // Creator Photo cropped from user's image
                                    Image(
                                        painter = painterResource(id = R.drawable.creator_avatar),
                                        contentDescription = "mokka coding Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "mokka coding",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.Verified,
                                        contentDescription = "Verified Creator",
                                        tint = Color(0xFF00F2FE),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Lead Android Developer & UI Engineer",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Specializing in High-Performance Native Android, Jetpack Compose, & Cybernetic Shader UIs.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. INSTAGRAM CONNECTION CARD
            AnimatedVisibility(
                visible = showInstaCard,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }
            ) {
                val instagramUrl = "https://www.instagram.com/dark.shadow_4531?stkn=MTZua29kbXpoOGhnMA=="
                
                GlassCard(
                    state = state,
                    shape = RoundedCornerShape(18.dp),
                    tintOverride = Color(0xF0180D26),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback browser
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Instagram Profile Avatar: Creator DP with iconic Instagram Story gradient ring
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        listOf(
                                            Color(0xFF833AB4),
                                            Color(0xFFFD1D1D),
                                            Color(0xFFF77737),
                                            Color(0xFFFFDC80),
                                            Color(0xFF833AB4)
                                        )
                                    )
                                )
                                .padding(2.5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color(0xFF0C081A))
                                    .border(1.5.dp, Color(0xFF0C081A), CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.insta_avatar),
                                    contentDescription = "dark.shadow_4531 Instagram DP",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CONNECT ON INSTAGRAM",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = Color(0xFFF77737)
                            )
                            Text(
                                text = "@dark.shadow_4531",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Tap to view profile & projects",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.55f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Rounded.OpenInNew,
                            contentDescription = "Open Link",
                            tint = Color(0xFF00F2FE),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Tech Architecture Specs Matrix
            AnimatedVisibility(
                visible = showTechStack,
                enter = fadeIn(tween(550)) + slideInVertically(tween(550)) { it / 2 }
            ) {
                Column {
                    Text(
                        text = "CORE SYSTEM SPECIFICATIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TechSpecChip(
                            icon = Icons.Rounded.Layers,
                            title = "AGSL SHADER 3.0",
                            desc = "Real-time backdrop refraction",
                            accent = accent,
                            state = state,
                            modifier = Modifier.weight(1f)
                        )
                        TechSpecChip(
                            icon = Icons.Rounded.Speed,
                            title = "EXOPLAYER GPU",
                            desc = "Zero latency hardware decode",
                            accent = Color(0xFF00FF9D),
                            state = state,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TechSpecChip(
                            icon = Icons.Rounded.Security,
                            title = "ANTI-POP TRAP",
                            desc = "Automated ad bypass pipeline",
                            accent = Color(0xFF00F2FE),
                            state = state,
                            modifier = Modifier.weight(1f)
                        )
                        TechSpecChip(
                            icon = Icons.Rounded.Hub,
                            title = "KOTLIN 2.0 COMPOSE",
                            desc = "Pure declarative 60FPS UI",
                            accent = secondaryAccent,
                            state = state,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Build Info Footer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x10FFFFFF))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LIQUIDSTREAM RUNTIME // ENCRYPTED BUILD 2026.09 // BY MOKKA CODING",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color.White.copy(alpha = 0.45f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Animated GIF Banner (Playing LMEB.gif at the end of About)
                    LiquidGlassGifBanner(
                        state = state,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun LiquidGlassGifBanner(
    state: LiquidGlassState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    GlassCard(
        state = state,
        shape = RoundedCornerShape(20.dp),
        tintOverride = Color(0xCC0E0A1E),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(R.drawable.lmeb)
                    .crossfade(true)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = "LiquidStream Banner Animation",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
            )
        }
    }
}

@Composable
private fun TechSpecChip(
    icon: ImageVector,
    title: String,
    desc: String,
    accent: Color,
    state: LiquidGlassState,
    modifier: Modifier = Modifier
) {
    GlassCard(
        state = state,
        shape = RoundedCornerShape(14.dp),
        tintOverride = Color(0xCC0E0A1E),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(accent.copy(alpha = 0.15f))
                    .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = desc,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.55f)
            )
        }
    }
}
