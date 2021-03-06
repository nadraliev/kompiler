package com.soutvoid.kompiler

import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*

const val DEFAULT_PACKAGE = ""

fun FileNode.compileToFile(path: String) {
    compileStaticMembersToFile(path)
}

fun FileNode.compileStaticMembersToFile(path: String) {
    val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)

    classWriter.visit(V1_8, ACC_PUBLIC, DEFAULT_PACKAGE + getClassName(), null, "java/lang/Object", null)

    properties.forEach {
        it.visitField(classWriter, ACC_PUBLIC or ACC_STATIC)
    }

    if (properties.isNotEmpty())
        properties.visitClinit(classWriter, DEFAULT_PACKAGE + getClassName())

    functions.forEach {
        it.visitMethod(classWriter, ACC_PUBLIC or ACC_STATIC)
    }

    classWriter.visitEnd()

    classWriter.toByteArray().writeClassToFile(path, getClassName())
}

fun ClassDeclaration.compileToFile() {

}

fun VarDeclaration.visitField(classWriter: ClassWriter, access: Int) {
    val fieldVisitor = classWriter.visitField(access,
            varName, type!!.getDescriptor(), null, null)
    fieldVisitor.visitEnd()
}

fun FunctionDeclaration.visitMethod(classWriter: ClassWriter, access: Int) {
    val methodVisitor = classWriter.visitMethod(access, name, getJvmDescription(), null, null)
    val start = Label()
    methodVisitor.visitCode()
    methodVisitor.visitLabel(start)
    statements?.forEach { it.visit(methodVisitor) }
    if (returnType is UnitType)
        methodVisitor.visitInsn(RETURN)
    methodVisitor.visitMaxs(-1, -1)
    methodVisitor.visitEnd()
}

fun List<VarDeclaration>.visitClinit(classWriter: ClassWriter, className: String) {
    val methodVisitor = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null)
    methodVisitor.visitCode()
    forEach {
        it.value.push(methodVisitor)
        methodVisitor.visitFieldInsn(PUTSTATIC, className, it.varName, it.type!!.getDescriptor())
    }
    methodVisitor.visitInsn(RETURN)
    methodVisitor.visitMaxs(-1, -1)
    methodVisitor.visitEnd()
}

fun Statement.visit(methodVisitor: MethodVisitor) {
    when (this) {
        is Expression -> push(methodVisitor)
        is VarDeclaration -> visitLocalVar(methodVisitor)
        is SimpleAssignment -> visit(methodVisitor)
        is IfStatement -> visit(methodVisitor)
        is ArrayAssignment -> visit(methodVisitor)
        is WhileLoop -> visit(methodVisitor)
        is ForLoop -> visit(methodVisitor)
        is Return -> visit(methodVisitor)
        else -> throw UnsupportedOperationException()
    }
}

fun Return.visit(methodVisitor: MethodVisitor) {
    expression?.visit(methodVisitor)
    methodVisitor.visitInsn(getOpcode(IRETURN, expression?.getType() ?: UnitType))
}

fun ForLoop.visit(methodVisitor: MethodVisitor) {
    val iteratorIndex = vars[iterator.varName] as Int
    val arrayCopyIndex = iteratorIndex + 1
    val arrayLengthIndex = iteratorIndex + 2
    val currentIndexIndex = iteratorIndex + 3
    val afterLoop = Label()
    val beforeLoop = Label()
    val iteratorType = iterator.type as Type
    //copy array
    iterable.push(methodVisitor)
    methodVisitor.visitVarInsn(ASTORE, arrayCopyIndex)

    //store array length
    methodVisitor.visitVarInsn(ALOAD, arrayCopyIndex)
    methodVisitor.visitInsn(ARRAYLENGTH)
    methodVisitor.visitVarInsn(ISTORE, arrayLengthIndex)

    //store index
    methodVisitor.visitLdcInsn(0)
    methodVisitor.visitVarInsn(ISTORE, currentIndexIndex)

    methodVisitor.visitLabel(beforeLoop)
    //check if current index is less than arr length
    methodVisitor.visitVarInsn(ILOAD, currentIndexIndex)
    methodVisitor.visitVarInsn(ILOAD, arrayLengthIndex)
    methodVisitor.visitJumpInsn(IF_ICMPGE, afterLoop)

    //load current value
    methodVisitor.visitVarInsn(ALOAD, arrayCopyIndex)
    methodVisitor.visitVarInsn(ILOAD, currentIndexIndex)
    methodVisitor.visitInsn(IALOAD)
    methodVisitor.visitVarInsn(getOpcode(ISTORE, iteratorType), iteratorIndex)

    statements?.forEach { it.visit(methodVisitor) }

    methodVisitor.visitIincInsn(currentIndexIndex, 1)
    methodVisitor.visitJumpInsn(GOTO, beforeLoop)

    methodVisitor.visitLabel(afterLoop)
}

