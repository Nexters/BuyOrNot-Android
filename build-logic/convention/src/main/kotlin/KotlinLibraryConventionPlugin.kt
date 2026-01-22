import com.sseotdabwa.convention.configureKotlin
import com.sseotdabwa.convention.configureTestKotlin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

class KotlinLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
            }

            extensions.configure<KotlinProjectExtension> {
                jvmToolchain(17)
            }

            configureKotlin()
            configureTestKotlin()
        }
    }
}
