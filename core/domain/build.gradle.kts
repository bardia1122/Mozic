plugins {
    alias(libs.plugins.mozic.android.library)
}

android {
    namespace = "com.example.mozic.core.domain"
}

dependencies {
    // Domain is the lowest layer: models + repository interfaces only.
    api(libs.kotlinx.coroutines.core)
    api(libs.androidx.paging.common)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
