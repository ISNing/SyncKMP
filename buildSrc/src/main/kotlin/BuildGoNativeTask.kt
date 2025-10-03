package com.github.syncthing

import org.gradle.api.DefaultTask
import org.gradle.api.Named
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.internal.file.FileOperations
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import javax.inject.Inject

abstract class BuildGoNativeTask @Inject constructor(
    private val execOps: ExecOperations,
    private val fsOps: FileSystemOperations,
    private val fileOps: FileOperations,
    private val archiveOps: ArchiveOperations,
    private val layout: ProjectLayout
) : DefaultTask() {

    // 这里直接传入 BuildTarget 子类
    @get:Input
    abstract val buildTarget: Property<BuildTarget>

    @get:Input
    abstract val moduleVersion: Property<String>

    @get:InputDirectory
    abstract val moduleSrcDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    private val logger = Logging.getLogger(BuildGoNativeTask::class.java)

    sealed class BuildTarget(
        @JvmField val name: String,
    ) : Named {
        open lateinit var goos: String
        open lateinit var goarch: String
        open var artifactExt: String = ""
        lateinit var artifactName: String
        open var libBaseName: String? = null
        open var outputFileName: String? = null
        override fun getName(): String = name
        open var cc: String? = null
            protected set

        lateinit var outputDir: DirectoryProperty

        open fun resolveCcIfNeeded(execCapture: (List<String>) -> String): String? = cc

        class GenericTarget(name: String): BuildTarget(name)

        class AppleTarget(
            name: String,
        ) : BuildTarget(
            name = name,
        ) {
            var sdk: String? = null

            init {
                goos = "ios"
                artifactExt = "dylib"
            }

            override fun resolveCcIfNeeded(execCapture: (List<String>) -> String): String {
                if (cc.isNullOrBlank()) {
                    val sdkName = sdk ?: "iphoneos"
                    cc = execCapture(listOf("xcrun", "--sdk", sdkName, "--find", "clang")).trim()
                }
                return cc!!
            }
        }

        class AndroidTarget(
            name: String,
            val ndkHome: String,
            val ndkVersion: String,
            val ccPattern: String,
            val minSdk: Int,
        ) : BuildTarget(
            name = name,
        ) {
            init {
                goos = "android"
                artifactExt = "so"
            }

            override fun resolveCcIfNeeded(execCapture: (List<String>) -> String): String {
                if (cc.isNullOrBlank()) {
                    val hostOs = when {
                        System.getProperty("os.name").startsWith("Mac") -> "darwin-x86_64"
                        System.getProperty("os.name").startsWith("Linux") -> "linux-x86_64"
                        System.getProperty("os.name").startsWith("Windows") -> "windows-x86_64"
                        else -> error("Unsupported host OS")
                    }
                    cc = ccPattern
                        .replace("\$NDK_HOME", ndkHome)
                        .replace("\$VERSION", ndkVersion)
                        .replace("\$HOST_TAG", hostOs)
                        .replace("\$MIN_SDK_VERSION", minSdk.toString())
                }
                return cc!!
            }
        }

        class DesktopTarget(
            name: String,
            val platform: String,
        ) : BuildTarget(
            name = name,
        ) {
            private var _goos: String? = null
            override var goos: String
                get() = _goos ?: when (platform) {
                    "windows" -> "windows"
                    "macos" -> "darwin"
                    else -> "linux"
                }
                set(value) = run { _goos = value }
        }
    }


    @TaskAction
    fun run() {
        runGoBuild(buildTarget.get(), moduleVersion.get())
    }

    private fun runGoBuild(target: BuildTarget, version: String) {
        val goBin = resolveGoBinary()
        val buildEnv = makeBuildEnv()

        val args = mutableListOf(
            goBin, "run", "build.go",
            "-goos", target.goos,
            "-goarch", target.goarch,
            "-version", version,
            "-no-upgrade", "build"
        )
        target.cc?.let { args += listOf("-cc", it) }

        execCapture(args, workDir = moduleSrcDir.asFile.get(), env = buildEnv)

        val artifactBaseName = when (target.goos) {
            "windows" -> target.artifactName + ".exe"
            else -> target.artifactName
        }
        val sourceArtifact = moduleSrcDir.file(artifactBaseName)

        // 确保目录存在
        fileOps.mkdir(outputDir)

        val outputFileName = target.outputFileName ?: "lib${target.libBaseName}.${target.artifactExt}"
        val outputFile = outputDir.file(outputFileName)

        // 删除旧文件
        if (outputFile.isPresent) fileOps.delete(outputFile)

        // 拷贝产物
        fsOps.copy {
            from(sourceArtifact)
            into(outputDir)
            rename { outputFileName }
        }

        logger.lifecycle("Built artifact for ${target.goos}/${target.goarch}: ${outputFile.get().asFile.absolutePath}")
    }

    private fun resolveGoBinary(): String {
        val goBin = System.getenv("GOROOT")?.let { "$it/bin/go" } ?: "go"
        if (File(goBin).exists()) return goBin

        val goVersion = "go1.21.0"
        val downloadUrl = "https://dl.google.com/go/${goVersion}.linux-amd64.tar.gz"

        val goDir = layout.buildDirectory.dir("go").get().asFile
        val archive = File(goDir, "go.tar.gz")

        if (!archive.exists()) {
            logger.lifecycle("Downloading Go toolchain: $downloadUrl")
            archive.parentFile.mkdirs()
            URL(downloadUrl).openStream().use { input ->
                archive.outputStream().use { output -> input.copyTo(output) }
            }
        }

        val goRoot = File(goDir, "go")
        if (!goRoot.exists()) {
            fsOps.copy {
                from(archiveOps.tarTree(archive))
                into(goDir)
            }
        }
        return File(goRoot, "bin/go").absolutePath
    }

    private fun makeBuildEnv(): Map<String, String> {
        val env = mutableMapOf<String, String>()
        env.putAll(System.getenv())
        env["GOPATH"] = layout.buildDirectory.dir("gopath").get().asFile.absolutePath
        env["GOCACHE"] = layout.buildDirectory.dir("gocache").get().asFile.absolutePath
        env["PATH"] = "${resolveGoBinary()}${File.pathSeparator}${System.getenv("PATH")}"
        return env
    }

    private fun execCapture(
        args: List<String>,
        workDir: File? = null,
        env: Map<String, String> = emptyMap()
    ): String {
        val outputStream = ByteArrayOutputStream()
        execOps.exec {
            commandLine = args
            if (workDir != null) workingDir = workDir
            environment.putAll(env)
            standardOutput = outputStream
            errorOutput = outputStream
            isIgnoreExitValue = false
        }
        return outputStream.toString()
    }
}
