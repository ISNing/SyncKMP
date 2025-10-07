package moe.isning.synckmp.page.main.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import moe.isning.synckmp.config.Device
import moe.isning.synckmp.http.SyncApi

class DevicesPageViewModel(private val api: SyncApi) : ViewModel() {
    private val _state = MutableStateFlow(DevicesPageState())
    val state: StateFlow<DevicesPageState> = _state.asStateFlow()

    fun loadDevices() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                
                // Get local device ID
                val systemStatus = api.getSystemStatus()
                val myDeviceId = systemStatus.myID
                
                val devices = api.getDevices()
                val connections = api.getConnections()
                
                val devicesWithConnection = devices.map { device ->
                    val connection = connections.connections[device.id]
                    DeviceWithConnection(device, connection)
                }
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    devices = devicesWithConnection,
                    myDeviceId = myDeviceId
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    fun addDevice(
        deviceId: String, 
        name: String, 
        addresses: List<String>,
        onComplete: (success: Boolean, message: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val device = Device(
                    id = deviceId,
                    name = name.ifBlank { null },
                    addresses = addresses.ifEmpty { listOf("dynamic") },
                    compression = "metadata",
                    introducer = false,
                    paused = false
                )
                
                val success = api.addDevice(device)
                if (success) {
                    onComplete(true, "Device added successfully")
                } else {
                    onComplete(false, "Failed to add device")
                }
            } catch (e: Exception) {
                onComplete(false, "Error: ${e.message ?: "Unknown error"}")
            }
        }
    }
    
    fun pauseDevice(deviceId: String) {
        viewModelScope.launch {
            try {
                val device = api.getDevice(deviceId)
                api.patchDevice(deviceId, device.copy(paused = true))
                _state.value = _state.value.copy(
                    operationMessage = "Device paused successfully"
                )
                loadDevices()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    operationMessage = "Failed to pause device: ${e.message}"
                )
            }
        }
    }
    
    fun resumeDevice(deviceId: String) {
        viewModelScope.launch {
            try {
                val device = api.getDevice(deviceId)
                api.patchDevice(deviceId, device.copy(paused = false))
                _state.value = _state.value.copy(
                    operationMessage = "Device resumed successfully"
                )
                loadDevices()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    operationMessage = "Failed to resume device: ${e.message}"
                )
            }
        }
    }

    fun deleteDevice(deviceId: String, cascadeFolders: Boolean, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                // do not allow deleting self
                val myId = runCatching { api.getSystemStatus().myID }.getOrNull()
                if (deviceId == myId) {
                    onComplete(false, "Cannot delete local device")
                    return@launch
                }

                if (cascadeFolders) {
                    // remove this device from all folders' share list
                    val folders = runCatching { api.getFolders() }.getOrDefault(emptyList())
                    folders.forEach { f ->
                        if (f.devices.any { it.id == deviceId }) {
                            val updated = f.copy(devices = f.devices.filter { it.id != deviceId })
                            runCatching { api.putFolder(f.id, updated) }
                        }
                    }
                }

                val ok = api.deleteDevice(deviceId)
                if (ok) {
                    _state.value = _state.value.copy(operationMessage = "Device deleted")
                    loadDevices()
                    onComplete(true, "Device deleted")
                } else {
                    onComplete(false, "Failed to delete device")
                }
            } catch (e: Exception) {
                onComplete(false, "Error: ${e.message ?: "Unknown error"}")
            }
        }
    }
}
