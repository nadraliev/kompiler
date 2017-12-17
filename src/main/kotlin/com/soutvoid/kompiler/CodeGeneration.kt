package com.soutvoid.kompiler

import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.Ownership
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.implementation.FixedValue
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import java.io.File
import java.io.FileOutputStream

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
    returnExpression?.visit(methodVisitor)
    methodVisitor.visitInsn(RETURN)
    methodVisitor.visitMaxs(-1,-1)
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
    methodVisitor.visitEnd()
}

fun Statement.visit(methodVisitor: MethodVisitor) {
    when(this) {
        is Expression -> push(methodVisitor)
        is VarDeclaration -> visitLocalVar(methodVisitor)
        else -> throw UnsupportedOperationException()
    }
}

fun VarDeclaration.visitLocalVar(methodVisitor: MethodVisitor) {
    value.push(methodVisitor)
    val index = closestParentIs<ContainsIndexes>()!!.vars.getValue(varName)
    methodVisitor.visitVarInsn(getOpcode(ISTORE, type!!.apply {}), index)
}

fun Expression.push(methodVisitor: MethodVisitor) {
    val vars = closestParentIs<ContainsIndexes>()?.vars
    vars ?: return
    when(this) {
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
        else -> throw UnsupportedOperationException()
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
}

fun NotEqualsExpression.push(methodVisitor: MethodVisitor) {
    left.push(methodVisitor)
    right.push(methodVisitor)
}

fun GreaterExpression.push(methodVisitor: MethodVisitor) {
    left.push(methodVisitor)
    right.push(methodVisitor)
}

fun GreaterOrEqualsExpression.push(methodVisitor: MethodVisitor) {
    left.push(methodVisitor)
    right.push(methodVisitor)
}

fun LessExpression.push(methodVisitor: MethodVisitor) {
    left.push(methodVisitor)
    right.push(methodVisitor)
}

fun LessOrEqualsExpression.push(methodVisitor: MethodVisitor) {
    left.push(methodVisitor)
    right.push(methodVisitor)
}

fun VarReference.push(methodVisitor: MethodVisitor) {
    val index = closestParentIs<ContainsIndexes>()?.vars?.getValue(varName)
    val type = findVarDeclaration(varName)!!.type!!.apply {}
    if (index != null) {
        methodVisitor.visitVarInsn(getOpcode(ILOAD, type), index)
    } else pushGlobalVar(methodVisitor)
}

fun VarReference.pushGlobalVar(methodVisitor: MethodVisitor) {
    //TODO implement
}

fun FunctionCall.push(methodVisitor: MethodVisitor) {
    val isStatic = parent !is ClassDeclaration
    val javaFuncDeclaration = javaFunctions.find { it.isDeclarationOf(this) }
    if (javaFuncDeclaration != null)
        pushJavaFuncCall(methodVisitor, javaFuncDeclaration)
}

fun FunctionCall.pushJavaFuncCall(methodVisitor: MethodVisitor, declaration: FunctionDeclaration) {
    ifNotNull(declaration.annotation?.parameter, parameters) { annotationValue, funcCallParams ->
        if (annotationValue is StringLit) {
            val className = annotationValue.value.substringBefore(".")
            val funcName = annotationValue.value.substringAfter(".")
            val value = funcCallParams[0]
            value.push(methodVisitor)
            methodVisitor.visitMethodInsn(INVOKESTATIC, className, funcName, declaration.getJvmDescription(), false)
        }
    }
}

fun Type.getDescriptor(): String = when(this) {
    is IntType -> "I"
    is DoubleType -> "D"
    is UnitType -> "V"
    is BooleanType -> "Z"
    is StringType -> org.objectweb.asm.Type.getDescriptor(String::class.java)
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
    repeat(depth) {descriptor += "["}
    descriptor += innerType.getDescriptor()
    return descriptor
}

fun getOpcode(intOpcode: Int, type: Type): Int = when(type) {
    is IntType -> intOpcode
    is DoubleType -> getDoubleOpcode(intOpcode)
    is BooleanType -> getBooleanOpcode(intOpcode)
    else -> throw UnsupportedOperationException()
}

fun getDoubleOpcode(intOpcode: Int): Int = when(intOpcode) {
    ILOAD -> DLOAD
    ISTORE -> DSTORE
    IADD -> DADD
    IDIV -> DDIV
    ISUB -> DSUB
    IMUL -> DMUL
    else -> throw UnsupportedOperationException()
}

fun getBooleanOpcode(intOpcode: Int): Int = when(intOpcode) {
    ILOAD -> ILOAD
    ISTORE -> ISTORE
    else -> throw UnsupportedOperationException()
}