plugins {
    java
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    implementation(project(":common:common-lambda"))
    implementation(project(":reports:reports-core"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

val buildDocker = tasks.register<Exec>("buildDocker") {
    outputs.upToDateWhen { false }
    commandLine("docker", "build", "-f", "src/main/docker/Dockerfile.native", "-t", "indigoeln/reports-lambda:built", ".")
}

tasks.named("assemble") {
    finalizedBy("buildDocker")
}
