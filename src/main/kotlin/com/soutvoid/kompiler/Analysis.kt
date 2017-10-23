package com.soutvoid.kompiler

fun ClassDeclaration.analyze() {
    properties?.let {
        it.findDuplicatesBy { element1, element2 -> element1.varName == element2.varName }.forEach {
            printDuplicatesError("properties", it.map { it.position })
        }
        it.forEach { it.analyze() }
    }
    functions?.let {
        it.findDuplicatesBy { element1, element2 -> element1.name == element2.name }.forEach {
            printDuplicatesError("functions", it.map { it.position })
        }
        it.forEach { it.analyze() }
    }
}

fun VarDeclaration.analyze() {
    value.analyze()
    val exploredValueType = value.exploreType()
    if (type != exploredValueType)
        printTypeMismatchError(position.startLine, position.startIndexInLine, type, exploredValueType)
}

fun FunctionDeclaration.analyze() {
    statements?.forEach { it.analyze() }
    returnExpression?.analyze()
    val returnExpressionType = returnExpression?.exploreType() ?: UnitType
    if (returnExpression == null && returnType != UnitType)
        printTypeMismatchError(position.startLine, position.startIndexInLine, returnType, UnitType)
    returnExpression?.let {
        if (returnExpressionType != returnType)
            printTypeMismatchError(it.position.startLine, it.position.startIndexInLine, returnType, returnExpressionType)
    }
}

fun Statement.analyze() {
    when (this) {
        is VarDeclaration -> analyze()
        is SimpleAssignment -> analyze()
        is ArrayAssignment -> analyze()
        is IfStatement -> analyze()
        is WhileLoop -> analyze()
        is ForLoop -> analyze()
    }
}

fun Expression.analyze() {
    when (this) {
        is FunctionCall -> analyze()
        is VarReference -> analyze()
        is ArrayInit -> analyze()
        is Range -> analyze()
        is Comparison -> analyze()
        is Multiplication -> analyze()
        is Division -> analyze()
        is Addition -> analyze()
        is Subtraction -> analyze()
        is IntLit, is DoubleLit, is BooleanLit, is StringLit -> {
        }//nothing to analyze here
        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }
}

fun FunctionCall.analyze() {

}

fun VarReference.analyze() {
    if (getVisibleVarDeclarations().find { it.varName == varName } == null)
        printUnresolvedReference(position.startLine, position.startIndexInLine, varName)
}

fun ArrayInit.analyze() {
    //nothing to analyze
}

fun Range.analyze() {
    //nothing to analyze
}

fun Comparison.analyze() {
    val leftType = left.exploreType()
    val rightType = right.exploreType()
    val resolvedType = resolveType(left, right, leftType, rightType)
    if (resolvedType is ArrayType)
        printOperationDoesNotSupportError(position.startLine, position.startIndexInLine, name(), resolvedType)
    if (resolvedType == null)
        printIncompatibleTypesError(position.startLine, position.startIndexInLine, leftType, rightType)
}

fun Multiplication.analyze() {
    val leftType = left.exploreType()
    val rightType = right.exploreType()
    val resolvedType = resolveType(left, right, leftType, rightType)
    if (resolvedType == null)
        printIncompatibleTypesError(position.startLine, position.startIndexInLine, leftType, rightType)
    else {
        if (resolvedType == BooleanType || resolvedType == StringType || resolvedType == RangeType || resolvedType == UnitType ||
                resolvedType is ArrayType)
            printOperationDoesNotSupportError(position.startLine, position.startIndexInLine, name(), resolvedType)
    }
}

fun Division.analyze() {
    val leftType = left.exploreType()
    val rightType = right.exploreType()
    val resolvedType = resolveType(left, right, leftType, rightType)
    if (resolvedType == null)
        printIncompatibleTypesError(position.startLine, position.startIndexInLine, leftType, rightType)
    else {
        if (resolvedType == BooleanType || resolvedType == StringType || resolvedType == RangeType || resolvedType == UnitType ||
                resolvedType is ArrayType)
            printOperationDoesNotSupportError(position.startLine, position.startIndexInLine, name(), resolvedType)
    }
}

fun Addition.analyze() {
    val leftType = left.exploreType()
    val rightType = right.exploreType()
    val resolvedType = resolveType(left, right, leftType, rightType)
    if (resolvedType == null)
        printIncompatibleTypesError(position.startLine, position.startIndexInLine, leftType, rightType)
    else {
        if (resolvedType == BooleanType || resolvedType == RangeType || resolvedType == UnitType ||
                resolvedType is ArrayType)
            printOperationDoesNotSupportError(position.startLine, position.startIndexInLine, name(), resolvedType)
    }
}

fun Subtraction.analyze() {
    val leftType = left.exploreType()
    val rightType = right.exploreType()
    val resolvedType = resolveType(left, right, leftType, rightType)
    if (resolvedType == null)
        printIncompatibleTypesError(position.startLine, position.startIndexInLine, leftType, rightType)
    else {
        if (resolvedType == BooleanType || resolvedType == StringType || resolvedType == RangeType || resolvedType == UnitType ||
                resolvedType is ArrayType)
            printOperationDoesNotSupportError(position.startLine, position.startIndexInLine, name(), resolvedType)
    }
}

