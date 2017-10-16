package com.soutvoid.kompiler

import io.bretty.console.tree.PrintableTreeNode

interface Node : PrintableTreeNode {
    var parent: Node?
    var position: Position
}
interface Expression: Node, Statement
interface Statement: Node

abstract class Type(override var position: Position = Position(0,0), override var parent: Node? = null): Node {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = javaClass.simpleName }

//Class
data class ClassDeclaration(var name: String,
                            var properties: List<VarDeclaration>?,
                            var functions: List<FunctionDeclaration>?,
                            override var position: Position,
                            override var parent: Node? = null): Node {
    
    override fun children(): MutableList<out PrintableTreeNode> = properties.join(functions).map { it as Node }.toMutableList()
    override fun name(): String = "class $name" }



//Function
data class FunctionDeclaration(var name: String,
                               var parameters: List<Parameter>?,
                               var returnType: Type,
                               var statements: List<Statement>?,
                               var returnExpression: Expression?,
                               override var position: Position,
                               override var parent: Node? = null): Node {
    override fun children(): MutableList<out PrintableTreeNode> = listOf<Statement>().join(statements).plusNotNull(returnExpression).map { it as Node }.toMutableList()
    override fun name(): String = "function $name(${parameters.toStringNames()}) : ${returnType.name()}" }



//Parameter
data class Parameter(var name: String,
                     var type: Type,
                     override var position: Position,
                     override var parent: Node? = null): Node {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = "$name : ${type.name()}" }



//Types
object IntType: Type()
object DoubleType: Type()
object BooleanType: Type()
object StringType: Type()
object UnitType: Type()
object RangeType: Type()
data class ArrayType(var type: Type, override var position: Position, override var parent: Node? = null): Type() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = "Array<${type.name()}>" }



//Expressions
interface BinaryExpression: Expression {
    var left: Expression
    var right: Expression
}
interface Comparison: BinaryExpression

//---FunctionCall
data class FunctionCall(var name: String,
                        var parameters: List<Expression>?,
                        override var position: Position,
                        override var parent: Node? = null): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = listOf<Node>().join(parameters).map { it as Node }.toMutableList()
    override fun name(): String = "$name()"}

//---Variable reference
data class VarReference(var varName: String,
                        override var position: Position,
                        override var parent: Node? = null): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = varName }

//---Array initialization
data class ArrayInit(var type: Type,
                     var size: Int,
                     override var position: Position,
                     override var parent: Node? = null): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = "Array<${type.name()}($size)>" }

//---Array access
data class ArrayAccess(var arrayName: String,
                       var index: Expression,
                       override var position: Position,
                       override var parent: Node? = null): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(index)
    override fun name(): String = "$arrayName[]"}

//---Range
data class Range(var start: Int,
                 var endInclusive: Int,
                 override var position: Position,
                 override var parent: Node? = null): Expression {
    override fun children(): MutableList<out PrintableTreeNode>  = mutableListOf()
    override fun name(): String = "$start..$endInclusive" }


//---Binary expressions

//------"=="
data class EqualsExpression(override var left: Expression,
                            override var right: Expression,
                            override var position: Position,
                            override var parent: Node? = null): Comparison {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "==" }

//------"!="
data class NotEqualsExpression(override var left: Expression,
                               override var right: Expression,
                               override var position: Position,
                               override var parent: Node? = null): Comparison {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "!="
}

//------"<"
data class LessExpression(override var left: Expression,
                          override var right: Expression,
                          override var position: Position,
                          override var parent: Node? = null): Comparison {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "<" }

//------">"
data class GreaterExpression(override var left: Expression,
                             override var right: Expression,
                             override var position: Position,
                             override var parent: Node? = null): Comparison {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = ">" }

//------"<="
data class LessOrEqualsExpression(override var left: Expression,
                                  override var right: Expression,
                                  override var position: Position,
                                  override var parent: Node? = null): Comparison {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "<=" }

//------">="
data class GreaterOrEqualsExpression(override var left: Expression,
                                     override var right: Expression,
                                     override var position: Position,
                                     override var parent: Node? = null): Comparison {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = ">=" }

//------"*"
data class Multiplication(override var left: Expression,
                          override var right: Expression,
                          override var position: Position,
                          override var parent: Node? = null): BinaryExpression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "*" }

//------"/"
data class Division(override var left: Expression,
                    override var right: Expression,
                    override var position: Position,
                    override var parent: Node? = null): BinaryExpression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "/" }

//------"+"
data class Addition(override var left: Expression,
                    override var right: Expression,
                    override var position: Position,
                    override var parent: Node? = null): BinaryExpression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "+" }

//------"-"
data class Subtraction(override var left: Expression,
                       override var right: Expression,
                       override var position: Position,
                       override var parent: Node? = null): BinaryExpression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "-" }



//---Literals
//------Integer
data class IntLit(var varue: String, override var position: Position, override var parent: Node? = null): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = varue }

//------Double
data class DoubleLit(var varue: String, override var position: Position, override var parent: Node? = null): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = varue }

//------ Boolean
data class BooleanLit(var varue: String, override var position: Position, override var parent: Node? = null): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = varue }

//------String
data class StringLit(var varue: String, override var position: Position, override var parent: Node? = null): Expression {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = varue }



//Statements
data class VarDeclaration(var varName: String,
                          var type: Type,
                          var value: Expression,
                          override var position: Position,
                          override var parent: Node? = null): Statement  {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(VarReference(varName, position, this), value)
    override fun name(): String = "=" }
data class SimpleAssignment(var varName: String,
                            var value: Expression,
                            override var position: Position,
                            override var parent: Node? = null): Statement {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(VarReference(varName, position, this), value)
    override fun name(): String = "=" }
data class ArrayAssignment(var arrayElement: ArrayAccess,
                           var value: Expression,
                           override var position: Position,
                           override var parent: Node? = null): Statement {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(arrayElement, value)
    override fun name(): String = "=" }
data class IfStatement(var expression: Expression,
                       var statements: List<Statement>,
                       override var position: Position,
                       override var parent: Node? = null): Statement {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(expression).join(statements).map { it as Node }.toMutableList()
    override fun name(): String = "if" }

//---Loops

//------While loop
data class WhileLoop(var factor: Expression,
                     var statements: List<Statement>,
                     override var position: Position,
                     override var parent: Node? = null): Statement {
    override fun children(): MutableList<out PrintableTreeNode> = listOf(factor).join(statements).map { it as Node }.toMutableList()
    override fun name(): String = "while" }

//------For loop
data class ForLoop(var iteratorName: String,
                   var iterable: Expression,
                   var statements: List<Statement>?,
                   override var position: Position,
                   override var parent: Node? = null): Statement {
    override fun children(): MutableList<out PrintableTreeNode> = listOf(iterable).join(statements).map { it as Node }.toMutableList()
    override fun name(): String = "for" }
