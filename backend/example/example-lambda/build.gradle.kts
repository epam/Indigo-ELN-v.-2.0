plugins {
    java
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    implementation("io.quarkus:quarkus-amazon-lambda-xray")

    implementation(project(":common:common-lambda"))
    implementation(project(":example:example-core"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
