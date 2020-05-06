package com.adt.kotlin.data.immutable.fingertree.view

/**
 * View of the right end of a sequence.
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


sealed class ViewRIF<V, A> {

    /**
     * The empty sequence.
     */
    class EmptyR<V, A> : ViewRIF<V, A>()

    /**
     * The sequence minus the rightmost element and the rightmost element.
     */
    class ViewR<V, A>(val hd: FingerTree<V, A>, val tl: A) : ViewRIF<V, A>()

}   // ViewRIF



// Functor extension functions:

fun <V, A, B> ViewRIF<V, A>.fmap(measured: Measured<V, B>, f: (A) -> B): ViewRIF<V, B> =
    when (this) {
        is ViewRIF.EmptyR -> ViewRIF.EmptyR()
        is ViewRIF.ViewR -> ViewRIF.ViewR(this.hd.fmap(measured, f), f(this.tl))
    }   // fmap



/**
 * Analyse the right end of a sequence.
 */
fun <V, A> viewR(tree: FingerTree<V, A>): ViewRIF<V, A> {
    val mk: MakeTree<V, A> = MakeTree(tree.measured)
    return when (tree) {
        is Empty -> ViewRIF.EmptyR()
        is Single -> ViewRIF.ViewR(mk.empty(), tree.a)
        is Deep -> {
            if (tree.suffix.isOne()) {
                val one: One<V, A> = tree.suffix as One<V, A>
                val viewR: ViewRIF<V, Node<V, A>> = viewR(tree.middle)
                when (viewR) {
                    is ViewRIF.EmptyR -> ViewRIF.ViewR(DigitF.digitToTree(tree.prefix, tree.measured), one.a)
                    is ViewRIF.ViewR -> ViewRIF.ViewR(mk.deep(tree.prefix, viewR.hd, NodeF.nodeToDigit(viewR.tl)), one.a)
                }
            } else
                ViewRIF.ViewR(mk.deep(tree.prefix, tree.middle, tree.suffix.tailDigit()), tree.suffix.headDigit())
        }
    }
}   // viewR
