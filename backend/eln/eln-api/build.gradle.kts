plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    api(project(":common:common"))
    api("com.google.guava:guava:33.4.8-jre")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
