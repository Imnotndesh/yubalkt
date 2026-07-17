package com.imnotndesh.yubalkt.ui.settings

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imnotndesh.yubalkt.core.network.ApiResponse
import com.imnotndesh.yubalkt.core.network.LogEntry
import com.imnotndesh.yubalkt.core.network.YubalApiService
import com.imnotndesh.yubalkt.core.storage.YubalStorage
import com.imnotndesh.yubalkt.ui.utils.DataCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(storage: YubalStorage, apiService: YubalApiService, onLogout: () -> Unit = {}) {
    var showLogs by remember { mutableStateOf(false) }
    if (showLogs) {
        LogsScreen(apiService = apiService, storage = storage, onBack = { showLogs = false })
        return
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cookiesConfigured by remember { mutableStateOf<Boolean?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isUploading = true; statusMessage = ""
                val content = withContext(Dispatchers.IO) {
                    try { context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() } catch (_: Exception) { null }
                }
                if (content != null) {
                    val baseUrl = storage.getInstanceUrl()
                    if (baseUrl != null) {
                        when (apiService.uploadCookies(baseUrl, content)) {
                            is ApiResponse.Success -> { cookiesConfigured = true; statusMessage = "Cookies uploaded successfully" }
                            is ApiResponse.Error -> statusMessage = "Upload failed"
                        }
                    }
                } else { statusMessage = "Could not read file" }
                isUploading = false
            }
        }
    }

    scope.launch {
        val baseUrl = storage.getInstanceUrl()
        if (baseUrl != null) {
            when (val result = apiService.getCookiesStatus(baseUrl)) {
                is ApiResponse.Success -> cookiesConfigured = result.data.configured
                is ApiResponse.Error -> {}
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Text(text = "Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))

            Text(text = "YouTube Cookies", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(text = "Upload a cookies.txt file (Netscape format) to access private playlists and age-restricted content.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(text = "cookies.txt", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.weight(1f))
                        when (cookiesConfigured) {
                            true -> { Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(4.dp)); Text(text = "Configured", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
                            false -> Text(text = "Not configured", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            null -> CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = { filePickerLauncher.launch(arrayOf("text/plain", "*/*")) }, enabled = !isUploading, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                            if (isUploading) { CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary); Spacer(Modifier.width(4.dp)); Text("Uploading...") }
                            else { Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Upload") }
                        }
                        if (cookiesConfigured == true) {
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = {
                                scope.launch {
                                    val baseUrl = storage.getInstanceUrl()
                                    if (baseUrl != null) {
                                        when (apiService.deleteCookies(baseUrl)) {
                                            is ApiResponse.Success -> { cookiesConfigured = false; statusMessage = "Cookies deleted" }
                                            is ApiResponse.Error -> statusMessage = "Delete failed"
                                        }
                                    }
                                }
                            }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer), shape = RoundedCornerShape(8.dp)) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    if (statusMessage.isNotEmpty()) { Spacer(Modifier.height(8.dp)); Text(text = statusMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(text = "Server Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { showLogs = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Article, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("View Server Logs")
            }

            Spacer(Modifier.height(24.dp))
            Text(text = "Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { showLogoutDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Logout (Clear Server URL)")
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("This will clear the saved server URL. You will need to set it up again.") },
            confirmButton = { Button(onClick = { scope.launch { storage.clearInstanceUrl(); storage.clearDraftUrl() }; showLogoutDialog = false; onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Logout") } },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogsScreen(apiService: YubalApiService, storage: YubalStorage, onBack: () -> Unit) {
    var logs by remember { mutableStateOf<List<LogEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val baseUrl = storage.getInstanceUrl()
        if (baseUrl != null) {
            when (val result = apiService.getLogs(baseUrl)) {
                is ApiResponse.Success -> { logs = result.data; DataCache.logs = result.data }
                is ApiResponse.Error -> logs = DataCache.logs
            }
        }
        loading = false
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Scaffold(topBar = {
            TopAppBar(title = { Text("Server Logs") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } })
        }) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
                if (loading) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                else if (logs.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No logs available", style = MaterialTheme.typography.bodyMedium) } }
                else { LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) { items(logs) { log -> Text(text = log.message, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace) } } }
            }
        }
    }
}