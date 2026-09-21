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
    api(project(":common:common-indigo"))
    api(project(":sampleregistration:sampleregistration-api"))
    api(project(":eln:eln-api"))

    api("io.quarkus:quarkus-flyway")

    testImplementation(project(":common:common-test"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

val testArtifacts by configurations.creating {
    extendsFrom(configurations.testRuntimeClasspath.get())
}

val testJar = tasks.register<Jar>("testJar") {
    archiveClassifier.set("tests")
    from(sourceSets.test.get().output)
    dependsOn("jar", "testClasses")
}

artifacts {
    add(testArtifacts.name, testJar)
}
