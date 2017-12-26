package com.soutvoid.kompiler

import io.bretty.console.tree.TreePrinter
import org.antlr.v4.gui.TreeViewer
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.Tree
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JFrame

var annotationsList = listOf("JavaFunction" to StringLit::class)
var javaFunctions: MutableList<FunctionDeclaration> = mutableListOf()

fun main(args: Array<String>) {

    getAntlrTree("javaFunctions.an").toAst("javaFunctionsAn").analyze()

    val tree = getAntlrTree(args[1]).toAst(File(args[1]).name)
    tree.analyze()
    tree.indexate()
    if (!thereWasError) {
        tree.compileToFile(args[2])
    }


//    val filePath = openSource()
//    if (filePath.isEmpty())
//        return
//
//    openJavaFunctionsFile()
//
//    val tree = getAntlrTree(filePath)
//
//    //showSyntaxTree(parser, tree)
//
//    val treeAst = tree.toAst(File(filePath).name)
//    treeAst.analyze()
//    treeAst.indexate()
//    println(TreePrinter.toString(treeAst))
//
//    if (!thereWasError) {
//        treeAst.compileToFile(File(filePath).parent)
//    }


}

fun openSource(): String {
    val jFileChooser = JFileChooser()
    jFileChooser.currentDirectory = File(System.getProperty("user.dir"))
    val sourcesDir = File("src/main/kotlin/com/soutvoid/kompiler")
    if (sourcesDir.exists())
        jFileChooser.currentDirectory = sourcesDir
    val returnVal = jFileChooser.showOpenDialog(null)
    if (returnVal == JFileChooser.APPROVE_OPTION)
        return jFileChooser.selectedFile.absolutePath
    else return ""
}

fun openJavaFunctionsFile() {
    val path = openSource()
    if (path.isNotBlank()) {
        val tree = getAntlrTree(path)
        tree.toAst(File(path).name).analyze()
    }
}

fun getAntlrTree(filePath: String): KotlinParser.FileContext {
    val stream = CharStreams.fromFileName(filePath)
    val lexer = KotlinLexer(stream)
    val tokenStream = CommonTokenStream(lexer)
    val parser = KotlinParser(tokenStream)
    parser.addParseListener(KotlinListener())
    return parser.file()
}

fun showSyntaxTree(parser: KotlinParser, tree: Tree) {
    val frame = JFrame("Syntax tree")
    val treeViewer = TreeViewer(parser.ruleNames.asList(), tree)
    treeViewer.scale = 1.5//scale a little
    frame.add(treeViewer)
    frame.setSize(640, 480)
    frame.isVisible = true
}

