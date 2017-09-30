package com.soutvoid.kompiler

import com.soutvoid.kompiler.KotlinParser.*

fun ClassDeclarationContext.toAst(): ClassDeclaration =
        ClassDeclaration(
                SimpleName().text,
                classBody()?.propertyDeclaration()?.map { it.toAst() },
                classBody()?.functionDeclaration()?.map { it.toAst() })

fun FunctionDeclarationContext.toAst(): FunctionDeclaration =
        FunctionDeclaration(
                SimpleName().text,
                functionParameters()?.functionParameter()?.map { it.parameter().toAst() },
                type()?.toAst(),
                functionBody().statements()?.statement()?.map { it.toAst() })

fun ParameterContext.toAst(): Parameter =
        Parameter(SimpleName().text, type().toAst())

fun TypeContext.toAst(): Type = when(text) {
    "Int" -> IntType
    "Double" -> DoubleType
    "Boolean" -> BooleanType
    "String" -> StringType
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun StatementContext.toAst(): Statement = when(this) {
    is DeclarationContext -> propertyDeclaration().toAst()
    is AssignmentContext -> Assignment(
            assignment().identifier().SimpleName().text,
            assignment().expression().toAst())
    is ExpressionContext -> expression().toAst()
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun PropertyDeclarationContext.toAst(): VarDeclaration = VarDeclaration(
        SimpleName().text,
        type().toAst(),
        expression().toAst())


fun ExpressionContext.toAst(): Expression = when(this) {
    is IdContext -> identifier().toAst()
    is FuncCallContext -> FunctionCall(
            functionCall().SimpleName().text,
            functionCall().identifiers()?.identifier()?.map { it.toAst() })
    is LiteralContext -> literalConstant().toAst()
    is ParenExpressionContext -> expression().toAst()
    is BinaryOperationContext -> toAst()
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun IdentifierContext.toAst(): VarReference
        = VarReference(SimpleName().text)

fun LiteralConstantContext.toAst(): Expression = when(this) {
    is IntLitContext -> IntLit(intLit().text)
    is DoubleLitContext -> DoubleLit(doubleLit().text)
    is BooleanLitContext -> BooleanLit(booleanLit().text)
    is StringLitContext -> StringLit(stringLit().text)
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun BinaryOperationContext.toAst(): Expression = when(operator.text) {
    "==" -> EqualsExpression(left.toAst(), right.toAst())
    "<" -> LessExpression(left.toAst(), right.toAst())
    ">" -> GreaterExpression(left.toAst(), right.toAst())
    "<=" -> LessOrEqualsExpression(left.toAst(), right.toAst())
    ">=" -> GreaterOrEqualsExpression(left.toAst(), right.toAst())
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}
