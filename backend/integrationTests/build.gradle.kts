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

    testImplementation("io.quarkus:quarkus-apache-httpclient")

    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:postgresql")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

tasks.named("compileIntegrationTestJava") {
    dependsOn(":eln:eln-lambda:assemble")
    dependsOn(":eln:eln-core:testJar")
    dependsOn(":reports:reports-lambda:assemble")
    dependsOn(":reports:reports-core:testJar")
    dependsOn(":integrationTests:testClasses")
}

tasks.named("quarkusIntTest", Test::class) {
    systemProperty("quarkus.http.test-host", "localhost")
    systemProperty("quarkus.http.test-port", "28080")
    outputs.upToDateWhen { false }
    filter { // !!!
        includeTestsMatching("*ExperimentServiceIT*")
    }
}
