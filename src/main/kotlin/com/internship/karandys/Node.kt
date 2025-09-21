package com.internship.karandys

sealed interface Node {
    class Assign(
        val variable: Expr.Var,
        val value: Expr,
        val next: Node
    ): Node
    class Return(val result: Expr): Node
    class Condition(
        val cond: Expr,
        val nextIfTrue: Node,
        val nextIfFalse: Node
    ): Node
    object Quit : Node
}

data class IndexedNode(val id: Int, val node: Node)
