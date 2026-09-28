plugins {
    java
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    implementation(project(":common:common-service"))
    implementation(project(":reports:reports-core"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

val buildDocker = tasks.register<Exec>("buildDocker") {
    outputs.upToDateWhen { false }
    commandLine("docker", "build", "-f", "src/main/docker/Dockerfile.jvm", "-t", "indigoeln/reports-service:built", ".")
}

tasks.named("assemble") {
    finalizedBy("buildDocker")
}
