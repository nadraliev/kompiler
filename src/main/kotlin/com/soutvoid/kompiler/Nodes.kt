package com.soutvoid.kompiler

import io.bretty.console.tree.PrintableTreeNode

interface Node : PrintableTreeNode {
    var parent: Node?
    var position: Position
}

interface Modificator: Node

interface ContainsIndexes: Node {
    var vars: MutableMap<String, Int>
}

interface DeclaresVar {
    var varName: String
    var type: Type?
}

abstract class Expression : Node, Statement {
    abstract var castTo: Type?
    abstract var type: Type?

    infix fun castTo(type: Type?) {
        castTo = type
    }

    fun typeOrBlank(): String {
        type?.let { return " type: ${it.name()}" }
        return ""
    }

    fun castToOrBlank(): String {
        castTo?.let { return " cast to: ${it.name()}" }
        return ""
    }
}

interface Statement : Node

abstract class Type(override var position: Position = Position(0, 0, 0, 0), override var parent: Node? = null) : Node {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = javaClass.simpleName
}

//FileNode
data class FileNode(var name: String,
                    var classes: List<ClassDeclaration>,
                    var properties: List<VarDeclaration>,
                    var functions: List<FunctionDeclaration>,
                    override var position: Position,
                    override var parent: Node? = null) : Node {
    override fun children(): MutableList<out PrintableTreeNode> = (classes + properties + functions).toMutableList()
    override fun name(): String = name }

//Class
data class ClassDeclaration(var name: String,
                            var properties: List<VarDeclaration>?,
                            var functions: List<FunctionDeclaration>?,
                            override var position: Position,
                            override var parent: Node? = null) : Node {

    override fun children(): MutableList<out PrintableTreeNode> = (properties join functions).map { it as Node }.toMutableList()
    override fun name(): String = "class $name"
}

//Annotation
data class Annotation(var name: String,
                      var parameter: Literal?,
                      override var position: Position,
                      override var parent: Node? = null) : Node {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = "@$name(${parameter?.name() ?: ""})"
}

data class ExternalModificator(override var position: Position,
                               override var parent: Node? = null): Modificator {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = "external" }

//Function
data class FunctionDeclaration(var name: String,
                               var annotation: Annotation? = null,
                               var parameters: List<Parameter>,
                               var returnType: Type,
                               var statements: List<Statement>?,
                               var modificators: List<Modificator> = listOf(),
                               override var position: Position,
                               override var parent: Node? = null,
                               override var vars: MutableMap<String, Int> = mutableMapOf()) : Node, ContainsIndexes {
    override fun children(): MutableList<out PrintableTreeNode> = (listOf<Statement>() plusNotNull annotation
            join parameters join statements).map { it as Node }.toMutableList()
    override fun name(): String = "function $name(${parameters.toStringNames()}) : ${returnType.name()}"
    override fun equals(other: Any?): Boolean {
        other.let {
            return it is FunctionDeclaration
                    && it.name == name
                    && it.parameters == parameters
        }
        return false
    }
}


//Parameter
data class Parameter(override var varName: String,
                     override var type: Type? = null,
                     override var position: Position,
                     override var parent: Node? = null) : Node, DeclaresVar {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = "$varName : ${type?.name()}"
    override fun equals(other: Any?): Boolean {
        other.let {
            return it is Parameter
                    && it.type == type
        }
        return false
    }
}

//Types
object IntType : Type() {
    override fun name(): String = "Integer" }

object DoubleType : Type() {
    override fun name(): String = "Double"}

object BooleanType : Type() {
    override fun name(): String = "Boolean"}

object StringType : Type() {
    override fun name(): String = "String"}

object UnitType : Type() {
    override fun name(): String = "Void"}

abstract class NestedType: Type() {
    abstract var innerType: Type
}

abstract class IterableType: NestedType()

data class RangeType(override var innerType: Type) : IterableType() {
    override fun name(): String = "Range"
    override fun equals(other: Any?): Boolean =
            other?.safecast(this::class)?.innerType == innerType
}

data class ArrayType(override var innerType: Type, override var position: Position, override var parent: Node? = null) : IterableType() {
    override fun name(): String = "Array<${innerType.name()}>"
    override fun equals(other: Any?): Boolean =
            other?.safecast(this::class)?.innerType == innerType
            || (other is RangeType && innerType == other.innerType)
}


//Expressions
abstract class BinaryExpression : Expression() {
    abstract var left: Expression
    abstract var right: Expression
}

abstract class Comparison : BinaryExpression()

