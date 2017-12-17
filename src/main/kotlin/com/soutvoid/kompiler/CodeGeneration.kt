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
    methodVisitor.visitCode()
    statements?.forEach { it.visit(methodVisitor) }
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
        else -> throw UnsupportedOperationException()
    }
}

fun Expression.push(methodVisitor: MethodVisitor) {
    val vars = closestParentIs<ContainsIndexes>()?.vars
    vars ?: return
    when(this) {
        is IntLit -> methodVisitor.visitLdcInsn(value.toInt())
        is DoubleLit -> methodVisitor.visitLdcInsn(value.toDouble())
        is BooleanLit -> methodVisitor.visitLdcInsn(value.toBoolean())
        is StringLit -> methodVisitor.visitLdcInsn(value)
        is FunctionCall -> push(methodVisitor)
        else -> throw UnsupportedOperationException()
    }
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
            val str = funcCallParams[0] as StringLit
            str.push(methodVisitor)
            methodVisitor.visitMethodInsn(INVOKESTATIC, className, funcName, declaration.getJvmDescription(), false)
        }
    }
}

fun Type.getDescriptor(): String = when(this) {
    is IntType -> "I"
    is DoubleType -> "D"
    is UnitType -> "V"
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