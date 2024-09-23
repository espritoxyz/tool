package org.usvm.machine.types

import org.ton.TvmAtomicDataCellLabel
import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmParameterInfo
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef
import org.usvm.api.readField
import org.usvm.isTrue
import org.usvm.machine.TvmContext
import org.usvm.machine.state.TvmMethodResult
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.readCellRef
import org.usvm.machine.types.dp.CalculatedTlbLabelInfo
import org.usvm.mkSizeExpr
import org.usvm.model.UModelBase
import org.usvm.sizeSort
import kotlin.math.max

class TvmAddressToLabelMapper(
    state: TvmState,
    inputInfo: InputParametersStructure,
    val calculatedTlbLabelInfo: CalculatedTlbLabelInfo,
    private val excludeInputsThatDoNotMatchGivenScheme: Boolean,
) {
    private val addressToLabels = hashMapOf<UConcreteHeapRef, LabelInfo>()
    private val grandchildrenOfAddressInitialized = hashSetOf<UConcreteHeapRef>()
    private val initialAddresses = hashSetOf<UConcreteHeapRef>()  // will be set during init
    private val structuralConstraintCalculatedFor = hashSetOf<UConcreteHeapRef>()

    fun structuralConstraintsWereCalculated(ref: UConcreteHeapRef): Boolean =
        ref in structuralConstraintCalculatedFor

    fun addressWasGiven(ref: UConcreteHeapRef) = ref in addressToLabels

    fun getLabelInfo(address: UConcreteHeapRef) = addressToLabels[address]

    private fun getChildrenAddresses(state: TvmState, ref: UConcreteHeapRef): List<UConcreteHeapRef> {
        val parentStruct = addressToLabels[ref]
        check(parentStruct != null) {
            "Must call getChildrenAddresses only for refs that TvmAddressToLabelMapper already knows about, but got $ref"
        }
        val maxRefs = parentStruct.variants.keys.fold(0) { acc, cellInfo ->
            if (cellInfo is TvmParameterInfo.DataCellInfo && cellInfo.dataCellStructure is TvmCompositeDataCellLabel) {
                max(acc, calculatedTlbLabelInfo.maxRefSize(cellInfo.dataCellStructure) ?: 0)
            } else {
                acc
            }
        }

        return List(maxRefs) {
            // resulting address is concrete because ref is concrete
            state.readCellRef(ref, state.ctx.mkSizeExpr(it), initConstraintsForChildren = false) as UConcreteHeapRef
        }
    }

    private fun generateLabelInfoForChildren(state: TvmState, ref: UConcreteHeapRef) = with(state.ctx) {
        val parentStruct = addressToLabels[ref]
            ?: error("CellInfo of ref $ref must be known at this point")
        check(ref !in grandchildrenOfAddressInitialized) {
            "Grandchildren must not be known yet"
        }

        val childAddresses = getChildrenAddresses(state, ref)
        check(childAddresses.all { it !in addressToLabels }) {
            "Structure of address must be set only once"
        }

        val childLabels = childAddresses.map { hashMapOf<TvmParameterInfo.CellInfo, UBoolExpr>() }

        parentStruct.variants.forEach { (cellInfo, guard) ->
            if (cellInfo !is TvmParameterInfo.DataCellInfo || cellInfo.dataCellStructure !is TvmCompositeDataCellLabel) {
                return@forEach
            }

            for (childIdx in 0..<TvmContext.MAX_REFS_NUMBER) {
                val childInfo = calculatedTlbLabelInfo.getLabelChildStructure(
                    state,
                    ref,
                    cellInfo.dataCellStructure,
                    childIdx
                ) ?: run {
                    check(calculatedTlbLabelInfo.labelHasUnknownLeaves(cellInfo.dataCellStructure) == true) {
                        "ChildStructure can be null only for children of labels with unknown leaves, " +
                            "but it is null for ref $childIdx of ${cellInfo.dataCellStructure}"
                    }
                    // TODO: add info about known children of parents with unknown leaves
                    mapOf(TvmParameterInfo.UnknownCellInfo to trueExpr)
                }

                childInfo.forEach { (childCellInfo, childGuard) ->
                    val oldChildGuard = childLabels[childIdx][childCellInfo] ?: falseExpr
                    childLabels[childIdx][childCellInfo] = oldChildGuard or (guard and childGuard)
                }
            }
        }

        (childAddresses zip childLabels).forEach { (childAddress, labelInfo) ->
            addressToLabels[childAddress] = LabelInfo(labelInfo)
        }
    }

    fun getLabelFromModel(model: UModelBase<TvmType>, ref: UConcreteHeapRef): TvmParameterInfo.CellInfo {
        val labelInfo = addressToLabels[ref]
        check(labelInfo != null) {
            "Must call this method only for refs which structures are known. No info about $ref"
        }
        return labelInfo.variants.entries.firstOrNull { (_, guard) ->
            model.eval(guard).isTrue
        }?.key 
            ?: error("One of the guards in LabelInfo for $ref must be true in the given model.")
    }

    fun initializeConstraintsForChildren(state: TvmState, ref: UConcreteHeapRef) = with(state.ctx) {
        check(state.methodResult is TvmMethodResult.NoCall) {
            "initializeAddressChildren should be called only when the state is not terminated, but " +
                    "given state's result is ${state.methodResult}"
        }

        if (ref !in addressToLabels || ref in grandchildrenOfAddressInitialized) {
            return
        }

        grandchildrenOfAddressInitialized.add(ref)

        val childAddresses = getChildrenAddresses(state, ref)
        check(childAddresses.all { it in addressToLabels }) {
            "Children's cellInfo must be known at this point"
        }

        val structuralConstraints = childAddresses.fold(trueExpr as UBoolExpr) { acc, childAddress ->
            // generate grandchildren of [ref]
            generateLabelInfoForChildren(state, childAddress)

            acc and generateStructuralConstraints(state, childAddress)
        }

        state.globalStructuralConstraintsHolder.addStructuralConstraint(structuralConstraints)
    }

    private fun generateStructuralConstraints(state: TvmState, ref: UConcreteHeapRef): UBoolExpr = with(state.ctx) {
        val labelInfo = addressToLabels[ref]
        check(labelInfo != null) {
            "At this point label info for $ref must be calculated"
        }
        check(ref !in structuralConstraintCalculatedFor) {
            "Structural constraints must be calculated for each address only once"
        }
        structuralConstraintCalculatedFor.add(ref)

        if (!excludeInputsThatDoNotMatchGivenScheme) {
            return trueExpr
        }

        labelInfo.variants.entries.fold(trueExpr as UBoolExpr) { acc, (cellInfo, guard) ->
            if (cellInfo !is TvmParameterInfo.DataCellInfo) {
                return@fold acc
            }

            val curGuard = when (val label = cellInfo.dataCellStructure) {
                is TvmAtomicDataCellLabel -> {
                    val dataLength = label.offset(state, ref, zeroSizeExpr)
                    val dataLengthField = state.memory.readField(ref, TvmContext.cellDataLengthField, sizeSort)
                    val refsLengthField = state.memory.readField(ref, TvmContext.cellRefsLengthField, sizeSort)
                    (dataLengthField eq dataLength) and (refsLengthField eq zeroSizeExpr)
                }

                is TvmCompositeDataCellLabel -> {
                    val sizeConstraints = calculatedTlbLabelInfo.getSizeConstraints(state, ref, label)
                    val switchConstraints = calculatedTlbLabelInfo.getSwitchConstraints(state, ref, label)
                    check((sizeConstraints == null) == (switchConstraints == null)) {
                        "sizeConstraints and switchConstraints must be null simultaneously"
                    }
                    if (sizeConstraints == null || switchConstraints == null) {
                        trueExpr
                    } else {
                        sizeConstraints and switchConstraints
                    }
                }
            }

            acc and (guard implies curGuard)
        }
    }

    fun getInitialStructuralConstraints(initialState: TvmState): UBoolExpr =
        initialAddresses.fold(initialState.ctx.trueExpr as UBoolExpr) { acc, ref ->
            initialState.ctx.mkAnd(acc, generateStructuralConstraints(initialState, ref))
        }

    init {
        inputInfo.cellToInfo.forEach { (address, cellInfo) ->
            addressToLabels[address] = LabelInfo(mapOf(cellInfo to state.ctx.trueExpr))
            initialAddresses.add(address)
            generateLabelInfoForChildren(state, address)
        }
    }
}

@JvmInline
value class LabelInfo(
    val variants: Map<TvmParameterInfo.CellInfo, UBoolExpr>,
)
