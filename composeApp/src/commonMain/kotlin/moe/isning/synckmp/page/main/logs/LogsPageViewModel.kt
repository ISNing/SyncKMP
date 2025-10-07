package moe.isning.synckmp.page.main.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import moe.isning.synckmp.http.SyncApi

class LogsPageViewModel(private val api: SyncApi) : ViewModel() {
    private val _state = MutableStateFlow(LogsPageState())
    val state: StateFlow<LogsPageState> = _state.asStateFlow()

    private var sinceToken: String? = null

    fun loadLogs(resetSince: Boolean) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                if (resetSince) sinceToken = null

                val errors = api.getSystemErrors().errors.orEmpty()
                val logs = api.getSystemLog(since = sinceToken).messages.orEmpty()
                sinceToken = logs.lastOrNull()?.time ?: sinceToken

                _state.value = _state.value.copy(
                    isLoading = false,
                    systemErrors = errors,
                    logEntries = if (resetSince) logs else (state.value.logEntries + logs).distinctBy { it.time + it.message }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    suspend fun clearErrors(): Boolean {
        return try {
            val ok = api.clearSystemError()
            if (ok) {
                // refresh
                val errors = api.getSystemErrors().errors.orEmpty()
                _state.value = _state.value.copy(systemErrors = errors)
            }
            ok
        } catch (_: Exception) { false }
    }
}
