package moe.isning.syncthing.strings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import cafe.adriel.lyricist.LyricistStrings

object Locales {
    const val EN = "en"
    const val ZH_HANS = "zh-Hans"
}

data class Strings(
    val navHome: String,
    val navFolders: String,
    val navDevices: String,
    val navLogs: String,
    val navSettings: String,
    val titleHome: String,
    val titleFolders: String,
    val titleDevices: String,
    val titleLogs: String,
    val titleSettings: String,
    val homeStatusCardRunning: String,
    val homeStatusCardStopped: String,
    val homeStatusCardStoppedDescription: String,
    val homeDeviceInfoDownloadSpeed: String,
    val homeDeviceInfoUploadSpeed: String,
    val homeDeviceInfoLocalStatus: String,
    val homeDeviceInfoListeningProgram: String,
    val homeDeviceInfoDeviceDiscovery: String,
    val homeDeviceInfoRunningTime: String,
    val homeDeviceInfoIdentifier: String,
    val desktopTrayOpenPanel: String,
    val desktopTrayExit: String,
    val annotated: AnnotatedString,
    val parameter: (locale: String) -> String,
    val plural: (count: Int) -> String,
    val list: List<String>,
)

@LyricistStrings(languageTag = Locales.EN, default = true)
val EnStrings = Strings(
    navHome = "Home",
    navFolders = "Folders",
    navDevices = "Devices",
    navLogs = "Logs",
    navSettings = "Settings",
    titleHome = "SyncKMP",
    titleFolders = "Folders",
    titleDevices = "Devices",
    titleLogs = "Logs",
    titleSettings = "Settings",
    homeStatusCardRunning = "Running",
    homeStatusCardStopped = "Stopped",
    homeStatusCardStoppedDescription = "Tap to start",
    homeDeviceInfoDownloadSpeed = "Download speed",
    homeDeviceInfoUploadSpeed = "Upload speed",
    homeDeviceInfoLocalStatus = "Local status",
    homeDeviceInfoListeningProgram = "Listening program",
    homeDeviceInfoDeviceDiscovery = "Device discovery",
    homeDeviceInfoRunningTime = "Running time",
    homeDeviceInfoIdentifier = "Identifier",

    desktopTrayOpenPanel = "Open panel",
    desktopTrayExit = "Exit",

    annotated = buildAnnotatedString {
        withStyle(SpanStyle(color = Color.Red)) {
            append("Hello ")
        }
        withStyle(SpanStyle(fontWeight = FontWeight.Light)) {
            append("Compose!")
        }
    },

    parameter = { locale ->
        "Current locale: $locale"
    },

    plural = { count ->
        val value = when (count) {
            0 -> "no"
            1, 2 -> "a few"
            in 3..10 -> "a bunch of"
            else -> "a lot of"
        }
        "I have $value apples"
    },

    list = listOf("Avocado", "Pineapple", "Plum")
)

@LyricistStrings(languageTag = Locales.ZH_HANS, default = false)
val ZhHansStrings = Strings(
    navHome = "主页",
    navFolders = "文件夹",
    navDevices = "设备",
    navLogs = "日志",
    navSettings = "设置",
    titleHome = "主页",
    titleFolders = "文件夹",
    titleDevices = "设备",
    titleLogs = "日志",
    titleSettings = "设置",
    homeStatusCardRunning = "运行中",
    homeStatusCardStopped = "已停止",
    homeStatusCardStoppedDescription = "点击启动",
    homeDeviceInfoDownloadSpeed = "下载速率",
    homeDeviceInfoUploadSpeed = "上传速率",
    homeDeviceInfoLocalStatus = "本地状态 (总计)",
    homeDeviceInfoListeningProgram = "监听程序",
    homeDeviceInfoDeviceDiscovery = "设备发现",
    homeDeviceInfoRunningTime = "运行时间",
    homeDeviceInfoIdentifier = "标识",

    desktopTrayOpenPanel = "打开面板",
    desktopTrayExit = "退出",

    annotated = buildAnnotatedString {
        withStyle(SpanStyle(color = Color.Red)) {
            append("Hello ")
        }
        withStyle(SpanStyle(fontWeight = FontWeight.Light)) {
            append("Compose!")
        }
    },

    parameter = { locale ->
        "Current locale: $locale"
    },

    plural = { count ->
        val value = when (count) {
            0 -> "no"
            1, 2 -> "a few"
            in 3..10 -> "a bunch of"
            else -> "a lot of"
        }
        "I have $value apples"
    },

    list = listOf("Avocado", "Pineapple", "Plum")
)