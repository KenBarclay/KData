package com.adt.kotlin.data.immutable.fingertree.node

/**
 * An inner node of the 2-3 tree.
 *
 * @param V                     the monoidal type with which to annotate nodes
 * @param A                     the type of the tree's elements
 *
 * @author	                    Ken Barclay
 * @since                       September 2019
 */

import com.adt.kotlin.data.immutable.fingertree.MakeTree
import com.adt.kotlin.data.immutable.fingertree.Measured
import com.adt.kotlin.data.immutable.fingertree.digit.Digit



sealed class Node<V, A>(val measured: Measured<V, A>, val measure: V) {

    abstract fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B

    abstract fun <B> foldRight(e: B, f: (A) -> (B) -> B): B

    abstract fun toDigit(): Digit<V, A>

    fun <B> map(f: (A) -> B, measured: Measured<V, B>): Node<V, B> {
        val mk: MakeTree<V, B> = MakeTree(measured)
        return when (this) {
            is Node2 -> mk.node2(f(this.a1), f(this.a2))
            is Node3 -> mk.node3(f(this.a1), f(this.a2), f(this.a3))
        }
    }   // map

}   // Node




class Node2<V, A> internal constructor(measured: Measured<V, A>, val a1: A, val a2: A) : Node<V, A>(measured, measured.sum(measured.measure(a1), measured.measure(a2))) {

    override fun toString(): String = "Node2(${measure}, ${a1}, ${a2})"

    override fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B {
        fun g(b: B, a: A): B = f(b)(a)
        return g(g(e, a1), a2)
    }

    override fun <B> foldRight(e: B, f: (A) -> (B) -> B): B {
        fun g(a: A, b: B): B = f(a)(b)
        return g(a1, g(a2, e))
    }

    override fun toDigit(): Digit<V, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return mk.two(a1, a2)
    }

}   // Node2



class Node3<V, A> internal constructor(measured: Measured<V, A>, val a1: A, val a2: A, val a3: A) : Node<V, A>(measured, measured.sum(measured.measure(a1), measured.sum(measured.measure(a2), measured.measure(a3)))) {

    override fun toString(): String = "Node3(${measure}, ${a1}, ${a2}, ${a3})"

    override fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B {
        fun g(b: B, a: A): B = f(b)(a)
        return g(g(g(e, a1), a2), a3)
    }

    override fun <B> foldRight(e: B, f: (A) -> (B) -> B): B {
        fun g(a: A, b: B): B = f(a)(b)
        return g(a1, g(a2, g(a3, e)))
    }

    override fun toDigit(): Digit<V, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return mk.three(a1, a2, a3)
    }

}   // Node3
