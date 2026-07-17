package com.imnotndesh.yubalkt.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.yubalkt.core.network.ApiResponse
import com.imnotndesh.yubalkt.core.network.LogEntry
import com.imnotndesh.yubalkt.core.network.YubalApiService
import com.imnotndesh.yubalkt.core.network.YubalJob
import com.imnotndesh.yubalkt.core.notification.JobNotificationManager
import com.imnotndesh.yubalkt.core.storage.YubalStorage
import com.imnotndesh.yubalkt.ui.utils.DataCache
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class HomeUiState(
    val isLoading: Boolean = true,
    val isConfigured: Boolean = false,
    val instanceUrl: String = "",
    val jobs: List<YubalJob> = emptyList(),
    val errorMessage: String? = null,
    val isClearingJobs: Boolean = false,
    val selectedJobLogs: List<LogEntry>? = null,
    val logsLoading: Boolean = false,
)

class HomeViewModel(
    private val storage: YubalStorage,
    private val apiService: YubalApiService,
    private val notificationManager: JobNotificationManager? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var pollingJob: Job? = null

    init { loadInstanceUrl() }

    private fun loadInstanceUrl() {
        viewModelScope.launch {
            val url = storage.getInstanceUrl()
            if (url != null) {
                _uiState.value = _uiState.value.copy(isConfigured = true, instanceUrl = url)
                startPolling(url)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, isConfigured = false)
            }
        }
    }

    private fun startPolling(baseUrl: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) { fetchJobs(baseUrl); delay(2000.milliseconds) }
        }
    }

    private suspend fun fetchJobs(baseUrl: String) {
        when (val result = apiService.listJobs(baseUrl)) {
            is ApiResponse.Success -> {
                DataCache.jobs = result.data.jobs
                DataCache.jobsLoaded = true
                _uiState.value = _uiState.value.copy(isLoading = false, jobs = result.data.jobs, errorMessage = null)
                val activeJobs = result.data.jobs.filter { it.status.isActive }
                notificationManager?.showJobNotifications(activeJobs, baseUrl)
                if (activeJobs.isEmpty()) notificationManager?.cancelAll()
            }
            is ApiResponse.Error -> {
                _uiState.value = _uiState.value.copy(isLoading = !DataCache.jobsLoaded, jobs = DataCache.jobs, errorMessage = if (DataCache.jobsLoaded && _uiState.value.jobs.isNotEmpty()) result.message else null)
            }
        }
    }

    fun onCancelJob(jobId: String) { val url = _uiState.value.instanceUrl; if (url.isEmpty()) return; viewModelScope.launch { apiService.cancelJob(url, jobId) } }

    fun onDeleteJob(jobId: String) { val url = _uiState.value.instanceUrl; if (url.isEmpty()) return; viewModelScope.launch { apiService.deleteJob(url, jobId) } }

    fun onClearFinished() {
        val url = _uiState.value.instanceUrl; if (url.isEmpty()) return
        viewModelScope.launch { _uiState.value = _uiState.value.copy(isClearingJobs = true); apiService.clearJobs(url); _uiState.value = _uiState.value.copy(isClearingJobs = false) }
    }

    fun onViewJobLogs(jobId: String) {
        val url = _uiState.value.instanceUrl; if (url.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(logsLoading = true, selectedJobLogs = null)
            when (val result = apiService.getLogs(url)) {
                is ApiResponse.Success -> { val jobLogs = result.data.filter { it.jobId == jobId }; _uiState.value = _uiState.value.copy(selectedJobLogs = jobLogs, logsLoading = false) }
                is ApiResponse.Error -> _uiState.value = _uiState.value.copy(selectedJobLogs = emptyList(), logsLoading = false)
            }
        }
    }

    fun onDismissLogs() { _uiState.value = _uiState.value.copy(selectedJobLogs = null) }

    fun dismissError() { _uiState.value = _uiState.value.copy(errorMessage = null) }

    fun refresh() { val url = _uiState.value.instanceUrl; if (url.isNotEmpty()) viewModelScope.launch { fetchJobs(url) } }

    override fun onCleared() { super.onCleared(); pollingJob?.cancel(); notificationManager?.cancelAll() }

    class Factory(private val storage: YubalStorage, private val apiService: YubalApiService, private val notificationManager: JobNotificationManager? = null) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(storage, apiService, notificationManager) as T
    }
}
