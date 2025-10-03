import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class GoNativeExtension @Inject constructor(objects: ObjectFactory) {
    val libraries: NamedDomainObjectContainer<LibrarySpec> =
        objects.domainObjectContainer(LibrarySpec::class.java) { name ->
            LibrarySpec(name, objects)
        }

    fun library(name: String, configure: LibrarySpec.() -> Unit) {
        libraries.create(name, configure)
    }
}