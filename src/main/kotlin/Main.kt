import com.internship.karandys.controlflowgraph.Expr
import com.internship.karandys.controlflowgraph.Stmt
import com.internship.karandys.controlflowgraph.ControlFlowGraph

fun main() {
    val program: Stmt = Stmt.Block(
        Stmt.Assign(Expr.Var("x"), Expr.Const(0)),
        Stmt.If(
            Expr.Var("a"),
            Stmt.Assign(Expr.Var("y"), Expr.Const(5)),
            Stmt.Assign(Expr.Var("y"), Expr.Const(1))
        ),
        Stmt.While(
            Expr.Var("b"),
            Stmt.Block(
                Stmt.Assign(Expr.Var("b"), Expr.Plus(
                    Expr.Var("b"),
                    Expr.Const(1)
                )),
                Stmt.Assign(Expr.Var("y"), Expr.Const(-1))
            )
        ),
        Stmt.Return(
            Expr.Plus(
                Expr.Mul(Expr.Var("x"), Expr.Const(2)),
                Expr.Var("y")
            )
        )
    )

    val cfg = ControlFlowGraph(program)
    val mermaid = cfg.toMermaid()
    println(mermaid)
}
