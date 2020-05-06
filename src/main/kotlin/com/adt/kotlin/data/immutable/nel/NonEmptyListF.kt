package com.adt.kotlin.data.immutable.nel

/**
 * A singly-linked list that is guaranteed to be non-empty. A data type which
 *   represents a non empty list, with single element (hd) and optional
 *   structure (tl).
 *
 * The documentation uses the notation [x0 :| x1, x2, ...] to represent a
 *   list instance.
 *
 * @param A                     the (covariant) type of elements in the list
 *
 * @author	                    Ken Barclay
 * @since                       October 2019
 */

import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.ListF
import com.adt.kotlin.data.immutable.list.ListF.empty
import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3


object NonEmptyListF {

    /**
     * Factory functions to create the base instances.
     */
    fun <A> cons(a: A, nel: NonEmptyList<A>): NonEmptyList<A> = NonEmptyList(a, ListF.cons(nel.hd, nel.tl))

    /**
     * Make a list with one element.
     *
     * Examples:
     *   singleton(5) = [5]
     *
     * @param x                     new element
     * @return                      new list with that one element
     */
    fun <A> singleton(x: A): NonEmptyList<A> = NonEmptyList(x, empty())



    fun <A> of(a1: A): NonEmptyList<A> =
        NonEmptyList(a1, empty())

    fun <A> of(a1: A, a2: A): NonEmptyList<A> =
        NonEmptyList(a1, ListF.of(a2))

    fun <A> of(a1: A, a2: A, a3: A): NonEmptyList<A> =
        NonEmptyList(a1, ListF.of(a2, a3))

    fun <A> of(a1: A, a2: A, a3: A, a4: A): NonEmptyList<A> =
        NonEmptyList(a1, ListF.of(a2, a3, a4))

    fun <A> of(a1: A, a2: A, a3: A, a4: A, a5: A): NonEmptyList<A> =
        NonEmptyList(a1, ListF.of(a2, a3, a4, a5))

    fun <A> of(a1: A, vararg seq: A): NonEmptyList<A> =
        NonEmptyList(a1, seq.foldRight(ListF.nil()) { x: A, xs: List<A> -> ListF.cons(x, xs) })

    /**
     * Returns a list of integers starting with the given from value and
     *   ending with the given to value (exclusive).
     *
     * Examples:
     *   range(1, 5) = [1, 2, 3, 4]
     *   range(1, 5, 1) = [1, 2, 3, 4]
     *   range(1, 5, 2) = [1, 3]
     *   range(1, 5, 3) = [1, 4]
     *   range(1, 5, 4) = [1]
     *   range(1, 5, 5) = [1]
     *   range(1, 5, 6) = [1]
     *   range(9, 5, -1) = [9, 8, 7, 6]
     *   range(9, 5, -3) = [9, 6]
     *
     * @param from                  the minimum value for the list (inclusive)
     * @param to                    the maximum value for the list (exclusive)
     * @param step                  increment
     * @return                      the list of integers from => to (exclusive)
     */
    fun range(from: Int, to: Int, step: Int = 1): NonEmptyList<Int> =
        NonEmptyList(ListF.range(from, to, step))

    /**
     * Returns a list of integers starting with the given from value and
     *   ending with the given to value (inclusive).
     *
     * Examples:
     *   closedRange(1, 5) = [1, 2, 3, 4, 5]
     *   closedRange(1, 5, 1) = [1, 2, 3, 4, 5]
     *   closedRange(1, 5, 2) = [1, 3, 5]
     *   closedRange(1, 5, 3) = [1, 4]
     *   closedRange(1, 5, 4) = [1, 5]
     *   closedRange(1, 5, 5) = [1]
     *   closedRange(1, 5, 6) = [1]
     *   closedRange(9, 5, -1) = [9, 8, 7, 6, 5]
     *   closedRange(9, 5, -3) = [9, 6]
     *
     * @param from                  the minimum value for the list (inclusive)
     * @param to                    the maximum value for the list (inclusive)
     * @param step                  increment
     * @return                      the list of integers from => to (inclusive)
     */
    fun closedRange(from: Int, to: Int, step: Int = 1): NonEmptyList<Int> =
        NonEmptyList(ListF.closedRange(from, to, step))

