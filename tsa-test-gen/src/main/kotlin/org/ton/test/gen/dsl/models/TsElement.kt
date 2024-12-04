package org.ton.test.gen.dsl.models

import org.ton.test.gen.dsl.render.TsVisitor
import org.ton.test.gen.dsl.wrapper.TsWrapperDescriptor
import org.usvm.test.resolver.TvmTestBuilderValue
import org.usvm.test.resolver.TvmTestDataCellValue
import org.usvm.test.resolver.TvmTestDictCellValue
import org.usvm.test.resolver.TvmTestIntegerValue
import org.usvm.test.resolver.TvmTestSliceValue

sealed interface TsElement {
    fun <R> accept(visitor: TsVisitor<R>): R = visitor.run {
        when (val element = this@TsElement) {
            is TsTestFile -> visit(element)
            is TsType -> visitType(element)
            is TsStatement -> visitStatement(element)
            is TsExpression<*> -> visitExpression(element)
        }
    }

    fun <R> TsVisitor<R>.visitType(element: TsType): R =
        when (element) {
            is TsVoid -> visit(element)
            is TsBoolean -> visit(element)
            is TsString -> visit(element)
            is TsCell -> visit(element)
            is TsSlice -> visit(element)
            is TsBuilder -> visit(element)
            is TsAddress -> visit(element)
            is TsBlockchain -> visit(element)
            is TsSendMessageResult -> visit(element)
            is TsInt -> visit(element)
            is TsBigint -> visit(element)
            is TsWrapper -> visit(element)
            is TsSandboxContract<*> -> visit(element)
        }

    fun <R> TsVisitor<R>.visitStatement(element: TsStatement): R =
        when (element) {
            is TsTestBlock -> visit(element)
            is TsTestCase -> visit(element)
            is TsBeforeEachBlock -> visit(element)
            is TsBeforeAllBlock -> visit(element)
            is TsEmptyLine -> visit(element)
            is TsAssignment<*> -> visit(element)
            is TsDeclaration<*> -> visit(element)
            is TsStatementExpression<*> -> visit(element)
            is TsExpectToEqual<*> -> visit(element)
            is TsExpectToHaveTransaction -> visit(element)
        }

    fun <R, T : TsType> TsVisitor<R>.visitExpression(element: TsExpression<T>): R =
        when (element) {
            is TsBooleanValue -> visit(element)
            is TsIntValue -> visit(element)
            is TsBigintValue -> visit(element)
            is TsStringValue -> visit(element)
            is TsDataCellValue -> visit(element)
            is TsDictValue -> visit(element)
            is TsSliceValue -> visit(element)
            is TsBuilderValue -> visit(element)
            is TsReference<*> -> visit(element)
            is TsNumAdd<*> -> visit(element)
            is TsNumSub<*> -> visit(element)
            is TsMethodCall<*> -> visit(element)
            is TsFieldRead<*, *> -> visit(element)
            is TsConstructorCall<*> -> visit(element)
        }
}

data class TsTestFile(
    val name: String,
    val wrappers: List<TsWrapperDescriptor<*>>,
    val testBlocks: List<TsTestBlock>
) : TsElement

/* statements */

sealed interface TsStatement : TsElement

data object TsEmptyLine : TsStatement

data class TsAssignment<T : TsType>(
    val assigned: TsReference<T>,
    val assignment: TsExpression<T>,
) : TsStatement

data class TsDeclaration<T : TsType>(
    val name: String,
    val type: T,
    val initializer: TsExpression<T>? = null,
) : TsStatement {
    val reference: TsReference<T> = TsReference(name, type)
}

data class TsStatementExpression<T : TsType>(val expr: TsExpression<T>) : TsStatement

/* expressions */

sealed interface TsExpression<T : TsType> : TsElement {
    val type: T
}

data class TsReference<T : TsType> internal constructor(
    val name: String,
    override val type: T,
) : TsExpression<T>

data class TsFieldRead<R : TsType, T : TsType>(
    val receiver: TsExpression<R>,
    val fieldName: String,
    override val type: T,
) : TsExpression<T>

data class TsBooleanValue(val value: Boolean) : TsExpression<TsBoolean> {
    override val type: TsBoolean
        get() = TsBoolean
}
data class TsIntValue(val value: Int) : TsExpression<TsInt> {
    override val type: TsInt
        get() = TsInt
}
data class TsBigintValue(val value: TvmTestIntegerValue) : TsExpression<TsBigint> {
    override val type: TsBigint
        get() = TsBigint
}
data class TsStringValue(val value: String) : TsExpression<TsString> {
    override val type: TsString
        get() = TsString
}
data class TsDataCellValue(val value: TvmTestDataCellValue) : TsExpression<TsCell> {
    override val type: TsCell
        get() = TsCell
}
data class TsDictValue(val value: TvmTestDictCellValue) : TsExpression<TsCell> {
    override val type: TsCell
        get() = TsCell
}
data class TsSliceValue(val value: TvmTestSliceValue) : TsExpression<TsSlice> {
    override val type: TsSlice
        get() = TsSlice
}
data class TsBuilderValue(val value: TvmTestBuilderValue) : TsExpression<TsBuilder> {
    override val type: TsBuilder
        get() = TsBuilder
}

/* arithmetic */

data class TsNumAdd<T : TsNum>(val lhs: TsExpression<T>, val rhs: TsExpression<T>) : TsExpression<T> {
    override val type: T
        get() = lhs.type
}
data class TsNumSub<T : TsNum>(val lhs: TsExpression<T>, val rhs: TsExpression<T>) : TsExpression<T> {
    override val type: T
        get() = lhs.type
}

/* test-utils */

data class TsExpectToEqual<T : TsType>(val actual: TsExpression<T>, val expected: TsExpression<T>) : TsStatement

data class TsExpectToHaveTransaction(
    val sendMessageResult: TsExpression<TsSendMessageResult>,
    val from: TsExpression<TsAddress>?,
    val to: TsExpression<TsAddress>?,
    val value: TsExpression<TsBigint>?,
    val body: TsExpression<TsCell>?,
    val exitCode: TsExpression<TsInt>?,
    val successful: TsExpression<TsBoolean>?,
    val aborted: TsExpression<TsBoolean>?,
    val deploy: TsExpression<TsBoolean>?,
) : TsStatement

/* executable */

sealed interface TsExecutableCall<T : TsType> : TsExpression<T> {
    val executableName: String
    val arguments: List<TsExpression<*>>
    val async: Boolean
}

data class TsMethodCall<T : TsType>(
    val caller: TsExpression<*>?,
    override val executableName: String,
    override val arguments: List<TsExpression<*>>,
    override val async: Boolean,
    override val type: T,
) : TsExecutableCall<T>

data class TsConstructorCall<T : TsType>(
    override val executableName: String,
    override val arguments: List<TsExpression<*>>,
    override val type: T,
) : TsExecutableCall<T> {
    override val async: Boolean
        get() = false
}
