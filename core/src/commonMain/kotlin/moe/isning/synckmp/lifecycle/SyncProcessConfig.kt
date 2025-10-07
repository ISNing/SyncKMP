package moe.isning.synckmp.lifecycle


data class SyncProcessConfig(
    override val binaryPath: String,
    override val workingDir: String,
    override val configPath: String,
    override val dataPath: String,
    override val apiKey: String,
    override val logFilePath: String,
    override val useRoot: Boolean = false,
    override val env: Map<String, String> = emptyMap(),
    override val extraArgs: List<String> = emptyList(),
    override val ioNice: Int? = null, // reserved for future tuning
    override val maxLogLines: Int = 200_000, // similar to old LOG_FILE_MAX_LINES
) : SyncProcessConfigProcessBuildable(
    binaryPath,
    workingDir,
    configPath,
    dataPath,
    apiKey,
    logFilePath,
    useRoot,
    env,
    extraArgs,
    ioNice,
    maxLogLines
)


open class SyncProcessConfigProcessBuildable(
    override val binaryPath: String,
    override val workingDir: String?,
    override val configPath: String?,
    override val dataPath: String?,
    override val apiKey: String? = null,
    override val logFilePath: String?,
    override val useRoot: Boolean = false,
    override val env: Map<String, String> = emptyMap(),
    override val extraArgs: List<String> = emptyList(),
    override val ioNice: Int? = null, // reserved for future tuning
    override val maxLogLines: Int = 200_000, // similar to old LOG_FILE_MAX_LINES
) : SyncProcessConfigInternal(
    binaryPath,
    workingDir,
    configPath,
    dataPath,
    apiKey,
    logFilePath,
    useRoot,
    env,
    extraArgs,
    ioNice,
    maxLogLines
)

open class SyncProcessConfigInternal(
    open val binaryPath: String?,
    open val workingDir: String?,
    open val configPath: String?,
    open val dataPath: String?,
    open val apiKey: String? = null,
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
