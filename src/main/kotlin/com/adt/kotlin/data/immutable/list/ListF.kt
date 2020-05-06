package com.adt.kotlin.data.immutable.list

/**
 * A class hierarchy defining an immutable list collection. The algebraic data
 *   type declaration is:
 *
 * datatype List[A] = Nil
 *                  | Cons of A * List[A]
 *
 * The implementation mimics functional Lists as found in Haskell. The
 *   member functions and the extension functions mostly use primitive
 *   recursion over the List value constructors. Local tail recursive
 *   functions are commonly used.
 *
 * The documentation uses the notation [...] to represent a list instance.
 *
 * @param A                     the (covariant) type of elements in the list
 *
 * @author	                    Ken Barclay
 * @since                       October 2019
 */

import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.Option.None
import com.adt.kotlin.data.immutable.option.Option.Some
import com.adt.kotlin.data.immutable.option.OptionF

import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3
import java.util.ArrayList



object ListF {

    /**
     * Factory functions to create the base instances.
     */
    fun <A> nil(): List<A> = List.Nil

    fun <A> cons(a: A, list: List<A>): List<A> = List.Cons(a, list)

    /**
     * Create an empty list.
     *
     * Examples:
     *   empty() = []
     *
     * @return                      empty list
     */
    fun <A> empty(): List<A> = List.Nil


    /**
     * Make a list with one element.
     *
     * Examples:
     *   singleton(5) = [5]
     *
     * @param x                     new element
     * @return                      new list with that one element
     */
    fun <A> singleton(x: A): List<A> = List.Cons(x, List.Nil)

    /**
     * Factory functions to create the base instances from a list of none or more elements.
     */
    fun <A> of(): List<A> = nil()

    fun <A> of(a1: A): List<A> = cons(a1, nil())

    fun <A> of(a1: A, a2: A): List<A> = cons(a1, cons(a2, nil()))

    fun <A> of(a1: A, a2: A, a3: A): List<A> = cons(a1, cons(a2, cons(a3, nil())))

    fun <A> of(a1: A, a2: A, a3: A, a4: A): List<A> = cons(a1, cons(a2, cons(a3, cons(a4, nil()))))

    fun <A> of(a1: A, a2: A, a3: A, a4: A, a5: A): List<A> = cons(a1, cons(a2, cons(a3, cons(a4, cons(a5, nil())))))

    fun <A> of(vararg seq: A): List<A> = from(*seq)



    /**
     * Convert a variable-length parameter series into an immutable list.
     *   If no parameters are present then an empty list is produced.
     *
     * Examples:
     *   from(1, 2, 3) = [1, 2, 3]
     *   from() = []
     *
     * @param seq                   variable-length parameter series
     * @return                      immutable list of the given values
     */
    fun <A> from(vararg seq: A): List<A> =
        seq.foldRight(nil()) {x: A, xs: List<A> -> cons(x, xs)}

    /**
     * Convert a java-based list into an immutable list.
     *
     * Examples:
     *   from([1, 2, 3]) = [1, 2, 3]
     *   from([]) = []
     *
     * @param list                  java based list of elements
     * @return                      immutable list of the given values
     */
    fun <A> from(list: kotlin.collections.List<A>): List<A> =
        list.foldRight(nil()) {x: A, xs: List<A> -> cons(x, xs)}

    /**
     * Convert a String into an immutable list of characters.
     */
    fun from(str: String): List<Char> =
        str.foldRight(nil()){ch, list -> cons(ch, list)}

    /**
     * Convert an array list into an immutable list.
     *
     * Examples:
     *   fromArray((1, 2, 3)) = [1, 2, 3]
     *   fromArray(()) = []
     *
     * @param xs                    array
     * @return                      immutable list of the given values
     */
    fun <A> fromArray(xs: Array<A>): List<A> =
        xs.foldRight(nil()) {x: A, ys: List<A> -> cons(x, ys)}

    /**
     * Convert an immutable list to an array list.
     *
     * Examples:
     *   toList([1, 2, 3]) = [1, 2, 3]
     *   toList([]) = []
     *
     * @param xs                    existing immutable list
     * @return                      an array list
     */
    fun <A> toList(xs: List<A>): kotlin.collections.List<A> =
        xs.foldLeft(arrayListOf()){list: ArrayList<A> -> {x: A -> list.add(x); list}}



