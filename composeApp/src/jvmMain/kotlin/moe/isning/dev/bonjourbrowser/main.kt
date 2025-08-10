package moe.isning.dev.bonjourbrowser

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "BonjourBrowser",
    ) {
        App()
    }
}