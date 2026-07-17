package com.imnotndesh.yubalkt.core.network

/**
 * Generic sealed class representing the result of an API call.
 *
 * Directly mirrors the TypeScript type from the browser extension:
 * ```
 * type ApiResponse<T> =
 *   | { ok: true; data: T }
 *   | { ok: false; status: number; error: string; message: string };
 * ```
 *
 * @param T The type of the successful response data.
 */
sealed class ApiResponse<out T> {

    /**
     * Successful response from the server.
     * @param data The deserialized response payload.
     */
    data class Success<T>(val data: T) : ApiResponse<T>()

    /**
     * Error response — either a server error (4xx/5xx) or a network failure.
     *
     * @param status  HTTP status code, or **0** if the request never reached the server (network error).
     * @param error   Machine-readable error code (e.g. `"network_error"`, `"invalid_url"`).
     * @param message Human-readable error description.
     */
    data class Error(
        val status: Int,
        val error: String,
        val message: String,
    ) : ApiResponse<Nothing>()
}