    /**
     * Returns a list of doubles starting with the given from value and
     *   ending with the given to value (exclusive).
     *
     * Examples:
     *   range(1.0, 5.0) = [1.0, 2.0, 3.0, 4.0]
     *   range(1.0, 5.0, 1.0) = [1.0, 2.0, 3.0, 4.0]
     *   range(1.0, 5.0, 2.0) = [1.0, 3.0]
     *   range(1.0, 5.0, 3.0) = [1.0, 4.0]
     *   range(1.0, 5.0, 4.0) = [1.0]
     *   range(1.0, 5.0, 5.0) = [1.0]
     *   range(1.0, 5.0, 6.0) = [1.0]
     *   range(9.0, 5.0, -1.0) = [9.0, 8.0, 7.0, 6.0]
     *   range(9.0, 5.0, -3.0) = [9.0, 6.0]
     *   range(3.5, 5.5, 0.5) = [3.5, 4.0, 4.5, 5.0]
     *   range(3.5, 5.5, 1.0) = [3.5, 4.5]
     *   range(3.5, 5.5, 1.5) = [3.5, 5.0]
     *
     * @param from                  the minimum value for the list (inclusive)
     * @param to                    the maximum value for the list (exclusive)
     * @param step                  increment
     * @return                      the list of doubles from => to (exclusive)
     */
    fun range(from: Double, to: Double, step: Double = 1.0): NonEmptyList<Double> =
        NonEmptyList(ListF.range(from, to, step))

    /**
     * Returns a list of doubles starting with the given from value and
     *   ending with the given to value (exclusive).
     *
     * Examples:
     *   closedRange(1.0, 5.0) = [1.0, 2.0, 3.0, 4.0, 5.0]
     *   closedRange(1.0, 5.0, 1.0) = [1.0, 2.0, 3.0, 4.0, 5.0]
     *   closedRange(1.0, 5.0, 2.0) = [1.0, 3.0, 5.0]
     *   closedRange(1.0, 5.0, 3.0) = [1.0, 4.0]
     *   closedRange(1.0, 5.0, 4.0) = [1.0, 5.0]
     *   closedRange(1.0, 5.0, 5.0) = [1.0]
     *   closedRange(1.0, 5.0, 6.0) = [1.0]
     *   closedRange(9.0, 5.0, -1.0) = [9.0, 8.0, 7.0, 6.0, 5.0]
     *   closedRange(9.0, 5.0, -3.0) = [9.0, 6.0]
     *   closedRange(3.5, 5.5, 0.5) = [3.5, 4.0, 4.5, 5.0, 5.5]
     *   closedRange(3.5, 5.5, 1.0) = [3.5, 4.5, 5.5]
     *   closedRange(3.5, 5.5, 1.5) = [3.5, 5.0]
     *
     * @param from                  the minimum value for the list (inclusive)
     * @param to                    the maximum value for the list (exclusive)
     * @param step                  increment
     * @return                      the list of doubles from => to (exclusive)
     */
    fun closedRange(from: Double, to: Double, step: Double = 1.0): NonEmptyList<Double> =
        NonEmptyList(ListF.closedRange(from, to, step))

    /**
     * Produce a list with n copies of the element t. Throws a
     *   ListException if the int argument is negative.
     *
     * Examples:
     *   replicate(4, 5) = [5 :| 5, 5, 5]
     *   replicate(0, 5) = exception
     *
     * @param n                     number of copies required
     * @param t                     element to be copied
     * @return                      list of the copied element
     */
    fun <A> replicate(n: Int, t: A): NonEmptyList<A> {
        return if (n <= 0)
            throw NonEmptyListException("replicate: number is negative")
        else
            NonEmptyList(ListF.replicate(n, t))
    }

    /**
     * Transform a list of pairs into a list of first components and a list of second components.
     *
     * Examples:
     *  [(1, 2) :| (3, 4), (5, 6)].unzip() = ([1 :| 3, 5], [2 :| 4, 6])
     *
     * @param xs                    list of pairs
     * @return                      pair of lists
     */
    fun <A, B> unzip(xs: NonEmptyList<Pair<A, B>>): Pair<NonEmptyList<A>, NonEmptyList<B>> {
        val xsList: List<Pair<A, B>> = xs.toList()
        val xsListUnzip: Pair<List<A>, List<B>> = ListF.unzip(xsList)
        return Pair(NonEmptyList(xsListUnzip.first), NonEmptyList((xsListUnzip.second)))
    }   // unzip



    // Functor extension functions:

    /**
     * Lift a function into the Option context.
     */
    fun <A, B> lift(f: (A) -> B): (NonEmptyList<A>) -> NonEmptyList<B> =
        {nela: NonEmptyList<A> -> nela.fmap(f)}



    // Applicative extension functions:

