plugins {
    id("buyornot.android.library")
}

android {
    namespace = "com.sseotdabwa.buyornot.core.testing"
}

// 스크린샷 테스트를 켜는 모듈들이 testImplementation 하나로 필요한 걸 모두 받도록 api 로 노출한다.
dependencies {
    api(libs.coil.compose)
    api(libs.roborazzi.compose.preview.scanner.support)
    api(libs.composable.preview.scanner.android)
    api(libs.robolectric)
    api(libs.junit)
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui.test.junit4)
}
