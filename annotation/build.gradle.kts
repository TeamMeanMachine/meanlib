
plugins {
    kotlin("jvm").version("1.9.21")
}

group = "org.team9432"

dependencies {
    implementation("com.squareup:kotlinpoet:1.14.2")
    implementation("com.squareup:kotlinpoet-ksp:1.14.2")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.21-1.0.15")

}

repositories {
    mavenCentral()
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}

