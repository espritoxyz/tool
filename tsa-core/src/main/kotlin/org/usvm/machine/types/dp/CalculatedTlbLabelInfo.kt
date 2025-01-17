package org.usvm.machine.types.dp

import kotlinx.collections.immutable.persistentListOf
import org.ton.TlbCompositeLabel
import org.ton.TlbStructure
import org.ton.TvmParameterInfo
import org.usvm.UBoolExpr
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.state.TvmState
import org.usvm.machine.types.forEach
import org.usvm.test.resolver.TvmTestDataCellValue

class CalculatedTlbLabelInfo(
    private val ctx: TvmContext,
    givenCompositeLabels: Collection<TlbCompositeLabel>,
) {
    private val compositeLabels = calculateClosure(givenCompositeLabels)

    private val maxTlbDepth: Int
        get() = ctx.tvmOptions.tlbOptions.maxTlbDepth

    fun labelHasUnknownLeaves(label: TlbCompositeLabel): Boolean? = hasUnknownLeaves[label]

    fun minimalLabelDepth(label: TlbCompositeLabel): Int? = minTlbDepth[label]

    fun maxRefSize(label: TlbCompositeLabel, maxDepth: Int = maxTlbDepth): Int? {
        require(maxDepth in 0..maxTlbDepth) {
            "Cannot calculate maxRefSize for depth $maxDepth"
        }
        return maxRefSizes[maxDepth][label]
    }

    fun getDataCellSize(
        state: TvmState,
        address: UConcreteHeapRef,
        label: TlbCompositeLabel,
        maxDepth: Int = maxTlbDepth,
    ): UExpr<TvmSizeSort>? {
        require(maxDepth in 0..maxTlbDepth) {
            "Cannot calculate dataCellSize for depth $maxDepth"
        }
        val abstractValue = dataLengths[maxDepth][label] ?: return null
        return abstractValue.apply(AbstractionForUExpr(address, ctx.zeroSizeExpr, persistentListOf(), state))
    }

    fun getLabelChildStructure(
        state: TvmState,
        address: UConcreteHeapRef,
        parentLabel: TlbCompositeLabel,
        childIdx: Int,
        maxDepth: Int = maxTlbDepth,
    ): Map<TvmParameterInfo.CellInfo, UBoolExpr>? {
        require(childIdx in 0..<TvmContext.MAX_REFS_NUMBER) {
            "childIdx $childIdx is out of range"
        }
        require(maxDepth in 0..maxTlbDepth) {
            "Cannot calculate childLabel for depth $maxDepth"
        }
        val childStructure = labelChildren[maxDepth][parentLabel]?.children?.get(childIdx)
            ?: return null
        return childStructure.variants.entries.associate { (struct, abstractGuard) ->
            val guard = abstractGuard.apply(AbstractionForUExpr(address, ctx.zeroSizeExpr, persistentListOf(), state))
            struct to guard
        }
    }

    fun getConditionForNumberOfChildrenExceeded(
        state: TvmState,
        address: UConcreteHeapRef,
        parentLabel: TlbCompositeLabel,
        maxDepth: Int = maxTlbDepth,
    ): UBoolExpr? {
        require(maxDepth in 0..maxTlbDepth) {
            "Cannot calculate conditionForNumberOfChildrenExceeded for depth $maxDepth"
        }
        val childrenStructure = labelChildren[maxDepth][parentLabel]
            ?: return null
        return childrenStructure.numberOfChildrenExceeded.apply(AbstractionForUExpr(address, ctx.zeroSizeExpr, persistentListOf(), state))
    }

    fun getDataConstraints(
        state: TvmState,
        address: UConcreteHeapRef,
        label: TlbCompositeLabel,
        maxDepth: Int = maxTlbDepth,
    ): UBoolExpr? {
        require(maxDepth in 0..maxTlbDepth) {
            "Cannot calculate switch constraints for depth $maxDepth"
        }
        val abstract = dataConstraints[maxDepth][label]
            ?: return null
        return abstract.apply(AbstractionForUExpr(address, ctx.zeroSizeExpr, persistentListOf(), state))
    }

    fun getIndividualTlbDepthBound(label: TlbCompositeLabel): Int? = individualMaxCellTlbDepth[label]

    fun getDefaultCell(label: TlbCompositeLabel): TvmTestDataCellValue? =
        defaultCells[label]

    fun getSizeConstraints(
        state: TvmState,
        address: UConcreteHeapRef,
        label: TlbCompositeLabel,
        maxDepth: Int = maxTlbDepth,
    ): UBoolExpr? {
        require(maxDepth in 1..maxTlbDepth) {
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
        label: TlbCompositeLabel,
        maxDepth: Int = maxTlbDepth,
    ): List<Pair<TlbStructure.Leaf, VertexCalculatedSize>>? {
        require(maxDepth in 1..maxTlbDepth) {
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

    private val hasUnknownLeaves: Map<TlbCompositeLabel, Boolean> = compositeLabels.associateWith {
        hasUnknownLeaves(it)
    }

    private val labelsWithoutUnknownLeaves = compositeLabels.filter { hasUnknownLeaves[it] == false }

    private val minTlbDepth: Map<TlbCompositeLabel, Int> = calculateMinTlbDepth(maxTlbDepth, compositeLabels)

    private val individualMaxCellTlbDepth: Map<TlbCompositeLabel, Int> =
        calculateMaxCellTlbDepths(maxTlbDepth, compositeLabels)

    private val defaultCells: Map<TlbCompositeLabel, TvmTestDataCellValue> =
        calculateDefaultCells(ctx, compositeLabels, individualMaxCellTlbDepth)

    private val maxRefSizes: List<Map<TlbCompositeLabel, Int>> =
        calculateMaximumRefs(maxTlbDepth, compositeLabels, individualMaxCellTlbDepth)

    private val dataLengths: List<Map<TlbCompositeLabel, AbstractSizeExpr>> =
        calculateDataLengths(ctx, labelsWithoutUnknownLeaves, individualMaxCellTlbDepth)

    private val labelChildren: List<Map<TlbCompositeLabel, ChildrenStructure>> =
        calculateChildrenStructures(ctx, labelsWithoutUnknownLeaves, dataLengths, individualMaxCellTlbDepth)

    private val dataConstraints: List<Map<TlbCompositeLabel, AbstractGuard>> =
        calculateDataConstraints(ctx, compositeLabels, dataLengths, individualMaxCellTlbDepth)

    init {
        // check correctness of declarations
        compositeLabels.forEach {
            it.internalStructure.forEach { struct ->
                if (struct is TlbStructure.KnownTypePrefix && struct.typeLabel is TlbCompositeLabel) {
                    require(hasUnknownLeaves[struct.typeLabel] != true) {
                        "Declarations with `Unknown` cannot be used in other declarations"
                    }
                }
            }
        }

        // check that all minDepths are <= maxTlbDepth
        compositeLabels.forEach {
            require(it in minTlbDepth) {
                "Minimal depth of ${it.name} is greater than maxTlbDepth=$maxTlbDepth"
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

private fun hasUnknownLeaves(label: TlbCompositeLabel): Boolean =
    hasUnknownLeaves(label.internalStructure)

private fun hasUnknownLeaves(struct: TlbStructure): Boolean =
    when (struct) {
        is TlbStructure.Empty -> false
        is TlbStructure.Unknown -> true
        is TlbStructure.KnownTypePrefix -> hasUnknownLeaves(struct.rest)
        is TlbStructure.SwitchPrefix -> struct.variants.any { hasUnknownLeaves(it.value) }
        is TlbStructure.LoadRef -> hasUnknownLeaves(struct.rest)
    }

private fun calculateClosure(labels: Collection<TlbCompositeLabel>): Set<TlbCompositeLabel> {
    val result = labels.toMutableSet()
    val queue = ArrayDeque(labels)
    while (queue.isNotEmpty()) {
        val label = queue.removeFirst()
        label.internalStructure.forEach { struct ->
            if (struct is TlbStructure.KnownTypePrefix && struct.typeLabel is TlbCompositeLabel) {
                val newLabel = struct.typeLabel
                if (newLabel !in result) {
                    result.add(newLabel)
                    queue.add(newLabel)
                }
            }
            if (struct is TlbStructure.LoadRef && struct.ref is TvmParameterInfo.DataCellInfo) {
                val newLabel = struct.ref.dataCellStructure
                if (newLabel is TlbCompositeLabel && newLabel !in result) {
                    result.add(newLabel)
                    queue.add(newLabel)
                }
            }
        }
    }
    return result
}
