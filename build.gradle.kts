// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    // Android & Kotlin plugins using the version catalog
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// Proper Kotlin DSL syntax for defining tasks
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
