package com.imnotndesh.yubalkt.core.network

/**
 * Low-level, engine-agnostic HTTP client.
 *
 * Implementations can use OkHttp, Ktor, java.net.HttpURLConnection, or
 * any other engine. The client handles the raw HTTP mechanics only —
 * JSON parsing and business logic live in [YubalApiService].
 *
 * Both HTTP and HTTPS URLs are supported via the URL string passed in.
 */
interface YubalHttpClient {

    /**
     * Perform a GET request.
     * @param url Fully qualified URL (http:// or https://).
     * @return Raw HTTP response with status code and body.
     */
    suspend fun get(url: String): HttpClientResponse

    /**
     * Perform a POST request with a JSON body.
     * @param url  Fully qualified URL.
     * @param body JSON string to send as the request body.
     * @return Raw HTTP response with status code and body.
     */
    suspend fun post(url: String, body: String): HttpClientResponse
    suspend fun delete(url: String): HttpClientResponse

}

/**
 * Raw HTTP response returned by [YubalHttpClient].
 *
 * @param statusCode    HTTP status code (e.g. 200, 404, 500).
 *                      0 indicates the request never reached the server.
 * @param body          Response body as a raw string, or null if empty.
 * @param isSuccessful  True for 2xx status codes.
 */
data class HttpClientResponse(
    val statusCode: Int,
    val body: String?,
    val isSuccessful: Boolean,
)
