plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    api("io.quarkus:quarkus-amazon-lambda-http")
    implementation("io.quarkus:quarkus-amazon-lambda-xray")

//    implementation("io.quarkus:quarkus-credentials")
//    implementation("io.quarkiverse.amazonservices:quarkus-amazon-secretsmanager")

    api(project(":common:common"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
