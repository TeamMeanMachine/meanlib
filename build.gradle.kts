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
version = "2022"
var wpiLibVersion = "2022.1.1-beta-1"
repositories {
    mavenCentral()
    maven { setUrl("https://frcmaven.wpi.edu/artifactory/release/")}
    maven { setUrl("https://plugins.gradle.org/m2/")}
    maven {setUrl("https://maven.ctr-electronics.com/release/")}
    maven {setUrl("https://www.revrobotics.com/content/sw/max/sdk/maven/")}
}

dependencies {
    // kotlin libs
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

    // frc libs
    implementation("edu.wpi.first.hal:hal-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpilibj:wpilibj-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpiutil:wpiutil-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpimath:wpimath-java:$wpiLibVersion")
    implementation("edu.wpi.first.ntcore:ntcore-jni:$wpiLibVersion")
    implementation("edu.wpi.first.ntcore:ntcore-java:$wpiLibVersion")
    implementation("com.ctre.phoenix:api-java:5.20.0-beta-1")
    implementation("com.revrobotics.frc:SparkMax-java:1.5.4")

    // other
    implementation("com.google.code.gson:gson:2.8.8")
    implementation("com.squareup.moshi:moshi:1.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")
    implementation("com.squareup.moshi:moshi-adapters:1.12.0")
    implementation( "org.ejml:ejml-simple:0.41")

}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses","-Xopt-in=kotlin.RequiresOptIn")
}
