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

import com.adt.kotlin.data.immutable.list.append
import com.adt.kotlin.data.immutable.list.concatenate
import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.List.Nil
import com.adt.kotlin.data.immutable.list.List.Cons
import com.adt.kotlin.data.immutable.list.ListF

import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3


object RoseTreeF {

    /**
     * Make a rose tree with one element.
     *
     * Examples:
     *   singleton(5) = [5]
     *
     * @param a                     new element
     * @return                      new RoseTree with that one element
     */
    fun <A> singleton(a: A): RoseTree<A> = RoseTree(a, ListF.empty())



    fun <A> of(a1: A): RoseTree<A> = RoseTree(a1, ListF.empty())

    fun <A> of(a1: A, a2: A): RoseTree<A> = RoseTree(a1, ListF.of(RoseTree(a2, ListF.empty())))

    fun <A> of(a1: A, a2: A, a3: A): RoseTree<A> =
        RoseTree(a1, ListF.of(RoseTree(a2, ListF.empty()), RoseTree(a3, ListF.empty())))

    fun <A> of(a1: A, a2: A, a3: A, a4: A): RoseTree<A> =
        RoseTree(a1, ListF.of(RoseTree(a2, ListF.empty()), RoseTree(a3, ListF.empty()), RoseTree(a4, ListF.empty())))

    fun <A> of(a1: A, a2: A, a3: A, a4: A, a5: A): RoseTree<A> =
        RoseTree(a1, ListF.of(RoseTree(a2, ListF.empty()), RoseTree(a3, ListF.empty()), RoseTree(a4, ListF.empty()), RoseTree(a5, ListF.empty())))

    /**
     * Build a tree from a seed value in breadth-first order.
     */
    internal fun <A, B> unfoldTree(f: (B) -> Pair<A, List<B>>, b: B): RoseTree<A> {
        val (a, bs) = f(b)
        return RoseTree(a, unfoldForest(f, bs))
    }   // unfoldTree

    /**
     * Build a forest from a list of seed values in breadth-first order.
     */
    internal fun <A, B> unfoldForest(f: (B) -> Pair<A, List<B>>, bs: List<B>): Forest<A> {
        val unfoldTreeC: ((B) -> Pair<A, List<B>>) -> (B) -> RoseTree<A> = {g: (B) -> Pair<A, List<B>> -> {b: B -> unfoldTree(g, b)}}
        return bs.map(unfoldTreeC(f))
    }   // unfoldForest



    // Functor extension functions:

    /**
     * Lift a function into the RoseTree context.
     */
    fun <A, B> lift(f: (A) -> B): (RoseTree<A>) -> RoseTree<B> =
        {rta: RoseTree<A> -> rta.fmap(f)}



    // Applicative extension functions:

    /**
     * Lift a function to actions.
     */
    fun <A, B> liftA(f: (A) -> B): (RoseTree<A>) -> RoseTree<B> =
        {rta: RoseTree<A> ->
            rta.ap(singleton(f))
        }   // liftA

    /**
     * Lift a binary function to actions.
     *
     * Some functors support an implementation of lift2 that is more efficient than the
     *   default one. In particular, if fmap is an expensive operation, it is likely
     *   better to use liftA2 than to fmap over the structure and then use ap.
     */
    fun <A, B, C> liftA2(f: (A) -> (B) -> C): (RoseTree<A>) -> (RoseTree<B>) -> RoseTree<C> =
        {rta: RoseTree<A> ->
            {rtb: RoseTree<B> ->
                rtb.ap(rta.fmap(f))
            }
        }   // liftA2

    fun <A, B, C> liftA2(f: (A, B) -> C): (RoseTree<A>) -> (RoseTree<B>) -> RoseTree<C> =
        liftA2(C2(f))

    /**
     * Lift a ternary function to actions.
     */
    fun <A, B, C, D> liftA3(f: (A) -> (B) -> (C) -> D): (RoseTree<A>) -> (RoseTree<B>) -> (RoseTree<C>) -> RoseTree<D> =
        {rta: RoseTree<A> ->
            {rtb: RoseTree<B> ->
                {rtc: RoseTree<C> ->
                    rtc.ap(rtb.ap(rta.fmap(f)))
                }
            }
        }   // liftA3

    fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (RoseTree<A>) -> (RoseTree<B>) -> (RoseTree<C>) -> RoseTree<D> =
        liftA3(C3(f))



    // Monad extension functions:

    /**
     * Lift a function to a monad.
     */
    fun <A, B> liftM(f: (A) -> B): (RoseTree<A>) -> RoseTree<B> =
        {rta: RoseTree<A> ->
            rta.bind{a: A -> singleton(f(a))}
        }   // liftM

    /**
     * Lift a binary function to a monad.
     */
    fun <A, B, C> liftM2(f: (A) -> (B) -> C): (RoseTree<A>) -> (RoseTree<B>) -> RoseTree<C> =
        {rta: RoseTree<A> ->
            {rtb: RoseTree<B> ->
                rta.bind{a: A -> rtb.bind{b: B -> singleton(f(a)(b))}}
            }
        }   // liftM2

    fun <A, B, C> liftM2(f: (A, B) -> C): (RoseTree<A>) -> (RoseTree<B>) -> RoseTree<C> =
        liftM2(C2(f))

    /**
     * Lift a ternary function to a monad.
     */
    fun <A, B, C, D> liftM3(f: (A) -> (B) -> (C) -> D): (RoseTree<A>) -> (RoseTree<B>) -> (RoseTree<C>) -> RoseTree<D> =
        {rta: RoseTree<A> ->
            {rtb: RoseTree<B> ->
                {rtc: RoseTree<C> ->
                    rta.bind{a: A -> rtb.bind{b: B -> rtc.bind{c: C -> singleton(f(a)(b)(c))}}}
                }
            }
        }   // liftM3

    fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (RoseTree<A>) -> (RoseTree<B>) -> (RoseTree<C>) -> RoseTree<D> =
        liftM3(C3(f))



    /**
     * 2-dimensional ASCII drawing of a tree.
     */
    fun <A> drawTree(tree: RoseTree<A>): String = unlines(draw(tree.map{a:A -> "$a"}))

    /**
     * 2-dimensional ASCII drawing of a forest.
     */
    internal fun drawForest(forest: Forest<String>): String = unlines(forest.map(::drawTree))

    internal fun draw(tree: RoseTree<String>): List<String> {
        fun shift(first: String, other: String, list: List<String>): List<String> =
                ListF.cons(first, ListF.replicate(list.size(), other)).zipWith(list){xs: String -> {ys: String -> xs + ys}}

        fun drawSubTrees(list: List<RoseTree<String>>): List<String> {
            return if (list.isEmpty())
                ListF.empty()
            else if (list.length() == 1)
                ListF.cons("|", shift(":- ", "   ", draw(list.head())))
            else
                ListF.cons("|", shift("+- ", "|  ", draw(list.head()))).concatenate(drawSubTrees(list.tail()))
        }   // drawSubTrees

        return ListF.from(tree.label.lines()).append(drawSubTrees(tree.subForest))
    }   // draw



// ---------- implementation ------------------------------

    private fun unlines(list: List<String>): String {
        fun recUnlines(list: List<String>, acc: StringBuffer): String {
            return when (list) {
                is Nil -> acc.toString()
                is Cons -> recUnlines(list.tail(), acc.append("${list.head()}\n"))
            }
        }   // recUnlines

        return recUnlines(list, StringBuffer())
    }   // unlines

}   // RoseTreeF