    /**
     * Return a list of integers starting with the given from value and
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
    fun range(from: Int, to: Int, step: Int = 1): List<Int> {
        if (step == 0)
            throw ListException("range: zero step disallowed: from: $from to: $to step: $step")
        if (step > 0 && from >= to)
            throw ListException("range: positive step requires from < to: from: $from to: $to step: $step")
        if (step < 0 && from <= to)
            throw ListException("range: negative step requires from > to: from: $from to: $to step: $step")

        tailrec
        fun recRange(from: Int, to: Int, step: Int, acc: ListBufferIF<Int>): List<Int> {
            return if (step > 0 && from >= to)
                acc.toList()
            else if (step < 0 && from <= to)
                acc.toList()
            else
                recRange(from + step, to, step, acc.append(from))
        }   // recRange

        return recRange(from, to, step, ListBuffer())
    }   // range

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
    fun closedRange(from: Int, to: Int, step: Int = 1): List<Int> {
        if (step == 0)
            throw ListException("closedRange: zero step disallowed: from: $from to: $to step: $step")
        if (step > 0 && from > to)
            throw ListException("closedRange: positive step requires from < to: from: $from to: $to step: $step")
        if (step < 0 && from < to)
            throw ListException("closedRange: negative step requires from > to: from: $from to: $to step: $step")

        tailrec
        fun recClosedRange(from: Int, to: Int, step: Int, acc: ListBufferIF<Int>): List<Int> {
            return if (step > 0 && from > to)
                acc.toList()
            else if (step < 0 && from < to)
                acc.toList()
            else
                recClosedRange(from + step, to, step, acc.append(from))
        }   // recClosedRange

        return recClosedRange(from, to, step, ListBuffer())
    }   // closedRange

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
    fun range(from: Double, to: Double, step: Double = 1.0): List<Double> {
        if (Math.abs(step) < 1e-10)
            throw ListException("range: zero step disallowed: from: $from to: $to step: $step")
        if (step > 0 && from >= to)
            throw ListException("range: positive step requires from < to: from: $from to: $to step: $step")
        if (step < 0 && from <= to)
            throw ListException("range: negative step requires from > to: from: $from to: $to step: $step")

        tailrec
        fun recRange(from: Double, to: Double, step: Double, acc: ListBufferIF<Double>): List<Double> {
            return if (step > 0 && from >= to)
                acc.toList()
            else if (step < 0 && from <= to)
                acc.toList()
            else
                recRange(from + step, to, step, acc.append(from))
        }   // recRange

        return recRange(from, to, step, ListBuffer())
    }   // range

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
     * @param to                    the maximum value for the list (inclusive)
     * @param step                  increment
     * @return                      the list of doubles from => to (inclusive)
     */
    fun closedRange(from: Double, to: Double, step: Double = 1.0): List<Double> {
        if (Math.abs(step) < 1e-10)
            throw ListException("closedRange: zero step disallowed: from: $from to: $to step: $step")
        if (step > 0 && from >= to)
            throw ListException("closedRange: positive step requires from < to: from: $from to: $to step: $step")
        if (step < 0 && from <= to)
            throw ListException("closedRange: negative step requires from > to: from: $from to: $to step: $step")

        tailrec
        fun recClosedRange(from: Double, to: Double, step: Double, acc: ListBufferIF<Double>): List<Double> {
            return if (step > 0 && from > to)
                acc.toList()
            else if (step < 0 && from < to)
                acc.toList()
            else
                recClosedRange(from + step, to, step, acc.append(from))
        }   // recClosedRange

        return recClosedRange(from, to, step, ListBuffer())
    }   // closedRange

