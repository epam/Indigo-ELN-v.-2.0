plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

repositories {
    mavenCentral()
}

dependencies {
    api("io.quarkus:quarkus-jdbc-postgresql")
    api("io.quarkus:quarkus-hibernate-orm")
    api("io.quarkus:quarkus-hibernate-orm-panache")
    api("io.hypersistence:hypersistence-utils-hibernate-71:3.11.0")

    //api("io.quarkus:quarkus-hibernate-orm")
    implementation("org.bouncycastle:bcprov-jdk18on")
    implementation("org.bouncycastle:bcpkix-jdk18on")

    implementation("one.util:streamex:0.8.3")
    implementation("com.itextpdf:itextpdf:5.5.13.4")

    api(project(":signature:signature-api"))

    implementation("io.quarkiverse.amazonservices:quarkus-amazon-cognito-user-pools")
    implementation("software.amazon.awssdk:url-connection-client")
    testImplementation(project(":common:common-test"))

    // https://mvnrepository.com/artifact/org.eclipse.microprofile.rest.client/microprofile-rest-client-api
    implementation("org.eclipse.microprofile.rest.client:microprofile-rest-client-api:4.0")
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
    includeEmptyDirs = false
    destinationDir = File("${projectDir}/build/nativelibs")
}

tasks.named("processResources") { dependsOn(copyNativeLibs) }

tasks.withType<Test> {
    environment("NATIVE_LIB_PATH", "${projectDir}/build/nativelibs")
//    environment("ENABLE_PROFILER", "true")
}

tasks.withType<io.quarkus.gradle.tasks.QuarkusDev> {
    dependsOn(copyNativeLibs)
    environmentVariables.set(mapOf("NATIVE_LIB_PATH" to "${projectDir}/build/nativelibs"))
}
