package com.soutvoid.kompiler

var thereWasError = false

fun printlnError(errorMessage: String) {
    thereWasError = true
    println("\u001B[31m $errorMessage \u001B[0m")
}

fun printDuplicatesError(elementName: String, positions: List<Position>) {
    printlnError("Duplicating $elementName at positions: " +
            positions.map { it.startLine.toString() + ":" + it.startIndexInLine }.joinToString(","))
}

fun printTypeMismatchError(line: Int, charInLine: Int, expected: Type?, got: Type?) {
    printlnError("Type mismatch at: $line:$charInLine. Expected: ${expected?.name()}, but got: ${got?.name()}")
}

fun printIncompatibleTypesError(line: Int, charInLine: Int, type1: Type?, type2: Type?) {
    printlnError("Incompatible types at $line:$charInLine: ${type1?.name()} and ${type2?.name()}")
}

fun printOperationDoesNotSupportError(line: Int, charInLine: Int, operation: String, type: Type) {
    printlnError("Operation $operation does not support ${type.name()}: $line:$charInLine")
}

fun printUnresolvedReferenceError(line: Int, charInLine: Int, reference: String) {
    printlnError("Can't resolve reference \"$reference\" at $line:$charInLine")
}

fun printNoSuchFunctionError(line: Int, charInLine: Int, funcCall: FunctionCall) {
    printlnError("Can't find function with name ${funcCall.name} and parameters \"${funcCall.parameters?.toStringNames()}\" called at $line:$charInLine")
}

fun printReferenceIsNotAnArray(line: Int, charInLine: Int, name: String) {
    printlnError("Reference $name at $line:$charInLine is not an array")
}