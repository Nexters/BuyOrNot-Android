import java.util.Properties

plugins {
    id("buyornot.android.application")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val localProperties =
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { load(it) }
        }
    }

android {
    namespace = "com.sseotdabwa.buyornot"

    defaultConfig {
        applicationId = "com.sseotdabwa.buyornot"
        versionCode = 4
        versionName = "0.1.0"

        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"${localProperties.getProperty("kakao.nativeAppKey", "")}\"")
        manifestPlaceholders["NATIVE_APP_KEY"] = localProperties.getProperty("kakao.nativeAppKey", "")
    }

    signingConfigs {
        create("release") {
            val storeFilePath = localProperties.getProperty("signed.store.file")
            val storePass = localProperties.getProperty("signed.store.password")
            val keyAliasValue = localProperties.getProperty("signed.key.alias")
            val keyPass = localProperties.getProperty("signed.key.password")

            if (storeFilePath != null && storePass != null && keyAliasValue != null && keyPass != null) {
                storeFile = file(storeFilePath)
                storePassword = storePass
                keyAlias = keyAliasValue
                keyPassword = keyPass
            } else {
                val missingProps =
                    listOfNotNull(
                        if (storeFilePath == null) "signed.store.file" else null,
                        if (storePass == null) "signed.store.password" else null,
                        if (keyAliasValue == null) "signed.key.alias" else null,
                        if (keyPass == null) "signed.key.password" else null,
                    )
                logger.warn("⚠️ Signing config incomplete. Missing properties in local.properties: $missingProps")
                logger.warn("⚠️ Build will use debug signing provided by Android Gradle Plugin.")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            signingConfig = signingConfigs.getByName("release")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(projects.domain)
    implementation(projects.core.data)
    implementation(projects.core.network)
    implementation(projects.core.datastore)
    implementation(projects.core.ui)
    implementation(projects.core.designsystem)
    implementation(projects.feature.auth)
    implementation(projects.feature.home)
    implementation(projects.feature.upload)
    implementation(projects.feature.mypage)
    implementation(projects.feature.notification)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kakao.common)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.crashlytics)

    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
