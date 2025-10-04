package moe.isning.syncthing.lifecycle


import androidx.compose.runtime.Composable
import me.zhanghai.compose.preference.createDefaultPreferenceFlow
import androidx.compose.runtime.collectAsState

@Composable
fun buildSyncthingProcessConfigFromPrefs(): SyncthingProcessConfigInternal {
    val prefs = createDefaultPreferenceFlow().collectAsState().value

    return SyncthingProcessConfigInternal(
        binaryPath = prefs["binary_path"],
        workingDir = prefs["working_dir"],
        configPath = prefs["config_path"],
        dataPath = prefs["data_path"],
        logFilePath = prefs["log_file_path"],
        useRoot = prefs["use_root"] ?: false,
        env = prefs["env"] ?: emptyMap(),
        extraArgs = prefs["extra_args"] ?: emptyList(),
        ioNice = prefs["io_nice"],
        maxLogLines = prefs["max_log_lines"] ?: 200_000,
    )
}

@Composable
expect fun buildSyncthingProcessConfigPlatformDefault(): SyncthingProcessConfig

@Composable
fun buildSyncthingProcessConfig(): SyncthingProcessConfig {
    val fromPrefs = buildSyncthingProcessConfigFromPrefs()
    val platformDefault = buildSyncthingProcessConfigPlatformDefault()

    return SyncthingProcessConfig(
        binaryPath = fromPrefs.binaryPath?.takeUnless { it.isEmpty() } ?: platformDefault.binaryPath,
        workingDir = fromPrefs.workingDir?.takeUnless { it.isEmpty() } ?: platformDefault.workingDir,
        configPath = fromPrefs.configPath?.takeUnless { it.isEmpty() } ?: platformDefault.configPath,
        dataPath = fromPrefs.dataPath?.takeUnless { it.isEmpty() } ?: platformDefault.dataPath,
        apiKey = fromPrefs.apiKey?.takeUnless { it.isEmpty() } ?: platformDefault.apiKey,
        logFilePath = fromPrefs.logFilePath?.takeUnless { it.isEmpty() } ?: platformDefault.logFilePath,
        useRoot = fromPrefs.useRoot,
        env = fromPrefs.env.ifEmpty { platformDefault.env },
        extraArgs = fromPrefs.extraArgs.ifEmpty { platformDefault.extraArgs },
        ioNice = fromPrefs.ioNice ?: platformDefault.ioNice,
        maxLogLines = fromPrefs.maxLogLines,
    )
}