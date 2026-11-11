plugins {
    kotlin("jvm") version "2.3.21"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("dev.detekt:detekt-api:2.0.0-alpha.3")
}

kotlin {
    jvmToolchain(17)
}