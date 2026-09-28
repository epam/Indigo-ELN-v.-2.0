plugins {
    java
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    implementation(project(":eln:eln-core"))
    implementation(project(":reports:reports-core"))
    testImplementation(project(":common:common-test"))
    testImplementation(project(":common:common-lambda"))
    testImplementation(project(path = ":eln:eln-core", configuration = "testArtifacts"))
    testImplementation(project(path = ":reports:reports-core", configuration = "testArtifacts"))
    testImplementation(project(path = ":signature:signature-core", configuration = "testArtifacts"))

    testImplementation("io.quarkus:quarkus-apache-httpclient")

    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:testcontainers-postgresql")
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
    systemProperty("quarkus.http.test-port", "28080")
    systemProperty("eln.storage.s3.bucket", "indigoeln-data")
    systemProperty("quarkus.s3.endpoint.override", "http://172.17.0.1:4566")
    systemProperty("eln.test.datasource.jdbc-url", "jdbc:postgresql://localhost:25432/eln")
    systemProperty("eln.test.datasource.username", "eln")
    systemProperty("eln.test.datasource.password", "eln")
    outputs.upToDateWhen { false }
    dependsOn(":eln:eln-lambda:assemble")
    dependsOn(":reports:reports-lambda:assemble")
    dependsOn(":signature:signature-lambda:assemble")
}
