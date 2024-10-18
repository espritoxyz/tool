package org.usvm.machine.interpreter

import io.ksmt.expr.KBitVecValue
import org.ton.bytecode.TvmDictDeleteDictdelInst
import org.ton.bytecode.TvmDictDeleteDictdelgetInst
import org.ton.bytecode.TvmDictDeleteDictdelgetrefInst
import org.ton.bytecode.TvmDictDeleteDictidelInst
import org.ton.bytecode.TvmDictDeleteDictidelgetInst
import org.ton.bytecode.TvmDictDeleteDictidelgetrefInst
import org.ton.bytecode.TvmDictDeleteDictudelInst
import org.ton.bytecode.TvmDictDeleteDictudelgetInst
import org.ton.bytecode.TvmDictDeleteDictudelgetrefInst
import org.ton.bytecode.TvmDictDeleteInst
import org.ton.bytecode.TvmDictGetDictgetInst
import org.ton.bytecode.TvmDictGetDictgetrefInst
import org.ton.bytecode.TvmDictGetDictigetInst
import org.ton.bytecode.TvmDictGetDictigetrefInst
import org.ton.bytecode.TvmDictGetDictugetInst
import org.ton.bytecode.TvmDictGetDictugetrefInst
import org.ton.bytecode.TvmDictGetInst
import org.ton.bytecode.TvmDictInst
import org.ton.bytecode.TvmDictMayberefInst
import org.ton.bytecode.TvmDictMinDictimaxInst
import org.ton.bytecode.TvmDictMinDictimaxrefInst
import org.ton.bytecode.TvmDictMinDictiminInst
import org.ton.bytecode.TvmDictMinDictiminrefInst
import org.ton.bytecode.TvmDictMinDictiremmaxInst
import org.ton.bytecode.TvmDictMinDictiremmaxrefInst
import org.ton.bytecode.TvmDictMinDictiremminInst
import org.ton.bytecode.TvmDictMinDictiremminrefInst
import org.ton.bytecode.TvmDictMinDictmaxInst
import org.ton.bytecode.TvmDictMinDictmaxrefInst
import org.ton.bytecode.TvmDictMinDictminInst
import org.ton.bytecode.TvmDictMinDictminrefInst
import org.ton.bytecode.TvmDictMinDictremmaxInst
import org.ton.bytecode.TvmDictMinDictremmaxrefInst
import org.ton.bytecode.TvmDictMinDictremminInst
import org.ton.bytecode.TvmDictMinDictremminrefInst
import org.ton.bytecode.TvmDictMinDictumaxInst
import org.ton.bytecode.TvmDictMinDictumaxrefInst
import org.ton.bytecode.TvmDictMinDictuminInst
import org.ton.bytecode.TvmDictMinDictuminrefInst
import org.ton.bytecode.TvmDictMinDicturemmaxInst
import org.ton.bytecode.TvmDictMinDicturemmaxrefInst
import org.ton.bytecode.TvmDictMinDicturemminInst
import org.ton.bytecode.TvmDictMinDicturemminrefInst
import org.ton.bytecode.TvmDictMinInst
import org.ton.bytecode.TvmDictNextDictgetnextInst
import org.ton.bytecode.TvmDictNextDictgetnexteqInst
import org.ton.bytecode.TvmDictNextDictgetprevInst
import org.ton.bytecode.TvmDictNextDictgetpreveqInst
import org.ton.bytecode.TvmDictNextDictigetnextInst
import org.ton.bytecode.TvmDictNextDictigetnexteqInst
import org.ton.bytecode.TvmDictNextDictigetprevInst
import org.ton.bytecode.TvmDictNextDictigetpreveqInst
import org.ton.bytecode.TvmDictNextDictugetnextInst
import org.ton.bytecode.TvmDictNextDictugetnexteqInst
import org.ton.bytecode.TvmDictNextDictugetprevInst
import org.ton.bytecode.TvmDictNextDictugetpreveqInst
import org.ton.bytecode.TvmDictNextInst
import org.ton.bytecode.TvmDictPrefixInst
import org.ton.bytecode.TvmDictSerialInst
import org.ton.bytecode.TvmDictSerialLddictInst
import org.ton.bytecode.TvmDictSerialLddictqInst
import org.ton.bytecode.TvmDictSerialLddictsInst
import org.ton.bytecode.TvmDictSerialPlddictInst
import org.ton.bytecode.TvmDictSerialPlddictqInst
import org.ton.bytecode.TvmDictSerialPlddictsInst
import org.ton.bytecode.TvmDictSerialSkipdictInst
import org.ton.bytecode.TvmDictSerialStdictInst
import org.ton.bytecode.TvmDictSetBuilderDictaddbInst
import org.ton.bytecode.TvmDictSetBuilderDictaddgetbInst
import org.ton.bytecode.TvmDictSetBuilderDictiaddbInst
import org.ton.bytecode.TvmDictSetBuilderDictiaddgetbInst
import org.ton.bytecode.TvmDictSetBuilderDictireplacebInst
import org.ton.bytecode.TvmDictSetBuilderDictireplacegetbInst
import org.ton.bytecode.TvmDictSetBuilderDictisetbInst
import org.ton.bytecode.TvmDictSetBuilderDictisetgetbInst
import org.ton.bytecode.TvmDictSetBuilderDictreplacebInst
import org.ton.bytecode.TvmDictSetBuilderDictreplacegetbInst
import org.ton.bytecode.TvmDictSetBuilderDictsetbInst
import org.ton.bytecode.TvmDictSetBuilderDictsetgetbInst
import org.ton.bytecode.TvmDictSetBuilderDictuaddbInst
import org.ton.bytecode.TvmDictSetBuilderDictuaddgetbInst
import org.ton.bytecode.TvmDictSetBuilderDictureplacebInst
import org.ton.bytecode.TvmDictSetBuilderDictureplacegetbInst
import org.ton.bytecode.TvmDictSetBuilderDictusetbInst
import org.ton.bytecode.TvmDictSetBuilderDictusetgetbInst
import org.ton.bytecode.TvmDictSetBuilderInst
import org.ton.bytecode.TvmDictSetDictaddInst
import org.ton.bytecode.TvmDictSetDictaddgetInst
import org.ton.bytecode.TvmDictSetDictaddgetrefInst
import org.ton.bytecode.TvmDictSetDictaddrefInst
import org.ton.bytecode.TvmDictSetDictiaddInst
import org.ton.bytecode.TvmDictSetDictiaddgetInst
import org.ton.bytecode.TvmDictSetDictiaddgetrefInst
import org.ton.bytecode.TvmDictSetDictiaddrefInst
import org.ton.bytecode.TvmDictSetDictireplaceInst
import org.ton.bytecode.TvmDictSetDictireplacegetInst
import org.ton.bytecode.TvmDictSetDictireplacegetrefInst
import org.ton.bytecode.TvmDictSetDictireplacerefInst
import org.ton.bytecode.TvmDictSetDictisetInst
import org.ton.bytecode.TvmDictSetDictisetgetInst
import org.ton.bytecode.TvmDictSetDictisetgetrefInst
import org.ton.bytecode.TvmDictSetDictisetrefInst
import org.ton.bytecode.TvmDictSetDictreplaceInst
import org.ton.bytecode.TvmDictSetDictreplacegetInst
import org.ton.bytecode.TvmDictSetDictreplacegetrefInst
import org.ton.bytecode.TvmDictSetDictreplacerefInst
import org.ton.bytecode.TvmDictSetDictsetInst
import org.ton.bytecode.TvmDictSetDictsetgetInst
import org.ton.bytecode.TvmDictSetDictsetgetrefInst
import org.ton.bytecode.TvmDictSetDictsetrefInst
import org.ton.bytecode.TvmDictSetDictuaddInst
import org.ton.bytecode.TvmDictSetDictuaddgetInst
import org.ton.bytecode.TvmDictSetDictuaddgetrefInst
import org.ton.bytecode.TvmDictSetDictuaddrefInst
import org.ton.bytecode.TvmDictSetDictureplaceInst
import org.ton.bytecode.TvmDictSetDictureplacegetInst
import org.ton.bytecode.TvmDictSetDictureplacegetrefInst
import org.ton.bytecode.TvmDictSetDictureplacerefInst
import org.ton.bytecode.TvmDictSetDictusetInst
import org.ton.bytecode.TvmDictSetDictusetgetInst
import org.ton.bytecode.TvmDictSetDictusetgetrefInst
import org.ton.bytecode.TvmDictSetDictusetrefInst
import org.ton.bytecode.TvmDictSetInst
import org.ton.bytecode.TvmDictSpecialInst
import org.ton.bytecode.TvmDictSubInst
import org.usvm.UBoolExpr
import org.usvm.UBvSort
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.api.makeSymbolicPrimitive
import org.usvm.api.readField
import org.usvm.api.writeField
import org.usvm.collection.set.primitive.USetEntryLValue
import org.usvm.collection.set.primitive.setEntries
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmContext.Companion.dictKeyLengthField
import org.usvm.machine.TvmStepScope
import org.usvm.machine.intValue
import org.usvm.machine.state.DictId
import org.usvm.machine.state.DictKeyInfo
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.addInt
import org.usvm.machine.state.addOnStack
import org.usvm.machine.state.allocEmptyCell
import org.usvm.machine.state.allocSliceFromCell
import org.usvm.machine.state.allocSliceFromData
import org.usvm.machine.state.assertIfSat
import org.usvm.machine.state.assertType
import org.usvm.machine.state.builderCopy
import org.usvm.machine.state.builderStoreDataBits
import org.usvm.machine.state.builderStoreNextRef
import org.usvm.machine.state.calcOnStateCtx
import org.usvm.machine.state.consumeDefaultGas
import org.usvm.machine.state.copyDict
import org.usvm.machine.state.dictAddKeyValue
import org.usvm.machine.state.dictContainsKey
import org.usvm.machine.state.dictGetValue
import org.usvm.machine.state.dictRemoveKey
import org.usvm.machine.state.doWithStateCtx
import org.usvm.machine.state.newStmt
import org.usvm.machine.state.nextStmt
import org.usvm.machine.state.sliceCopy
import org.usvm.machine.state.sliceMoveDataPtr
import org.usvm.machine.state.sliceMoveRefPtr
import org.usvm.machine.state.slicePreloadDataBits
import org.usvm.machine.state.slicePreloadNextRef
import org.usvm.machine.state.takeLastBuilder
import org.usvm.machine.state.takeLastCell
import org.usvm.machine.state.takeLastIntOrThrowTypeError
import org.usvm.machine.state.takeLastSlice
import org.usvm.machine.types.TvmBuilderType
import org.usvm.machine.types.TvmCellType
import org.usvm.machine.types.TvmDictCellType
import org.usvm.machine.types.TvmIntegerType
import org.usvm.machine.types.TvmNullType
import org.usvm.machine.types.TvmSliceType
import org.usvm.machine.types.TvmSymbolicCellMaybeConstructorBit
import org.usvm.machine.types.makeSliceTypeLoad
import org.usvm.utils.intValueOrNull

