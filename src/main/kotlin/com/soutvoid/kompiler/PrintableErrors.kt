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

fun printIsNotIterableError(line: Int, charInLine: Int) {
    printlnError("Expected iterable at $line:$charInLine")
}

fun printNoSuchAnnotationError(line: Int, charInLine: Int, annotationName: String) {
    printlnError("Can't resolve annotation $annotationName at $line:$charInLine")
}

fun printJavaFunctionAlreadyExists(line: Int, charInLine: Int, funcName: String) {
    printlnError("Function $funcName at $line:$charInLine already exists in java functions")
}

fun printArraySizeError(line: Int, charInLine: Int, arrName: String) {
    printlnError("Array size must be integer more than 0. $arrName at $line:$charInLine")
}

fun printRangeTypeError(line: Int, charInLine: Int, type: Type?) {
    printlnError("Range cannot be of type ${type?.name()}. At $line:$charInLine")
}