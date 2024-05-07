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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinx_serialization}")

    implementation(group = "org.slf4j", name = "slf4j-simple", version = Versions.slf4j)
    testImplementation("ch.qos.logback:logback-classic:${Versions.logback}")

    // todo: remove ksmt-core exclude after upgrading ksmt version in USVM
    implementation("io.ksmt:ksmt-core:${Versions.ksmt}")

    implementation(group = Packages.usvm, name = "usvm-core", version = Versions.usvm) {
        exclude(group = "io.ksmt", module = "ksmt-core")
    }
}

val zipTvmDisasm by tasks.registering(Zip::class) {
    from("../tvm-disasm")
    archiveFileName.set("tvm-disasm.zip")
    destinationDirectory.set(layout.buildDirectory.dir("zips"))
}

tasks.processResources {
    from(zipTvmDisasm) {
        into("lib")
    }
}
