plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    api(project(":common:common-service"))
    api(project(":common:common-aws"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
