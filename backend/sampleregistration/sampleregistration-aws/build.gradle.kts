plugins {
    java
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    implementation(project(":common:common-aws-service"))
    implementation(project(":sampleregistration:sampleregistration-core"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

val buildDocker = tasks.register<Exec>("buildDocker") {
    outputs.upToDateWhen { false }
    commandLine("docker", "build", "-f", "src/main/docker/Dockerfile.jvm", "-t", "indigoeln/sampleregistration-aws:built", ".")
}

tasks.named("assemble") {
    finalizedBy("buildDocker")
}