fun WhileLoop.visit(methodVisitor: MethodVisitor) {
    val beforeLoop = Label()
    val afterLoop = Label()
    methodVisitor.visitLabel(beforeLoop)
    factor.push(methodVisitor)
    methodVisitor.visitJumpInsn(IFEQ, afterLoop)
    statements.forEach { it.visit(methodVisitor) }
    methodVisitor.visitJumpInsn(GOTO, beforeLoop)
    methodVisitor.visitLabel(afterLoop)
}

fun ArrayAssignment.visit(methodVisitor: MethodVisitor) {
    val index = findIndex(arrayElement.arrayName, this)
    val type = findVarDeclaration(arrayElement.arrayName)!!.type as ArrayType
    val innerType = type.innerType
    if (index != -1)
        methodVisitor.visitVarInsn(ALOAD, index)
    else
        methodVisitor.visitFieldInsn(GETSTATIC, getClassName(), arrayElement.arrayName, type.getDescriptor())
    arrayElement.index.push(methodVisitor)
    value.push(methodVisitor)
    methodVisitor.visitInsn(getArrayOpcode(IASTORE, innerType))

}

fun IfStatement.visit(methodVisitor: MethodVisitor) {
    expression.push(methodVisitor)
    val notTrue = Label()
    val isTrue = Label()
    methodVisitor.visitJumpInsn(IFEQ, notTrue)
    statements.forEach { it.visit(methodVisitor) }
    methodVisitor.visitJumpInsn(GOTO, isTrue)
    methodVisitor.visitLabel(notTrue)
    elseStatements.forEach { it.visit(methodVisitor) }
    methodVisitor.visitLabel(isTrue)
}

fun SimpleAssignment.visit(methodVisitor: MethodVisitor) {
    value.push(methodVisitor)
    val index = findIndex(varName, this)
    val type = findVarDeclaration(varName)!!.type!!.apply {}
    if (index != -1)
        methodVisitor.visitVarInsn(getOpcode(ISTORE, type), index)
    else
        methodVisitor.visitFieldInsn(PUTSTATIC, getClassName(), varName, type.getDescriptor())
}

fun VarDeclaration.visitLocalVar(methodVisitor: MethodVisitor) {
    value.push(methodVisitor)
    val index = findIndex(varName, this)
    methodVisitor.visitVarInsn(getOpcode(ISTORE, type!!.apply {}), index)
}

fun Expression.push(methodVisitor: MethodVisitor) {
    when (this) {
        is IntLit -> methodVisitor.visitLdcInsn(value.toInt())
        is DoubleLit -> methodVisitor.visitLdcInsn(value.toDouble())
        is BooleanLit -> methodVisitor.visitLdcInsn(value.toBoolean().getInt())
        is StringLit -> methodVisitor.visitLdcInsn(value)
        is FunctionCall -> push(methodVisitor)
        is VarReference -> push(methodVisitor)
        is Addition -> push(methodVisitor)
        is Subtraction -> push(methodVisitor)
        is Division -> push(methodVisitor)
        is Multiplication -> push(methodVisitor)
        is EqualsExpression -> push(methodVisitor)
        is NotEqualsExpression -> push(methodVisitor)
        is GreaterExpression -> push(methodVisitor)
        is GreaterOrEqualsExpression -> push(methodVisitor)
        is LessExpression -> push(methodVisitor)
        is LessOrEqualsExpression -> push(methodVisitor)
        is ArrayInit -> push(methodVisitor)
        is ArrayAccess -> push(methodVisitor)
        is Range -> push(methodVisitor)
        is Increment -> push(methodVisitor)
        is Decrement -> push(methodVisitor)
        else -> throw UnsupportedOperationException()
    }
    ifNotNull(type, castTo) { from, to ->
        methodVisitor.visitInsn(getCastOpcode(from, to))
    }
}

