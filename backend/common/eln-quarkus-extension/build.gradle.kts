plugins {
    `eln-conventions`
    id("io.quarkus.extension")
}

quarkusExtension {
    deploymentModule.set(":common:eln-quarkus-extension-deployment")
}

dependencies {
    implementation("io.quarkus:quarkus-arc")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

tasks.named("validateExtension") {
    notCompatibleWithConfigurationCache("https://github.com/quarkusio/quarkus/issues/49919")
}
