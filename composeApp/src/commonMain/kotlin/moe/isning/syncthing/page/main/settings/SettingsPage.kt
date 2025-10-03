package moe.isning.syncthing.page.main.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference
import me.zhanghai.compose.preference.textFieldPreference

/**
 *
 *     val binaryPath: String,
 *     val workingDir: String? = null,
 *     val logFilePath: String? = null,
 *     val useRoot: Boolean = false,
 *     val env: Map<String, String> = emptyMap(),
 *     val extraArgs: List<String> = emptyList(),
 *     val ioNice: Int? = null, // reserved for future tuning
 *     val maxLogLines: Int = 200_000, // similar to old LOG_FILE_MAX_LINES
 */
@Composable
fun SettingsPage() {
    ProvidePreferenceLocals {
        // Other composable wrapping the LazyColumn ...
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            preferenceCategory(
                key = "process_config",
                title = {
                    Text("Process Configuration")
                })
            textFieldPreference(
                key = "binary_path",
                defaultValue = "",
                title = { Text(text = "Binary Path") },
                icon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = null) },
                summary = { Text(text = it.ifEmpty{ "Default"}) },
                textToValue = { it },
            )
            textFieldPreference(
                key = "working_dir",
                defaultValue = "",
                title = { Text(text = "Working Directory") },
                icon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = null) },
                summary = { Text(text = it.ifEmpty{ "Default"}) },
                textToValue = { it },
            )
            textFieldPreference(
                key = "log_file_path",
                defaultValue = "",
                title = { Text(text = "Log File Path") },
                icon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = null) },
                summary = { Text(text = it.ifEmpty{ "Default"}) },
                textToValue = { it },
            )
            switchPreference(
                key = "use_root",
                defaultValue = false,
                title = { Text(text = "Use Root") },
                icon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = null) },
            )
            preference(
                key = "environment_variables",
                title = { Text(text = "Environment Variables") },
                icon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = null) },
            )
            textFieldPreference(
                key = "extra_args",
                defaultValue = "",
                title = { Text(text = "Extra Arguments") },
                icon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = null) },
                summary = { Text(text = it.ifEmpty{ "Empty"}) },
                textToValue = { it },
            )
            textFieldPreference(
                key = "io_nice",
                defaultValue = null,
                title = { Text(text = "IO Niceness") },
                icon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = null) },
                summary = { Text(text = it.toString()) },
                textToValue = { it.toIntOrNull() },//FIXME
                valueToText = { it?.toString() ?: "" },
            )
            textFieldPreference(
                key = "max_log_lines",
                defaultValue = 200_000,
                title = { Text(text = "Max Log Lines") },
                icon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = null) },
                summary = { Text(text = it.toString()) },
                textToValue = { it.toIntOrNull() ?: 200_000 },//FIXME
            )
        }
    }
}