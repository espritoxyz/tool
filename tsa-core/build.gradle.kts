val usvmRepo = "com.github.UnitTestBot.usvm"
val usvmVersion = "875f4e236c"

plugins {
    id("tsa.kotlin-conventions")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

dependencies {
    implementation("org.ton:ton-kotlin-crypto:0.3.1")
    implementation("org.ton:ton-kotlin-tvm:0.3.1")
    implementation("org.ton:ton-kotlin-tonapi-tl:0.3.1")
    implementation("org.ton:ton-kotlin-tlb:0.3.1")
    implementation("org.ton:ton-kotlin-tl:0.3.1")
    implementation("org.ton:ton-kotlin-hashmap-tlb:0.3.1")
    implementation("org.ton:ton-kotlin-contract:0.3.1")
    implementation("org.ton:ton-kotlin-block-tlb:0.3.1")
    implementation("org.ton:ton-kotlin-bitstring:0.3.1")
    implementation("org.ton:ton-kotlin-bigint:0.3.1")

    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable-jvm:${Versions.collections}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:${Versions.kotlinx_serialization}")

    implementation(group = usvmRepo, name = "usvm-core", version = usvmVersion)
    implementation(group = usvmRepo, name = "usvm-jvm", version = usvmVersion)
}
