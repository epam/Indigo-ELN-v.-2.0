plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    api(project(":common:common-aws"))
    api(project(":eln:eln-core"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
