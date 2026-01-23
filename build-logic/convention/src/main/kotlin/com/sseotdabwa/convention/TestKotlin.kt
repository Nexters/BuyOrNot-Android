package com.sseotdabwa.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureTestKotlin() {
    dependencies {
        add("testImplementation", libs.findLibrary("junit").get())
        add("testImplementation", libs.findLibrary("kotlin.test").get())
        add("testImplementation", libs.findLibrary("kotlinx.coroutines.test").get())
    }
}
