package com.soutvoid.kompiler

import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.ModifierContributor
import net.bytebuddy.description.modifier.Ownership
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.dynamic.scaffold.InstrumentedType
import net.bytebuddy.implementation.FixedValue
import net.bytebuddy.implementation.Implementation
import net.bytebuddy.implementation.StubMethod
import net.bytebuddy.implementation.bytecode.ByteCodeAppender
import org.omg.CORBA.Object
import java.io.File
import java.lang.reflect.Array
import java.lang.reflect.Field

fun FileNode.generate() {
    classes.forEach { it.generate() }
    if (!properties.isEmpty() || !functions.isEmpty()) {
        val className = this.name.replace("an", "An").replace(".", "")
        generateStaticClass(className, properties, functions)
    }
}

fun generateStaticClass(name: String, properties: List<VarDeclaration>, functions: List<FunctionDeclaration>) {
    var dType = ByteBuddy()
            .subclass(Object().`class`)
            .name(name)
            .defineMethod("main", Void.TYPE, Visibility.PUBLIC, Ownership.STATIC)
            .withParameters(arrayOf<String>().javaClass)
            .intercept(StubMethod.INSTANCE)
            .make()
            .saveIn(File("generatedBytecode"))

}

fun ClassDeclaration.generate() {
    val dType = ByteBuddy()
            .subclass(Object().`class`)
            .name(name)
            .make()
            .saveIn(File("generatedBytecode"))
}