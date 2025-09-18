plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    api(project(":common:common"))
    api(project(":eln:eln-api"))
    api("io.quarkus:quarkus-rest-client-jackson")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