    /**
     * Produce a list with n copies of the element t. Throws a
     *   ListException if the int argument is negative.
     *
     * Examples:
     *   replicate(4, 5) = [5, 5, 5, 5]
     *   replicate(0, 5) = []
     *
     * @param n                     number of copies required
     * @param t                     element to be copied
     * @return                      list of the copied element
     */
    fun <A> replicate(n: Int, t: A): List<A> {
        tailrec
        fun recReplicate(m: Int, a: A, acc: ListBufferIF<A>): List<A> {
            return if (m == 0)
                acc.toList()
            else
                recReplicate(m - 1, a, acc.append(a))
        }   // recReplicate

        return if (n < 0)
            throw ListException("replicate: number is negative")
        else
            recReplicate(n, t, ListBuffer())
    }   // replicate

    /**
     * Produce a list with n copies of the list xs. Throws a ListException
     *   if the int argument is negative.
     *
     * Examples:
     *   replicate(3, [1, 2, 3]) = [1, 2, 3, 1, 2, 3, 1, 2, 3]
     *   replicate(0, [1, 2, 3]) = []
     *   replicate(5, []) = []
     */
    fun <A> replicate(n: Int, xs: List<A>): List<A> {
        tailrec
        fun recReplicate(n: Int, xs: List<A>, acc: ListBufferIF<A>): List<A> {
            return if (n == 0)
                acc.toList()
            else
                recReplicate(n - 1, xs, acc.append(xs))
        }   // recReplicate

        return if (n < 0)
            throw ListException("replicate: number is negative")
        else
            recReplicate(n, xs, ListBuffer())
    }   // replicate

    /**
     * shallowFlatten is used to flatten a nested list structure. The
     *   shallowFlatten does not recurse into sub-lists, eg:
     *
     * Examples:
     *   shallowFlatten([[1, 2, 3], [4, 5]]) = [1, 2, 3, 4, 5]
     *   shallowFlatten([[[1, 2], [3]], [[4, 5]]]) = [[1, 2], [3], [4, 5]]
     *
     * @param xss                   existing list of lists
     * @return                      new list of flattened list
     */
    fun <A> shallowFlatten(xss: List<List<A>>): List<A> {
        val appendC: (List<A>) -> (List<A>) -> List<A> = {xs: List<A> -> {ys: List<A> -> xs.append(ys)}}
        return xss.foldLeft(empty<A>(), appendC)
    }   // shallowFlatten

    fun <A> flatten(xss: List<List<A>>): List<A> = shallowFlatten(xss)

    fun <A> concat(xss: List<List<A>>): List<A> = shallowFlatten(xss)

    /**
     * Transform a list of pairs into a list of first components and a list of second components.
     *
     * Examples:
     *  [(1, 2), (3, 4), (5, 6)].unzip() = ([1, 3, 5], [2, 4, 6])
     *  [].unzip() = ([], [])
     *
     * @param xs                    list of pairs
     * @return                      pair of lists
     */
    fun <A, B> unzip(xs: List<Pair<A, B>>): Pair<List<A>, List<B>> =
        xs.foldRight(Pair(List.Nil, List.Nil), {pr: Pair<A, B> -> {prs: Pair<List<A>, List<B>> -> Pair(List.Cons(pr.first, prs.first), List.Cons(pr.second, prs.second))}})

    /**
     * Unfold across the given function starting at the given value to produce a list.
     *
     * @param b                 the start value to begin the unfold
     * @param f                 the function to unfold across
     * @return                  a new list that is a result of unfolding until the function does not produce a value
     */
    fun <A, B> unfold(b: B, f: (B) -> Option<Pair<A, B>>): List<A> {
        val op: Option<Pair<A, B>> = f(b)
        return when (op) {
            is None -> nil()
            is Some -> {
                val pr: Pair<A, B> = op.value
                cons(pr.first, unfold(pr.second, f))
            }
        }
    }   // unfold

    /**
     * Create a list where the first item is calculated by applying the function on the third parameter,
     *   the second item by applying the function on the previous result and so on.
     *
     * @param f                 the function to iterate with
     * @param predicate         the predicate which must be true for the next item in order to continue the iteration
     * @param a                 the input to the first iteration
     * @return                  a list where the first item is calculated by applying the function on the third parameter,
     *                              the second item by applying the function on the previous result and so on
     */
    fun <A> iterateWhile(f: (A) -> A, predicate: (A) -> Boolean, a: A): List<A> =
        unfold(a){aa: A -> OptionF.iif(Pair(aa, f(aa))){_ -> predicate(aa)}}




