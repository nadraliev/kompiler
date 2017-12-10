package com.soutvoid.kompiler

import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.Ownership
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.implementation.FixedValue
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import java.io.File
import java.io.FileOutputStream

fun FileNode.compileToFile() {
    compileStaticMembersToFile()
}

fun FileNode.compileStaticMembersToFile() {
    val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)

    classWriter.visit(V1_8, ACC_PUBLIC, getClassName(), null, "java/lang/Object", null)

    properties.forEach {
        it.visitField(classWriter, ACC_PUBLIC or ACC_STATIC)
    }

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
            varName, type.toSandyType().jvmDescription, null, null)
    fieldVisitor.visitEnd()
}

fun FunctionDeclaration.visitMethod(classWriter: ClassWriter, access: Int) {
    val description = "("+
            parameters.joinToString(separator = ",", transform = { it.type.toSandyType().jvmDescription }) +
            ")" + returnType.toSandyType().jvmDescription
    val methodVisitor = classWriter.visitMethod(access, name, description, null, null)
    methodVisitor.visitEnd()
}

fun ByteArray.writeClassToFile(name: String) {
    val fos = FileOutputStream(name + ".class")
    fos.write(this)
    fos.close()
}

interface SandyType {
    val jvmDescription: String
}

fun Type.toSandyType(): SandyType = when(this) {
    is IntType -> IntTypeSandy
    is DoubleType -> DoubleTypeSandy
    is UnitType -> UnitTypeSandy
    else -> throw UnsupportedOperationException()
}

object IntTypeSandy: SandyType {
    override val jvmDescription: String
        get() = "I"
}

object DoubleTypeSandy: SandyType {
    override val jvmDescription: String
        get() = "D"
}

object UnitTypeSandy: SandyType {
    override val jvmDescription: String
        get() = "V"
}