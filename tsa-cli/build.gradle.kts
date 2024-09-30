plugins {
    id("tsa.kotlin-conventions")
}

dependencies {
    implementation(project(":tsa-core"))
    implementation(project(":tsa-sarif"))
    implementation(project(":tsa-test-gen"))

    implementation("com.github.ajalt.clikt:clikt:${Versions.clikt}")

}

tasks.register<JavaExec>("run") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "org.ton.MainKt"
}
