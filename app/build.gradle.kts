plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "ng.com.chprbn.mobile"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "ng.com.chprbn.mobile"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // TODO: when a staging API exists, change this to the staging base URL.
            // Both build types point at prod today because there is no staging.
            buildConfigField(
                "String",
                "BASE_URL",
                "\"https://app.chprbn.gov.ng/api/v1/mobile/\""
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField(
                "String",
                "BASE_URL",
                "\"https://app.chprbn.gov.ng/api/v1/mobile/\""
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Kover plugin is applied so we can wire up coverage reporting once AGP 9.x is
// supported by Kover. As of Kover 0.9.1 + AGP 9.2.1, custom-variant inclusion
// (`add("debug")`) cannot resolve the Android variant, so reports return
// "No sources". Revisit when Kover ships AGP 9 support, or downgrade AGP.
// kover { currentProject { createVariant("appDebug") { add("debug") } } }

// Domain-layer import enforcement.
//
// Files under core/domain/ and feature/<name>/domain/ must stay framework-free:
// no Room, Retrofit, Gson, Android, Compose, and no leakage into a feature's
// data/ package. This task scans those source files and fails the build if any
// forbidden import line is present. Cheap to run, no external rule engine
// (Detekt) required.
val verifyDomainImports by tasks.registering {
    group = "verification"
    description = "Fails the build if domain/** Kotlin files import forbidden packages."

    val domainSources = fileTree(layout.projectDirectory.dir("src/main/java")) {
        include(
            "**/feature/*/domain/**/*.kt",
            "**/core/domain/**/*.kt"
        )
    }
    inputs.files(domainSources)

    doLast {
        val forbidden = listOf(
            Regex("""^\s*import\s+ng\.com\.chprbn\.mobile\.feature\.[^.]+\.data\.""")
                to "feature.<name>.data.*  (data layer)",
            Regex("""^\s*import\s+androidx\.room\.""")          to "androidx.room.*",
            Regex("""^\s*import\s+retrofit2\.""")               to "retrofit2.*",
            Regex("""^\s*import\s+com\.google\.gson\.""")       to "com.google.gson.*",
            Regex("""^\s*import\s+android\.""")                 to "android.*",
            Regex("""^\s*import\s+androidx\.compose\.""")       to "androidx.compose.*"
        )

        val violations = mutableListOf<String>()
        domainSources.forEach { file ->
            file.useLines { lines ->
                lines.forEachIndexed { idx, line ->
                    forbidden.forEach { (pattern, label) ->
                        if (pattern.containsMatchIn(line)) {
                            violations += "${file.relativeTo(rootDir)}:${idx + 1}  forbidden import [$label]  →  ${line.trim()}"
                        }
                    }
                }
            }
        }
        if (violations.isNotEmpty()) {
            throw GradleException(
                "Domain-layer import violations (see docs/exam-assessment-clean-architecture-plan.md §5.4):\n" +
                    violations.joinToString(separator = "\n")
            )
        }
    }
}

tasks.named("check") { dependsOn(verifyDomainImports) }

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.hilt.navigation.compose)

    // Security
    implementation(libs.androidx.security.crypto)
    implementation(libs.sqlcipher)
    implementation(libs.androidx.sqlite.ktx)

    // Coil
    implementation(libs.coil.compose)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.compose.foundation)
    ksp(libs.hilt.compiler)

    // WorkManager + Hilt integration (background sync engine)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)


    // CameraX + ML Kit for QR scanning
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.barcode.scanning)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}