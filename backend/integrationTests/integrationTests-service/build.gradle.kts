plugins {
    java
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    implementation(project(":eln:eln-core"))
    implementation(project(":reports:reports-core"))
    testImplementation(project(":common:common-test"))
    testImplementation(project(":common:common-service"))
    testImplementation(project(path = ":eln:eln-core", configuration = "testArtifacts"))
    testImplementation(project(path = ":reports:reports-core", configuration = "testArtifacts"))
    testImplementation(project(path = ":signature:signature-core", configuration = "testArtifacts"))

    testImplementation("io.quarkus:quarkus-apache-httpclient")

    testImplementation("org.testcontainers:testcontainers")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

tasks.named("compileIntegrationTestJava") {
    dependsOn(":eln:eln-core:testJar")
    dependsOn(":reports:reports-core:testJar")
    dependsOn(":signature:signature-core:testJar")
}

tasks.named("test", Test::class) {
    failOnNoDiscoveredTests = false
}

tasks.named("quarkusIntTest", Test::class) {
    systemProperty("quarkus.http.test-host", "localhost")
    systemProperty("quarkus.http.test-port", "38080")
    outputs.upToDateWhen { false }
    dependsOn(":eln:eln-service:assemble")
    dependsOn(":reports:reports-service:assemble")
    dependsOn(":signature:signature-service:assemble")
}
