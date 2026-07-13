plugins {
    alias(libs.plugins.mozic.android.library)
    alias(libs.plugins.mozic.android.hilt)
}

android {
    namespace = "com.example.mozic.core.media"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:common"))

    implementation(libs.kotlinx.coroutines.android)

    // Media3 / ExoPlayer is added here by Person A in A1 (:core:media).

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
