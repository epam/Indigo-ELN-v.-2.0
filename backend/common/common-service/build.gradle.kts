plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    api("io.quarkus:quarkus-oidc")
    api("io.quarkus:quarkus-smallrye-health")

    api(project(":common:common"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
