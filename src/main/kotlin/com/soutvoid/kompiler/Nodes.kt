package com.soutvoid.kompiler

import io.bretty.console.tree.PrintableTreeNode

interface Node : PrintableTreeNode
interface Expression: Node, Statement
interface Statement: Node

abstract class Type: Node {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = javaClass.simpleName }

//--------------------------Class
data class ClassDeclaration(val name: String,
                            val properties: List<VarDeclaration>?,
                            val functions: List<FunctionDeclaration>?): Node {
    override fun children(): MutableList<out PrintableTreeNode> = properties.join(functions).map { it as Node }.toMutableList()

    override fun name(): String = "class $name" }

//-------------------------Function
data class FunctionDeclaration(val name: String,
                               val parameters: List<Parameter>?,
                               val returnType: Type?,
                               val statements: List<Statement>?): Node {
    override fun children(): MutableList<out PrintableTreeNode> = parameters.join(statements).map { it as Node }.toMutableList()

    override fun name(): String = "function $name : ${returnType?.name()?:"Unit"}" }

//-----------------------Parameter
data class Parameter(val name: String, val type: Type): Node {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()

    override fun name(): String = "$name ${type.name()}" }

//Types
object IntType: Type()
object DoubleType: Type()
object BooleanType: Type()
object StringType: Type()

//Expressions
interface BinaryExpression: Expression {
    val left: Expression
    val right: Expression
}
data class FunctionCall(val name: String, val parameters: List<VarReference>?): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = parameters?.toMutableList()?: mutableListOf<Node>()
    override fun name(): String = name}

data class VarReference(val varName: String): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = varName }

data class EqualsExpression(override val left: Expression, override val right: Expression): BinaryExpression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "==" }
data class LessExpression(override val left: Expression, override val right: Expression): BinaryExpression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "<" }
data class GreaterExpression(override val left: Expression, override val right: Expression): BinaryExpression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = ">" }
data class LessOrEqualsExpression(override val left: Expression, override val right: Expression): BinaryExpression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "<=" }
data class GreaterOrEqualsExpression(override val left: Expression, override val right: Expression): BinaryExpression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = ">=" }

data class IntLit(val value: String): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = value }
data class DoubleLit(val value: String): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = value }
data class BooleanLit(val value: String): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = value }
data class StringLit(val value: String): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = value }


//Statements
data class VarDeclaration(val varName: String, val type: Type, val value: Expression): Statement  {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(VarReference(varName), value)
    override fun name(): String = "=" }
data class Assignment(val varName: String, val value: Expression): Statement {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(VarReference(varName), value)
    override fun name(): String = "=" }
data class IfStatement(val expression: Expression, val statements: List<Statement>): Statement {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(expression).join(statements).map { it as Node }.toMutableList()
    override fun name(): String = "if" }