import com.sun.org.apache.bcel.internal.Repository
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    maven
    kotlin("jvm") version "1.3.10"
}

group = "org.team2471.lib"
version = "2019"

repositories {
    mavenCentral()
    maven { setUrl("http://first.wpi.edu/FRC/roborio/maven/release") }
    maven { setUrl("https://raw.githubusercontent.com/Open-RIO/Maven-Mirror/master/m2") }
}

dependencies {
    // kotlin libs
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.0")

    // frc libs
    implementation("edu.wpi.first.wpilibj:wpilibj-java:2018.4.1")
    implementation("edu.wpi.first.wpiutil:wpiutil-java:3.2.0")
    implementation("edu.wpi.first.ntcore:ntcore-java:4.1.0")
    implementation("openrio.mirror.third.ctre:CTRE-phoenix-java:5.3.1.0")

    // other
    implementation("com.google.code.gson:gson:2.8.2")
    implementation("com.squareup.moshi:moshi:1.5.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.5.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
//    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
}
