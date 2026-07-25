plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    api(project(":reports:reports-api"))
    api(project(":eln:eln-api"))

    implementation("io.quarkiverse.jasperreports:quarkus-jasperreports:1.3.0")
    runtimeOnly("xerces:xercesImpl:2.12.2")

    testImplementation(project(":common:common-test"))
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

sourceSets {
    main {
        resources {
            srcDir(layout.buildDirectory.dir("resources/jasper"))
        }
    }
}

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

tasks.withType(Test::class.java) {
    jvmArgs("-agentlib:native-image-agent=config-output-dir=./build/native-config")
}
