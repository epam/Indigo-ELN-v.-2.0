plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
}

dependencies {
    implementation("org.sonarqube:org.sonarqube.gradle.plugin:7.3.1.8318")
    implementation("io.github.dsdolzhenko.dotenv:io.github.dsdolzhenko.dotenv.gradle.plugin:0.3.2")
}
