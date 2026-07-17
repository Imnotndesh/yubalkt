package com.imnotndesh.yubalkt.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogEntry(
    @SerialName("entry_type") val entryType: String = "default",
    val message: String,
    val timestamp: String? = null,
    @SerialName("job_id") val jobId: String? = null,
    val phase: String? = null,
    val stats: LogStats? = null,
)

@Serializable
data class LogStats(
    @SerialName("stats_type") val statsType: String,
    val success: Int = 0,
    val cached: Int = 0,
    val failed: Int = 0,
    @SerialName("skipped_by_reason") val skippedByReason: Map<String, Int> = emptyMap(),
)
