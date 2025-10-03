package moe.isning.syncthing.lifecycle

import androidx.compose.runtime.Composable
import me.zhanghai.compose.preference.createDefaultPreferenceFlow
import moe.isning.syncthing.resourcesDir
import moe.isning.syncthing.syncKmpConfigDir
import java.io.File

@Composable
actual fun buildSyncthingProcessConfigPlatformDefault(): SyncthingProcessConfig {
    return SyncthingProcessConfig(
        binaryPath = File(resourcesDir, "syncthing.exe").absolutePath.also { println("Syncthing binary path: $it") },
        workingDir = syncKmpConfigDir.absolutePath,
        configPath = File(syncKmpConfigDir, "config.xml").absolutePath,
        dataPath = File(syncKmpConfigDir, "data").absolutePath,
        logFilePath = File(syncKmpConfigDir, "syncthing.log").absolutePath,
    )
}