    /**
     * Lift a function to actions.
     */
    fun <A, B> liftA(f: (A) -> B): (NonEmptyList<A>) -> NonEmptyList<B> =
        {nela: NonEmptyList<A> ->
            nela.ap(singleton(f))
        }   // liftA

    /**
     * Lift a binary function to actions.
     *
     * Some functors support an implementation of lift2 that is more efficient than the
     *   default one. In particular, if fmap is an expensive operation, it is likely
     *   better to use liftA2 than to fmap over the structure and then use ap.
     */
    fun <A, B, C> liftA2(f: (A) -> (B) -> C): (NonEmptyList<A>) -> (NonEmptyList<B>) -> NonEmptyList<C> =
        {nela: NonEmptyList<A> ->
            {nelb: NonEmptyList<B> ->
                nelb.ap(nela.fmap(f))
            }
        }   // liftA2

    fun <A, B, C> liftA2(f: (A, B) -> C): (NonEmptyList<A>) -> (NonEmptyList<B>) -> NonEmptyList<C> =
        liftA2(C2(f))

    /**
     * Lift a ternary function to actions.
     */
    fun <A, B, C, D> liftA3(f: (A) -> (B) -> (C) -> D): (NonEmptyList<A>) -> (NonEmptyList<B>) -> (NonEmptyList<C>) -> NonEmptyList<D> =
        {nela: NonEmptyList<A> ->
            {nelb: NonEmptyList<B> ->
                {nelc: NonEmptyList<C> ->
                    nelc.ap(nelb.ap(nela.fmap(f)))
                }
            }
        }   // liftA3

    fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (NonEmptyList<A>) -> (NonEmptyList<B>) -> (NonEmptyList<C>) -> NonEmptyList<D> =
        liftA3(C3(f))



    // Monad extension functions:

    /**
     * Lift a function to a monad.
     */
    fun <A, B> liftM(f: (A) -> B): (NonEmptyList<A>) -> NonEmptyList<B> =
        {nela: NonEmptyList<A> ->
            nela.bind{a: A -> singleton(f(a))}
        }   // liftM

    /**
     * Lift a binary function to a monad.
     */
    fun <A, B, C> liftM2(f: (A) -> (B) -> C): (NonEmptyList<A>) -> (NonEmptyList<B>) -> NonEmptyList<C> =
        {nela: NonEmptyList<A> ->
            {nelb: NonEmptyList<B> ->
                nela.bind{a: A -> nelb.bind{b: B -> singleton(f(a)(b))}}
            }
        }   // liftM2

    fun <A, B, C> liftM2(f: (A, B) -> C): (NonEmptyList<A>) -> (NonEmptyList<B>) -> NonEmptyList<C> =
        liftM2(C2(f))

    /**
     * Lift a ternary function to a monad.
     */
    fun <A, B, C, D> liftM3(f: (A) -> (B) -> (C) -> D): (NonEmptyList<A>) -> (NonEmptyList<B>) -> (NonEmptyList<C>) -> NonEmptyList<D> =
        {nela: NonEmptyList<A> ->
            {nelb: NonEmptyList<B> ->
                {nelc: NonEmptyList<C> ->
                    nela.bind{a: A -> nelb.bind{b: B -> nelc.bind{c: C -> singleton(f(a)(b)(c))}}}
                }
            }
        }   // liftM3

    fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (NonEmptyList<A>) -> (NonEmptyList<B>) -> (NonEmptyList<C>) -> NonEmptyList<D> =
        liftM3(C3(f))

    /**
     * Map the given function across the two Lists.
     */
    fun <A, B, C> map2(la: NonEmptyList<A>, lb: NonEmptyList<B>, f: (A) -> (B) -> C): NonEmptyList<C> =
        la.bind{a: A -> lb.map{b: B -> f(a)(b)}}

    fun <A, B, C> map2(la: NonEmptyList<A>, lb: NonEmptyList<B>, f: (A, B) -> C): NonEmptyList<C> =
        map2(la, lb, C2(f))

    /**
     * Map the given function across the three Lists.
     */
    fun <A, B, C, D> map3(la: NonEmptyList<A>, lb: NonEmptyList<B>, lc: NonEmptyList<C>, f: (A) -> (B) -> (C) -> D): NonEmptyList<D> =
        la.bind{a: A -> lb.bind{b: B -> lc.map{c: C -> f(a)(b)(c)}}}

    fun <A, B, C, D> map3(la: NonEmptyList<A>, lb: NonEmptyList<B>, lc: NonEmptyList<C>, f: (A, B, C) -> D): NonEmptyList<D> =
        map3(la, lb, lc, C3(f))

}   // NonEmptyListF
