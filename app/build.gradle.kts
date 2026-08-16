import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import java.util.Properties

plugins {
    id("buyornot.android.application")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.appdistribution)
    alias(libs.plugins.firebase.perf)
}

val localProperties =
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { load(it) }
        }
    }

fun gradlePropertyOrNull(key: String): String? = providers.gradleProperty(key).orNull?.takeUnless { it.isBlank() }

/**
 * 앱 링크로 가로챌 host. 비밀값이 아니고, 누락되면 앱 링크가 «조용히» 안 되는 값이라
 * local.properties가 아니라 빌드 스크립트에 상수로 둔다.
 *
 * 도메인은 하이픈이 있는 buy-or-not, 패키지는 buyornot으로 표기가 다르다.
 * 오타를 내면 빌드는 성공하고 검증만 실패하므로 여기 한 곳에서만 정의한다.
 *
 * 이 값과 짝이 되는 assetlinks.json이 각 host에 호스팅돼 있어야 autoVerify가 통과한다.
 */
val appLinkHostProd = "buy-or-not.com"
val appLinkHostDev = "dev.buy-or-not.com"

val firebaseDistributionTesters = gradlePropertyOrNull("firebaseAppDistributionTesters")
val firebaseDistributionGroups = gradlePropertyOrNull("firebaseAppDistributionGroups")
val firebaseDistributionReleaseNotes = gradlePropertyOrNull("firebaseAppDistributionReleaseNotes")

android {
    namespace = "com.sseotdabwa.buyornot"

    defaultConfig {
        applicationId = "com.sseotdabwa.buyornot"
        versionCode = 11
        versionName = "0.3.4"

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
            buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"${localProperties.getProperty("kakao.nativeAppKeyDebug", "")}\"")
            manifestPlaceholders["NATIVE_APP_KEY"] = localProperties.getProperty("kakao.nativeAppKeyDebug", "")
            manifestPlaceholders["appLinkHost"] = appLinkHostDev
            buildConfigField("String", "APP_LINK_HOST", "\"$appLinkHostDev\"")
            firebaseAppDistribution {
                artifactType = "APK"
                firebaseDistributionReleaseNotes?.let { releaseNotes = it }
                firebaseDistributionTesters?.let { testers = it }
                firebaseDistributionGroups?.let { groups = it }
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
            manifestPlaceholders["appLinkHost"] = appLinkHostProd
            buildConfigField("String", "APP_LINK_HOST", "\"$appLinkHostProd\"")
            firebaseAppDistribution {
                artifactType = "APK"
                firebaseDistributionReleaseNotes?.let { releaseNotes = it }
                firebaseDistributionTesters?.let { testers = it }
                firebaseDistributionGroups?.let { groups = it }
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(projects.domain)
    implementation(projects.core.analytics)
    implementation(projects.core.common)
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
    implementation(libs.androidx.metrics.performance)
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
    implementation(libs.firebase.performance)

    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
