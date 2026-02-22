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

    if (debugBaseUrl == null || releaseBaseUrl == null) {
        val missingProps =
            listOfNotNull(
                if (debugBaseUrl == null) "DEBUG_BASE_URL" else null,
                if (releaseBaseUrl == null) "RELEASE_BASE_URL" else null,
            )
        logger.warn("⚠️ Base URL configuration incomplete. Missing properties in local.properties: $missingProps")
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", debugBaseUrl)
        }
        release {
            buildConfigField("String", "BASE_URL", releaseBaseUrl)
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
