plugins {
    id("com.android.library") // todo: use build-logic
    id("com.kafka.compose")
    id("com.kafka.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.base.domain)
                implementation(projects.data.repo)
                implementation(projects.ui.common)

                api(compose.animation)
                api(compose.foundation)
                api(compose.material3)
                api(compose.components.resources)
                api(compose.runtime)
                api(compose.uiTooling)
                api(compose.ui)

                implementation(libs.haze)
                implementation(libs.haze.materials)
            }
        }

        val jvmCommon by creating {
            dependsOn(commonMain)
        }

        val jvmMain by getting {
            dependsOn(jvmCommon)
        }

        val androidMain by getting {
            dependsOn(jvmCommon)

            dependencies {
                implementation(projects.ui.downloader)

                implementation(libs.lottie.compose)
                implementation(libs.lottie.core)
            }
        }
    }
}

android {
    namespace = "com.kafka.ui.components"
}
