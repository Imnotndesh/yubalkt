package com.imnotndesh.yubalkt.ui.playlists

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.imnotndesh.yubalkt.core.network.SubscriptionResponse
import com.imnotndesh.yubalkt.ui.utils.SkeletonBox
import com.imnotndesh.yubalkt.ui.utils.SubscriptionCardSkeleton

@Composable
fun PlaylistsScreen(viewModel: PlaylistsViewModel) {
    val state by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(isRefreshing = false, onRefresh = {}) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { SkeletonBox(modifier = Modifier.fillMaxWidth(0.5f).height(24.dp)) }
                    items(3) { SubscriptionCardSkeleton() }
                }
            }
        }
        return
    }


    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = {
        isRefreshing = true; viewModel.refresh(); isRefreshing = false
    }) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Subscriptions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (state.subscriptions.isNotEmpty()) {
                        Button(onClick = viewModel::onSyncAll, enabled = !state.isSyncingAll, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary), shape = RoundedCornerShape(8.dp)) {
                            if (state.isSyncingAll) { CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary); Spacer(Modifier.width(4.dp)) }
                            Text("Sync All")
                        }
                    }
                }
            }
            if (state.subscriptions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Subscriptions, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                            Spacer(Modifier.height(12.dp))
                            Text(text = "No subscriptions", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "Subscribe to playlists from the share sheet or web UI", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                }
            } else {
                item {
                    Text(text = state.instanceUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                items(state.subscriptions, key = { it.id }) { sub -> SubscriptionCard(subscription = sub, onSync = { viewModel.onSyncSubscription(sub.id) }, onDelete = { viewModel.onDeleteSubscription(sub.id) }) }
            }
        }
    }
}

@Composable
private fun SubscriptionCard(subscription: SubscriptionResponse, onSync: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (subscription.thumbnailUrl != null) {
                AsyncImage(model = subscription.thumbnailUrl, contentDescription = subscription.name, modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(subscription.url))) }, contentScale = ContentScale.Crop)
            } else { Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = subscription.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = subscription.type.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    if (subscription.lastSyncedAt != null) { Spacer(Modifier.width(8.dp)); Text(text = "Synced ${formatTimeAgo(subscription.lastSyncedAt)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
                }
            }
            IconButton(onClick = onSync) { Icon(imageVector = Icons.Default.PlayCircle, contentDescription = "Sync", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp)) }
            IconButton(onClick = onDelete) { Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

private fun formatTimeAgo(isoString: String): String {
    return try {
        val instant = java.time.Instant.parse(isoString)
        val now = java.time.Instant.now()
        val minutes = java.time.Duration.between(instant, now).toMinutes()
        when { minutes < 1 -> "just now"; minutes < 60 -> "${minutes}m ago"; minutes < 1440 -> "${minutes / 60}h ago"; else -> "${minutes / 1440}d ago" }
    } catch (_: Exception) { "" }
}
