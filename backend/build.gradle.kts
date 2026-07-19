plugins {
    kotlin("jvm") version "2.2.10"
    application
}

group = "com.example.mozicbackend"
version = "0.1.0"

val ktorVersion = "3.5.1"

// Catalog/social/auth REST is served by Supabase now (see backend/supabase/) —
// this module is just a placeholder for C3's future WebSocket chat server, so
// dependencies are trimmed to the bare minimum until that work starts. Add
// content-negotiation/serialization/websockets/etc. back then, not before.
dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
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
