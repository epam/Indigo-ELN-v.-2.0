plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
//    id("io.github.f-cramer.jasperreports") version "0.0.4"
}

dependencies {
    api(project(":common:common"))
    api(project(":reports:reports-api"))
    api(project(":eln:eln-api"))
//    implementation(project(":database:flyway")) // TODO move flyway to a separate lambda and move dependency to testImplementation

    implementation("io.quarkiverse.jasperreports:quarkus-jasperreports:1.0.11")

//    implementation("net.sf.jasperreports:jasperreports:7.0.3") {
//        exclude(group = "org.apache.xmlgraphics")
//    }
//    implementation("net.sf.jasperreports:jasperreports-pdf:7.0.3") {
//        exclude(group = "org.apache.xmlgraphics")
//    }
//    implementation("io.quarkiverse.openpdf:quarkus-openpdf:3.3.0")
//    implementation("com.ibm.icu:icu4j:77.1") // used by jasperreports
//    jasperreportsClasspath("net.sf.jasperreports:jasperreports-jdt:7.0.3")
//    jasperreportsClasspath("net.sf.jasperreports:jasperreports-json:7.0.3")

    testImplementation(project(":common:common-test"))

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

tasks.named("processResources") { dependsOn(copyNativeLibs) }

//tasks.named("jar") { dependsOn("compileAllReports") }
//tasks.named("quarkusDependenciesBuild") { dependsOn("compileAllReports") }
//tasks.named("compileTestJava") { dependsOn("compileAllReports") }

tasks.withType<Test> {
    environment("NATIVE_LIB_PATH", "${projectDir}/build/nativelibs")
//    jvmArgs("-agentlib:native-image-agent=config-output-dir=${projectDir}/build/native-config")
}

tasks.withType<io.quarkus.gradle.tasks.QuarkusDev> {
    dependsOn(copyNativeLibs)
    environmentVariables.set(mapOf("NATIVE_LIB_PATH" to "${projectDir}/build/nativelibs"))
}

//jasperreports {
//    classpath.from(configurations.compileClasspath)
//    classpath.from(project.sourceSets.main.get().output)
//    srcDir = file("src/main/reports")
//    outDir = file("build/resources/main/reports")
//}
