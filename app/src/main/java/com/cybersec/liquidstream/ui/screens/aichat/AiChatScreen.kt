package com.cybersec.liquidstream.ui.screens.aichat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybersec.liquidstream.core.glass.LiquidGlassColors
import com.cybersec.liquidstream.core.glass.LiquidGlassState
import com.cybersec.liquidstream.core.glass.liquidGlassNavigation
import com.cybersec.liquidstream.core.glass.physics.liquidGelPress
import com.cybersec.liquidstream.core.glass.prism.PrismDispersionBrush
import com.cybersec.liquidstream.data.model.AiChatMessage
import com.cybersec.liquidstream.data.model.AiModelInfo
import com.cybersec.liquidstream.data.model.ChatRole
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel,
    state: LiquidGlassState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showModelSheet by remember { mutableStateOf(false) }

    // Auto scroll to bottom when messages update or stream tokens arrive
    LaunchedEffect(uiState.messages.size, uiState.messages.lastOrNull()?.content?.length) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val isKeyboardOpen = imeBottom > 0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidGlassColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .then(
                    if (isKeyboardOpen) Modifier else Modifier.navigationBarsPadding()
                )
        ) {
            // 1. Liquid Glass Top Bar
            AiChatTopBar(
                selectedModel = uiState.selectedModel,
                activeModelName = uiState.activeStreamingModel,
                isGenerating = uiState.isGenerating,
                state = state,
                onBack = onBack,
                onModelClick = { showModelSheet = true },
                onClearChat = { viewModel.clearChat() }
            )

            // 2. Chat Stream Messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    AiChatMessageItem(
                        message = message,
                        state = state
                    )
                }

                if (uiState.isGenerating && (uiState.messages.lastOrNull()?.content?.isEmpty() == true)) {
                    item {
                        GeneratingShimmerIndicator(
                            modelName = uiState.activeStreamingModel,
                            state = state
                        )
                    }
                }
            }

            // 3. Suggestion Chips
            AnimatedVisibility(
                visible = !uiState.isGenerating,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(viewModel.suggestionChips) { chipText ->
                        SuggestionChip(
                            text = chipText,
                            state = state,
                            onClick = {
                                viewModel.sendMessage(chipText)
                            }
                        )
                    }
                }
            }

            // 4. Liquid Glass Input Dock
            AiChatInputDock(
                inputText = uiState.inputQuery,
                isGenerating = uiState.isGenerating,
                state = state,
                onTextChange = { viewModel.onInputChange(it) },
                onSend = { viewModel.sendMessage() }
            )
        }

        // 5. Model Selection Bottom Sheet
        if (showModelSheet) {
            ModelSelectionBottomSheet(
                selectedModel = uiState.selectedModel,
                models = AiModelInfo.AVAILABLE_MODELS,
                state = state,
                onModelSelect = { model ->
                    viewModel.selectModel(model)
                    showModelSheet = false
                },
                onDismiss = { showModelSheet = false }
            )
        }
    }
}

