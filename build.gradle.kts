import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.10.0")
    }
}

plugins {
    java
    maven
}

apply {
    plugin("kotlin")
    plugin("org.jetbrains.dokka")
}

group = "org.team2471.lib"
version = "2020"

repositories {
    mavenCentral()
    maven { setUrl("https://frcmaven.wpi.edu/artifactory/release/")}
    maven { setUrl("https://plugins.gradle.org/m2/")}
    maven { setUrl("http://devsite.ctr-electronics.com/maven/release/") }
    maven { setUrl("http://www.revrobotics.com/content/sw/max/sdk/maven/") }

}

dependencies {
    // kotlin libs
    compile(kotlin("stdlib-jdk8", "1.3.61"))
    compile(kotlin("reflect", "1.3.61"))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.0")

    // frc libs
    implementation("edu.wpi.first.wpilibj:wpilibj-java:2020.1.2")
    implementation("edu.wpi.first.hal:hal-java:2020.1.2")
    implementation("edu.wpi.first.wpiutil:wpiutil-java:2020.1.2")
    implementation("edu.wpi.first.ntcore:ntcore-java:2020.1.2")
    implementation("com.ctre.phoenix:api-java:5.14.1")
    implementation("com.revrobotics.frc:SparkMax-java:1.4.1")


    // other
    implementation("com.google.code.gson:gson:2.8.2")
    compile("com.squareup.moshi:moshi:1.8.0")
    compile("com.squareup.moshi:moshi-kotlin:1.8.0")
    compile("com.squareup.moshi:moshi-adapters:1.8.0")

}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses", "-Xuse-experimental=kotlin.Experimental")
}
