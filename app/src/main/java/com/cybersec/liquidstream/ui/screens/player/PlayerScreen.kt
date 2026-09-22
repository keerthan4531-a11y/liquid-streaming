package com.cybersec.liquidstream.ui.screens.player

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.VolumeMute
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.ui.PlayerView
import com.cybersec.liquidstream.PipManager
import com.cybersec.liquidstream.core.glass.LiquidGlassColors
import com.cybersec.liquidstream.core.glass.LiquidGlassState
import com.cybersec.liquidstream.data.model.StreamSource
import com.cybersec.liquidstream.ui.components.GlassCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun PlayerScreen(
    streamSource: StreamSource,
    viewModel: PlayerViewModel,
    state: LiquidGlassState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsState()
    val isInPip by PipManager.isInPipMode
    val coroutineScope = rememberCoroutineScope()

    var isFullscreen by remember { mutableStateOf(false) }

    // Audio Manager setup
    val audioManager = remember {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    val maxVolume = remember {
        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
    }

    // Gesture HUD overlay states
    var brightnessLevel by remember {
        val cur = activity?.window?.attributes?.screenBrightness ?: 0.5f
        mutableFloatStateOf(if (cur < 0f) 0.5f else cur)
    }
    var showBrightnessHud by remember { mutableStateOf(false) }

    var currentVolume by remember {
        mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
    }
    var showVolumeHud by remember { mutableStateOf(false) }

    // Double Tap Seek Ripple states
    var showLeftSeekRipple by remember { mutableStateOf(false) }
    var showRightSeekRipple by remember { mutableStateOf(false) }

    // Screen Lock temporary unlock button
    var showUnlockPrompt by remember { mutableStateOf(false) }

    // Double Tap Timing Tracker
    var lastTapTime by remember { mutableLongStateOf(0L) }
    var lastTapX by remember { mutableFloatStateOf(0f) }

    fun toggleFullscreen() {
        val act = activity ?: return
        val newFs = !isFullscreen
        isFullscreen = newFs
        val window = act.window
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (newFs) {
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    LaunchedEffect(streamSource.directStreamUrl) {
        viewModel.initializePlayer(context, streamSource)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.releasePlayer()
            val act = context as? Activity
            if (act != null) {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                val insetsController = WindowCompat.getInsetsController(act.window, act.window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // --- PIP MODE: Render clean video frame only ---
    if (isInPip) {
        val player = viewModel.getPlayer()
        if (player != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = false
                        resizeMode = uiState.aspectRatioMode.resizeMode
                    }
                },
                update = { pv ->
                    pv.player = viewModel.getPlayer()
                    pv.resizeMode = uiState.aspectRatioMode.resizeMode
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(uiState.isScreenLocked) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val isLeftHalf = down.position.x < (size.width / 2f)
                    val startBrightness = brightnessLevel
                    val startVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    var isDrag = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val dragEvent = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!dragEvent.pressed) {
                            break
                        }
                        val deltaY = dragEvent.position.y - down.position.y
                        if (!isDrag && abs(deltaY) > 25f) {
                            isDrag = true
                        }
                        if (isDrag && !uiState.isScreenLocked) {
                            dragEvent.consume()
                            // Swipe up increases (+), swipe down decreases (-)
                            val fraction = -(deltaY) / (size.height * 0.65f)

                            if (isLeftHalf) {
                                // Brightness control
                                val targetB = (startBrightness + fraction).coerceIn(0.01f, 1.0f)
                                brightnessLevel = targetB
                                showBrightnessHud = true
                                activity?.let { act ->
                                    val lp = act.window.attributes
                                    lp.screenBrightness = targetB
                                    act.window.attributes = lp
                                }
                            } else {
                                // Volume control
                                val targetV = (startVolume + (fraction * maxVolume).toInt()).coerceIn(0, maxVolume)
                                currentVolume = targetV
                                showVolumeHud = true
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetV, 0)
                            }
                        }
                    }

                    // Auto-hide HUD sliders shortly after touch release
                    if (isDrag) {
                        coroutineScope.launch {
                            delay(1200)
                            showBrightnessHud = false
                            showVolumeHud = false
                        }
                    } else {
                        // Pointer tapped without dragging: check double tap vs single tap
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime < 350L && abs(down.position.x - lastTapX) < 120f) {
                            // Double Tap detected!
                            lastTapTime = 0L
                            if (!uiState.isScreenLocked) {
                                if (isLeftHalf) {
                                    viewModel.skipBackward()
                                    showLeftSeekRipple = true
                                    coroutineScope.launch {
                                        delay(750)
                                        showLeftSeekRipple = false
                                    }
                                } else {
                                    viewModel.skipForward()
                                    showRightSeekRipple = true
                                    coroutineScope.launch {
                                        delay(750)
                                        showRightSeekRipple = false
                                    }
                                }
                            }
                        } else {
                            // Single tap
                            lastTapTime = now
                            lastTapX = down.position.x
                            if (uiState.isScreenLocked) {
                                showUnlockPrompt = true
                                coroutineScope.launch {
                                    delay(3000)
                                    showUnlockPrompt = false
                                }
                            } else {
                                viewModel.toggleControls()
                            }
                        }
                    }
                }
            }
    ) {
        // Error State
        if (uiState.hasError) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = state.themePreset.primaryAccent,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Playback Failed",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.errorMessage,
                    color = LiquidGlassColors.TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                GlassCard(state = state, shape = RoundedCornerShape(12.dp)) {
                    Text(
                        text = "Close Player",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onClose() }
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }
            return@Box
        }

        // Video Surface
        val player = viewModel.getPlayer()
        if (player != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = false
                        resizeMode = uiState.aspectRatioMode.resizeMode
                    }
                },
                update = { pv ->
                    pv.player = viewModel.getPlayer()
                    pv.resizeMode = uiState.aspectRatioMode.resizeMode
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Buffering Indicator
        if (uiState.isBuffering) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = state.themePreset.primaryAccent,
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Buffering...", color = LiquidGlassColors.TextSecondary, fontSize = 13.sp)
                }
            }
        }

        // --- DOUBLE TAP SEEK RIPPLE FEEDBACK (LEFT -10s) ---
        AnimatedVisibility(
            visible = showLeftSeekRipple,
            enter = fadeIn(tween(150)) + scaleIn(tween(150)),
            exit = fadeOut(tween(300)) + scaleOut(tween(300)),
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(95.dp)
                    .clip(CircleShape)
                    .background(Color(0x77000000))
                    .border(1.5.dp, state.themePreset.primaryAccent.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Replay10,
                        contentDescription = "Rewind 10s",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "-10s",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // --- DOUBLE TAP SEEK RIPPLE FEEDBACK (RIGHT +10s) ---
        AnimatedVisibility(
            visible = showRightSeekRipple,
            enter = fadeIn(tween(150)) + scaleIn(tween(150)),
            exit = fadeOut(tween(300)) + scaleOut(tween(300)),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(95.dp)
                    .clip(CircleShape)
                    .background(Color(0x77000000))
                    .border(1.5.dp, state.themePreset.primaryAccent.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Forward10,
                        contentDescription = "Forward 10s",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "+10s",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // --- SWIPE GESTURE BRIGHTNESS HUD (LEFT) ---
        AnimatedVisibility(
            visible = showBrightnessHud,
            enter = fadeIn(tween(100)),
            exit = fadeOut(tween(300)),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
        ) {
            GlassCard(
                state = state,
                shape = RoundedCornerShape(24.dp),
                tintOverride = Color(0xCC100D22)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (brightnessLevel > 0.5f) Icons.Rounded.BrightnessHigh else Icons.Rounded.BrightnessMedium,
                        contentDescription = "Brightness",
                        tint = state.themePreset.primaryAccent,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(100.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(fraction = brightnessLevel)
                                .align(Alignment.BottomCenter)
                                .background(state.themePreset.primaryAccent)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "${(brightnessLevel * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- SWIPE GESTURE VOLUME HUD (RIGHT) ---
        AnimatedVisibility(
            visible = showVolumeHud,
            enter = fadeIn(tween(100)),
            exit = fadeOut(tween(300)),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
        ) {
            GlassCard(
                state = state,
                shape = RoundedCornerShape(24.dp),
                tintOverride = Color(0xCC100D22)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (currentVolume == 0) Icons.Rounded.VolumeMute else Icons.Rounded.VolumeUp,
                        contentDescription = "Volume",
                        tint = state.themePreset.primaryAccent,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(100.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(fraction = (currentVolume.toFloat() / maxVolume.toFloat()).coerceIn(0f, 1f))
                                .align(Alignment.BottomCenter)
                                .background(state.themePreset.primaryAccent)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "${((currentVolume.toFloat() / maxVolume.toFloat()) * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- QUICK FLOATING TOAST BADGE (Aspect Ratio / Lock status feedback) ---
        AnimatedVisibility(
            visible = uiState.quickToastText != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = if (isFullscreen) 24.dp else 56.dp)
        ) {
            GlassCard(
                state = state,
                shape = RoundedCornerShape(16.dp),
                tintOverride = Color(0xEE120E22),
                elevated = true
            ) {
                Text(
                    text = uiState.quickToastText ?: "",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // --- SCREEN LOCKED: TEMPORARY UNLOCK PROMPT ---
        AnimatedVisibility(
            visible = uiState.isScreenLocked && showUnlockPrompt,
            enter = fadeIn(tween(200)) + scaleIn(tween(200)),
            exit = fadeOut(tween(250)) + scaleOut(tween(250)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            GlassCard(
                state = state,
                shape = RoundedCornerShape(20.dp),
                tintOverride = state.themePreset.primaryAccent.copy(alpha = 0.35f),
                elevated = true
            ) {
                Row(
                    modifier = Modifier
                        .clickable {
                            viewModel.setScreenLocked(false)
                            showUnlockPrompt = false
                        }
                        .padding(horizontal = 22.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LockOpen,
                        contentDescription = "Unlock",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "TAP TO UNLOCK SCREEN",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // --- CONTROLS HUD ---
        AnimatedVisibility(
            visible = uiState.areControlsVisible && !uiState.hasError && !uiState.isScreenLocked,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x55000000))
            ) {
                // Top Bar
                GlassCard(
                    state = state,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (isFullscreen) 16.dp else 44.dp, start = 16.dp, end = 16.dp)
                        .height(52.dp)
                        .align(Alignment.TopCenter)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.Close, "Close", tint = Color.White)
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = streamSource.title.take(24),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Aspect Ratio Mode Button
                            GlassCard(
                                state = state,
                                shape = RoundedCornerShape(8.dp),
                                tintOverride = Color(0x33FFFFFF)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clickable { viewModel.cycleAspectRatio() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AspectRatio,
                                        contentDescription = "Aspect Ratio",
                                        tint = state.themePreset.primaryAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = uiState.aspectRatioMode.label.take(4),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Picture-in-Picture (PiP) Button
                            IconButton(
                                onClick = {
                                    activity?.let { PipManager.enterPip(it) }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PictureInPictureAlt,
                                    contentDescription = "Picture-in-Picture",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Touch Lock Button
                            IconButton(
                                onClick = { viewModel.setScreenLocked(true) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = "Lock Screen",
                                    tint = state.themePreset.primaryAccent,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // Resolution Badge
                            GlassCard(state = state, shape = RoundedCornerShape(8.dp), elevated = true) {
                                val cleanRes = Regex("""\b(\d{3,4}x\d{3,4}|\d{3,4}p)\b""", RegexOption.IGNORE_CASE)
                                    .find(streamSource.videoResolution)?.value ?: streamSource.videoResolution.take(10).ifEmpty { "1080p HD" }
                                Text(
                                    text = cleanRes,
                                    color = state.themePreset.primaryAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Center Play/Pause & Skip Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassCard(state = state, shape = CircleShape, modifier = Modifier.size(52.dp)) {
                        IconButton(onClick = { viewModel.skipBackward() }, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Rounded.Replay10, "Rewind", tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                    }

                    GlassCard(
                        state = state, shape = CircleShape,
                        tintOverride = state.themePreset.primaryAccent.copy(alpha = 0.85f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        IconButton(onClick = { viewModel.togglePlayPause() }, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    GlassCard(state = state, shape = CircleShape, modifier = Modifier.size(52.dp)) {
                        IconButton(onClick = { viewModel.skipForward() }, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Rounded.Forward10, "Forward", tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                    }
                }

                // Bottom Seek Bar HUD with Fullscreen button directly above timeline
                GlassCard(
                    state = state,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = if (isFullscreen) 12.dp else 24.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        // Header directly above the timeline with Full Screen toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${fmtTime(uiState.currentPositionMs)} / ${fmtTime(uiState.durationMs)}",
                                color = LiquidGlassColors.TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            // Full Screen Option Button directly above the timeline
                            GlassCard(
                                state = state,
                                shape = RoundedCornerShape(10.dp),
                                tintOverride = state.themePreset.primaryAccent.copy(alpha = 0.28f),
                                elevated = true
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clickable { toggleFullscreen() }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFullscreen) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                                        contentDescription = if (isFullscreen) "Exit Full Screen" else "Full Screen",
                                        tint = state.themePreset.primaryAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isFullscreen) "Exit Fullscreen" else "Full Screen",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Timeline Slider
                        Slider(
                            value = uiState.currentPositionMs.toFloat(),
                            onValueChange = { viewModel.seekTo(it.toLong()) },
                            valueRange = 0f..(uiState.durationMs.toFloat().coerceAtLeast(1f)),
                            colors = SliderDefaults.colors(
                                thumbColor = state.themePreset.primaryAccent,
                                activeTrackColor = state.themePreset.primaryAccent,
                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                            )
                        )

                        // Bottom row with Speed and Resolution
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { viewModel.cycleSpeed() }
                                    .padding(vertical = 2.dp)
                            ) {
                                Icon(Icons.Rounded.Speed, null, tint = state.themePreset.primaryAccent, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("${uiState.playbackSpeed}x Speed", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            val cleanRes = Regex("""\b(\d{3,4}x\d{3,4}|\d{3,4}p)\b""", RegexOption.IGNORE_CASE)
                                .find(streamSource.videoResolution)?.value ?: streamSource.videoResolution.take(10).ifEmpty { "1080p HD" }
                            Text(
                                text = cleanRes,
                                color = state.themePreset.primaryAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun fmtTime(ms: Long): String {
    val s = ms / 1000
    val h = s / 3600; val m = (s % 3600) / 60; val sec = s % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, sec) else "%02d:%02d".format(m, sec)
}
