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
                block().statements()?.statement()?.map { it.toAst() })

fun ParameterContext.toAst(): Parameter =
        Parameter(SimpleName().text, type().toAst())

fun TypeContext.toAst(): Type = when(this) {
    is IntContext -> IntType
    is DoubleContext -> DoubleType
    is BoolContext -> BooleanType
    is StringContext -> StringType
    is ArrayContext -> ArrayType(type().toAst())
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun StatementContext.toAst(): Statement = when(this) {
    is DeclarationStatementContext -> declaration().propertyDeclaration().toAst()
    is AssignmentStatementContext -> assignment().toAst()
    is ExpressionStatementContext -> expression().toAst()
    is IfStatementContext -> ifSt().toAst()
    is LoopStatementContext -> loop().toAst()
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun AssignmentContext.toAst(): Statement = when(this) {
    is SimpleIdentAssignContext -> SimpleAssignment(
            identifier().SimpleName().text,
            expression().toAst())
    is ArrayAssignContext -> ArrayAssignment(
            ArrayAccess(arrayAccessExpr().identifier().text, arrayAccessExpr().IntegerLiteral().text.toInt()),
            expression().toAst()
    )
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun LoopContext.toAst(): Statement = when(this) {
    is WhileStatementContext -> WhileLoop(whileLoop().expression().toAst(),
            whileLoop().block()?.statements()?.statement()?.map { it.toAst() }?: listOf(whileLoop().statement().toAst()))
    is ForStatementContext -> ForLoop(forLoop().identifier().text,
            forLoop().expression().toAst(),
            forLoop().block()?.statements()?.statement()?.map { it.toAst() }?: listOf(forLoop().statement().toAst()))
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
    is ArrayInitContext -> ArrayInit(arrayInitExpr().type().toAst(), arrayInitExpr().IntegerLiteral().text.toInt())
    is ArrayAccessContext -> ArrayAccess(arrayAccessExpr().identifier().text, arrayAccessExpr().IntegerLiteral().text.toInt())
    is RangeContext -> Range(rangeExpression().IntegerLiteral(0).text.toInt(), rangeExpression().IntegerLiteral(1).text.toInt())
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun IdentifierContext.toAst(): VarReference
        = VarReference(SimpleName().text)

fun LiteralConstantContext.toAst(): Expression = when(this) {
    is IntLitContext -> IntLit(IntegerLiteral().text)
    is DoubleLitContext -> DoubleLit(DoubleLiteral().text)
    is BooleanLitContext -> BooleanLit(BooleanLiteral().text)
    is StringLitContext -> StringLit(StringLiteral().text)
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun BinaryOperationContext.toAst(): Expression = when(operator.text) {
    "*" -> Multiplication(left.toAst(), right.toAst())
    "/" -> Division(left.toAst(), right.toAst())
    "+" -> Addition(left.toAst(), right.toAst())
    "-" -> Substruction(left.toAst(), right.toAst())
    "==" -> EqualsExpression(left.toAst(), right.toAst())
    "<" -> LessExpression(left.toAst(), right.toAst())
    ">" -> GreaterExpression(left.toAst(), right.toAst())
    "<=" -> LessOrEqualsExpression(left.toAst(), right.toAst())
    ">=" -> GreaterOrEqualsExpression(left.toAst(), right.toAst())
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun IfStContext.toAst(): Statement = IfStatement(
        expression().toAst(),
        block()?.statements()?.statement()?.map { it.toAst() }?: listOf(statement().toAst()))
