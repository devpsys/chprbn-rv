import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
    id("com.google.dagger.hilt.android")
}

// Release-signing config is loaded lazily from `keystore.properties` at the
// project root (gitignored). When the file is absent — fresh clone, CI without
// secrets, dev who only builds debug — the release build type is left
// unsigned: `:assembleRelease` still produces an APK at
// `app/build/outputs/apk/release/app-release-unsigned.apk`, suitable for an
// `adb install -t` smoke test. Play uploads require the signed variant, which
// only succeeds when the keystore is present. See `keystore.properties.example`.
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) {
        keystorePropsFile.inputStream().use { load(it) }
    }
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

    signingConfigs {
        if (keystorePropsFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
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
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
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

    // Opt in to Kotlin's upcoming default for constructor-parameter
    // annotations: apply to both the value parameter AND the generated
    // property/field. Silences ~7 transitional warnings at @Inject /
    // @TypeConverters sites. Harmless: Hilt only reads param-level @Inject,
    // and Room's @TypeConverters at a backing field is equivalent to at the
    // value parameter for our cases. See KT-73255.
    kotlin {
        compilerOptions {
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
    }

    testOptions {
        // Compose UI Test + Robolectric need real Android resources (R.* IDs,
        // strings, drawables) on the unit-test classpath. Without this,
        // `stringResource(R.string.…)` blows up at test time.
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    packaging {
        resources {
            // Robolectric drags in JUnit 5 jars which each ship META-INF/LICENSE.md
            // / NOTICE.md; the androidTest APK merge fails on the collision.
            // Excluding the duplicates is the standard fix.
            excludes += listOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/NOTICE.md",
            )
        }
    }
}

// Room schema export — required for MigrationTestHelper-based migration tests.
// Schemas are committed under `app/schemas/<DatabaseFqn>/<version>.json` and act
// as the immutable historical record each `Migration(n, n+1)` is validated
// against. Without this directive Room cannot reconstruct the prior version
// during tests.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
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
    // Render-snapshot tests (P3-3): real Compose tree rendering on the JVM
    // via Robolectric, with semantic assertions from compose-ui-test. Picks
    // up @Preview-style state and verifies the screen renders without
    // crashing and surfaces the expected user-visible strings. Stand-in
    // for pixel-based snapshots until Roborazzi / AGP previewScreenshot
    // catches up to AGP 9.x.
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.mockk.android)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}