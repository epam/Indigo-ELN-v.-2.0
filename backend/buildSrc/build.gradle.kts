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
}
