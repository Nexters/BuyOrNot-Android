plugins {
    id("buyornot.android.feature")
    id("buyornot.android.screenshot")
}

android {
    namespace = "com.sseotdabwa.buyornot.feature.mypage"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.domain)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(libs.coil.compose)
    implementation(libs.kakao.user)
    implementation(libs.androidx.credentials)
}
