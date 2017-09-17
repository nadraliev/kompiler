package com.soutvoid.kompiler

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

fun main(args: Array<String>) {
    val expression = "(4+2)*4/3"
    val stream = CharStreams.fromString(expression)
    val lexer = CalculatorLexer(stream)
    val tokenStream = CommonTokenStream(lexer)
    val parser = CalculatorParser(tokenStream)
    val tree = parser.expression()
    val result = Calculator().visit(tree)
    println(result)
}

