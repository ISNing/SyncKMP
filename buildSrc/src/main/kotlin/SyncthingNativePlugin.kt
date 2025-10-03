import org.gradle.api.*
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import javax.inject.Inject

///**
// * Gradle plugin exposing a Kotlin-style DSL to declare Go native build targets.
// *
// * Root DSL: `goNativeBuild { val lib by creating { ... } }`
// * - Inside each library you can declare targets:
// *   - android { outputRoot = ...; androidArm64(); androidArm(); androidX86(); androidX64() }
// *   - macos { macosX64(); macosArm64() }
// *   - ios { iosX64(); iosArm64(); iosSimulatorArm64() }
// *   - watchos { watchosX64(); watchosArm64(); watchosSimulatorArm64() }
// * - Configure per-library:
// *   - goModulePath (required): relative path to the Go module root (containing go.mod)
// *   - baseName (optional): output library base name (default: "gonative"); files are named lib<baseName>.*
// * - Configure per-Android block:
// *   - ndkVersion, minSdk, outputRoot (use {archName} placeholder to inject ABI)
// *
// * The plugin will register tasks like:
// * - Android: buildAndroidArm64GoNative, buildAndroidArmGoNative, buildAndroidX86GoNative, buildAndroidX86_64GoNative
// * - macOS: buildMacosX64GoNative, buildMacosArm64GoNative
// * - iOS/watchOS: buildIos… and buildWatchos…
// * - Aggregate: buildNative (depends on present Android tasks)
// */
//class GoNativePlugin : Plugin<Project> {
//    override fun apply(project: Project) {
//        val container = project.container(LibrarySpec::class.java) { name ->
//            project.objects.newInstance(LibrarySpec::class.java, name, project.objects, project.layout)
//        }
//        // Register the DSL root as a container so `val lib by creating {}` works directly inside `goNativeBuild {}`
//        project.extensions.add("goNativeBuild", container)
//
//        // Ensure tasks are registered after the libraries are fully configured
//        project.afterEvaluate {
//            container.forEach { lib ->
//                lib.registerTasks(project)
//            }
//        }
//    }
//}

