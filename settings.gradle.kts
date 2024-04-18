rootProject.name = "tsa"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include("tsa-core")
include("tsa-cli")
include("tsa-intellij")
