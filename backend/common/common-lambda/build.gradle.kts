plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    api("io.quarkus:quarkus-amazon-lambda-http")
    implementation("io.quarkus:quarkus-amazon-lambda-xray")
    api("io.quarkiverse.amazonservices:quarkus-amazon-s3")
    api("io.quarkiverse.amazonservices:quarkus-amazon-cognito-user-pools")
    api("software.amazon.awssdk:url-connection-client")

//    implementation("io.quarkus:quarkus-credentials")
//    implementation("io.quarkiverse.amazonservices:quarkus-amazon-secretsmanager")

    api(project(":common:common"))

    implementation("io.quarkus:quarkus-container-image-docker")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
