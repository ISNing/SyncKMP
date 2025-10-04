package moe.isning.syncthing.config

import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import java.io.File
import kotlin.collections.emptyList

class ConfigXmlParser(private val file: File) : ConfigApi {

    private val xml = XML {
        indentString = "    "
    }

    @Volatile
    private var config: Configuration = load() ?: Configuration(0)

    private fun load(): Configuration? {
        if (!file.exists() || !file.isFile) return null
        return xml.decodeFromString<Configuration>(file.readText())
    }


    private fun save() {
        val out = xml.encodeToString(Configuration.serializer(), config)
        file.writeText(out)
    }

    // --- helpers: merge/patch operations ---
    private fun mergeFolder(existing: Folder, patch: Folder): Folder {
        return existing.copy(
            id = existing.id, // id 不变（patch.id 可忽略）
            label = patch.label ?: existing.label,
            filesystemType = patch.filesystemType ?: existing.filesystemType,
            path = patch.path ?: existing.path,
            type = patch.type ?: existing.type,
            devices = if (patch.devices.isNotEmpty()) patch.devices else existing.devices,
            rescanIntervalS = patch.rescanIntervalS ?: existing.rescanIntervalS,
            fsWatcherEnabled = patch.fsWatcherEnabled ?: existing.fsWatcherEnabled,
            fsWatcherDelayS = patch.fsWatcherDelayS ?: existing.fsWatcherDelayS,
            fsWatcherTimeoutS = patch.fsWatcherTimeoutS ?: existing.fsWatcherTimeoutS,
            ignorePerms = patch.ignorePerms ?: existing.ignorePerms,
            autoNormalize = patch.autoNormalize ?: existing.autoNormalize,
            minDiskFree = patch.minDiskFree ?: existing.minDiskFree,
            versioning = patch.versioning ?: existing.versioning,
            copiers = patch.copiers ?: existing.copiers,
            pullerMaxPendingKiB = patch.pullerMaxPendingKiB ?: existing.pullerMaxPendingKiB,
            hashers = patch.hashers ?: existing.hashers,
            order = patch.order ?: existing.order,
            ignoreDelete = patch.ignoreDelete ?: existing.ignoreDelete,
            scanProgressIntervalS = patch.scanProgressIntervalS ?: existing.scanProgressIntervalS,
            pullerPauseS = patch.pullerPauseS ?: existing.pullerPauseS,
            pullerDelayS = patch.pullerDelayS ?: existing.pullerDelayS,
            maxConflicts = patch.maxConflicts ?: existing.maxConflicts,
            disableSparseFiles = patch.disableSparseFiles ?: existing.disableSparseFiles,
            paused = patch.paused ?: existing.paused,
            markerName = patch.markerName ?: existing.markerName,
            copyOwnershipFromParent = patch.copyOwnershipFromParent ?: existing.copyOwnershipFromParent,
            modTimeWindowS = patch.modTimeWindowS ?: existing.modTimeWindowS,
            maxConcurrentWrites = patch.maxConcurrentWrites ?: existing.maxConcurrentWrites,
            disableFsync = patch.disableFsync ?: existing.disableFsync,
            blockPullOrder = patch.blockPullOrder ?: existing.blockPullOrder,
            copyRangeMethod = patch.copyRangeMethod ?: existing.copyRangeMethod,
            caseSensitiveFS = patch.caseSensitiveFS ?: existing.caseSensitiveFS,
            junctionsAsDirs = patch.junctionsAsDirs ?: existing.junctionsAsDirs,
            syncOwnership = patch.syncOwnership ?: existing.syncOwnership,
            sendOwnership = patch.sendOwnership ?: existing.sendOwnership,
            syncXattrs = patch.syncXattrs ?: existing.syncXattrs,
            sendXattrs = patch.sendXattrs ?: existing.sendXattrs,
            xattrFilter = patch.xattrFilter ?: existing.xattrFilter,
        )
    }

