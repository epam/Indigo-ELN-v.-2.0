plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    api("io.quarkus:quarkus-test-security-jwt")

    api(project(":common:common"))

    api("io.quarkus:quarkus-junit5")
    api("io.quarkus:quarkus-test-security")
    api("io.rest-assured:rest-assured")
    api("org.assertj:assertj-core:3.27.2")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
