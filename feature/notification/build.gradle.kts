plugins {
    id("buyornot.android.feature")
}

android {
    namespace = "com.sseotdabwa.buyornot.feature.notification"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.domain)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.unit)
}
