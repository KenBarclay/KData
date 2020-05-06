package com.adt.kotlin.data.immutable.fingertree.view

/**
 * View of the left end of a sequence.
 *
 * @param V                     the monoidal type with which to annotate nodes
 * @param A                     the type of the tree's elements
 *
 * @author	                    Ken Barclay
 * @since                       September 2019
 */

import com.adt.kotlin.data.immutable.fingertree.*
import com.adt.kotlin.data.immutable.fingertree.digit.DigitF
import com.adt.kotlin.data.immutable.fingertree.digit.One
import com.adt.kotlin.data.immutable.fingertree.node.Node
import com.adt.kotlin.data.immutable.fingertree.node.NodeF


sealed class ViewLIF<V, A> {

    /**
     * The empty sequence.
     */
    class EmptyL<V, A> : ViewLIF<V, A>()

    /**
     * Leftmost element and the rest of the sequence.
     */
    class ViewL<V, A> internal constructor(val hd: A, val tl: FingerTree<V, A>) : ViewLIF<V, A>()

}   // ViewLIF



// Functor extension functions:

fun <V, A, B> ViewLIF<V, A>.fmap(measured: Measured<V, B>, f: (A) -> B): ViewLIF<V, B> =
    when (this) {
        is ViewLIF.EmptyL -> ViewLIF.EmptyL()
        is ViewLIF.ViewL -> ViewLIF.ViewL(f(this.hd), this.tl.fmap(measured, f))
    }   // fmap



/**
 * Analyse the left end of a sequence.
 */
fun <V, A> viewL(tree: FingerTree<V, A>): ViewLIF<V, A> {
    val mk: MakeTree<V, A> = MakeTree(tree.measured)
    return when (tree) {
        is Empty -> ViewLIF.EmptyL()
        is Single -> ViewLIF.ViewL(tree.a, mk.empty())
        is Deep -> {
            if (tree.prefix.isOne()) {
                val one: One<V, A> = tree.prefix as One<V, A>
                val viewL: ViewLIF<V, Node<V, A>> = viewL(tree.middle)
                when (viewL) {
                    is ViewLIF.EmptyL -> ViewLIF.ViewL(one.a, DigitF.digitToTree(tree.suffix, tree.measured))
                    is ViewLIF.ViewL -> ViewLIF.ViewL(one.a, mk.deep(NodeF.nodeToDigit(viewL.hd), viewL.tl, tree.suffix))
                }
            } else
                ViewLIF.ViewL(tree.prefix.headDigit(), mk.deep(tree.prefix.tailDigit(), tree.middle, tree.suffix))
        }
    }
}   // viewL
