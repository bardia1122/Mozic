package com.example.mozic.convention

/**
 * Single source of truth for the SDK / Java levels shared by every module.
 * Values mirror the project's original app configuration and must stay in sync
 * with `:app` (compileSdk 36, minSdk 29, targetSdk 36, Java 11).
 */
object ProjectConfig {
    // compileSdk 37 is required by the project's own dependency versions
    // (androidx.core 1.19.0, androidx.lifecycle 2.11.0 declare minCompileSdk 37).
    // targetSdk/minSdk are unchanged, so this is a compile-only bump.
    const val COMPILE_SDK = 37
    const val COMPILE_SDK_MINOR = 0
    const val MIN_SDK = 29
    const val TARGET_SDK = 36
}
