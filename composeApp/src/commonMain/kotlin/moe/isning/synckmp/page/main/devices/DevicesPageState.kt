package moe.isning.synckmp.page.main.devices

import moe.isning.synckmp.config.Device
import moe.isning.synckmp.http.ConnectionInfo

data class DeviceWithConnection(
    val device: Device,
    val connection: ConnectionInfo? = null
)

data class DevicesPageState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val devices: List<DeviceWithConnection> = emptyList(),
    val operationMessage: String? = null,
    val myDeviceId: String? = null
)
