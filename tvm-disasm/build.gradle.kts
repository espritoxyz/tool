plugins {
    id("tsa.kotlin-conventions")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

dependencies {
    implementation("org.ton:ton-kotlin-tvm:0.3.1")
    implementation("org.ton:ton-kotlin-hashmap-tlb:0.3.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinx_serialization}")
}

val pathToSpec = File(rootProject.projectDir, "tvm-spec/cp0.json")

tasks.processResources {
    from(pathToSpec)
}
