plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    api("io.quarkus:quarkus-amazon-lambda-http")
//    implementation("io.opentelemetry:opentelemetry-extension-aws")
    implementation("io.opentelemetry.contrib:opentelemetry-aws-xray-propagator:1.46.0-alpha")

//    implementation("io.quarkus:quarkus-credentials")
//    implementation("io.quarkiverse.amazonservices:quarkus-amazon-secretsmanager")

    api(project(":common:common"))

    implementation("io.quarkus:quarkus-container-image-docker")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
