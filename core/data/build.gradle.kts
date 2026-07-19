plugins {
    alias(libs.plugins.mozic.android.library)
    alias(libs.plugins.mozic.android.hilt)
}

android {
    namespace = "com.example.mozic.core.data"
}

// Export Room schemas so migrations are reviewable in version control.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.paging.runtime)

    // DownloadWorker is a plain CoroutineWorker constructed by hand via
    // DownloadWorkerFactory (see that class's kdoc) — no androidx.hilt-work
    // codegen dependency needed.
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.okhttp)

    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
