package moe.isning.syncthing

import java.io.File

val resourcesDir = File(System.getProperty("compose.application.resources.dir")).also { println("Resources dir: $it") }

val userHomeDir = File(System.getProperty("user.home")).also { println("User home dir: $it") }

val configDir = File(userHomeDir, ".config").also { if (!it.exists()) it.mkdir(); println("Config dir: $it") }

val syncKmpConfigDir = File(configDir, "sync_kmp").also { if (!it.exists()) it.mkdir(); println("SyncKMP config dir: $it") }
