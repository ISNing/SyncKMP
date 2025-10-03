package moe.isning.syncthing.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun buildSyncthingProcessConfigPlatformDefault(): SyncthingProcessConfig {
    val context = LocalContext.current
    return SyncthingProcessConfig(
        binaryPath = "${context.applicationInfo.nativeLibraryDir}/libsyncnative.so",
        workingDir = context.filesDir.absolutePath,
        configPath = context.filesDir.resolve("config.xml").absolutePath,
        logFilePath = context.filesDir.resolve("sync.log").absolutePath,
    )
}