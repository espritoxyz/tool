package org.usvm.machine.types.dp

import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmDataCellStructure
import org.ton.TvmParameterInfo
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.state.TvmState
import org.usvm.machine.types.forEach
import org.usvm.test.resolver.TvmTestDataCellValue

const val MAX_TLB_DEPTH = 10
const val MAX_CELL_DEPTH_FOR_DEFAULT = 10

class CalculatedTlbLabelInfo(
    private val ctx: TvmContext,
    givenCompositeLabels: Collection<TvmCompositeDataCellLabel>,
) {
    private val compositeLabels = calculateClosure(givenCompositeLabels)

    fun labelHasUnknownLeaves(label: TvmCompositeDataCellLabel): Boolean? = hasUnknownLeaves[label]

    fun minimalLabelDepth(label: TvmCompositeDataCellLabel): Int? = minTlbDepth[label]

    fun maxRefSize(label: TvmCompositeDataCellLabel, maxDepth: Int = MAX_TLB_DEPTH): Int? {
        require(maxDepth in 0..MAX_TLB_DEPTH) {
            "Cannot calculate maxRefSize for depth $maxDepth"
        }
        return maxRefSizes[maxDepth][label]
    }

    fun getDataCellSize(
        state: TvmState,
        address: UConcreteHeapRef,
        label: TvmCompositeDataCellLabel,
        maxDepth: Int = MAX_TLB_DEPTH,
    ): UExpr<TvmSizeSort>? {
        require(maxDepth in 0..MAX_TLB_DEPTH) {
            "Cannot calculate dataCellSize for depth $maxDepth"
        }
        val abstractValue = dataLengths[maxDepth][label] ?: return null
        return abstractValue.apply(AbstractionForUExpr(address, ctx.zeroSizeExpr, state))
    }

    fun getLabelChildStructure(
        state: TvmState,
        address: UConcreteHeapRef,
        parentLabel: TvmCompositeDataCellLabel,
        childIdx: Int,
        maxDepth: Int = MAX_TLB_DEPTH,
    ): Map<TvmParameterInfo.CellInfo, UBoolExpr>? {
        require(childIdx in 0..<TvmContext.MAX_REFS_NUMBER) {
            "childIdx $childIdx is out of range"
        }
        require(maxDepth in 0..MAX_TLB_DEPTH) {
            "Cannot calculate childLabel for depth $maxDepth"
        }
        val childStructure = labelChildren[maxDepth][parentLabel]?.children?.get(childIdx)
            ?: return null
        return childStructure.variants.entries.associate { (struct, abstractGuard) ->
            val guard = abstractGuard.apply(AbstractionForUExpr(address, ctx.zeroSizeExpr, state))
            struct to guard
        }
    }

    fun getConditionForNumberOfChildrenExceeded(
        state: TvmState,
        address: UConcreteHeapRef,
        parentLabel: TvmCompositeDataCellLabel,
        maxDepth: Int = MAX_TLB_DEPTH,
    ): UBoolExpr? {
        require(maxDepth in 0..MAX_TLB_DEPTH) {
            "Cannot calculate conditionForNumberOfChildrenExceeded for depth $maxDepth"
        }
        val childrenStructure = labelChildren[maxDepth][parentLabel]
            ?: return null
        return childrenStructure.numberOfChildrenExceeded.apply(AbstractionForUExpr(address, ctx.zeroSizeExpr, state))
    }

    fun getSwitchConstraints(
        state: TvmState,
        address: UConcreteHeapRef,
        label: TvmCompositeDataCellLabel,
        maxDepth: Int = MAX_TLB_DEPTH,
    ): UBoolExpr? {
        require(maxDepth in 0..MAX_TLB_DEPTH) {
            "Cannot calculate switch constraints for depth $maxDepth"
        }
        val abstract = switchConstraints[maxDepth][label]
            ?: return null
        return abstract.apply(AbstractionForUExpr(address, ctx.zeroSizeExpr, state))
    }

    fun getIndividualTlbDepthBound(label: TvmCompositeDataCellLabel): Int? = individualMaxCellTlbDepth[label]

    fun getDefaultCell(label: TvmCompositeDataCellLabel): TvmTestDataCellValue? =
        defaultCells[label]

    fun getSizeConstraints(
        state: TvmState,
        address: UConcreteHeapRef,
        label: TvmCompositeDataCellLabel,
        maxDepth: Int = MAX_TLB_DEPTH,
    ): UBoolExpr? {
        require(maxDepth in 1..MAX_TLB_DEPTH) {
            "Cannot calculate size constraints for depth $maxDepth"
        }
        if (label !in compositeLabels) {
            return null
        }
        return calculateGeneralSizeConstraints(
            address,
            state,
            label.internalStructure,
            dataLengths[maxDepth - 1],
            labelChildren[maxDepth - 1],
        )
    }

    fun getLeavesInfo(
        state: TvmState,
        address: UConcreteHeapRef,
        label: TvmCompositeDataCellLabel,
        maxDepth: Int = MAX_TLB_DEPTH,
    ): List<Pair<TvmDataCellStructure.Leaf, VertexCalculatedSize>>? {
        require(maxDepth in 1..MAX_TLB_DEPTH) {
            "Cannot calculate information about sizes for depth $maxDepth"
        }
        if (label !in compositeLabels) {
            return null
        }
        return calculateSizeInfoForLeaves(
            address,
            state,
            label.internalStructure,
            dataLengths[maxDepth - 1],
            labelChildren[maxDepth - 1],
        )
    }

    private val hasUnknownLeaves: Map<TvmCompositeDataCellLabel, Boolean> = compositeLabels.associateWith {
        hasUnknownLeaves(it)
    }

    private val labelsWithoutUnknownLeaves = compositeLabels.filter { hasUnknownLeaves[it] == false }

    private val minTlbDepth: Map<TvmCompositeDataCellLabel, Int> = calculateMinTlbDepth(compositeLabels)

    private val individualMaxCellTlbDepth: Map<TvmCompositeDataCellLabel, Int> =
        calculateMaxCellTlbDepths(compositeLabels)

    private val defaultCells: Map<TvmCompositeDataCellLabel, TvmTestDataCellValue> =
        calculateDefaultCells(compositeLabels, individualMaxCellTlbDepth)

    private val maxRefSizes: List<Map<TvmCompositeDataCellLabel, Int>> =
        calculateMaximumRefs(compositeLabels, individualMaxCellTlbDepth)

    private val dataLengths: List<Map<TvmCompositeDataCellLabel, AbstractSizeExpr>> =
        calculateDataLengths(ctx, labelsWithoutUnknownLeaves, individualMaxCellTlbDepth)

    private val labelChildren: List<Map<TvmCompositeDataCellLabel, ChildrenStructure>> =
        calculateChildrenStructures(ctx, labelsWithoutUnknownLeaves, dataLengths, individualMaxCellTlbDepth)

    private val switchConstraints: List<Map<TvmCompositeDataCellLabel, AbstractGuard>> =
        calculateSwitchConstraints(ctx, compositeLabels, dataLengths, individualMaxCellTlbDepth)

    init {
        // check correctness of declarations
        compositeLabels.forEach {
            it.internalStructure.forEach { struct ->
                if (struct is TvmDataCellStructure.KnownTypePrefix && struct.typeOfPrefix is TvmCompositeDataCellLabel) {
                    require(hasUnknownLeaves[struct.typeOfPrefix] != true) {
                        "Declarations with `Unknown` cannot be used in other declarations"
                    }
                }
            }
        }

        // check that all minDepths are <= MAX_TLB_DEPTH
        compositeLabels.forEach {
            require(it in minTlbDepth) {
                "Minimal depth of ${it.name} is greater than MAX_TLB_DEPTH=$MAX_TLB_DEPTH"
            }
        }

        // check that we have default cells for all labels
        compositeLabels.forEach {
            require(it in defaultCells) {
                "Couldn't calculate default cell for label ${it.name}"
            }
        }
    }
}

