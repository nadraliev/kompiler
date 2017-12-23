package com.soutvoid.kompiler

fun FileNode.analyze() {
    filterChildrenIs<Expression>().forEach { it.getType() }  //fill in types for all expressions
    checkForConflictsWithJavaFuncs()
    classes.findDuplicatesBy { element1, element2 -> element1.name == element2.name }.forEach {
        printDuplicatesError("classes", it.map { it.position })
    }
    classes.forEach { it.analyze() }
    properties.checkForDuplicateVarDeclarations()
    properties.forEach { it.analyze() }
    functions.findDuplicatesBy { element1, element2 -> element1 == element2 }.forEach {
        printDuplicatesError("functions", it.map { it.position })
    }
    functions.forEach { it.analyze() }
}

fun ClassDeclaration.analyze() {
    properties?.let {
        it.checkForDuplicateVarDeclarations()
        it.forEach { it.analyze() }
    }
    functions?.let {
        it.findDuplicatesBy { element1, element2 -> element1 == element2 }.forEach {
            printDuplicatesError("functions", it.map { it.position })
        }
        it.forEach { it.analyze() }
    }
}

fun VarDeclaration.analyze() {
    value.analyze()
    val exploredValueType = value.getType()
    if (type != exploredValueType)
        printTypeMismatchError(position.startLine, position.startIndexInLine, type, exploredValueType)
}

fun Annotation.analyze() {
    val annotation = annotationsList.find { it.first == name && it.second.isInstance(parameter) }
    if (annotation == null)
        printNoSuchAnnotationError(position.startLine, position.startIndexInLine, name)
}

fun FunctionDeclaration.analyze() {
    annotation?.analyze()
    maybeAddToJavaFunctions()
    statements?.forEach { it.analyze() }
    statements?.checkForDuplicateVarDeclarations()
    getReturnStatements().forEach { returnSt ->
        returnSt.analyze()
        val returnExpressionType = returnSt.expression?.getType() ?: UnitType
        if (returnExpressionType != returnType)
                printTypeMismatchError(returnSt.position.startLine,
                        returnSt.position.startIndexInLine, returnType, returnExpressionType)
    }
    if (getReturnStatements().isEmpty() && returnType != UnitType)
        printTypeMismatchError(position.startLine, position.startIndexInLine, returnType, UnitType)
}

fun FunctionDeclaration.maybeAddToJavaFunctions() {
    annotation?.let {
        if (it.name == annotationsList[0].first) {
            if (javaFunctions.find { this == it } == null)
                javaFunctions.add(this)
        }
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
        is ArrayAccess -> analyze()
        is Range -> analyze()
        is Comparison -> analyze()
        is Multiplication -> analyze()
        is Division -> analyze()
        is Addition -> analyze()
        is Subtraction -> analyze()
        is Increment -> analyze()
        is Decrement -> analyze()
        is IntLit, is DoubleLit, is BooleanLit, is StringLit -> { }//nothing to analyze here
        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }
}

fun Increment.analyze() {
    if (expression.getType() !is IntType)
        printOperationDoesNotSupportError(position.startLine, position.startIndexInLine,
                name(), expression.getType())
}

fun Decrement.analyze() {
    if (expression.getType() !is IntType)
        printOperationDoesNotSupportError(position.startLine, position.startIndexInLine,
                name(), expression.getType())
}

fun FunctionCall.analyze() {
    var declaration = getVisibleNodesIs<FunctionDeclaration>()
            .find { it.isDeclarationOf(this) }
    if (declaration == null)
        declaration = javaFunctions.find { it.isDeclarationOf(this) }
    if (declaration == null)
        printNoSuchFunctionError(position.startLine, position.startIndexInLine, this)
}

fun VarReference.analyze() {
    if (findVarDeclaration(varName) == null)
        printUnresolvedReferenceError(position.startLine, position.startIndexInLine, varName)
}

