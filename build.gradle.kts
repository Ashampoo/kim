import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform") version "2.2.0"
    id("com.android.library") version "8.9.3"
    id("signing")
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
    id("com.asarkar.gradle.build-time-tracker") version "5.0.1"
    id("me.qoomon.git-versioning") version "6.4.4"
    id("com.goncalossilva.resources") version "0.10.1"
    id("com.github.ben-manes.versions") version "0.52.0"
    id("com.vanniktech.maven.publish") version "0.34.0"
}

repositories {
    google()
    mavenCentral()
}

val productName: String = "Ashampoo Kim"

val ktorVersion: String = "3.2.2"
val xmpCoreVersion: String = "1.5.2"
val dateTimeVersion: String = "0.7.1"
val kotlinxIoVersion: String = "0.8.0"

description = productName
group = "com.ashampoo"
version = "0.0.0"

gitVersioning.apply {

    refs {
        /* Main branch contains the current dev version */
        branch("main") {
            version = "\${commit.short}"
        }
        /* Release / tags have real version numbers */
        tag("v(?<version>.*)") {
            version = "\${ref.version}"
        }
    }

    /* Fallback if branch was not found (for feature branches) */
    rev {
        version = "\${commit.short}"
    }
}

apply(plugin = "io.gitlab.arturbosch.detekt")

buildTimeTracker {
    sortBy.set(com.asarkar.gradle.buildtimetracker.Sort.DESC)
}

detekt {
    source.from("src", "build.gradle.kts")
    allRules = true
    config.setFrom("$projectDir/detekt.yml")
    parallel = true
    ignoreFailures = true
    autoCorrect = true
}

kover {
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
}

