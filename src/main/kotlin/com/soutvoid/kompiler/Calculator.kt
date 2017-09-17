package com.soutvoid.kompiler

class Calculator: CalculatorBaseVisitor<Int>() {

    override fun visitParenthesis(ctx: CalculatorParser.ParenthesisContext?): Int? {
        return visit(ctx?.expression())
    }

    override fun visitMulDiv(ctx: CalculatorParser.MulDivContext?): Int? {
        return if (ctx?.MUL() != null)
            visit(ctx.left) * visit(ctx.right)
        else
            visit(ctx?.left) / visit(ctx?.right)
    }

    override fun visitAddSub(ctx: CalculatorParser.AddSubContext?): Int? {
        return if (ctx?.ADD() != null)
            visit(ctx.left) + visit(ctx.right)
        else
            visit(ctx?.left) - visit(ctx?.right)
    }

    override fun visitInt(ctx: CalculatorParser.IntContext?): Int? {
        return ctx?.INT()?.text?.toInt()
    }
}