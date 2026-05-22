plugins {
    `java-library`
    `eln-conventions`
    id("io.quarkus")
}

dependencies {
    api("io.quarkus:quarkus-test-security-jwt")

    api(project(":common:common"))

    api("io.quarkus:quarkus-junit5")
    api("io.quarkus:quarkus-test-security")
    api("io.quarkus:quarkus-junit5-mockito")
    api("io.rest-assured:rest-assured")
    api("org.assertj:assertj-core:3.27.7")
    api("com.tngtech.archunit:archunit-junit5:1.4.1")
    api("org.mockito:mockito-core:5.23.0")
    api("org.mockito:mockito-junit-jupiter:5.23.0")
    api("io.quarkiverse.wiremock:quarkus-wiremock-test:1.6.3")

    api("io.github.openfeign:feign-core:13.12")
    api("io.github.openfeign:feign-jackson:13.12")
    api("io.github.openfeign:feign-jaxrs4:13.12")
    api("io.github.openfeign:feign-slf4j:13.12")
    api("io.github.openfeign:feign-form:13.12")
    api("io.github.openfeign:feign-httpclient:13.12")
    api("com.fasterxml.jackson.module:jackson-module-parameter-names:2.20.0")
    api("io.smallrye:smallrye-jwt-common")
    api("io.smallrye:smallrye-jwt-build")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
