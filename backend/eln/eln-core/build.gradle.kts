plugins {
    `java-library`
    `eln-conventions`
}

dependencies {
    api(project(":eln:eln-api"))
    api(project(":reports:reports-api"))
    api("io.quarkus:quarkus-smallrye-health")
    implementation(project(":database:flyway")) // TODO move flyway to a separate lambda and move dependency to testImplementation

    api("io.quarkus:quarkus-jdbc-postgresql")
    api("io.quarkus:quarkus-hibernate-orm")
    api("io.quarkus:quarkus-hibernate-orm-panache")
    api("io.hypersistence:hypersistence-utils-hibernate-71:3.11.0")
//    annotationProcessor("org.hibernate.orm:hibernate-processor:7.1.10.Final")
    implementation("io.quarkus:quarkus-cache")

    implementation("com.epam.indigo:indigo:1.35.0-rc.2")
    implementation("com.epam.indigo:indigo-renderer:1.35.0-rc.2")
//    implementation("com.epam.indigo:indigo-inchi:1.30.0")
//    implementation("com.epam.indigo:bingo-nosql:1.30.1")

    implementation("io.quarkiverse.amazonservices:quarkus-amazon-cognito-user-pools")
    implementation("software.amazon.awssdk:url-connection-client")
    testImplementation(project(":common:common-test"))

    // for calculation reports
    testImplementation("io.github.java-diff-utils:java-diff-utils:4.16")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

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
    include("**/darwin-x86_64/*.dylib")
    include("**/darwin-aarch64/*.dylib")
    includeEmptyDirs = false
    destinationDir = File("${projectDir}/build/nativelibs")
}

tasks.named("processResources") { dependsOn(copyNativeLibs) }

tasks.withType<Test> {
    environment("NATIVE_LIB_PATH", "${projectDir}/build/nativelibs")
    environment("ENABLE_PROFILER", "true")
}
