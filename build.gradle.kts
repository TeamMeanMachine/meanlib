import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm").version("2.1.0")
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
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:2.0.0")
    }
}

var doReplay = false

group = "org.team2471.lib"
version = "2025"
var wpiLibVersion =  "2025.2.1"
var advantageKitVersion = "4.1.2"
repositories {
    mavenCentral()
    maven { setUrl("https://frcmaven.wpi.edu/artifactory/release/") }
    maven { setUrl("https://plugins.gradle.org/m2/") }
    maven { setUrl("https://maven.ctr-electronics.com/release/") }
    maven { setUrl("https://maven.revrobotics.com/") }
    maven { setUrl("https://maven.photonvision.org/repository/internal")}
    maven { setUrl("https://frcmaven.wpi.edu/artifactory/littletonrobotics-mvn-release/")}
    maven { setUrl("https://lib.choreo.autos/dep")}
    maven { setUrl("https://shenzhen-robotics-alliance.github.io/maple-sim/vendordep/repos/releases")}
}

dependencies {
    //AdvantageKit libs
    implementation("org.littletonrobotics.akit:akit-java:$advantageKitVersion")
    implementation("org.littletonrobotics.akit:akit-wpilibio:$advantageKitVersion")

    // kotlin libs
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

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
    implementation("com.ctre.phoenix6:wpiapi-java:25.1.0")
    implementation("com.revrobotics.frc:REVLib-java:2025.0.0")
    implementation("edu.wpi.first.wpilibNewCommands:wpilibNewCommands-java:$wpiLibVersion")

    // other
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.squareup.moshi:moshi:1.15.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")
    implementation("com.squareup.moshi:moshi-adapters:1.12.0")
    implementation("org.ejml:ejml-simple:0.41")

    implementation("org.photonvision:photonlib-java:v2025.1.1")
    implementation("org.photonvision:photontargeting-java:v2025.1.1")
    implementation("com.fasterxml.jackson.core:jackson-core:2.16.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.16.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.2")

    implementation("org.ironmaple:maplesim-java:0.3.3")
    implementation("choreo:ChoreoLib-java:2025.0.1")

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