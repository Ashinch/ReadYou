buildscript {
    dependencies {
        classpath(libs.daggerHiltPlugin)
    }
}

plugins {
    id("com.android.application") version libs.versions.androidGradlePlugin.get() apply false
    id("com.android.library") version libs.versions.androidGradlePlugin.get() apply false
    id("org.jetbrains.kotlin.android") version libs.versions.kotlin.get() apply false
    id("com.mikepenz.aboutlibraries.plugin") version libs.versions.aboutLibsRelease.get() apply false
    id("com.google.devtools.ksp") version libs.versions.ksp.get() apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}