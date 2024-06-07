buildscript {
    dependencies {
        classpath(libs.daggerHiltPlugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.aboutlibraries.plugin) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}