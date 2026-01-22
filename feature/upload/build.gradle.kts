plugins {
    id("buyornot.android.feature")
}

android {
    namespace = "com.sseotdabwa.buyornot.feature.upload"
}

dependencies {
    implementation(projects.domain)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
}