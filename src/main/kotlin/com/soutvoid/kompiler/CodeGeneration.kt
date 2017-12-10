package com.soutvoid.kompiler

import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.Ownership
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.implementation.FixedValue
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import java.io.File
import java.io.FileOutputStream

const val DEFAULT_PACKAGE = "com/soutvoid/kompiler/"

fun FileNode.compileToFile() {
    compileStaticMembersToFile()
}

fun FileNode.compileStaticMembersToFile() {
    val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)

    classWriter.visit(V1_8, ACC_PUBLIC, DEFAULT_PACKAGE + getClassName(), null, "java/lang/Object", null)

    properties.forEach {
        it.visitField(classWriter, ACC_PUBLIC or ACC_STATIC)
    }

    properties.visitClinit(classWriter, DEFAULT_PACKAGE + getClassName())

    functions.forEach {
        it.visitMethod(classWriter, ACC_PUBLIC or ACC_STATIC)
    }

    classWriter.visitEnd()

    classWriter.toByteArray().writeClassToFile(getClassName())
}

fun ClassDeclaration.compileToFile() {

}

fun VarDeclaration.visitField(classWriter: ClassWriter, access: Int) {
    val fieldVisitor = classWriter.visitField(access,
            varName, type.getDescriptor(), null, null)
    fieldVisitor.visitEnd()
}

fun FunctionDeclaration.visitMethod(classWriter: ClassWriter, access: Int) {
    val methodVisitor = classWriter.visitMethod(access, name, getJvmDescription(), null, null)
    methodVisitor.visitEnd()
}

fun List<VarDeclaration>.visitClinit(classWriter: ClassWriter, className: String) {
    val methodVisitor = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null)
    methodVisitor.visitCode()
    forEach {
        it.value.push(methodVisitor)
        methodVisitor.visitFieldInsn(PUTSTATIC, className, it.varName, it.type.getDescriptor())
    }
    methodVisitor.visitInsn(RETURN)
    methodVisitor.visitEnd()
}

fun Expression.push(methodVisitor: MethodVisitor) {
    val vars = closestParentIs<ContainsIndexes>()?.vars
    vars ?: return
    when(this) {
        is IntLit -> methodVisitor.visitLdcInsn(value.toInt())
        is DoubleLit -> methodVisitor.visitLdcInsn(value.toDouble())
        is BooleanLit -> methodVisitor.visitLdcInsn(value.toBoolean())
        else -> throw UnsupportedOperationException()
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
    descriptor += "L${innerType.getDescriptor()};"
    return descriptor
}