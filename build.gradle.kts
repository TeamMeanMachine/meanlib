import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm").version("1.9.21")
    id ("java")
    id ("idea")
    id ("maven-publish")
    id ("java-library")
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.9.10")
    }
}

var doReplay = false

group = "org.team2471.lib"
version = "2024"
var wpiLibVersion =  "2024.3.2"
var advantageKitVersion = "3.2.0"
repositories {
    mavenCentral()
    maven { setUrl("https://frcmaven.wpi.edu/artifactory/release/") }
    maven { setUrl("https://plugins.gradle.org/m2/") }
    maven { setUrl("https://maven.ctr-electronics.com/release/") }
    maven { setUrl("https://maven.revrobotics.com/") }
    maven { setUrl("https://maven.photonvision.org/repository/internal")}
    maven { setUrl("https://frcmaven.wpi.edu/artifactory/littletonrobotics-mvn-release/")}

}

dependencies {
    //AdvantageKit libs
    implementation("org.littletonrobotics.akit.junction:junction-core:$advantageKitVersion")
    implementation("org.littletonrobotics.akit.junction:wpilib-shim:$advantageKitVersion")
    implementation("org.littletonrobotics.akit.conduit:conduit-api:$advantageKitVersion")
    implementation("org.littletonrobotics.akit.conduit:conduit-wpilibio:$advantageKitVersion")

    // kotlin libs
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

    // frc libs
    implementation("edu.wpi.first.apriltag:apriltag-java:$wpiLibVersion")
    implementation("edu.wpi.first.hal:hal-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpilibj:wpilibj-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpiutil:wpiutil-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpiunits:wpiunits-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpimath:wpimath-java:$wpiLibVersion")
    implementation("edu.wpi.first.ntcore:ntcore-jni:$wpiLibVersion")
    implementation("edu.wpi.first.ntcore:ntcore-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpilibNewCommands:wpilibNewCommands-java:$wpiLibVersion")
    implementation("com.ctre.phoenix6:api-java:24.2.0")
    implementation("com.revrobotics.frc:REVLib-java:2024.2.1")
    implementation("edu.wpi.first.wpilibNewCommands:wpilibNewCommands-java:$wpiLibVersion")

    // other
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.squareup.moshi:moshi:1.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")
    implementation("com.squareup.moshi:moshi-adapters:1.12.0")
    implementation("org.ejml:ejml-simple:0.41")

    implementation("org.photonvision:photonlib-java:v2024.3.1")
    implementation("org.photonvision:photontargeting-java:v2024.3.1")
    implementation("com.fasterxml.jackson.core:jackson-core:2.16.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.16.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.2")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
}

val compileKotlin: KotlinCompile by tasks

//compileKotlin.kotlinOptions {
//    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses","-Xopt-in=kotlin.RequiresOptIn")
//}
