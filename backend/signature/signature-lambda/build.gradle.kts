plugins {
    java
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    implementation(project(":common:common-lambda"))
    implementation(project(":signature:signature-core"))
    implementation("io.quarkus:quarkus-flyway")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

val buildDocker = tasks.register<Exec>("buildDocker") {
    outputs.upToDateWhen { false }
    commandLine("docker", "build", "-f", "src/main/docker/Dockerfile.native", "-t", "indigoeln/signature-lambda:built", ".")
    standardOutput = System.out
    errorOutput = System.err
}

tasks.named("assemble") {
    finalizedBy("buildDocker")
}
