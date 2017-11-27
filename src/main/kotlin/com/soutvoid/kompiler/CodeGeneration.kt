package com.soutvoid.kompiler

import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.Ownership
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.implementation.FixedValue
import java.io.File

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
    functions.forEach {
        dType = dType.defineMethod(it.name, it.returnType.getJavaType(), Visibility.PUBLIC, Ownership.STATIC)
                .intercept(FixedValue.value(1))
    }
    dType.make().saveIn(File("generatedBytecode"))
//            .defineMethod("main", Void.TYPE, Visibility.PUBLIC, Ownership.STATIC)
//            .withParameters(arrayOf<String>().javaClass)
//            .intercept(StubMethod.INSTANCE)
//            .defineField("shit", Integer.TYPE, Visibility.PUBLIC)
//            .make()
//            .saveIn(File("generatedBytecode"))

}

fun ClassDeclaration.generate() {
    val dType = ByteBuddy()
            .subclass(Object().`class`)
            .name(name)
            .make()
            .saveIn(File("generatedBytecode"))
}