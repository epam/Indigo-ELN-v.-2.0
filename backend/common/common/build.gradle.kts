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
    api("io.quarkus:quarkus-rest-client-jackson")

    api("org.jspecify:jspecify:1.0.0")
    api("org.jetbrains:annotations:26.0.2-1")
    api("com.google.guava:guava:33.6.0-jre")
    api("org.apache.commons:commons-math3:3.6.1")
    api("org.apache.commons:commons-lang3:3.20.0")
    api("one.util:streamex:0.8.4")
    api("org.openapitools:jackson-databind-nullable:0.2.10")

    api(project(":common:eln-quarkus-extension"))

    testImplementation(project(":common:common-test"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
