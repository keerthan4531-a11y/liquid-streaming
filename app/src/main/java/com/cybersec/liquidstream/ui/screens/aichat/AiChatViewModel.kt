package com.cybersec.liquidstream.ui.screens.aichat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cybersec.liquidstream.data.model.AiChatMessage
import com.cybersec.liquidstream.data.model.AiModelInfo
import com.cybersec.liquidstream.data.model.ChatRole
import com.cybersec.liquidstream.data.repository.AiChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AiChatUiState(
    val messages: List<AiChatMessage> = listOf(
        AiChatMessage(
            role = ChatRole.ASSISTANT,
            content = "வணக்கம்! நான் **CineAI** 🎬✨.\n\nஉங்களுக்கு பிடித்த தமிழ் திரைப்படங்கள், 2026 புது ரிலீஸ்கள், பரிந்துரைகள், நடிகர்கள், இயக்குநர்கள் மற்றும் பாடல் தகவல்கள் பற்றி என்னிடம் கேளுங்கள்!",
            modelName = "Claude Opus 4.8 (MiniTool)"
        )
    ),
    val selectedModel: AiModelInfo = AiModelInfo.DEFAULT_MODEL,
    val activeStreamingModel: String = AiModelInfo.DEFAULT_MODEL.displayName,
    val isGenerating: Boolean = false,
    val inputQuery: String = "",
    val error: String? = null
)

class AiChatViewModel(
    private val repository: AiChatRepository = AiChatRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    private var streamingJob: Job? = null

    val suggestionChips = listOf(
        "🍿 2026 Tamil Big Releases",
        "🔥 Top Action Thrillers",
        "🎬 Lokesh Cinematic Universe",
        "🎵 Anirudh Musical Hits",
        "🎭 Kamal Haasan Classics",
        "⚡ Vijay & Ajith Blockbusters"
    )

    fun onInputChange(newText: String) {
        _uiState.update { it.copy(inputQuery = newText) }
    }

    fun selectModel(model: AiModelInfo) {
        _uiState.update {
            it.copy(
                selectedModel = model,
                activeStreamingModel = model.displayName
            )
        }
    }

    fun sendMessage(customPrompt: String? = null) {
        val prompt = (customPrompt ?: _uiState.value.inputQuery).trim()
        if (prompt.isEmpty() || _uiState.value.isGenerating) return

        val userMessage = AiChatMessage(
            role = ChatRole.USER,
            content = prompt
        )

        // Clear input and append user message
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputQuery = "",
                isGenerating = true,
                error = null
            )
        }

        // Prepare placeholder assistant message
        val assistantMessageId = java.util.UUID.randomUUID().toString()
        val initialAssistantMessage = AiChatMessage(
            id = assistantMessageId,
            role = ChatRole.ASSISTANT,
            content = "",
            modelName = _uiState.value.selectedModel.displayName,
            isStreaming = true
        )

        _uiState.update {
            it.copy(messages = it.messages + initialAssistantMessage)
        }

        val allMessages = _uiState.value.messages
        val selectedModelId = _uiState.value.selectedModel.id

        streamingJob?.cancel()
        streamingJob = viewModelScope.launch {
            val contentBuilder = StringBuilder()

            repository.streamChatCompletion(
                messages = allMessages.filter { it.id != assistantMessageId },
                preferredModel = selectedModelId,
                onModelActive = { activeModelId ->
                    val matchedModel = AiModelInfo.AVAILABLE_MODELS.find { it.id == activeModelId }
                    val name = matchedModel?.displayName ?: activeModelId
                    _uiState.update { it.copy(activeStreamingModel = name) }
                }
            ).catch { e ->
                _uiState.update { state ->
                    val updatedMessages = state.messages.map { msg ->
                        if (msg.id == assistantMessageId) {
                            msg.copy(
                                content = contentBuilder.toString().ifEmpty {
                                    "மன்னிக்கவும், தகவல் பெறுவதில் சிறு தடங்கல் ஏற்பட்டுள்ளது. தயவுசெய்து மீண்டும் முயற்சிக்கவும்."
                                },
                                isStreaming = false,
                                isError = contentBuilder.isEmpty()
                            )
                        } else msg
                    }
                    state.copy(
                        messages = updatedMessages,
                        isGenerating = false,
                        error = e.localizedMessage
                    )
                }
            }.collect { token ->
                contentBuilder.append(token)
                val currentText = contentBuilder.toString()

                _uiState.update { state ->
                    val updatedMessages = state.messages.map { msg ->
                        if (msg.id == assistantMessageId) {
                            msg.copy(
                                content = currentText,
                                modelName = state.activeStreamingModel,
                                isStreaming = true
                            )
                        } else msg
                    }
                    state.copy(messages = updatedMessages)
                }
            }

            // Stream finished
            _uiState.update { state ->
                val updatedMessages = state.messages.map { msg ->
                    if (msg.id == assistantMessageId) {
                        msg.copy(
                            isStreaming = false,
                            modelName = state.activeStreamingModel
                        )
                    } else msg
                }
                state.copy(
                    messages = updatedMessages,
                    isGenerating = false
                )
            }
        }
    }

    fun clearChat() {
        streamingJob?.cancel()
        _uiState.update {
            it.copy(
                messages = listOf(
                    AiChatMessage(
                        role = ChatRole.ASSISTANT,
                        content = "உரையாடல் மீட்டமைக்கப்பட்டது ✨. உங்களுக்கு என்ன திரைப்படம் பற்றிய தகவல் தேவை?",
                        modelName = it.selectedModel.displayName
                    )
                ),
                isGenerating = false,
                error = null
            )
        }
    }
}
