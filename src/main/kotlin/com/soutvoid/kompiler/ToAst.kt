package com.soutvoid.kompiler

import com.soutvoid.kompiler.KotlinParser.*
import javax.naming.OperationNotSupportedException

fun FileContext.toAst(name: String): FileNode =
        FileNode(
                name,
                classDeclaration()?.map { it.toAst() }?: listOf(),
                propertyDeclaration()?.map { it.toAst() }?: listOf(),
                functionDeclaration()?.map { it.toAst() }?: listOf(),
                considerPosition()
        ).fillInParents()

fun ClassDeclarationContext.toAst(): ClassDeclaration =
        ClassDeclaration(
                SimpleName().text,
                classBody()?.propertyDeclaration()?.map { it.toAst() },
                classBody()?.functionDeclaration()?.map { it.toAst() },
                considerPosition(),
                null).fillInParents()

fun AnnotationContext.toAst(): Annotation =
        Annotation(SimpleName().text,
                literalConstant()?.toAst() as Literal?,
                considerPosition()).fillInParents()

fun FunctionModificatorContext.toAst(): FunctionModificator = when(text) {
    "abstract" -> Abstract(considerPosition()).fillInParents()
    else -> throw OperationNotSupportedException()
}

fun FunctionDeclarationContext.toAst(): FunctionDeclaration =
        FunctionDeclaration(
                SimpleName().text,
                annotation()?.toAst(),
                functionModificator()?.toAst(),
                functionParameters()?.functionParameter()?.map { it.parameter().toAst() } ?: emptyList(),
                type()?.toAst() ?: UnitType,
                body?.statements()?.statement()?.map { it.toAst() },
                body?.expression()?.toAst(),
                considerPosition()).fillInParents()

fun ParameterContext.toAst(): Parameter =
        Parameter(SimpleName().text, type().toAst(), considerPosition()).fillInParents()

fun TypeContext.toAst(): Type = when (this) {
    is IntContext -> IntType
    is DoubleContext -> DoubleType
    is BoolContext -> BooleanType
    is StringContext -> StringType
    is ArrayContext -> ArrayType(type().toAst(), considerPosition()).fillInParents()
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun StatementContext.toAst(): Statement = when (this) {
    is DeclarationStatementContext -> declaration().propertyDeclaration().toAst().fillInParents()
    is AssignmentStatementContext -> assignment().toAst()
    is ExpressionStatementContext -> expression().toAst()
    is IfStatementContext -> ifSt().toAst()
    is LoopStatementContext -> loop().toAst()
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun AssignmentContext.toAst(): Statement = when (this) {
    is SimpleIdentAssignContext -> SimpleAssignment(
            identifier().SimpleName().text,
            expression().toAst(), considerPosition()).fillInParents()
    is ArrayAssignContext -> ArrayAssignment(
            ArrayAccess(arrayAccessExpr().identifier().text,
                    arrayAccessExpr().expression().toAst(),
                    considerPosition()).fillInParents(),
            expression().toAst(),
            considerPosition()
    ).fillInParents()
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun LoopContext.toAst(): Statement = when (this) {
    is WhileStatementContext -> WhileLoop(whileLoop().expression().toAst(),
            whileLoop().block()?.statements()?.statement()?.map { it.toAst() } ?: listOf(whileLoop().statement().toAst()),
            considerPosition()).fillInParents()
    is ForStatementContext -> ForLoop(forLoop().identifier().toAst(),
            forLoop().expression().toAst(),
            forLoop().block()?.statements()?.statement()?.map { it.toAst() } ?: listOf(forLoop().statement().toAst()),
            considerPosition()).fillInParents()
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun PropertyDeclarationContext.toAst(): VarDeclaration = VarDeclaration(
        SimpleName().text,
        type().toAst(),
        expression().toAst(),
        considerPosition()).fillInParents()


fun ExpressionContext.toAst(): Expression = when (this) {
    is IdContext -> identifier().toAst()
    is FuncCallContext -> FunctionCall(
            functionCall().SimpleName().text,
            functionCall().expressions()?.expression()?.map { it.toAst() },
            considerPosition()).fillInParents()
    is LiteralContext -> literalConstant().toAst()
    is ParenExpressionContext -> expression().toAst()
    is BinaryOperationContext -> toAst()
    is ArrayInitContext -> ArrayInit(arrayInitExpr().type().toAst(),
            arrayInitExpr().IntegerLiteral().text.toInt(),
            considerPosition()).fillInParents()
    is ArrayAccessContext -> ArrayAccess(arrayAccessExpr().identifier().text,
            arrayAccessExpr().expression().toAst(),
            considerPosition()).fillInParents()
    is RangeContext -> Range(rangeExpression().IntegerLiteral(0).text.toInt(),
            rangeExpression().IntegerLiteral(1).text.toInt(),
            considerPosition()).fillInParents()
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun IdentifierContext.toAst(): VarReference
        = VarReference(SimpleName().text, considerPosition()).fillInParents()

fun LiteralConstantContext.toAst(): Expression = when (this) {
    is IntLitContext -> IntLit(IntegerLiteral().text, considerPosition()).fillInParents()
    is DoubleLitContext -> DoubleLit(DoubleLiteral().text, considerPosition()).fillInParents()
    is BooleanLitContext -> BooleanLit(BooleanLiteral().text, considerPosition()).fillInParents()
    is StringLitContext -> StringLit(StringLiteral().text.drop(1).dropLast(1), considerPosition()).fillInParents()
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun BinaryOperationContext.toAst(): Expression = when (operator.text) {
    "*" -> Multiplication(left.toAst(), right.toAst(), considerPosition()).fillInParents()
    "/" -> Division(left.toAst(), right.toAst(), considerPosition()).fillInParents()
    "+" -> Addition(left.toAst(), right.toAst(), considerPosition()).fillInParents()
    "-" -> Subtraction(left.toAst(), right.toAst(), considerPosition()).fillInParents()
    "!=" -> NotEqualsExpression(left.toAst(), right.toAst(), considerPosition()).fillInParents()
    "==" -> EqualsExpression(left.toAst(), right.toAst(), considerPosition()).fillInParents()
    "<" -> LessExpression(left.toAst(), right.toAst(), considerPosition()).fillInParents()
    ">" -> GreaterExpression(left.toAst(), right.toAst(), considerPosition()).fillInParents()
    "<=" -> LessOrEqualsExpression(left.toAst(), right.toAst(), considerPosition()).fillInParents()
    ">=" -> GreaterOrEqualsExpression(left.toAst(), right.toAst(), considerPosition()).fillInParents()
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun IfStContext.toAst(): Statement = IfStatement(
        expression().toAst(),
        block()?.statements()?.statement()?.map { it.toAst() } ?: listOf(statement().toAst()),
        considerPosition()).fillInParents()
