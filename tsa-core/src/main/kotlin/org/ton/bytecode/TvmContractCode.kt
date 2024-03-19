package org.ton.bytecode

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

interface TvmContractCode {
    val methods: Map<Int, TvmMethod>

    companion object {
        private val defaultSerializationModule: SerializersModule
            get() = SerializersModule {
                polymorphic(TvmInstLocation::class) {
                    subclass(TvmInstMethodLocation::class)
                    subclass(TvmInstLambdaLocation::class)
                }

                polymorphic(TvmCodeBlock::class) {
                    subclass(TvmMethod::class)
                    subclass(TvmLambda::class)
                }

                registerTvmInstSerializer()
            }

        fun fromJson(bytecode: String): TvmContractCode {
            val json = Json {
                serializersModule = defaultSerializationModule
            }

            return json.decodeFromString<TvmContractCodeImpl>(bytecode)
        }
    }
}

@Serializable
data class TvmContractCodeImpl(override val methods: Map<Int, TvmMethod>) : TvmContractCode
