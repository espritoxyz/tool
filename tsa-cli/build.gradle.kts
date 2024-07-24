plugins {
    id("tsa.kotlin-conventions")
}

dependencies {
    implementation(project(":tsa-core"))
    implementation(project(":tsa-sarif"))
    implementation("com.github.ajalt.clikt:clikt:${Versions.clikt}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinx_serialization}")
}

tasks.register<JavaExec>("run") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "org.ton.MainKt"
}
