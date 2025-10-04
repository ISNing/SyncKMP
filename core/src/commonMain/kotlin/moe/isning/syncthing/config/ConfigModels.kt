package moe.isning.syncthing.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlId
import nl.adaptivity.xmlutil.serialization.XmlMapEntryName
import nl.adaptivity.xmlutil.serialization.XmlOtherAttributes
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue


@Serializable
data class Folder(
    @XmlId
    val id: String,
    @XmlElement(false)
    val label: String? = null,
    @XmlElement(true)
    val filesystemType: String? = null,
    @XmlElement(false)
    val path: String? = null,
    @XmlElement(false)
    val type: String? = null,
    @XmlSerialName("device")
    val devices: List<FolderDevice> = emptyList(),
    @XmlElement(false)
    val rescanIntervalS: Int? = null,
    @XmlElement(false)
    val fsWatcherEnabled: Boolean? = null,
    @XmlElement(false)
    val fsWatcherDelayS: Int? = null,
    @XmlElement(false)
    val fsWatcherTimeoutS: Int? = null,
    @XmlElement(false)
    val ignorePerms: Boolean? = null,
    @XmlElement(false)
    val autoNormalize: Boolean? = null,
    @XmlElement(true)
    val minDiskFree: MinDiskFree? = null,
    @XmlElement(true)
    val versioning: Versioning? = null,
    @XmlElement(true)
    val copiers: Int? = null,
    @XmlElement(true)
    val pullerMaxPendingKiB: Int? = null,
    @XmlElement(true)
    val hashers: Int? = null,
    @XmlElement(true)
    val order: String? = null,
    @XmlElement(true)
    val ignoreDelete: Boolean? = null,
    @XmlElement(true)
    val scanProgressIntervalS: Int? = null,
    @XmlElement(true)
    val pullerPauseS: Int? = null,
    @XmlElement(true)
    val pullerDelayS: Int? = null,
    @XmlElement(true)
    val maxConflicts: Int? = null,
    @XmlElement(true)
    val disableSparseFiles: Boolean? = null,
    @XmlElement(true)
    val paused: Boolean? = null,
    @XmlElement(true)
    val markerName: String? = null,
    @XmlElement(true)
    val copyOwnershipFromParent: Boolean? = null,
    @XmlElement(true)
    val modTimeWindowS: Int? = null,
    @XmlElement(true)
    val maxConcurrentWrites: Int? = null,
    @XmlElement(true)
    val disableFsync: Boolean? = null,
    @XmlElement(true)
    val blockPullOrder: String? = null,
    @XmlElement(true)
    val copyRangeMethod: String? = null,
    @XmlElement(true)
    val caseSensitiveFS: Boolean? = null,
    @XmlElement(true)
    val junctionsAsDirs: Boolean? = null,
    @XmlElement(true)
    val syncOwnership: Boolean? = null,
    @XmlElement(true)
    val sendOwnership: Boolean? = null,
    @XmlElement(true)
    val syncXattrs: Boolean? = null,
    @XmlElement(true)
    val sendXattrs: Boolean? = null,
    @XmlElement(true)
    val xattrFilter: XattrFilter? = null,
    @Transient
    @XmlOtherAttributes
    val others: Map<String, String> = emptyMap()
)

@Serializable
data class MinDiskFree(
    @XmlValue(true)
    val value: Int? = null,
    @XmlElement(false)
    val unit: String? = null,
    @Transient
    @XmlOtherAttributes
val others: Map<String, String> = emptyMap()
)

@Serializable
data class Versioning(
    @XmlElement(false) val type: String? = null,
    @SerialName("params")
    @XmlMapEntryName("param")
    val params: Map<String, String>? = null,
    @XmlElement(true)
    val cleanupIntervalS: Int? = null,
    @XmlElement(true)
    val fsPath: String? = null,
    @XmlElement(true)
    val fsType: String? = null,
    @Transient
    @XmlOtherAttributes
    val others: Map<String, String> = emptyMap()
)

@Serializable
data class XattrEntry(
    @XmlElement(false) val match: String,
    @XmlElement(false) val permit: Boolean,
    @Transient
    @XmlOtherAttributes
    val others: Map<String, String> = emptyMap()
)

@Serializable
data class XattrFilter(
    @XmlChildrenName("entry")
    val entries: List<XattrEntry>? = null,
    @XmlElement(true)
    val maxSingleEntrySize: Int? = null,
    @XmlElement(true)
    val maxTotalSize: Int? = null,
    @Transient
    @XmlOtherAttributes
    val others: Map<String, String> = emptyMap()
    )