//---FunctionCall
data class FunctionCall(var name: String,
                        var parameters: List<Expression>?,
                        override var position: Position,
                        override var parent: Node? = null,
                        override var castTo: Type? = null,
                        override var type: Type? = null) : Expression() {
    override fun children(): MutableList<out PrintableTreeNode> = (listOf<Node>() join parameters).map { it as Node }.toMutableList()
    override fun name(): String = "$name()${typeOrBlank()}${castToOrBlank()}"
}

//---Variable reference
data class VarReference(var varName: String,
                        override var position: Position,
                        override var parent: Node? = null,
                        override var castTo: Type? = null,
                        override var type: Type? = null) : Expression() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = varName + typeOrBlank() + castToOrBlank()
}

//---Array initialization
data class ArrayInit(var innerType: Type,
                     var size: Int,
                     override var position: Position,
                     override var parent: Node? = null,
                     override var castTo: Type? = null,
                     override var type: Type? = null) : Expression() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = "Array<${innerType.name()}($size)>${typeOrBlank()}${castToOrBlank()}"
}

//---Array access
data class ArrayAccess(var arrayName: String,
                       var index: Expression,
                       override var position: Position,
                       override var parent: Node? = null,
                       override var castTo: Type? = null,
                       override var type: Type? = null) : Expression() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(index)
    override fun name(): String = "$arrayName[]${typeOrBlank()}${castToOrBlank()}"
}

//---Range
data class Range(var start: Expression,
                 var endInclusive: Expression,
                 override var position: Position,
                 override var parent: Node? = null,
                 override var castTo: Type? = null,
                 override var type: Type? = null) : Expression() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(start, endInclusive)
    override fun name(): String = "Range ${typeOrBlank()}${castToOrBlank()}"
}

//---Increment
data class Increment(val expression: Expression,
                     override var position: Position,
                     override var parent: Node? = null,
                     override var castTo: Type? = null,
                     override var type: Type? = null): Expression() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(expression)

    override fun name(): String = "++"
}

//---Decrement
data class Decrement(val expression: Expression,
                     override var position: Position,
                     override var parent: Node? = null,
                     override var castTo: Type? = null,
                     override var type: Type? = null): Expression() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(expression)

    override fun name(): String = "--"
}


//---Binary expressions

//------"=="
data class EqualsExpression(override var left: Expression,
                            override var right: Expression,
                            override var position: Position,
                            override var parent: Node? = null,
                            override var castTo: Type? = null,
                            override var type: Type? = null) : Comparison() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "==${typeOrBlank()}${castToOrBlank()}"
}

//------"!="
data class NotEqualsExpression(override var left: Expression,
                               override var right: Expression,
                               override var position: Position,
                               override var parent: Node? = null,
                               override var castTo: Type? = null,
                               override var type: Type? = null) : Comparison() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "!=${typeOrBlank()}${castToOrBlank()}"
}

//------"<"
data class LessExpression(override var left: Expression,
                          override var right: Expression,
                          override var position: Position,
                          override var parent: Node? = null,
                          override var castTo: Type? = null,
                          override var type: Type? = null) : Comparison() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "<${typeOrBlank()}${castToOrBlank()}"
}

//------">"
data class GreaterExpression(override var left: Expression,
                             override var right: Expression,
                             override var position: Position,
                             override var parent: Node? = null,
                             override var castTo: Type? = null,
                             override var type: Type? = null) : Comparison() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = ">${typeOrBlank()}${castToOrBlank()}"
}

//------"<="
data class LessOrEqualsExpression(override var left: Expression,
                                  override var right: Expression,
                                  override var position: Position,
                                  override var parent: Node? = null,
                                  override var castTo: Type? = null,
                                  override var type: Type? = null) : Comparison() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "<=${typeOrBlank()}${castToOrBlank()}"
}

//------">="
data class GreaterOrEqualsExpression(override var left: Expression,
                                     override var right: Expression,
                                     override var position: Position,
                                     override var parent: Node? = null,
                                     override var castTo: Type? = null,
                                     override var type: Type? = null) : Comparison() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = ">=${typeOrBlank()}${castToOrBlank()}"
}

//------"*"
data class Multiplication(override var left: Expression,
                          override var right: Expression,
                          override var position: Position,
                          override var parent: Node? = null,
                          override var castTo: Type? = null,
                          override var type: Type? = null) : BinaryExpression() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "*${typeOrBlank()}${castToOrBlank()}"
}

//------"/"
data class Division(override var left: Expression,
                    override var right: Expression,
                    override var position: Position,
                    override var parent: Node? = null,
                    override var castTo: Type? = null,
                    override var type: Type? = null) : BinaryExpression() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "/${typeOrBlank()}${castToOrBlank()}"
}