private fun hasUnknownLeaves(label: TvmCompositeDataCellLabel): Boolean =
    hasUnknownLeaves(label.internalStructure)

private fun hasUnknownLeaves(struct: TvmDataCellStructure): Boolean =
    when (struct) {
        is TvmDataCellStructure.Empty -> false
        is TvmDataCellStructure.Unknown -> true
        is TvmDataCellStructure.KnownTypePrefix -> hasUnknownLeaves(struct.rest)
        is TvmDataCellStructure.SwitchPrefix -> struct.variants.any { hasUnknownLeaves(it.value) }
        is TvmDataCellStructure.LoadRef -> hasUnknownLeaves(struct.selfRest)
    }

private fun calculateClosure(labels: Collection<TvmCompositeDataCellLabel>): Set<TvmCompositeDataCellLabel> {
    val result = labels.toMutableSet()
    val queue = ArrayDeque(labels)
    while (queue.isNotEmpty()) {
        val label = queue.removeFirst()
        label.internalStructure.forEach { struct ->
            if (struct is TvmDataCellStructure.KnownTypePrefix && struct.typeOfPrefix is TvmCompositeDataCellLabel) {
                val newLabel = struct.typeOfPrefix
                if (newLabel !in result) {
                    result.add(newLabel)
                    queue.add(newLabel)
                }
            }
            if (struct is TvmDataCellStructure.LoadRef && struct.ref is TvmParameterInfo.DataCellInfo) {
                val newLabel = struct.ref.dataCellStructure
                if (newLabel is TvmCompositeDataCellLabel && newLabel !in result) {
                    result.add(newLabel)
                    queue.add(newLabel)
                }
            }
        }
    }
    return result
}
