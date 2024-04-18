plugins {
  id("tsa.kotlin-conventions")
  id("org.jetbrains.intellij") version "1.16.1"
}

group = "org.explyt.rd"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

// If you are confused, read this thread, please:
// https://youtrack.jetbrains.com/issue/IDEA-296777/Unable-to-access-coroutines-Dispatchers.Main-Fails-with-ServiceConfigurationError
configurations.all {
  exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
  exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core-jvm")
  exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-jdk8")
}

// The following is a workaround to fix Intellij class loaders.
// By default Intellij always loads some Kotlin stdlib classes with `PathClassLoader`,
// while other Kotlin stdlib are loaded by `PluginClassLoader` (see `PluginClassLoader.mustBeLoadedByPlatform()`).
//
// What Intellij Platform developers failed to realize is that it sometimes causes `LinkageError`.
// For example, you get `LinkageError`, if you execute `10.seconds.toInt(DurationUnit.MILLISECONDS)`
// in a fresh Intellij plugin (from template) with `implementation(kotlin("stdlib-jdk8")` dependency.
//
// TODO report this to JetBrains
configurations.runtimeClasspath.get().apply {
  exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
  exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
  exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2024.1")
  type.set("IC")

  plugins.set(listOf(/* Plugin Dependencies */))
}

dependencies {
  implementation(project(":tsa-core"))
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
  }

  patchPluginXml {
    sinceBuild.set("231")
    untilBuild.set("241.*")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }

  buildPlugin {
    archiveFileName.set("explyt-ton-intellij.zip")
  }
}
