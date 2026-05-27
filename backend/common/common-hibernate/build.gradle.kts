plugins {
    `java-library`
    `eln-conventions`
}

dependencies {
    api(project(":common:common"))

    api("io.quarkus:quarkus-jdbc-postgresql")
    api("io.quarkus:quarkus-hibernate-orm")
    api("io.quarkus:quarkus-hibernate-orm-panache")
    api("io.hypersistence:hypersistence-utils-hibernate-71:3.11.0")
//    annotationProcessor("org.hibernate.orm:hibernate-processor:7.1.10.Final")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