    // Functor extension functions:

    /**
     * Lift a function into the List context.
     */
    fun <A, B> lift(f: (A) -> B): (List<A>) -> List<B> =
        {la: List<A> -> la.fmap(f)}



    // Applicative extension functions:

    /**
     * Lift a function to actions.
     */
    fun <A, B> liftA(f: (A) -> B): (List<A>) -> List<B> =
        {la: List<A> ->
            la.ap(singleton(f))
        }   // liftA

    /**
     * Lift a binary function to actions.
     *
     * Some functors support an implementation of lift2 that is more efficient than the
     *   default one. In particular, if fmap is an expensive operation, it is likely
     *   better to use liftA2 than to fmap over the structure and then use ap.
     */
    fun <A, B, C> liftA2(f: (A) -> (B) -> C): (List<A>) -> (List<B>) -> List<C> =
        {la: List<A> ->
            {lb: List<B> ->
                lb.ap(la.fmap(f))
            }
        }   // liftA2

    fun <A, B, C> liftA2(f: (A, B) -> C): (List<A>) -> (List<B>) -> List<C> =
        liftA2(C2(f))

    /**
     * Lift a ternary function to actions.
     */
    fun <A, B, C, D> liftA3(f: (A) -> (B) -> (C) -> D): (List<A>) -> (List<B>) -> (List<C>) -> List<D> =
        {la: List<A> ->
            {lb: List<B> ->
                {lc: List<C> ->
                    lc.ap(lb.ap(la.fmap(f)))
                }
            }
        }   // liftA3

    fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (List<A>) -> (List<B>) -> (List<C>) -> List<D> =
        liftA3(C3(f))



    // Monad extension functions:

    /**
     * Lift a function to a monad.
     */
    fun <A, B> liftM(f: (A) -> B): (List<A>) -> List<B> =
        {la: List<A> ->
            la.bind{a: A -> singleton(f(a))}
        }   // liftM

    /**
     * Lift a binary function to a monad.
     */
    fun <A, B, C> liftM2(f: (A) -> (B) -> C): (List<A>) -> (List<B>) -> List<C> =
        {la: List<A> ->
            {lb: List<B> ->
                la.bind{a: A -> lb.bind{b: B -> singleton(f(a)(b))}}
            }
        }   // liftM2

    fun <A, B, C> liftM2(f: (A, B) -> C): (List<A>) -> (List<B>) -> List<C> =
        liftM2(C2(f))

    /**
     * Lift a ternary function to a monad.
     */
    fun <A, B, C, D> liftM3(f: (A) -> (B) -> (C) -> D): (List<A>) -> (List<B>) -> (List<C>) -> List<D> =
        {la: List<A> ->
            {lb: List<B> ->
                {lc: List<C> ->
                    la.bind{a: A -> lb.bind{b: B -> lc.bind{c: C -> singleton(f(a)(b)(c))}}}
                }
            }
        }   // liftM3

    fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (List<A>) -> (List<B>) -> (List<C>) -> List<D> =
        liftM3(C3(f))

    /**
     * Map the given function across the two Lists.
     */
    fun <A, B, C> map2(la: List<A>, lb: List<B>, f: (A) -> (B) -> C): List<C> =
        la.bind{a: A -> lb.map{b: B -> f(a)(b)}}

    fun <A, B, C> map2(la: List<A>, lb: List<B>, f: (A, B) -> C): List<C> =
        map2(la, lb, C2(f))

    /**
     * Map the given function across the three Lists.
     */
    fun <A, B, C, D> map3(la: List<A>, lb: List<B>, lc: List<C>, f: (A) -> (B) -> (C) -> D): List<D> =
        la.bind{a: A -> lb.bind{b: B -> lc.map{c: C -> f(a)(b)(c)}}}

    fun <A, B, C, D> map3(la: List<A>, lb: List<B>, lc: List<C>, f: (A, B, C) -> D): List<D> =
        map3(la, lb, lc, C3(f))

}   // ListF
