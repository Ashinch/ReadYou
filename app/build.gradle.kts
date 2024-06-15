import java.util.Properties
import java.io.FileInputStream

plugins {
alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.room)
    alias(libs.plugins.hilt)
}

fun fetchGitCommitHash(): String {
    val process = ProcessBuilder("git", "rev-parse", "--verify", "--short", "HEAD")
        .redirectErrorStream(true)
        .start()
    return process.inputStream.bufferedReader().use { it.readText().trim() }
}

val gitCommitHash = fetchGitCommitHash()
val keyProps = Properties()
val keyPropsFile: File = rootProject.file("signature/keystore.properties")
if (keyPropsFile.exists()) {
    println("Loading keystore properties from ${keyPropsFile.absolutePath}")
    keyProps.load(FileInputStream(keyPropsFile))
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "me.ash.reader"
        minSdk = 26
        targetSdk = 33
        versionCode = 25
        versionName = "0.10.1"

        buildConfigField("String", "USER_AGENT_STRING", "\"ReadYou/${'$'}{versionName}(${versionCode})\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        room {
            schemaDirectory("$projectDir/schemas")
        }

        ksp {
            arg("room.incremental","true")
        }
    }

    flavorDimensions.add("channel")
    productFlavors {
        create("github") {
            dimension = "channel"
        }
        create("fdroid") {
            dimension = "channel"
        }
        create("googlePlay") {
            dimension = "channel"
            applicationIdSuffix = ".google.play"
        }
    }
    signingConfigs {
        create("release") {
            keyAlias = keyProps["keyAlias"] as String?
            keyPassword = keyProps["keyPassword"] as String?
            storeFile = keyProps["storeFile"]?.let { file(it as String) }
            storePassword = keyProps["storePassword"] as String?
        }
    }
    lint {
        disable.addAll(listOf("MissingTranslation", "ExtraTranslation"))
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName = "ReadYou-${defaultConfig.versionName}-${gitCommitHash}.apk"
        }
    }
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }
    androidResources {
        generateLocaleConfig = true
    }
    namespace = "me.ash.reader"
}

dependencies {
    implementation(libs.aboutLibrariesCore)
    implementation(libs.aboutLibrariesComposeM3)
    implementation(libs.composeHtml)
    implementation(libs.androidSVG)
    implementation(libs.opmlParser)
    implementation(libs.readability4j)
    implementation(libs.rome)
    implementation(libs.coilBase)
    implementation(libs.coilCompose)
    implementation(libs.coilSvg)
    implementation(libs.coilGif)
    implementation(libs.telephoto)
    implementation(libs.okhttp)
    implementation(libs.okhttpCoroutines)
    implementation(libs.retrofit)
    implementation(libs.retrofitGson)
    implementation(libs.profileinstaller)
    implementation(libs.workRuntimeKtx)
    implementation(libs.datastorePreferences)
    implementation(libs.roomPaging)
    implementation(libs.roomCommon)
    implementation(libs.roomKtx)
    ksp(libs.roomCompiler)
    implementation(libs.pagingCommonKtx)
    implementation(libs.pagingRuntimeKtx)
    implementation(libs.pagingCompose)
    implementation(libs.browser)
    implementation(libs.navigationCompose)
    implementation(libs.lifecycleViewModelCompose)
    implementation(libs.lifecycleRuntimeKtx)
    implementation(libs.composeMaterial3)
    implementation(libs.accompanistPager)
    implementation(libs.accompanistFlowlayout)
    implementation(libs.accompanistSwiperefresh)
    implementation(platform(libs.composeBom))
    androidTestImplementation(platform(libs.composeBom))
    implementation(libs.composeAnimationGraphics)
    implementation(libs.composeUi)
    implementation(libs.composeUiUtil)
    implementation(libs.composeMaterial)
    implementation(libs.composeMaterialIconsExtended)
    implementation(libs.composeUiTooling)
    implementation(libs.composeUiToolingPreview)
    androidTestImplementation(libs.composeUiTestJunit4)
    implementation(libs.hiltWork)
    implementation(libs.hiltAndroid)
    ksp(libs.hiltAndroidCompiler)
    ksp(libs.hiltCompiler)
    implementation(libs.hiltNavigationCompose)
    implementation(libs.coreKtx)
    implementation(libs.activityCompose)
    implementation(libs.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.junitExt)
    androidTestImplementation(libs.espressoCore)
    testImplementation(libs.mockitoCore)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(libs.mockitoKotlin)
}