//abstract class LibrarySpec @Inject constructor(
//    val name: String,
//    private val objects: ObjectFactory,
//    private val layout: org.gradle.api.file.ProjectLayout,
//) {
//    // Library-level generic properties
//    val baseName: Property<String> = objects.property(String::class.java).convention("gonative")
//    val goModulePath: Property<String> = objects.property(String::class.java)
//
//    // Unified target specification with platform-specific configs encapsulated
//    private sealed class TargetSpec {
//        abstract val goArch: String // amd64 or arm64
//        abstract val goOs: String
//        abstract val archName: String // ios-x64, ios-arm64, ios-simulator-arm64
//        abstract val outputTemplate: String
//
//        class AndroidTargetSpec(
//            val abi: String, // arm64, arm, x86, x86_64
//            val ndkVersion: String,
//            val minSdk: Int,
//            override val outputTemplate: String // may contain {archName}
//        ) : TargetSpec() {
//            override val goArch: String = when (abi) {
//                "arm64" -> "arm64"
//                "arm" -> "arm"
//                "x86" -> "386"
//                "x86_64" -> "amd64"
//                else -> throw GradleException("Unknown Android ABI: $abi")
//            }
//            override val goOs: String = "android"
//
//            override val archName: String = when (abi) {
//                "arm64" -> "arm64-v8a"
//                "arm" -> "armeabi-v7a"
//                "x86" -> "x86"
//                "x86_64" -> "x86_64"
//                else -> throw GradleException("Unknown Android ABI: $abi")
//            }
//        }
//
//        open class NativeTargetSpec(
//            override val goOs: String,
//            override val goArch: String, // amd64 or arm64
//            override val archName: String, // ios-x64, ios-arm64, ios-simulator-arm64
//            override val outputTemplate: String,
//        ) : TargetSpec()
//
//        class IosTargetSpec(
//            override val goArch: String, // amd64 or arm64
//            val sdk: String, // iphonesimulator or ios
//            override val archName: String, // ios-x64, ios-arm64, ios-simulator-arm64
//            override val outputTemplate: String
//        ) : NativeTargetSpec("ios", goArch, archName, outputTemplate)
//    }
//
//    private val targets = mutableListOf<TargetSpec>()
//
//    // Base target group with common output root handling
//    abstract inner class TargetGroup(initialRoot: org.gradle.api.file.DirectoryProperty) {
//        val outputRoot: org.gradle.api.file.DirectoryProperty = initialRoot
//    }
//
//    // Android targets/configuration block
//    inner class AndroidTargets(initialRoot: org.gradle.api.file.DirectoryProperty) : TargetGroup(initialRoot) {
//        val ndkVersion: Property<String> = objects.property(String::class.java)
//        val minSdk: Property<Int> = objects.property(Int::class.java)
//
//        private fun addAndroid(abi: String) {
//            val ndk = ndkVersion.orNull
//                ?: throw GradleException("goNativeBuild.${name}.android: ndkVersion must be set before declaring targets")
//            val min = minSdk.orNull
//                ?: throw GradleException("goNativeBuild.${name}.android: minSdk must be set before declaring targets")
//            val templatePath = outputRoot.get().asFile.path
//            targets += TargetSpec.AndroidTargetSpec(abi, ndk, min, templatePath)
//        }
//
//        fun androidArm64() {
//            addAndroid("arm64")
//        }
//
//        fun androidArm() {
//            addAndroid("arm")
//        }
//
//        fun androidX86() {
//            addAndroid("x86")
//        }
//
//        fun androidX64() {
//            addAndroid("x86_64")
//        }
//
//        // Aliases matching Kotlin/Native Android naming
//        fun androidNativeArm64() {
//            androidArm64()
//        }
//
//        fun androidNativeArm32() {
//            androidArm()
//        }
//
//        fun androidNativeX86() {
//            androidX86()
//        }
//
//        fun androidNativeX64() {
//            androidX64()
//        }
//    }
//
//    // Native (host) targets/configuration block (currently macOS)
//    inner class NativeTargets(initialRoot: org.gradle.api.file.DirectoryProperty) : TargetGroup(initialRoot) {
//        private fun addNative(tag: String) {
//            val templatePath = outputRoot.get().asFile.path
//            targets += TargetSpec.NativeTargetSpec(tag, templatePath)
//        }
//
//        fun macosX64() {
//            addNative("darwin-x86_64")
//        }
//
//        fun macosArm64() {
//            addNative("darwin-arm64")
//        }
//
//        fun mingwX64() {
//            addNative("windows-x86_64")
//        }
//
//        fun mingwArm64() {
//            addNative("windows-arm64")
//        }
//
//        fun linuxX64() {
//            addNative("linux-x86_64")
//        }
//
//        fun linuxArm64() {
//            addNative("linux-arm64")
//        }
//
//        // iOS/watchOS concrete targets
//        fun iosX64() {
//            val templatePath = outputRoot.get().asFile.path
//            targets += TargetSpec.IosTargetSpec(
//                goArch = "amd64",
//                sdk = "iphonesimulator",
//                archName = "ios-x64",
//                outputTemplate = templatePath
//            )
//        }
//
//        fun iosArm64() {
//            val templatePath = outputRoot.get().asFile.path
//            targets += TargetSpec.IosTargetSpec(
//                goArch = "arm64",
//                sdk = "iphoneos",
//                archName = "ios-arm64",
//                outputTemplate = templatePath
//            )
//        }
//
//        fun iosSimulatorArm64() {
//            val templatePath = outputRoot.get().asFile.path
//            targets += TargetSpec.IosTargetSpec(
//                goArch = "arm64",
//                sdk = "iphonesimulator",
//                archName = "ios-simulator-arm64",
//                outputTemplate = templatePath
//            )
//        }
//
//        // Android configuration + targets block
//        fun android(block: AndroidTargets.() -> Unit) {
//            val defaultDir = objects.directoryProperty().apply {
//                set(layout.buildDirectory.dir("generated/goNative/${name}/android/jniLibs/{archName}"))
//            }
//            val at = AndroidTargets(defaultDir)
//            at.block()
//        }
//
//        // Explicit platform blocks to align with Kotlin-style configuration
//        fun macos(block: NativeTargets.() -> Unit) {
//            val defaultDir = objects.directoryProperty().apply {
//                set(layout.buildDirectory.dir("generated/goNative/${name}/native/{archName}"))
//            }
//            val nt = NativeTargets(defaultDir)
//            nt.block()
//        }
//
//        // Explicit platform blocks to align with Kotlin-style configuration
//        fun windows(block: NativeTargets.() -> Unit) {
//            val defaultDir = objects.directoryProperty().apply {
//                set(layout.buildDirectory.dir("generated/goNative/${name}/native/{archName}"))
//            }
//            val nt = NativeTargets(defaultDir)
//            nt.block()
//        }
//
//        fun native(block: NativeTargets.() -> Unit) = macos(block) // alias for now
//
//        fun ios(block: NativeTargets.() -> Unit) {
//            val defaultDir = objects.directoryProperty().apply {
//                set(layout.buildDirectory.dir("generated/goNative/${name}/ios/{archName}"))
//            }
//            val nt = NativeTargets(defaultDir)
//            nt.block()
//        }
//
//        fun watchos(block: NativeTargets.() -> Unit) {
//            val defaultDir = objects.directoryProperty().apply {
//                set(layout.buildDirectory.dir("generated/goNative/${name}/watchos/{archName}"))
//            }
//            val nt = NativeTargets(defaultDir)
//            nt.block()
//        }
//
//        private fun replaceOrAppendArch(path: String, archName: String): java.io.File {
//            return if (path.contains("{archName}")) {
//                java.io.File(path.replace("{archName}", archName))
//            } else java.io.File(path, archName)
//        }
//
//        private fun stripArchToken(path: String): java.io.File {
//            return if (path.contains("{archName}")) java.io.File(path.replace("{archName}", "")) else java.io.File(path)
//        }
//
//
//        private fun registerBuildTask(
//            project: Project,
//            taskName: String,
//            platform: String,
//            destArgValue: String,
//            outputDirFile: java.io.File,
//            extra: BuildSyncthingTask.() -> Unit = {}
//        ): TaskProvider<BuildSyncthingTask> {
//            return project.tasks.register(taskName, BuildSyncthingTask::class.java) {
//                targetPlatform.set(platform)
//                destArg.set(destArgValue)
//                outputDir.set(project.layout.dir(project.provider { outputDirFile }))
//                goModulePath.set(this@LibrarySpec.goModulePath)
//                libBaseName.set(this@LibrarySpec.baseName)
//                extra()
//            }
//        }
//
//        internal fun registerTasks(project: Project) {
//            val createdTasks = mutableListOf<TaskProvider<*>>()
//
//            // Build a PascalCase suffix from library name for task names
//            @Suppress("UNUSED_VARIABLE")
//            val libSuffix = name.split(Regex("[^A-Za-z0-9]+")).filter { it.isNotEmpty() }
//                .joinToString("") { part -> part.replaceFirstChar { ch -> ch.uppercaseChar() } }
//
//            // Android tasks from target specs
//            run {
//                val androidSpecs = targets.filterIsInstance<TargetSpec.AndroidTargetSpec>()
//                val androidTaskProviders = mutableListOf<TaskProvider<*>>()
//                androidSpecs.forEach { spec ->
//                    fun androidMapping(abi: String): Triple<String, String, String> = when (abi) {
//                        "arm64" -> Triple("arm64", "aarch64-linux-android%s-clang", "arm64-v8a")
//                        "arm" -> Triple("arm", "armv7a-linux-androideabi%s-clang", "armeabi-v7a")
//                        "x86" -> Triple("386", "i686-linux-android%s-clang", "x86")
//                        "x86_64" -> Triple("amd64", "x86_64-linux-android%s-clang", "x86_64")
//                        else -> throw GradleException("Unknown Android ABI: ${'$'}{spec.abi}")
//                    }
//                    val (goArchVal, ccPat, abiDir) = androidMapping(spec.abi)
//                    val taskName = when (spec.abi) {
//                        "arm64" -> "buildAndroidArm64${libSuffix}GoNative"
//                        "arm" -> "buildAndroidArm${libSuffix}GoNative"
//                        "x86" -> "buildAndroidX86${libSuffix}GoNative"
//                        else -> "buildAndroidX86_64${libSuffix}GoNative"
//                    }
//                    val outDirFile = replaceOrAppendArch(spec.outputTemplate, abiDir)
//                    val t = registerBuildTask(project, taskName, "android", abiDir, outDirFile) {
//                        ndkVersion.set(spec.ndkVersion)
//                        minSdk.set(spec.minSdk)
//                        goArch.set(goArchVal)
//                        ccPattern.set(ccPat)
//                    }
//                    createdTasks += t
//                    androidTaskProviders += t
//                }
//                if (androidSpecs.isNotEmpty()) {
//                    project.tasks.register("build${libSuffix}Native") {
//                        dependsOn(androidTaskProviders)
//                    }
//                }
//            }
//
//            // macOS tasks from target specs
//            run {
//                val macSpecs = targets.filterIsInstance<TargetSpec.NativeTargetSpec>()
//                macSpecs.forEach { spec ->
//                    val (taskName, archTag) = if (spec.archTag == "darwin-x86_64")
//                        "buildMacosX64${libSuffix}GoNative" to "darwin-x86_64" else "buildMacosArm64${libSuffix}GoNative" to "darwin-arm64"
//                    if (project.tasks.findByName(taskName) == null) {
//                        val baseOut = stripArchToken(spec.outputTemplate)
//                        val t = project.tasks.register(taskName, BuildSyncthingTask::class.java) {
//                            targetPlatform.set("macos")
//                            destArg.set(archTag)
//                            outputDir.set(project.layout.dir(project.provider { baseOut }))
//                            goModulePath.set(this@LibrarySpec.goModulePath)
//                            libBaseName.set(this@LibrarySpec.baseName)
//                        }
//                        createdTasks += t
//                    }
//                }
//            }
//
//            // iOS tasks from target specs
//            run {
//                val iosSpecs = targets.filterIsInstance<TargetSpec.IosTargetSpec>()
//                iosSpecs.forEach { spec ->
//                    val taskName = when {
//                        spec.sdk == "iphoneos" -> "buildIosArm64${libSuffix}GoNative"
//                        spec.sdk == "iphonesimulator" && spec.goArch == "amd64" -> "buildIosX64${libSuffix}GoNative"
//                        else -> "buildIosSimulatorArm64${libSuffix}GoNative"
//                    }
//                    val outDirFile = replaceOrAppendArch(spec.outputTemplate, spec.archName)
//                    if (project.tasks.findByName(taskName) == null) {
//                        val t = project.tasks.register(taskName, BuildSyncthingTask::class.java) {
//                            targetPlatform.set("ios")
//                            goArch.set(spec.goArch)
//                            appleSdk.set(spec.sdk)
//                            destArg.set(spec.archName)
//                            outputDir.set(project.layout.dir(project.provider { outDirFile }))
//                            goModulePath.set(this@LibrarySpec.goModulePath)
//                            libBaseName.set(this@LibrarySpec.baseName)
//                        }
//                        createdTasks += t
//                    }
//                }
//            }
//        }
//    }
//}