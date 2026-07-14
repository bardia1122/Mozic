plugins {
    alias(libs.plugins.mozic.android.library)
}

android {
    namespace = "com.example.mozic.core.domain"
}

dependencies {
    // Domain is the lowest layer: models + repository interfaces only.
    // `Result` (the repositories' one-shot return type) lives in :core:common,
    // so it's re-exported here as part of the contract surface.
    api(project(":core:common"))
    api(libs.kotlinx.coroutines.core)
    api(libs.androidx.paging.common)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
