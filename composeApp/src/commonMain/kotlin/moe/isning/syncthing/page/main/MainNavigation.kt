package moe.isning.syncthing.page.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.twotone.Assignment
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.twotone.DeviceHub
import androidx.compose.material.icons.twotone.FolderShared
import androidx.compose.material.icons.twotone.Home
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import cafe.adriel.lyricist.LocalStrings
import kotlinx.serialization.Serializable

@Serializable
sealed class MainNavigationRoute(val route: String) {
    @Serializable
    data object Home : MainNavigationRoute("home")

    @Serializable
    data object Folders : MainNavigationRoute("folders")
    @Serializable
    data class FolderStatusDetail(val id: Long) : MainNavigationRoute("folder/${id}/detail")
    @Serializable
    data class FolderEditConfig(val id: Long) : MainNavigationRoute("folder/${id}/edit")

    @Serializable
    data object Devices : MainNavigationRoute("devices")
    @Serializable
    data class DeviceStatusDetail(val id: Long) : MainNavigationRoute("device/${id}/detail")
    @Serializable
    data class DeviceEditConfig(val id: Long) : MainNavigationRoute("device/${id}/edit")

    @Serializable
    data object Logs : MainNavigationRoute("logs")
    @Serializable
    data object Settings : MainNavigationRoute("settings")
}


enum class MainNavigationData(
    val route: MainNavigationRoute,
    val label: @Composable () -> String,
    val icon: ImageVector,
    val iconActive: ImageVector,
    val contentDescription: @Composable () -> String,
) {
    HOME(
        MainNavigationRoute.Home,
        { LocalStrings.current.navHome },
        Icons.TwoTone.Home,
        Icons.Filled.Home,
        { LocalStrings.current.navHome }),

    FOLDERS(
        MainNavigationRoute.Folders,
        { LocalStrings.current.navFolders },
        Icons.TwoTone.FolderShared,
        Icons.Filled.FolderShared,
        { LocalStrings.current.navFolders }),
    DEVICES(
        MainNavigationRoute.Devices,
        { LocalStrings.current.navDevices },
        Icons.TwoTone.DeviceHub,
        Icons.Filled.DeviceHub,
        { LocalStrings.current.navDevices }),
    LOGS(
        MainNavigationRoute.Logs,
        { LocalStrings.current.navLogs },
        Icons.AutoMirrored.TwoTone.Assignment,
        Icons.AutoMirrored.Filled.Assignment,
        { LocalStrings.current.navLogs }),
    SETTINGS(
        MainNavigationRoute.Settings,
        { LocalStrings.current.navSettings },
        Icons.TwoTone.Settings,
        Icons.Filled.Settings,
        { LocalStrings.current.navSettings }),
}