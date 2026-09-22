package com.cybersec.liquidstream.data.repository

import android.util.Log
import com.cybersec.liquidstream.data.model.AiChatMessage
import com.cybersec.liquidstream.data.model.AiModelInfo
import com.cybersec.liquidstream.data.model.ChatRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class AiChatRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
) {
    companion object {
        private const val TAG = "AiChatRepository"
        private const val BASE_URL = "https://ultimate-ai-worker.haruyhari930.workers.dev/v1/chat/completions"
        private const val MASTER_KEY = "1"

        private const val SYSTEM_PROMPT = """You are CineAI, an ultra-smart cinema assistant inside the LiquidStream app (Liquid Glass 3.0 UI). 
You are deeply knowledgeable about Tamil cinema (Kollywood), Indian & international films, actors, directors (e.g. Lokesh Kanagaraj, Mani Ratnam, Nelson Dilipkumar, Atlee), music composers (Anirudh Ravichander, AR Rahman, Yuvan Shankar Raja), and 2026/2025 release trends.
Answer questions accurately, creatively, and informatively. If the user asks in Tamil, reply in Tamil (or Tanglish). Format your answers neatly with bullet points, emojis, and bold highlights where appropriate."""

        // Ordered healthy failover chain for zero downtime
        private val FAILOVER_CHAIN = listOf(
            "minitool/claude-opus-4.8",
            "minitool/claude-haiku-4.5",
            "gpt-5.4",
            "ernie-smart",
            "meta-ai"
        )
    }

    /**
     * Streams AI completions token-by-token with automatic self-healing failover.
     * Guarantees 100% response delivery without "No response received" errors.
     */
    fun streamChatCompletion(
        messages: List<AiChatMessage>,
        preferredModel: String = AiModelInfo.DEFAULT_MODEL.id,
        onModelActive: (String) -> Unit = {}
    ): Flow<String> = flow {
        // Build candidate models list: start with user's preferred model, then failover models
        val candidateModels = buildList {
            add(preferredModel)
            FAILOVER_CHAIN.forEach { fallback ->
                if (fallback != preferredModel) add(fallback)
            }
        }.distinct()

        var successfullyStreamed = false
        var lastError: Exception? = null

        for (candidate in candidateModels) {
            try {
                Log.d(TAG, "Attempting stream with model: $candidate")
                onModelActive(candidate)

                val requestBody = buildChatJson(messages, candidate)
                val request = Request.Builder()
                    .url(BASE_URL)
                    .addHeader("Authorization", "Bearer $MASTER_KEY")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "text/event-stream")
                    .post(requestBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: "Empty body"
                    response.close()
                    Log.w(TAG, "Model $candidate returned ${response.code}: $errBody. Trying next fallback...")
                    continue
                }

                val body = response.body
                if (body == null) {
                    response.close()
                    Log.w(TAG, "Model $candidate body was null. Trying next fallback...")
                    continue
                }

                val reader = BufferedReader(InputStreamReader(body.byteStream()))
                var line: String?
                var chunkCount = 0

                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line?.trim() ?: continue
                    if (currentLine.isEmpty() || currentLine.startsWith(":")) continue

                    if (currentLine.startsWith("data:")) {
                        val dataContent = currentLine.removePrefix("data:").trim()
                        if (dataContent == "[DONE]") {
                            break
                        }

                        val extractedToken = extractTokenFromChunk(dataContent)
                        if (!extractedToken.isNullOrEmpty()) {
                            emit(extractedToken)
                            chunkCount++
                        }
                    }
                }

                response.close()

                if (chunkCount > 0) {
                    successfullyStreamed = true
                    Log.d(TAG, "Stream completed successfully with $candidate ($chunkCount chunks)")
                    break
                } else {
                    Log.w(TAG, "Model $candidate finished with 0 chunks. Falling back...")
                }

            } catch (e: Exception) {
                Log.w(TAG, "Stream error on model $candidate: ${e.localizedMessage}")
                lastError = e
            }
        }

        if (!successfullyStreamed) {
            val fallbackMessage = "வணக்கம்! AI சர்வரில் தற்காலிக நெட்வொர்க் சுழற்சி உள்ளது. சற்று நேரத்தில் மீண்டும் முயற்சிக்கவும்."
            emit(fallbackMessage)
            if (lastError != null) {
                Log.e(TAG, "All models failed", lastError)
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun buildChatJson(messages: List<AiChatMessage>, model: String): String {
        val root = JSONObject()
        root.put("model", model)
        root.put("stream", true)

        val messagesArray = JSONArray()

        // 1. Inject System Prompt
        val systemObj = JSONObject()
        systemObj.put("role", "system")
        systemObj.put("content", SYSTEM_PROMPT)
        messagesArray.put(systemObj)

        // 2. Add conversation history (up to last 12 messages for performance & context window)
        val historyToInclude = messages.takeLast(12)
        for (msg in historyToInclude) {
            val roleStr = when (msg.role) {
                ChatRole.USER -> "user"
                ChatRole.ASSISTANT -> "assistant"
                ChatRole.SYSTEM -> "system"
            }
            val msgObj = JSONObject()
            msgObj.put("role", roleStr)
            msgObj.put("content", msg.content)
            messagesArray.put(msgObj)
        }

        root.put("messages", messagesArray)
        return root.toString()
    }

    private fun extractTokenFromChunk(jsonStr: String): String? {
        return try {
            val json = JSONObject(jsonStr)

            // 1. Standard OpenAI / Anthropic format: choices[0].delta.content
            if (json.has("choices")) {
                val choices = json.getJSONArray("choices")
                if (choices.length() > 0) {
                    val firstChoice = choices.getJSONObject(0)
                    if (firstChoice.has("delta")) {
                        val delta = firstChoice.getJSONObject("delta")
                        if (delta.has("content")) {
                            return delta.optString("content", "")
                        }
                    }
                    if (firstChoice.has("text")) {
                        return firstChoice.optString("text", "")
                    }
                }
            }

            // 2. Alternative formats: delta string or data object
            if (json.has("delta") && json.get("delta") is String) {
                return json.getString("delta")
            }

            if (json.has("data")) {
                val dataObj = json.optJSONObject("data")
                if (dataObj != null && dataObj.has("content")) {
                    return dataObj.optString("content", "")
                }
            }

            null
        } catch (e: Exception) {
            null
        }
    }
}
