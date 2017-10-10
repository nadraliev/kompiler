package com.soutvoid.kompiler

fun printlnError(errorMessage: String) {
    println("\u001B[31m $errorMessage \u001B[0m")
}

fun printDuplicatesError(elementName: String, positions: List<Position>) {
    printlnError("Duplicating $elementName at positions: " +
            positions.map { it.line.toString() + ":" + it.indexInLine }.joinToString(","))
}

fun printTypeMismatchError(line: Int, charInLine: Int, expected: Type?, got: Type?) {
    printlnError("Type mismatch at: $line:$charInLine. Expected: ${expected?.name()}, but got: ${got?.name()}")
}