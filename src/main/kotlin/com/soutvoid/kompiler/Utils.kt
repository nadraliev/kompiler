package com.soutvoid.kompiler

import io.bretty.console.tree.PrintableTreeNode
import org.antlr.v4.runtime.ParserRuleContext
import kotlin.reflect.KClass

fun List<Any>?.join(list: List<Any>?): List<Any> {
    val firstList = this ?: listOf()
    val secondList = list ?: listOf()
    return firstList.plus(secondList)
}

fun <T,R> ifNotNull(param1: T?, param2: R?, action: (param1: T, param2: R) -> Unit) {
    if (param1 != null && param2 != null)
        action(param1, param2)
}

fun List<Any>.plusNotNull(newElement: Any?): List<Any> =
        if (newElement != null) this.plus(newElement) else this

fun List<PrintableTreeNode>?.toStringNames(separator: CharSequence = ","): String {
    if (this == null) return ""
    return map { it.name() }.joinToString(separator)
}

data class Position(val startLine: Int, val startIndexInLine: Int,
                    val endLine: Int, val endIndexInLine: Int)

fun ParserRuleContext.considerPosition(): Position =
        Position(start.line, start.charPositionInLine,
                stop.line, stop.charPositionInLine)

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

fun Node.getVisibleVarDeclarations(): List<VarDeclaration> {
    val result = mutableListOf<VarDeclaration>()
    var parentNode: Node? = parent
    while (parentNode != null) {
        result.addAll(parentNode.children().filterIsInstance<VarDeclaration>())
        parentNode = parentNode.parent
    }
    return result
}