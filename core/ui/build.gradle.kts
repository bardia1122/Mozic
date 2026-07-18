plugins {
    alias(libs.plugins.mozic.android.library)
    alias(libs.plugins.mozic.android.library.compose)
}

android {
    namespace = "com.example.mozic.core.ui"
}

dependencies {
    api(project(":core:designsystem"))
    implementation(project(":core:domain"))

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation)
}
