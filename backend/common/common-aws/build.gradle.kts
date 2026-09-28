plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    api("io.quarkiverse.amazonservices:quarkus-amazon-s3")
    api("io.quarkiverse.amazonservices:quarkus-amazon-cognito-user-pools")
    api("software.amazon.awssdk:url-connection-client")

    api(project(":common:common"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