fun Increment.push(methodVisitor: MethodVisitor) {
    val type = expression.getType() as Type
    expression.push(methodVisitor)
    methodVisitor.visitLdcInsn(1)
    methodVisitor.visitInsn(getOpcode(IADD, expression.getType() as Type))
    if (expression is VarReference) {
        val index = findIndex(expression.varName, this)
        methodVisitor.visitVarInsn(getOpcode(ISTORE, type), index)
        if (!isStandaloneStatement())
            methodVisitor.visitVarInsn(getOpcode(ILOAD, type), index)
    }
}

fun Decrement.push(methodVisitor: MethodVisitor) {
    val type = expression.getType() as Type
    expression.push(methodVisitor)
    methodVisitor.visitLdcInsn(1)
    methodVisitor.visitInsn(getOpcode(ISUB, expression.getType() as Type))
    if (expression is VarReference) {
        val index = findIndex(expression.varName, this)
        methodVisitor.visitVarInsn(getOpcode(ISTORE, type), index)
        if (!isStandaloneStatement())
            methodVisitor.visitVarInsn(getOpcode(ILOAD, type), index)
    }
}

fun Range.push(methodVisitor: MethodVisitor) {
    endInclusive.push(methodVisitor)
    start.push(methodVisitor)
    methodVisitor.visitInsn(getOpcode(ISUB, start.type!!.apply {}))
    start.push(methodVisitor)
    endInclusive.push(methodVisitor)
    var startLess = Label()
    methodVisitor.visitJumpInsn(IF_ICMPLT, startLess)
    methodVisitor.visitInsn(INEG)
    methodVisitor.visitLabel(startLess)
    methodVisitor.visitLdcInsn(1)
    methodVisitor.visitInsn(IADD)
    methodVisitor.visitIntInsn(NEWARRAY, (type as NestedType).innerType.getAtype())

    val valueIndex = closestParentIs<ContainsIndexes>()?.maxIndex() ?: 1
    val index = closestParentIs<ContainsIndexes>()?.maxIndex()?.plus(1) ?: 2

    start.push(methodVisitor)
    methodVisitor.visitVarInsn(ISTORE, valueIndex)
    methodVisitor.visitLdcInsn(0)
    methodVisitor.visitVarInsn(ISTORE, index)
    val beforeLoop = Label()
    val afterLoop = Label()
    methodVisitor.visitLabel(beforeLoop)
    methodVisitor.visitVarInsn(ILOAD, valueIndex)
    endInclusive.push(methodVisitor)

    methodVisitor.visitLdcInsn(1)
    start.push(methodVisitor)
    endInclusive.push(methodVisitor)
    startLess = Label()
    val afterIf = Label()
    methodVisitor.visitJumpInsn(IF_ICMPLT, startLess)
    methodVisitor.visitInsn(ISUB)
    methodVisitor.visitJumpInsn(GOTO, afterIf)
    methodVisitor.visitLabel(startLess)
    methodVisitor.visitInsn(IADD)
    methodVisitor.visitLabel(afterIf)

    methodVisitor.visitJumpInsn(IF_ICMPEQ, afterLoop)
    //load value to array
    methodVisitor.visitInsn(DUP)
    methodVisitor.visitVarInsn(ILOAD, index)
    methodVisitor.visitVarInsn(ILOAD, valueIndex)
    methodVisitor.visitInsn(IASTORE)
    //increment index
    methodVisitor.visitVarInsn(ILOAD, index)
    methodVisitor.visitLdcInsn(1)
    methodVisitor.visitInsn(IADD)
    methodVisitor.visitVarInsn(ISTORE, index)

    //increment/decrement value
    val increment = Label()
    val decrement = Label()
    start.push(methodVisitor)
    endInclusive.push(methodVisitor)
    methodVisitor.visitJumpInsn(IF_ICMPGT, decrement)
    methodVisitor.visitVarInsn(ILOAD, valueIndex)
    methodVisitor.visitLdcInsn(1)
    methodVisitor.visitInsn(IADD)
    methodVisitor.visitVarInsn(ISTORE, valueIndex)
    methodVisitor.visitJumpInsn(GOTO, beforeLoop)

    methodVisitor.visitLabel(decrement)
    methodVisitor.visitVarInsn(ILOAD, valueIndex)
    methodVisitor.visitLdcInsn(1)
    methodVisitor.visitInsn(ISUB)
    methodVisitor.visitVarInsn(ISTORE, valueIndex)
    methodVisitor.visitJumpInsn(GOTO, beforeLoop)


    methodVisitor.visitLabel(afterLoop)

}

