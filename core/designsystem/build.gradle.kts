plugins {
    alias(libs.plugins.mozic.android.library)
    alias(libs.plugins.mozic.android.library.compose)
}

android {
    namespace = "com.example.mozic.core.designsystem"
}

dependencies {
    // Re-export Material 3 + icons so consumers get the theme surface transitively.
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.ui.tooling.preview)
}
