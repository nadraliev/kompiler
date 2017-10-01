package com.soutvoid.kompiler

import io.bretty.console.tree.TreePrinter
import org.antlr.v4.gui.TreeViewer
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import javax.swing.JFrame

fun main(args: Array<String>) {
    val expression =
            "class Test{\n" +
            "fun test() {\n" +
                    "var foo: Int = 5\n" +
                    "foo =7\n" +
                    "if (1 < 0) {\n" +
                    "var wrong: Boolean = true\n" +
                    "}\n" +
                    "}\n" +
                    "\n" +
                    "fun test2(param: Double) {\n" +
                    "var boo: Boolean = 5 < 3\n" +
                    "if (boo)\n" +
                    "var right: Int = 3\n" +
                    "}\n" +
                    "}\n"

    val stream = CharStreams.fromString(expression)
    val lexer = KotlinLexer(stream)
    val tokenStream = CommonTokenStream(lexer)
    val parser = KotlinParser(tokenStream)
    parser.addParseListener(KotlinListener())
    val tree = parser.classDeclaration()

    val frame = JFrame("AST for expression: " + expression)
    val treeViewer = TreeViewer(parser.ruleNames.asList(), tree)
    treeViewer.scale = 1.5//scale a little
    frame.add(treeViewer)
    frame.setSize(640, 480)
    frame.isVisible = true

    val treeAst = tree.toAst()
    println(TreePrinter.toString(treeAst))
}

