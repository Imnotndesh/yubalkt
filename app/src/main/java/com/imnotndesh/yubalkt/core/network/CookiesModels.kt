package com.imnotndesh.yubalkt.core.network

import kotlinx.serialization.Serializable

@Serializable
data class CookiesStatusResponse(
    val configured: Boolean,
)

@Serializable
data class CookiesUploadResponse(
    val status: String,
)
