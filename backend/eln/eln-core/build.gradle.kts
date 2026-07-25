plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    api(project(":common:common-hibernate"))
    api(project(":eln:eln-api"))
    api(project(":reports:reports-api"))
    api(project(":signature:signature-api"))
    api("io.quarkus:quarkus-smallrye-health")
    implementation(project(":database:flyway")) // TODO move flyway to a separate lambda and move dependency to testImplementation

    implementation("io.quarkus:quarkus-cache")

    implementation("com.epam.indigo:indigo:1.43.0")
    implementation("com.epam.indigo:indigo-renderer:1.43.0")

    testImplementation(project(":common:common-test"))

    // for calculation reports
    testImplementation("io.github.java-diff-utils:java-diff-utils:4.17")
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
    from(configurations.runtimeClasspath.get().filter { it.name.contains("indigo") }.map { zipTree(it)})
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