fun ArrayAccess.push(methodVisitor: MethodVisitor) {
    val varIndex = findIndex(arrayName, this)
    val type = findVarDeclaration(arrayName)!!.type as ArrayType
    val innerType = type.innerType
    if (varIndex != -1)
        methodVisitor.visitVarInsn(ALOAD, varIndex)
    else
        methodVisitor.visitFieldInsn(GETSTATIC, getClassName(), arrayName, type.getDescriptor())
    index.push(methodVisitor)
    methodVisitor.visitInsn(getArrayOpcode(IALOAD, innerType))

}

fun ArrayInit.push(methodVisitor: MethodVisitor) {
    methodVisitor.visitLdcInsn(size)
    if (innerType is ArrayType) {
        //TODO nested arrays
    } else {
        if (innerType is StringType)
            methodVisitor.visitTypeInsn(ANEWARRAY,
                    (innerType as StringType).getAtype())
        else {
            methodVisitor.visitIntInsn(NEWARRAY, innerType.getAtype())
        }
    }
}

fun Addition.push(methodVisitor: MethodVisitor) {
    left.push(methodVisitor)
    right.push(methodVisitor)
    methodVisitor.visitInsn(getOpcode(IADD, type!!.apply {}))
}

fun Subtraction.push(methodVisitor: MethodVisitor) {
    left.push(methodVisitor)
    right.push(methodVisitor)
    methodVisitor.visitInsn(getOpcode(ISUB, type!!.apply {}))
}

fun Division.push(methodVisitor: MethodVisitor) {
    left.push(methodVisitor)
    right.push(methodVisitor)
    methodVisitor.visitInsn(getOpcode(IDIV, type!!.apply {}))
}

fun Multiplication.push(methodVisitor: MethodVisitor) {
    left.push(methodVisitor)
    right.push(methodVisitor)
    methodVisitor.visitInsn(getOpcode(IMUL, type!!.apply {}))
}

fun EqualsExpression.push(methodVisitor: MethodVisitor) {
    left.push(methodVisitor)
    right.push(methodVisitor)
    val notEqual = Label()
    val equal = Label()
    if (left.type is DoubleType || right.type is DoubleType) {
        methodVisitor.visitInsn(DCMPL)
        methodVisitor.visitJumpInsn(IFNE, notEqual)
    } else {
        methodVisitor.visitJumpInsn(IF_ICMPNE, notEqual)
    }
    methodVisitor.visitLdcInsn(1)
    methodVisitor.visitJumpInsn(GOTO, equal)
    methodVisitor.visitLabel(notEqual)
    methodVisitor.visitLdcInsn(0)
    methodVisitor.visitLabel(equal)
}

fun NotEqualsExpression.push(methodVisitor: MethodVisitor) {
    left.push(methodVisitor)
    right.push(methodVisitor)
    val notTrue = Label()
    val isTrue = Label()
    if (left.type is DoubleType || right.type is DoubleType) {
        methodVisitor.visitInsn(DCMPL)
        methodVisitor.visitJumpInsn(IFEQ, notTrue)
    } else {
        methodVisitor.visitJumpInsn(IF_ICMPEQ, notTrue)
    }
    methodVisitor.visitLdcInsn(1)
    methodVisitor.visitJumpInsn(GOTO, isTrue)
    methodVisitor.visitLabel(notTrue)
    methodVisitor.visitLdcInsn(0)
    methodVisitor.visitLabel(isTrue)
}

