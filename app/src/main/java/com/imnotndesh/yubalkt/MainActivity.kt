package com.imnotndesh.yubalkt

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.imnotndesh.yubalkt.core.network.*
import com.imnotndesh.yubalkt.core.notification.JobNotificationManager
import com.imnotndesh.yubalkt.core.storage.YubalStorage
import com.imnotndesh.yubalkt.core.storage.YubalStorageImpl
import com.imnotndesh.yubalkt.ui.home.HomeScreen
import com.imnotndesh.yubalkt.ui.home.HomeViewModel
import com.imnotndesh.yubalkt.ui.playlists.PlaylistsScreen
import com.imnotndesh.yubalkt.ui.playlists.PlaylistsViewModel
import com.imnotndesh.yubalkt.ui.settings.SettingsScreen
import com.imnotndesh.yubalkt.ui.setup.SetupScreen
import com.imnotndesh.yubalkt.ui.setup.SetupViewModel
import com.imnotndesh.yubalkt.ui.theme.YubalktTheme
import com.imnotndesh.yubalkt.ui.utils.Content
import com.imnotndesh.yubalkt.ui.utils.Home
import com.imnotndesh.yubalkt.ui.utils.Playlists
import com.imnotndesh.yubalkt.ui.utils.Screens
import com.imnotndesh.yubalkt.ui.utils.Settings
import com.imnotndesh.yubalkt.ui.utils.Setup
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val storage by lazy { YubalStorageImpl(applicationContext) }
    private val httpClient by lazy { OkHttpYubalClient() }
    private val apiService by lazy { YubalApiServiceImpl(httpClient) }
    private val notificationManager by lazy { JobNotificationManager(this) }
    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            YubalktTheme {
                YubalApp(storage = storage, apiService = apiService, notificationManager = notificationManager, pendingShareUrl = extractSharedUrl(intent))
            }
        }
    }

    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent) }

    private fun extractSharedUrl(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND) return null
        if (intent.type != "text/plain") return null
        return intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
    }
}

private sealed class AppStartState {
    data object Loading : AppStartState()
    data object Setup : AppStartState()
    data class Content(val videoUrl: String) : AppStartState()
    data object Home : AppStartState()
}

