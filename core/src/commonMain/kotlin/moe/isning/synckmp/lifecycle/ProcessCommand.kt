package moe.isning.synckmp.lifecycle


enum class ProcessCommand {
    DeviceId,      // "device-id"
    Generate,      // "generate"
    Serve,         // "serve --no-browser"
    ResetDatabase, // "debug reset-database"
    ResetDeltas,   // "serve --debug-reset-delta-idxs"
}