kotlin {

    explicitApi()

    androidTarget {

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }

        publishLibraryVariants("release")
    }

    mingwX64("win") {
        binaries {
            executable(setOf(NativeBuildType.RELEASE)) {
                baseName = "kim"
                entryPoint = "com.ashampoo.kim.main"
            }
            staticLib(namePrefix = "", setOf(NativeBuildType.RELEASE)) {
                baseName = "kim"
            }
        }
    }

    linuxX64 {
        binaries {
            executable(setOf(NativeBuildType.RELEASE)) {
                entryPoint = "com.ashampoo.kim.main"
            }
            staticLib(namePrefix = "", setOf(NativeBuildType.RELEASE)) {
                baseName = "kim"
            }
        }
    }

    linuxArm64 {
        binaries {
            executable(setOf(NativeBuildType.RELEASE)) {
                entryPoint = "com.ashampoo.kim.main"
            }
            staticLib(namePrefix = "", setOf(NativeBuildType.RELEASE)) {
                baseName = "kim"
            }
        }
    }

    jvm {

        java {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }

    js()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs()

//    @OptIn(ExperimentalWasmDsl::class)
//    wasmWasi()

    @Suppress("UnusedPrivateMember") // False positive
    val commonMain by sourceSets.getting {

        dependencies {

            /* Date handling */
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:$dateTimeVersion")

            /* XMP handling */
            api("com.ashampoo:xmpcore:$xmpCoreVersion")
        }
    }

    @Suppress("UnusedPrivateMember") // False positive
    val commonTest by sourceSets.getting {
        dependencies {

            /* Kotlin Test */
            implementation(kotlin("test"))

            implementation("org.jetbrains.kotlinx:kotlinx-io-core:$kotlinxIoVersion")
        }
    }

    val xcf = XCFramework()

    listOf(
        /* App Store */
        iosArm64(),
        /* Apple Intel iOS Simulator */
        iosX64(),
        /* Apple Silicon iOS Simulator */
        iosSimulatorArm64(),
        /* macOS Devices */
        macosX64(),
        macosArm64()
    ).forEach {

        it.binaries.executable(setOf(NativeBuildType.RELEASE)) {
            baseName = "kim"
            entryPoint = "com.ashampoo.kim.main"
        }

        it.binaries.framework(
            buildTypes = setOf(NativeBuildType.RELEASE)
        ) {
            baseName = "kim"
            /* Part of the XCFramework */
            xcf.add(this)
        }
    }

    /*
     * Extra sourceSet to exclude unsupported features from JS / wasmJS targets.
     */
    val ktorMain by sourceSets.creating {

        dependsOn(commonMain)

        dependencies {

            /*
             * Ktor extensions
             *
             * Not available in commonMain due to missing WASM support.
             */
            api("io.ktor:ktor-io:$ktorVersion")

            /*
             * Multiplatform file access
             *
             * Not available in commonMain due to missing JS browser support.
             */
            api("org.jetbrains.kotlinx:kotlinx-io-core:$kotlinxIoVersion")
        }
    }

    val posixMain by sourceSets.creating {

        dependsOn(commonMain)
        dependsOn(ktorMain)
    }

    @Suppress("UnusedPrivateMember") // False positive
    val jvmMain by sourceSets.getting {

        dependsOn(commonMain)
        dependsOn(ktorMain)
    }

    @Suppress("UnusedPrivateMember") // False positive
    val androidMain by sourceSets.getting {

        dependsOn(commonMain)
        dependsOn(ktorMain)
    }

    @Suppress("UnusedPrivateMember") // False positive
    val winMain by sourceSets.getting {
        dependsOn(posixMain)
    }

    @Suppress("UnusedPrivateMember") // False positive
    val linuxX64Main by sourceSets.getting {
        dependsOn(posixMain)
    }

    @Suppress("UnusedPrivateMember") // False positive
    val linuxArm64Main by sourceSets.getting {
        dependsOn(posixMain)
    }

    val iosArm64Main by sourceSets.getting
    val iosX64Main by sourceSets.getting
    val iosSimulatorArm64Main by sourceSets.getting
    val macosX64Main by sourceSets.getting
    val macosArm64Main by sourceSets.getting

    @Suppress("UnusedPrivateMember") // False positive
    val appleMain by sourceSets.creating {

        dependsOn(commonMain)
        dependsOn(ktorMain)
        dependsOn(posixMain)

        iosArm64Main.dependsOn(this)
        iosX64Main.dependsOn(this)
        iosSimulatorArm64Main.dependsOn(this)
        macosX64Main.dependsOn(this)
        macosArm64Main.dependsOn(this)
    }

    val iosArm64Test by sourceSets.getting
    val iosX64Test by sourceSets.getting
    val iosSimulatorArm64Test by sourceSets.getting
    val macosX64Test by sourceSets.getting
    val macosArm64Test by sourceSets.getting

    @Suppress("UnusedPrivateMember") // False positive
    val appleTest by sourceSets.creating {

        dependsOn(commonTest)

        iosArm64Test.dependsOn(this)
        iosX64Test.dependsOn(this)
        iosSimulatorArm64Test.dependsOn(this)
        macosX64Test.dependsOn(this)
        macosArm64Test.dependsOn(this)
    }

    @Suppress("UnusedPrivateMember") // False positive
    val jsMain by sourceSets.getting {

        dependsOn(commonMain)

        dependencies {
            api(npm("pako", "2.1.0"))
        }
    }

    val wasmJsMain by sourceSets.getting
    // val wasmWasiMain by sourceSets.getting

    @Suppress("UnusedPrivateMember") // False positive
    val wasmMain by sourceSets.creating {

        dependsOn(commonMain)

        wasmJsMain.dependsOn(this)
        // wasmWasiMain.dependsOn(this)

        dependencies {

            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.3")

            implementation(npm("pako", "2.1.0"))
        }
    }
}

// region Writing version.txt for GitHub Actions
val writeVersion: TaskProvider<Task> = tasks.register("writeVersion") {
    doLast {
        File("build/version.txt").writeText(project.version.toString())
    }
}

tasks.getByPath("build").finalizedBy(writeVersion)
// endregion

// region Android setup
android {

    namespace = "com.ashampoo.kim"

    // For API 35 tests fail. Something seems incompatible.
    compileSdk = 34

    sourceSets["main"].res.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}
// endregion

// region Maven publish

val signingEnabled: Boolean = System.getenv("SIGNING_ENABLED")?.toBoolean() ?: false

mavenPublishing {

    publishToMavenCentral()

    if (signingEnabled)
        signAllPublications()

    coordinates(
        groupId = "com.ashampoo",
        artifactId = "kim",
        version = version.toString()
    )

    pom {

        name = productName
        description = "Kotlin Multiplatform library for image metadata manipulation"
        url = "https://github.com/Ashampoo/kim"

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }

        developers {
            developer {
                name = "Ashampoo GmbH & Co. KG"
                url = "https://www.ashampoo.com/"
            }
        }

        scm {
            url = "https://github.com/Ashampoo/kim"
            connection = "scm:git:git://github.com/Ashampoo/kim.git"
        }
    }
}
// endregion
