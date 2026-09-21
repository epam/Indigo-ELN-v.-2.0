plugins {
    java
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    implementation(project(":common:common-lambda"))
    implementation(project(":eln:eln-core"))
    implementation(project(":eln:eln-core-aws"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

val copyNativeLibs = tasks.register<Copy>("copyNativeLibs") {
    from(configurations.runtimeClasspath.get().filter { it.name.contains("indigo") }.map { zipTree(it)})
    include("**/linux-x86_64/*.so")
    includeEmptyDirs = false
    destinationDir = File("${projectDir}/build/nativelibs")
}

tasks.named("processResources") {
    dependsOn(copyNativeLibs)
}

val buildDocker = tasks.register<Exec>("buildDocker") {
    outputs.upToDateWhen { false }
    commandLine("docker", "build", "-f", "src/main/docker/Dockerfile.native", "-t", "indigoeln/sampleregistration-lambda:built", ".")
    standardOutput = System.out
    errorOutput = System.err
}

tasks.named("assemble") {
    finalizedBy("buildDocker")
}
