package com.adt.kotlin.data.immutable.fingertree
/**
 * Provides 2-3 finger trees, a functional representation of persistent
 *   sequences supporting access to the ends in amortized O(1) time.
 *   Concatenation and splitting time is O(log n) in the size of the
 *   smaller piece.
 *
 * A general purpose data structure that can serve as a sequence,
 *   priority queue, search tree, priority search queue and more.
 *
 * This class serves as a data structure construction kit, rather
 *   than a data structure in its own right. By supplying a monoid,
 *   a measurement function, insertion, deletion, and so forth,
 *   any purely functional data structure can be emulated.
 *
 * Based on "Finger trees: a simple general-purpose data structure",
 *   by Ralf Hinze and Ross Paterson.
 *
 * @author	                    Ken Barclay
 * @since                       September 2019
 */

import com.adt.kotlin.data.immutable.option.*
import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.fingertree.digit.*
import com.adt.kotlin.data.immutable.fingertree.node.*
import com.adt.kotlin.data.immutable.fingertree.view.*
import com.adt.kotlin.hkfp.typeclass.Monoid


object FingerTreeF {

    /**
     * Factory constructor function.
     */
    fun <V, A> measured(monoid: Monoid<V>, measuring: (A) -> V): Measured<V, A> =
        Measured(monoid, measuring)

