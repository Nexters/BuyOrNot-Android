plugins {
    id("buyornot.android.feature")
}

android {
    namespace = "com.sseotdabwa.buyornot.feature.mypage"
}

dependencies {
    implementation(projects.domain)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
}
