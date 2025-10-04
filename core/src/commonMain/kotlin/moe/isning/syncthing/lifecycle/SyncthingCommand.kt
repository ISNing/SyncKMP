package moe.isning.syncthing.lifecycle

/**
 * Commands supported by the embedded Syncthing native binary.
 * Mirrors the old SyncthingRunnable.Command in a cleaner form.
 */
enum class SyncthingCommand {
    DeviceId,      // "device-id"
    Generate,      // "generate"
    Serve,         // "serve --no-browser"
    ResetDatabase, // "debug reset-database"
    ResetDeltas,   // "serve --debug-reset-delta-idxs"
}
