import java.util.Properties

plugins {
    id("buyornot.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties =
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { load(it) }
        }
    }

android {
    namespace = "com.sseotdabwa.buyornot.core.network"

    buildFeatures {
        buildConfig = true
    }

    val debugBaseUrl = localProperties.getProperty("debug.base.url")
    val releaseBaseUrl = localProperties.getProperty("release.base.url")

    buildTypes {
        debug {
            val url =
                requireNotNull(debugBaseUrl) {
                    "local.properties에 'debug.base.url'이 설정되지 않았습니다."
                }
            buildConfigField("String", "BASE_URL", "\"$url\"")
        }
        release {
            val url =
                requireNotNull(releaseBaseUrl) {
                    "local.properties에 'release.base.url'이 설정되지 않았습니다."
                }
            buildConfigField("String", "BASE_URL", "\"$url\"")
        }
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.datastore)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
