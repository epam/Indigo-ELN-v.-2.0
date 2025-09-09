plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    api("io.quarkus:quarkus-flyway")
    api("org.flywaydb:flyway-database-postgresql")

    testImplementation(project(":common:common-test"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