fun GreaterExpression.push(methodVisitor: MethodVisitor) {
    left.push(methodVisitor)
    right.push(methodVisitor)
    val exprIsTrue = Label()
    val exprIsNotTrue = Label()
    if (left.type is DoubleType || right.type is DoubleType) {
        methodVisitor.visitInsn(DCMPL)
        methodVisitor.visitJumpInsn(IFLE, exprIsNotTrue)
    } else {
        methodVisitor.visitJumpInsn(IF_ICMPLE, exprIsNotTrue)
    }
    methodVisitor.visitLdcInsn(1)
    methodVisitor.visitJumpInsn(GOTO, exprIsTrue)
    methodVisitor.visitLabel(exprIsNotTrue)
    methodVisitor.visitLdcInsn(0)
    methodVisitor.visitLabel(exprIsTrue)
}

fun GreaterOrEqualsExpression.push(methodVisitor: MethodVisitor) {
    left.push(methodVisitor)
    right.push(methodVisitor)
    val exprIsTrue = Label()
    val exprIsNotTrue = Label()
    if (left.type is DoubleType || right.type is DoubleType) {
        methodVisitor.visitInsn(DCMPL)
        methodVisitor.visitJumpInsn(IFLT, exprIsNotTrue)
    } else {
        methodVisitor.visitJumpInsn(IF_ICMPLT, exprIsNotTrue)
    }
    methodVisitor.visitLdcInsn(1)
    methodVisitor.visitJumpInsn(GOTO, exprIsTrue)
    methodVisitor.visitLabel(exprIsNotTrue)
    methodVisitor.visitLdcInsn(0)
    methodVisitor.visitLabel(exprIsTrue)
}

fun LessExpression.push(methodVisitor: MethodVisitor) {
    left.push(methodVisitor)
    right.push(methodVisitor)
    val exprIsTrue = Label()
    val exprIsNotTrue = Label()
    if (left.type is DoubleType || right.type is DoubleType) {
        methodVisitor.visitInsn(DCMPL)
        methodVisitor.visitJumpInsn(IFGE, exprIsNotTrue)
    } else {
        methodVisitor.visitJumpInsn(IF_ICMPGE, exprIsNotTrue)
    }
    methodVisitor.visitLdcInsn(1)
    methodVisitor.visitJumpInsn(GOTO, exprIsTrue)
    methodVisitor.visitLabel(exprIsNotTrue)
    methodVisitor.visitLdcInsn(0)
    methodVisitor.visitLabel(exprIsTrue)
}

fun LessOrEqualsExpression.push(methodVisitor: MethodVisitor) {
    left.push(methodVisitor)
    right.push(methodVisitor)
    val exprIsTrue = Label()
    val exprIsNotTrue = Label()
    if (left.type is DoubleType || right.type is DoubleType) {
        methodVisitor.visitInsn(DCMPL)
        methodVisitor.visitJumpInsn(IFGT, exprIsNotTrue)
    } else {
        methodVisitor.visitJumpInsn(IF_ICMPGT, exprIsNotTrue)
    }
    methodVisitor.visitLdcInsn(1)
    methodVisitor.visitJumpInsn(GOTO, exprIsTrue)
    methodVisitor.visitLabel(exprIsNotTrue)
    methodVisitor.visitLdcInsn(0)
    methodVisitor.visitLabel(exprIsTrue)
}

fun VarReference.push(methodVisitor: MethodVisitor) {
    val index = findIndex(varName, this)
    val type = findVarDeclaration(varName)!!.type!!.apply {}
    if (index != -1)
        methodVisitor.visitVarInsn(getOpcode(ILOAD, type), index)
    else
        methodVisitor.visitFieldInsn(GETSTATIC, getClassName(), varName, type.getDescriptor())
}

fun FunctionCall.push(methodVisitor: MethodVisitor) {
    val isStatic = parent !is ClassDeclaration
    val javaFuncDeclaration = javaFunctions.find { it.isDeclarationOf(this) }
    if (javaFuncDeclaration != null)
        pushJavaFuncCall(methodVisitor, javaFuncDeclaration)
    else {
        val declarations = getVisibleNodesIs<FunctionDeclaration>()
        val declaration = declarations.find { it.isDeclarationOf(this) }
        parameters?.forEach { it.push(methodVisitor) }
        if (isStatic) {
            methodVisitor.visitMethodInsn(INVOKESTATIC, declaration!!.getClassName(), name, declaration.getJvmDescription(), false)
        }
    }
}

