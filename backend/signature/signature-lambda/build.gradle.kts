plugins {
    java
    `eln-conventions`
    id("io.quarkus")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common:common-lambda"))
    implementation(project(":signature:signature-core"))
    implementation("org.flywaydb:flyway-core:11.17.0")

    testImplementation(project(":common:common-test"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
