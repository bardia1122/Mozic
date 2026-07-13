package com.example.mozic.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Applies the Android + Kotlin configuration shared by every module so it is
 * written once here instead of duplicated across ~18 build files.
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension,
) {
    // In AGP 9 the base CommonExtension exposes these as getters; the block
    // (Action) forms live only on the concrete Application/Library extensions,
    // so configure them via property access to stay extension-agnostic.
    commonExtension.compileSdk = ProjectConfig.COMPILE_SDK
    commonExtension.compileSdkMinor = ProjectConfig.COMPILE_SDK_MINOR
    commonExtension.defaultConfig.minSdk = ProjectConfig.MIN_SDK
    commonExtension.compileOptions.sourceCompatibility = JavaVersion.VERSION_11
    commonExtension.compileOptions.targetCompatibility = JavaVersion.VERSION_11

    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
}

/**
 * Enables Jetpack Compose and wires the shared Compose BOM + baseline artifacts.
 * The Compose compiler plugin itself is applied by the `*.compose` convention
 * plugins that call this.
 */
internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension,
) {
    val libs = extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

    commonExtension.buildFeatures.compose = true

    dependencies {
        val bom = libs.findLibrary("androidx-compose-bom").get()
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))
        add("implementation", libs.findLibrary("androidx-compose-ui").get())
        add("implementation", libs.findLibrary("androidx-compose-ui-graphics").get())
        add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
        add("implementation", libs.findLibrary("androidx-compose-material3").get())
        add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
    }
}
