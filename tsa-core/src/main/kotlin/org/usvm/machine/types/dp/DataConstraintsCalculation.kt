package org.usvm.machine.types.dp

import org.ton.TlbCompositeLabel
import org.ton.TlbStructure
import org.ton.TlbIntegerLabelOfConcreteSize
import org.usvm.api.readField
import org.usvm.machine.TvmContext
import org.usvm.machine.state.preloadDataBitsFromCellWithoutChecks
import org.usvm.machine.types.TlbVariableField
import org.usvm.mkSizeExpr
import org.usvm.sizeSort

fun calculateDataConstraints(
    ctx: TvmContext,
    labels: Collection<TlbCompositeLabel>,
    dataLengths: List<Map<TlbCompositeLabel, AbstractSizeExpr>>,
    individualMaxCellTlbDepth: Map<TlbCompositeLabel, Int>,
): List<Map<TlbCompositeLabel, AbstractGuard>> =
    calculateMapsByTlbDepth(ctx.tvmOptions.tlbOptions.maxTlbDepth, labels) { label, curDepth, prevDepthValues ->
        val tlbDepthBound = individualMaxCellTlbDepth[label]
            ?: error("individualMaxCellTlbDepth must be calculated for all labels")

        if (tlbDepthBound >= curDepth) {
            val dataLengthsFromPreviousDepth = if (curDepth == 0) emptyMap() else dataLengths[curDepth - 1]
            getDataConstraints(ctx, label.internalStructure, prevDepthValues, dataLengthsFromPreviousDepth)
        } else {
            prevDepthValues[label] ?: error("The value should be counted by now")
        }
    }

private fun getDataConstraints(
    ctx: TvmContext,
    struct: TlbStructure,
    constraintsFromPreviousDepth: Map<TlbCompositeLabel, AbstractGuard>,
    dataLengthsFromPreviousDepth: Map<TlbCompositeLabel, AbstractSizeExpr>,
): AbstractGuard = with(ctx) {
    when (struct) {
        is TlbStructure.Unknown, is TlbStructure.Empty -> {
            // no constraints here
            abstractTrue
        }

        is TlbStructure.LoadRef -> {
            // no need for data shift
            getDataConstraints(ctx, struct.rest, constraintsFromPreviousDepth, dataLengthsFromPreviousDepth)
        }

        is TlbStructure.KnownTypePrefix -> {
            val offset = getKnownTypePrefixDataLength(struct, dataLengthsFromPreviousDepth)
                ?: return abstractFalse  // cannot construct with given depth

            val innerGuard = if (struct.typeLabel is TlbCompositeLabel) {
                constraintsFromPreviousDepth[struct.typeLabel]?.addTlbLevel(struct)
                    ?: return abstractFalse  // cannot construct with given depth

            } else if (struct.typeLabel is TlbIntegerLabelOfConcreteSize && struct.typeLabel.canBeUsedAsSizeForTlbIntegers) {
                AbstractGuard { (addr, prefixSize, path, state) ->
                    val field = TlbVariableField(struct.id, path)
                    val symbol = state.memory.readField(addr, field, sizeSort)
                    val data = state.preloadDataBitsFromCellWithoutChecks(addr, prefixSize, struct.typeLabel.concreteSize)
                    val high = mkBvExtractExpr(high = 31, low = struct.typeLabel.concreteSize, symbol)
                    val low = mkBvExtractExpr(high = struct.typeLabel.concreteSize - 1, low = 0, symbol)
                    check(data.sort.sizeBits == low.sort.sizeBits)
                    (high eq mkBv(0, high.sort)) and (data eq low)
                }

            } else {
                abstractTrue
            }

            val further = getDataConstraints(ctx, struct.rest, constraintsFromPreviousDepth, dataLengthsFromPreviousDepth)

            innerGuard and further.shift(offset)
        }

        is TlbStructure.SwitchPrefix -> {
            val switchSize = mkSizeExpr(struct.switchSize)
            struct.variants.entries.fold(abstractFalse) { acc, (key, variant) ->
                val further = getDataConstraints(
                    ctx,
                    variant,
                    constraintsFromPreviousDepth,
                    dataLengthsFromPreviousDepth
                ).shift(AbstractSizeExpr { switchSize })

                val switchGuard = generateSwitchGuard(struct.switchSize, key)

                acc or (switchGuard and further)
            }
        }
    }
}
