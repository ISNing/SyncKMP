package moe.isning.syncthing

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.window.application
import cafe.adriel.lyricist.LocalStrings
import io.ktor.client.HttpClient
import moe.isning.syncthing.di.commonModules
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import org.slf4j.simple.SimpleLogger
import java.io.File

fun main() = application {
    System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")

    startKoin {
        modules(commonModules)
    }
    val windowState = rememberWindowState()

    val trayIcon = rememberVectorPainter(Icons.Default.Info)
    val localStrings = LocalStrings.current

    Tray(
        icon = trayIcon,
        tooltip = "SyncKMP",
        onAction = {
            windowState.isMinimized = false
            windowState.placement = WindowPlacement.Floating
        }
    ) {
        Item(localStrings.desktopTrayOpenPanel) {
            windowState.isMinimized = false
            windowState.placement = WindowPlacement.Floating
        }
        Item(localStrings.desktopTrayExit) { exitApplication() }
    }

    Window(
        state = windowState,
        onCloseRequest = {
            // 拦截关闭：仅最小化到托盘，不退出进程
            windowState.isMinimized = true
            // 不调用 exitApplication()
        },
        title = "SyncKMP",
    ) {
        App()
    }
}