@Composable
fun YubalApp(storage: YubalStorageImpl, apiService: YubalApiServiceImpl, notificationManager: JobNotificationManager, pendingShareUrl: String?) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var startState by remember { mutableStateOf<AppStartState>(AppStartState.Loading) }
    var showShareSheet by remember { mutableStateOf(pendingShareUrl != null) }
    var shareUrl by remember { mutableStateOf(pendingShareUrl) }

    LaunchedEffect(Unit) {
        val instanceUrl = storage.getInstanceUrl()
        startState = when {
            instanceUrl == null -> AppStartState.Setup
            else -> AppStartState.Home
        }
    }

    if (startState is AppStartState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val startDestination = remember(startState) {
        when (startState) {
            is AppStartState.Loading -> Setup.route
            is AppStartState.Setup -> Setup.route
            is AppStartState.Content -> Content.createRoute((startState as AppStartState.Content).videoUrl)
            is AppStartState.Home -> Home.route
        }
    }

    val showBottomBar = when { currentRoute == null -> false; else -> Screens.all.any { it.route == currentRoute && it.showBottomBar } }

    Scaffold(bottomBar = {
        AnimatedVisibility(visible = showBottomBar) {
            NavigationBar {
                Screens.bottomNav.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(selected = selected, onClick = {
                        navController.navigate(screen.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true }
                    }, icon = { if (screen.icon != null) Icon(imageVector = screen.icon, contentDescription = screen.title) }, label = { Text(screen.title) })
                }
            }
        }
    }) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally { it / 4 } },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally { it / 4 } },
        ) {
            composable(Setup.route) {
                val vm: SetupViewModel = viewModel(factory = SetupViewModel.Factory(storage, apiService))
                SetupScreen(viewModel = vm, showBack = false, onNavigateBack = { navController.navigate(
                    Home.route) { popUpTo(Setup.route) { inclusive = true } } })
            }
            composable(Home.route) {
                val vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory(storage, apiService, notificationManager))
                HomeScreen(viewModel = vm, onNavigateToSetup = { navController.navigate(Setup.route) { popUpTo(
                    Home.route) { inclusive = false } } })
            }
            composable(Playlists.route) {
                val vm: PlaylistsViewModel = viewModel(factory = PlaylistsViewModel.Factory(storage, apiService))
                PlaylistsScreen(viewModel = vm)
            }
            composable(Settings.route) {
                SettingsScreen(storage = storage, apiService = apiService, onLogout = {
                    navController.navigate(Setup.route) { popUpTo(0) { inclusive = true } }
                })
            }
        }
    }

    if (showShareSheet && shareUrl != null) {
        ShareBottomSheet(url = shareUrl!!, apiService = apiService, storage = storage, onDismiss = { showShareSheet = false; shareUrl = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareBottomSheet(url: String, apiService: YubalApiService, storage: YubalStorage, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var contentInfo by remember { mutableStateOf<ContentInfo?>(null) }
    var loading by remember { mutableStateOf(true) }
    var actionLoading by remember { mutableStateOf(false) }
    var actionDone by remember { mutableStateOf(false) }
    var actionMessage by remember { mutableStateOf("") }

    LaunchedEffect(url) {
        val baseUrl = storage.getInstanceUrl()
        if (baseUrl != null) {
            when (val result = apiService.getContentInfo(baseUrl, url)) {
                is ApiResponse.Success -> contentInfo = result.data
                is ApiResponse.Error -> {}
            }
        }
        loading = false
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (contentInfo != null) {
                val info = contentInfo!!
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (info.thumbnail_url != null) {
                        AsyncImage(model = info.thumbnail_url, contentDescription = info.title, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                        Spacer(Modifier.width(16.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = info.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(text = info.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = info.kind.name.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.height(20.dp))
                if (actionDone) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp)); Text(text = actionMessage.ifEmpty { "Processing..." }, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF16A34A))
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { scope.launch { sheetState.hide(); onDismiss() } }, modifier = Modifier.fillMaxWidth()) { Text("Done") }
                } else {
                    Button(onClick = {
                        scope.launch {
                            actionLoading = true
                            val baseUrl = storage.getInstanceUrl()
                            if (baseUrl != null) {
                                when (apiService.createJob(baseUrl, url)) {
                                    is ApiResponse.Success -> { actionMessage = "Download queued!"; actionDone = true }
                                    is ApiResponse.Error -> { actionMessage = "Failed to queue"; actionDone = true }
                                }
                            }
                            actionLoading = false
                        }
                    }, enabled = !actionLoading, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
                        if (actionLoading) { CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary); Spacer(Modifier.width(8.dp)) }
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Download")
                    }
                    if (info.kind == ContentKind.PLAYLIST || info.kind == ContentKind.ALBUM) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = {
                            scope.launch {
                                actionLoading = true
                                val baseUrl = storage.getInstanceUrl()
                                if (baseUrl != null) {
                                    when (apiService.createSubscription(baseUrl, url)) {
                                        is ApiResponse.Success -> { actionMessage = "Subscribed!"; actionDone = true }
                                        is ApiResponse.Error -> { actionMessage = "Failed to subscribe"; actionDone = true }
                                    }
                                }
                                actionLoading = false
                            }
                        }, enabled = !actionLoading, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
                            if (actionLoading) { CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                            Icon(imageVector = Icons.Default.Subscriptions, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                            Text("Subscribe to ${if (info.kind == ContentKind.ALBUM) "Album" else "Playlist"}")
                        }
                    }
                }
            } else {
                Text(text = "Could not load content info", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp)); Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
