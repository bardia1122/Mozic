plugins {
    alias(libs.plugins.mozic.android.feature)
}

android {
    namespace = "com.example.mozic.feature.settings"
}

dependencies {
    // AppCompatDelegate.setApplicationLocales — the per-app language API (B1).
    implementation(libs.androidx.appcompat)
}
