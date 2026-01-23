package com.sseotdabwa.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureHiltKotlin() {
    with(pluginManager) {
        apply("com.google.devtools.ksp")
    }

    dependencies {
        add("implementation", libs.findLibrary("hilt.core").get())
        add("ksp", libs.findLibrary("hilt.compiler").get())
    }
}
