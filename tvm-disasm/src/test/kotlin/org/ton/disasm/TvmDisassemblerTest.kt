package org.ton.disasm

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class TvmDisassemblerTest {
    private val disassembler = TvmDisassembler

    @Test
    fun testLongInt() {
        val path = getResourcePath<TvmDisassemblerTest>("/samples/longint.boc")
        val bytes = path.toFile().readBytes()
        val result = disassembler.disassemble(bytes)
        val expected = """
            {
             "mainMethod": [{"type":"SETCP","location":{"type":"TvmMainMethodLocation","index":0},"n":0},{"type":"DICTPUSHCONST","location":{"type":"TvmMainMethodLocation","index":2},"n":19},{"type":"DICTIGETJMPZ","location":{"type":"TvmMainMethodLocation","index":2}},{"type":"THROWARG","location":{"type":"TvmMainMethodLocation","index":2},"n":11}],
             "methods": {
              "0": {
               "id": "0",
               "instList": [
                {
                 "type": "PUSHINT_LONG",
                 "location": {
                  "type": "TvmInstMethodLocation",
                  "methodId": "0",
                  "index": 0
                 },
                 "x": "1000000000000000000000000000000000000000000000000000000000000000000010"
                },
                {
                 "type": "XCHG_0I",
                 "location": {
                  "type": "TvmInstMethodLocation",
                  "methodId": "0",
                  "index": 1
                 },
                 "i": 1
                },
                {
                 "type": "ADD",
                 "location": {
                  "type": "TvmInstMethodLocation",
                  "methodId": "0",
                  "index": 2
                 }
                },
                {
                 "type": "POP",
                 "location": {
                  "type": "TvmInstMethodLocation",
                  "methodId": "0",
                  "index": 3
                 },
                 "i": 0
                }
               ]
              }
             }
            }
        """.trimIndent()

        val expectedAsJson = Json.parseToJsonElement(expected)
        assertEquals(expectedAsJson, result)
    }

    @Test
    fun testPumpers() {
        val pumpers = getResourcePath<TvmDisassemblerTest>("/samples/EQCV_FsDSymN83YeKZKj_7sgwQHV0jJhCTvX5SkPHHxVOi0D.boc")
        val bytes = pumpers.toFile().readBytes()
        val result = disassembler.disassemble(bytes)
        assertTrue { result["methods"]?.jsonObject?.get("0") != null }
    }

    @Test
    fun testHoneypotWallet() {
        val boc = getResourcePath<TvmDisassemblerTest>("/samples/contract_EQAyQ-wYe8U5hhWFtjEWsgyTFQYv1NYQiuoNz6H3L8tcPG3g.boc")
        val bytes = boc.toFile().readBytes()
        val result = disassembler.disassemble(bytes)
        val expectedPath = getResourcePath<TvmDisassemblerTest>("/samples/contract_EQAyQ-wYe8U5hhWFtjEWsgyTFQYv1NYQiuoNz6H3L8tcPG3g.json")
        val parsedExpected = Json.parseToJsonElement(expectedPath.toFile().readText())
        assertEquals(parsedExpected, result)
    }

    @Test
    fun testCheburashkaWallet() {
        val boc = getResourcePath<TvmDisassemblerTest>("/samples/cheburashka_wallet.boc")
        val bytes = boc.toFile().readBytes()
        val result = disassembler.disassemble(bytes)
        val expectedPath = getResourcePath<TvmDisassemblerTest>("/samples/cheburashka_wallet.json")
        val parsedExpected = Json.parseToJsonElement(expectedPath.toFile().readText())
        assertEquals(parsedExpected, result)
    }

    private inline fun <reified T> getResourcePath(path: String): Path {
        return T::class.java.getResource(path)?.path?.let { Path(it) }
            ?: error("Resource $path was not found")
    }
}
