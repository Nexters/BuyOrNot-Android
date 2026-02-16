plugins {
    id("buyornot.android.feature")
}

android {
    namespace = "com.sseotdabwa.buyornot.feature.notification"
}

dependencies {
    implementation(projects.domain)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.unit)
}
