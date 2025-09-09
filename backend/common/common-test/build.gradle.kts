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
    api("io.rest-assured:rest-assured")
    api("org.assertj:assertj-core:3.27.2")

    api("io.github.openfeign:feign-core:13.6")
    api("io.github.openfeign:feign-jackson:13.6")
    api("io.github.openfeign:feign-jaxrs4:13.5")
    api("io.github.openfeign:feign-slf4j:13.2.1")
    api("io.github.openfeign:feign-form:13.6")
    api("io.github.openfeign:feign-httpclient:13.5")
    api("com.fasterxml.jackson.module:jackson-module-parameter-names:2.18.2")
    api("io.smallrye:smallrye-jwt-common") //:4.6.1")
    api("io.smallrye:smallrye-jwt-build")
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"
