import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    java
    jacoco
    id("com.github.ben-manes.versions")
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project
val quarkusAmazonServicesVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-amazon-services-bom:${quarkusAmazonServicesVersion}"))

    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")
    testCompileOnly("org.projectlombok:lombok:1.18.46")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.46")
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    annotationProcessor("org.hibernate.orm:hibernate-jpamodelgen:7.3.7.Final")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<org.gradle.api.tasks.compile.JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
    testLogging {
        showStandardStreams = false
        events = setOf(/*TestLogEvent.PASSED, */TestLogEvent.FAILED, TestLogEvent.SKIPPED)
    }
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.named<JacocoReport>("jacocoTestReport") {
    reports {
        xml.required.set(true)
    }
}

// Quarkus loads app classes through its own classloader, which the standard jacoco javaagent
// doesn't see through, leaving @QuarkusTest-exercised code at 0% coverage. quarkus-jacoco
// instruments at build time instead; point its output at the same exec file jacocoTestReport
// already reads so no extra merging is needed.
plugins.withId("io.quarkus") {
    dependencies {
        "testImplementation"("io.quarkus:quarkus-jacoco")
    }

    tasks.withType<Test> {
        systemProperty("quarkus.jacoco.data-file", layout.buildDirectory.file("jacoco/$name.exec").get().asFile.absolutePath)
        systemProperty("quarkus.jacoco.reuse-data-file", "true")
    }
}

// Register the aggregated report task on the root project (first subproject creates it, rest skip).
// Uses convention paths rather than task output properties so testReport never triggers test re-runs.
val testReport = rootProject.tasks.findByName("testReport") as TestReport?
    ?: rootProject.tasks.create("testReport", TestReport::class.java) {
        description = "Aggregates test reports from all submodules into one HTML report."
        group = "verification"
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("reports/tests/all"))
    }

tasks.withType<Test>().configureEach {
    testReport.testResults.from(project.layout.buildDirectory.dir("test-results/$name/binary"))
    finalizedBy(testReport)
}
