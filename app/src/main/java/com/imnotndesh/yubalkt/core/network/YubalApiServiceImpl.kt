package com.imnotndesh.yubalkt.core.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class YubalApiServiceImpl(
    private val httpClient: YubalHttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : YubalApiService {

    override suspend fun healthCheck(baseUrl: String): ApiResponse<Unit> {
        val response = httpClient.get("$baseUrl/api/health")
        return if (response.isSuccessful) ApiResponse.Success(Unit)
        else {
            val (errorCode, errorMessage) = parseErrorBody(response.body)
            ApiResponse.Error(status = response.statusCode, error = errorCode, message = errorMessage)
        }
    }

    override suspend fun getContentInfo(baseUrl: String, tabUrl: String): ApiResponse<ContentInfo> {
        val encoded = encodeUrlParam(tabUrl)
        return request(url = "$baseUrl/api/info?url=$encoded")
    }

    override suspend fun createJob(baseUrl: String, tabUrl: String): ApiResponse<Unit> {
        return request(url = "$baseUrl/api/jobs", method = HttpMethod.POST, body = """{"url":"${escapeJson(tabUrl)}"}""")
    }

    override suspend fun createSubscription(baseUrl: String, tabUrl: String): ApiResponse<Unit> {
        return request(url = "$baseUrl/api/subscriptions", method = HttpMethod.POST, body = """{"url":"${escapeJson(tabUrl)}"}""")
    }

    override suspend fun listJobs(baseUrl: String): ApiResponse<JobsResponse> {
        return request(url = "$baseUrl/api/jobs")
    }

    override suspend fun cancelJob(baseUrl: String, jobId: String): ApiResponse<Unit> {
        return request(url = "$baseUrl/api/jobs/$jobId/cancel", method = HttpMethod.POST)
    }

    override suspend fun clearJobs(baseUrl: String): ApiResponse<ClearJobsResponse> {
        return request(url = "$baseUrl/api/jobs", method = HttpMethod.DELETE)
    }

    override suspend fun deleteJob(baseUrl: String, jobId: String): ApiResponse<Unit> {
        return request(url = "$baseUrl/api/jobs/$jobId", method = HttpMethod.DELETE)
    }

    override suspend fun listSubscriptions(baseUrl: String): ApiResponse<SubscriptionListResponse> {
        return request(url = "$baseUrl/api/subscriptions")
    }

    override suspend fun syncAllSubscriptions(baseUrl: String): ApiResponse<SyncResponse> {
        return request(url = "$baseUrl/api/subscriptions/sync", method = HttpMethod.POST)
    }

    override suspend fun syncSubscription(baseUrl: String, subscriptionId: String): ApiResponse<SyncResponse> {
        return request(url = "$baseUrl/api/subscriptions/$subscriptionId/sync", method = HttpMethod.POST)
    }

    override suspend fun deleteSubscription(baseUrl: String, subscriptionId: String): ApiResponse<Unit> {
        return request(url = "$baseUrl/api/subscriptions/$subscriptionId", method = HttpMethod.DELETE)
    }

    override suspend fun getCookiesStatus(baseUrl: String): ApiResponse<CookiesStatusResponse> {
        return request(url = "$baseUrl/api/cookies/status")
    }

    override suspend fun uploadCookies(baseUrl: String, content: String): ApiResponse<CookiesUploadResponse> {
        return request(url = "$baseUrl/api/cookies", method = HttpMethod.POST, body = """{"content":"${escapeJson(content)}"}""")
    }

    override suspend fun deleteCookies(baseUrl: String): ApiResponse<CookiesUploadResponse> {
        return request(url = "$baseUrl/api/cookies", method = HttpMethod.DELETE)
    }

    override suspend fun getLogs(baseUrl: String): ApiResponse<List<LogEntry>> {
        return request(url = "$baseUrl/api/logs")
    }

    private enum class HttpMethod { GET, POST, DELETE }

    private suspend inline fun <reified T> request(url: String, method: HttpMethod = HttpMethod.GET, body: String? = null): ApiResponse<T> {
        val response = when (method) {
            HttpMethod.GET -> httpClient.get(url)
            HttpMethod.POST -> httpClient.post(url, body ?: "")
            HttpMethod.DELETE -> httpClient.delete(url)
        }
        return if (response.isSuccessful) {
            if (response.body.isNullOrEmpty() || response.body == "null") {
                @Suppress("UNCHECKED_CAST") (ApiResponse.Success(Unit) as ApiResponse<T>)
            } else {
                try { val data = json.decodeFromString<T>(response.body); ApiResponse.Success(data) }
                catch (_: Exception) {
                    try { @Suppress("UNCHECKED_CAST") (json.decodeFromString<Unit>(response.body) as ApiResponse<T>) }
                    catch (_: Exception) { ApiResponse.Error(status = response.statusCode, error = "parse_error", message = "Failed to parse server response") }
                }
            }
        } else {
            val message = if (response.statusCode == 0) "Could not connect to server — check the URL and ensure the server is running"
            else { val (_, errorMessage) = parseErrorBody(response.body); errorMessage }
            ApiResponse.Error(status = response.statusCode, error = if (response.statusCode == 0) "network_error" else "server_error", message = message)
        }
    }

    private fun parseErrorBody(body: String?): Pair<String, String> {
        if (body == null) return Pair("unknown_error", "Unknown error")
        return try {
            val obj = json.parseToJsonElement(body).jsonObject
            val error = obj["error"]?.jsonPrimitive?.content ?: "unknown_error"
            val message = (obj["message"]?.jsonPrimitive?.content ?: obj["detail"]?.jsonPrimitive?.content ?: "Unknown error")
            Pair(error, message)
        } catch (_: Exception) { Pair("unknown_error", body.take(200)) }
    }

    private fun encodeUrlParam(value: String): String = java.net.URLEncoder.encode(value, "UTF-8")

    private fun escapeJson(value: String): String = value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
}