    private fun mergeDevice(existing: Device, patch: Device): Device {
        return existing.copy(
            id = existing.id,
            name = patch.name ?: existing.name,
            addresses = if (!patch.addresses.isNullOrEmpty()) patch.addresses else existing.addresses,
            compression = patch.compression ?: existing.compression,
            certName = patch.certName ?: existing.certName,
            introducer = patch.introducer ?: existing.introducer,
            skipIntroductionRemovals = patch.skipIntroductionRemovals ?: existing.skipIntroductionRemovals,
            introducedBy = patch.introducedBy ?: existing.introducedBy,
            paused = patch.paused ?: existing.paused,
            allowedNetworks = if (!patch.allowedNetworks.isNullOrEmpty()) patch.allowedNetworks else existing.allowedNetworks,
            autoAcceptFolders = patch.autoAcceptFolders ?: existing.autoAcceptFolders,
            maxSendKbps = patch.maxSendKbps ?: existing.maxSendKbps,
            maxRecvKbps = patch.maxRecvKbps ?: existing.maxRecvKbps,
            ignoredFolders = if (!patch.ignoredFolders.isNullOrEmpty()) patch.ignoredFolders else existing.ignoredFolders,
            maxRequestKiB = patch.maxRequestKiB ?: existing.maxRequestKiB,
            untrusted = patch.untrusted ?: existing.untrusted,
            remoteGUIPort = patch.remoteGUIPort ?: existing.remoteGUIPort,
            numConnections = patch.numConnections ?: existing.numConnections
        )
    }

    private fun mergeOptions(existing: Options, patch: Options): Options {
        return existing.copy(
            listenAddresses = if (!patch.listenAddresses.isNullOrEmpty()) patch.listenAddresses else existing.listenAddresses,
            globalAnnounceServers = if (!patch.globalAnnounceServers.isNullOrEmpty()) patch.globalAnnounceServers else existing.globalAnnounceServers,
            globalAnnounceEnabled = patch.globalAnnounceEnabled ?: existing.globalAnnounceEnabled,
            localAnnounceEnabled = patch.localAnnounceEnabled ?: existing.localAnnounceEnabled,
            localAnnouncePort = patch.localAnnouncePort ?: existing.localAnnouncePort,
            localAnnounceMCAddr = patch.localAnnounceMCAddr ?: existing.localAnnounceMCAddr,
            maxSendKbps = patch.maxSendKbps ?: existing.maxSendKbps,
            maxRecvKbps = patch.maxRecvKbps ?: existing.maxRecvKbps,
            reconnectionIntervalS = patch.reconnectionIntervalS ?: existing.reconnectionIntervalS,
            relaysEnabled = patch.relaysEnabled ?: existing.relaysEnabled,
            relayReconnectIntervalM = patch.relayReconnectIntervalM ?: existing.relayReconnectIntervalM,
            startBrowser = patch.startBrowser ?: existing.startBrowser,
            natEnabled = patch.natEnabled ?: existing.natEnabled,
            natLeaseMinutes = patch.natLeaseMinutes ?: existing.natLeaseMinutes,
            natRenewalMinutes = patch.natRenewalMinutes ?: existing.natRenewalMinutes,
            natTimeoutSeconds = patch.natTimeoutSeconds ?: existing.natTimeoutSeconds,
            urAccepted = patch.urAccepted ?: existing.urAccepted,
            urSeen = patch.urSeen ?: existing.urSeen,
            urUniqueId = patch.urUniqueId ?: existing.urUniqueId,
            urURL = patch.urURL ?: existing.urURL,
            urPostInsecurely = patch.urPostInsecurely ?: existing.urPostInsecurely,
            urInitialDelayS = patch.urInitialDelayS ?: existing.urInitialDelayS,
            autoUpgradeIntervalH = patch.autoUpgradeIntervalH ?: existing.autoUpgradeIntervalH,
            upgradeToPreReleases = patch.upgradeToPreReleases ?: existing.upgradeToPreReleases,
            keepTemporariesH = patch.keepTemporariesH ?: existing.keepTemporariesH,
            cacheIgnoredFiles = patch.cacheIgnoredFiles ?: existing.cacheIgnoredFiles,
            progressUpdateIntervalS = patch.progressUpdateIntervalS ?: existing.progressUpdateIntervalS,
            limitBandwidthInLan = patch.limitBandwidthInLan ?: existing.limitBandwidthInLan,
            minHomeDiskFree = patch.minHomeDiskFree ?: existing.minHomeDiskFree,
            releasesURL = patch.releasesURL ?: existing.releasesURL,
            alwaysLocalNets = if (!patch.alwaysLocalNets.isNullOrEmpty()) patch.alwaysLocalNets else existing.alwaysLocalNets,
            overwriteRemoteDeviceNamesOnConnect = patch.overwriteRemoteDeviceNamesOnConnect ?: existing.overwriteRemoteDeviceNamesOnConnect,
            tempIndexMinBlocks = patch.tempIndexMinBlocks ?: existing.tempIndexMinBlocks,
            unackedNotificationIDs = if (!patch.unackedNotificationIDs.isNullOrEmpty()) patch.unackedNotificationIDs else existing.unackedNotificationIDs,
            trafficClass = patch.trafficClass ?: existing.trafficClass,
            setLowPriority = patch.setLowPriority ?: existing.setLowPriority,
            maxFolderConcurrency = patch.maxFolderConcurrency ?: existing.maxFolderConcurrency,
            crURL = patch.crURL ?: existing.crURL,
            crashReportingEnabled = patch.crashReportingEnabled ?: existing.crashReportingEnabled,
            stunKeepaliveStartS = patch.stunKeepaliveStartS ?: existing.stunKeepaliveStartS,
            stunKeepaliveMinS = patch.stunKeepaliveMinS ?: existing.stunKeepaliveMinS,
            stunServers = if (!patch.stunServers.isNullOrEmpty()) patch.stunServers else existing.stunServers,
            maxConcurrentIncomingRequestKiB = patch.maxConcurrentIncomingRequestKiB ?: existing.maxConcurrentIncomingRequestKiB,
            announceLANAddresses = patch.announceLANAddresses ?: existing.announceLANAddresses,
            sendFullIndexOnUpgrade = patch.sendFullIndexOnUpgrade ?: existing.sendFullIndexOnUpgrade,
            featureFlags = if (!patch.featureFlags.isNullOrEmpty()) patch.featureFlags else existing.featureFlags,
            auditEnabled = patch.auditEnabled ?: existing.auditEnabled,
            auditFile = patch.auditFile ?: existing.auditFile,
            connectionLimitEnough = patch.connectionLimitEnough ?: existing.connectionLimitEnough,
            connectionLimitMax = patch.connectionLimitMax ?: existing.connectionLimitMax,
            connectionPriorityTcpLan = patch.connectionPriorityTcpLan ?: existing.connectionPriorityTcpLan,
            connectionPriorityQuicLan = patch.connectionPriorityQuicLan ?: existing.connectionPriorityQuicLan,
            connectionPriorityTcpWan = patch.connectionPriorityTcpWan ?: existing.connectionPriorityTcpWan,
            connectionPriorityQuicWan = patch.connectionPriorityQuicWan ?: existing.connectionPriorityQuicWan,
            connectionPriorityRelay = patch.connectionPriorityRelay ?: existing.connectionPriorityRelay,
            connectionPriorityUpgradeThreshold = patch.connectionPriorityUpgradeThreshold ?: existing.connectionPriorityUpgradeThreshold
        )
    }