//------"+"
data class Addition(override var left: Expression,
                    override var right: Expression,
                    override var position: Position,
                    override var parent: Node? = null,
                    override var castTo: Type? = null,
                    override var type: Type? = null) : BinaryExpression() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "+${typeOrBlank()}${castToOrBlank()}"
}

//------"-"
data class Subtraction(override var left: Expression,
                       override var right: Expression,
                       override var position: Position,
                       override var parent: Node? = null,
                       override var castTo: Type? = null,
                       override var type: Type? = null) : BinaryExpression() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(left, right)
    override fun name(): String = "-${typeOrBlank()}${castToOrBlank()}"
}


//---Literals

interface Literal: Node

//------Integer
data class IntLit(var value: String, override var position: Position, override var parent: Node? = null,
                  override var castTo: Type? = null,
                  override var type: Type? = null) : Expression(), Literal {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = value + typeOrBlank() + castToOrBlank()
}

//------Double
data class DoubleLit(var value: String, override var position: Position, override var parent: Node? = null,
                     override var castTo: Type? = null,
                     override var type: Type? = null) : Expression(), Literal {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = value + typeOrBlank() + castToOrBlank()
}

//------ Boolean
data class BooleanLit(var value: String, override var position: Position, override var parent: Node? = null,
                      override var castTo: Type? = null,
                      override var type: Type? = null) : Expression(), Literal {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = value + typeOrBlank() + castToOrBlank()
}

//------String
data class StringLit(var value: String, override var position: Position, override var parent: Node? = null,
                     override var castTo: Type? = null,
                     override var type: Type? = null) : Expression(), Literal {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = value + typeOrBlank() + castToOrBlank()
}


//Statements
data class VarDeclaration(override var varName: String,
                          override var type: Type? = null,
                          var value: Expression,
                          override var position: Position,
                          override var parent: Node? = null) : Statement, DeclaresVar {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(VarReference(varName, position, this), value)
    override fun name(): String = "="
}

data class SimpleAssignment(var varName: String,
                            var value: Expression,
                            override var position: Position,
                            override var parent: Node? = null) : Statement {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(VarReference(varName, position, this), value)
    override fun name(): String = "="
}

data class ArrayAssignment(var arrayElement: ArrayAccess,
                           var value: Expression,
                           override var position: Position,
                           override var parent: Node? = null) : Statement {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf(arrayElement, value)
    override fun name(): String = "="
}

data class IfStatement(var expression: Expression,
                       var statements: List<Statement>,
                       var elseStatements: List<Statement>,
                       override var position: Position,
                       override var parent: Node? = null,
                       override var vars: MutableMap<String, Int> = mutableMapOf()) : Statement, ContainsIndexes {
    override fun children(): MutableList<out PrintableTreeNode> = (mutableListOf(expression)
            join statements join elseStatements).map { it as Node }.toMutableList()
    override fun name(): String = "if"
}

//---Loops

//------While loop
data class WhileLoop(var factor: Expression,
                     var statements: List<Statement>,
                     override var position: Position,
                     override var parent: Node? = null,
                     override var vars: MutableMap<String, Int> = mutableMapOf()) : Statement, ContainsIndexes {
    override fun children(): MutableList<out PrintableTreeNode> = (listOf(factor) join statements).map { it as Node }.toMutableList()
    override fun name(): String = "while"
}

//------For loop
data class ForLoop(var iterator: ForLoopIterator,
                   var iterable: Expression,
                   var statements: List<Statement>?,
                   override var position: Position,
                   override var parent: Node? = null,
                   override var vars: MutableMap<String, Int> = mutableMapOf()) : Statement, ContainsIndexes {
    override fun children(): MutableList<out PrintableTreeNode> = (listOf(iterator) join listOf(iterable) join statements).map { it as Node }.toMutableList()
    override fun name(): String = "for"
}

data class ForLoopIterator(override var varName: String,
                           override var type: Type? = null,
                           override var position: Position,
                           override var parent: Node? = null,
                           override var castTo: Type? = null): DeclaresVar, Expression() {
    override fun children(): MutableList<out PrintableTreeNode> = mutableListOf()
    override fun name(): String = "$varName: ${type?.name()}"
}


//------Return
data class Return(val expression: Expression?,
                  override var position: Position,
                  override var parent: Node? = null): Statement {
    override fun children(): MutableList<out PrintableTreeNode> = listOf<Node>()
            .plusNotNull(expression).map { it as Node }.toMutableList()
    override fun name(): String = "return"
}