    /**
     * The empty sequence.
     *
     * @param measured              measuring instrument
     * @return                      an empty finger tree
     */
    fun <V, A> empty(measured: Measured<V, A>): FingerTree<V, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return mk.empty()
    }

    /**
     * A singleton sequence.
     *
     * @param measured              measuring instrument
     * @param a                     associated value
     * @return                      a singleton finger tree
     */
    fun <V, A> singleton(measured: Measured<V, A>, a: A): FingerTree<V, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return mk.single(a)
    }

    /**
     * Factory functions to create a tree from a list of none or more elements.
     */
    fun <V, A> of(measured: Measured<V, A>): FingerTree<V, A> =
        empty(measured)

    fun <V, A> of(measured: Measured<V, A>, a1: A): FingerTree<V, A> =
        singleton(measured, a1)

    fun <V, A> of(measured: Measured<V, A>, a1: A, a2: A): FingerTree<V, A> =
        singleton(measured, a1).addRight(a2)

    fun <V, A> of(measured: Measured<V, A>, a1: A, a2: A, a3: A): FingerTree<V, A> =
        singleton(measured, a1).addRight(a2).addRight(a3)

    fun <V, A> of(measured: Measured<V, A>, a1: A, a2: A, a3: A, a4: A): FingerTree<V, A> =
        singleton(measured, a1).addRight(a2).addRight(a3).addRight(a4)

    fun <V, A> of(measured: Measured<V, A>, a1: A, a2: A, a3: A, a4: A, a5: A): FingerTree<V, A> =
        singleton(measured, a1).addRight(a2).addRight(a3).addRight(a4).addRight(a5)

    fun <V, A> of(measured: Measured<V, A>, vararg seq: A): FingerTree<V, A> = from(measured, *seq)

    /**
     * Create a tree from a finite list of elements.
     *
     * @param list                  the sequence list
     * @param measured              measuring instrument
     * @return                      populated finger tree
     */
    fun <V, A> from(measured: Measured<V, A>, vararg seq: A): FingerTree<V, A> =
            seq.foldRight(Empty(measured)){a: A, tree: FingerTree<V, A> -> tree.addLeft(a)}
    /**
     * Create a tree from a finite list of elements.
     *
     * @param list                  the sequence list
     * @param measured              measuring instrument
     * @return                      populated finger tree
     */
    fun <V, A> from(measured: Measured<V, A>, list: List<A>): FingerTree<V, A> =
            list.foldRight(Empty(measured)){a: A, tree: FingerTree<V, A> -> tree.addLeft(a)}

    fun <V, A> splitTree(predicate: (V) -> Boolean, v: V, tree: FingerTree<V, A>, measured: Measured<V, A>): Split<FingerTree<V, A>, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return when (tree) {
            is Empty -> throw FingerTreeException("splitTree: empty tree")
            is Single -> {
                val et: FingerTree<V, A> = mk.empty()
                Split(et, tree.a, et)
            }
            is Deep -> {
                val vpr: V = measured.sum(v, tree.prefix.measure())
                val vm: V = if (tree.middle.isEmpty()) vpr else measured.sum(vpr, tree.middle.measure())
                if (predicate(vpr)) {
                    val split: Split<Option<Digit<V, A>>, A> = DigitF.splitDigit(predicate, v, tree.prefix, measured)
                    val digitToTreeC: (Digit<V, A>) -> FingerTree<V, A> = {digit: Digit<V, A> -> DigitF.digitToTree(digit, measured)}
                    val tr: FingerTree<V, A> = OptionF.option(split.t1, mk.empty(), digitToTreeC)
                    val a: A = split.a
                    val deepL: FingerTree<V, A> = deepL(split.t2, tree.middle, tree.suffix, measured)
                    Split(tr, a, deepL)
                } else if (predicate(vm)) {
                    val splitTree: Split<FingerTree<V, Node<V, A>>, Node<V, A>> = splitTree(predicate, vpr, tree.middle, measured.nodeMeasured())
                    val splitNode: Split<Option<Digit<V, A>>, A> = NodeF.splitNode(predicate, if (splitTree.t1.isEmpty()) vpr else measured.sum(vpr, splitTree.t1.measure()), splitTree.a, measured)
                    val deepR: FingerTree<V, A> = deepR(tree.prefix, splitTree.t1, splitNode.t1, measured)
                    val deepL: FingerTree<V, A> = deepL(splitNode.t2, splitTree.t2, tree.suffix, measured)
                    val a: A = splitNode.a
                    Split(deepR, a, deepL)
                } else {
                    val split: Split<Option<Digit<V, A>>, A> = DigitF.splitDigit(predicate, vm, tree.suffix, measured)
                    val deepR: FingerTree<V, A> = deepR(tree.prefix, tree.middle, split.t1, measured)
                    val digitToTreeC: (Digit<V, A>) -> FingerTree<V, A> = {digit: Digit<V, A> -> DigitF.digitToTree(digit, measured)}
                    val tr: FingerTree<V, A> = OptionF.option(split.t2, mk.empty(), digitToTreeC)
                    val a: A = split.a
                    Split(deepR, a, tr)
                }
            }
        }
    }   // splitTree

    fun <V, A> deepL(op: Option<Digit<V, A>>, tree: FingerTree<V, Node<V, A>>, suffix: Digit<V, A>, measured: Measured<V, A>): FingerTree<V, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return if (op.isEmpty()) {
            val viewL: ViewLIF<V, Node<V, A>> = viewL(tree)
            when (viewL) {
                is ViewLIF.EmptyL -> DigitF.digitToTree(suffix, measured)
                is ViewLIF.ViewL -> mk.deep(NodeF.nodeToDigit(viewL.hd), viewL.tl, suffix)
            }
        } else {
            val prefix: Digit<V, A> = op.get()
            mk.deep(prefix, tree, suffix)
        }
    }   // deepL

    fun <V, A> deepR(prefix: Digit<V, A>, tree: FingerTree<V, Node<V, A>>, op: Option<Digit<V, A>>, measured: Measured<V, A>): FingerTree<V, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return if (op.isEmpty()) {
            val viewR: ViewRIF<V, Node<V, A>> = viewR(tree)
            when (viewR) {
                is ViewRIF.EmptyR -> DigitF.digitToTree(prefix, measured)
                is ViewRIF.ViewR -> mk.deep(prefix, viewR.hd, NodeF.nodeToDigit(viewR.tl))
            }
        } else {
            val suffix: Digit<V, A> = op.get()
            mk.deep(prefix, tree, suffix)
        }
    }   // deepR

}   // FingerTreeF
