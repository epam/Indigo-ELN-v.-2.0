plugins {
    `eln-conventions`
    id("io.quarkus.extension")
}

quarkusExtension {
    deploymentModule.set(":common:eln-quarkus-extension-deployment")
}

dependencies {
    implementation("io.quarkus:quarkus-arc")
    implementation("org.jspecify:jspecify:1.0.0")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

tasks.withType<io.quarkus.extension.gradle.tasks.ValidateExtensionTask> {
    notCompatibleWithConfigurationCache("Quarkus validateExtension accesses Task.project at execution time — https://github.com/quarkusio/quarkus/issues/49919")
}
