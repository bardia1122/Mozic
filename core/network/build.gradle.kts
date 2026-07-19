plugins {
    alias(libs.plugins.mozic.android.library)
    alias(libs.plugins.mozic.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.mozic.core.network"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        // Supabase's "publishable" (anon) key is designed to ship inside client
        // apps — Row Level Security policies (backend/supabase/schema.sql), not
        // secrecy, gate what it can read/write. The secret/service_role key
        // (which bypasses RLS) stays server-side in backend/.env and never
        // appears here.
        buildConfigField("String", "SUPABASE_URL", "\"https://ktwzmigxumrpblamerzw.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"sb_publishable_dmw6Mu50sUCob3rlS23ZSg_xyiHiUJL\"")
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:common"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.paging.common)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
