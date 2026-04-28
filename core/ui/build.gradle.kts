plugins {
    id("buyornot.android.library")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.sseotdabwa.buyornot.core.ui"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
}
