package moe.isning.syncthing.page.main.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import cafe.adriel.lyricist.LocalStrings
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import moe.isning.syncthing.http.SystemVersion
import moe.isning.syncthing.lifecycle.LocalServiceController
import moe.isning.syncthing.lifecycle.SyncthingServiceController
import moe.isning.syncthing.page.main.devices.DeviceInfoRow

private val logger = KotlinLogging.logger {}

@OptIn(DelicateCoroutinesApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomePage(
    isRunning: Boolean = true
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val controller: SyncthingServiceController = LocalServiceController.current
    val api = controller.api

    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).fillMaxSize(), topBar = {
            LargeTopAppBar(
                title = { Text(LocalStrings.current.titleHome) },
//                actions = {
//                    Button
//                }
                scrollBehavior = scrollBehavior
            )
        }) {
        Box(
            modifier = Modifier.fillMaxSize().padding(it)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp, 0.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(
                        containerColor = if (isRunning) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    ),
                    onClick = {
                        scope.launch {
                            logger.info { "Home page: toggle status" }
                            if (!controller.isRunning)
                                controller.startServe()
                            else controller.shutdownAndStop()
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (isRunning) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onErrorContainer
                        )
                        Column {
                            Text(
                                text = if (isRunning) LocalStrings.current.homeStatusCardRunning
                                else LocalStrings.current.homeStatusCardStopped,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isRunning) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = if (isRunning) "v2.0.10, Windows (64-bit Intel/AMD)"
                                else LocalStrings.current.homeStatusCardStoppedDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isRunning) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DeviceInfoRow(
                            Icons.Default.Download,
                            LocalStrings.current.homeDeviceInfoDownloadSpeed,
                            "0 B/s (6.65 MiB)"
                        )
                        DeviceInfoRow(
                            Icons.Default.Upload,
                            LocalStrings.current.homeDeviceInfoUploadSpeed,
                            "0 B/s (496 KiB)"
                        )
                        DeviceInfoRow(
                            Icons.Default.Folder,
                            LocalStrings.current.homeDeviceInfoLocalStatus,
                            "3,152 文件 / 1,084 文件夹 ~1.24 GiB"
                        )
                        DeviceInfoRow(
                            Icons.Default.BubbleChart,
                            LocalStrings.current.homeDeviceInfoListeningProgram,
                            "3/3",
                            highlight = true
                        )
                        DeviceInfoRow(
                            Icons.Default.Devices,
                            LocalStrings.current.homeDeviceInfoDeviceDiscovery,
                            "5/5",
                            highlight = true
                        )
                        DeviceInfoRow(
                            Icons.Default.AccessTime,
                            LocalStrings.current.homeDeviceInfoRunningTime,
                            "6天 23时 3分"
                        )
                        DeviceInfoRow(
                            Icons.Default.Key,
                            LocalStrings.current.homeDeviceInfoIdentifier,
                            "FK7IFGI",
                            highlight = true
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
