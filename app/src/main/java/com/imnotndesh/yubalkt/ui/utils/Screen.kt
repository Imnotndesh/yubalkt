package com.imnotndesh.yubalkt.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val description: String, val showBottomBar: Boolean, val icon: ImageVector?)

object Setup : Screen(route = "setup", title = "Server Setup", description = "Configure your yubal server connection", showBottomBar = false, icon = null)

object Content : Screen(route = "content/{videoUrl}", title = "Content", description = "View YouTube content and send to your yubal server", showBottomBar = false, icon = null) {
    fun createRoute(videoUrl: String): String = "content/$videoUrl"
}

object Home : Screen(route = "home", title = "Home", description = "Recent activity and quick actions", showBottomBar = true, icon = Icons.Default.Home)

object Playlists : Screen(route = "playlists", title = "Playlists", description = "Manage playlist subscriptions and syncing", showBottomBar = true, icon = Icons.Default.Subscriptions)

object Settings : Screen(route = "settings", title = "Settings", description = "Manage your yubal server and preferences", showBottomBar = true, icon = Icons.Default.Settings)

object Screens {
    val bottomNav: List<Screen> = listOf(Home, Playlists, Settings)
    val all: List<Screen> = listOf(Setup, Content, Home, Playlists, Settings)
}
