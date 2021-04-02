import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.0")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.9.17")
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
version = "2019"

repositories {
    mavenCentral()
    maven { setUrl("http://first.wpi.edu/FRC/roborio/maven/release") }
    maven { setUrl("http://devsite.ctr-electronics.com/maven/release/") }
    maven { setUrl("http://www.revrobotics.com/content/sw/max/sdk/maven/") }

}

dependencies {
    // kotlin libs
    compile(kotlin("stdlib-jdk8", "1.3.10"))
    compile(kotlin("reflect", "1.3.10"))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.0")

    // frc libs

//    compile group: "edu.wpi.first.ntcore", name: "ntcore-java", version: "4.+"
//    runtime group: "edu.wpi.first.ntcore", name: "ntcore-jni", version: "4.+", classifier: "all"
//    runtime group: "edu.wpi.first.wpiutil", name: "wpiutil-java", version: "3.+", classifier: "all"
//    runtime group: "edu.wpi.first.hal", name: "hal-java", version: "2019+", classifier: "all"
    implementation("edu.wpi.first.wpilibj:wpilibj-java:2019.4.1")
    implementation("edu.wpi.first.hal:hal-java:2019.4.1")
    implementation("edu.wpi.first.wpiutil:wpiutil-java:2019.4.1")
    implementation("edu.wpi.first.ntcore:ntcore-java:2019.4.1")
    implementation("com.ctre.phoenix:api-java:5.14.1")
    implementation("com.revrobotics.frc:SparkMax-java:1.5.2")


    // other
    implementation("com.google.code.gson:gson:2.8.2")
    compile("com.squareup.moshi:moshi:1.8.0")
    compile("com.squareup.moshi:moshi-kotlin:1.8.0")
    compile("com.squareup.moshi:moshi-adapters:1.8.0")

}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses", "-Xuse-experimental=kotlin.Experimental")
}
