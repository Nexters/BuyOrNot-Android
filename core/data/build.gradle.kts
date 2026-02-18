plugins {
    id("buyornot.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.sseotdabwa.buyornot.core.data"
}

dependencies {
    implementation(projects.domain)
    implementation(projects.core.common)
    implementation(projects.core.network)
    implementation(projects.core.datastore)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
