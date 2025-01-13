plugins {
    id("tsa.kotlin-conventions")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

dependencies {
    implementation(project(":tvm-disasm"))

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

    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable-jvm:${Versions.collections}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinx_serialization}")

    implementation(group = "org.slf4j", name = "slf4j-simple", version = Versions.slf4j)
    testImplementation("ch.qos.logback:logback-classic:${Versions.logback}")

    api(group = Packages.ksmtBv2Int, name = "ksmt-core", version = Versions.ksmtBv2Int)
    implementation(group = Packages.ksmtBv2Int, name = "ksmt-bv2int", version = Versions.ksmtBv2Int)
    implementation(group = Packages.ksmtBv2Int, name = "ksmt-yices", version = Versions.ksmtBv2Int)
    implementation(group = Packages.ksmtBv2Int, name = "ksmt-z3", version = Versions.ksmtBv2Int)

    // todo: remove ksmt-core exclude after upgrading ksmt version in USVM
    api(group = Packages.usvm, name = "usvm-core", version = Versions.usvm) {
        exclude(group = "io.ksmt")
    }
}

val pathToSpec = File(rootProject.projectDir, "tvm-spec/cp0.json")

tasks.processResources {
    from(pathToSpec)
}
