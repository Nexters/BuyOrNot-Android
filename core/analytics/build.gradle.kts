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
            buildConfigField("boolean", "SEND_TO_MIXPANEL", "false")
            buildConfigField("String", "MIXPANEL_TOKEN", "\"\"")
        }
        create("qa") {
            initWith(getByName("debug"))
            buildConfigField("boolean", "SEND_TO_MIXPANEL", "true")
            // QA 전용 Mixpanel 프로젝트가 정해지기 전까지는 운영 토큰으로 보낸다.
            buildConfigField(
                "String",
                "MIXPANEL_TOKEN",
                "\"${localProperties.getProperty("mixpanel.qa.token") ?: localProperties.getProperty("mixpanel.token", "")}\"",
            )
        }
        release {
            buildConfigField("boolean", "SEND_TO_MIXPANEL", "true")
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

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.performance)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // android.jar의 org.json은 유닛테스트에서 Stub!을 던지므로 실제 구현체를 테스트에만 넣는다.
    testImplementation(libs.json)
}
