import com.github.syncthing.BuildGoNativeTask
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

open class LibrarySpec @Inject constructor(
    @JvmField val name: String,
    objects: ObjectFactory
) : Named {

    override fun getName(): String = name

    val baseName: Property<String> = objects.property(String::class.java).convention("gonative")
    val goModuleVersion: Property<String> = objects.property(String::class.java)
    val goModuleSrc: DirectoryProperty = objects.directoryProperty()

    val targets: NamedDomainObjectContainer<BuildGoNativeTask.BuildTarget> =
        objects.domainObjectContainer(BuildGoNativeTask.BuildTarget::class.java) { targetName ->
            BuildGoNativeTask.BuildTarget.GenericTarget(targetName)
        }

    fun target(name: String, configure: BuildGoNativeTask.BuildTarget.() -> Unit) {
        targets.create(name, configure)
    }

    // --- Android convenience methods ---
    fun androidArm(ndkHome: String, ndkVersion: String, minSdk: Int) {
        val t = BuildGoNativeTask.BuildTarget.AndroidTarget(
            name = "androidArm",
            ndkHome = ndkHome,
            ndkVersion = ndkVersion,
            ccPattern = "\$NDK_HOME/toolchains/llvm/prebuilt/\$HOST_TAG/bin/armv7a-linux-androideabi\$MIN_SDK_VERSION-clang",
            minSdk = minSdk
        )
        t.goarch = "arm"
        targets.add(t)
    }

    fun androidArm64(ndkHome: String, ndkVersion: String, minSdk: Int) {
        val t = BuildGoNativeTask.BuildTarget.AndroidTarget(
            name = "androidArm64",
            ndkHome = ndkHome,
            ndkVersion = ndkVersion,
            ccPattern = "\$NDK_HOME/toolchains/llvm/prebuilt/\$HOST_TAG/bin/aarch64-linux-android\$MIN_SDK_VERSION-clang",
            minSdk = minSdk
        )
        t.goarch = "arm64"
        targets.add(t)
    }

    fun androidX86(ndkHome: String, ndkVersion: String, minSdk: Int) {
        val t = BuildGoNativeTask.BuildTarget.AndroidTarget(
            name = "androidX86",
            ndkHome = ndkHome,
            ndkVersion = ndkVersion,
            ccPattern = "\$NDK_HOME/toolchains/llvm/prebuilt/\$HOST_TAG/bin/i686-linux-android\$MIN_SDK_VERSION-clang",
            minSdk = minSdk
        )
        t.goarch = "386"
        targets.add(t)
    }

    fun androidX64(ndkHome: String, ndkVersion: String, minSdk: Int) {
        val t = BuildGoNativeTask.BuildTarget.AndroidTarget(
            name = "androidX64",
            ndkHome = ndkHome,
            ndkVersion = ndkVersion,
            ccPattern = "\$NDK_HOME/toolchains/llvm/prebuilt/\$HOST_TAG/bin/x86_64-linux-android\$MIN_SDK_VERSION-clang",
            minSdk = minSdk
        )
        t.goarch = "amd64"
        targets.add(t)
    }

    // --- Apple convenience methods ---
    fun iosArm64(sdk: String? = null) {
        val t = BuildGoNativeTask.BuildTarget.AppleTarget("iosArm64")
        t.goarch = "arm64"
        t.sdk = sdk ?: "iphoneos"
        targets.add(t)
    }

    fun iosX64(sdk: String? = null) {
        val t = BuildGoNativeTask.BuildTarget.AppleTarget("iosX64")
        t.goarch = "amd64"
        t.sdk = sdk ?: "iphonesimulator"
        targets.add(t)
    }

    fun iosSimulatorArm64(sdk: String? = null) {
        val t = BuildGoNativeTask.BuildTarget.AppleTarget("iosSimulatorArm64")
        t.goarch = "arm64"
        t.sdk = sdk ?: "iphonesimulator"
        targets.add(t)
    }

    // --- Desktop convenience ---
    fun linuxAmd64() { addDesktop("linux", "amd64") }
    fun linuxArm64() { addDesktop("linux", "arm64") }
    fun macosX64() { addDesktop("macos", "amd64") }
    fun macosArm64() { addDesktop("macos", "arm64") }
    fun windowsX64() { addDesktop("windows", "amd64") }

    private fun addDesktop(platform: String, arch: String) {
        val t = BuildGoNativeTask.BuildTarget.DesktopTarget("${platform}_$arch", platform)
        t.goarch = arch
        targets.add(t)
    }
}
