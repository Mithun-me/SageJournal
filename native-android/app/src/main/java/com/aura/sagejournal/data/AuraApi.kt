package com.aura.sagejournal.data

import com.aura.sagejournal.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Serializable
data class ReflectionResult(
    val reflection: String? = null,
    val themes: List<String> = emptyList(),
    @SerialName("suggestedAffirmation") val affirmation: String? = null,
)

@Serializable
data class PromptResult(val prompt: String? = null, val category: String? = null)

@Serializable
data class AffirmationResult(
    val quote: String? = null,
    val author: String? = null,
    val source: String? = null,
    val reflection: String? = null,
    val theme: String? = null,
)

@Serializable
data class InsightResult(val insight: String? = null, val tip: String? = null)

/**
 * Client for the Express endpoints already in server.ts. Every call returns
 * null on any failure rather than throwing: the server is optional to the app,
 * and a journal must keep working on a train.
 *
 * Nothing here is a source of truth. A reflection that never arrives leaves
 * the entry without one, which the detail screen already handles.
 */
class AuraApi(private val baseUrl: String = BuildConfig.AURA_API_BASE) {

    private val client = OkHttpClient.Builder()
        // Short: a hung request must not leave a spinner up on a journal.
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    /** True when a build actually has a backend configured. */
    val configured: Boolean get() = baseUrl.isNotBlank()

    private suspend inline fun <reified T> post(path: String, body: String): T? =
        withContext(Dispatchers.IO) {
            // A release built without -PauraApiBase has no backend at all;
            // skip the work rather than constructing an invalid request.
            if (baseUrl.isBlank()) return@withContext null
            runCatching {
                val request = Request.Builder()
                    .url(baseUrl.trimEnd('/') + path)
                    .post(body.toRequestBody(jsonType))
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    response.body?.string()?.let { json.decodeFromString<T>(it) }
                }
            }.getOrNull()
        }

    suspend fun reflect(title: String, content: String, mood: String): ReflectionResult? =
        post(
            "/api/gemini/reflect",
            json.encodeToString(ReflectRequest.serializer(), ReflectRequest(title, content, mood)),
        )

    suspend fun dailyPrompt(mood: String): PromptResult? =
        post("/api/gemini/prompt", json.encodeToString(PromptRequest.serializer(), PromptRequest(currentMood = mood)))

    suspend fun dailyAffirmation(topic: String): AffirmationResult? =
        post("/api/gemini/daily-affirmation", json.encodeToString(TopicRequest.serializer(), TopicRequest(topic)))

    suspend fun insights(streak: Int, entries: Int, mood: String): InsightResult? =
        post("/api/gemini/insights", json.encodeToString(InsightRequest.serializer(), InsightRequest(streak, entries, mood)))

    suspend fun reachable(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(baseUrl.trimEnd('/') + "/api/health").build()
            client.newCall(request).execute().use { it.isSuccessful }
        }.getOrDefault(false)
    }
}

@Serializable private data class ReflectRequest(
    val title: String, val content: String, val mood: String,
)
@Serializable private data class PromptRequest(val currentMood: String)
@Serializable private data class TopicRequest(val topic: String)
@Serializable private data class InsightRequest(
    val streak: Int, val entriesCount: Int, val dominantMood: String,
)