fun ArrayInit.analyze() {
    if (size < 1)
        printArraySizeError(position.startLine, position.startIndexInLine, name())
}

fun ArrayAccess.analyze() {
    val declaration = findVarDeclaration(arrayName)
    if (declaration == null )
        printUnresolvedReferenceError(position.startLine, position.startIndexInLine, arrayName)
    else {
        val type = declaration.type
        if (type !is ArrayType)
            printReferenceIsNotAnArray(position.startLine, position.startIndexInLine, arrayName)
    }
    index.analyze()
}

fun Range.analyze() {
    val startType = start.getType()
    val endType = endInclusive.getType()
    if (startType != IntType || endType != IntType)
        printRangeTypeError(position.startLine, position.startIndexInLine, startType)
    if (startType != endType)
        printTypeMismatchError(position.startLine, position.startIndexInLine, startType, endType)
}

fun Comparison.analyze() {
    val leftType = left.getType()
    val rightType = right.getType()
    val resolvedType = resolveType(left, right, leftType, rightType)
    if (resolvedType is ArrayType)
        printOperationDoesNotSupportError(position.startLine, position.startIndexInLine, javaClass.simpleName, resolvedType)
    if (resolvedType == null)
        printIncompatibleTypesError(position.startLine, position.startIndexInLine, leftType, rightType)
}

fun Multiplication.analyze() {
    val leftType = left.getType()
    val rightType = right.getType()
    val resolvedType = resolveType(left, right, leftType, rightType)
    if (resolvedType == null)
        printIncompatibleTypesError(position.startLine, position.startIndexInLine, leftType, rightType)
    else {
        if (resolvedType == BooleanType || resolvedType == StringType || resolvedType is RangeType || resolvedType == UnitType ||
                resolvedType is ArrayType)
            printOperationDoesNotSupportError(position.startLine, position.startIndexInLine, javaClass.simpleName, resolvedType)
    }
}

fun Division.analyze() {
    val leftType = left.getType()
    val rightType = right.getType()
    val resolvedType = resolveType(left, right, leftType, rightType)
    if (resolvedType == null)
        printIncompatibleTypesError(position.startLine, position.startIndexInLine, leftType, rightType)
    else {
        if (resolvedType == BooleanType || resolvedType == StringType || resolvedType is RangeType || resolvedType == UnitType ||
                resolvedType is ArrayType)
            printOperationDoesNotSupportError(position.startLine, position.startIndexInLine, javaClass.simpleName, resolvedType)
    }
}

fun Addition.analyze() {
    val leftType = left.getType()
    val rightType = right.getType()
    val resolvedType = resolveType(left, right, leftType, rightType)
    if (resolvedType == null)
        printIncompatibleTypesError(position.startLine, position.startIndexInLine, leftType, rightType)
    else {
        if (resolvedType == BooleanType || resolvedType is RangeType || resolvedType == UnitType ||
                resolvedType is ArrayType)
            printOperationDoesNotSupportError(position.startLine, position.startIndexInLine, javaClass.simpleName, resolvedType)
    }
}

fun Subtraction.analyze() {
    val leftType = left.getType()
    val rightType = right.getType()
    val resolvedType = resolveType(left, right, leftType, rightType)
    if (resolvedType == null)
        printIncompatibleTypesError(position.startLine, position.startIndexInLine, leftType, rightType)
    else {
        if (resolvedType == BooleanType || resolvedType == StringType || resolvedType is RangeType || resolvedType == UnitType ||
                resolvedType is ArrayType)
            printOperationDoesNotSupportError(position.startLine, position.startIndexInLine, javaClass.simpleName, resolvedType)
    }
}

fun SimpleAssignment.analyze() {
    val expectedType = findVarDeclaration(varName)?.type
    val actualType = value.getType()
    if (expectedType != actualType)
        printTypeMismatchError(position.startLine, position.startIndexInLine, expectedType, actualType)
}

