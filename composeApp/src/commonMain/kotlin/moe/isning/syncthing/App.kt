package moe.isning.syncthing

import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import moe.isning.syncthing.lifecycle.LocalServiceController
import moe.isning.syncthing.lifecycle.buildDefaultSyncthingServiceController
import moe.isning.syncthing.lifecycle.buildSyncthingProcessConfig
import moe.isning.syncthing.page.main.MainPage
import moe.isning.syncthing.ui.theme.AppTheme


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun App() {
    val serviceProcessConfig = buildSyncthingProcessConfig()
    val serviceController = buildDefaultSyncthingServiceController(serviceProcessConfig)

    CompositionLocalProvider(LocalServiceController provides serviceController) {
        AppTheme {
            MainPage()
        }
    }
}