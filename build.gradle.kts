import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.9.10")
    }
}

plugins {
    java
    `maven-publish`
   `java-library`
}
apply {
    plugin("kotlin")
    plugin("org.jetbrains.dokka")
}

group = "org.team2471.lib"
version = "2024"
var wpiLibVersion =  "2024.2.1"
repositories {
    mavenCentral()
    maven { setUrl("https://frcmaven.wpi.edu/artifactory/release/") }
    maven { setUrl("https://plugins.gradle.org/m2/") }
    maven { setUrl("https://maven.ctr-electronics.com/release/") }
    maven { setUrl("https://maven.revrobotics.com/") }
}

dependencies {
    // kotlin libs
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

    // frc libs
    implementation("edu.wpi.first.hal:hal-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpilibj:wpilibj-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpiutil:wpiutil-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpiunits:wpiunits-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpimath:wpimath-java:$wpiLibVersion")
    implementation("edu.wpi.first.ntcore:ntcore-jni:$wpiLibVersion")
    implementation("edu.wpi.first.ntcore:ntcore-java:$wpiLibVersion")
    implementation("com.ctre.phoenix6:api-java:24.1.0")
    implementation("com.revrobotics.frc:REVLib-java:2024.1.1")


    // other
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.squareup.moshi:moshi:1.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")
    implementation("com.squareup.moshi:moshi-adapters:1.12.0")
    implementation( "org.ejml:ejml-simple:0.41")

}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_16.toString()
}

val compileKotlin: KotlinCompile by tasks

//compileKotlin.kotlinOptions {
//    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses","-Xopt-in=kotlin.RequiresOptIn")
//}
