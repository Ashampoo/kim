import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform") version "2.0.21"
    id("com.android.library") version "8.5.0"
    id("maven-publish")
    id("signing")
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
    id("org.sonarqube") version "4.3.1.3277"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
    id("com.asarkar.gradle.build-time-tracker") version "4.3.0"
    id("me.qoomon.git-versioning") version "6.4.4"
    id("com.goncalossilva.resources") version "0.9.0"
    id("com.github.ben-manes.versions") version "0.51.0"
    id("org.jetbrains.dokka") version "1.9.20"
}

repositories {
    google()
    mavenCentral()
}

val productName: String = "Ashampoo Kim"

val ktorVersion: String = "3.0.1"
val xmpCoreVersion: String = "1.4.2"
val dateTimeVersion: String = "0.6.1"
val kotlinxIoVersion: String = "0.6.0"

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
apply(plugin = "org.sonarqube")
apply(plugin = "kover")

buildTimeTracker {
    sortBy.set(com.asarkar.gradle.buildtimetracker.Sort.DESC)
}

sonar {
    properties {

        property("sonar.projectKey", "kim")
        property("sonar.projectName", productName)
        property("sonar.organization", "ashampoo")
        property("sonar.host.url", "https://sonarcloud.io")

        property(
            "sonar.sources",
            listOf(
                "./src/androidMain/kotlin",
                "./src/appleMain/kotlin",
                "./src/commonMain/kotlin",
                "./src/jvmMain/kotlin",
                "./src/posixMain/kotlin"
            )
        )
        property(
            "sonar.tests",
            listOf(
                "./src/commonTest/kotlin"
            )
        )

        property("sonar.android.lint.report", "build/reports/lint-results.xml")
        property("sonar.kotlin.detekt.reportPaths", "build/reports/detekt/detekt.xml")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/kover/xml/report.xml")
    }
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

koverMerged {
    xmlReport {
    }
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
}

kotlin {

    explicitApi()

    androidTarget {

        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
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

    js(IR) {

        moduleName = "kim-js"

        browser()
        nodejs()

        binaries.library()
        binaries.executable()

        compilations.all {
            compileTaskProvider.configure {
                compilerOptions.freeCompilerArgs.add("-Xir-minimized-member-names=false")
            }
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {

        moduleName = "kim-wasm"

        browser()
        nodejs()

        binaries.library()
        binaries.executable()
    }

    // WASI support is planned for kotlinx-datetime v0.7
    // @OptIn(ExperimentalWasmDsl::class)
    // wasmWasi()

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

ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
ext["signing.secretKeyRingFile"] = "secring.pgp"
ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

val signingEnabled: Boolean = System.getenv("SIGNING_ENABLED")?.toBoolean() ?: false

afterEvaluate {

    if (signingEnabled) {

        /*
         * Explicitly configure that signing comes before publishing.
         * Otherwise the task execution of "publishAllPublicationsToSonatypeRepository" will fail.
         */

        val signJvmPublication by tasks.getting
        val signAndroidReleasePublication by tasks.getting
        val signIosArm64Publication by tasks.getting
        val signIosX64Publication by tasks.getting
        val signIosSimulatorArm64Publication by tasks.getting
        val signMacosArm64Publication by tasks.getting
        val signMacosX64Publication by tasks.getting
        val signWinPublication by tasks.getting
        val signLinuxX64Publication by tasks.getting
        val signLinuxArm64Publication by tasks.getting
        val signJsPublication by tasks.getting
        val signWasmJsPublication by tasks.getting
        // val signWasmWasiPublication by tasks.getting
        val signKotlinMultiplatformPublication by tasks.getting

        val publishJvmPublicationToSonatypeRepository by tasks.getting
        val publishAndroidReleasePublicationToSonatypeRepository by tasks.getting
        val publishIosArm64PublicationToSonatypeRepository by tasks.getting
        val publishIosX64PublicationToSonatypeRepository by tasks.getting
        val publishIosSimulatorArm64PublicationToSonatypeRepository by tasks.getting
        val publishMacosArm64PublicationToSonatypeRepository by tasks.getting
        val publishMacosX64PublicationToSonatypeRepository by tasks.getting
        val publishWinPublicationToSonatypeRepository by tasks.getting
        val publishLinuxX64PublicationToSonatypeRepository by tasks.getting
        val publishLinuxArm64PublicationToSonatypeRepository by tasks.getting
        val publishJsPublicationToSonatypeRepository by tasks.getting
        val publishWasmJsPublicationToSonatypeRepository by tasks.getting
        // val publishWasmWasiPublicationToSonatypeRepository by tasks.getting
        val publishKotlinMultiplatformPublicationToSonatypeRepository by tasks.getting
        val publishAllPublicationsToSonatypeRepository by tasks.getting

        val signTasks = listOf(
            signJvmPublication, signAndroidReleasePublication,
            signIosArm64Publication, signIosX64Publication,
            signIosSimulatorArm64Publication,
            signMacosArm64Publication, signMacosX64Publication,
            signWinPublication, signLinuxX64Publication, signLinuxArm64Publication,
            signJsPublication, signWasmJsPublication, // signWasmWasiPublication,
            signKotlinMultiplatformPublication
        )

        val publishTasks = listOf(
            publishJvmPublicationToSonatypeRepository,
            publishAndroidReleasePublicationToSonatypeRepository,
            publishIosArm64PublicationToSonatypeRepository,
            publishIosX64PublicationToSonatypeRepository,
            publishIosSimulatorArm64PublicationToSonatypeRepository,
            publishMacosArm64PublicationToSonatypeRepository,
            publishMacosX64PublicationToSonatypeRepository,
            publishWinPublicationToSonatypeRepository,
            publishLinuxX64PublicationToSonatypeRepository,
            publishLinuxArm64PublicationToSonatypeRepository,
            publishJsPublicationToSonatypeRepository,
            publishWasmJsPublicationToSonatypeRepository,
            // publishWasmWasiPublicationToSonatypeRepository,
            publishKotlinMultiplatformPublicationToSonatypeRepository,
            publishAllPublicationsToSonatypeRepository
        )

        /* Each publish task depenends on every sign task. */
        for (publishTask in publishTasks)
            for (signTask in signTasks)
                publishTask.dependsOn(signTask)
    }
}

fun getExtraString(name: String): String? = ext[name]?.toString()

publishing {
    publications {

        // Configure maven central repository
        repositories {
            maven {
                name = "sonatype"
                setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = getExtraString("ossrhUsername")
                    password = getExtraString("ossrhPassword")
                }
            }
        }

        publications.withType<MavenPublication> {

            artifact(javadocJar.get())

            pom {

                name.set(productName)
                description.set("Kotlin Multiplatform library for image metadata manipulation")
                url.set("https://github.com/Ashampoo/kim")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        name.set("Ashampoo GmbH & Co. KG")
                        url.set("https://www.ashampoo.com/")
                    }
                }

                scm {
                    connection.set("https://github.com/Ashampoo/kim.git")
                    url.set("https://github.com/Ashampoo/kim")
                }
            }
        }

        if (signingEnabled) {

            signing {
                sign(publishing.publications)
            }
        }
    }
}
// endregion
