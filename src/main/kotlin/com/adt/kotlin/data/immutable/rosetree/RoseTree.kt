package com.adt.kotlin.data.immutable.rosetree

/**
 * A rose tree is a tree structure with a variable and unbounded number of
 *   branches per node. Not every tree is some kind of search tree. Data
 *   structures are often designed to correspond to or capture aspects
 *   of a domain model. A rose tree can be used as a generic representation
 *   for an abstract syntax tree.
 *
 * @author	                    Ken Barclay
 * @since                       October 2018
 */

import com.adt.kotlin.data.immutable.list.*
import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.List.Nil
import com.adt.kotlin.data.immutable.list.List.Cons
import com.adt.kotlin.data.immutable.list.ListF.empty
import com.adt.kotlin.data.immutable.list.ListF.cons

import com.adt.kotlin.hkfp.fp.FunctionF.C2



typealias Forest<A> = List<RoseTree<A>>

class RoseTree<A>(val label: A, val subForest: Forest<A>) {

    constructor(label: A) : this(label, empty())

    /**
     * Are two trees equal?
     *
     * @param other             the other tree
     * @return                  true if both trees are the same; false otherwise
     */
    override fun equals(other: Any?): Boolean {
        fun recEquals(rt1: RoseTree<A>, rt2: RoseTree<A>): Boolean {
            fun recForestEquals(ft1: Forest<A>, ft2: Forest<A>): Boolean {
                return when(ft1) {
                    is Nil -> when(ft2) {
                        is Nil -> true
                        is Cons -> false
                    }
                    is Cons -> when(ft2) {
                        is Nil -> false
                        is Cons -> (ft1.hd.label == ft2.hd.label) && recForestEquals(ft1.tl, ft2.tl)
                    }
                }
            }   // recForestEquals

            return (rt1.label == rt2.label) && recForestEquals(rt1.subForest, rt2.subForest)
        }   // recEquals

        return if (this === other)
            true
        else if (other == null || this::class.java != other::class.java)
            false
        else {
            @Suppress("UNCHECKED_CAST") val otherRoseTree: RoseTree<A> = other as RoseTree<A>
            recEquals(this, otherRoseTree)
        }
    }   // equals

    /**
     * Return the elements of the tree in pre-order.
     *
     * Examples:
     *   RT(1, [RT(2), RT(3), RT(4)]).flatten()  == [1, 2, 3, 4]
     *   RT(1, [RT(2), RT(2), RT(3, [RT(4), RT(5)]), RT(6)]).flatten() == [1, 2, 3, 4, 5, 6]
     */
    fun flatten(): List<A> {
        fun recFlatten(tree: RoseTree<A>, acc: List<A>): List<A> {
            val recFlattenC: (RoseTree<A>) -> (List<A>) -> List<A> = {tr: RoseTree<A> -> {acc: List<A> -> recFlatten(tr, acc)}}
            return cons(tree.label, tree.subForest.foldRight(acc, recFlattenC))
        }   // recFlatten

        return recFlatten(this, empty())
    }   // flatten

    /**
     * foldLeft is a higher-order function that folds a binary function into this
     *   context.
     *
     * @param e                 initial value
     * @param f                 curried binary function:: B -> A -> B
     * @return                  folded result
     */
    fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B = flatten().foldLeft(e, f)

    fun <B> foldLeft(e: B, f: (B, A) -> B): B = this.foldLeft(e, C2(f))

    /**
     * foldRight is a higher-order function that folds a binary function into this
     *   context.
     *
     * @param e                 initial value
     * @param f                 curried binary function:: A -> B -> B
     * @return                  folded result
     */
    fun <B> foldRight(e: B, f: (A) -> (B) -> B): B = flatten().foldRight(e, f)

    fun <B> foldRight(e: B, f: (A, B) -> B): B = flatten().foldRight(e, f)

    /**
     * Fold a tree into a summary value in depth-first order. For each node
     *   in the tree, apply f to the label and the result of applying f to
     *   each subForest.
     */
    fun <B> foldTree(f: (A) -> (List<B>) -> B): B {
        fun recFoldTree(tree: RoseTree<A>): B {
            return f(tree.label)(tree.subForest.map(::recFoldTree))
        }   // recFoldTree

        return recFoldTree(this)
    }   // foldTree

