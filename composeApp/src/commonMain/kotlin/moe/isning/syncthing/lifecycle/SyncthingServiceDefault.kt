package moe.isning.syncthing.lifecycle


import androidx.compose.runtime.compositionLocalOf
import moe.isning.syncthing.http.SyncthingApi

expect fun buildDefaultSyncthingServiceController(
    cfg: SyncthingProcessConfig,
    api: SyncthingApi,
): SyncthingServiceController


val LocalServiceController =
    compositionLocalOf<SyncthingServiceController> { throw IllegalStateException("No service controller provided") }
