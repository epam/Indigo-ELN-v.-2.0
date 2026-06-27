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
    api(project(":common:common"))
    api(project(":common:common-hibernate"))
    api(project(":signature:signature-api"))
    api(project(":eln:eln-api"))

    api("io.quarkus:quarkus-flyway")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.20.0")

    implementation("io.quarkus:quarkus-cache")

    implementation("org.bouncycastle:bcprov-jdk18on")
    implementation("org.bouncycastle:bcpkix-jdk18on")

    implementation("one.util:streamex:0.8.3")
    implementation("io.quarkiverse.openpdf:quarkus-openpdf:3.3.2")

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
