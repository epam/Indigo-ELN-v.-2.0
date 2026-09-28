plugins {
    id("org.sonarqube")
}

sonar {
    properties {
        property("sonar.projectKey", "Indigo-ELN-Backend")
        property("sonar.projectName", "Indigo ELN Backend")
    }
}
