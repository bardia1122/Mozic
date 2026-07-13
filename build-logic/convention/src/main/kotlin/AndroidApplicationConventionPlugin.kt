import com.android.build.api.dsl.ApplicationExtension
import com.example.mozic.convention.ProjectConfig
import com.example.mozic.convention.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // AGP 9 ships built-in Kotlin support, so applying the Android plugin
        // also registers the `kotlin` extension — no separate kotlin.android.
        pluginManager.apply("com.android.application")

        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid(this)
            defaultConfig {
                targetSdk = ProjectConfig.TARGET_SDK
                versionCode = 1
                versionName = "1.0"
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
        }
    }
}