fun ArrayAssignment.analyze() {
    val expectedType = (findVarDeclaration(arrayElement.arrayName)?.type as? ArrayType)?.innerType
    val actualType = value.getType()
    if (expectedType != actualType)
        printTypeMismatchError(position.startLine, position.startIndexInLine, expectedType, actualType)
}

fun IfStatement.analyze() {
    expression.analyze()
    val actualType = expression.getType()
    if (actualType != BooleanType)
        printTypeMismatchError(expression.position.startLine, expression.position.startIndexInLine, BooleanType, actualType)
    statements.forEach { it.analyze() }
    statements.checkForDuplicateVarDeclarations()
    elseStatements.forEach { it.analyze() }
    elseStatements.checkForDuplicateVarDeclarations()
}

fun WhileLoop.analyze() {
    factor.analyze()
    val actualType = factor.getType()
    if (actualType != BooleanType)
        printTypeMismatchError(factor.position.startLine, factor.position.startIndexInLine, BooleanType, actualType)
    statements.forEach { it.analyze() }
    statements.checkForDuplicateVarDeclarations()
}

fun ForLoop.analyze() {
    if (iterable.getType() !is IterableType)
        printIsNotIterableError(iterable.position.startLine, iterable.position.startIndexInLine)
    iterator.analyze()
    iterable.analyze()
    statements?.forEach { it.analyze() }
    statements?.let { it.checkForDuplicateVarDeclarations() }
}

fun ForLoopIterator.analyze() {
    //nothing to analyze
}

fun Expression.getType(): Type? {
    if (type == null)
        type = exploreType()
    return type
}

fun Expression.exploreType(): Type? = when (this) {
    is FunctionCall -> exploreType()
    is ForLoopIterator -> exploreType()
    is VarReference -> exploreType()
    is ArrayInit -> ArrayType(innerType, position, parent)
    is ArrayAccess -> exploreType()
    is Range -> start.getType()?.let { RangeType(it) }
    is EqualsExpression, is NotEqualsExpression, is LessExpression, is GreaterExpression, is LessOrEqualsExpression, is GreaterOrEqualsExpression -> BooleanType
    is Multiplication -> resolveType(left, right, left.exploreType(), right.exploreType())
    is Division -> resolveType(left, right, left.exploreType(), right.exploreType())
    is Addition -> resolveType(left, right, left.exploreType(), right.exploreType())
    is Subtraction -> resolveType(left, right, left.exploreType(), right.exploreType())
    is IntLit -> IntType
    is DoubleLit -> DoubleType
    is BooleanLit -> BooleanType
    is StringLit -> StringType
    is Increment -> expression.getType()
    is Decrement -> expression.getType()
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun FunctionCall.exploreType(): Type? {
    var type = getVisibleNodesIs<FunctionDeclaration>().find { it.isDeclarationOf(this) }?.returnType
    if (type == null)
        type = javaFunctions.find { it.isDeclarationOf(this) }?.returnType
    return type
}

fun ForLoopIterator.exploreType(): Type? {
    val forLoop = parent as ForLoop
    return (forLoop.iterable.getType() as? IterableType)?.innerType
}

fun ArrayAccess.exploreType(): Type? {
    val declaration = findVarDeclaration(arrayName)
    return (declaration?.type as? IterableType)?.innerType
}

fun VarReference.exploreType(): Type? {
    return findVarDeclaration(varName)?.type
}

fun resolveType(expr1: Expression, expr2: Expression,
                type1: Type?, type2: Type?): Type? {
    val compatibleType = areTypesCompatible(type1, type2)
    compatibleType?.let { return it }

    val autoCastType = isAutoCastPossible(type1, type2)
    autoCastType?.let {
        if (type1 != it) {
            expr1 castTo it
        }
        if (type2 != it) {
            expr2 castTo it
        }
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
            is RangeType -> if (type1 == type2) type1 else null
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