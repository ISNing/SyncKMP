package moe.isning.synckmp.lifecycle

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import java.io.*
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private val logger = KotlinLogging.logger {  }
/**
 * Spawns and supervises the Syncthing native process.
 * JVM/Android APIs are used here by design (runner will live in androidMain).
 */
class JvmSyncthingProcessRunner(
    private val scope: CoroutineScope,
    private val notificationSink: NotificationSink = NoopNotificationSink,
) : SyncthingProcessRunner {
    private val processRef = AtomicReference<Process?>(null)
    private var ioJob: Job? = null

    override val isRunning: Boolean
        get() {
            val p = processRef.get() ?: return false
            return try {
                p.exitValue(); false
            } catch (_: IllegalThreadStateException) {
                true
            }
        }

    override suspend fun start(command: ProcessCommand, cfg: SyncProcessConfigProcessBuildable): Int? =
        withContext(Dispatchers.IO) {
        if (isRunning) return@withContext null

            val args = buildArgs(command, cfg).also { println("Starting synckmp process with args: $it") }
        val pb = if (cfg.useRoot) {
            val cmdLine = args.joinToString(" ") { shellEscape(it) }
            ProcessBuilder(listOf("su", "-c", cmdLine))
        } else {
            ProcessBuilder(args)
        }
        cfg.workingDir?.let { pb.directory(File(it)) }

        if (cfg.env.isNotEmpty()) {
            val env = pb.environment()
            cfg.env.forEach { (k, v) -> env[k] = v }
        }
        pb.redirectErrorStream(false)
        val process = pb.start()
        processRef.set(process)
        notificationSink.onStarted()
        ioJob = pipeProcessIO(process, cfg)
        null
    }

    override suspend fun awaitExit(): Int = withContext(Dispatchers.IO) {
        val p = processRef.get() ?: return@withContext 0
        val code = p.waitFor()
        try {
            ioJob?.cancelAndJoin()
        } catch (_: Throwable) {
        }
        while (p.isAlive) {
            yield()
        }
        processRef.set(null)
        if (code != 0) notificationSink.onCrashed(code) else notificationSink.onStopped(code)
        code
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun stop(graceful: Boolean, killDelayMs: Long): Int? = withContext(Dispatchers.IO) {
        val p = processRef.get() ?: return@withContext null
        return@withContext try {
            p.destroy()
            if (!graceful) {
                p.destroyForcibly()
            }
            if (killDelayMs > 0) {
                val deadline = Clock.System.now().toEpochMilliseconds() + killDelayMs
                while (Clock.System.now().toEpochMilliseconds() < deadline) {
                    val alive = try {
                        p.exitValue(); false
                    } catch (_: IllegalThreadStateException) {
                        true
                    }
                    if (!alive) break
                    Thread.sleep(50)
                }
            }
            try {
                ioJob?.cancelAndJoin()
            } catch (_: Throwable) {
            }
            while (p.isAlive) {
                yield()
            }
            processRef.set(null)
            runCatching { p.exitValue() }.getOrNull()
        } catch (t: Throwable) {
            null
        }
    }

    private fun pipeProcessIO(process: Process, cfg: SyncProcessConfigProcessBuildable): Job {
        val logFile = cfg.logFilePath?.let { File(it) }?.also { if(!it.exists()) it.createNewFile() }
        var writer: FileWriter? = logFile?.let { FileWriter(it, true) }
        return scope.launch(Dispatchers.IO) {
            try {
                val nativeLogger = KotlinLogging.logger("${logger.name}/NativeLog")
                val outReader = BufferedReader(InputStreamReader(process.inputStream, Charset.defaultCharset()))
                val errReader = BufferedReader(InputStreamReader(process.errorStream, Charset.defaultCharset()))
                var linesWritten = 0

                val outJob = launch {
                    try {
                        outReader.useLines { seq ->
                            seq.forEach { line ->
                                notificationSink.onOutput(line, false)
                                writer?.let {
                                    it.appendLine(line)
                                    nativeLogger.trace { line }
                                    linesWritten++
                                    if (linesWritten >= cfg.maxLogLines) {
                                        try {
                                            it.flush()
                                        } catch (_: Throwable) {
                                        }
                                        try {
                                            it.close()
                                        } catch (_: Throwable) {
                                        }
                                        rotateLog(logFile)
                                        writer = logFile?.let { FileWriter(it, true) }
                                        linesWritten = 0
                                    }
                                }
                            }
                        }
                    } catch (e: InterruptedIOException) {
                        logger.info(e) { "Interrupted while reading from process output stream" }
                    } catch (e: IOException) {
                        logger.info(e) { "Failed to read from process output stream" }
                    }
                }
                val errJob = launch {
                    try {
                        errReader.useLines { seq ->
                            seq.forEach { line ->
                                notificationSink.onOutput(line, true)
                                writer?.appendLine(line)
                                nativeLogger.warn { line }
                            }
                        }
                    } catch (e: InterruptedIOException) {
                        logger.info(e) { "Interrupted while reading from process error stream" }
                    } catch (e: IOException) {
                        logger.info(e) { "Failed to read from process output stream" }
                    }
                }
                outJob.join()
                errJob.join()
            } catch (_: Throwable) {
            } finally {
                try {
                    writer?.flush()
                } catch (_: Throwable) {
                }
                try {
                    writer?.close()
                } catch (_: Throwable) {
                }
            }
        }
    }

    private fun rotateLog(file: File?) {
        if (file == null) return
        try {
            if (file.exists()) {
                val bak = File(file.parentFile, file.name + ".1")
                if (bak.exists()) bak.delete()
                file.renameTo(bak)
            }
        } catch (_: Throwable) {
        }
    }
}

actual fun buildSyncProcessRunner(
    scope: CoroutineScope,
    notificationSink: NotificationSink
): SyncthingProcessRunner = JvmSyncthingProcessRunner(scope, notificationSink)
