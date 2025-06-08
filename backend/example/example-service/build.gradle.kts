plugins {
    java
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    implementation(project(":common:common-service"))
    implementation(project(":example:example-core"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
