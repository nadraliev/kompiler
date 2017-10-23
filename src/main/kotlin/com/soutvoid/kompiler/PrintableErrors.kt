package com.soutvoid.kompiler

fun printlnError(errorMessage: String) {
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

fun printUnresolvedReference(line: Int, charInLine: Int, reference: String) {
    printlnError("Can't resolve reference \"$reference\" at $line:$charInLine")
}