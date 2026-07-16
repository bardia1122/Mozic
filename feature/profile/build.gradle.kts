plugins {
    alias(libs.plugins.mozic.android.feature)
}

android {
    namespace = "com.example.mozic.feature.profile"
}

dependencies {
    // Photo-picker launcher for the avatar picker (rememberLauncherForActivityResult).
    implementation(libs.androidx.activity.compose)
    // Without an explicit request here, this module's dependency graph has
    // nothing pinning androidx.core:core-ktx above activity-compose's own
    // transitive minimum, which failed to resolve (see PROGRESS.md B6 notes).
    implementation(libs.androidx.core.ktx)
}
