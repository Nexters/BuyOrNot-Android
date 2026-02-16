plugins {
    id("buyornot.android.library")
}

android {
    namespace = "com.sseotdabwa.buyornot.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
