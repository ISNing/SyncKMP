package moe.isning.synckmp.lifecycle


import androidx.compose.runtime.compositionLocalOf
import moe.isning.synckmp.http.SyncApi

expect fun buildDefaultSyncServiceController(
    cfg: SyncProcessConfig,
    api: SyncApi,
): SyncServiceController


val LocalServiceController =
    compositionLocalOf<SyncServiceController> { throw IllegalStateException("No service controller provided") }
