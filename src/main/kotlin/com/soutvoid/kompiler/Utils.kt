package com.soutvoid.kompiler

fun List<Any>?.join(list: List<Any>?): List<Any> {
    val firstList = this?: listOf()
    val secondList = list?: listOf()
    return firstList.plus(secondList)
}