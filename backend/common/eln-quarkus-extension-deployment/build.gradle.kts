plugins {
    `eln-conventions`
}

dependencies {
    implementation("io.quarkus:quarkus-core-deployment")
    implementation("io.quarkus:quarkus-arc-deployment")

    implementation(project(":common:eln-quarkus-extension"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
