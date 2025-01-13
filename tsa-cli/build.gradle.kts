import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("tsa.kotlin-conventions")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

dependencies {
    implementation(project(":tsa-core"))
    implementation(project(":tsa-sarif"))
    implementation(project(":tsa-test-gen"))
    implementation(project(":tvm-disasm"))

    implementation("com.github.ajalt.clikt:clikt:${Versions.clikt}")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinx_serialization}")

}

val mainClassName = "org.ton.MainKt"

tasks.register<JavaExec>("run") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = mainClassName
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = mainClassName
    }
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    val implementation = project.configurations["implementation"].dependencies.toSet()
    val runtimeOnly = project.configurations["runtimeOnly"].dependencies.toSet()
    val dependencies = (implementation + runtimeOnly)
    project.configurations.shadow.get().dependencies.addAll(dependencies)
}
