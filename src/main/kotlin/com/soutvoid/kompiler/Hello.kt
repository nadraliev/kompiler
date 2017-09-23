package com.soutvoid.kompiler

import org.antlr.v4.gui.TreeViewer
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import javax.swing.JFrame

fun main(args: Array<String>) {
    val expression = "" +
            "open fun test()" +
            "{var name = 5;" +
            "var name1;" +
            "name1 = 10;}"
    val stream = CharStreams.fromString(expression)
    val lexer = KotlinLexer(stream)
    val tokenStream = CommonTokenStream(lexer)
    val parser = KotlinParser(tokenStream)
    parser.addParseListener(KotlinListener())
    val tree = parser.functionDeclaration()

    val frame = JFrame("AST for expression: " + expression)
    val treeViewer = TreeViewer(parser.ruleNames.asList(), tree)
    treeViewer.scale = 1.5//scale a little
    frame.add(treeViewer)
    frame.setSize(640, 480)
    frame.isVisible = true
}

