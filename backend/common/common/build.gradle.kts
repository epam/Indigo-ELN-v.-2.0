plugins {
    `eln-conventions`
    `java-library`
    id("io.quarkus")
}

dependencies {
    api("io.quarkus:quarkus-core")
    api("io.quarkus:quarkus-arc")
    api("io.quarkus:quarkus-rest")
    api("io.quarkus:quarkus-security")
    api("io.quarkus:quarkus-rest-jackson")
    api("io.quarkus:quarkus-hibernate-validator")
    api("io.quarkus:quarkus-logging-json")
    api("org.eclipse.microprofile.jwt:microprofile-jwt-auth-api")
    api("io.quarkus:quarkus-smallrye-openapi")

    api("org.jspecify:jspecify:1.0.0")
    api("com.google.guava:guava:33.5.0-jre")
    api("one.util:streamex:0.8.4")

    testImplementation(project(":common:common-test"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
