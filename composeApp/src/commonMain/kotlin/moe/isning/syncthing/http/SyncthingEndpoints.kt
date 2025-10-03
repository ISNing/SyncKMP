package moe.isning.syncthing.http

/**
 * REST endpoints used by Syncthing.
 * Updated for config endpoints as of Syncthing v1.12.0+.
 */
object SyncthingEndpoints {
    // System
    const val SYSTEM_DISCOVERY = "/rest/system/discovery"
    const val SYSTEM_LOGLEVELS = "/rest/system/loglevels"
    const val VERSION = "/rest/system/version"
    const val SYSTEM_STATUS = "/rest/system/status"
    const val CONNECTIONS = "/rest/system/connections"
    const val SYSTEM_SHUTDOWN = "/rest/system/shutdown"
    const val SYSTEM_ERROR_CLEAR = "/rest/system/error/clear"
    const val SYSTEM_ERROR = "/rest/system/error"
    const val SYSTEM_LOG = "/rest/system/log"
    const val SYSTEM_LOG_TXT = "/rest/system/log.txt"


    // Config (since v1.12.0)
    const val CONFIG = "/rest/config"
    const val CONFIG_RESTART_REQUIRED = "/rest/config/restart-required"
    const val CONFIG_FOLDERS = "/rest/config/folders"
    const val CONFIG_DEVICES = "/rest/config/devices"
    const val CONFIG_FOLDER_ID = "/rest/config/folders/" // append {id}
    const val CONFIG_DEVICE_ID = "/rest/config/devices/" // append {id}
    const val CONFIG_DEFAULTS_FOLDER = "/rest/config/defaults/folder"
    const val CONFIG_DEFAULTS_DEVICE = "/rest/config/defaults/device"
    const val CONFIG_DEFAULTS_IGNORES = "/rest/config/defaults/ignores"
    const val CONFIG_OPTIONS = "/rest/config/options"
    const val CONFIG_LDAP = "/rest/config/ldap"
    const val CONFIG_GUI = "/rest/config/gui"

    // Pending
    const val PENDING_DEVICES = "/rest/cluster/pending/devices"
    const val PENDING_FOLDERS = "/rest/cluster/pending/folders"

    // Debug/Support
    const val DEBUG_SUPPORT = "/rest/debug/support"

    // Database/folders
    const val DB_COMPLETION = "/rest/db/completion"
    const val DB_IGNORES = "/rest/db/ignores"
    const val DB_STATUS = "/rest/db/status"
    const val DB_OVERRIDE = "/rest/db/override"
    const val DB_REVERT = "/rest/db/revert"
    const val DB_SCAN = "/rest/db/scan"

    // Services/misc
    const val DEVICE_ID = "/rest/svc/deviceid"
    const val REPORT = "/rest/svc/report"

    // Events
    const val EVENTS = "/rest/events"
    const val EVENTS_DISK = "/rest/events/disk"

    // Stats
    const val STATS_DEVICE = "/rest/stats/device"
}