fun SimpleAssignment.analyze() {
    val expectedType = getVisibleVarDeclarations().find { it.varName == varName }?.type
    val actualType = value.exploreType()
    if (expectedType != actualType)
        printTypeMismatchError(position.startLine, position.startIndexInLine, expectedType, actualType)
}

fun ArrayAssignment.analyze() {
    val expectedType = getVisibleVarDeclarations().find { it.varName == arrayElement.arrayName }?.type
    val actualType = value.exploreType()
    if (expectedType != actualType)
        printTypeMismatchError(position.startLine, position.startIndexInLine, expectedType, actualType)
}

fun IfStatement.analyze() {
    expression.analyze()
    val actualType = expression.exploreType()
    if (actualType != BooleanType)
        printTypeMismatchError(expression.position.startLine, expression.position.startIndexInLine, BooleanType, actualType)
    statements.forEach { it.analyze() }
}

fun WhileLoop.analyze() {
    factor.analyze()
    val actualType = factor.exploreType()
    if (actualType != BooleanType)
        printTypeMismatchError(factor.position.startLine, factor.position.startIndexInLine, BooleanType, actualType)
    statements.forEach { it.analyze() }
}

fun ForLoop.analyze() {
    iterable.analyze()
    statements?.forEach { it.analyze() }
}

fun Expression.exploreType(): Type? = when (this) {
    is FunctionCall -> closestParentIs<ClassDeclaration>()?.functions?.find { it.name == name }?.returnType
    is VarReference -> getVisibleVarDeclarations().find { it.varName == varName }?.type
    is ArrayInit -> ArrayType(type, position, parent)
    is Range -> RangeType
    is EqualsExpression, is NotEqualsExpression, is LessExpression, is GreaterExpression, is LessOrEqualsExpression, is GreaterOrEqualsExpression -> BooleanType
    is Multiplication -> resolveType(left, right, left.exploreType(), right.exploreType())
    is Division -> resolveType(left, right, left.exploreType(), right.exploreType())
    is Addition -> resolveType(left, right, left.exploreType(), right.exploreType())
    is Subtraction -> resolveType(left, right, left.exploreType(), right.exploreType())
    is IntLit -> IntType
    is DoubleLit -> DoubleType
    is BooleanLit -> BooleanType
    is StringLit -> StringType
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun resolveType(expr1: Expression, expr2: Expression,
                type1: Type?, type2: Type?): Type? {
    val compatibleType = areTypesCompatible(type1, type2)
    compatibleType?.let { return it }

    val autoCastType = isAutoCastPossible(type1, type2)
    autoCastType?.let {
        if (type1 != it)
            expr1 castTo it
        if (type2 != it)
            expr2 castTo it
        return it
    }
    return null
}

fun isAutoCastPossible(type1: Type?, type2: Type?): Type? {
    if (type1 == null || type2 == null) return null
    var result: Type? = null
    ifNotNull(type1, type2) { t1, t2 ->
        result = when (t1) {
            is IntType -> resolveByInt(t2)
            is DoubleType -> resolveByDouble(t2)
            is BooleanType -> resolveByBoolean(t2)
            is StringType -> resolveByString(t2)
            is UnitType -> resolveByUnit(t2)
            is RangeType -> resolveByRange(t2)
            is ArrayType -> if (type1 == type2) type1 else null
            else -> throw IllegalArgumentException(t1.javaClass.canonicalName)
        }
    }
    return result
}

fun areTypesCompatible(type1: Type?, type2: Type?): Type? {
    if (type1 == null || type2 == null) return null
    var result: Type? = null
    ifNotNull(type1, type2) { t1, t2 ->
        result = if (t1 == t2) t1 else null
    }
    return result
}

fun resolveByInt(type: Type): Type? = when (type) {
    is IntType -> IntType
    is DoubleType -> DoubleType
    is BooleanType -> null
    is StringType -> StringType
    is UnitType -> null
    is RangeType -> null
    else -> throw IllegalArgumentException(type.javaClass.canonicalName)
}

fun resolveByDouble(type: Type): Type? = when (type) {
    is IntType -> DoubleType
    is DoubleType -> DoubleType
    is BooleanType -> null
    is StringType -> StringType
    is UnitType -> null
    is RangeType -> null
    else -> throw IllegalArgumentException(type.javaClass.canonicalName)
}

fun resolveByBoolean(type: Type): Type? = when (type) {
    is IntType -> null
    is DoubleType -> null
    is BooleanType -> BooleanType
    is StringType -> StringType
    is UnitType -> null
    is RangeType -> null
    else -> throw IllegalArgumentException(type.javaClass.canonicalName)
}

fun resolveByString(type: Type): Type? = when (type) {
    is IntType -> StringType
    is DoubleType -> StringType
    is BooleanType -> StringType
    is StringType -> StringType
    is UnitType -> null
    is RangeType -> null
    else -> throw IllegalArgumentException(type.javaClass.canonicalName)
}

fun resolveByUnit(type: Type): Type? = when (type) {
    is IntType -> null
    is DoubleType -> null
    is BooleanType -> null
    is StringType -> null
    is UnitType -> UnitType
    is RangeType -> null
    else -> throw IllegalArgumentException(type.javaClass.canonicalName)
}

fun resolveByRange(type: Type): Type? = when (type) {
    is IntType -> null
    is DoubleType -> null
    is BooleanType -> null
    is StringType -> null
    is UnitType -> null
    is RangeType -> RangeType
    else -> throw IllegalArgumentException(type.javaClass.canonicalName)
}