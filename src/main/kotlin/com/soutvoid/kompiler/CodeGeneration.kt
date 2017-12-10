package com.soutvoid.kompiler

import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.Ownership
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.implementation.FixedValue
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes.*
import java.io.File

fun FileNode.generate(): ByteArray {
    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
    cw.visit(V1_8, ACC_PUBLIC, getClassName(), null, "java/lang/Object", null)
    val mainMethodWriter = cw.visitMethod(ACC_PUBLIC or ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null)
    mainMethodWriter.visitCode()
    // labels are used by ASM to mark points in the code
    val methodStart = Label()
    val methodEnd = Label()
    // with this call we indicate to what point in the method the label methodStart corresponds
    mainMethodWriter.visitLabel(methodStart)

    mainMethodWriter.visitLocalVariable("variable", "I", null, methodStart, methodEnd, 0)
    mainMethodWriter.visitLdcInsn(300)
    mainMethodWriter.visitVarInsn(ISTORE, 0)


    mainMethodWriter.visitLabel(methodEnd)
    // And we had the return instruction
    mainMethodWriter.visitInsn(RETURN)
    mainMethodWriter.visitEnd()
    mainMethodWriter.visitMaxs(1, 2)
    cw.visitEnd()

    return cw.toByteArray()
}