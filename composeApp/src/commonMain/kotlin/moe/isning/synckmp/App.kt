package moe.isning.synckmp

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import moe.isning.synckmp.http.SyncApi
import moe.isning.synckmp.http.SyncApiConfig
import moe.isning.synckmp.http.SyncHttpClientFactory
import moe.isning.synckmp.lifecycle.LocalServiceController
import moe.isning.synckmp.lifecycle.buildDefaultSyncServiceController
import moe.isning.synckmp.lifecycle.buildSyncProcessConfig
import moe.isning.synckmp.page.main.MainPage
import moe.isning.synckmp.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun App() {
    val serviceProcessConfig = buildSyncProcessConfig()
    val apiConfig = SyncApiConfig("http://127.0.0.1:8384", serviceProcessConfig.apiKey)
    val serviceController = buildDefaultSyncServiceController(
        serviceProcessConfig, SyncApi(
            apiConfig,
            SyncHttpClientFactory.create(apiConfig)
        )
    )

    CompositionLocalProvider(LocalServiceController provides serviceController) {
        LaunchedEffect(Unit) {
            runCatching {
                serviceController.startServe(waitForWebGui = true)
            }.onFailure {
            }
        }
        AppTheme {
            MainPage()
        }
    }
}