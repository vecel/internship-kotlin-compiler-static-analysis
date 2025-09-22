package com.internship.karandys.controlflowgraph

sealed interface Stmt {
    class Block(vararg val stmt: Stmt) : Stmt {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Block

            return stmt.contentEquals(other.stmt)
        }

        override fun hashCode(): Int {
            return stmt.contentHashCode()
        }
    }

    class Assign(val variable: Expr.Var, val value: Expr) : Stmt {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Assign

            if (variable != other.variable) return false
            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            var result = variable.hashCode()
            result = 31 * result + value.hashCode()
            return result
        }
    }

    class If(
        val cond: Expr,
        val thenStmt: Stmt,
        val elseStmt: Stmt? = null
    ) : Stmt {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as If

            if (cond != other.cond) return false
            if (thenStmt != other.thenStmt) return false
            if (elseStmt != other.elseStmt) return false

            return true
        }

        override fun hashCode(): Int {
            var result = cond.hashCode()
            result = 31 * result + thenStmt.hashCode()
            result = 31 * result + (elseStmt?.hashCode() ?: 0)
            return result
        }
    }

    class While(val cond: Expr, val body: Stmt) : Stmt {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as While

            if (cond != other.cond) return false
            if (body != other.body) return false

            return true
        }

        override fun hashCode(): Int {
            var result = cond.hashCode()
            result = 31 * result + body.hashCode()
            return result
        }
    }

    class Return(val result: Expr) : Stmt {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Return

            return result == other.result
        }

        override fun hashCode(): Int {
            return result.hashCode()
        }
    }
}

sealed interface Expr {
    class Const(val value: Int) : Expr {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Const

            return value == other.value
        }

        override fun hashCode(): Int {
            return value
        }

        override fun toString(): String {
            return value.toString()
        }
    }

    class Var(val name: String) : Expr {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Var

            return name == other.name
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }

        override fun toString(): String {
            return name
        }
    }

    class Eq(val left: Expr, val right: Expr) : Expr
    class NEq(val left: Expr, val right: Expr) : Expr
    class Lt(val left: Expr, val right: Expr) : Expr

    class Plus(val left: Expr, val right: Expr) : Expr {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Plus

            if (left != other.left) return false
            if (right != other.right) return false

            return true
        }

        override fun hashCode(): Int {
            var result = left.hashCode()
            result = 31 * result + right.hashCode()
            return result
        }

        override fun toString(): String {
            return "${left.toString()} + ${right.toString()}"
        }
    }

    class Minus(val left: Expr, val right: Expr) : Expr
    class Mul(val left: Expr, val right: Expr) : Expr {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Mul

            if (left != other.left) return false
            if (right != other.right) return false

            return true
        }

        override fun hashCode(): Int {
            var result = left.hashCode()
            result = 31 * result + right.hashCode()
            return result
        }

        override fun toString(): String {
            return "${left.toString()} * ${right.toString()}"
        }
    }
}