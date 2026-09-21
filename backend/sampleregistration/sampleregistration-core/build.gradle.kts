plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

repositories {
    mavenCentral()
}

dependencies {
    //implementation(project(":database:flyway"))
    api(project(":common:common"))
    api(project(":common:common-hibernate"))
    api(project(":common:common-indigo"))
    api(project(":sampleregistration:sampleregistration-api"))
    api(project(":eln:eln-api"))

    api("io.quarkus:quarkus-flyway")

    testImplementation(project(":common:common-test"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

val testArtifacts by configurations.creating {
    extendsFrom(configurations.testRuntimeClasspath.get())
}

val testJar = tasks.register<Jar>("testJar") {
    archiveClassifier.set("tests")
    from(sourceSets.test.get().output)
    dependsOn("jar", "testClasses")
}

artifacts {
    add(testArtifacts.name, testJar)
}

val copyNativeLibs = tasks.register<Copy>("copyNativeLibs") {
    from(configurations.runtimeClasspath.get().filter { it.name.startsWith("indigo-") }.map { zipTree(it)})
    include("**/linux-x86_64/*.so")
    include("**/darwin-x86_64/*.dylib")
    include("**/darwin-aarch64/*.dylib")
    includeEmptyDirs = false
    destinationDir = File("${projectDir}/build/nativelibs")
}

tasks.named("processResources") { dependsOn(copyNativeLibs) }

tasks.withType<Test> {
    environment("NATIVE_LIB_PATH", "${projectDir}/build/nativelibs")
}

tasks.withType<io.quarkus.gradle.tasks.QuarkusDev> {
    dependsOn(copyNativeLibs)
    environmentVariables.set(mapOf("NATIVE_LIB_PATH" to "${projectDir}/build/nativelibs"))
}
