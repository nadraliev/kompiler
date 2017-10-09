package com.soutvoid.kompiler

import io.bretty.console.tree.PrintableTreeNode

interface Node : PrintableTreeNode
interface Expression: Node, Statement
interface Statement: Node

abstract class Type: Node {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = javaClass.simpleName }

//Class
data class ClassDeclaration(val name: String,
                            val properties: List<VarDeclaration>?,
                            val functions: List<FunctionDeclaration>?,
                            val position: Position): Node {
    override fun children(): MutableList<out PrintableTreeNode> = properties.join(functions).map { it as Node }.toMutableList()

    override fun name(): String = "class $name" }



//Function
data class FunctionDeclaration(val name: String,
                               val parameters: List<Parameter>?,
                               val returnType: Type?,
                               val statements: List<Statement>?,
                               val returnExpression: Expression?,
                               val position: Position): Node {
    override fun children(): MutableList<out PrintableTreeNode> = listOf<Statement>().join(statements).plusNotNull(returnExpression).map { it as Node }.toMutableList()
    override fun name(): String = "function $name(${parameters.toStringNames()}) : ${returnType?.name()?:"Unit"}" }



//Parameter
data class Parameter(val name: String, val type: Type, val position: Position): Node {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = "$name : ${type.name()}" }



//Types
object IntType: Type()
object DoubleType: Type()
object BooleanType: Type()
object StringType: Type()
data class ArrayType(val type: Type, val position: Position): Type() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = "Array<${type.name()}>" }



//Expressions
interface BinaryExpression: Expression {
    val left: Expression
    val right: Expression
}

//---FunctionCall
data class FunctionCall(val name: String, val parameters: List<VarReference>?, val position: Position): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = "$name(${parameters?.map { it.varName }?.joinToString(",")})"}

//---Variable reference
data class VarReference(val varName: String, val position: Position): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = varName }

//---Array initialization
data class ArrayInit(val type: Type, val size: Int, val position: Position): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = "Array<${type.name()}($size)>" }

//---Array access
data class ArrayAccess(val arrayName: String, val index: Int, val position: Position): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = "$arrayName[$index]"}

//---Range
data class Range(val start: Int, val endInclusive: Int, val position: Position): Expression {
    override fun children(): MutableList<out PrintableTreeNode>  = mutableListOf()
    override fun name(): String = "$start..$endInclusive" }


//---Binary expressions

//------"=="
data class EqualsExpression(override val left: Expression, override val right: Expression, val position: Position): BinaryExpression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "==" }

//------"<"
data class LessExpression(override val left: Expression, override val right: Expression, val position: Position): BinaryExpression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "<" }

//------">"
data class GreaterExpression(override val left: Expression, override val right: Expression, val position: Position): BinaryExpression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = ">" }

//------"<="
data class LessOrEqualsExpression(override val left: Expression, override val right: Expression, val position: Position): BinaryExpression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "<=" }

//------">="
data class GreaterOrEqualsExpression(override val left: Expression, override val right: Expression, val position: Position): BinaryExpression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = ">=" }

//------"*"
data class Multiplication(override val left: Expression, override val right: Expression, val position: Position): BinaryExpression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "*" }

//------"/"
data class Division(override val left: Expression, override val right: Expression, val position: Position): BinaryExpression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "/" }

//------"+"
data class Addition(override val left: Expression, override val right: Expression, val position: Position): BinaryExpression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "+" }

//------"-"
data class Substruction(override val left: Expression, override val right: Expression, val position: Position): BinaryExpression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "-" }



//---Literals
//------Integer
data class IntLit(val value: String, val position: Position): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = value }

//------Double
data class DoubleLit(val value: String, val position: Position): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = value }

//------ Boolean
data class BooleanLit(val value: String, val position: Position): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = value }

//------String
data class StringLit(val value: String, val position: Position): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = value }



//---Statements
data class VarDeclaration(val varName: String, val type: Type, val value: Expression, val position: Position): Statement  {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(VarReference(varName, position), value)
    override fun name(): String = "=" }
data class SimpleAssignment(val varName: String, val value: Expression, val position: Position): Statement {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(VarReference(varName, position), value)
    override fun name(): String = "=" }
data class ArrayAssignment(val arrayElement: ArrayAccess, val value: Expression, val position: Position): Statement {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(arrayElement, value)
    override fun name(): String = "=" }
data class IfStatement(val expression: Expression, val statements: List<Statement>, val position: Position): Statement {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(expression).join(statements).map { it as Node }.toMutableList()
    override fun name(): String = "if" }

//---Loops

//------While loop
data class WhileLoop(val factor: Expression, val statements: List<Statement>, val position: Position): Statement {
    override fun children(): MutableList<out PrintableTreeNode> = listOf(factor).join(statements).map { it as Node }.toMutableList()
    override fun name(): String = "while" }

//------For loop
data class ForLoop(val iteratorName: String, val iterable: Expression, val statements: List<Statement>?, val position: Position): Statement {
    override fun children(): MutableList<out PrintableTreeNode> = listOf(iterable).join(statements).map { it as Node }.toMutableList()
    override fun name(): String = "for" }
