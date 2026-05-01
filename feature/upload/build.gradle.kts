plugins {
    id("buyornot.android.feature")
}

android {
    namespace = "com.sseotdabwa.buyornot.feature.upload"
}

dependencies {
    implementation(projects.core.analytics)
    implementation(projects.core.common)
    implementation(projects.domain)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(libs.coil.compose)
    implementation(libs.android.image.cropper)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
}
