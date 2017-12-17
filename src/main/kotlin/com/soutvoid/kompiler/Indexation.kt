package com.soutvoid.kompiler

fun FileNode.indexate() {
    filterChildrenIs<FunctionDeclaration>().forEach { it.indexate() }
}

fun FunctionDeclaration.indexate() {
    parameters.forEachIndexed { index, parameter -> vars.put(parameter.varName, index) }
    statements?.filterIsInstance<VarDeclaration>()?.forEachIndexed { index, varDeclaration ->
        vars.put(varDeclaration.varName, index + maxIndex() + 1)
    }
    statements?.filterIsInstance<WhileLoop>()?.forEach {
        it.indexate(maxIndex() + 1)
    }
    statements?.filterIsInstance<ForLoop>()?.forEach {
        it.indexate(maxIndex() + 1)
    }
}

fun WhileLoop.indexate(startIndex: Int) {
    statements.filterIsInstance<VarDeclaration>().forEachIndexed { index, varDeclaration ->
        vars.put(varDeclaration.varName, startIndex + index)
    }
    statements.filterIsInstance<WhileLoop>().forEach {
        it.indexate(maxIndex() + 1)
    }
    statements.filterIsInstance<ForLoop>().forEach {
        it.indexate(maxIndex() + 1)
    }
}

fun ForLoop.indexate(startIndex: Int) {
    vars.put(iterator.varName, startIndex)
    statements?.filterIsInstance<VarDeclaration>()?.forEachIndexed { index, varDeclaration ->
        vars.put(varDeclaration.varName, startIndex + index + 1)
    }
    statements?.filterIsInstance<WhileLoop>()?.forEach {
        it.indexate(maxIndex() + 1)
    }
    statements?.filterIsInstance<ForLoop>()?.forEach {
        it.indexate(maxIndex() + 1)
    }
}