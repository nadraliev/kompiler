package com.soutvoid.kompiler

import org.antlr.v4.gui.TreeViewer
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import javax.swing.JFrame
import javax.swing.JPanel
import java.util.Arrays
import java.util.Arrays.asList



fun main(args: Array<String>) {
    val expression = "456+98/2*(3-1)"
    val stream = CharStreams.fromString(expression)
    val lexer = CalculatorLexer(stream)
    val tokenStream = CommonTokenStream(lexer)
    val parser = CalculatorParser(tokenStream)
    val tree = parser.expression()
    val result = Calculator().visit(tree)
    println(result)

//    val frame = JFrame("AST for expression: " + expression)
//    val treeViewer = TreeViewer(parser.ruleNames.asList(), tree)
//    treeViewer.scale = 1.5//scale a little
//    frame.add(treeViewer)
//    frame.setSize(640, 480)
//    frame.isVisible = true
}

