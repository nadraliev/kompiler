package com.soutvoid.kompiler

fun FileNode.indexate() {
    filterChildrenIs<FunctionDeclaration>().forEach { it.indexate() }
}

fun FunctionDeclaration.indexate() {
    parameters.forEachIndexed { index, parameter -> vars.put(parameter.varName, index) }
    statements?.filterIsInstance<VarDeclaration>()?.forEach {
        vars.put(it.varName, maxIndex() + 1)
        if (it.type is DoubleType)
            vars.put("", maxIndex() + 1)
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
        if (varDeclaration.type is DoubleType)
            vars.put("", startIndex + index)
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
        if (varDeclaration.type is DoubleType)
            vars.put("", startIndex + index)
    }
    statements?.filterIsInstance<WhileLoop>()?.forEach {
        it.indexate(maxIndex() + 1)
    }
    statements?.filterIsInstance<ForLoop>()?.forEach {
        it.indexate(maxIndex() + 1)
    }
}