package com.soutvoid.kompiler

import io.bretty.console.tree.TreePrinter
import org.antlr.v4.gui.TreeViewer
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.Tree
import javax.swing.JFileChooser
import javax.swing.JFrame

fun main(args: Array<String>) {

    val filePath = openSource()
    if (filePath.isEmpty())
        return

    val stream = CharStreams.fromFileName(filePath)
    val lexer = KotlinLexer(stream)
    val tokenStream = CommonTokenStream(lexer)
    val parser = KotlinParser(tokenStream)
    parser.addParseListener(KotlinListener())
    val tree = parser.classDeclaration()

    showSyntaxTree(parser, tree)

    val treeAst = tree.toAst()
    println(TreePrinter.toString(treeAst))
}

fun openSource(): String {
    val jFileChooser = JFileChooser()
    val returnVal = jFileChooser.showOpenDialog(null)
    if (returnVal == JFileChooser.APPROVE_OPTION)
        return jFileChooser.selectedFile.absolutePath
    else return ""
}

fun showSyntaxTree(parser: KotlinParser, tree: Tree) {
    val frame = JFrame("Syntax tree")
    val treeViewer = TreeViewer(parser.ruleNames.asList(), tree)
    treeViewer.scale = 1.5//scale a little
    frame.add(treeViewer)
    frame.setSize(640, 480)
    frame.isVisible = true
}