    private fun mergeGui(existing: Gui, patch: Gui): Gui {
        return existing.copy(
            enabled = patch.enabled ?: existing.enabled,
            address = patch.address ?: existing.address,
            unixSocketPermissions = patch.unixSocketPermissions ?: existing.unixSocketPermissions,
            user = patch.user ?: existing.user,
            password = patch.password ?: existing.password,
            authMode = patch.authMode ?: existing.authMode,
            metricsWithoutAuth = patch.metricsWithoutAuth ?: existing.metricsWithoutAuth,
            useTLS = patch.useTLS ?: existing.useTLS,
            apiKey = patch.apiKey ?: existing.apiKey,
            insecureAdminAccess = patch.insecureAdminAccess ?: existing.insecureAdminAccess,
            theme = patch.theme ?: existing.theme,
            insecureSkipHostcheck = patch.insecureSkipHostcheck ?: existing.insecureSkipHostcheck,
            insecureAllowFrameLoading = patch.insecureAllowFrameLoading ?: existing.insecureAllowFrameLoading,
            sendBasicAuthPrompt = patch.sendBasicAuthPrompt ?: existing.sendBasicAuthPrompt
        )
    }

    // --- ConfigApi methods ---

    override suspend fun getFolders(): List<Folder> {
        return config.folders
    }

    override suspend fun putFolders(folders: List<Folder>): Boolean {
        config = config.copy(folders = folders)
        save()
        return true
    }

    override suspend fun getFolder(id: String): Folder {
        return config.folders.firstOrNull { it.id == id }
            ?: throw NoSuchElementException("Folder with id=$id not found")
    }

    override suspend fun addFolder(folder: Folder): Boolean {
        if (config.folders.any { it.id == folder.id }) return false
        config = config.copy(folders = config.folders + folder)
        save()
        return true
    }

    override suspend fun putFolder(id: String, folder: Folder): Boolean {
        if (config.folders.none { it.id == id }) return false
        config = config.copy(folders = config.folders.map { if (it.id == id) folder else it })
        save()
        return true
    }

