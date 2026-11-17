package com.tudominio.asistente.data.remote

import com.tudominio.asistente.core.router.AiDecision
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class CloudLlmClient(private val apiKey: String) {

    private val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateDecision(
        systemPrompt: String,
        compactContext: String,
        userText: String
    ): AiDecision = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext AiDecision(textOutput = "No hay clave API de nube configurada para el Nivel 2.")
        }
        try {
            val fullPrompt = buildString {
                appendLine(systemPrompt)
                if (compactContext.isNotBlank()) {
                    appendLine()
                    appendLine(compactContext)
                }
                appendLine()
                appendLine("Usuario: $userText")
            }
            val requestBodyDto = GeminiRequestDto(
                contents = listOf(ContentDto(parts = listOf(PartDto(text = fullPrompt))))
            )
            val payload = json.encodeToString(requestBodyDto)
            val body = payload.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(endpoint).post(body).build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext AiDecision("Error en API de nube: HTTP ${response.code}")
                }
                val responseString = response.body?.string() ?: return@withContext AiDecision("Respuesta vacía de la nube.")
                val geminiResponse = json.decodeFromString<GeminiResponseDto>(responseString)
                val replyText = geminiResponse.candidates
                    .firstOrNull()?.content?.parts?.firstOrNull()?.text 
                    ?: "Sin respuesta procesable de la nube."
                AiDecision(textOutput = replyText.trim())
            }
        } catch (e: Exception) {
            AiDecision(textOutput = "Fallo de conexión con la nube: ${e.localizedMessage}")
        }
    }
}

@Serializable
private data class GeminiRequestDto(val contents: List<ContentDto>)
@Serializable
private data class ContentDto(val parts: List<PartDto>)
@Serializable
private data class PartDto(val text: String)
@Serializable
private data class GeminiResponseDto(val candidates: List<CandidateDto> = emptyList())
@Serializable
private data class CandidateDto(val content: ContentDto? = null)
