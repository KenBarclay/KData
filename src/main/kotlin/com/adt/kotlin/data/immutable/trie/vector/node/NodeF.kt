package com.adt.kotlin.data.immutable.trie.vector.node

/**
 * The Vector is a persistent version of the classical vector data structure.
 *   The structure supports efficient, non-destructive operations. It is a port
 *   of the Haskell port from Clojure.
 *
 * The algebraic data type declaration is:
 *
 * datatype Node[A] = EmptyNode[A]
 *                  | RootNode[A] of Int * Int * Int * Int * List[A] * Array[Node[A]]
 *                  | InternalNode[A] of Array[Node[A]]
 *                  | DataNode[A] of Array[A]
 *
 * @param A                     the type of elements in the vector
 *
 * @author	                    Ken Barclay
 * @since                       December 2014
 */

import com.adt.kotlin.data.immutable.trie.vector.VectorException

import com.adt.kotlin.data.immutable.trie.vector.node.Node.EmptyNode
import com.adt.kotlin.data.immutable.trie.vector.node.Node.RootNode

import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.ListF

import kotlin.collections.List as KList



object NodeF {

    internal val shiftStep: Int = 5
    internal val chunk: Int = 32          // 2 ^ shiftStep


    /**
     * The empty vector.
     *
     * @return                  an empty vector
     */
    fun <A> empty(): Node<A> = EmptyNode()

    /**
     * Make a vector with one element.
     *
     * @param a                     new element
     * @return                      new vector with that one element
     */
    fun <A> singleton(a: A): Node<A> = RootNode(1, shiftStep, 0, 0, ListF.singleton(a), arrayOf())

    /**
     * Convert a variable-length parameter series into a vector.
     *
     * @param seq                   variable-length parameter series
     * @return                      vector of the given values
     */
    fun <A> fromSequence(vararg seq: A): Node<A> =
            seq.fold(empty()){vec: Node<A>, a: A -> vec.append(a)}

    /**
     * Convert an array into a vector.
     *
     * @param array                 array of values
     * @return                      immutable vector of the given values
     */
    fun <A> from(array: Array<A>): Node<A> =
            array.fold(empty()){vec: Node<A>, a: A -> vec.append(a)}

    /**
     * Convert an array list into a vector.
     *
     * @param xs                    list of values
     * @return                      immutable vector of the given values
     */
    fun <A> from(xs: KList<A>): Node<A> =
            xs.fold(empty()){vec: Node<A>, a: A -> vec.append(a)}

    /**
     * Convert an immutable list into a vector.
     *
     * @param xs                    immutable list of values
     * @return                      immutable vector of the given values
     */
    fun <A> from(xs: List<A>): Node<A> =
            xs.foldLeft(empty<A>()) {vec: Node<A> -> {a: A -> vec.append(a)}}



// ---------- implementation ------------------------------

    fun <A> tailOffset(node: Node<A>): Int {
        return if (node is EmptyNode)
            0
        else if (node is RootNode<A>) {
            val len: Int = node.size
            if (len < NodeF.chunk)
                0
            else
                (len - 1) shr NodeF.shiftStep shl NodeF.shiftStep
        } else
            throw VectorException("tailOffset: internal nodes should not be exposed")
    }   // tailOffset

}   // NodeF
