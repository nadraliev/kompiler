package com.soutvoid.kompiler

fun FileNode.indexate() {
    filterChildrenIs<FunctionDeclaration>().forEach { it.indexate() }
}

fun FunctionDeclaration.indexate() {
    statements?.filterIsInstance<VarDeclaration>()?.forEachIndexed { index, varDeclaration ->
        vars.put(varDeclaration.varName, index)
    }
    statements?.filterIsInstance<WhileLoop>()?.forEach {
        val closestVarName = it.findClosestSiblingIs<VarDeclaration>()?.varName
        it.indexate(vars[closestVarName]?.plus(1) ?: 0)
    }
    statements?.filterIsInstance<ForLoop>()?.forEach {
        val closestVarName = it.findClosestSiblingIs<VarDeclaration>()?.varName
        it.indexate(vars[closestVarName]?.plus(1) ?: 0)
    }
}

fun WhileLoop.indexate(startIndex: Int) {
    statements.filterIsInstance<VarDeclaration>().forEachIndexed { index, varDeclaration ->
        vars.put(varDeclaration.varName, startIndex + index)
    }
    statements.filterIsInstance<WhileLoop>().forEach {
        val closestVarName = it.findClosestSiblingIs<VarDeclaration>()?.varName
        it.indexate(vars[closestVarName]?.plus(1) ?: startIndex)
    }
    statements.filterIsInstance<ForLoop>().forEach {
        val closestVarName = it.findClosestSiblingIs<VarDeclaration>()?.varName
        it.indexate(vars[closestVarName]?.plus(1) ?: startIndex)
    }
}

fun ForLoop.indexate(startIndex: Int) {
    vars.put(iterator.varName, startIndex)
    statements?.filterIsInstance<VarDeclaration>()?.forEachIndexed { index, varDeclaration ->
        vars.put(varDeclaration.varName, startIndex + index + 1)
    }
    statements?.filterIsInstance<WhileLoop>()?.forEach {
        val closestVarName = it.findClosestSiblingIs<VarDeclaration>()?.varName
        it.indexate(vars[closestVarName]?.plus(1) ?: startIndex + 1)
    }
    statements?.filterIsInstance<ForLoop>()?.forEach {
        val closestVarName = it.findClosestSiblingIs<VarDeclaration>()?.varName
        it.indexate(vars[closestVarName]?.plus(1) ?: startIndex + 1)
    }
}

inline fun <reified T: Node> Node.findClosestSiblingIs(): T? {
    val parent = parent
    parent?.let {
        val siblingsOfType = parent.children().filterIsInstance<T>()
                .filter { it.position.startLine < position.startLine }
        return siblingsOfType.minBy { position.startLine - it.position.startLine }
    }
    return null
}