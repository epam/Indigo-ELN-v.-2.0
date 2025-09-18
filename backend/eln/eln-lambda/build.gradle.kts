plugins {
    java
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    implementation("io.quarkus:quarkus-container-image-docker")

    implementation(project(":common:common-lambda"))
    implementation(project(":eln:eln-core"))
    implementation(project(":eln-quarkus-extension:runtime"))
    testImplementation(project(":common:common-test"))
    testImplementation(project(path = ":eln:eln-core", configuration = "testArtifacts"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

val copyNativeLibs by tasks.registering(Copy::class) {
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

tasks.named("compileIntegrationTestJava") {
    dependsOn(":eln:eln-lambda:assemble")
    dependsOn(":eln:eln-core:testJar")
}

tasks.named("quarkusIntTest", Test::class) {
//    systemProperty("quarkus.http.test-port", "8083")
    outputs.upToDateWhen { false }
}