class TvmDictOperationInterpreter(private val ctx: TvmContext) {
    fun visitTvmDictInst(scope: TvmStepScope, inst: TvmDictInst) {
        scope.consumeDefaultGas(inst)

        when (inst) {
            is TvmDictGetInst -> visitDictGet(scope, inst)
            is TvmDictSetInst -> visitDictSet(scope, inst)
            is TvmDictSetBuilderInst -> visitDictSetBuilder(scope, inst)
            is TvmDictDeleteInst -> visitDictDelete(scope, inst)
            is TvmDictSerialInst -> visitDictSerial(scope, inst)
            is TvmDictMinInst -> visitDictMin(scope, inst)
            is TvmDictNextInst -> visitDictNext(scope, inst)
            is TvmDictSubInst -> TODO()
            is TvmDictMayberefInst -> TODO()
            is TvmDictPrefixInst -> TODO()
            is TvmDictSpecialInst -> error("Dict special inst should not be handled there: $inst")
        }
    }

    private fun visitDictSet(scope: TvmStepScope, inst: TvmDictSetInst) {
        when (inst) {
            is TvmDictSetDictaddInst -> doDictSet(inst, scope, DictKeyType.SLICE, DictValueType.SLICE, getOldValue = false, DictSetMode.ADD)
            is TvmDictSetDictaddgetInst -> doDictSet(inst, scope, DictKeyType.SLICE, DictValueType.SLICE, getOldValue = true, DictSetMode.ADD)
            is TvmDictSetDictaddrefInst -> doDictSet(inst, scope, DictKeyType.SLICE, DictValueType.CELL, getOldValue = false, DictSetMode.ADD)
            is TvmDictSetDictaddgetrefInst -> doDictSet(inst, scope, DictKeyType.SLICE, DictValueType.CELL, getOldValue = true, DictSetMode.ADD)
            is TvmDictSetDictreplaceInst -> doDictSet(inst, scope, DictKeyType.SLICE, DictValueType.SLICE, getOldValue = false, DictSetMode.REPLACE)
            is TvmDictSetDictreplacegetInst -> doDictSet(inst, scope, DictKeyType.SLICE, DictValueType.SLICE, getOldValue = true, DictSetMode.REPLACE)
            is TvmDictSetDictreplacerefInst -> doDictSet(inst, scope, DictKeyType.SLICE, DictValueType.CELL, getOldValue = false, DictSetMode.REPLACE)
            is TvmDictSetDictreplacegetrefInst -> doDictSet(inst, scope, DictKeyType.SLICE, DictValueType.CELL, getOldValue = true, DictSetMode.REPLACE)
            is TvmDictSetDictsetInst -> doDictSet(inst, scope, DictKeyType.SLICE, DictValueType.SLICE, getOldValue = false, DictSetMode.SET)
            is TvmDictSetDictsetgetInst -> doDictSet(inst, scope, DictKeyType.SLICE, DictValueType.SLICE, getOldValue = true, DictSetMode.SET)
            is TvmDictSetDictsetrefInst -> doDictSet(inst, scope, DictKeyType.SLICE, DictValueType.CELL, getOldValue = false, DictSetMode.SET)
            is TvmDictSetDictsetgetrefInst -> doDictSet(inst, scope, DictKeyType.SLICE, DictValueType.CELL, getOldValue = true, DictSetMode.SET)
            is TvmDictSetDictiaddInst -> doDictSet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.SLICE, getOldValue = false, DictSetMode.ADD)
            is TvmDictSetDictiaddgetInst -> doDictSet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.SLICE, getOldValue = true, DictSetMode.ADD)
            is TvmDictSetDictiaddrefInst -> doDictSet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.CELL, getOldValue = false, DictSetMode.ADD)
            is TvmDictSetDictiaddgetrefInst -> doDictSet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.CELL, getOldValue = true, DictSetMode.ADD)
            is TvmDictSetDictireplaceInst -> doDictSet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.SLICE, getOldValue = false, DictSetMode.REPLACE)
            is TvmDictSetDictireplacegetInst -> doDictSet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.SLICE, getOldValue = true, DictSetMode.REPLACE)
            is TvmDictSetDictireplacerefInst -> doDictSet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.CELL, getOldValue = false, DictSetMode.REPLACE)
            is TvmDictSetDictireplacegetrefInst -> doDictSet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.CELL, getOldValue = true, DictSetMode.REPLACE)
            is TvmDictSetDictisetInst -> doDictSet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.SLICE, getOldValue = false, DictSetMode.SET)
            is TvmDictSetDictisetgetInst -> doDictSet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.SLICE, getOldValue = true, DictSetMode.SET)
            is TvmDictSetDictisetrefInst -> doDictSet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.CELL, getOldValue = false, DictSetMode.SET)
            is TvmDictSetDictisetgetrefInst -> doDictSet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.CELL, getOldValue = true, DictSetMode.SET)
            is TvmDictSetDictuaddInst -> doDictSet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.SLICE, getOldValue = false, DictSetMode.ADD)
            is TvmDictSetDictuaddgetInst -> doDictSet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.SLICE, getOldValue = true, DictSetMode.ADD)
            is TvmDictSetDictuaddrefInst -> doDictSet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.CELL, getOldValue = false, DictSetMode.ADD)
            is TvmDictSetDictuaddgetrefInst -> doDictSet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.CELL, getOldValue = true, DictSetMode.ADD)
            is TvmDictSetDictureplaceInst -> doDictSet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.SLICE, getOldValue = false, DictSetMode.REPLACE)
            is TvmDictSetDictureplacegetInst -> doDictSet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.SLICE, getOldValue = true, DictSetMode.REPLACE)
            is TvmDictSetDictureplacerefInst -> doDictSet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.CELL, getOldValue = false, DictSetMode.REPLACE)
            is TvmDictSetDictureplacegetrefInst -> doDictSet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.CELL, getOldValue = true, DictSetMode.REPLACE)
            is TvmDictSetDictusetInst -> doDictSet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.SLICE, getOldValue = false, DictSetMode.SET)
            is TvmDictSetDictusetgetInst -> doDictSet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.SLICE, getOldValue = true, DictSetMode.SET)
            is TvmDictSetDictusetrefInst -> doDictSet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.CELL, getOldValue = false, DictSetMode.SET)
            is TvmDictSetDictusetgetrefInst -> doDictSet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.CELL, getOldValue = true, DictSetMode.SET)
        }
    }

    private fun visitDictSetBuilder(scope: TvmStepScope, inst: TvmDictSetBuilderInst) {
        when (inst) {
            is TvmDictSetBuilderDictaddbInst -> doDictSet(inst, scope, DictKeyType.SLICE, DictValueType.BUILDER, getOldValue = false, DictSetMode.ADD)
            is TvmDictSetBuilderDictaddgetbInst -> doDictSet(inst, scope, DictKeyType.SLICE, DictValueType.BUILDER, getOldValue = true, DictSetMode.ADD)
            is TvmDictSetBuilderDictreplacebInst -> doDictSet(inst, scope, DictKeyType.SLICE, DictValueType.BUILDER, getOldValue = false, DictSetMode.REPLACE)
            is TvmDictSetBuilderDictreplacegetbInst -> doDictSet(inst, scope, DictKeyType.SLICE, DictValueType.BUILDER, getOldValue = true, DictSetMode.REPLACE)
            is TvmDictSetBuilderDictsetbInst -> doDictSet(inst, scope, DictKeyType.SLICE, DictValueType.BUILDER, getOldValue = false, DictSetMode.SET)
            is TvmDictSetBuilderDictsetgetbInst -> doDictSet(inst, scope, DictKeyType.SLICE, DictValueType.BUILDER, getOldValue = true, DictSetMode.SET)
            is TvmDictSetBuilderDictiaddbInst -> doDictSet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.BUILDER, getOldValue = false, DictSetMode.ADD)
            is TvmDictSetBuilderDictiaddgetbInst -> doDictSet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.BUILDER, getOldValue = true, DictSetMode.ADD)
            is TvmDictSetBuilderDictireplacebInst -> doDictSet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.BUILDER, getOldValue = false, DictSetMode.REPLACE)
            is TvmDictSetBuilderDictireplacegetbInst -> doDictSet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.BUILDER, getOldValue = true, DictSetMode.REPLACE)
            is TvmDictSetBuilderDictisetbInst -> doDictSet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.BUILDER, getOldValue = false, DictSetMode.SET)
            is TvmDictSetBuilderDictisetgetbInst -> doDictSet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.BUILDER, getOldValue = true, DictSetMode.SET)
            is TvmDictSetBuilderDictuaddbInst -> doDictSet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.BUILDER, getOldValue = false, DictSetMode.ADD)
            is TvmDictSetBuilderDictuaddgetbInst -> doDictSet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.BUILDER, getOldValue = true, DictSetMode.ADD)
            is TvmDictSetBuilderDictureplacebInst -> doDictSet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.BUILDER, getOldValue = false, DictSetMode.REPLACE)
            is TvmDictSetBuilderDictureplacegetbInst -> doDictSet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.BUILDER, getOldValue = true, DictSetMode.REPLACE)
            is TvmDictSetBuilderDictusetbInst -> doDictSet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.BUILDER, getOldValue = false, DictSetMode.SET)
            is TvmDictSetBuilderDictusetgetbInst -> doDictSet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.BUILDER, getOldValue = true, DictSetMode.SET)
        }
    }

    private fun visitDictGet(scope: TvmStepScope, inst: TvmDictGetInst) {
        when (inst) {
            is TvmDictGetDictgetInst -> doDictGet(inst, scope, DictKeyType.SLICE, DictValueType.SLICE)
            is TvmDictGetDictgetrefInst -> doDictGet(inst, scope, DictKeyType.SLICE, DictValueType.CELL)
            is TvmDictGetDictigetInst -> doDictGet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.SLICE)
            is TvmDictGetDictigetrefInst -> doDictGet(inst, scope, DictKeyType.SIGNED_INT, DictValueType.CELL)
            is TvmDictGetDictugetInst -> doDictGet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.SLICE)
            is TvmDictGetDictugetrefInst -> doDictGet(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.CELL)
        }
    }

    private fun visitDictDelete(scope: TvmStepScope, inst: TvmDictDeleteInst) {
        when (inst) {
            is TvmDictDeleteDictdelInst -> doDictDelete(inst, scope, DictKeyType.SLICE, DictValueType.SLICE, getOldValue = false)
            is TvmDictDeleteDictdelgetInst -> doDictDelete(inst, scope, DictKeyType.SLICE, DictValueType.SLICE, getOldValue = true)
            is TvmDictDeleteDictdelgetrefInst -> doDictDelete(inst, scope, DictKeyType.SLICE, DictValueType.CELL, getOldValue = true)
            is TvmDictDeleteDictidelInst -> doDictDelete(inst, scope, DictKeyType.SIGNED_INT, DictValueType.SLICE, getOldValue = false)
            is TvmDictDeleteDictidelgetInst -> doDictDelete(inst, scope, DictKeyType.SIGNED_INT, DictValueType.SLICE, getOldValue = true)
            is TvmDictDeleteDictidelgetrefInst -> doDictDelete(inst, scope, DictKeyType.SIGNED_INT, DictValueType.CELL, getOldValue = true)
            is TvmDictDeleteDictudelInst -> doDictDelete(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.SLICE, getOldValue = false)
            is TvmDictDeleteDictudelgetInst -> doDictDelete(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.SLICE, getOldValue = true)
            is TvmDictDeleteDictudelgetrefInst -> doDictDelete(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.CELL, getOldValue = true)
        }
    }

    private fun visitDictSerial(scope: TvmStepScope, inst: TvmDictSerialInst) {
        when (inst) {
            is TvmDictSerialLddictInst -> doLoadDict(inst, scope, returnUpdatedSlice = true)
            is TvmDictSerialLddictqInst -> TODO()
            is TvmDictSerialLddictsInst -> TODO()
            is TvmDictSerialPlddictInst -> doLoadDict(inst, scope, returnUpdatedSlice = false)
            is TvmDictSerialPlddictqInst -> TODO()
            is TvmDictSerialPlddictsInst -> TODO()
            is TvmDictSerialSkipdictInst -> TODO()
            is TvmDictSerialStdictInst -> doStoreDictToBuilder(inst, scope)
        }
    }

    private fun visitDictMin(scope: TvmStepScope, inst: TvmDictMinInst) {
        when (inst) {
            is TvmDictMinDictimaxInst -> doDictMinMax(inst, scope, DictKeyType.SIGNED_INT, DictValueType.SLICE, DictMinMaxMode.MAX, removeKey = false)
            is TvmDictMinDictimaxrefInst -> doDictMinMax(inst, scope, DictKeyType.SIGNED_INT, DictValueType.CELL, DictMinMaxMode.MAX, removeKey = false)
            is TvmDictMinDictiminInst -> doDictMinMax(inst, scope, DictKeyType.SIGNED_INT, DictValueType.SLICE, DictMinMaxMode.MIN, removeKey = false)
            is TvmDictMinDictiminrefInst -> doDictMinMax(inst, scope, DictKeyType.SIGNED_INT, DictValueType.CELL, DictMinMaxMode.MIN, removeKey = false)
            is TvmDictMinDictiremmaxInst -> doDictMinMax(inst, scope, DictKeyType.SIGNED_INT, DictValueType.SLICE, DictMinMaxMode.MAX, removeKey = true)
            is TvmDictMinDictiremmaxrefInst -> doDictMinMax(inst, scope, DictKeyType.SIGNED_INT, DictValueType.CELL, DictMinMaxMode.MAX, removeKey = true)
            is TvmDictMinDictiremminInst -> doDictMinMax(inst, scope, DictKeyType.SIGNED_INT, DictValueType.SLICE, DictMinMaxMode.MIN, removeKey = true)
            is TvmDictMinDictiremminrefInst -> doDictMinMax(inst, scope, DictKeyType.SIGNED_INT, DictValueType.CELL, DictMinMaxMode.MIN, removeKey = true)
            is TvmDictMinDictmaxInst -> doDictMinMax(inst, scope, DictKeyType.SLICE, DictValueType.SLICE, DictMinMaxMode.MAX, removeKey = false)
            is TvmDictMinDictmaxrefInst -> doDictMinMax(inst, scope, DictKeyType.SLICE, DictValueType.CELL, DictMinMaxMode.MAX, removeKey = false)
            is TvmDictMinDictminInst -> doDictMinMax(inst, scope, DictKeyType.SLICE, DictValueType.SLICE, DictMinMaxMode.MIN, removeKey = false)
            is TvmDictMinDictminrefInst -> doDictMinMax(inst, scope, DictKeyType.SLICE, DictValueType.CELL, DictMinMaxMode.MIN, removeKey = false)
            is TvmDictMinDictremmaxInst -> doDictMinMax(inst, scope, DictKeyType.SLICE, DictValueType.SLICE, DictMinMaxMode.MAX, removeKey = true)
            is TvmDictMinDictremmaxrefInst -> doDictMinMax(inst, scope, DictKeyType.SLICE, DictValueType.CELL, DictMinMaxMode.MAX, removeKey = true)
            is TvmDictMinDictremminInst -> doDictMinMax(inst, scope, DictKeyType.SLICE, DictValueType.SLICE, DictMinMaxMode.MIN, removeKey = true)
            is TvmDictMinDictremminrefInst -> doDictMinMax(inst, scope, DictKeyType.SLICE, DictValueType.CELL, DictMinMaxMode.MIN, removeKey = true)
            is TvmDictMinDictumaxInst -> doDictMinMax(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.SLICE, DictMinMaxMode.MAX, removeKey = false)
            is TvmDictMinDictumaxrefInst -> doDictMinMax(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.CELL, DictMinMaxMode.MAX, removeKey = false)
            is TvmDictMinDictuminInst -> doDictMinMax(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.SLICE, DictMinMaxMode.MIN, removeKey = false)
            is TvmDictMinDictuminrefInst -> doDictMinMax(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.CELL, DictMinMaxMode.MIN, removeKey = false)
            is TvmDictMinDicturemmaxInst -> doDictMinMax(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.SLICE, DictMinMaxMode.MAX, removeKey = true)
            is TvmDictMinDicturemmaxrefInst -> doDictMinMax(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.CELL, DictMinMaxMode.MAX, removeKey = true)
            is TvmDictMinDicturemminInst -> doDictMinMax(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.SLICE, DictMinMaxMode.MIN, removeKey = true)
            is TvmDictMinDicturemminrefInst -> doDictMinMax(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.CELL, DictMinMaxMode.MIN, removeKey = true)
        }
    }

    private fun visitDictNext(scope: TvmStepScope, inst: TvmDictNextInst) {
        when (inst) {
            is TvmDictNextDictgetnextInst -> doDictNextPrev(inst, scope, DictKeyType.SLICE, DictValueType.SLICE, DictNextPrevMode.NEXT, allowEq = false)
            is TvmDictNextDictgetnexteqInst -> doDictNextPrev(inst, scope, DictKeyType.SLICE, DictValueType.SLICE, DictNextPrevMode.NEXT, allowEq = true)
            is TvmDictNextDictgetprevInst -> doDictNextPrev(inst, scope, DictKeyType.SLICE, DictValueType.SLICE, DictNextPrevMode.PREV, allowEq = false)
            is TvmDictNextDictgetpreveqInst -> doDictNextPrev(inst, scope, DictKeyType.SLICE, DictValueType.SLICE, DictNextPrevMode.PREV, allowEq = true)
            is TvmDictNextDictigetnextInst -> doDictNextPrev(inst, scope, DictKeyType.SIGNED_INT, DictValueType.SLICE, DictNextPrevMode.NEXT, allowEq = false)
            is TvmDictNextDictigetnexteqInst -> doDictNextPrev(inst, scope, DictKeyType.SIGNED_INT, DictValueType.SLICE, DictNextPrevMode.NEXT, allowEq = true)
            is TvmDictNextDictigetprevInst -> doDictNextPrev(inst, scope, DictKeyType.SIGNED_INT, DictValueType.SLICE, DictNextPrevMode.PREV, allowEq = false)
            is TvmDictNextDictigetpreveqInst -> doDictNextPrev(inst, scope, DictKeyType.SIGNED_INT, DictValueType.SLICE, DictNextPrevMode.PREV, allowEq = true)
            is TvmDictNextDictugetnextInst -> doDictNextPrev(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.SLICE, DictNextPrevMode.NEXT, allowEq = false)
            is TvmDictNextDictugetnexteqInst -> doDictNextPrev(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.SLICE, DictNextPrevMode.NEXT, allowEq = true)
            is TvmDictNextDictugetprevInst -> doDictNextPrev(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.SLICE, DictNextPrevMode.PREV, allowEq = false)
            is TvmDictNextDictugetpreveqInst -> doDictNextPrev(inst, scope, DictKeyType.UNSIGNED_INT, DictValueType.SLICE, DictNextPrevMode.PREV, allowEq = true)
        }
    }

    // this is actually load_maybe_ref, not necessarily load_dict
    private fun doLoadDict(inst: TvmDictSerialInst, scope: TvmStepScope, returnUpdatedSlice: Boolean) {
        val slice = scope.calcOnStateCtx { stack.takeLastSlice() }
        if (slice == null) {
            scope.doWithState(ctx.throwTypeCheckError)
            return
        }

        val updatedSlice = scope.calcOnState { memory.allocConcrete(TvmSliceType) }
        scope.makeSliceTypeLoad(slice, TvmSymbolicCellMaybeConstructorBit(ctx), updatedSlice) {

            // hide the original [scope] from this closure
            @Suppress("NAME_SHADOWING", "UNUSED_VARIABLE")
            val scope = Unit

            val maybeConstructorTypeBit = slicePreloadDataBits(slice, bits = 1)
                ?: return@makeSliceTypeLoad

            val isNotEmpty = calcOnStateCtx { mkEq(maybeConstructorTypeBit, mkBv(value = 1, sizeBits = 1u)) }

            fork(
                isNotEmpty,
                blockOnFalseState = {
                    addOnStack(ctx.nullValue, TvmNullType)

                    if (returnUpdatedSlice) {
                        updatedSlice.also { sliceCopy(slice, it) }
                        sliceMoveDataPtr(updatedSlice, bits = 1)
                        addOnStack(updatedSlice, TvmSliceType)
                    }

                    newStmt(inst.nextStmt())
                },
            ) ?: return@makeSliceTypeLoad

            doWithStateCtx {
                val dictCellRef = slicePreloadNextRef(slice) ?: return@doWithStateCtx
                addOnStack(dictCellRef, TvmCellType)

                if (returnUpdatedSlice) {
                    updatedSlice.also { sliceCopy(slice, it) }
                    sliceMoveDataPtr(updatedSlice, bits = 1)
                    sliceMoveRefPtr(updatedSlice)
                    addOnStack(updatedSlice, TvmSliceType)
                }

                newStmt(inst.nextStmt())
            }
        }
    }

    private fun doStoreDictToBuilder(inst: TvmDictSerialInst, scope: TvmStepScope) {
        val builder = scope.calcOnStateCtx { stack.takeLastBuilder() }
        if (builder == null) {
            scope.doWithState(ctx.throwTypeCheckError)
            return
        }

        // this instruction can be used to store data cells, not just dict cells
        val dictCellRef = loadDict(scope, assertType = false)

        val resultBuilder = scope.calcOnStateCtx { memory.allocConcrete(TvmBuilderType) }
        scope.doWithStateCtx { builderCopy(builder, resultBuilder) }

        scope.doWithStateCtx {
            if (dictCellRef == null) {
                builderStoreDataBits(resultBuilder, mkBv(value = 0, sizeBits = 1u))
            } else {
                builderStoreDataBits(resultBuilder, mkBv(value = 1, sizeBits = 1u))
                builderStoreNextRef(resultBuilder, dictCellRef)
            }
        }

        scope.doWithStateCtx {
            addOnStack(resultBuilder, TvmBuilderType)
            newStmt(inst.nextStmt())
        }
    }

    private fun doDictSet(
        inst: TvmDictInst,
        scope: TvmStepScope,
        keyType: DictKeyType,
        valueType: DictValueType,
        getOldValue: Boolean,
        mode: DictSetMode
    ) {
        val keyLength = loadKeyLength(scope)
        val dictCellRef = loadDict(scope)
        val key = loadKey(scope, keyType, keyLength) ?: return
        val value = loadValue(scope, valueType)

        if (value == null) {
            scope.doWithState(ctx.throwTypeCheckError)
            return
        }

        val dictId = DictId(keyLength)
        val resultDict = scope.calcOnState { memory.allocConcrete(TvmDictCellType) }

        val dictContainsKey = dictCellRef?.let {
            scope.calcOnState { dictContainsKey(dictCellRef, dictId, key) }
        } ?: ctx.falseExpr

        val oldValue = dictCellRef?.let {
            scope.calcOnState { dictGetValue(dictCellRef, dictId, key) }
        }

        if (dictCellRef != null) {
            assertDictKeyLength(scope, dictCellRef, keyLength)
                ?: return
            scope.doWithState { copyDict(dictCellRef, resultDict, dictId, key.sort) }
        } else {
            scope.doWithStateCtx {
                memory.writeField(resultDict, dictKeyLengthField, int257sort, keyLength.toBv257(), guard = trueExpr)
            }
        }

        scope.doWithState { dictAddKeyValue(resultDict, dictId, key, value) }

        scope.fork(
            dictContainsKey,
            blockOnFalseState = {
                dictSetResultStack(dictCellRef, resultDict, oldValue = null, valueType, mode, getOldValue, keyContains = false)
                newStmt(inst.nextStmt())
            }
        ) ?: return

        require(oldValue != null) {
            "Unexpected null dict that contains key $key"
        }

        val unwrappedOldValue = oldValue.takeIf { getOldValue }?.let {
            unwrapDictValue(scope, it, valueType)
                ?: return
        }
        scope.doWithState {
            dictSetResultStack(dictCellRef, resultDict, unwrappedOldValue, valueType, mode, getOldValue, keyContains = true)
            newStmt(inst.nextStmt())
        }
    }

    private fun TvmState.dictSetResultStack(
        initialDictRef: UHeapRef?,
        result: UHeapRef,
        oldValue: UHeapRef?,
        oldValueType: DictValueType,
        mode: DictSetMode,
        getOldValue: Boolean,
        keyContains: Boolean
    ) {
        val returnOldDict = keyContains && mode == DictSetMode.ADD || !keyContains && mode == DictSetMode.REPLACE
        if (returnOldDict) {
            if (initialDictRef == null) {
                addOnStack(ctx.nullValue, TvmNullType)
            } else {
                addOnStack(initialDictRef, TvmCellType)
            }
        } else {
            addOnStack(result, TvmCellType)
        }

        if (keyContains && getOldValue) {
            require(oldValue != null) {
                "Unexpected null previous dict value to store"
            }
            storeValue(oldValueType, oldValue)
        }

        val status = when (mode) {
            DictSetMode.SET -> if (getOldValue) keyContains else null
            DictSetMode.ADD -> !keyContains
            DictSetMode.REPLACE -> keyContains
        }

        if (status != null) {
            val statusValue = if (status) ctx.trueValue else ctx.falseValue
            stack.addInt(statusValue)
        }
    }

    private fun doDictGet(
        inst: TvmDictGetInst,
        scope: TvmStepScope,
        keyType: DictKeyType,
        valueType: DictValueType
    ) {
        val keyLength = loadKeyLength(scope)
        val dictCellRef = loadDict(scope)
        val key = loadKey(scope, keyType, keyLength) ?: return

        if (dictCellRef == null) {
            scope.doWithStateCtx {
                addOnStack(falseValue, TvmIntegerType)
                newStmt(inst.nextStmt())
            }
            return
        }

        assertDictKeyLength(scope, dictCellRef, keyLength)
            ?: return

        val dictId = DictId(keyLength)

        val dictContainsKey = scope.calcOnState {
            dictContainsKey(dictCellRef, dictId, key)
        }

        val sliceValue = scope.calcOnStateCtx { dictGetValue(dictCellRef, dictId, key) }

        scope.fork(
            dictContainsKey,
            blockOnFalseState = {
                addOnStack(ctx.falseValue, TvmIntegerType)
                newStmt(inst.nextStmt())
            }
        ) ?: return

        val unwrappedValue = unwrapDictValue(scope, sliceValue, valueType)
            ?: return

        scope.doWithState {
            storeValue(valueType, unwrappedValue)
            addOnStack(ctx.trueValue, TvmIntegerType)
            newStmt(inst.nextStmt())
        }
    }

    private fun doDictDelete(
        inst: TvmDictDeleteInst,
        scope: TvmStepScope,
        keyType: DictKeyType,
        valueType: DictValueType,
        getOldValue: Boolean
    ) {
        val keyLength = loadKeyLength(scope)
        val dictCellRef = loadDict(scope)
        val key = loadKey(scope, keyType, keyLength) ?: return

        if (dictCellRef == null) {
            scope.doWithStateCtx {
                addOnStack(nullValue, TvmNullType)
                addOnStack(falseValue, TvmIntegerType)
                newStmt(inst.nextStmt())
            }
            return
        }

        assertDictKeyLength(scope, dictCellRef, keyLength)
            ?: return

        val dictId = DictId(keyLength)

        val dictContainsKey = scope.calcOnState { dictContainsKey(dictCellRef, dictId, key) }

        scope.fork(
            condition = dictContainsKey,
            blockOnFalseState = {
                addOnStack(dictCellRef, TvmCellType)
                addOnStack(ctx.falseValue, TvmIntegerType)
                newStmt(inst.nextStmt())
            }
        ) ?: return

        val value = scope.calcOnState { dictGetValue(dictCellRef, dictId, key) }
        val unwrappedValue = unwrapDictValue(scope, value, valueType)
            ?: return

        handleDictRemoveKey(scope, dictCellRef, dictId, key,
            originalDictContainsKeyEmptyResult = {
                addOnStack(ctx.nullValue, TvmNullType)

                if (getOldValue) {
                    storeValue(valueType, unwrappedValue)
                }

                addOnStack(ctx.trueValue, TvmIntegerType)
                newStmt(inst.nextStmt())
            },
            originalDictContainsKeyNonEmptyResult = { resultDict ->
                addOnStack(resultDict, TvmCellType)

                if (getOldValue) {
                    storeValue(valueType, unwrappedValue)
                }

                addOnStack(ctx.trueValue, TvmIntegerType)
                newStmt(inst.nextStmt())
            }
        )
    }

    private fun doDictMinMax(
        inst: TvmDictMinInst,
        scope: TvmStepScope,
        keyType: DictKeyType,
        valueType: DictValueType,
        mode: DictMinMaxMode,
        removeKey: Boolean
    ){
        val keyLength = loadKeyLength(scope)
        val dictCellRef = loadDict(scope)

        if (dictCellRef == null) {
            scope.doWithStateCtx {
                if (removeKey) {
                    addOnStack(nullValue, TvmNullType)
                }

                addOnStack(falseValue, TvmIntegerType)
                newStmt(inst.nextStmt())
            }
            return
        }

        assertDictKeyLength(scope, dictCellRef, keyLength)
            ?: return

        val dictId = DictId(keyLength)
        val keySort = ctx.mkBvSort(keyLength.toUInt())

        val resultElement = scope.calcOnStateCtx { makeSymbolicPrimitive(keySort) }

        val allSetEntries = scope.calcOnStateCtx {
            memory.setEntries(dictCellRef, dictId, keySort, DictKeyInfo)
        }

        val storedKeys = scope.calcOnStateCtx {
            allSetEntries.entries.map { entry ->
                val setContainsEntry = dictContainsKey(dictCellRef, dictId, entry.setElement)
                entry.setElement to setContainsEntry
            }
        }

        val dictContainsResultElement = scope.calcOnStateCtx {
            dictContainsKey(dictCellRef, dictId, resultElement)
        }

        val resultIsMinMax = scope.calcOnStateCtx {
            storedKeys.map { (storeKey, storedKeyContains) ->
                val compareLessThan = when (mode) {
                    DictMinMaxMode.MIN -> true
                    DictMinMaxMode.MAX -> false
                }
                val cmp = compareKeys(keyType, compareLessThan, allowEq = true, resultElement, storeKey)
                mkImplies(storedKeyContains, cmp)
            }.let { mkAnd(it) }
        }

        scope.assert(
            ctx.mkAnd(dictContainsResultElement, resultIsMinMax),
            unsatBlock = { error("Dict min/max element is not in the dict") }
        ) ?: return

        val value = scope.calcOnState { dictGetValue(dictCellRef, dictId, resultElement) }
        val unwrappedValue = unwrapDictValue(scope, value, valueType)
            ?: return

        if (!removeKey) {
            scope.doWithStateCtx {
                storeValue(valueType, unwrappedValue)
                storeKey(keyType, resultElement)
                addOnStack(trueValue, TvmIntegerType)
                newStmt(inst.nextStmt())
            }
            return
        }

        handleDictRemoveKey(scope, dictCellRef, dictId, resultElement,
            originalDictContainsKeyEmptyResult = {
                addOnStack(ctx.nullValue, TvmNullType)

                storeValue(valueType, unwrappedValue)
                storeKey(keyType, resultElement)
                addOnStack(ctx.trueValue, TvmIntegerType)
                newStmt(inst.nextStmt())
            },
            originalDictContainsKeyNonEmptyResult = { resultDict ->
                addOnStack(resultDict, TvmCellType)

                storeValue(valueType, unwrappedValue)
                storeKey(keyType, resultElement)
                addOnStack(ctx.trueValue, TvmIntegerType)
                newStmt(inst.nextStmt())
            }
        )
    }

    private fun doDictNextPrev(
        inst: TvmDictNextInst,
        scope: TvmStepScope,
        keyType: DictKeyType,
        valueType: DictValueType,
        mode: DictNextPrevMode,
        allowEq: Boolean
    ) {
        val keyLength = loadKeyLength(scope)
        val dictCellRef = loadDict(scope)
        val key = loadKey(scope, keyType, keyLength) ?: return

        if (dictCellRef == null) {
            scope.doWithStateCtx {
                addOnStack(falseValue, TvmIntegerType)
                newStmt(inst.nextStmt())
            }
            return
        }

        assertDictKeyLength(scope, dictCellRef, keyLength)
            ?: return

        val dictId = DictId(keyLength)
        val keySort = ctx.mkBvSort(keyLength.toUInt())

        val resultElement = scope.calcOnStateCtx { makeSymbolicPrimitive(keySort) }

        val allSetEntries = scope.calcOnStateCtx {
            memory.setEntries(dictCellRef, dictId, keySort, DictKeyInfo)
        }

        val storedKeys = scope.calcOnStateCtx {
            allSetEntries.entries.map { entry ->
                val setContainsEntry = dictContainsKey(dictCellRef, dictId, entry.setElement)
                entry.setElement to setContainsEntry
            }
        }

        val dictContainsResultElement = scope.calcOnStateCtx {
            dictContainsKey(dictCellRef, dictId, resultElement)
        }

        val resultIsNextPrev = scope.calcOnStateCtx {
            val compareLessThan = when (mode) {
                DictNextPrevMode.NEXT -> false
                DictNextPrevMode.PREV -> true
            }
            compareKeys(keyType, compareLessThan, allowEq, resultElement, key)
        }

        val resultIsClosest = scope.calcOnStateCtx {
            storedKeys.map { (storeKey, storedKeyContains) ->
                val compareLessThan = when (mode) {
                    DictNextPrevMode.NEXT -> false
                    DictNextPrevMode.PREV -> true
                }
                val storedKeyRelevant = compareKeys(keyType, compareLessThan, allowEq, storeKey, key)

                val compareClosestLessThan = when (mode) {
                    DictNextPrevMode.NEXT -> true
                    DictNextPrevMode.PREV -> false
                }
                val resultIsClosest = compareKeys(
                    keyType, compareClosestLessThan, allowEq = true, resultElement, storeKey
                )

                mkImplies(storedKeyContains and storedKeyRelevant, resultIsClosest)
            }.let { mkAnd(it) }
        }

        val dictHasNextKeyConstraint = ctx.mkAnd(dictContainsResultElement, resultIsNextPrev, resultIsClosest)

        if (!scope.assertIfSat(dictHasNextKeyConstraint)) {
            // There is no next key in the dict
            scope.doWithStateCtx {
                addOnStack(falseValue, TvmIntegerType)
                newStmt(inst.nextStmt())
            }
            return
        }

        scope.doWithStateCtx {
            // explicitly store key
            val setContainsElemLValue = USetEntryLValue(keySort, dictCellRef, resultElement, dictId, DictKeyInfo)
            memory.write(setContainsElemLValue, rvalue = trueExpr, guard = trueExpr)
        }

        val value = scope.calcOnState { dictGetValue(dictCellRef, dictId, resultElement) }
        val unwrappedValue = unwrapDictValue(scope, value, valueType)
            ?: return

        scope.doWithStateCtx {
            storeValue(valueType, unwrappedValue)
            storeKey(keyType, resultElement)
            addOnStack(trueValue, TvmIntegerType)
            newStmt(inst.nextStmt())
        }
    }

    private fun <T : UBvSort> TvmContext.compareKeys(
        keyType: DictKeyType,
        compareLessThan: Boolean,
        allowEq: Boolean,
        left: UExpr<T>,
        right: UExpr<T>
    ): UBoolExpr = when (keyType) {
        DictKeyType.SIGNED_INT -> when {
            compareLessThan && allowEq -> mkBvSignedLessOrEqualExpr(left, right)
            compareLessThan && !allowEq -> mkBvSignedLessExpr(left, right)
            !compareLessThan && allowEq -> mkBvSignedGreaterOrEqualExpr(left, right)
            else -> mkBvSignedGreaterExpr(left, right)
        }
        DictKeyType.SLICE, // todo: check slice comparison
        DictKeyType.UNSIGNED_INT -> when {
            compareLessThan && allowEq -> mkBvUnsignedLessOrEqualExpr(left, right)
            compareLessThan && !allowEq -> mkBvUnsignedLessExpr(left, right)
            !compareLessThan && allowEq -> mkBvUnsignedGreaterOrEqualExpr(left, right)
            else -> mkBvUnsignedGreaterExpr(left, right)
        }
    }

    private fun assertDictKeyLength(scope: TvmStepScope, dict: UHeapRef, keyLength: Int): Unit? = scope.calcOnStateCtx {
        val dictKeyLength = scope.calcOnState { memory.readField(dict, dictKeyLengthField, int257sort) }
        val dictKeyConst = dictKeyLength.intValueOrNull

        if (dictKeyConst == null) {
            // [dict] is input dict, as otherwise [dictKeyLength] would be a constant value

            return@calcOnStateCtx memory.writeField(
                dict,
                dictKeyLengthField,
                int257sort,
                keyLength.toBv257(),
                guard = trueExpr
            )
        }

        if (keyLength != dictKeyConst) {
            throwRealDictError(this)
            return@calcOnStateCtx null
        }

        Unit
    }

    private fun loadKeyLength(scope: TvmStepScope): Int {
        val keyLengthExpr = scope.takeLastIntOrThrowTypeError()

        if (keyLengthExpr !is KBitVecValue<*>) {
            TODO("Non-concrete key length: $keyLengthExpr")
        }

        val keyLength = keyLengthExpr.intValue()

        check(keyLength <= TvmContext.MAX_DATA_LENGTH) {
            "Unexpected key length: $keyLength"
        }
        return keyLength
    }

    // todo: dict is slice?
    // todo: verify key length
    private fun loadDict(scope: TvmStepScope, assertType: Boolean = true): UHeapRef? =
        scope.calcOnState {
            if (stack.lastIsNull()) {
                stack.pop(0)
                null
            } else {
                takeLastCell()?.also {
                    if (assertType) {
                        assertType(it, TvmDictCellType)
                    }
                }
            }
        }

    private fun loadKey(
        scope: TvmStepScope,
        keyType: DictKeyType,
        keyLength: Int
    ): UExpr<UBvSort>? = with(ctx) {
        // todo: handle keyLength errors
        when (keyType) {
            DictKeyType.SIGNED_INT -> scope.takeLastIntOrThrowTypeError()
                ?.let { mkBvExtractExpr(high = keyLength - 1, low = 0, it) }
            DictKeyType.UNSIGNED_INT -> scope.takeLastIntOrThrowTypeError()
                ?.let { mkBvExtractExpr(high = keyLength - 1, low = 0, it) }
            DictKeyType.SLICE -> {
                val slice = scope.calcOnState { stack.takeLastSlice() }
                if (slice == null) {
                    scope.doWithState(throwTypeCheckError)
                    return null
                }

                scope.slicePreloadDataBits(slice, keyLength)
            }
        }
    }

    private fun TvmState.storeKey(keyType: DictKeyType, key: UExpr<UBvSort>) = with(ctx) {
        when (keyType) {
            DictKeyType.SIGNED_INT -> {
                val keyValue = key.signedExtendToInteger()
                addOnStack(keyValue, TvmIntegerType)
            }

            DictKeyType.UNSIGNED_INT -> {
                val keyValue = key.unsignedExtendToInteger()
                addOnStack(keyValue, TvmIntegerType)
            }

            DictKeyType.SLICE -> {
                val resultSlice = allocSliceFromData(key)
                addOnStack(resultSlice, TvmSliceType)
            }
        }
    }

    private fun loadValue(scope: TvmStepScope, valueType: DictValueType) = scope.calcOnState {
        when (valueType) {
            DictValueType.SLICE -> stack.takeLastSlice()
            DictValueType.CELL -> {
                val cell = takeLastCell() ?: return@calcOnState null
                val builder = allocEmptyCell()

                builderStoreNextRef(builder, cell)
                allocSliceFromCell(builder)
            }
            DictValueType.BUILDER -> {
                val builder = stack.takeLastBuilder() ?: return@calcOnState null
                scope.calcOnState { allocSliceFromCell(builder) }
            }
        }
    }

    private fun unwrapDictValue(scope: TvmStepScope, sliceValue: UHeapRef, valueType: DictValueType): UHeapRef? =
        when (valueType) {
            DictValueType.SLICE -> sliceValue
            DictValueType.CELL -> scope.slicePreloadNextRef(sliceValue)
            DictValueType.BUILDER -> error("Unexpected dict value type: $valueType")
        }

    private fun TvmState.storeValue(valueType: DictValueType, value: UHeapRef) {
        when (valueType) {
            DictValueType.SLICE -> addOnStack(value, TvmSliceType)
            DictValueType.CELL -> addOnStack(value, TvmCellType)
            DictValueType.BUILDER -> error("Unexpected value type to store: $valueType")
        }
    }

    private fun handleDictRemoveKey(
        scope: TvmStepScope,
        dictCellRef: UHeapRef,
        dictId: DictId,
        key: UExpr<UBvSort>,
        originalDictContainsKeyEmptyResult: TvmState.() -> Unit,
        originalDictContainsKeyNonEmptyResult: TvmState.(UHeapRef) -> Unit,
    ) {
        val resultDict = scope.calcOnState { memory.allocConcrete(TvmCellType) }

        scope.doWithStateCtx {
            copyDict(dictCellRef, resultDict, dictId, key.sort)
            dictRemoveKey(resultDict, dictId, key)
        }

        val resultSetEntries = scope.calcOnStateCtx {
            memory.setEntries(resultDict, dictId, key.sort, DictKeyInfo)
        }

        val resultSetContainsAnyStoredKey = scope.calcOnStateCtx {
            resultSetEntries.entries.map { entry ->
                dictContainsKey(resultDict, dictId, entry.setElement)
            }.let { mkOr(it) }
        }

        val resultKeySetIsEmpty = scope.calcOnStateCtx {
            // todo: empty input dict
            mkOr(resultSetContainsAnyStoredKey, mkBool(resultSetEntries.isInput))
        }

        val resultDictIsEmpty = scope.calcOnStateCtx { resultKeySetIsEmpty }

        scope.fork(
            resultDictIsEmpty,
            blockOnTrueState = { originalDictContainsKeyEmptyResult() },
            blockOnFalseState = { originalDictContainsKeyNonEmptyResult(resultDict) },
        )
    }

    private enum class DictKeyType {
        SIGNED_INT, UNSIGNED_INT, SLICE
    }

    private enum class DictSetMode {
        SET, // always set
        ADD, // set only if absent
        REPLACE // set only if present
    }

    private enum class DictMinMaxMode {
        MIN, MAX
    }

    private enum class DictNextPrevMode {
        NEXT, PREV
    }

    private enum class DictValueType {
        SLICE, CELL, BUILDER
    }
}
