package com.soutvoid.kompiler

import io.bretty.console.tree.TreePrinter
import org.antlr.v4.gui.TreeViewer
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.Tree
import java.io.*
import javax.swing.JFileChooser
import javax.swing.JFrame

var annotationsList = listOf("JavaFunction" to StringLit::class)
var javaFunctions: MutableList<FunctionDeclaration> = mutableListOf()

fun main(args: Array<String>) {

    //Analyze and register external java functions
    val inputStream = Main::class.java.getResourceAsStream("/JavaFunctions.an")
    val javaTree = getAntlrTree(inputStream).toAst("JavaFunctionsAn")
    javaTree.analyze()


    //Analyze, compile sources
    val tree = getAntlrTree(args[0]).toAst(File(args[0]).name)
    tree.analyze()
    tree.indexate()
    if (!thereWasError) {
        tree.compileToFile("")
        copyCompiledJavaFunctions()
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

fun getAntlrTree(filePath: String): KotlinParser.FileContext {
    val stream = CharStreams.fromFileName(filePath)
    val lexer = KotlinLexer(stream)
    val tokenStream = CommonTokenStream(lexer)
    val parser = KotlinParser(tokenStream)
    parser.addParseListener(KotlinListener())
    return parser.file()
}

fun getAntlrTree(inputStream: InputStream): KotlinParser.FileContext {
    val stream = CharStreams.fromStream(inputStream)
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

fun copyCompiledJavaFunctions() {
    var compiledJavaFuncsIn: InputStream? = null
    var compiledJavaFuncsOut: OutputStream? = null
    try {
        compiledJavaFuncsIn = Main::class.java.getResourceAsStream("/JavaFunctions.class")
        compiledJavaFuncsOut = FileOutputStream("JavaFunctions.class")
        while (compiledJavaFuncsIn.available() > 0)
            compiledJavaFuncsOut.write(compiledJavaFuncsIn.read())
    } finally {
        compiledJavaFuncsIn?.close()
        compiledJavaFuncsOut?.close()
    }
}

