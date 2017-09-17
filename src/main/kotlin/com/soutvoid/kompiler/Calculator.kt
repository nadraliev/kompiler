package com.soutvoid.kompiler

class Calculator: CalculatorBaseVisitor<Int>() {

    override fun visitParenthesis(ctx: CalculatorParser.ParenthesisContext?): Int {
        return super.visitParenthesis(ctx)
    }

    override fun visitMulDiv(ctx: CalculatorParser.MulDivContext?): Int {
        return super.visitMulDiv(ctx)
    }

    override fun visitAddSub(ctx: CalculatorParser.AddSubContext?): Int {
        return super.visitAddSub(ctx)
    }

    override fun visitInt(ctx: CalculatorParser.IntContext?): Int {
        return super.visitInt(ctx)
    }
}