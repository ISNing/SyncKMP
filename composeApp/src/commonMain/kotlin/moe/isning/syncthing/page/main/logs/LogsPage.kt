package moe.isning.syncthing.page.main.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import cafe.adriel.lyricist.LocalStrings
import kotlinx.coroutines.launch
import moe.isning.syncthing.lifecycle.LocalServiceController
import moe.isning.syncthing.http.LogEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsPage() {
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
	val controller = LocalServiceController.current
	val api = controller.api

	val viewModel = remember { LogsPageViewModel(api) }
	val state by viewModel.state.collectAsState()

	val snackbarHostState = remember { SnackbarHostState() }
	val clipboard = LocalClipboardManager.current
	val scope = rememberCoroutineScope()

	LaunchedEffect(Unit) {
		viewModel.loadLogs(resetSince = true)
	}

	Scaffold(
		modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).fillMaxSize(),
		topBar = {
			LargeTopAppBar(
				title = { Text(LocalStrings.current.titleLogs) },
				actions = {
					IconButton(onClick = { viewModel.loadLogs(resetSince = false) }) {
						Icon(Icons.Default.Refresh, contentDescription = "Refresh logs")
					}
					IconButton(onClick = {
						scope.launch {
							val ok = viewModel.clearErrors()
							snackbarHostState.showSnackbar(
								if (ok) "Cleared system errors" else "Failed to clear errors"
							)
						}
					}) {
						Icon(Icons.Default.DeleteSweep, contentDescription = "Clear errors")
					}
					IconButton(onClick = {
						val text = buildString {
							if (state.systemErrors.isNotEmpty()) {
								appendLine("[Errors]")
								state.systemErrors.forEach { appendLine("${it.time}  ${it.message}") }
								appendLine()
							}
							appendLine("[Logs]")
							state.logEntries.forEach { appendLine("${it.time}  ${it.message}") }
						}
						clipboard.setText(AnnotatedString(text))
						scope.launch { snackbarHostState.showSnackbar("Logs copied") }
					}) {
						Icon(Icons.Default.ContentCopy, contentDescription = "Copy logs")
					}
				},
				scrollBehavior = scrollBehavior
			)
		},
		snackbarHost = { SnackbarHost(snackbarHostState) }
	) { paddingValues ->
		Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
			when {
				state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
				state.error != null -> {
					Column(
						modifier = Modifier.align(Alignment.Center).padding(16.dp),
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.spacedBy(8.dp)
					) {
						Text(text = state.error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
						androidx.compose.material3.FilledTonalButton(onClick = { viewModel.loadLogs(resetSince = true) }) {
							Text("Retry")
						}
					}
				}
				else -> {
					LazyColumn(
						modifier = Modifier.fillMaxSize(),
						contentPadding = PaddingValues(16.dp),
						verticalArrangement = Arrangement.spacedBy(12.dp)
					) {
						if (state.systemErrors.isNotEmpty()) {
							item { ErrorsCard(state.systemErrors) }
						}
						item {
							Text(
								text = "Recent Logs",
								style = MaterialTheme.typography.titleMedium,
								modifier = Modifier.padding(bottom = 4.dp)
							)
						}
						items(state.logEntries) { entry ->
							LogRow(entry)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun ErrorsCard(errors: List<LogEntry>) {
	ElevatedCard(
		modifier = Modifier.fillMaxWidth(),
		colors = CardDefaults.elevatedCardColors(
			containerColor = MaterialTheme.colorScheme.errorContainer
		)
	) {
		Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
			Text(
				text = "System Errors (${errors.size})",
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onErrorContainer
			)
			Divider(color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f))
			errors.take(50).forEach { e ->
				Text(
					text = "${e.time}  ${e.message}",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onErrorContainer
				)
			}
			if (errors.size > 50) {
				Text(
					text = "+ ${errors.size - 50} more…",
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
				)
			}
		}
	}
}

@Composable
private fun LogRow(entry: LogEntry) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Text(
			text = entry.time,
			style = MaterialTheme.typography.labelSmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
		Text(
			text = entry.message,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		Divider(modifier = Modifier.padding(top = 8.dp))
	}
}
