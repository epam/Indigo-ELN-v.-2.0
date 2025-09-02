plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    api(project(":eln:eln-core"))

    testImplementation(project(":common:common-test"))

    // for integration tests
    testImplementation("io.github.openfeign:feign-core:13.6")
    testImplementation("io.github.openfeign:feign-jackson:13.6")
    testImplementation("io.github.openfeign:feign-jaxrs4:13.5")
    testImplementation("io.github.openfeign:feign-slf4j:13.2.1")
    testImplementation("io.github.openfeign:feign-form:13.6")
    testImplementation("io.github.openfeign:feign-httpclient:13.5")
    testImplementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.18.2")
    testImplementation("io.smallrye:smallrye-jwt-common") //:4.6.1")
    testImplementation("io.smallrye:smallrye-jwt-build")
    // for calculation reports
    testImplementation("io.github.java-diff-utils:java-diff-utils:4.12")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

//tasks.named("compileAllReports") {
//    dependsOn("compileJava")
//}

//tasks.named("jar") {
//    dependsOn("compileAllReports")
//}
//tasks.named("quarkusDependenciesBuild") {
//    dependsOn("compileAllReports")
//}
//tasks.named("compileTestJava") {
//    dependsOn("compileAllReports")
//}


val testArtifacts by configurations.creating {
    extendsFrom(configurations.testRuntimeClasspath.get())
}

val testJar by tasks.registering(Jar::class) {
    archiveClassifier.set("tests")
    from(sourceSets.test.get().output)
    dependsOn("jar", "testClasses")
}

artifacts {
    add(testArtifacts.name, testJar)
}

val copyNativeLibs by tasks.registering(Copy::class) {
    from(configurations.runtimeClasspath.get().filter { it.name.contains("indigo") }.map { zipTree(it)})
    include("**/linux-x86_64/*.so")
    includeEmptyDirs = false
    destinationDir = File("${projectDir}/build/nativelibs")
}

tasks.withType<Test> {
    environment("NATIVE_LIB_PATH", "${projectDir}/build/nativelibs")
    jvmArgs("-agentlib:native-image-agent=config-output-dir=${projectDir}/build/native-config")
}

tasks.withType<io.quarkus.gradle.tasks.QuarkusDev> {
    dependsOn(copyNativeLibs)
    environmentVariables.set(mapOf("NATIVE_LIB_PATH" to "${projectDir}/build/nativelibs"))
}
