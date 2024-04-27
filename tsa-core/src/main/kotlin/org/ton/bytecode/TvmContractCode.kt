package org.ton.bytecode

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.ton.bigint.BigIntSerializer
import java.math.BigInteger

interface TvmContractCode {
    val methods: Map<BigInteger, TvmMethod>

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
                registerTvmArtificialLoadAddrInstSerializer()

                contextual(BigInteger::class, BigIntSerializer)
            }

        val json = Json {
            prettyPrint = true
            serializersModule = defaultSerializationModule
        }

        fun fromJson(bytecode: String): TvmContractCode {
            return json.decodeFromString<TvmContractCodeImpl>(bytecode)
        }
    }
}

@Serializable
data class TvmContractCodeImpl(
    override val methods: Map<@Serializable(BigIntSerializer::class) BigInteger, TvmMethod>
) : TvmContractCode

@Serializable(with = TvmInstListSerializer::class)
data class TvmInstList(val list: List<TvmInst>): List<TvmInst> by list

class TvmInstListSerializer : KSerializer<TvmInstList> {
    private val listSerializer = ListSerializer(TvmInst.serializer())

    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun deserialize(decoder: Decoder): TvmInstList =
        TvmInstList(listSerializer.deserialize(decoder))

    override fun serialize(encoder: Encoder, value: TvmInstList) {
        listSerializer.serialize(encoder, value.list)
    }
}
