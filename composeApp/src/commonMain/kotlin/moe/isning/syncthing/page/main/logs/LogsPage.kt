package moe.isning.syncthing.page.main.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import cafe.adriel.lyricist.LocalStrings
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
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

	// UI 本地筛选/刷新控制
	var searchQuery by remember { mutableStateOf("") }
	var onlyErrors by remember { mutableStateOf(false) }
	var autoRefresh by remember { mutableStateOf(true) }

	LaunchedEffect(Unit) {
		viewModel.loadLogs(resetSince = true)
	}

	// 自动刷新（追加新日志）
	LaunchedEffect(autoRefresh) {
		while (autoRefresh) {
			viewModel.loadLogs(resetSince = false)
			delay(3_000)
		}
	}

	Scaffold(
		modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).fillMaxSize(),
		topBar = {
			LargeTopAppBar(
				title = { Text(LocalStrings.current.titleLogs) },
				actions = {
					IconButton(onClick = { viewModel.loadLogs(resetSince = false) }) {
						Icon(Icons.Default.Refresh, contentDescription = "刷新日志")
					}
					IconButton(onClick = {
						scope.launch {
							val ok = viewModel.clearErrors()
							snackbarHostState.showSnackbar(
								if (ok) "已清除系统错误" else "清除失败"
							)
						}
					}) {
						Icon(Icons.Default.DeleteSweep, contentDescription = "清除错误")
					}
					IconButton(onClick = {
						val visibleLogs: List<LogEntry> = if (onlyErrors) {
							state.systemErrors
						} else {
							val base = state.logEntries
							if (searchQuery.isBlank()) base else base.filter {
								it.message.contains(searchQuery, ignoreCase = true) ||
										it.time.contains(searchQuery, ignoreCase = true)
							}
						}
						val text = buildString {
							if (onlyErrors) {
								appendLine("[系统错误] (${visibleLogs.size})")
							} else {
								if (state.systemErrors.isNotEmpty()) {
									appendLine("[系统错误] (${state.systemErrors.size})")
									state.systemErrors.forEach { appendLine("${it.time}  ${it.message}") }
									appendLine()
								}
								appendLine("[日志] (${visibleLogs.size})")
							}
							visibleLogs.forEach { appendLine("${it.time}  ${it.message}") }
						}
						clipboard.setText(AnnotatedString(text))
						scope.launch { snackbarHostState.showSnackbar("已复制日志") }
					}) {
						Icon(Icons.Default.ContentCopy, contentDescription = "复制日志")
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
						Text(text = state.error ?: "未知错误", color = MaterialTheme.colorScheme.error)
						androidx.compose.material3.FilledTonalButton(onClick = { viewModel.loadLogs(resetSince = true) }) {
							Text("重试")
						}
					}
				}
				else -> {
					Column(modifier = Modifier.fillMaxSize()) {
						// 搜索与筛选区
						Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
							OutlinedTextField(
								value = searchQuery,
								onValueChange = { searchQuery = it },
								modifier = Modifier.fillMaxWidth(),
								singleLine = true,
								leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
								placeholder = { Text("搜索时间或内容…") }
							)
							Spacer(Modifier.height(8.dp))
							Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
								FilterChip(
									selected = autoRefresh,
									onClick = { autoRefresh = !autoRefresh },
									label = { Text(if (autoRefresh) "自动刷新 开" else "自动刷新 关") }
								)
								FilterChip(
									selected = onlyErrors,
									onClick = { onlyErrors = !onlyErrors },
									label = { Text("仅错误") }
								)
							}
						}

						// 列表
						val filteredLogs = remember(searchQuery, state.logEntries) {
							if (searchQuery.isBlank()) state.logEntries else state.logEntries.filter {
								it.message.contains(searchQuery, ignoreCase = true) ||
										it.time.contains(searchQuery, ignoreCase = true)
							}
						}

						LazyColumn(
							modifier = Modifier.fillMaxSize(),
							contentPadding = PaddingValues(16.dp),
							verticalArrangement = Arrangement.spacedBy(12.dp)
						) {
							if (state.systemErrors.isNotEmpty()) {
								item { ErrorsCard(state.systemErrors) }
							}
							if (!onlyErrors) {
								item {
									Text(
										text = "最近日志" + if (filteredLogs.isNotEmpty()) " (${filteredLogs.size})" else "",
										style = MaterialTheme.typography.titleMedium,
										modifier = Modifier.padding(bottom = 4.dp)
									)
								}
								if (filteredLogs.isEmpty()) {
									item {
										Text(
											text = if (searchQuery.isBlank()) "暂无日志" else "无匹配结果",
											style = MaterialTheme.typography.bodyMedium,
											color = MaterialTheme.colorScheme.onSurfaceVariant
										)
									}
								} else {
									items(filteredLogs) { entry ->
										LogRow(entry)
									}
								}
							}
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
				text = "系统错误 (${errors.size})",
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
					text = "+ ${errors.size - 50} 更多…",
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
