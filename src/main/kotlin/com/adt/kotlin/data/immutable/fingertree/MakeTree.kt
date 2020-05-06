package com.adt.kotlin.data.immutable.fingertree

/**
 * A builder of trees and tree components, supplied with a
 *   particular monoid and measuring function.
 *
 * @author	                    Ken Barclay
 * @since                       September 2019
 */

import com.adt.kotlin.data.immutable.fingertree.digit.*
import com.adt.kotlin.data.immutable.fingertree.node.Node
import com.adt.kotlin.data.immutable.fingertree.node.Node2
import com.adt.kotlin.data.immutable.fingertree.node.Node3


class MakeTree<V, A> internal constructor(val measured: Measured<V, A>) {

    /**
     * Construct an empty tree.
     *
     * @return                  the empty tree
     */
    fun empty(): FingerTree<V, A> = Empty(measured)

    /**
     * Construct a singleton tree.
     *
     * @param a                 a single element for the tree
     * @return                  a tree with the given value as the single element
     */
    fun single(a: A): FingerTree<V, A> = Single(measured, a)

    /**
     * Constructs a deep tree. This structure consists of two digits, of 1 to 4 elements each, on the left and right,
     * with the rest of the tree in the middle.
     *
     * @param prefix            the leftmost elements of the tree
     * @param middle            the subtree, which is a Finger Tree of 2-3 nodes
     * @param suffix            the rightmost elements of the tree
     * @return                  a new finger tree with the given prefix, suffix, and middle
     */
    fun deep(prefix: Digit<V, A>, middle: FingerTree<V, Node<V, A>>, suffix: Digit<V, A>): FingerTree<V, A> =
            DeepF.deep(measured, prefix, middle, suffix)

    /**
     * Constructs a deep tree with the given annotation value.
     *
     * @param v                 the value with which to annotate this tree
     * @param prefix            the leftmost elements of the tree
     * @param middle            the subtree, which is a Finger Tree of 2-3 nodes
     * @param suffix            the rightmost elements of the tree
     * @return                  a new finger tree with the given prefix, suffix, and middle, and annotated with the given value
     *
    fun deep(v: V, prefix: DigitIF<V, A>, middle: FingerTreeIF<V, NodeIF<V, A>>, suffix: DigitIF<V, A>): FingerTreeIF<V, A> =
    Deep(measured, v, prefix, middle, suffix)
     *****/



    /**
     * A digit of one element.
     *
     * @param a                 the element of the digit
     * @return                  a digit of the given element
     */
    fun one(a: A): One<V, A> = One(measured, a)

    /**
     * A digit of two elements.
     *
     * @param a1                the first element of the digit
     * @param a2                the second element of the digit
     * @return                  a digit of the given elements
     */
    fun two(a1: A, a2: A): Two<V, A> = Two(measured, a1, a2)

    /**
     * A digit of three elements.
     *
     * @param a1                the first element of the digit
     * @param a2                the second element of the digit
     * @param a3                the third element of the digit
     * @return                  a digit of the given elements
     */
    fun three(a1: A, a2: A, a3: A): Three<V, A> = Three(measured, a1, a2, a3)

    /**
     * A digit of four elements.
     *
     * @param a1                the first element of the digit
     * @param a2                the second element of the digit
     * @param a3                the third element of the digit
     * @param a4                the fourth element of the digit
     * @return                  a digit of the given elements
     */
    fun four(a1: A, a2: A, a3: A, a4: A): Four<V, A> = Four(measured, a1, a2, a3, a4)



    /**
     * A binary tree node.
     *
     * @param a1                the left child of the node
     * @param a2                the right child of the node
     * @return                  a new binary tree node
     */
    fun node2(a1: A, a2: A): Node2<V, A> = Node2(measured, a1, a2)

    /**
     * A trinary tree node.
     *
     * @param a1                the left child of the node
     * @param a2                the middle child of the node
     * @param a3                the right child of the node
     * @return                  a new trinary tree node
     */
    fun node3(a1: A, a2: A, a3: A): Node3<V, A> = Node3(measured, a1, a2, a3)

}   // MakeTree
