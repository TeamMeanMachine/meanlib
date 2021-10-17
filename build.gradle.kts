import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.30")
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
version = "2020"
var wpiLibVersion = "2021.3.1"
repositories {
    mavenCentral()
    maven { setUrl("https://frcmaven.wpi.edu/artifactory/release/")}
    maven { setUrl("https://plugins.gradle.org/m2/")}
}

dependencies {
    // kotlin libs
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")

    // frc libs
    implementation("edu.wpi.first.hal:hal-java:2020.1.2")
    implementation("edu.wpi.first.wpilibj:wpilibj-java:2021.3.1")
    implementation("edu.wpi.first.wpiutil:wpiutil-java:2021.3.1")
    implementation("edu.wpi.first.wpimath:wpimath-java:2021.3.1")
    implementation("edu.wpi.first.ntcore:ntcore-jni:2021.3.1")
    implementation("edu.wpi.first.ntcore:ntcore-java:2021.3.1")
    implementation("com.ctre.phoenix:api-java:5.17.3")
    implementation("com.revrobotics.frc:SparkMax-java:1.5.1")

    // other
    implementation("com.google.code.gson:gson:2.8.8")
    implementation("com.squareup.moshi:moshi:1.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")
    implementation("com.squareup.moshi:moshi-adapters:1.12.0")
    implementation( "org.ejml:ejml-simple:0.41")

}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses","-Xopt-in=kotlin.RequiresOptIn")
}
