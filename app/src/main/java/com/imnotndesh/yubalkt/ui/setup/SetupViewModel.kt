package com.imnotndesh.yubalkt.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.yubalkt.core.network.ApiResponse
import com.imnotndesh.yubalkt.core.network.YubalApiService
import com.imnotndesh.yubalkt.core.storage.YubalStorage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the current phase of the connection test button.
 * Mirrors the [TestPhase] from the browser extension's [SetupPage].
 */
enum class TestPhase {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR,
}

/**
 * UI state for the [SetupScreen].
 *
 * @property urlInput      The current text in the URL input field.
 * @property testPhase     Current phase of the "Test Connection" button.
 * @property testErrorMsg  Human-readable error message (only set when phase is ERROR).
 * @property statusText    Status text shown below the input (e.g., "Enter a server URL").
 * @property isSaving      True while the save operation is in progress.
 */
data class SetupUiState(
    val urlInput: String = "",
    val testPhase: TestPhase = TestPhase.IDLE,
    val testErrorMsg: String = "",
    val statusText: String = "",
    val isSaving: Boolean = false,
)

/**
 * ViewModel for the [SetupScreen].
 *
 * Orchestrates:
 * - Loading the previously saved (or draft) URL on init.
 * - Testing the connection via [YubalApiService.healthCheck].
 * - Saving the URL to [YubalStorage] on confirmation.
 */
class SetupViewModel(
    private val storage: YubalStorage,
    private val apiService: YubalApiService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    /**
     * One-shot event fired when the user successfully saves a URL.
     * The [MainActivity] observes this and navigates away.
     */
    private val _savedEvent = MutableSharedFlow<Unit>()
    val savedEvent: SharedFlow<Unit> = _savedEvent.asSharedFlow()

    init {
        loadInitialUrl()
    }

    // ──────────────────────────────────────────────
    // Public actions (called from the UI)
    // ──────────────────────────────────────────────

    /** Update the text in the URL input field as the user types. */
    fun onUrlInputChanged(value: String) {
        _uiState.value = _uiState.value.copy(urlInput = value, statusText = "")
    }

    /** Called when the user taps "Test Connection". */
    fun onTestConnection() {
        val url = trimmedUrl() ?: return

        // Basic URL validation
        if (!isValidUrl(url)) {
            _uiState.value = _uiState.value.copy(
                statusText = "Not a valid URL",
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                testPhase = TestPhase.LOADING,
                statusText = "",
                testErrorMsg = "",
            )

            when (val result = apiService.healthCheck(url)) {
                is ApiResponse.Success -> {
                    _uiState.value = _uiState.value.copy(
                        testPhase = TestPhase.SUCCESS,
                    )
                    // Auto-reset to IDLE after 2 seconds (like the extension)
                }
                is ApiResponse.Error -> {
                    val msg = if (result.status == 0) {
                        "Can't reach server"
                    } else {
                        "Error: ${result.message}"
                    }
                    _uiState.value = _uiState.value.copy(
                        testPhase = TestPhase.ERROR,
                        testErrorMsg = msg,
                    )
                }
            }
        }
    }

    /** Auto-reset test phase back to IDLE (called by the UI after a delay). */
    fun resetTestPhase() {
        _uiState.value = _uiState.value.copy(testPhase = TestPhase.IDLE)
    }

    /** Called when the user taps "Save". */
    fun onSave() {
        val url = trimmedUrl() ?: run {
            _uiState.value = _uiState.value.copy(
                statusText = "Enter a server URL",
            )
            return
        }

        if (!isValidUrl(url)) {
            _uiState.value = _uiState.value.copy(
                statusText = "Not a valid URL",
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            // First do a health check to confirm the server is reachable
            when (apiService.healthCheck(url)) {
                is ApiResponse.Success -> {
                    storage.saveInstanceUrl(url)
                    storage.clearDraftUrl()
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    _savedEvent.emit(Unit)
                }
                is ApiResponse.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        statusText = "Can't reach server — check the URL and try again",
                    )
                }
            }
        }
    }

    /** Trim and strip trailing slashes from the input (matches extension behaviour). */
    private fun trimmedUrl(): String? {
        val trimmed = _uiState.value.urlInput.trim().trimEnd('/')
        return trimmed.ifEmpty { null }
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            java.net.URL(url)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun loadInitialUrl() {
        viewModelScope.launch {
            val draft = storage.getDraftUrl()
            val initial = draft ?: storage.getInstanceUrl() ?: ""
            _uiState.value = _uiState.value.copy(urlInput = initial)
        }
    }

    class Factory(
        private val storage: YubalStorage,
        private val apiService: YubalApiService,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SetupViewModel(storage, apiService) as T
        }
    }
}
