plugins {
    alias(libs.plugins.mozic.android.library)
    alias(libs.plugins.mozic.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.mozic.core.network"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:common"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)

    // Ktor client is added here by Person C in C2 (:core:network).

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
