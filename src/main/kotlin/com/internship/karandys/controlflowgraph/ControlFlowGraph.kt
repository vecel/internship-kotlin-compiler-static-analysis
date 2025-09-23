package com.internship.karandys.controlflowgraph

class ControlFlowGraph(ast: Stmt) {
    val head = nodeFromStmt(ast, Node.Quit)

    fun traverse(stopPredicate: (Node) -> Boolean, action: (Node) -> Unit) {
        val queue = ArrayDeque<Node>()
        queue.add(head)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (stopPredicate(node)) continue
            when (node) {
                is Node.Assign -> queue.addLast(node.next)
                is Node.Condition -> {
                    queue.addLast(node.nextIfTrue)
                    queue.addLast(node.nextIfFalse)
                }
                else -> null
            }
            action(node)
        }
    }

    fun mapVariables() {
        val variables = mutableMapOf<Expr.Var, Expr.Const?>()
        val visited = mutableSetOf<Node>()

        // Getting all variables from assign expressions
        traverse(
            stopPredicate = { node -> node in visited }
        ) { node ->
            if (node is Node.Assign) {
                variables[node.variable] = null
            }
            visited.add(node)
        }

        // We have adapted BFS to keep parent's variables in queue
        data class WorkNode(val node: Node, val vars: Map<Expr.Var, Expr.Const?>)
        val queue = ArrayDeque<WorkNode>()
        queue.add(WorkNode(head, variables))
        while (queue.isNotEmpty()) {
            val (node, vars) = queue.removeFirst()
            val before = node.variables.toMap()
            node.update(vars)
            val after = node.variables.toMap()

            if (before == after) continue

            when (node) {
                is Node.Assign -> queue.addLast(WorkNode(node.next, node.variables))
                is Node.Condition -> {
                    queue.addLast(WorkNode(node.nextIfTrue, node.variables))
                    queue.addLast(WorkNode(node.nextIfFalse, node.variables))
                }
                else -> null
            }
        }
    }

//    fun withReplacedVars(): Node {
//        return head.withReplacedVars()
//    }

    fun toMermaid(): String {
        val indexed = getIndexedNodesMap()
        val builder = StringBuilder("flowchart TD\n")

        // Naming nodes on the chart
        indexed.forEach { (node, id) ->
            when (node) {
                is Node.Assign,
                is Node.Condition,
                is Node.Return -> builder.append("   $id[$node]\n")
                else -> null
            }
        }

        // Creating edges for the chart
        val visited = mutableSetOf<Node>()
        traverse(
            stopPredicate = { node -> node in visited }
        ) { node ->
            when (node) {
                is Node.Assign -> {
                    builder.append("   ${indexed[node]} --> ${indexed[node.next]}\n")
                }
                is Node.Condition -> {
                    builder.append("   ${indexed[node]} --> |True|${indexed[node.nextIfTrue]}\n")
                    builder.append("   ${indexed[node]} --> |False|${indexed[node.nextIfFalse]}\n")
                }
                else -> null
            }
            visited.add(node)
        }

        return builder.toString()
    }

    private fun nodeFromStmt(stmt: Stmt, next: Node): Node {
        return when (stmt) {
            is Stmt.Assign -> Node.Assign(stmt.variable, stmt.value, next)
            is Stmt.If -> nodeFromIfStmt(stmt, next)
            is Stmt.Block -> nodeFromBlockStmt(stmt, next)
            is Stmt.While -> nodeFromWhileStmt(stmt, next)
            is Stmt.Return -> Node.Return(stmt.result)
        }
    }

    private fun nodeFromIfStmt(stmt: Stmt.If, next: Node): Node {
        return Node.Condition(
            stmt.cond,
            nodeFromStmt(stmt.thenStmt, next),
            if (stmt.elseStmt != null) nodeFromStmt(stmt.elseStmt, next) else next
        )
    }

    private fun nodeFromBlockStmt(stmt: Stmt.Block, next: Node): Node {
        val stack = mutableListOf(*stmt.stmt)
        var lastNode = next
        while (stack.isNotEmpty()) {
            val topStmt = stack.removeLast()
            val node = nodeFromStmt(topStmt, lastNode)
            lastNode = node
        }
        return lastNode
    }

    private fun nodeFromWhileStmt(stmt: Stmt.While, next: Node): Node {
        val whileNode = Node.Condition(stmt.cond, Node.Quit, next)
        val nextIfTrueNode = nodeFromStmt(stmt.body, whileNode)
        whileNode.nextIfTrue = nextIfTrueNode
        return whileNode
    }

    private fun getIndexedNodesMap(): MutableMap<Node, Int> {
        val indexed = mutableMapOf<Node, Int>()
        var id = 1
        traverse(
            stopPredicate = { node -> node in indexed.keys }
        ) { node ->
            if (node !in indexed.keys) indexed[node] = id++
        }
        return indexed
    }


}