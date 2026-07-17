package com.imnotndesh.yubalkt.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionResponse(
    val id: String,
    val type: String,
    val url: String,
    val name: String,
    val enabled: Boolean,
    @SerialName("max_items") val maxItems: Int? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("last_synced_at") val lastSyncedAt: String? = null,
)

@Serializable
data class SubscriptionListResponse(
    val items: List<SubscriptionResponse>,
)

@Serializable
data class SyncResponse(
    @SerialName("job_ids") val jobIds: List<String>,
)
