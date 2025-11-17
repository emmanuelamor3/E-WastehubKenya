plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)

}

group = "com.example.e_wastehubkenya"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.example.e_wastehubkenya.ApplicationKt")
}

dependencies {
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.logback.classic)
    testImplementation(libs.ktor.server.tests.jvm)
    testImplementation(libs.kotlin.test.junit)
}
