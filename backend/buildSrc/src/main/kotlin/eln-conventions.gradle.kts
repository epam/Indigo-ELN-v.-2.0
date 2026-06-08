plugins {
    java
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