@Serializable
data class FolderDevice(
    @SerialName("deviceID") @XmlId @XmlSerialName("id") val id: String,
    @XmlElement(false) val introducedBy: String? = null,
    @XmlElement(true)
    val encryptionPassword: String? = null,
    @Transient
    @XmlOtherAttributes
    val others: Map<String, String> = emptyMap()
)


@Serializable
data class Device(
    @SerialName("deviceID")
    @XmlId
    @XmlSerialName("id") val id: String,
    @XmlElement(false)
    val name: String? = null,
    @XmlChildrenName("address")
    val addresses: List<String>? = null,
    @XmlElement(false)
    val compression: String? = null,
    @XmlElement(true)
    val certName: String? = null,
    @XmlElement(false)
    val introducer: Boolean? = null,
    @XmlElement(false)
    val skipIntroductionRemovals: Boolean? = null,
    @XmlElement(false)
    val introducedBy: String? = null,
    @XmlElement(true)
    val paused: Boolean? = null,
    @XmlElement(true)
    val allowedNetworks: List<String>? = null,
    @XmlElement(true)
    val autoAcceptFolders: Boolean? = null,
    @XmlElement(true)
    val maxSendKbps: Int? = null,
    @XmlElement(true)
    val maxRecvKbps: Int? = null,
    @XmlElement(true)
    val ignoredFolders: List<String>? = null,
    @XmlElement(true)
    val maxRequestKiB: Int? = null,
    @XmlElement(true)
    val untrusted: Boolean? = null,
    @XmlElement(true)
    val remoteGUIPort: Int? = null,
    @XmlElement(true)
    val numConnections: Int? = null,
    @Transient
    @XmlOtherAttributes
    val others: Map<String, String> = emptyMap()
)

@Serializable
data class Options(
    @XmlChildrenName("listenAddress")
    val listenAddresses: List<String>? = null,

    @XmlChildrenName("globalAnnounceServer")
    val globalAnnounceServers: List<String>? = null,
    @XmlElement(true)
    val globalAnnounceEnabled: Boolean? = null,
    @XmlElement(true)
    val localAnnounceEnabled: Boolean? = null,
    @XmlElement(true)
    val localAnnouncePort: Int? = null,
    @XmlElement(true)
    val localAnnounceMCAddr: String? = null,
    @XmlElement(true)
    val maxSendKbps: Int? = null,
    @XmlElement(true)
    val maxRecvKbps: Int? = null,
    @XmlElement(true)
    val reconnectionIntervalS: Int? = null,
    @XmlElement(true)
    val relaysEnabled: Boolean? = null,
    @XmlElement(true)
    val relayReconnectIntervalM: Int? = null,
    @XmlElement(true)
    val startBrowser: Boolean? = null,
    @XmlElement(true)
    val natEnabled: Boolean? = null,
    @XmlElement(true)
    val natLeaseMinutes: Int? = null,
    @XmlElement(true)
    val natRenewalMinutes: Int? = null,
    @XmlElement(true)
    val natTimeoutSeconds: Int? = null,
    @XmlElement(true)
    val urAccepted: Int? = null,
    @XmlElement(true)
    val urSeen: Int? = null,
    @XmlElement(true)
    val urUniqueId: String? = null,
    @XmlElement(true)
    val urURL: String? = null,
    @XmlElement(true)
    val urPostInsecurely: Boolean? = null,
    @XmlElement(true)
    val urInitialDelayS: Int? = null,
    @XmlElement(true)
    val autoUpgradeIntervalH: Int? = null,
    @XmlElement(true)
    val upgradeToPreReleases: Boolean? = null,
    @XmlElement(true)
    val keepTemporariesH: Int? = null,
    @XmlElement(true)
    val cacheIgnoredFiles: Boolean? = null,
    @XmlElement(true)
    val progressUpdateIntervalS: Int? = null,
    @XmlElement(true)
    val limitBandwidthInLan: Boolean? = null,
    @XmlElement(true)
    val minHomeDiskFree: MinDiskFree? = null,
    @XmlElement(true)
    val releasesURL: String? = null,
    @XmlElement(true)
    val alwaysLocalNets: List<String>? = null,
    @XmlElement(true)
    val overwriteRemoteDeviceNamesOnConnect: Boolean? = null,
    @XmlElement(true)
    val tempIndexMinBlocks: Int? = null,
    @XmlElement(true)
    val unackedNotificationIDs: List<String>? = null,
    @XmlElement(true)
    val trafficClass: Int? = null,
    @XmlElement(true)
    val setLowPriority: Boolean? = null,
    @XmlElement(true)
    val maxFolderConcurrency: Int? = null,
    @XmlElement(true)
    val crURL: String? = null,
    @XmlElement(true)
    val crashReportingEnabled: Boolean? = null,
    @XmlElement(true)
    val stunKeepaliveStartS: Int? = null,
    @XmlElement(true)
    val stunKeepaliveMinS: Int? = null,
    @XmlElement(true)
    val stunServers: List<String>? = null,
    @XmlElement(true)
    val maxConcurrentIncomingRequestKiB: Int? = null,
    @XmlElement(true)
    val announceLANAddresses: Boolean? = null,
    @XmlElement(true)
    val sendFullIndexOnUpgrade: Boolean? = null,
    @XmlElement(true)
    val featureFlags: List<String>? = null,
    @XmlElement(true)
    val auditEnabled: Boolean? = null,
    @XmlElement(true)
    val auditFile: String? = null,
    @XmlElement(true)
    val connectionLimitEnough: Int? = null,
    @XmlElement(true)
    val connectionLimitMax: Int? = null,
    @XmlElement(true)
    val connectionPriorityTcpLan: Int? = null,
    @XmlElement(true)
    val connectionPriorityQuicLan: Int? = null,
    @XmlElement(true)
    val connectionPriorityTcpWan: Int? = null,
    @XmlElement(true)
    val connectionPriorityQuicWan: Int? = null,
    @XmlElement(true)
    val connectionPriorityRelay: Int? = null,
    @XmlElement(true)
    val connectionPriorityUpgradeThreshold: Int? = null,
    @Transient
    @XmlOtherAttributes
    val others: Map<String, String> = emptyMap()
)

