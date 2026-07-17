package com.imnotndesh.yubalkt.core.network

interface YubalApiService {

    suspend fun healthCheck(baseUrl: String): ApiResponse<Unit>

    suspend fun getContentInfo(baseUrl: String, tabUrl: String): ApiResponse<ContentInfo>

    suspend fun createJob(baseUrl: String, tabUrl: String): ApiResponse<Unit>

    suspend fun createSubscription(baseUrl: String, tabUrl: String): ApiResponse<Unit>

    suspend fun listJobs(baseUrl: String): ApiResponse<JobsResponse>

    suspend fun cancelJob(baseUrl: String, jobId: String): ApiResponse<Unit>

    suspend fun clearJobs(baseUrl: String): ApiResponse<ClearJobsResponse>

    suspend fun deleteJob(baseUrl: String, jobId: String): ApiResponse<Unit>

    suspend fun listSubscriptions(baseUrl: String): ApiResponse<SubscriptionListResponse>

    suspend fun syncAllSubscriptions(baseUrl: String): ApiResponse<SyncResponse>

    suspend fun syncSubscription(baseUrl: String, subscriptionId: String): ApiResponse<SyncResponse>

    suspend fun deleteSubscription(baseUrl: String, subscriptionId: String): ApiResponse<Unit>

    suspend fun getCookiesStatus(baseUrl: String): ApiResponse<CookiesStatusResponse>

    suspend fun uploadCookies(baseUrl: String, content: String): ApiResponse<CookiesUploadResponse>

    suspend fun deleteCookies(baseUrl: String): ApiResponse<CookiesUploadResponse>

    suspend fun getLogs(baseUrl: String): ApiResponse<List<LogEntry>>
}
