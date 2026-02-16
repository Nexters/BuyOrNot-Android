import java.util.Properties

plugins {
    id("buyornot.android.feature")
}

val localProperties =
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { load(it) }
        }
    }

android {
    namespace = "com.sseotdabwa.buyornot.feature.auth"

    defaultConfig {
        resValue("string", "web_client_id", localProperties.getProperty("google.webClientId", ""))
    }
}

dependencies {
    implementation(projects.domain)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.playservices)
    implementation(libs.kakao.user)
    implementation(libs.googleid)
    implementation(libs.lottie.compose)
}
