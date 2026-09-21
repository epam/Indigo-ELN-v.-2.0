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

val copyNativeLibs = tasks.register<Copy>("copyNativeLibs") {
    from(configurations.runtimeClasspath.get().filter { it.name.contains("indigo") }.map { zipTree(it)})
    include("**/linux-x86_64/*.so")
    includeEmptyDirs = false
    destinationDir = File("${projectDir}/build/nativelibs")
}

tasks.named("processResources") {
    dependsOn(copyNativeLibs)
}

tasks.withType<Test> {
    environment("NATIVE_LIB_PATH", "${projectDir}/build/nativelibs")
}

val buildDocker = tasks.register<Exec>("buildDocker") {
    outputs.upToDateWhen { false }
    commandLine("docker", "build", "-f", "src/main/docker/Dockerfile.jvm", "-t", "indigoeln/sampleregistration-aws:built", ".")
    standardOutput = System.out
    errorOutput = System.err
}

tasks.named("assemble") {
    finalizedBy("buildDocker")
}