fun FunctionCall.pushJavaFuncCall(methodVisitor: MethodVisitor, declaration: FunctionDeclaration) {
    ifNotNull(declaration.annotation?.parameter, parameters) { annotationValue, funcCallParams ->
        if (annotationValue is StringLit) {
            val className = annotationValue.value.substringBefore(".")
            val funcName = annotationValue.value.substringAfter(".")
            funcCallParams.forEach { it.push(methodVisitor) }
            val jvm = declaration.getJvmDescription()
            methodVisitor.visitMethodInsn(INVOKESTATIC, className, funcName, declaration.getJvmDescription(), false)
        }
    }
}

fun Type.getDescriptor(): String = when (this) {
    is IntType -> "I"
    is DoubleType -> "D"
    is UnitType -> "V"
    is BooleanType -> "Z"
    is StringType -> "Ljava/lang/String;"
    is ArrayType -> getDescriptor()
    else -> throw UnsupportedOperationException()
}

fun ArrayType.getDescriptor(): String {
    var depth = 1
    var innerType = innerType
    while (innerType is ArrayType) {
        depth++
        innerType = innerType.innerType
    }
    var descriptor = ""
    repeat(depth) { descriptor += "[" }
    descriptor += innerType.getDescriptor()
    return descriptor
}

fun getOpcode(intOpcode: Int, type: Type): Int = when (type) {
    is IntType -> intOpcode
    is DoubleType -> getDoubleOpcode(intOpcode)
    is BooleanType -> getBooleanOpcode(intOpcode)
    is StringType, is ArrayType -> getObjectReferenceOpcode(intOpcode)
    is UnitType -> getVoidOpcode(intOpcode)
    else -> throw UnsupportedOperationException()
}

fun getVoidOpcode(intOpcode: Int): Int = when (intOpcode) {
    IRETURN -> RETURN
    else -> throw UnsupportedOperationException()
}

fun getObjectReferenceOpcode(intOpcode: Int): Int = when (intOpcode) {
    ILOAD -> ALOAD
    ISTORE -> ASTORE
    IRETURN -> ARETURN
    else -> throw UnsupportedOperationException()
}

fun getDoubleOpcode(intOpcode: Int): Int = when (intOpcode) {
    ILOAD -> DLOAD
    ISTORE -> DSTORE
    IADD -> DADD
    IDIV -> DDIV
    ISUB -> DSUB
    IMUL -> DMUL
    IRETURN -> DRETURN
    else -> throw UnsupportedOperationException()
}

fun getBooleanOpcode(intOpcode: Int): Int = intOpcode

fun getCastOpcode(from: Type, to: Type): Int = when (from to to) {
    IntType to DoubleType -> I2D
    DoubleType to IntType -> D2I
    else -> throw UnsupportedOperationException()
}

fun getArrayOpcode(intOpcode: Int, type: Type): Int = when (type) {
    is IntType -> intOpcode
    is DoubleType -> getDoubleArrayOpcode(intOpcode)
    is BooleanType -> getBooleanArrayOpcode(intOpcode)
    is StringType, is ArrayType -> getObjectReferenceArrayOpcode(intOpcode)
    else -> throw UnsupportedOperationException()
}

fun getObjectReferenceArrayOpcode(intOpcode: Int): Int = when (intOpcode) {
    IALOAD -> AALOAD
    IASTORE -> AASTORE
    else -> throw UnsupportedOperationException()
}

fun getDoubleArrayOpcode(intOpcode: Int): Int = when (intOpcode) {
    IALOAD -> DALOAD
    IASTORE -> DASTORE
    else -> throw UnsupportedOperationException()
}

fun getBooleanArrayOpcode(intOpcode: Int): Int = intOpcode

fun Type.getAtype(): Int = when (this) {
    is IntType -> T_INT
    is DoubleType -> T_DOUBLE
    is BooleanType -> T_BOOLEAN
    else -> throw UnsupportedOperationException()
}

fun StringType.getAtype(): String = "java/lang/String"
fun ArrayType.getAtype(): String = getDescriptor()