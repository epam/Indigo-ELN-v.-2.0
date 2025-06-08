plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    api(project(":example:example-api"))

    testImplementation(project(":common:common-test"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