@Composable
private fun AiChatTopBar(
    selectedModel: AiModelInfo,
    activeModelName: String,
    isGenerating: Boolean,
    state: LiquidGlassState,
    onBack: () -> Unit,
    onModelClick: () -> Unit,
    onClearChat: () -> Unit
) {
    val barShape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassNavigation(state = state, shape = barShape)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .liquidGelPress(onClick = onBack)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // Model Switcher Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E1E28).copy(alpha = 0.85f))
                    .border(
                        width = 1.dp,
                        brush = PrismDispersionBrush.chromaticBorderBrush(
                            intensity = 0.70f,
                            primaryAccent = state.themePreset.primaryAccent
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onModelClick() }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Pulsing green live status dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isGenerating) state.themePreset.primaryAccent else Color(0xFF00E676))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isGenerating) activeModelName else selectedModel.displayName,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isGenerating) "Streaming live..." else "${selectedModel.provider} • Tap to switch",
                            color = Color.White.copy(alpha = 0.50f),
                            fontSize = 9.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Select Model",
                        tint = Color.White.copy(alpha = 0.70f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Clear Chat Button
            IconButton(
                onClick = onClearChat,
                modifier = Modifier
                    .size(40.dp)
                    .liquidGelPress(onClick = onClearChat)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Clear Chat",
                    tint = Color.White.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
private fun AiChatMessageItem(
    message: AiChatMessage,
    state: LiquidGlassState
) {
    val isUser = message.role == ChatRole.USER
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // AI Bot Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                state.themePreset.primaryAccent,
                                state.themePreset.secondaryAccent
                            )
                        )
                    )
                    .shadow(elevation = 6.dp, shape = CircleShape, ambientColor = state.themePreset.primaryAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = "CineAI",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.82f else 0.88f),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Model attribution tag for AI
            if (!isUser && message.modelName != null) {
                Text(
                    text = "🤖 ${message.modelName}",
                    color = state.themePreset.primaryAccent.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            // Message Bubble
            val bubbleShape = if (isUser) {
                RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
            } else {
                RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
            }

            val bubbleBg = if (isUser) {
                Brush.linearGradient(
                    listOf(
                        state.themePreset.primaryAccent.copy(alpha = 0.32f),
                        state.themePreset.primaryAccent.copy(alpha = 0.16f)
                    )
                )
            } else {
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1A1A24).copy(alpha = 0.90f),
                        Color(0xFF12121A).copy(alpha = 0.85f)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .shadow(
                        elevation = if (isUser) 4.dp else 8.dp,
                        shape = bubbleShape,
                        ambientColor = if (isUser) state.themePreset.primaryAccent.copy(alpha = 0.20f) else Color.Black
                    )
                    .clip(bubbleShape)
                    .background(bubbleBg)
                    .border(
                        width = 1.dp,
                        brush = if (isUser) {
                            SolidColor(state.themePreset.primaryAccent.copy(alpha = 0.40f))
                        } else {
                            PrismDispersionBrush.chromaticBorderBrush(
                                intensity = 0.60f,
                                primaryAccent = state.themePreset.primaryAccent
                            )
                        },
                        shape = bubbleShape
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Column {
                    if (isUser) {
                        Text(
                            text = message.content,
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 14.sp,
                            lineHeight = 21.sp
                        )
                    } else {
                        if (message.content.isEmpty() && message.isStreaming) {
                            Text(
                                text = "...",
                                color = Color.White.copy(alpha = 0.50f),
                                fontSize = 14.sp
                            )
                        } else {
                            FormattedAiContent(
                                content = message.content,
                                primaryColor = state.themePreset.primaryAccent
                            )
                        }
                    }

                    // Action row (Copy icon for AI messages)
                    if (!isUser && message.content.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (copied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                                contentDescription = "Copy",
                                tint = if (copied) Color(0xFF00E676) else Color.White.copy(alpha = 0.45f),
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(message.content))
                                        copied = true
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneratingShimmerIndicator(
    modelName: String,
    state: LiquidGlassState
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 46.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            color = state.themePreset.primaryAccent,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$modelName is thinking...",
            color = Color.White.copy(alpha = alpha),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SuggestionChip(
    text: String,
    state: LiquidGlassState,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(Color(0xFF181824).copy(alpha = 0.75f))
            .border(
                width = 1.dp,
                brush = PrismDispersionBrush.chromaticBorderBrush(
                    intensity = 0.50f,
                    primaryAccent = state.themePreset.primaryAccent
                ),
                shape = shape
            )
            .liquidGelPress(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.90f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AiChatInputDock(
    inputText: String,
    isGenerating: Boolean,
    state: LiquidGlassState,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val dockShape = RoundedCornerShape(28.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = dockShape,
                    ambientColor = state.themePreset.primaryAccent.copy(alpha = 0.25f)
                )
                .clip(dockShape)
                .background(Color(0xFF181822).copy(alpha = 0.92f))
                .border(
                    width = 1.dp,
                    brush = PrismDispersionBrush.chromaticBorderBrush(
                        intensity = 0.75f,
                        primaryAccent = state.themePreset.primaryAccent
                    ),
                    shape = dockShape
                )
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = onTextChange,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(state.themePreset.primaryAccent),
                maxLines = 4,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Ask CineAI about Tamil movies, plots, releases...",
                                color = Color.White.copy(alpha = 0.35f),
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button with liquid gel press physics
            val sendEnabled = inputText.isNotBlank() && !isGenerating
            val sendBg = if (sendEnabled) {
                Brush.linearGradient(
                    listOf(
                        state.themePreset.primaryAccent,
                        state.themePreset.secondaryAccent
                    )
                )
            } else {
                SolidColor(Color.White.copy(alpha = 0.12f))
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(sendBg)
                    .liquidGelPress(onClick = { if (sendEnabled) onSend() }),
                contentAlignment = Alignment.Center
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = state.themePreset.primaryAccent,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send",
                        tint = if (sendEnabled) Color.Black else Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelSelectionBottomSheet(
    selectedModel: AiModelInfo,
    models: List<AiModelInfo>,
    state: LiquidGlassState,
    onModelSelect: (AiModelInfo) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF14141E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.SmartToy,
                    contentDescription = null,
                    tint = state.themePreset.primaryAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Select AI Model Engine",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Zero-downtime auto-failover active",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            models.forEach { model ->
                val isSelected = model.id == selectedModel.id
                val itemShape = RoundedCornerShape(14.dp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(itemShape)
                        .background(
                            if (isSelected) {
                                state.themePreset.primaryAccent.copy(alpha = 0.18f)
                            } else {
                                Color(0xFF1D1D28)
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) {
                                state.themePreset.primaryAccent
                            } else {
                                Color.White.copy(alpha = 0.08f)
                            },
                            shape = itemShape
                        )
                        .liquidGelPress(onClick = { onModelSelect(model) })
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = model.displayName,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (model.isRecommended) {
                                                state.themePreset.primaryAccent.copy(alpha = 0.30f)
                                            } else {
                                                Color.White.copy(alpha = 0.12f)
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = model.badge,
                                        color = if (model.isRecommended) state.themePreset.primaryAccent else Color.White.copy(alpha = 0.80f),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = model.description,
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 11.sp
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(state.themePreset.primaryAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// CineAI Rich Markdown Formatted Renderer
// ==========================================

sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class Table(val rows: List<List<String>>, val hasHeader: Boolean) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
}

fun parseInlineMarkdown(text: String, primaryColor: Color): AnnotatedString {
    val clean = text.trim()
    val builder = AnnotatedString.Builder()
    val regex = Regex("""(\*\*([^*]+)\*\*|\*([^*]+)\*|`([^`]+)`)""")
    var lastIndex = 0
    for (match in regex.findAll(clean)) {
        if (match.range.first > lastIndex) {
            builder.append(clean.substring(lastIndex, match.range.first))
        }
        val fullMatch = match.value
        when {
            fullMatch.startsWith("**") && fullMatch.endsWith("**") -> {
                val inner = fullMatch.substring(2, fullMatch.length - 2)
                builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White))
                builder.append(inner)
                builder.pop()
            }
            fullMatch.startsWith("*") && fullMatch.endsWith("*") -> {
                val inner = fullMatch.substring(1, fullMatch.length - 1)
                builder.pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = Color.White.copy(alpha = 0.9f)))
                builder.append(inner)
                builder.pop()
            }
            fullMatch.startsWith("`") && fullMatch.endsWith("`") -> {
                val inner = fullMatch.substring(1, fullMatch.length - 1)
                builder.pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, color = primaryColor))
                builder.append(" $inner ")
                builder.pop()
            }
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < clean.length) {
        builder.append(clean.substring(lastIndex))
    }
    return builder.toAnnotatedString()
}

fun parseMarkdownBlocks(lines: List<String>): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    var i = 0
    while (i < lines.size) {
        val line = lines[i].trim()
        if (line.isEmpty()) {
            i++
            continue
        }

        // 1. Table
        if (line.startsWith("|") && line.indexOf('|', startIndex = 1) != -1) {
            val tableRows = mutableListOf<List<String>>()
            var hasHeader = false
            while (i < lines.size && lines[i].trim().startsWith("|")) {
                val cur = lines[i].trim()
                if (cur.matches(Regex("""^\|[\s\-:|]+\|$"""))) {
                    hasHeader = true
                    i++
                    continue
                }
                val rawSplit = cur.split("|")
                val cells = rawSplit
                    .map { it.trim() }
                    .filterIndexed { idx, _ -> idx > 0 && idx < rawSplit.size - 1 }
                if (cells.isNotEmpty()) {
                    tableRows.add(cells)
                }
                i++
            }
            if (tableRows.isNotEmpty()) {
                blocks.add(MarkdownBlock.Table(tableRows, hasHeader))
            }
            continue
        }

        // 2. Header
        if (line.startsWith("#")) {
            val level = line.takeWhile { it == '#' }.length
            val text = line.dropWhile { it == '#' || it == ' ' }
            blocks.add(MarkdownBlock.Header(level, text))
            i++
            continue
        }

        // 3. Bullet Item
        if (line.startsWith("* ") || line.startsWith("- ") || line.startsWith("• ")) {
            val text = line.drop(2).trim()
            blocks.add(MarkdownBlock.BulletItem(text))
            i++
            continue
        }

        // 4. Numbered Item
        val numMatch = Regex("""^(\d+)\.\s+(.*)$""").find(line)
        if (numMatch != null) {
            val num = numMatch.groupValues[1]
            val text = numMatch.groupValues[2]
            blocks.add(MarkdownBlock.NumberedItem(num, text))
            i++
            continue
        }

        // 5. Paragraph
        blocks.add(MarkdownBlock.Paragraph(line))
        i++
    }
    return blocks
}

@Composable
fun FormattedAiContent(
    content: String,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    if (content.isEmpty()) return

    val lines = content.lines()
    val blocks = remember(content) { parseMarkdownBlocks(lines) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (block in blocks) {
            when (block) {
                is MarkdownBlock.Header -> {
                    Text(
                        text = block.text,
                        fontSize = when (block.level) {
                            1 -> 17.sp
                            2 -> 15.sp
                            else -> 14.sp
                        },
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                is MarkdownBlock.Table -> {
                    AiTableCard(table = block, primaryColor = primaryColor)
                }
                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = primaryColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(block.text, primaryColor),
                            fontSize = 13.5.sp,
                            color = Color.White.copy(alpha = 0.95f),
                            lineHeight = 19.sp
                        )
                    }
                }
                is MarkdownBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}.",
                            color = primaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(block.text, primaryColor),
                            fontSize = 13.5.sp,
                            color = Color.White.copy(alpha = 0.95f),
                            lineHeight = 19.sp
                        )
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = parseInlineMarkdown(block.text, primaryColor),
                        fontSize = 13.5.sp,
                        color = Color.White.copy(alpha = 0.95f),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AiTableCard(
    table: MarkdownBlock.Table,
    primaryColor: Color
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(shape)
            .background(Color(0xFF14141E))
            .border(0.8.dp, primaryColor.copy(alpha = 0.35f), shape)
    ) {
        table.rows.forEachIndexed { rowIndex, rowCells ->
            val isHeader = rowIndex == 0 && table.hasHeader
            val rowBg = when {
                isHeader -> primaryColor.copy(alpha = 0.20f)
                rowIndex % 2 == 1 -> Color(0xFF1A1A26)
                else -> Color.Transparent
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rowBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowCells.forEachIndexed { colIndex, cellText ->
                    Box(
                        modifier = Modifier
                            .weight(if (colIndex == 0) 1.2f else 1f)
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = parseInlineMarkdown(cellText, primaryColor),
                            fontSize = if (isHeader) 12.sp else 11.5.sp,
                            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                            color = if (isHeader) primaryColor else Color.White.copy(alpha = 0.90f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (rowIndex < table.rows.size - 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(Color.White.copy(alpha = 0.08f))
                )
            }
        }
    }
}

