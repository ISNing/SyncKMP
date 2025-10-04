package moe.isning.syncthing.page.main.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import moe.isning.syncthing.http.SyncthingApi

class DevicesPageViewModel(private val api: SyncthingApi) : ViewModel() {
    private val _state = MutableStateFlow(DevicesPageState())
    val state: StateFlow<DevicesPageState> = _state.asStateFlow()

    fun loadDevices() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                
                val devices = api.getDevices()
                val connections = api.getConnections()
                
                val devicesWithConnection = devices.map { device ->
                    val connection = connections.connections[device.id]
                    DeviceWithConnection(device, connection)
                }
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    devices = devicesWithConnection
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    fun pauseDevice(deviceId: String) {
        viewModelScope.launch {
            try {
                val device = api.getDevice(deviceId)
                api.patchDevice(deviceId, device.copy(paused = true))
                loadDevices()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun resumeDevice(deviceId: String) {
        viewModelScope.launch {
            try {
                val device = api.getDevice(deviceId)
                api.patchDevice(deviceId, device.copy(paused = false))
                loadDevices()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
