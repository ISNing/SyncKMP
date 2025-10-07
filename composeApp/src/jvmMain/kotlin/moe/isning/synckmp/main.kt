package moe.isning.synckmp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.window.*
import cafe.adriel.lyricist.LocalStrings
import moe.isning.synckmp.di.commonModules
import org.koin.core.context.GlobalContext.startKoin
import org.slf4j.simple.SimpleLogger

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