    /**
     * Traverse this tree in tee-order and construct a list of the labels.
     *
     * Examples:
     *   RT(1, [RT(2), RT(3), RT(4)]).labels()  == [1, 2, 3, 4]
     *   RT(1, [RT(2), RT(2), RT(3, [RT(4), RT(5)]), RT(6)]).labels() == [1, 2, 3, 4, 5, 6]
     */
    fun labels(): List<A> {
        fun recLabels(tree: RoseTree<A>, acc: ListBufferIF<A>): ListBufferIF<A> {
            return if (tree.subForest.isEmpty())
                acc.append(tree.label)
            else
                tree.subForest.foldLeft(acc.append(tree.label)){ac -> {rt -> recLabels(rt, ac)}}
        }   // recValues

        return recLabels(this, ListBuffer()).toList()
    }   // labels

    /**
     * Return the last element of this tree.
     *
     * Examples:
     *   RT(1, [RT(2), RT(3), RT(4)]).last()  == 4
     *   RT(1, [RT(2), RT(2), RT(3, [RT(4), RT(5)]), RT(6)]).last() == 6
     */
    fun last(): A = if (subForest.isEmpty()) label else subForest.last().last()

    /**
     * Return the list of nodes at each level of the tree.
     *
     * Examples:
     *   RT(1, [RT(2), RT(3), RT(4)]).levels()  == [[1], [2, 3, 4]]
     *   RT(1, [RT(2), RT(2), RT(3, [RT(4), RT(5)]), RT(6)]).levels() == [[1], [2, 3, 6], [4, 5]]
     */
    fun levels(): List<List<A>> {
        val flatSubForests: (List<RoseTree<A>>) -> List<RoseTree<A>> =
            {lta: List<RoseTree<A>> ->
                lta.bind{ta: RoseTree<A> ->
                    ta.subForest
                }
            }
        val roots: (List<RoseTree<A>>) -> List<A> =
            {lta: List<RoseTree<A>> ->
                lta.map{ta: RoseTree<A> -> ta.label}
            }
        return ListF.iterateWhile(flatSubForests, {list -> !list.isEmpty()}, ListF.singleton(this)).map(roots)
    }   // levels

    /**
     * Apply the function parameter to each item in this rose tree, delivering
     *   a new rose tree. The result rose tree has the same shape as this rose tree.
     */
    fun <B> map(f: (A) -> B): RoseTree<B> {
        return RoseTree(f(label), subForest.map{tree: RoseTree<A> -> tree.map(f)})
    }   // map

    /**
     * Determine the number of elements in the tree.
     */
    fun size(): Int {
        fun recSize(tree: RoseTree<A>): Int {
            return 1 + tree.subForest.foldLeft(0){sum -> {subTree -> sum + recSize(subTree)}}
        }   // recSize

        return recSize(this)
    }   // size

    /**
     * Produce a string representation of a tree.
     *
     * @return                  string representation (using drawTree)
     */
    override fun toString(): String = RoseTreeF.drawTree(this)

    /**
     * Zip this tree with another, using the given function. The resulting tree is the structural
     *   intersection of the two trees.
     *
     * @param rtb               a tree to zip this tree with
     * @param f                 a function with which to zip together the two trees
     * @return                  a new tree of the results of applying the given function over this
     *                              tree and the given tree, position-wise
     */
    fun <B, C> zipWith(rtb: RoseTree<B>, f: (A) -> (B) -> C): RoseTree<C> {
        fun recZipWith(rta: RoseTree<A>, rtb: RoseTree<B>, f: (A) -> (B) -> C): RoseTree<C> {
            val roseZipWith: (RoseTree<A>) -> (RoseTree<B>) -> RoseTree<C> =
                {ra: RoseTree<A> ->
                    {rb: RoseTree<B> ->
                        recZipWith(ra, rb, f)
                    }
                }   // roseZipWith

            return RoseTree(f(rta.label)(rtb.label), rta.subForest.zipWith(rtb.subForest, roseZipWith))
        }   // recZipWith

        return recZipWith(this, rtb, f)
    }   // zipWith

    fun <B, C> zipWith(rtb: RoseTree<B>, f: (A, B) -> C): RoseTree<C> = this.zipWith(rtb, C2(f))

}   // RoseTree
