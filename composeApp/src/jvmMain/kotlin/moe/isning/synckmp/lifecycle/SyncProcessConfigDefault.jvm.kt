package moe.isning.synckmp.lifecycle

import androidx.compose.runtime.Composable
import moe.isning.synckmp.resourcesDir
import moe.isning.synckmp.syncKmpConfigDir
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private fun getJvmPlatform(): String {
    val osName = System.getProperty("os.name")

    return when {
        osName.contains("Mac", ignoreCase = true) -> "macOS"
        osName.contains("Windows", ignoreCase = true) -> "Windows"
        osName.contains("Linux", ignoreCase = true) -> "Linux"
        else -> "Unknown Desktop"
    }
}

private val platform by lazy { getJvmPlatform() }
private val binaryName by lazy {
    ("synckmp" + when (platform) {
        "Windows" -> ".exe"
        else -> ""
    })
}

@OptIn(ExperimentalUuidApi::class)
@Composable
actual fun buildSyncProcessConfigPlatformDefault(): SyncProcessConfig {
    return SyncProcessConfig(
        binaryPath = File(resourcesDir, binaryName).absolutePath.also { println("SyncKMP binary path: $it") },
        workingDir = syncKmpConfigDir.absolutePath,
        configPath = File(syncKmpConfigDir, "config.xml").absolutePath,
        dataPath = File(syncKmpConfigDir, "data").absolutePath,
        apiKey = Uuid.random().toHexString(),
        logFilePath = File(syncKmpConfigDir, "synckmp.log").absolutePath,
    )
}