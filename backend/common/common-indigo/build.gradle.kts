plugins {
    `java-library`
    `eln-conventions`
}

dependencies {
    api(project(":common:common"))

    implementation("com.epam.indigo:indigo:1.45.0")
    implementation("com.epam.indigo:indigo-renderer:1.45.0")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
