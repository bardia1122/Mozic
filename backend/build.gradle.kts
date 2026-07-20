plugins {
    kotlin("jvm") version "2.2.10"
    kotlin("plugin.serialization") version "2.2.10"
    application
}

group = "com.example.mozicbackend"
version = "0.1.0"

val ktorVersion = "3.5.1"

// C3: the WebSocket chat server. Catalog/social/auth REST is served by
// Supabase directly (see backend/supabase/) — this module's only job is the
// send/ack/read/typing protocol Supabase's Realtime can't provide (see
// PROTOCOL.md). Chat persistence still goes through the same Supabase
// Postgres project, via plain HTTP calls to PostgREST (the ktor-client-*
// deps below), not a JDBC driver — no new infra, same "boring" choice C1 made.
dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")

    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("ch.qos.logback:logback-classic:1.5.38")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.example.mozicbackend.ApplicationKt")
}

tasks.test {
    useJUnitPlatform()
}
