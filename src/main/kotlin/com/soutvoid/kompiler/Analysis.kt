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
    val exploredValueType = value.exploreType()
    if (type != exploredValueType)
        printTypeMismatchError(position.line, position.indexInLine, type, exploredValueType)
}

fun FunctionDeclaration.analyze() {
    statements?.forEach { it.analyze() }
    returnExpression?.analyze()
    val returnExpressionType = returnExpression?.exploreType() ?: UnitType
    if (returnExpression == null && returnType != UnitType)
        printTypeMismatchError(position.line, position.indexInLine, returnType, UnitType)
    returnExpression?.let {
        if (returnExpressionType != returnType)
            printTypeMismatchError(it.position.line, it.position.indexInLine, returnType, returnExpressionType)
    }
}

fun Statement.analyze() {
    when(this) {
        is VarDeclaration -> analyze()
        is SimpleAssignment -> analyze()
        is ArrayAssignment -> analyze()
        is IfStatement -> analyze()
        is WhileLoop -> analyze()
        is ForLoop -> analyze()
    }
}

fun Expression.analyze() {
    //TODO implement
}

fun SimpleAssignment.analyze() {
    val expectedType = getVisibleVarDeclarations().find { it.varName == varName }?.type
    val actualType = value.exploreType()
    if (expectedType != actualType)
        printTypeMismatchError(position.line, position.indexInLine, expectedType, actualType)
}

fun ArrayAssignment.analyze() {
    val expectedType = getVisibleVarDeclarations().find { it.varName == arrayElement.arrayName }?.type
    val actualType = value.exploreType()
    if (expectedType != actualType)
        printTypeMismatchError(position.line, position.indexInLine, expectedType, actualType)
}

fun IfStatement.analyze() {
    expression.analyze()
    val actualType = expression.exploreType()
    if (actualType != BooleanType)
        printTypeMismatchError(expression.position.line, expression.position.indexInLine, BooleanType, actualType)
    statements.forEach { it.analyze() }
}

fun WhileLoop.analyze() {
    factor.analyze()
    val actualType = factor.exploreType()
    if (actualType != BooleanType)
        printTypeMismatchError(factor.position.line, factor.position.indexInLine, BooleanType, actualType)
    statements.forEach { it.analyze() }
}

fun ForLoop.analyze() {
    iterable.analyze()
    statements?.forEach { it.analyze() }
}

fun Expression.exploreType(): Type? = when(this) {
    is FunctionCall -> closestParentIs<ClassDeclaration>()?.functions?.find { it.name == name }?.returnType
    is VarReference -> getVisibleVarDeclarations().find { it.varName == varName }?.type
    is ArrayInit -> ArrayType(type, position, parent)
    is Range -> RangeType
    is EqualsExpression, is LessExpression, is GreaterExpression, is LessOrEqualsExpression, is GreaterOrEqualsExpression -> BooleanType
    is Multiplication -> resolveType(left.exploreType(), right.exploreType())
    is Division -> resolveType(left.exploreType(), right.exploreType())
    is Addition -> resolveType(left.exploreType(), right.exploreType())
    is Subtraction -> resolveType(left.exploreType(), right.exploreType())
    is IntLit -> IntType
    is DoubleLit -> DoubleType
    is BooleanLit -> BooleanType
    is StringLit -> StringType
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun resolveType(type1: Type?, type2: Type?): Type? {
    if (type1 == null && type2 == null) return null
    if (type1 == null) return type2
    if (type2 == null) return type1
    var result: Type? = null
    ifNotNull(type1, type2) { t1, t2 ->
        result = when(t1) {
            is IntType -> resolveByInt(t2)
            is DoubleType -> resolveByDouble(t2)
            is BooleanType -> resolveByBoolean(t2)
            is StringType -> resolveByString(t2)
            is UnitType -> resolveByUnit(t2)
            is RangeType -> resolveByRange(t2)
            else -> throw IllegalArgumentException(t1.javaClass.canonicalName)
        }
    }
    return result
}

fun resolveByInt(type: Type): Type? = when(type) {
    is IntType -> IntType
    is DoubleType -> DoubleType
    is BooleanType -> null
    is StringType -> null
    is UnitType -> null
    is RangeType -> null
    else -> throw IllegalArgumentException(type.javaClass.canonicalName)
}

fun resolveByDouble(type: Type): Type? = when(type) {
    is IntType -> DoubleType
    is DoubleType -> DoubleType
    is BooleanType -> null
    is StringType -> null
    is UnitType -> null
    is RangeType -> null
    else -> throw IllegalArgumentException(type.javaClass.canonicalName)
}

fun resolveByBoolean(type: Type): Type? = when(type) {
    is IntType -> null
    is DoubleType -> null
    is BooleanType -> BooleanType
    is StringType -> null
    is UnitType -> null
    is RangeType -> null
    else -> throw IllegalArgumentException(type.javaClass.canonicalName)
}

fun resolveByString(type: Type): Type? = when(type) {
    is IntType -> null
    is DoubleType -> null
    is BooleanType -> null
    is StringType -> StringType
    is UnitType -> null
    is RangeType -> null
    else -> throw IllegalArgumentException(type.javaClass.canonicalName)
}

fun resolveByUnit(type: Type): Type? = when(type) {
    is IntType -> null
    is DoubleType -> null
    is BooleanType -> null
    is StringType -> null
    is UnitType -> UnitType
    is RangeType -> null
    else -> throw IllegalArgumentException(type.javaClass.canonicalName)
}

fun resolveByRange(type: Type): Type? = when(type) {
    is IntType -> null
    is DoubleType -> null
    is BooleanType -> null
    is StringType -> null
    is UnitType -> null
    is RangeType -> RangeType
    else -> throw IllegalArgumentException(type.javaClass.canonicalName)
}