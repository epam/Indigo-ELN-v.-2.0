plugins {
    id("org.sonarqube")
}

sonar {
    properties {
        property("sonar.projectKey", "ELNLocal")
        property("sonar.host.url", "http://localhost:9000/")
    }
}