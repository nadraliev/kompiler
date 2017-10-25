package com.soutvoid.kompiler

import net.bytebuddy.ByteBuddy
import net.bytebuddy.dynamic.DynamicType
import org.omg.CORBA.Object
import java.io.File

fun ClassDeclaration.generate() {
    val dType = ByteBuddy()
            .subclass(Object().`class`)
            .name(name)
            .make()
            .saveIn(File("generatedBytecode"))
}