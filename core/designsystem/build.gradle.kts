plugins {
    id("buyornot.android.library")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.sseotdabwa.buyornot.core.designsystem"

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            // Roborazzi 권고. 이게 없으면 하드웨어 가속 렌더 경로를 타지 못해 렌더 결과가 달라진다.
            all {
                it.systemProperties["robolectric.pixelCopyRenderMode"] = "hardware"
            }
        }
    }
}

// Roborazzi가 @Preview 컴포저블을 찾아 Robolectric 테스트를 생성한다.
roborazzi {
    generateComposePreviewRobolectricTests {
        enable = true
        packages = listOf("com.sseotdabwa.buyornot.core.designsystem")
        // 이 레포의 프리뷰는 전부 private fun 이다.
        includePrivatePreviews = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.runtime)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.roborazzi.compose.preview.scanner.support)
    testImplementation(libs.composable.preview.scanner.android)
    testImplementation(libs.robolectric)
    testImplementation(libs.junit)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
}
