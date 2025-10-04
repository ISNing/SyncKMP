package moe.isning.syncthing.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
actual fun buildSyncthingProcessConfigPlatformDefault(): SyncthingProcessConfig {
    val context = LocalContext.current
    return SyncthingProcessConfig(
        binaryPath = "${context.applicationInfo.nativeLibraryDir}/libsyncnative.so",
        workingDir = context.filesDir.absolutePath,
        configPath = context.filesDir.resolve("config.xml").absolutePath,
        dataPath = context.filesDir.resolve("data").absolutePath,
        apiKey = Uuid.random().toHexString(),
        logFilePath = context.filesDir.resolve("sync.log").absolutePath,
    )
}