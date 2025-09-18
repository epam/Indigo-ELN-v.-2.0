plugins {
    java
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    implementation(project(":common:common-lambda"))
    implementation(project(":reports:reports-core"))
    implementation(project(":eln-quarkus-extension:runtime"))
    testImplementation(project(":common:common-test"))
    testImplementation(project(path = ":reports:reports-core", configuration = "testArtifacts"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

tasks.withType<Test> {
    environment("NATIVE_LIB_PATH", "${projectDir}/build/nativelibs")
    environment("QUARKUS_LOG_LEVEL", "TRACE")
}

tasks.named("compileIntegrationTestJava") {
    dependsOn(":reports:reports-lambda:assemble")
    dependsOn(":reports:reports-core:testJar")
}

tasks.named("quarkusIntTest", Test::class) {
    systemProperty("quarkus.profile", "test,integration-test")
    outputs.upToDateWhen { false }
}
