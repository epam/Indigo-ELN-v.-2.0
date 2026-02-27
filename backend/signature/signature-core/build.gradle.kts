plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

repositories {
    mavenCentral()
}

dependencies {
    //implementation(project(":database:flyway"))

    api("io.quarkus:quarkus-flyway")
    api("io.quarkus:quarkus-jdbc-postgresql")
    api("io.quarkus:quarkus-hibernate-orm")
    api("io.hypersistence:hypersistence-utils-hibernate-71:3.11.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.20.0")

    implementation("io.quarkus:quarkus-cache")

    implementation("org.bouncycastle:bcprov-jdk18on")
    implementation("org.bouncycastle:bcpkix-jdk18on")

    implementation("one.util:streamex:0.8.3")
    implementation("com.github.librepdf:openpdf:3.0.1")

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
