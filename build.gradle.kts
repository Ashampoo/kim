import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform") version "1.8.20"
    id("com.android.library") version "7.4.2"
    id("maven-publish")
    id("signing")
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
    id("org.sonarqube") version "4.0.0.2929"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
    id("com.asarkar.gradle.build-time-tracker") version "4.3.0"
    id("me.qoomon.git-versioning") version "6.4.1"
}

repositories {
    google()
    mavenCentral()
}

val productName = "Ashampoo Kim"

val ktorVersion: String = "2.3.0"

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
        property("sonar.organization", "realashampoo")
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
                "./src/commonTest/kotlin",
                "./src/jvmTest/kotlin",
            )
        )

        /* Include Android Lint */
        property("sonar.android.lint.report", "${project.buildDir}/reports/lint-results.xml")

        /* Include Detekt issues */
        val detektPath = "${project.buildDir}/reports/detekt/detekt.xml"
        println("Detekt report: $detektPath")
        property("sonar.kotlin.detekt.reportPaths", detektPath)

        /* Include Kover code coverage */
        val koverPath = "${project.buildDir}/reports/kover/xml/report.xml"
        println("Kover report: $koverPath")
        property("sonar.coverage.jacoco.xmlReportPaths", koverPath)
    }
}

detekt {
    source = files("src", "build.gradle.kts")
    allRules = true
    config = files("detekt.yml")
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
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.22.0")
}

kotlin {

    android {

        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }

        publishLibraryVariants("release")
    }

    mingwX64("win") {
        binaries {
            executable(
                buildTypes = setOf(NativeBuildType.RELEASE)
            )
        }
    }

    jvm {

        java {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    @Suppress("UnusedPrivateMember") // False positive
    val commonMain by sourceSets.getting {

        dependencies {

            /* Date handling */
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

            /* Needed for Charset class. */
            /* Defined as api() to prevent problems when used from a pure-java project. */
            api("io.ktor:ktor-io:$ktorVersion")
        }
    }

    @Suppress("UnusedPrivateMember", "UNUSED_VARIABLE") // False positive
    val commonTest by sourceSets.getting {
        dependencies {

            /* Kotlin Test */
            implementation(kotlin("test"))
        }
    }

    val xcf = XCFramework()

    listOf(
        /* App Store */
        iosArm64(),
        /* Apple Silicon iOS Simulator */
        iosSimulatorArm64(),
        /* macOS Devices */
        macosX64(),
        macosArm64()
    ).forEach {

        it.binaries.executable(
            buildTypes = setOf(NativeBuildType.RELEASE)
        ) {
            baseName = "kim"
        }

        it.binaries.framework(
            buildTypes = setOf(NativeBuildType.RELEASE)
        ) {
            baseName = "kim"
            /* Part of the XCFramework */
            xcf.add(this)
        }
    }

    // See https://youtrack.jetbrains.com/issue/KT-55751
    val myAttribute = Attribute.of("KT-55751", String::class.java)

    // replace releaseFrameworkIosFat by the name of the first configuration that conflicts
    configurations.named("releaseFrameworkIosFat").configure {
        attributes {
            // put a unique attribute
            attribute(myAttribute, "release-all")
        }
    }

    // replace releaseFrameworkOsxFat by the name of the first configuration that conflicts
    configurations.named("releaseFrameworkOsxFat").configure {
        attributes {
            // put a unique attribute
            attribute(myAttribute, "release-all")
        }
    }

    val jvmMain by sourceSets.getting

    @Suppress("UnusedPrivateMember", "UNUSED_VARIABLE") // False positive
    val androidMain by sourceSets.getting {
        dependsOn(jvmMain)
    }

    val posixMain by sourceSets.creating {
        dependsOn(commonMain)
    }

    @Suppress("UnusedPrivateMember", "UNUSED_VARIABLE") // False positive
    val winMain by sourceSets.getting {
        dependsOn(posixMain)
    }

    val iosArm64Main by sourceSets.getting
    val iosSimulatorArm64Main by sourceSets.getting
    val macosX64Main by sourceSets.getting
    val macosArm64Main by sourceSets.getting

    @Suppress("UnusedPrivateMember", "UNUSED_VARIABLE") // False positive
    val appleMain by sourceSets.creating {

        dependsOn(commonMain)
        dependsOn(posixMain)

        iosArm64Main.dependsOn(this)
        iosSimulatorArm64Main.dependsOn(this)
        macosX64Main.dependsOn(this)
        macosArm64Main.dependsOn(this)
    }
}

// region Writing version.txt for GitHub Actions
val writeVersion = tasks.register("writeVersion") {
    doLast {
        File("build/version.txt").writeText(project.version.toString())
    }
}

tasks.getByPath("build").finalizedBy(writeVersion)
// endregion

// region Android setup
android {

    namespace = "com.ashampoo.kim"

    compileSdk = 33

    sourceSets["main"].res.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(11)
        targetCompatibility = JavaVersion.toVersion(11)
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

val javadocJar by tasks.registering(Jar::class) {
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
        val signIosSimulatorArm64Publication by tasks.getting
        val signMacosArm64Publication by tasks.getting
        val signMacosX64Publication by tasks.getting
        val signWinPublication by tasks.getting
        val signKotlinMultiplatformPublication by tasks.getting

        val publishJvmPublicationToSonatypeRepository by tasks.getting
        val publishAndroidReleasePublicationToSonatypeRepository by tasks.getting
        val publishIosArm64PublicationToSonatypeRepository by tasks.getting
        val publishIosSimulatorArm64PublicationToSonatypeRepository by tasks.getting
        val publishMacosArm64PublicationToSonatypeRepository by tasks.getting
        val publishMacosX64PublicationToSonatypeRepository by tasks.getting
        val publishWinPublicationToSonatypeRepository by tasks.getting
        val publishKotlinMultiplatformPublicationToSonatypeRepository by tasks.getting
        val publishAllPublicationsToSonatypeRepository by tasks.getting

        val signTasks = listOf(
            signJvmPublication, signAndroidReleasePublication,
            signIosArm64Publication, signIosSimulatorArm64Publication,
            signMacosArm64Publication, signMacosX64Publication,
            signWinPublication, signKotlinMultiplatformPublication
        )

        val publishTasks = listOf(
            publishJvmPublicationToSonatypeRepository,
            publishAndroidReleasePublicationToSonatypeRepository,
            publishIosArm64PublicationToSonatypeRepository,
            publishIosSimulatorArm64PublicationToSonatypeRepository,
            publishMacosArm64PublicationToSonatypeRepository,
            publishMacosX64PublicationToSonatypeRepository,
            publishWinPublicationToSonatypeRepository,
            publishKotlinMultiplatformPublicationToSonatypeRepository,
            publishAllPublicationsToSonatypeRepository
        )

        /* Each publish task depenends on every sign task. */
        for (publishTask in publishTasks)
            for (signTask in signTasks)
                publishTask.dependsOn(signTask)
    }
}

fun getExtraString(name: String) = ext[name]?.toString()

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
                url.set("https://github.com/RealAshampoo/kim")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        name.set("Ashampoo GmbH & Co. KG")
                        url.set("https://www.ashampoo.com/")
                    }
                }

                scm {
                    connection.set("https://github.com/RealAshampoo/kim.git")
                    url.set("https://github.com/RealAshampoo/kim")
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
