package com.cybersec.liquidstream.data.model

import java.util.UUID

enum class ChatRole {
    USER,
    ASSISTANT,
    SYSTEM
}

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: ChatRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelName: String? = null,
    val isStreaming: Boolean = false,
    val isError: Boolean = false
)

data class AiModelInfo(
    val id: String,
    val displayName: String,
    val provider: String,
    val description: String,
    val badge: String,
    val isRecommended: Boolean = false
) {
    companion object {
        val DEFAULT_MODEL = AiModelInfo(
            id = "minitool/claude-opus-4.8",
            displayName = "Claude Opus 4.8",
            provider = "MiniTool AI",
            description = "High-precision conversational AI with deep cinematic reasoning & Tamil language fluency.",
            badge = "Recommended",
            isRecommended = true
        )

        val AVAILABLE_MODELS = listOf(
            DEFAULT_MODEL,
            AiModelInfo(
                id = "minitool/claude-haiku-4.5",
                displayName = "Claude Haiku 4.5",
                provider = "MiniTool AI",
                description = "Ultra-fast response streaming for quick movie queries and summaries.",
                badge = "Fast Stream"
            ),
            AiModelInfo(
                id = "minitool/gpt-5.6-luna",
                displayName = "GPT-5.6 Luna",
                provider = "MiniTool AI",
                description = "Creative storytelling model with enhanced movie knowledge and plot analysis.",
                badge = "MiniTool SOTA"
            ),
            AiModelInfo(
                id = "gpt-5.4",
                displayName = "GPT-5.4 Turbo",
                provider = "SurfSense",
                description = "General intelligence model with high speed and broad cultural knowledge.",
                badge = "High Speed"
            ),
            AiModelInfo(
                id = "ernie-smart",
                displayName = "ERNIE Smart Mode",
                provider = "Baidu Core",
                description = "High-stability enterprise model with zero downtime under massive traffic.",
                badge = "Stable"
            ),
            AiModelInfo(
                id = "meta-ai",
                displayName = "Meta AI",
                provider = "Meta AI Engine",
                description = "Conversational assistant with strong pop-culture and entertainment grounding.",
                badge = "Smart"
            )
        )
    }
}
