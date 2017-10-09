package com.soutvoid.kompiler

import io.bretty.console.tree.PrintableTreeNode
import org.antlr.v4.runtime.ParserRuleContext

fun List<Any>?.join(list: List<Any>?): List<Any> {
    val firstList = this?: listOf()
    val secondList = list?: listOf()
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