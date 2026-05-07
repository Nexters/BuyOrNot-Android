import java.util.Properties

plugins {
    id("buyornot.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val localProperties =
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { load(it) }
        }
    }

android {
    namespace = "com.sseotdabwa.buyornot.core.analytics"

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "MIXPANEL_TOKEN", "\"\"")
        }
        release {
            buildConfigField(
                "String",
                "MIXPANEL_TOKEN",
                "\"${localProperties.getProperty("mixpanel.token", "")}\"",
            )
        }
    }
}

dependencies {
    implementation(libs.mixpanel.android)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
