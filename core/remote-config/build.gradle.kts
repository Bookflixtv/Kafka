@Suppress("DSL_SCOPE_VIOLATION") // Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.cacheFixPlugin)
}

android {
    namespace = "com.kafka.remote.config"
}

dependencies {
    implementation(projects.base.domain)

    implementation(libs.androidx.core)

    implementation(libs.kotlin.serialization)

    implementation(platform(libs.google.bom))
    implementation(libs.google.remoteConfig)
}
