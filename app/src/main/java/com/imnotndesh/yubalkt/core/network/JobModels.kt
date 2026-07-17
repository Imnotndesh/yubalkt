// File: core/network/JobModels.kt
// Package: com.imnotndesh.yubalkt.core.network
package com.imnotndesh.yubalkt.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YubalJob(
    val id: String,
    val url: String,
    val status: JobStatusEnum = JobStatusEnum.PENDING,
    val progress: Double = 0.0,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("content_info") val contentInfo: JobContentInfo? = null,
    val source: String = "manual",
    @SerialName("max_items") val maxItems: Int? = null,
)

@Serializable
enum class JobStatusEnum {
    @SerialName("pending") PENDING,
    @SerialName("fetching_info") FETCHING_INFO,
    @SerialName("downloading") DOWNLOADING,
    @SerialName("importing") IMPORTING,
    @SerialName("completed") COMPLETED,
    @SerialName("failed") FAILED,
    @SerialName("cancelled") CANCELLED;

    val isFinished: Boolean
        get() = this == COMPLETED || this == FAILED || this == CANCELLED

    val isActive: Boolean
        get() = this == PENDING || this == FETCHING_INFO || this == DOWNLOADING || this == IMPORTING

    val displayName: String
        get() = when (this) {
            PENDING -> "Queued"
            FETCHING_INFO -> "Fetching info..."
            DOWNLOADING -> "Downloading"
            IMPORTING -> "Importing"
            COMPLETED -> "Completed"
            FAILED -> "Failed"
            CANCELLED -> "Cancelled"
        }
}

@Serializable
data class JobContentInfo(
    val title: String,
    val artist: String,
    val kind: String,
    val year: Int? = null,
    @SerialName("track_count") val trackCount: Int? = null,
    @SerialName("playlist_id") val playlistId: String = "",
    val url: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("audio_codec") val audioCodec: String? = null,
    @SerialName("audio_bitrate") val audioBitrate: Int? = null,
)

@Serializable
data class JobsResponse(val jobs: List<YubalJob>)

@Serializable
data class ClearJobsResponse(val cleared: Int)
