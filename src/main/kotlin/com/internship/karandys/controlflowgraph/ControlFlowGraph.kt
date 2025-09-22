package com.internship.karandys.controlflowgraph

class ControlFlowGraph(ast: Stmt) {
    val head = nodeFromStmt(ast, Node.Quit)

    fun traverse(action: (Node) -> Unit) {
        val visited = mutableSetOf<Node>()
        val queue = ArrayDeque<Node>()
        queue.add(head)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node in visited) continue
            visited.add(node)
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

    fun toMermaid(): String {
        val indexed = getIndexedNodesMap()
        val builder = StringBuilder("flowchart TD\n")

        // Naming nodes on the chart
        indexed.forEach { (node, id) ->
            when (node) {
                is Node.Assign -> builder.append("   $id[${node.variable} = ${node.value}]\n")
                is Node.Condition -> builder.append("   $id[if ${node.cond}]\n")
                is Node.Return -> builder.append("   $id[return ${node.result}]\n")
                else -> null
            }
        }

        // Creating edges for the chart
        traverse { node ->
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
        traverse { node ->
            if (node !in indexed.keys) indexed[node] = id++
        }
        return indexed
    }


}