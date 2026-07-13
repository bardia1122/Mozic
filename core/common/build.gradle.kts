plugins {
    alias(libs.plugins.mozic.android.library)
    alias(libs.plugins.mozic.android.hilt)
}

android {
    namespace = "com.example.mozic.core.common"
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
