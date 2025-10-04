package moe.isning.syncthing.page.main.devices

import moe.isning.syncthing.config.Device
import moe.isning.syncthing.http.ConnectionInfo

data class DeviceWithConnection(
    val device: Device,
    val connection: ConnectionInfo? = null
)

data class DevicesPageState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val devices: List<DeviceWithConnection> = emptyList()
)
