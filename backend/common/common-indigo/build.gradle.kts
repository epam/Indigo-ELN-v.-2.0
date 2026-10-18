plugins {
    `java-library`
    `eln-conventions`
}

dependencies {
    api(project(":common:common"))

    api("com.epam.indigo:indigo:1.46.0")
    api("com.epam.indigo:indigo-renderer:1.46.0")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
