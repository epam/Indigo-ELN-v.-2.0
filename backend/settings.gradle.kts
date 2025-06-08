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
    }
}

include("common:common")
include("common:common-service")
include("common:common-lambda")
include("common:common-test")

//include("example:example-api")
//include("example:example-core")
//include("example:example-service")
//include("example:example-lambda")

//include("signature:signature-api")
//include("signature:signature-core")
//include("signature:signature-service")
//include("signature:signature-lambda")

include("eln:eln-api")
include("eln:eln-core")
//include("eln:eln-service")
include("eln:eln-lambda")
