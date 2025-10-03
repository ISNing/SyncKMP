package moe.isning.syncthing.lifecycle

import androidx.compose.runtime.Composable
import me.zhanghai.compose.preference.createDefaultPreferenceFlow
import androidx.compose.runtime.collectAsState

/**
 * Configuration for running the Syncthing native process.
 * This class is JVM/Android oriented and intended for androidMain, but placed here per request.
 */
data class SyncthingProcessConfig(
    override val binaryPath: String,
    override val workingDir: String,
    override val configPath: String,
    override val dataPath: String?,
    override val logFilePath: String,
    override val useRoot: Boolean = false,
    override val env: Map<String, String> = emptyMap(),
    override val extraArgs: List<String> = emptyList(),
    override val ioNice: Int? = null, // reserved for future tuning
    override val maxLogLines: Int = 200_000, // similar to old LOG_FILE_MAX_LINES
) : SyncthingProcessConfigProcessBuildable(binaryPath, workingDir, configPath, dataPath, logFilePath, useRoot, env, extraArgs, ioNice, maxLogLines)

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
        logFilePath = fromPrefs.logFilePath?.takeUnless { it.isEmpty() } ?: platformDefault.logFilePath,
        useRoot = fromPrefs.useRoot,
        env = fromPrefs.env.ifEmpty { platformDefault.env },
        extraArgs = fromPrefs.extraArgs.ifEmpty { platformDefault.extraArgs },
        ioNice = fromPrefs.ioNice ?: platformDefault.ioNice,
        maxLogLines = fromPrefs.maxLogLines,
    )
}

open class SyncthingProcessConfigProcessBuildable(
    open val binaryPath: String,
    open val workingDir: String?,
    open val configPath: String?,
    open val dataPath: String?,
    open val logFilePath: String?,
    open val useRoot: Boolean = false,
    open val env: Map<String, String> = emptyMap(),
    open val extraArgs: List<String> = emptyList(),
    open val ioNice: Int? = null, // reserved for future tuning
    open val maxLogLines: Int = 200_000, // similar to old LOG_FILE_MAX_LINES
)

open class SyncthingProcessConfigInternal(
    open val binaryPath: String?,
    open val workingDir: String?,
    open val configPath: String?,
    open val dataPath: String?,
    open val logFilePath: String?,
    open val useRoot: Boolean = false,
    open val env: Map<String, String> = emptyMap(),
    open val extraArgs: List<String> = emptyList(),
    open val ioNice: Int? = null, // reserved for future tuning
    open val maxLogLines: Int = 200_000, // similar to old LOG_FILE_MAX_LINES
)

/**
 * Notification sink abstraction so core lifecycle has no Android dependency.
 */
interface NotificationSink {
    fun onStarted() {}
    fun onCrashed(exitCode: Int) {}
    fun onStopped(exitCode: Int?) {}
    fun onOutput(line: String, isError: Boolean) {}
}

object NoopNotificationSink : NotificationSink
