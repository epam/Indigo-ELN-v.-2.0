plugins {
    java
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    implementation(project(":common:common-lambda"))
    implementation(project(":signature:signature-core"))
    implementation("io.quarkus:quarkus-flyway")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
