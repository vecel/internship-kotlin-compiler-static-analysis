package com.internship.karandys.controlflowgraph

class VariablesMap : Node {
    override val variables = mutableMapOf<Expr.Var, Expr.Const?>()
}

sealed interface Node {

    val variables: MutableMap<Expr.Var, Expr.Const?>

    fun variablesString() : String {
        if (variables.isEmpty()) return ""
        val result = StringBuilder("\n   Vars")
        for (kv in variables) {
            result.append("\n   ${kv.key}: ${kv.value}")
        }
        return result.toString()
    }

    fun update(vars: Map<Expr.Var, Expr.Const?>) {
        if (vars.isEmpty()) return
        vars.forEach { (key, value) ->
            if (key !in variables.keys) {
                variables[key] = value
            }
            if (variables[key] != value) {
                variables[key] = null
            }
        }
    }

    fun withReplacedVars(): Node {
//        TODO("Force to implement it in children")
        return this
    }

    class Assign (
        val variable: Expr.Var,
        val value: Expr,
        val next: Node
    ): Node by VariablesMap() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Assign

            if (variable != other.variable) return false
            if (value != other.value) return false
            if (next != other.next) return false

            return true
        }

        override fun hashCode(): Int {
            var result = variable.hashCode()
            result = 31 * result + value.hashCode()
            result = 31 * result + next.hashCode()
            return result
        }

        override fun toString(): String {
            return "$variable = ${value}${variablesString()}"
        }

        override fun update(vars: Map<Expr.Var, Expr.Const?>) {
            super.update(vars)
            if (value is Expr.Const) {
                variables[variable] = value
            }
        }

        override fun withReplacedVars(): Node {
            return Assign(
                variable,
                value.withReplacedVars(variables),
                next.withReplacedVars()
            )
        }
    }

    class Return(val result: Expr): Node by VariablesMap() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Return

            return result == other.result
        }

        override fun hashCode(): Int {
            return result.hashCode()
        }

        override fun toString(): String {
            return "Return $result${variablesString()}"
        }

        override fun withReplacedVars(): Node {
            return Return(
                result.withReplacedVars(variables)
            )
        }
    }

    class Condition(
        val cond: Expr,
        var nextIfTrue: Node,
        val nextIfFalse: Node
    ): Node by VariablesMap() {
        private fun isLoop(): Boolean {
            return when (nextIfTrue) {
                is Assign -> (nextIfTrue as Assign).next === this
                is Condition -> (nextIfTrue as Condition).nextIfTrue === this || (nextIfTrue as Condition).nextIfFalse === this
                else -> false
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Condition

            if (cond != other.cond) return false
            if (isLoop()) {
                // Checking hash codes is not enough in production, but I will leave it in this minimal working example
                if (!other.isLoop() || nextIfTrue.hashCode() != other.nextIfTrue.hashCode()) return false
            } else {
                if (other.isLoop() || nextIfTrue != other.nextIfTrue) return false
            }
            if (nextIfFalse != other.nextIfFalse) return false

            return true
        }

        override fun hashCode(): Int {
            var result = cond.hashCode()
            result = 31 * result + nextIfFalse.hashCode()
            return result
        }

        override fun toString(): String {
            return "If $cond${variablesString()}"
        }

        override fun withReplacedVars(): Node {
            return Condition(
                cond.withReplacedVars(variables),
                nextIfTrue.withReplacedVars(),
                nextIfFalse.withReplacedVars()
            )
        }
    }

    object Quit : Node by VariablesMap()
}
