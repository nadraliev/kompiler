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
    //TODO implement
}

fun Expression.analyze() {
    //TODO implement
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
    //TODO implement
    return null
}