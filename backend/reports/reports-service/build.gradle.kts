plugins {
    java
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    implementation("io.quarkus:quarkus-container-image-docker")

    implementation(project(":common:common-service"))
    implementation(project(":reports:reports-core"))

    testImplementation(project(":common:common-test"))
    testImplementation(project(path = ":reports:reports-core", configuration = "testArtifacts"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

tasks.withType<Test> {
    environment("NATIVE_LIB_PATH", "${projectDir}/build/nativelibs")
}

tasks.named("compileIntegrationTestJava") {
    dependsOn(":reports:reports-lambda:assemble")
    dependsOn(":reports:reports-core:testJar")
}

tasks.named("quarkusIntTest", Test::class) {
    systemProperty("quarkus.profile", "test,integration-test")
    outputs.upToDateWhen { false }
}
