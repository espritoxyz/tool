package org.example.org.ton.bytecode

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.ton.bytecode.*

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

                polymorphic(TvmInst::class) {
                    subclass(TvmStackNopInst::class)
                    subclass(TvmStackXchg0IInst::class)
                    subclass(TvmStackXchgIJInst::class)
                    subclass(TvmStackPushInst::class)
                    subclass(TvmStackPopInst::class)

                    subclass(TvmNullInst::class)
                    subclass(TvmIsNullInst::class)
                    subclass(TvmMkTupleInst::class)
                    subclass(TvmNilInst::class)
                    subclass(TvmTupleIndexInst::class)
                    subclass(TvmUntupleInst::class)
                    subclass(TvmCheckIsTupleInst::class)
                    subclass(TvmTupleExplodeInst::class)
                    subclass(TvmTupleSetInst::class)
                    subclass(TvmTupleIndexQInst::class)
                    subclass(TvmTupleSetQInst::class)
                    subclass(TvmTupleLenInst::class)
                    subclass(TvmIsTupleInst::class)


                    subclass(TvmPushInt4Inst::class)
                    subclass(TvmPushInt8Inst::class)
                    subclass(TvmPushInt16Inst::class)
                    subclass(TvmPushRefInst::class)


                    subclass(TvmAddInst::class)
                    subclass(TvmSubInst::class)
                    subclass(TvmMulInst::class)
                    subclass(TvmDivInst::class)
                    subclass(TvmModInst::class)
                    subclass(TvmModInst::class)


                    subclass(TvmSignInst::class)
                    subclass(TvmLessInst::class)
                    subclass(TvmEqualInst::class)
                    subclass(TvmLeqInst::class)
                    subclass(TvmGreaterInst::class)
                    subclass(TvmNeqInst::class)
                    subclass(TvmSemptyInst::class)


                    subclass(TvmNewCellInst::class)
                    subclass(TvmEndCellInst::class)
                    subclass(TvmStoreUnsignedIntInst::class)

                    subclass(TvmCellToSliceInst::class)
                    subclass(TvmEndSliceInst::class)
                    subclass(TvmLoadUnsignedIntInst::class)
                    subclass(TvmLoadRef::class)


                    subclass(TvmExecuteInst::class)
                    subclass(TvmJumpXInst::class)
                    subclass(TvmReturnInst::class)

                    subclass(TvmIfInst::class)

                    subclass(TvmRepeatInst::class)

                    subclass(TvmPushCtrInst::class)

                    subclass(TvmCallDictInst::class)


                    subclass(TvmThrowShortZeroParameterInst::class)
                    subclass(TvmThrowArgInst::class)


                    subclass(TvmNewDictInst::class)

                    subclass(TvmSimpleDictGetInst::class)

                    subclass(TvmDictPushConst::class)
                    subclass(TvmDictGetJumpZInst::class)


                    subclass(TvmAnyDebugInst::class)


                    subclass(TvmSetZeroCodepageInst::class)
                }
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
