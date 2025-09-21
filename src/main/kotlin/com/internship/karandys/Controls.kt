package com.internship.karandys

sealed interface Stmt {
    class Block(vararg val stmt: Stmt) : Stmt
    class Assign(val variable: Expr.Var, val value: Expr) : Stmt
    class If(
        val cond: Expr,
        val thenStmt: Stmt,
        val elseStmt: Stmt? = null
    ) : Stmt
    class Return(val result: Expr) : Stmt
}

sealed interface Expr {
    class Const(val value: Int) : Expr
    class Var(val name: String) : Expr
    class Eq(val left: Expr, val right: Expr) : Expr
    class NEq(val left: Expr, val right: Expr) : Expr
    class Lt(val left: Expr, val right: Expr) : Expr

    class Plus(val left: Expr, val right: Expr) : Expr
    class Minus(val left: Expr, val right: Expr) : Expr
    class Mul(val left: Expr, val right: Expr) : Expr
}