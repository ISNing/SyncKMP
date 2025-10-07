package moe.isning.synckmp.config

interface ConfigApi {
    /** 获取所有 folders 配置 */
    suspend fun getFolders(): List<Folder>

    /** 批量替换所有 folders 配置 */
    suspend fun putFolders(folders: List<Folder>): Boolean

    /** 获取单个 folder 配置 */
    suspend fun getFolder(id: String): Folder

    /** 新增单个 folder */
    suspend fun addFolder(folder: Folder): Boolean

    /** 替换单个 folder 配置 */
    suspend fun putFolder(id: String, folder: Folder): Boolean

    /** 局部更新单个 folder 配置 */
    suspend fun patchFolder(id: String, patch: Folder): Boolean

    /** 删除单个 folder 配置 */
    suspend fun deleteFolder(id: String): Boolean

    /** 获取所有 devices 配置 */
    suspend fun getDevices(): List<Device>

    /** 批量替换所有 devices 配置 */
    suspend fun putDevices(devices: List<Device>): Boolean

    /** 获取单个 device 配置 */
    suspend fun getDevice(id: String): Device

    /** 新增单个 device */
    suspend fun addDevice(device: Device): Boolean

    /** 替换单个 device 配置 */
    suspend fun putDevice(id: String, device: Device): Boolean

    suspend fun patchDevice(id: String, patch: Device): Boolean

    /** 删除单个 device 配置 */
    suspend fun deleteDevice(id: String): Boolean

    /** 获取 options 配置 */
    suspend fun getOptions(): Options

    /** 替换 options 配置 */
    suspend fun putOptions(options: Options): Boolean

    /** 局部更新 options 配置 */
    suspend fun patchOptions(patch: Options): Boolean

    /** 获取默认 folder 配置模板 */
    suspend fun getDefaultFolder(): Folder

    /** 替换默认 folder 配置模板 */
    suspend fun putDefaultFolder(defaults: Folder): Boolean

    /** 局部更新默认 folder 配置模板 */
    suspend fun patchDefaultFolder(patch: Folder): Boolean

    /** 获取默认 device 配置模板 */
    suspend fun getDefaultDevice(): Device

    /** 替换默认 device 配置模板 */
    suspend fun putDefaultDevice(defaults: Device): Boolean

    /** 局部更新默认 device 配置模板 */
    suspend fun patchDefaultDevice(patch: Device): Boolean

    /** 获取默认 ignores 配置，直接返回 lines 字段的 List<String> */
    suspend fun getDefaultIgnores(): List<String>
    suspend fun putDefaultIgnores(lines: List<String>): Boolean

    /** 获取 gui 配置 */
    suspend fun getGui(): Gui

    /** 替换 gui 配置 */
    suspend fun putGui(gui: Gui): Boolean

    /** 局部更新 gui 配置 */
    suspend fun patchGui(patch: Gui): Boolean
}