    override suspend fun patchFolder(id: String, patch: Folder): Boolean {
        val idx = config.folders.indexOfFirst { it.id == id }
        if (idx == -1) return false
        val merged = mergeFolder(config.folders[idx], patch)
        val mutable = config.folders.toMutableList()
        mutable[idx] = merged
        config = config.copy(folders = mutable)
        save()
        return true
    }

    override suspend fun deleteFolder(id: String): Boolean {
        if (config.folders.none { it.id == id }) return false
        config = config.copy(folders = config.folders.filterNot { it.id == id })
        save()
        return true
    }

    override suspend fun getDevices(): List<Device> {
        return config.devices
    }

    override suspend fun putDevices(devices: List<Device>): Boolean {
        config = config.copy(devices = devices)
        save()
        return true
    }

    override suspend fun getDevice(id: String): Device {
        return config.devices.firstOrNull { it.id == id }
            ?: throw NoSuchElementException("Device with id=$id not found")
    }

    override suspend fun addDevice(device: Device): Boolean {
        if (config.devices.any { it.id == device.id }) return false
        config = config.copy(devices = config.devices + device)
        save()
        return true
    }

    override suspend fun putDevice(id: String, device: Device): Boolean {
        if (config.devices.none { it.id == id }) return false
        config = config.copy(devices = config.devices.map { if (it.id == id) device else it })
        save()
        return true
    }

    override suspend fun patchDevice(id: String, patch: Device): Boolean {
        val idx = config.devices.indexOfFirst { it.id == id }
        if (idx == -1) return false
        val merged = mergeDevice(config.devices[idx], patch)
        val mutable = config.devices.toMutableList()
        mutable[idx] = merged
        config = config.copy(devices = mutable)
        save()
        return true
    }

    override suspend fun deleteDevice(id: String): Boolean {
        if (config.devices.none { it.id == id }) return false
        config = config.copy(devices = config.devices.filterNot { it.id == id })
        save()
        return true
    }

    override suspend fun getOptions(): Options {
        return config.options ?: Options()
    }

    override suspend fun putOptions(options: Options): Boolean {
        config = config.copy(options = options)
        save()
        return true
    }

    override suspend fun patchOptions(patch: Options): Boolean {
        val existing = config.options ?: Options()
        val merged = mergeOptions(existing, patch)
        config = config.copy(options = merged)
        save()
        return true
    }

    override suspend fun getDefaultFolder(): Folder {
        return config.defaults?.folder ?: throw NoSuchElementException("default folder not found")
    }

    override suspend fun putDefaultFolder(defaults: Folder): Boolean {
        val newDefaults = (config.defaults ?: Defaults()).copy(folder = defaults)
        config = config.copy(defaults = newDefaults)
        save()
        return true
    }

    override suspend fun patchDefaultFolder(patch: Folder): Boolean {
        val existing = config.defaults?.folder ?: throw NoSuchElementException("default folder not found")
        val merged = mergeFolder(existing, patch)
        val newDefaults = (config.defaults ?: Defaults()).copy(folder = merged)
        config = config.copy(defaults = newDefaults)
        save()
        return true
    }

    override suspend fun getDefaultDevice(): Device {
        return config.defaults?.device ?: throw NoSuchElementException("default device not found")
    }

    override suspend fun putDefaultDevice(defaults: Device): Boolean {
        val newDefaults = (config.defaults ?: Defaults()).copy(device = defaults)
        config = config.copy(defaults = newDefaults)
        save()
        return true
    }

    override suspend fun patchDefaultDevice(patch: Device): Boolean {
        val existing = config.defaults?.device ?: throw NoSuchElementException("default device not found")
        val merged = mergeDevice(existing, patch)
        val newDefaults = (config.defaults ?: Defaults()).copy(device = merged)
        config = config.copy(defaults = newDefaults)
        save()
        return true
    }

    override suspend fun getDefaultIgnores(): List<String> =
        config.defaults?.ignores?.lines ?: emptyList()

    override suspend fun putDefaultIgnores(lines: List<String>): Boolean {
        val newIgnores = Ignores(lines = lines)
        val newDefaults = (config.defaults ?: Defaults()).copy(ignores = newIgnores)
        config = config.copy(defaults = newDefaults)
        save()
        return true
    }

    override suspend fun getGui(): Gui {
        return config.gui ?: Gui()
    }

    override suspend fun putGui(gui: Gui): Boolean {
        config = config.copy(gui = gui)
        save()
        return true
    }

    override suspend fun patchGui(patch: Gui): Boolean {
        val existing = config.gui ?: Gui()
        val merged = mergeGui(existing, patch)
        config = config.copy(gui = merged)
        save()
        return true
    }
}
