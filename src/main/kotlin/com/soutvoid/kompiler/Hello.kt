package com.soutvoid.kompiler

import io.bretty.console.tree.TreePrinter
import org.antlr.v4.gui.TreeViewer
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.RuleContext
import org.antlr.v4.runtime.tree.Tree
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import javax.swing.JFileChooser
import javax.swing.JFrame

var annotationsList = listOf("JavaFunction" to StringLit::class)
var javaFunctions: MutableList<FunctionDeclaration> = mutableListOf()

fun main(args: Array<String>) {

    val filePath = openSource()
    if (filePath.isEmpty())
        return

    searchForJavaFunctions(filePath)

    val tree = getAntlrTree(filePath)

    //showSyntaxTree(parser, tree)

    val treeAst = tree.toAst(File(filePath).name)
    treeAst.analyze()
    treeAst.indexate()
    println(TreePrinter.toString(treeAst))

    if (!thereWasError) {
        treeAst.compileToFile()
    }

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

fun searchForJavaFunctions(sourcePath: String) {
    val sourceFile = File(sourcePath)
    val sourceDir = File(sourceFile.parent)
    val anFiles = sourceDir.listFiles { file, s -> s.endsWith(".an", false) }
    val trees = anFiles.map { getAntlrTree(it.absolutePath).toAst(it.name) }
    trees.forEach{ it.analyze() }
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