@Serializable
data class Gui(
    @XmlElement(false)
    val enabled: Boolean? = null,
    @XmlElement(true)
    val address: String? = null,
    @XmlElement(true)
    val unixSocketPermissions: String? = null,
    @XmlElement(true)
    val user: String? = null,
    @XmlElement(true)
    val password: String? = null,
    @XmlElement(true)
    val authMode: String? = null,
    @XmlElement(true)
    val metricsWithoutAuth: Boolean? = null,
    @SerialName("useTLS")
    @XmlElement(false)
    @XmlSerialName("tls")
    val useTLS: Boolean? = null,
    @XmlElement(true)
    val apiKey: String? = null,
    @XmlElement(true)
    val insecureAdminAccess: Boolean? = null,
    @XmlElement(true)
    val theme: String? = null,
    @XmlElement(true)
    val insecureSkipHostcheck: Boolean? = null,
    @XmlElement(true)
    val insecureAllowFrameLoading: Boolean? = null,
    @XmlElement(false)
    val sendBasicAuthPrompt: Boolean? = null,
    @Transient
    @XmlOtherAttributes
    val others: Map<String, String> = emptyMap()
)

// ==================== Defaults ====================

@Serializable
data class Ignores(
    @SerialName("lines")
    @XmlSerialName("line")
    val lines: List<String> = emptyList(),
    @Transient
    @XmlOtherAttributes
    val others: Map<String, String> = emptyMap()
)

@Serializable
data class Defaults(
    @XmlElement(true)
    val folder: Folder? = null,
    @XmlElement(true)
    val device: Device? = null,
    @XmlElement(true)
    val ignores: Ignores? = null,
    @Transient
    @XmlOtherAttributes
    val others: Map<String, String> = emptyMap()
)

// ==================== Root ====================

@Serializable
data class Ldap(
    @XmlElement(true)
    val address: String? = null,
    @XmlElement(true)
    val bindDN: String? = null,
    @XmlElement(true)
    val transport: String? = null,
    @XmlElement(true)
    val insecureSkipVerify: Boolean? = null,
    @XmlElement(true)
    val searchBaseDN: String? = null,
    @Transient
    @XmlOtherAttributes
    val others: Map<String, String> = emptyMap()
)

@Serializable
data class Configuration(
    @XmlElement(false)
    val version: Int?,
    @SerialName("folder") @XmlElement(true)
    val folders: List<Folder> = emptyList(),
    @SerialName("device") @XmlElement(true)
    val devices: List<Device> = emptyList(),
    @XmlElement(true)
    val gui: Gui? = null,
    @XmlElement(true)
    val ldap: Ldap? = null,
    @XmlElement(true)
    val options: Options? = null,
    @XmlElement(true)
    val defaults: Defaults? = null,
    @Transient
    @XmlOtherAttributes
    val others: Map<String, String> = emptyMap()
)
