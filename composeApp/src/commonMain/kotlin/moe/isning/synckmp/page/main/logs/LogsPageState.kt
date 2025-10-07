package moe.isning.synckmp.page.main.logs

import moe.isning.synckmp.http.LogEntry

data class LogsPageState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val systemErrors: List<LogEntry> = emptyList(),
    val logEntries: List<LogEntry> = emptyList()
)
