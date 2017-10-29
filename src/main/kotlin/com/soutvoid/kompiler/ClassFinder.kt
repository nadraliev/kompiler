package com.soutvoid.kompiler

val searchPackages = listOf(
        "java.lang",
        "java.util",
        "my.company",
        "my.company.other")

fun findClassByName(name: String): Class<*>? {
    searchPackages.forEach {
        try {
            return Class.forName(it + "." + name)
        } catch (e: Exception) {
        }
    }
    return null
}