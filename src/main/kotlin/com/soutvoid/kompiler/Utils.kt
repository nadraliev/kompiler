package com.soutvoid.kompiler

import io.bretty.console.tree.PrintableTreeNode

fun List<Any>?.join(list: List<Any>?): List<Any> {
    val firstList = this?: listOf()
    val secondList = list?: listOf()
    return firstList.plus(secondList)
}

fun List<PrintableTreeNode>?.toStringNames(separator: CharSequence = ","): String {
    if (this == null) return ""
    return map { it.name() }.joinToString(separator)
}