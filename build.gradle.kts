import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm").version("1.9.21")
    id ("java")
    id ("idea")
    id ("maven-publish")
    id ("java-library")
    id ("edu.wpi.first.GradleRIO") version "2024.3.2"
    id ("com.google.devtools.ksp") version "1.9.21-1.0.15"
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

group = "org.team2471.lib"
version = "2024"
var wpiLibVersion =  "2024.3.1"
var advantageKitVersion = "3.2.0"
repositories {
    mavenCentral()
    maven { setUrl("https://frcmaven.wpi.edu/artifactory/release/") }
    maven { setUrl("https://plugins.gradle.org/m2/") }
    maven { setUrl("https://maven.ctr-electronics.com/release/") }
    maven { setUrl("https://maven.revrobotics.com/") }
    maven { setUrl("https://maven.photonvision.org/repository/internal")}
    maven {
        url = uri("https://maven.pkg.github.com/Mechanical-Advantage/AdvantageKit")
        credentials {
            username = "Mechanical-Advantage-Bot"
            password = "\u0067\u0068\u0070\u005f\u006e\u0056\u0051\u006a\u0055\u004f\u004c\u0061\u0079\u0066\u006e\u0078\u006e\u0037\u0051\u0049\u0054\u0042\u0032\u004c\u004a\u006d\u0055\u0070\u0073\u0031\u006d\u0037\u004c\u005a\u0030\u0076\u0062\u0070\u0063\u0051"
        }
    }
}

dependencies {
    //@Logged annotation processor
    implementation("org.team9432:annotation")
    ksp("org.team9432:annotation")

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
    implementation("com.ctre.phoenix6:api-java:24.2.0")
    implementation("com.revrobotics.frc:REVLib-java:2024.2.1")

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
