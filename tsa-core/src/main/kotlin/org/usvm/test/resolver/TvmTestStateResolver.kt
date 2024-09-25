package org.usvm.test.resolver

import io.ksmt.expr.KBitVecValue
import io.ksmt.utils.BvUtils.toBigIntegerSigned
import kotlinx.collections.immutable.persistentListOf
import org.ton.TvmAtomicDataCellLabel
import org.ton.TvmCompositeDataCellLabel
import org.ton.TvmParameterInfo
import org.ton.bytecode.TvmCodeBlock
import org.ton.bytecode.TvmInst
import org.usvm.NULL_ADDRESS
import org.usvm.UAddressSort
import org.usvm.UConcreteHeapAddress
import org.usvm.UConcreteHeapRef
import org.usvm.UExpr
import org.usvm.UHeapRef
import org.usvm.USort
import org.usvm.api.readField
import org.usvm.isTrue
import org.usvm.machine.TvmContext
import org.usvm.machine.TvmSizeSort
import org.usvm.machine.intValue
import org.usvm.machine.state.TvmCellRefsRegionValueInfo
import org.usvm.machine.state.TvmMethodResult
import org.usvm.machine.state.TvmRefsMemoryRegion
import org.usvm.machine.state.TvmStack
import org.usvm.machine.state.TvmStack.TvmStackTupleValue
import org.usvm.machine.state.TvmStack.TvmStackTupleValueConcreteNew
import org.usvm.machine.state.TvmState
import org.usvm.machine.state.calcConsumedGas
import org.usvm.machine.state.ensureSymbolicBuilderInitialized
import org.usvm.machine.state.ensureSymbolicCellInitialized
import org.usvm.machine.state.ensureSymbolicSliceInitialized
import org.usvm.machine.state.lastStmt
import org.usvm.machine.state.tvmCellRefsRegion
import org.usvm.machine.types.TvmDataCellLoadedTypeInfo
import org.usvm.machine.types.TvmDataCellType
import org.usvm.machine.types.TvmDictCellType
import org.usvm.machine.types.TvmReadingOfUnexpectedType
import org.usvm.machine.types.TvmReadingOutOfSwitchBounds
import org.usvm.machine.types.TvmSymbolicCellDataBitArray
import org.usvm.machine.types.TvmSymbolicCellDataCoins
import org.usvm.machine.types.TvmSymbolicCellDataInteger
import org.usvm.machine.types.TvmSymbolicCellDataMsgAddr
import org.usvm.machine.types.TvmSymbolicCellDataType
import org.usvm.machine.types.TvmSymbolicCellMaybeConstructorBit
import org.usvm.machine.types.TvmType
import org.usvm.machine.types.TvmUnexpectedEndOfReading
import org.usvm.machine.types.TvmUnexpectedDataReading
import org.usvm.machine.types.TvmUnexpectedRefReading
import org.usvm.machine.types.defaultCellValueOfMinimalLength
import org.usvm.machine.types.getPossibleTypes
import org.usvm.memory.UMemory
import org.usvm.model.UModelBase
import org.usvm.sizeSort
import java.math.BigInteger
import org.usvm.UBvSort
import org.usvm.collection.set.primitive.setEntries
import org.usvm.machine.TvmContext.Companion.dictKeyLengthField
import org.usvm.machine.state.DictId
import org.usvm.machine.state.DictKeyInfo
import org.usvm.machine.state.dictContainsKey
import org.usvm.machine.state.dictGetValue

