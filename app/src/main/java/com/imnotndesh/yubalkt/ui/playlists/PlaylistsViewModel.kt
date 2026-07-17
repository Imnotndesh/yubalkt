package com.imnotndesh.yubalkt.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.yubalkt.core.network.ApiResponse
import com.imnotndesh.yubalkt.core.network.SubscriptionResponse
import com.imnotndesh.yubalkt.core.network.YubalApiService
import com.imnotndesh.yubalkt.core.storage.YubalStorage
import com.imnotndesh.yubalkt.ui.utils.DataCache
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlaylistsUiState(
    val isLoading: Boolean = true,
    val subscriptions: List<SubscriptionResponse> = emptyList(),
    val isSyncingAll: Boolean = false,
    val instanceUrl: String = "",
)

class PlaylistsViewModel(
    private val storage: YubalStorage,
    private val apiService: YubalApiService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistsUiState())
    val uiState: StateFlow<PlaylistsUiState> = _uiState.asStateFlow()
    private var pollingJob: Job? = null

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val url = storage.getInstanceUrl()
            if (url != null) {
                _uiState.value = _uiState.value.copy(instanceUrl = url)
                startPolling(url)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun startPolling(baseUrl: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) { fetch(baseUrl); delay(5_000L) }
        }
    }

    private suspend fun fetch(baseUrl: String) {
        when (val result = apiService.listSubscriptions(baseUrl)) {
            is ApiResponse.Success -> {
                DataCache.subscriptions = result.data.items
                DataCache.subscriptionsLoaded = true
                _uiState.value = _uiState.value.copy(isLoading = false, subscriptions = result.data.items)
            }
            is ApiResponse.Error -> {
                _uiState.value = _uiState.value.copy(isLoading = !DataCache.subscriptionsLoaded, subscriptions = DataCache.subscriptions)
            }
        }
    }


    fun onSyncAll() {
        val url = _uiState.value.instanceUrl; if (url.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncingAll = true)
            apiService.syncAllSubscriptions(url)
            delay(1000)
            _uiState.value = _uiState.value.copy(isSyncingAll = false)
        }
    }

    fun onSyncSubscription(subscriptionId: String) {
        val url = _uiState.value.instanceUrl; if (url.isEmpty()) return
        viewModelScope.launch { apiService.syncSubscription(url, subscriptionId) }
    }

    fun onDeleteSubscription(subscriptionId: String) {
        val url = _uiState.value.instanceUrl; if (url.isEmpty()) return
        viewModelScope.launch { apiService.deleteSubscription(url, subscriptionId) }
    }

    fun refresh() {
        val url = _uiState.value.instanceUrl
        if (url.isNotEmpty()) viewModelScope.launch { fetch(url) }
    }

    override fun onCleared() { super.onCleared(); pollingJob?.cancel() }

    class Factory(private val storage: YubalStorage, private val apiService: YubalApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PlaylistsViewModel(storage, apiService) as T
    }
}
