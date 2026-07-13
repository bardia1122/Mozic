import com.android.build.api.dsl.LibraryExtension
import com.example.mozic.convention.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // AGP 9 ships built-in Kotlin support, so applying the Android plugin
        // also registers the `kotlin` extension — no separate kotlin.android.
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)
        }
    }
}
