import com.android.build.gradle.LibraryExtension
import com.sseotdabwa.convention.configureComposeAndroid
import com.sseotdabwa.convention.configureHiltAndroid
import com.sseotdabwa.convention.configureKotlinAndroid
import com.sseotdabwa.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                configureComposeAndroid(this)
                defaultConfig.targetSdk = 36
            }

            configureHiltAndroid()

            dependencies {
                add("implementation", libs.findLibrary("androidx.lifecycle.viewmodel.compose").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.runtime.compose").get())
                add("implementation", libs.findLibrary("androidx.navigation.compose").get())
                add("implementation", libs.findLibrary("hilt.navigation.compose").get())
            }
        }
    }
}
