import com.github.syncthing.BuildGoNativeTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

class GoNativePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("goNativeBuild", GoNativeExtension::class.java, project.objects)

        project.afterEvaluate {
            extension.libraries.forEach { lib ->
                lib.targets.forEach { target ->
                    val taskName = "build" +
                            target.goos.replaceFirstChar { it.uppercaseChar() } +
                            target.goarch.replaceFirstChar { it.uppercaseChar() } +
                            lib.name.replaceFirstChar { it.uppercaseChar() } +
                            "GoNative"

                    val outDir = project.layout.buildDirectory.dir("${lib.name}/${target.goos}/${target.goarch}")

                    val task: TaskProvider<BuildGoNativeTask> =
                        project.tasks.register(taskName, BuildGoNativeTask::class.java) {
                            buildTarget.set(target)
                            outputDir.set(outDir)
                            moduleSrcDir.set(lib.goModuleSrc)
                            moduleVersion.set(lib.goModuleVersion) // 可改为 DSL 配置
                            target.libBaseName = lib.baseName.get()
                            target.artifactName = lib.baseName.get()
                        }
                }
            }
        }
    }
}
