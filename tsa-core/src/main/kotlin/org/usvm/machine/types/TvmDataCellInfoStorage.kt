package org.usvm.machine.types

import org.ton.TvmAtomicDataCellLabel
import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmDataCellStructure
import org.ton.TvmInputInfo
import org.ton.TvmParameterInfo
import org.usvm.UBoolExpr
import org.usvm.UHeapRef
import org.usvm.machine.TvmContext
import org.usvm.machine.state.TvmMethodResult
import org.usvm.machine.state.TvmState
import org.usvm.machine.types.dp.CalculatedTlbLabelInfo
import org.usvm.mkSizeGeExpr
import org.usvm.mkSizeLeExpr
import org.usvm.utils.extractAddresses


class TvmDataCellInfoStorage private constructor(
    private val ctx: TvmContext,
    val mapper: TvmAddressToLabelMapper,
    val sliceMapper: TvmSliceToTlbStackMapper,
) {
    fun notifyAboutChildRequest(state: TvmState, ref: UHeapRef) = with(ctx) {
        val staticAddresses = extractAddresses(ref).map { it.second }

        staticAddresses.forEach {
            if (state.methodResult is TvmMethodResult.NoCall) {
                mapper.initializeConstraintsForChildren(state, it)
            }
        }
    }

    fun getLabelForFreshSlice(cellRef: UHeapRef): Map<TvmParameterInfo.CellInfo, UBoolExpr> = with(ctx) {
        val staticAddresses = extractAddresses(cellRef, extractAllocated = true)
        val result = hashMapOf<TvmParameterInfo.CellInfo, UBoolExpr>()

        staticAddresses.forEach { (guard, ref) ->
            val labelInfo = mapper.getLabelInfo(ref) ?: LabelInfo(mapOf(TvmParameterInfo.UnknownCellInfo to trueExpr))
            labelInfo.variants.forEach { (info, innerGuard) ->
                val oldGuard = result[info] ?: falseExpr
                result[info] = oldGuard or (guard and innerGuard)
            }
        }

        return result
    }

    fun getNoUnexpectedEndOfReadingCondition(
        state: TvmState,
        endOfCell: TvmDataCellLoadedTypeInfo.EndOfCell
    ): UBoolExpr = with(ctx) {
        val labelInfo = mapper.getLabelInfo(endOfCell.cellAddress)
            ?: return trueExpr
        return labelInfo.variants.entries.fold(trueExpr as UBoolExpr) { acc, (curInfo, guard) ->
            if (curInfo !is TvmParameterInfo.DataCellInfo) {
                // case of DictCell: do nothing
                return@fold acc
            }
            when (val label = curInfo.dataCellStructure) {
                is TvmAtomicDataCellLabel -> {
                    val refNumberGuard = endOfCell.refNumber eq zeroSizeExpr
                    val dataLengthGuard = endOfCell.offset eq label.offset(state, endOfCell.cellAddress, zeroSizeExpr)
                    acc and (guard implies (refNumberGuard and dataLengthGuard))
                }
                is TvmCompositeDataCellLabel -> {
                    val leafInfo = mapper.calculatedTlbLabelInfo.getLeavesInfo(state, endOfCell.cellAddress, label)
                        ?: return@fold acc

                    leafInfo.fold(acc) { innerAcc, (struct, sizeInfo) ->
                        when (struct) {
                            is TvmDataCellStructure.Unknown -> {
                                val newGuard = mkSizeGeExpr(endOfCell.offset, sizeInfo.dataLength) and
                                        mkSizeGeExpr(endOfCell.refNumber, sizeInfo.refsLength)
                                innerAcc and ((guard and sizeInfo.guard) implies  newGuard)
                            }

                            is TvmDataCellStructure.Empty -> {
                                val newGuard = (endOfCell.offset eq sizeInfo.dataLength) and
                                        (endOfCell.refNumber eq sizeInfo.refsLength)
                                innerAcc and ((guard and sizeInfo.guard) implies  newGuard)
                            }
                        }
                    }
                }
            }
        }
    }

    fun getNoUnexpectedLoadRefCondition(
        state: TvmState,
        loadRef: TvmDataCellLoadedTypeInfo.LoadRef,
    ): UBoolExpr = with(ctx) {
        val labelInfo = mapper.getLabelInfo(loadRef.cellAddress)
            ?: return trueExpr
        return labelInfo.variants.entries.fold(trueExpr as UBoolExpr) { acc, (curInfo, guard) ->
            if (curInfo !is TvmParameterInfo.DataCellInfo) {
                // TODO: throw error for treating DictCell as DataCell
                return@fold acc
            }
            when (val label = curInfo.dataCellStructure) {
                is TvmAtomicDataCellLabel -> {
                    acc and (guard implies (loadRef.refNumber eq zeroSizeExpr))
                }
                is TvmCompositeDataCellLabel -> {
                    val leafInfo = mapper.calculatedTlbLabelInfo.getLeavesInfo(state, loadRef.cellAddress, label)
                        ?: return@fold acc

                    leafInfo.fold(acc) { innerAcc, (struct, sizeInfo) ->
                        when (struct) {
                            is TvmDataCellStructure.Unknown -> {
                                innerAcc
                            }
                            is TvmDataCellStructure.Empty -> {
                                val newGuard = mkSizeLeExpr(loadRef.refNumber, sizeInfo.refsLength)
                                innerAcc and ((guard and sizeInfo.guard) implies newGuard)
                            }
                        }
                    }
                }
            }
        }
    }

    fun clone(): TvmDataCellInfoStorage = TvmDataCellInfoStorage(ctx, mapper, sliceMapper.clone())

    companion object {
        fun build(
            checkDataCellContentTypes: Boolean,
            excludeInputsThatDoNotMatchGivenScheme: Boolean,
            state: TvmState,
            info: TvmInputInfo,
        ): TvmDataCellInfoStorage {
            if (!checkDataCellContentTypes) {
                val calculatedTlbLabelInfo = CalculatedTlbLabelInfo(state.ctx, emptyList())
                val emptyInputInfo = InputParametersStructure(emptyMap(), emptyMap())
                val mapper = TvmAddressToLabelMapper(state, emptyInputInfo, calculatedTlbLabelInfo, excludeInputsThatDoNotMatchGivenScheme)
                return TvmDataCellInfoStorage(state.ctx, mapper, TvmSliceToTlbStackMapper())
            }

            val inputAddresses = extractInputParametersAddresses(state, info)
            val labels = inputAddresses.cellToInfo.values.mapNotNull {
                (it as? TvmParameterInfo.DataCellInfo)?.dataCellStructure as? TvmCompositeDataCellLabel
            }
            val calculatedTlbLabelInfo = CalculatedTlbLabelInfo(state.ctx, labels)
            val mapper = TvmAddressToLabelMapper(state, inputAddresses, calculatedTlbLabelInfo, excludeInputsThatDoNotMatchGivenScheme)
            val sliceMapper = TvmSliceToTlbStackMapper.constructInitialSliceMapper(state.ctx, inputAddresses)

            return TvmDataCellInfoStorage(state.ctx, mapper, sliceMapper)
        }
    }
}
