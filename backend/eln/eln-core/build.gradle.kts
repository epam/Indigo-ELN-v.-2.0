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
}

dependencies {
    api("io.quarkus:quarkus-hibernate-orm")
    api("io.quarkus:quarkus-hibernate-orm-panache")
    api("io.hypersistence:hypersistence-utils-hibernate-63:3.9.9")
    api("com.github.starnowski.posjsonhelper.text:hibernate6-text:0.4.2") // TODO remove
//    api("io.quarkiverse.hibernatetypes:quarkus-hibernate-types:2.2.0")

    api(project(":eln:eln-api"))
    api("one.util:streamex:0.8.3")
    api("com.google.guava:guava:33.4.0-jre")
    implementation("com.epam.indigo:indigo:1.30.1")
    implementation("com.epam.indigo:indigo-renderer:1.30.1")
//    implementation("com.epam.indigo:indigo-inchi:1.30.0")
//    implementation("com.epam.indigo:bingo-nosql:1.30.1")

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
    url= "jdbc:postgresql://localhost:15432/quarkus"
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

tasks.named("processResources") {
    dependsOn(copyNativeLibs)
}

tasks.withType<Test> {
    environment("NATIVE_LIB_PATH", "${projectDir}/build/nativelibs")
}
