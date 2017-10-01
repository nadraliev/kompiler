package com.soutvoid.kompiler

interface Node
interface Expression: Node, Statement
interface Statement: Node
interface Type: Node

data class ClassDeclaration(val name: String,
                            val properties: List<VarDeclaration>?,
                            val functions: List<FunctionDeclaration>?): Node
data class FunctionDeclaration(val name: String,
                               val parameters: List<Parameter>?,
                               val returnType: Type?,
                               val statements: List<Statement>?): Node
data class Parameter(val name: String, val type: Type): Node

//Types
object IntType: Type
object DoubleType: Type
object BooleanType: Type
object StringType: Type

//Expressions
interface BinaryExpression: Expression {
    val left: Expression
    val right: Expression
}
data class FunctionCall(val name: String, val parameters: List<VarReference>?): Expression
data class VarReference(val varName: String): Expression
data class EqualsExpression(override val left: Expression, override val right: Expression): BinaryExpression
data class LessExpression(override val left: Expression, override val right: Expression): BinaryExpression
data class GreaterExpression(override val left: Expression, override val right: Expression): BinaryExpression
data class LessOrEqualsExpression(override val left: Expression, override val right: Expression): BinaryExpression
data class GreaterOrEqualsExpression(override val left: Expression, override val right: Expression): BinaryExpression
data class IntLit(val value: String): Expression
data class DoubleLit(val value: String): Expression
data class BooleanLit(val value: String): Expression
data class StringLit(val value: String): Expression


//Statements
data class VarDeclaration(val varName: String, val type: Type, val value: Expression): Statement
data class Assignment(val varName: String, val value: Expression): Statement
data class IfStatement(val expression: Expression, val statements: List<Statement>): Statement