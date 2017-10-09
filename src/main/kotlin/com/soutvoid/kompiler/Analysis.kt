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
        printlnError("Type mismatch at: ${position.line}:${position.indexInLine}. Expected: ${type.name()}, but got: ${exploredValueType?.name()}")
}

fun FunctionDeclaration.analyze() {
    //TODO implement
}

fun Expression.exploreType(): Type? = when(this) {
    is FunctionCall -> closestParentIs<ClassDeclaration>()?.functions?.find { it.name == name }?.returnType
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

fun printDuplicatesError(elementName: String, positions: List<Position>) {
    printlnError("Duplicating $elementName at positions: " +
            positions.map { it.line.toString() + ":" + it.indexInLine }.joinToString(","))
}