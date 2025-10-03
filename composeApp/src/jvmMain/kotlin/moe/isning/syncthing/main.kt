package moe.isning.syncthing

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cafe.adriel.lyricist.LocalStrings
import com.kdroid.composetray.tray.api.Tray
import io.ktor.client.HttpClient
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import org.slf4j.simple.SimpleLogger
import java.io.File

fun main() = application {
    System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")

    startKoin {
        modules(module {
            factory { HttpClient() }
        })
    }
    var isWindowOpen by remember { mutableStateOf(true) }

    val trayIcon = rememberVectorPainter(Icons.Default.Info)
    val localStrings = LocalStrings.current

    Tray(
        icon = trayIcon,
        tooltip = "SyncKMP",
        primaryAction = {
            isWindowOpen = true
        }
    ) {
        Item(
            label = localStrings.desktopTrayOpenPanel,
            onClick = {
                isWindowOpen = true
            }
        )
        Item(
            label = localStrings.desktopTrayExit,
            onClick = {
                exitApplication()
            }
        )
    }

    Window(
        visible = isWindowOpen,
        onCloseRequest = {
            isWindowOpen = false
        },
        title = "SyncKMP",
    ) {
        App()
    }
}