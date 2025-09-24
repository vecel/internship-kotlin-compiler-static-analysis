package com.internship.karandys.controlflowgraph

import org.junit.Test
import org.junit.jupiter.api.Assertions.*

class ControlFlowGraphTest {

    @Test
    fun testAssignNode() {
        val ast = Stmt.Block(
            Stmt.Assign(Expr.Var("x"), Expr.Const(0)),
            Stmt.Assign(Expr.Var("a"), Expr.Const(17))
        )
        val cfg = ControlFlowGraph(ast)
        val head = Node.Assign(
            Expr.Var("x"),
            Expr.Const(0),
            Node.Assign(Expr.Var("a"), Expr.Const(17), Node.Quit)
        )

        cfg.head as Node.Assign

        assertEquals(head, cfg.head)
        assertEquals(head.next, cfg.head.next)
    }

    @Test
    fun testConditionNode() {
        val ast = Stmt.Block(
            Stmt.If(
                Expr.Var("a"),
                Stmt.Assign(Expr.Var("y"), Expr.Const(5)),
                Stmt.Assign(Expr.Var("y"), Expr.Const(10))
            ),
            Stmt.Return(Expr.Var("y"))
        )
        val cfg = ControlFlowGraph(ast)

        val returnNode = Node.Return(Expr.Var("y"))
        val head = Node.Condition(
            Expr.Var("a"),
            Node.Assign(Expr.Var("y"), Expr.Const(5), returnNode),
            Node.Assign(Expr.Var("y"), Expr.Const(10), returnNode)
        )

        cfg.head as Node.Condition

        assertEquals(head, cfg.head)
        assertEquals(head.nextIfTrue, cfg.head.nextIfTrue)
        assertEquals(head.nextIfFalse, cfg.head.nextIfFalse)
        assertNotEquals(head.nextIfFalse, cfg.head.nextIfTrue)
    }

    @Test
    fun testReturnNode() {
        val ast = Stmt.Block(
            Stmt.Assign(Expr.Var("x"), Expr.Const(13)),
            Stmt.Return(Expr.Plus(Expr.Var("x"), Expr.Const(2)))
        )
        val cfg = ControlFlowGraph(ast)

        val head = Node.Assign(
            Expr.Var("x"),
            Expr.Const(13),
            Node.Return(Expr.Plus(Expr.Var("x"), Expr.Const(2)))
        )

        cfg.head as Node.Assign

        assertEquals(head, cfg.head)
        assertEquals(head.next, cfg.head.next)
    }

    @Test
    fun testWhileConditionNode() {
        val ast = Stmt.Block(
            Stmt.While(
                Expr.Var("x"),
                Stmt.Assign(Expr.Var("x"), Expr.Plus(Expr.Var("x"), Expr.Const(1))),
            ),
            Stmt.Return(Expr.Var("x"))
        )
        val cfg = ControlFlowGraph(ast)

        val head = Node.Condition(
            Expr.Var("x"),
            Node.Quit,
            Node.Return(Expr.Var("x")),
            true
        )
        val body = Node.Assign(Expr.Var("x"), Expr.Plus(Expr.Var("x"), Expr.Const(1)), head)
        head.nextIfTrue = body

        cfg.head as Node.Condition

        assertEquals(head, cfg.head)
        assertEquals(body, cfg.head.nextIfTrue)
        assertEquals(head.nextIfFalse, cfg.head.nextIfFalse)
    }

    @Test
    fun testMermaidRepresentation() {
        val ast: Stmt = Stmt.Block(
            Stmt.Assign(Expr.Var("x"), Expr.Const(0)),
            Stmt.If(
                Expr.Var("a"),
                Stmt.Assign(Expr.Var("y"), Expr.Const(5)),
                Stmt.Assign(Expr.Var("y"), Expr.Const(1))
            ),
            Stmt.Return(
                Expr.Plus(
                    Expr.Mul(Expr.Var("x"), Expr.Const(2)),
                    Expr.Var("y")
                )
            )
        )
        val cfg = ControlFlowGraph(ast)

        val mermaid =
            "flowchart TD\n" +
            "   1[x = 0]\n" +
            "   2[If a]\n" +
            "   3[y = 5]\n" +
            "   4[y = 1]\n" +
            "   5[Return x * 2 + y]\n" +
            "   1 --> 2\n" +
            "   2 --> |True|3\n" +
            "   2 --> |False|4\n" +
            "   3 --> 5\n" +
            "   4 --> 5\n"

        assertEquals(mermaid, cfg.toMermaid())
    }

    @Test
    fun testMermaidWithVariablesRepresentation() {
        val ast: Stmt = Stmt.Block(
            Stmt.Assign(Expr.Var("x"), Expr.Const(0)),
            Stmt.If(
                Expr.Const(1),
                Stmt.Assign(Expr.Var("y"), Expr.Const(0)),
                Stmt.Assign(Expr.Var("y"), Expr.Const(1))
            ),
            Stmt.Return(Expr.Var("x"))
        )
        val cfg = ControlFlowGraph(ast)
        cfg.trackVariables()

        val unknownY = "\n   Vars\n   x: 0\n   y: null"
        val knownY0 = "\n   Vars\n   x: 0\n   y: 0"
        val knownY1 = "\n   Vars\n   x: 0\n   y: 1"
        val mermaid =
            "flowchart TD\n" +
            "   1[x = 0$unknownY]\n" +
            "   2[If 1$unknownY]\n" +
            "   3[y = 0$knownY0]\n" +
            "   4[y = 1$knownY1]\n" +
            "   5[Return x$unknownY]\n" +
            "   1 --> 2\n" +
            "   2 --> |True|3\n" +
            "   2 --> |False|4\n" +
            "   3 --> 5\n" +
            "   4 --> 5\n"

        assertEquals(mermaid, cfg.toMermaid())
    }

    @Test
    fun testWithReplacedVars() {
        val expr = Expr.Plus(
            Expr.Var("x"),
            Expr.Mul(Expr.Var("y"), Expr.Const(2))
        )
        val vars = mapOf<Expr.Var, Expr.Const?>(Expr.Var("x") to Expr.Const(2), Expr.Var("y") to Expr.Const(0))
        val replaced = expr.withReplacedVars(vars)

        val expected = Expr.Plus(
            Expr.Const(2),
            Expr.Mul(Expr.Const(0), Expr.Const(2))
        )

        assertEquals(expected, replaced)
    }

    @Test
    fun testWithReplacedVarsWithNull() {
        val expr = Expr.Plus(
            Expr.Var("x"),
            Expr.Mul(Expr.Var("y"), Expr.Const(2))
        )
        val vars = mapOf<Expr.Var, Expr.Const?>(Expr.Var("x") to Expr.Const(2), Expr.Var("y") to null)
        val replaced = expr.withReplacedVars(vars)

        val expected = Expr.Plus(
            Expr.Const(2),
            Expr.Mul(Expr.Var("y"), Expr.Const(2))
        )

        assertEquals(expected, replaced)
    }
}