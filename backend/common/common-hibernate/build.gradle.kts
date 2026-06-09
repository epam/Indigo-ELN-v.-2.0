plugins {
    `java-library`
    `eln-conventions`
}

dependencies {
    api(project(":common:common"))

    api("io.quarkus:quarkus-jdbc-postgresql")
    api("io.quarkus:quarkus-hibernate-orm")
    api("io.quarkus:quarkus-hibernate-orm-panache")
    api("io.hypersistence:hypersistence-utils-hibernate-71:3.15.2")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
