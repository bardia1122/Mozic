import com.example.mozic.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * Shared configuration for every `:feature:*` module: Android library + Compose
 * + Hilt, plus the baseline dependencies each feature needs (design system, UI
 * kit, domain contract, navigation, lifecycle, paging). Keeps the ~10 feature
 * build files down to a namespace declaration.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("mozic.android.library")
            apply("mozic.android.library.compose")
            apply("mozic.android.hilt")
        }

        dependencies {
            add("implementation", project(":core:designsystem"))
            add("implementation", project(":core:ui"))
            add("implementation", project(":core:domain"))
            add("implementation", project(":core:common"))

            add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
            add("implementation", libs.findLibrary("androidx-navigation-compose").get())
            add("implementation", libs.findLibrary("androidx-hilt-navigation-compose").get())
            add("implementation", libs.findLibrary("androidx-paging-compose").get())
            add("implementation", libs.findLibrary("androidx-compose-material-icons-extended").get())

            add("testImplementation", libs.findLibrary("junit").get())
            add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
        }
    }
}
