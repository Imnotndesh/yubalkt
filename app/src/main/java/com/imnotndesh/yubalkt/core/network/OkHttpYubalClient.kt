package com.imnotndesh.yubalkt.core.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class OkHttpYubalClient(
    private val connectTimeoutSeconds: Long = 10,
    private val readTimeoutSeconds: Long = 30,
) : YubalHttpClient {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun get(url: String): HttpClientResponse {
        return executeRequest {
            Request.Builder().url(url).get().build()
        }
    }

    override suspend fun post(url: String, body: String): HttpClientResponse {
        return executeRequest {
            Request.Builder()
                .url(url)
                .post(body.toRequestBody(jsonMediaType))
                .build()
        }
    }
    override suspend fun delete(url: String): HttpClientResponse {
        return executeRequest {
            Request.Builder().url(url).delete().build()
        }
    }

    private suspend fun executeRequest(
        block: () -> Request,
    ): HttpClientResponse = withContext(Dispatchers.IO) {
        try {
            val request = block()
            val response = client.newCall(request).execute()
            val body = response.body.string()
            Log.d("OkHttpYubalClient", "→ ${request.method} ${request.url}")
            Log.d("OkHttpYubalClient", "← ${response.code} ${body?.take(200)}")
            HttpClientResponse(
                statusCode = response.code,
                body = body,
                isSuccessful = response.isSuccessful,
            )
        } catch (e: Exception) {
            Log.e("OkHttpYubalClient", "Request failed", e)
            HttpClientResponse(
                statusCode = 0,
                body = null,
                isSuccessful = false,
            )
        }
    }
}
