package com.imnotndesh.yubalkt.ui.settings

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            SectionLabel("YOUTUBE COOKIES")
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Upload a cookies.txt file (Netscape format) to access private playlists and age-restricted content.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(text = "cookies.txt", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
                        Spacer(Modifier.weight(1f))
                        when (cookiesConfigured) {
                            true -> StatusPill(text = "Configured", color = MaterialTheme.colorScheme.primary, icon = Icons.Default.CheckCircle)
                            false -> StatusPill(text = "Not configured", color = MaterialTheme.colorScheme.onSurfaceVariant, icon = null)
                            null -> CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { filePickerLauncher.launch(arrayOf("text/plain", "*/*")) },
                            enabled = !isUploading,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                Spacer(Modifier.width(6.dp))
                                Text("Uploading...", style = MaterialTheme.typography.labelLarge)
                            } else {
                                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Upload", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (cookiesConfigured == true) {
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        val baseUrl = storage.getInstanceUrl()
                                        if (baseUrl != null) {
                                            when (apiService.deleteCookies(baseUrl)) {
                                                is ApiResponse.Success -> { cookiesConfigured = false; statusMessage = "Cookies deleted" }
                                                is ApiResponse.Error -> statusMessage = "Delete failed"
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    if (statusMessage.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text(text = statusMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            SectionLabel("SERVER LOGS")
            Spacer(Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLogs = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Article, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(text = "View Server Logs", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }

            Spacer(Modifier.height(28.dp))

            SectionLabel("ACCOUNT")
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Logout (Clear Server URL)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Powered by yt-dlp & ytmusicapi",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("This will clear the saved server URL. You will need to set it up again.") },
            confirmButton = {
                Button(
                    onClick = { scope.launch { storage.clearInstanceUrl(); storage.clearDraftUrl() }; showLogoutDialog = false; onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp),
                ) { Text("Logout") }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } },
            shape = RoundedCornerShape(16.dp),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun StatusPill(text: String, color: androidx.compose.ui.graphics.Color, icon: androidx.compose.ui.graphics.vector.ImageVector?) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = color)
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
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Server Logs", fontFamily = FontFamily.Monospace) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                )
            },
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
                if (loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else if (logs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Article,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            )
                            Spacer(Modifier.height(10.dp))
                            Text("No logs available", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(logs) { log ->
                                Text(
                                    text = log.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}