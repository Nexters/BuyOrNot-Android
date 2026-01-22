package com.sseotdabwa.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureHiltAndroid() {
    with(pluginManager) {
        apply("com.google.devtools.ksp")
        apply("com.google.dagger.hilt.android")
    }

    dependencies {
        add("implementation", libs.findLibrary("hilt.android").get())
        add("ksp", libs.findLibrary("hilt.compiler").get())
    }
}
