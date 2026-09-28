plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    api(project(":common:common"))
    api(project(":eln:eln-api"))

    api("io.quarkus:quarkus-flyway")
    api("io.quarkus:quarkus-jdbc-postgresql")
    api("org.flywaydb:flyway-database-postgresql")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")

    testImplementation(project(":common:common-test"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

tasks.withType<JavaCompile>() {
    options.compilerArgs.add("-parameters")
}
