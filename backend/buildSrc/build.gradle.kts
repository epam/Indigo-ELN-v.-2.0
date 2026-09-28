plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
}

dependencies {
    implementation("com.github.ben-manes:gradle-versions-plugin:0.52.0")
    implementation("org.sonarqube:org.sonarqube.gradle.plugin:7.3.1.8318")
}
