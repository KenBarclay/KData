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
import com.adt.kotlin.data.immutable.fingertree.Split
import com.adt.kotlin.data.immutable.fingertree.digit.Digit
import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.OptionF



object NodeF {

    fun <V, A, B> foldLeft(f: (B) -> (A) -> B): (B) -> (Node<V, A>) -> B {
        return {b: B -> {node: Node<V, A> -> node.foldLeft(b, f)}}
    }

    fun <V, A, B> foldRight(f: (A) -> (B) -> B): (B) -> (Node<V, A>) -> B {
        return {b: B -> {node: Node<V, A> -> node.foldRight(b, f)}}
    }

    fun<V, A, B> liftM(f: (A) -> B, measured: Measured<V, B>): (Node<V, A>) -> Node<V, B> {
        return {node: Node<V, A> -> node.map(f, measured)}
    }

    fun <V, A> nodeToDigit(node: Node<V, A>): Digit<V, A> {
        val mk: MakeTree<V, A> = MakeTree(node.measured)
        return when (node) {
            is Node2 -> mk.two(node.a1, node.a2)
            is Node3 -> mk.three(node.a1, node.a2, node.a3)
        }
    }   // nodeToDigit

    fun <V, A> splitNode(predicate: (V) -> Boolean, v: V, node: Node<V, A>, measured: Measured<V, A>): Split<Option<Digit<V, A>>, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return when (node) {
            is Node2 -> {
                val va: V = measured.sum(v, measured.measure(node.a1))
                if (predicate(va))
                    Split(OptionF.none(), node.a1, OptionF.some<Digit<V, A>>(mk.one(node.a2)))
                else
                    Split(OptionF.some<Digit<V, A>>(mk.one(node.a1)), node.a2, OptionF.none())
            }
            is Node3 -> {
                val va: V = measured.sum(v, measured.measure(node.a1))
                val vab: V = measured.sum(va, measured.measure(node.a2))
                if (predicate(va))
                    Split(OptionF.none(), node.a1, OptionF.some<Digit<V, A>>(mk.two(node.a2, node.a3)))
                else if (predicate(vab))
                    Split(OptionF.some<Digit<V, A>>(mk.one(node.a1)), node.a2, OptionF.some<Digit<V, A>>(mk.one(node.a3)))
                else
                    Split(OptionF.some<Digit<V, A>>(mk.two(node.a1, node.a2)), node.a3, OptionF.none())
            }
        }
    }   // splitNode

}   // NodeF
