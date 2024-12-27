rootProject.name = "tsa"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include("tsa-core")
include("tsa-cli")
include("tsa-sarif")
include("tsa-test-gen")
include("tsa-safety-properties")
include("tvm-disasm")
