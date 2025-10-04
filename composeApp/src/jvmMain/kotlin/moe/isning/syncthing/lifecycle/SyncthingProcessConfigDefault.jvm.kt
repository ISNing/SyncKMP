package moe.isning.syncthing.lifecycle

import androidx.compose.runtime.Composable
import moe.isning.syncthing.resourcesDir
import moe.isning.syncthing.syncKmpConfigDir
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
actual fun buildSyncthingProcessConfigPlatformDefault(): SyncthingProcessConfig {
    return SyncthingProcessConfig(
        binaryPath = File(resourcesDir, "syncthing.exe").absolutePath.also { println("Syncthing binary path: $it") },
        workingDir = syncKmpConfigDir.absolutePath,
        configPath = File(syncKmpConfigDir, "config.xml").absolutePath,
        dataPath = File(syncKmpConfigDir, "data").absolutePath,
        apiKey = Uuid.random().toHexString(),
        logFilePath = File(syncKmpConfigDir, "syncthing.log").absolutePath,
    )
}