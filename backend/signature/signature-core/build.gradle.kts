plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
    id("org.flywaydb.flyway") version "11.20.1"
}

repositories {
    mavenCentral()
}

dependencies {
    //implementation(project(":database:flyway"))

    api("io.quarkus:quarkus-flyway")
    api("io.quarkus:quarkus-jdbc-postgresql")
    api("io.quarkus:quarkus-hibernate-orm")
    api("io.quarkus:quarkus-hibernate-orm-panache")
    api("io.hypersistence:hypersistence-utils-hibernate-71:3.11.0")
    api("org.flywaydb:flyway-database-postgresql")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.20.0")

    implementation("io.quarkus:quarkus-cache")

    implementation("org.bouncycastle:bcprov-jdk18on")
    implementation("org.bouncycastle:bcpkix-jdk18on")

    implementation("one.util:streamex:0.8.3")
    implementation("com.itextpdf:itextpdf:5.5.13.4")

    api(project(":signature:signature-api"))

    implementation("io.quarkiverse.amazonservices:quarkus-amazon-cognito-user-pools")
    implementation("software.amazon.awssdk:url-connection-client")
    testImplementation(project(":common:common-test"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

val testArtifacts by configurations.creating {
    extendsFrom(configurations.testRuntimeClasspath.get())
}

val testJar by tasks.registering(Jar::class) {
    archiveClassifier.set("tests")
    from(sourceSets.test.get().output)
    dependsOn("jar", "testClasses")
}

artifacts {
    add(testArtifacts.name, testJar)
}
