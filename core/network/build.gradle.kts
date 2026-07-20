import java.util.Properties

plugins {
    alias(libs.plugins.mozic.android.library)
    alias(libs.plugins.mozic.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// C5: the WS chat server (backend/'s Ktor project) isn't deployed anywhere
// phone-reachable — unlike Supabase, it only runs where a teammate starts it
// (`cd backend && ./gradlew run`), reachable at whatever LAN IP that machine
// has that session. That's a per-developer, changes-every-WiFi-reconnect
// value, so it belongs in local.properties (gitignored, machine-specific),
// not hardcoded here next to the stable Supabase URL/key above. Falls back to
// the emulator's host-loopback alias if unset.
val localProperties =
    Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) file.inputStream().use(::load)
    }
val chatWsHost = localProperties.getProperty("CHAT_WS_HOST") ?: "10.0.2.2:8080"

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
        buildConfigField("String", "CHAT_WS_HOST", "\"$chatWsHost\"")
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
    implementation(libs.ktor.client.websockets)

    // ProcessLifecycleOwner — the chat socket only needs to run while the app
    // is foregrounded (CLAUDE_PERSON_C.md §5's C5 plan).
    implementation(libs.androidx.lifecycle.process)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
