import com.android.build.api.dsl.ApplicationExtension
import com.sseotdabwa.convention.configureKotlinAndroid
import com.sseotdabwa.convention.configureTestAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 36
            }

            configureTestAndroid()
        }
    }
}
