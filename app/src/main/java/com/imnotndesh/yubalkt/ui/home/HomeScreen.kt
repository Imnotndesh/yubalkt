package com.imnotndesh.yubalkt.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.imnotndesh.yubalkt.core.network.JobStatusEnum
import com.imnotndesh.yubalkt.core.network.YubalJob
import com.imnotndesh.yubalkt.ui.utils.DataCache
import com.imnotndesh.yubalkt.ui.utils.JobCardSkeleton
import com.imnotndesh.yubalkt.ui.utils.KpiCardSkeleton

@Composable
fun HomeScreen(viewModel: HomeViewModel, onNavigateToSetup: () -> Unit = {}) {
    val state by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    var completedExpanded by remember { mutableStateOf(true) }

    if (!state.isConfigured && !state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(16.dp)); Text(text = "No server configured", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp)); Text(text = "Connect to your yubal server to get started", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp)); Button(onClick = onNavigateToSetup) { Text("Set up server") }
            }
        }
        return
    }
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val ongoing = state.jobs.filter { it.status.isActive }
    val completed = state.jobs.filter { it.status.isFinished }

    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = { isRefreshing = true; viewModel.refresh(); isRefreshing = false }) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Downloads", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (completed.isNotEmpty()) {
                        Button(onClick = viewModel::onClearFinished, enabled = !state.isClearingJobs, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer), shape = RoundedCornerShape(8.dp)) {
                            if (state.isClearingJobs) { CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp); Spacer(Modifier.width(4.dp)) }
                            Text("Clear")
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KpiCard(count = ongoing.size, label = "Ongoing", icon = Icons.Default.PlayCircle, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                    KpiCard(count = completed.size, label = "Completed", icon = Icons.Default.CheckCircle, color = Color(0xFF16A34A), modifier = Modifier.weight(1f))
                }
            }


            if (state.errorMessage != null && (state.jobs.isNotEmpty() || !state.isLoading)) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp)); Text(text = state.errorMessage!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp)); IconButton(onClick = { viewModel.dismissError() }, modifier = Modifier.size(24.dp)) { Icon(imageVector = Icons.Default.Clear, contentDescription = "Dismiss", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onErrorContainer) }
                        }
                    }
                }
            }

            if (ongoing.isEmpty() && completed.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                            Spacer(Modifier.height(12.dp)); Text(text = "No downloads yet", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "Share a YouTube URL to start a download", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            if (ongoing.isNotEmpty()) {
                item { Text(text = "Ongoing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                items(ongoing, key = { it.id }) { job ->
                    JobCard(job = job, onCancel = { viewModel.onCancelJob(job.id) }, onDelete = { viewModel.onDeleteJob(job.id) }, onViewLogs = { viewModel.onViewJobLogs(job.id) })
                }
            } else if (state.isLoading) {
                item { Text(text = "Ongoing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                items(3) { JobCardSkeleton() }
            }


            if (completed.isNotEmpty()) {
                item {
                    Row(modifier = Modifier.fillMaxWidth().clickable { completedExpanded = !completedExpanded }, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Completed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Icon(imageVector = if (completedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = if (completedExpanded) "Collapse" else "Expand")
                    }
                }
                item {
                    AnimatedVisibility(visible = completedExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            completed.forEach { job ->
                                JobCard(job = job, onCancel = { viewModel.onCancelJob(job.id) }, onDelete = { viewModel.onDeleteJob(job.id) }, onViewLogs = { viewModel.onViewJobLogs(job.id) })
                            }
                        }
                    }
                }
            }

            if (state.selectedJobLogs != null) {
                item {
                    AlertDialog(
                        onDismissRequest = { viewModel.onDismissLogs() },
                        title = { Text("Job Logs") },
                        text = {
                            if (state.logsLoading) { Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                            else if (state.selectedJobLogs!!.isEmpty()) { Text("No logs available for this job") }
                            else { Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState())) { state.selectedJobLogs!!.forEach { log -> Text(text = log.message, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(vertical = 2.dp)) } } }
                        },
                        confirmButton = { TextButton(onClick = { viewModel.onDismissLogs() }) { Text("Close") } }
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiCard(count: Int, label: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = count.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
                Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun JobCard(job: YubalJob, onCancel: () -> Unit, onDelete: () -> Unit, onViewLogs: () -> Unit = {}) {
    val context = LocalContext.current
    val statusColor = when (job.status) {
        JobStatusEnum.PENDING -> MaterialTheme.colorScheme.tertiary
        JobStatusEnum.FETCHING_INFO -> MaterialTheme.colorScheme.primary
        JobStatusEnum.DOWNLOADING -> MaterialTheme.colorScheme.primary
        JobStatusEnum.IMPORTING -> MaterialTheme.colorScheme.primary
        JobStatusEnum.COMPLETED -> Color(0xFF16A34A)
        JobStatusEnum.FAILED -> MaterialTheme.colorScheme.error
        JobStatusEnum.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusIcon: ImageVector = when (job.status) {
        JobStatusEnum.PENDING -> Icons.Default.HourglassBottom
        JobStatusEnum.FETCHING_INFO -> Icons.Default.PlayCircle
        JobStatusEnum.DOWNLOADING -> Icons.Default.PlayCircle
        JobStatusEnum.IMPORTING -> Icons.Default.PlayCircle
        JobStatusEnum.COMPLETED -> Icons.Default.CheckCircle
        JobStatusEnum.FAILED -> Icons.Default.Error
        JobStatusEnum.CANCELLED -> Icons.Default.PauseCircle
    }
    val title = job.contentInfo?.title ?: extractTitleFromUrl(job.url)
    val artist = job.contentInfo?.artist
    val thumbnailUrl = job.contentInfo?.thumbnailUrl
    val ytUrl = job.contentInfo?.url ?: job.url
    val progressPercent = job.progress.toInt().coerceIn(0, 100)
    Card(modifier = Modifier.fillMaxWidth().then(if (job.status == JobStatusEnum.FAILED) Modifier.clickable { onViewLogs() } else Modifier), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (thumbnailUrl != null) {
                    AsyncImage(model = thumbnailUrl, contentDescription = title, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ytUrl))) }, contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(10.dp))
                } else {
                    Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (artist != null) Text(text = artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (job.status == JobStatusEnum.FAILED) Text(text = "Tap to view logs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
                if (job.status.isActive) { IconButton(onClick = onCancel) { Icon(imageVector = Icons.Default.Clear, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.error) } }
                else if (job.status.isFinished) { IconButton(onClick = onDelete) { Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant) } }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = job.status.displayName, style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Medium)
                if (job.status.isActive && job.progress > 0.0) { Spacer(Modifier.width(4.dp)); Text(text = "($progressPercent%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Spacer(Modifier.width(12.dp))
                Text(text = job.url, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            }
            if (job.status.isActive) {
                Spacer(Modifier.height(6.dp))
                LinearWavyProgressIndicator(progress = { (job.progress.toFloat() / 100f).coerceIn(0f, 1f) }, color = statusColor, trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), amplitude = { 0.4f }, wavelength = 48.dp, modifier = Modifier.fillMaxWidth().height(10.dp))
            }
        }
    }
}

private fun extractTitleFromUrl(url: String): String = when {
    url.contains("watch?v=") -> "Track (${url.substringAfter("watch?v=").substringBefore("&")})"
    url.contains("playlist?list=") -> "Playlist (${url.substringAfter("playlist?list=").substringBefore("&")})"
    else -> url.take(50)
}
