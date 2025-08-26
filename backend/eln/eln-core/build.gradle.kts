import org.flywaydb.gradle.task.AbstractFlywayTask

buildscript {
    dependencies {
        classpath("org.postgresql:postgresql:42.7.5")
        classpath("org.flywaydb:flyway-database-postgresql:11.3.2")
    }
}

plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
    id("org.flywaydb.flyway") version "11.3.2"
    id("io.github.f-cramer.jasperreports") version "0.0.4"
}

dependencies {
    api("io.quarkus:quarkus-hibernate-orm")
    api("io.quarkus:quarkus-hibernate-orm-panache")
    api("io.hypersistence:hypersistence-utils-hibernate-63:3.9.9")

    api(project(":eln:eln-api"))
    implementation("com.epam.indigo:indigo:1.33.0-rc.3")
    implementation("com.epam.indigo:indigo-renderer:1.33.0-rc.3")
//    implementation("com.epam.indigo:indigo-inchi:1.30.0")
//    implementation("com.epam.indigo:bingo-nosql:1.30.1")

    implementation("io.quarkiverse.amazonservices:quarkus-amazon-cognito-user-pools")
    implementation("software.amazon.awssdk:url-connection-client")
//    implementation("io.quarkiverse.jasperreports:quarkus-jasperreports:1.0.7")

    implementation("net.sf.jasperreports:jasperreports:7.0.3") {
        exclude(group = "org.apache.xmlgraphics")
    }
    implementation("net.sf.jasperreports:jasperreports-pdf:7.0.3") {
        exclude(group = "org.apache.xmlgraphics")
    }
    implementation("io.quarkiverse.openpdf:quarkus-openpdf:3.3.0")
    implementation("com.ibm.icu:icu4j:77.1") // used by jasperreports
    jasperreportsClasspath("net.sf.jasperreports:jasperreports-jdt:7.0.3")
    jasperreportsClasspath("net.sf.jasperreports:jasperreports-json:7.0.3")

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

flyway {
    url= "jdbc:postgresql://localhost:15433/quarkus"
    driver = "org.postgresql.Driver"
    user = "quarkus"
    password = "quarkus"
    locations = arrayOf("classpath:db/migration")
}

tasks {
    withType<AbstractFlywayTask> {
        notCompatibleWithConfigurationCache("because https://github.com/flyway/flyway/issues/3550")
    }
}

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

tasks.named("jar") { dependsOn("compileAllReports") }
tasks.named("quarkusDependenciesBuild") { dependsOn("compileAllReports") }
tasks.named("compileTestJava") { dependsOn("compileAllReports") }

tasks.withType<Test> {
    environment("NATIVE_LIB_PATH", "${projectDir}/build/nativelibs")
//    jvmArgs("-agentlib:native-image-agent=config-output-dir=${projectDir}/build/native-config")
}

tasks.withType<io.quarkus.gradle.tasks.QuarkusDev> {
    dependsOn(copyNativeLibs)
    environmentVariables.set(mapOf("NATIVE_LIB_PATH" to "${projectDir}/build/nativelibs"))
}

jasperreports {
    classpath.from(configurations.compileClasspath)
    classpath.from(project.sourceSets.main.get().output)
    srcDir = file("src/main/reports")
    outDir = file("build/resources/main/reports")
}
