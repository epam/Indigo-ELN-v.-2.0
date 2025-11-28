plugins {
    `java-library`
    `eln-conventions`
    // no io.quarkus, workaround for Protobuf bug https://github.com/google/protobuf-gradle-plugin/issues/659
    id("com.google.protobuf") version "0.9.5"
}

dependencies {
    api(project(":common:common"))
//    implementation("io.quarkus:quarkus-grpc")
    api("com.google.protobuf:protobuf-java") // version will be taken from quarkus-bom
}

group = "com.epam.indigoeln"
version = "3.0.0-SNAPSHOT"

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.32.1" // version must match the one from quarkus-bom
    }
}
