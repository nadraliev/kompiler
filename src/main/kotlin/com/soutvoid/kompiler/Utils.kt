package com.soutvoid.kompiler

import io.bretty.console.tree.PrintableTreeNode
import org.antlr.v4.runtime.ParserRuleContext
import kotlin.reflect.KClass

fun List<Any>?.join(list: List<Any>?): List<Any> {
    val firstList = this ?: listOf()
    val secondList = list ?: listOf()
    return firstList.plus(secondList)
}

fun List<Any>.plusNotNull(newElement: Any?): List<Any> =
        if (newElement != null) this.plus(newElement) else this

fun List<PrintableTreeNode>?.toStringNames(separator: CharSequence = ","): String {
    if (this == null) return ""
    return map { it.name() }.joinToString(separator)
}

data class Position(val line: Int, val indexInLine: Int)

fun ParserRuleContext.considerPosition(): Position =
        Position(start.line, start.charPositionInLine)

fun <T> T.fillInParents(): T where T : Node {
    children().map { it as Node }.forEach { it.parent = this }
    return this
}

fun <T> List<T>.findDuplicatesBy(predicate: (element1: T, element2: T) -> Boolean): List<List<T>> {
    val result = mutableListOf<MutableList<T>>()
    forEach { element ->
        if (result.filter { it.contains(element) }.isEmpty()) {
            val duplicates = filter { predicate(element, it) }
            if (duplicates.size > 1)
                result.add(duplicates.toMutableList())
        }
    }
    return result
}

fun printlnError(errorMessage: String) {
    println("\u001B[31m $errorMessage \u001B[0m")
}

fun Node.closestParent(predicate: (parent: Node) -> Boolean): Node? {
    parent?.let {
        return if (predicate(it))
            it
        else it.closestParent(predicate)
    }
    return null
}

inline fun <reified T> Node.closestParentIs(): T? {
    val parent = closestParent { it is T }
    return if (parent != null)
        parent as T
    else null
}

fun resolveType(type1: Type?, type2: Type?): Type? {
    //TODO implement
    return null
}