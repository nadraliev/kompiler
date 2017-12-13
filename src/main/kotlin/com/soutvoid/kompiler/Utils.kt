package com.soutvoid.kompiler

import io.bretty.console.tree.PrintableTreeNode
import org.antlr.v4.runtime.ParserRuleContext
import java.io.FileOutputStream
import java.util.*
import kotlin.reflect.KClass

fun <T: Any> Any.safecast(clazz: KClass<out T>): T? {
    try {
        return clazz.javaObjectType.cast(this)
    } catch (e: Exception) {
        return null
    }
}

infix fun List<Any>?.join(list: List<Any>?): List<Any> {
    val firstList = this ?: listOf()
    val secondList = list ?: listOf()
    return firstList.plus(secondList)
}

inline fun <T,R> ifNotNull(param1: T?, param2: R?, action: (param1: T, param2: R) -> Unit) {
    if (param1 != null && param2 != null)
        action(param1, param2)
}

infix fun List<Any>.plusNotNull(newElement: Any?): List<Any> =
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

inline fun <T, reified R> List<T>.findDuplicatesIs(crossinline predicate: (element1: R, element2: R) -> Boolean): List<List<R>> {
    return findDuplicatesBy { element1, element2 ->
        element1 is R && element2 is R && predicate(element1, element2)
    }.map { it.map { it as R } }
}

inline fun <T: Node> List<T>.checkForDuplicateVarDeclarations() {
    findDuplicatesIs<Node, VarDeclaration> { element1, element2 ->  element1.varName == element2.varName }.forEach {
        printDuplicatesError("properties", it.map { it.position })
    }
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

inline fun <reified T> Node.getVisibleNodesIs(): List<T> {
    val result = mutableListOf<T>()
    var parentNode: Node? = parent
    while (parentNode != null) {
        result.addAll(parentNode.children().filterIsInstance<T>())
        parentNode = parentNode.parent
    }
    return result
}

inline fun <reified T> Node.filterChildrenIs(): List<T> {
    val result = mutableListOf<T>()
    val childrenStack = Stack<Node>()
    children().map { it as Node }.forEach { childrenStack.push(it) }
    while (!childrenStack.empty()) {
        val child = childrenStack.pop()
        if (child is T) result.add(child)
        child.children().map { it as Node }.forEach { childrenStack.push(it) }
    }
    return result
}

fun FunctionDeclaration.isDeclarationOf(funcCall: FunctionCall): Boolean {
    ifNotNull(parameters, funcCall.parameters) { declarationParams, callParams ->
        if (declarationParams.size != callParams.size) return false
        declarationParams.forEachIndexed { index, parameter ->
            if (parameter.type != callParams[index].type) return false
        }
        return true
    }
    return false
}

fun Type.getJavaType(): Class<*>? = when(this) {
    is IntType, is BooleanType, is DoubleType, is StringType -> findClassByName(name())
    else -> throw throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun FileNode.getClassName(): String =
        name.replace("an", "An").replace(".", "")

fun ByteArray.writeClassToFile(path: String, name: String) {
    val fos = FileOutputStream(path + "/" + name + ".class")
    fos.write(this)
    fos.close()
}

fun FunctionDeclaration.getJvmDescription(): String =
        "("+ parameters.joinToString(separator = ",", transform = { it.type.getDescriptor() }) +
                ")" + returnType.getDescriptor()