class TvmTestStateResolver(
    private val ctx: TvmContext,
    private val model: UModelBase<TvmType>,
    private val state: TvmState,
) {
    private val stack: TvmStack
        get() = state.stack

    private val memory: UMemory<TvmType, TvmCodeBlock>
        get() = state.memory

    private val resolvedCache = mutableMapOf<UConcreteHeapAddress, TvmTestCellValue>()

    fun resolveParameters(): List<TvmTestValue> = stack.inputValues.filterNotNull().map { resolveStackValue(it) }.reversed()

    fun resolveResultStack(): TvmMethodSymbolicResult {
        val results = state.stack.results

        // Do not include exit code for exceptional results to the result
        val resultsWithoutExitCode = if (state.methodResult is TvmMethodResult.TvmFailure) results.dropLast(1) else results
        val resolvedResults = resultsWithoutExitCode.filterNotNull().map { resolveStackValue(it) }

        return when (val it = state.methodResult) {
            TvmMethodResult.NoCall -> error("Missed result for state $state")
            is TvmMethodResult.TvmFailure -> TvmMethodFailure(it, state.lastStmt, it.exit.exitCode, resolvedResults)
            is TvmMethodResult.TvmSuccess -> TvmSuccessfulExecution(it.exit.exitCode, resolvedResults)
            is TvmMethodResult.TvmStructuralError -> resolveTvmStructuralError(state.lastStmt, resolvedResults, it)
        }
    }

    private fun resolveTvmStructuralError(
        lastStmt: TvmInst,
        stack: List<TvmTestValue>,
        exit: TvmMethodResult.TvmStructuralError,
    ): TvmExecutionWithStructuralError {
        val resolvedExit = when (val structuralExit = exit.exit) {
            is TvmUnexpectedDataReading -> TvmUnexpectedDataReading(
                resolveCellDataType(structuralExit.readingType),
            )
            is TvmReadingOfUnexpectedType -> TvmReadingOfUnexpectedType(
                labelType = structuralExit.labelType,
                actualType = resolveCellDataType(structuralExit.actualType),
            )
            is TvmUnexpectedEndOfReading -> TvmUnexpectedEndOfReading
            is TvmUnexpectedRefReading -> TvmUnexpectedRefReading
            is TvmReadingOutOfSwitchBounds -> TvmReadingOutOfSwitchBounds(resolveCellDataType(structuralExit.readingType))
        }
        return TvmExecutionWithStructuralError(lastStmt, stack, resolvedExit)
    }

    fun resolveGasUsage(): Int = model.eval(state.calcConsumedGas()).intValue()

    private fun resolveStackValue(stackValue: TvmStack.TvmStackValue): TvmTestValue {
        return when (stackValue) {
            is TvmStack.TvmStackIntValue -> resolveInt257(stackValue.intValue)
            is TvmStack.TvmStackCellValue -> resolveCell(stackValue.cellValue.also { state.ensureSymbolicCellInitialized(it) })
            is TvmStack.TvmStackSliceValue -> resolveSlice(stackValue.sliceValue.also { state.ensureSymbolicSliceInitialized(it) })
            is TvmStack.TvmStackBuilderValue -> resolveBuilder(stackValue.builderValue.also { state.ensureSymbolicBuilderInitialized(it) })
            is TvmStack.TvmStackNullValue -> TvmTestNullValue
            is TvmStack.TvmStackContinuationValue -> TODO()
            is TvmStackTupleValue -> resolveTuple(stackValue)
        }
    }

    private fun <T : USort> evaluateInModel(expr: UExpr<T>): UExpr<T> = model.eval(expr)

    private fun resolveTuple(tuple: TvmStackTupleValue): TvmTestTupleValue = when (tuple) {
        is TvmStackTupleValueConcreteNew -> {
            val elements = tuple.entries.map {
                it.cell(stack)?.let { value -> resolveStackValue(value) }
                    ?: TvmTestNullValue // We do not care what is its real value as it was never used
            }

            TvmTestTupleValue(elements)
        }
        is TvmStack.TvmStackTupleValueInputValue -> {
            val size = resolveInt(tuple.size)
            val elements = (0 ..< size).map {
                tuple[it, stack].cell(stack)?.let { value -> resolveStackValue(value) }
                    ?: TvmTestNullValue // We do not care what is its real value as it was never used
            }

            TvmTestTupleValue(elements)
        }
    }

    private fun resolveBuilder(builder: UHeapRef): TvmTestBuilderValue {
        val ref = evaluateInModel(builder) as UConcreteHeapRef

        val cached = resolvedCache[ref.address]
        check(cached is TvmTestDataCellValue?)
        if (cached != null) {
            return TvmTestBuilderValue(cached.data, cached.refs)
        }

        val cell = resolveDataCell(ref, builder)
        return TvmTestBuilderValue(cell.data, cell.refs)
    }

    private fun resolveSlice(slice: UHeapRef): TvmTestSliceValue = with(ctx) {
        val cellValue = resolveCell(memory.readField(slice, TvmContext.sliceCellField, addressSort))
        require(cellValue is TvmTestDataCellValue)
        val dataPosValue = resolveInt(memory.readField(slice, TvmContext.sliceDataPosField, sizeSort))
        val refPosValue = resolveInt(memory.readField(slice, TvmContext.sliceRefPosField, sizeSort))

        TvmTestSliceValue(cellValue, dataPosValue, refPosValue)
    }

    private fun resolveDataCell(modelRef: UConcreteHeapRef, cell: UHeapRef): TvmTestDataCellValue = with(ctx) {
        if (modelRef.address == NULL_ADDRESS) {
            return@with TvmTestDataCellValue()
        }

        val data = resolveCellData(cell)

        val refsLength = resolveInt(memory.readField(cell, TvmContext.cellRefsLengthField, sizeSort)).coerceAtMost(TvmContext.MAX_REFS_NUMBER)
        val refs = mutableListOf<TvmTestCellValue>()

        val storedRefs = mutableMapOf<Int, TvmTestCellValue>()
        val updateNode = memory.tvmCellRefsRegion().getRefsUpdateNode(modelRef)

        resolveRefUpdates(updateNode, storedRefs, refsLength)

        for (idx in 0 until refsLength) {
            val refCell = storedRefs[idx]
                ?: TvmTestDataCellValue()

            refs.add(refCell)
        }

        val knownActions = state.dataCellLoadedTypeInfo.addressToActions[modelRef] ?: persistentListOf()
        val tvmCellValue = TvmTestDataCellValue(data, refs, resolveTypeLoad(knownActions))

        tvmCellValue.also { resolvedCache[modelRef.address] = tvmCellValue }
    }

    private fun resolveDictCell(modelRef: UConcreteHeapRef, dict: UHeapRef): TvmTestDictCellValue = with(ctx) {
        if (modelRef.address == NULL_ADDRESS) {
            error("Unexpected dict ref: $modelRef")
        }

        val keyLength = extractInt(memory.readField(dict, dictKeyLengthField, int257sort))
        val dictId = DictId(keyLength)
        // entries stored during execution
        val memoryKeySetEntries = memory.setEntries(dict, dictId, mkBvSort(keyLength.toUInt()), DictKeyInfo)
        // input entries
        val modelKeySetEntries = model.setEntries(modelRef, dictId, mkBvSort(keyLength.toUInt()), DictKeyInfo)
        val keySetEntries = memoryKeySetEntries.entries + modelKeySetEntries.entries

        val keySet = mutableSetOf<UExpr<UBvSort>>()
        val resultEntries = mutableMapOf<TvmTestIntegerValue, TvmTestSliceValue>()

        for (entry in keySetEntries) {
            val key = entry.setElement
            val keyContains = state.dictContainsKey(dict, dictId, key)
            if (evaluateInModel(keyContains).isTrue) {
                val evaluatedKey = evaluateInModel(key)
                if (!keySet.add(evaluatedKey)) {
                    continue
                }

                val resolvedKey = TvmTestIntegerValue(extractInt257(evaluatedKey))
                val value = state.dictGetValue(dict, dictId, evaluatedKey)
                val resolvedValue = resolveSlice(value)

                resultEntries[resolvedKey] = resolvedValue
            }
        }

        return TvmTestDictCellValue(keyLength, resultEntries).also { resolvedCache[modelRef.address] = it }
    }

    private fun buildDefaultCell(cellInfo: TvmParameterInfo.CellInfo): TvmTestCellValue =
        when (cellInfo) {
            is TvmParameterInfo.UnknownCellInfo -> {
                TvmTestDataCellValue()
            }
            is TvmParameterInfo.DictCellInfo -> {
                TvmTestDictCellValue(cellInfo.keySize, emptyMap())
            }
            is TvmParameterInfo.DataCellInfo -> {
                when (val label = cellInfo.dataCellStructure) {
                    is TvmAtomicDataCellLabel -> {
                        TvmTestDataCellValue(data = label.defaultCellValueOfMinimalLength())
                    }
                    is TvmCompositeDataCellLabel -> {
                        val defaultValue = state.dataCellInfoStorage.mapper.calculatedTlbLabelInfo.getDefaultCell(label)
                        check(defaultValue != null) {
                            "Default cell for label ${label.name} must be calculated"
                        }
                        defaultValue
                    }
                }
            }
        }

    private fun resolveCell(cell: UHeapRef): TvmTestCellValue = with(ctx) {
        val modelRef = evaluateInModel(cell) as UConcreteHeapRef
        if (modelRef.address == NULL_ADDRESS) {
            return@with TvmTestDataCellValue()
        }

        val cached = resolvedCache[modelRef.address]
        if (cached != null) return cached

        val mapper = state.dataCellInfoStorage.mapper

        // This is a special situation for a case when a child of some cell with TL-B scheme
        // was requested for the first time only during test resolving process.
        // Since structural constraints are generated lazily, they were not generated for
        // this child yet. To avoid generation of a test that violates TL-B scheme
        // we provide [TvmTestCellValue] with default contents for the scheme.
        if (!mapper.structuralConstraintsWereCalculated(modelRef) && mapper.addressWasGiven(modelRef)) {
            return buildDefaultCell(mapper.getLabelFromModel(model, modelRef))
        }

        val typeVariants = state.getPossibleTypes(modelRef)

        // If typeVariants has more than one type, we can choose any of them.
        val type = typeVariants.first()

        require(type is TvmDictCellType || type is TvmDataCellType)

        if (type is TvmDictCellType) {
            return resolveDictCell(modelRef, cell)
        }

        resolveDataCell(modelRef, cell)
    }

    private fun resolveRefUpdates(
        updateNode: TvmRefsMemoryRegion.TvmRefsRegionUpdateNode<TvmSizeSort, UAddressSort>?,
        storedRefs: MutableMap<Int, TvmTestCellValue>,
        refsLength: Int,
    ) {
        @Suppress("NAME_SHADOWING")
        var updateNode = updateNode

        while (updateNode != null) {
            when (updateNode) {
                is TvmRefsMemoryRegion.TvmRefsRegionInputNode -> {
                    val idx = resolveInt(updateNode.key)
                    // [idx] might be >= [refsLength]
                    // because we read refs when generating structural constraints
                    // without checking actual number of refs in a cell
                    if (idx < refsLength) {
                        val value = TvmCellRefsRegionValueInfo(state).actualizeSymbolicValue(updateNode.value)
                        val refCell = resolveCell(value)
                        storedRefs.putIfAbsent(idx, refCell)
                    }
                }

                is TvmRefsMemoryRegion.TvmRefsRegionEmptyUpdateNode -> {}
                is TvmRefsMemoryRegion.TvmRefsRegionCopyUpdateNode -> {
                    val guardValue = evaluateInModel(updateNode.guard)
                    if (guardValue.isTrue) {
                        resolveRefUpdates(updateNode.updates, storedRefs, refsLength)
                    }
                }
                is TvmRefsMemoryRegion.TvmRefsRegionPinpointUpdateNode -> {
                    val guardValue = evaluateInModel(updateNode.guard)
                    if (guardValue.isTrue) {
                        val idx = resolveInt(updateNode.key)
                        if (idx < refsLength) {
                            val refCell = resolveCell(updateNode.value)
                            storedRefs.putIfAbsent(idx, refCell)
                        }
                    }
                }
            }

            updateNode = updateNode.prevUpdate
        }
    }

    private fun resolveTypeLoad(loads: List<TvmDataCellLoadedTypeInfo.Action>): List<TvmCellDataTypeLoad> =
        loads.mapNotNull {
            if (it is TvmDataCellLoadedTypeInfo.LoadData && model.eval(it.guard).isTrue) {
                TvmCellDataTypeLoad(resolveCellDataType(it.type), resolveInt(it.offset))
            } else {
                null
            }
        }

    private fun resolveCellDataType(type: TvmSymbolicCellDataType): TvmCellDataType =
        when (type) {
            is TvmSymbolicCellDataInteger -> TvmCellDataInteger(resolveInt(type.sizeBits), type.isSigned, type.endian)
            is TvmSymbolicCellMaybeConstructorBit -> TvmCellDataMaybeConstructorBit
            is TvmSymbolicCellDataBitArray -> TvmCellDataBitArray(resolveInt(type.sizeBits))
            is TvmSymbolicCellDataMsgAddr -> TvmCellDataMsgAddr
            is TvmSymbolicCellDataCoins -> TvmCellDataCoins(resolveInt(type.coinsPrefix))
        }

    private fun resolveInt257(expr: UExpr<out USort>): TvmTestIntegerValue {
        val value = extractInt257(evaluateInModel(expr))
        return TvmTestIntegerValue(value)
    }

    private fun resolveCellData(cell: UHeapRef): String = with(ctx) {
        val symbolicData = memory.readField(cell, TvmContext.cellDataField, cellDataSort)
        val data = extractCellData(evaluateInModel(symbolicData))
        val dataLength = resolveInt(memory.readField(cell, TvmContext.cellDataLengthField, sizeSort))
            .coerceAtMost(TvmContext.MAX_DATA_LENGTH).coerceAtLeast(0)

        return data.take(dataLength)
    }

    private fun resolveInt(expr: UExpr<out USort>): Int = extractInt(evaluateInModel(expr))

    private fun extractInt(expr: UExpr<out USort>): Int =
        (expr as? KBitVecValue)?.toBigIntegerSigned()?.toInt() ?: error("Unexpected expr $expr")

    private fun extractCellData(expr: UExpr<out USort>): String =
        (expr as? KBitVecValue)?.stringValue ?: error("Unexpected expr $expr")

    private fun extractInt257(expr: UExpr<out USort>): BigInteger =
        (expr as? KBitVecValue)?.toBigIntegerSigned() ?: error("Unexpected expr $expr")
}