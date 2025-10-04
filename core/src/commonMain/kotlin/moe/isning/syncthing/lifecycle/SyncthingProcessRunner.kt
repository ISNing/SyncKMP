package moe.isning.syncthing.lifecycle

import kotlinx.coroutines.CoroutineScope


expect fun buildSyncthingProcessRunner(
    scope: CoroutineScope,
    notificationSink: NotificationSink = NoopNotificationSink,
): SyncthingProcessRunner

/**
 * Spawns and supervises the Syncthing native process.
 * JVM/Android APIs are used here by design (runner will live in androidMain).
 */
interface SyncthingProcessRunner {
    val isRunning: Boolean

    suspend fun start(command: SyncthingCommand, cfg: SyncthingProcessConfigProcessBuildable): Int?

    suspend fun awaitExit(): Int

    suspend fun stop(graceful: Boolean = true, killDelayMs: Long = 3000): Int?

    fun buildArgs(command: SyncthingCommand, cfg: SyncthingProcessConfigProcessBuildable): List<String> {
        val base = mutableListOf(cfg.binaryPath)
        base += when (command) {
            SyncthingCommand.DeviceId -> listOf("device-id")
            SyncthingCommand.Generate -> listOf("generate")
            SyncthingCommand.Serve -> listOf("serve", "--no-browser")
            SyncthingCommand.ResetDatabase -> listOf("debug", "reset-database")
            SyncthingCommand.ResetDeltas -> listOf("serve", "--debug-reset-delta-idxs")
        }
        if (cfg.configPath != null && cfg.dataPath == null || cfg.configPath == null && cfg.dataPath != null) {
            throw IllegalArgumentException("Both configPath and dataPath must be specified")
        }
        cfg.configPath?.let { base += listOf("--config", it) }
        cfg.dataPath?.let { base += listOf("--data", it) }
        if (cfg.configPath == null && cfg.dataPath == null)
            cfg.workingDir?.let { base += listOf("--home", it) }
        cfg.apiKey?.let { base += listOf("--gui-apikey=$it") }
        base.addAll(cfg.extraArgs)
        return base.toList()
    }

    fun shellEscape(s: String): String = buildString {
        append('"')
        s.forEach { ch ->
            when (ch) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                else -> append(ch)
            }
        }
        append('"')
    }
}
