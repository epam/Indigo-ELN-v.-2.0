rootProject.name = "IndigoServerless"

pluginManagement {
    val quarkusPluginVersion: String by settings
    val quarkusPluginId: String by settings
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        id(quarkusPluginId) version quarkusPluginVersion
        id("io.quarkus.extension") version quarkusPluginVersion
    }
}

include("common:common")
include("common:common-service")
include("common:common-lambda")
include("common:common-test")

include("common:eln-quarkus-extension")
include("common:eln-quarkus-extension-deployment")

include("database:flyway")

include("signature:signature-api")
include("signature:signature-core")
include("signature:signature-service")
include("signature:signature-lambda")

include("database:flyway")

include("eln:eln-api")
include("eln:eln-core")
include("eln:eln-service")
include("eln:eln-lambda")

include("reports:reports-api")
include("reports:reports-core")
include("reports:reports-lambda")
include("reports:reports-service")

include("integrationTests")
