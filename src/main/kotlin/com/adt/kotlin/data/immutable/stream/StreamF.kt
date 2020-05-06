package com.adt.kotlin.data.immutable.stream

/**
 * A sequence of elements supporting sequential aggregate operations.
 *   To perform a computation, stream operations are composed into a
 *   stream pipeline. A stream pipeline consists of a source
 *   (which might be an array, a collection, a generator function, etc),
 *   zero or more intermediate operations (which transform a stream
 *   into another stream, such as filter), and a terminal operation
 *   (which produces a result or side-effect, such as count or forEach).
 *   Streams are lazy: computation on the source data is only performed
 *   when the terminal operation is initiated, and source elements are
 *   consumed only as needed.
 *
 * @param A                     the (covariant) type of elements in the stream
 *
 * @author	                    Ken Barclay
 * @since                       November 2019
 */

import com.adt.kotlin.data.immutable.stream.Stream.Nil
import com.adt.kotlin.data.immutable.stream.Stream.Cons

import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3
import com.adt.kotlin.data.immutable.list.List.Nil as ListNil
import com.adt.kotlin.data.immutable.list.List.Cons as ListCons



object StreamF {

    /**
     * Create an empty stream.
     */
    fun <A> empty(): Stream<A> = Nil

    /**
     * Make a stream with one element.
     *
     * Examples:
     *   singleton(5) = [5]
     *
     * @param a                     new element
     * @return                      new stream with that one element
     */
    fun <A> singleton(a: A): Stream<A> = Cons(a){-> Nil}

    /**
     * Convert a variable-length parameter series into an immutable stream.
     *   If no parameters are present then an empty stream is produced.
     *
     * Intermediate operation.
     *
     * Examples:
     *   of(1, 2, 3) = [1, 2, 3]
     *   of() = []
     *
     * @param seq                   variable-length parameter series
     * @return                      immutable stream of the given values
     */
    fun <A> of(vararg seq: A): Stream<A> {
        fun recOf(idx: Int, seq: Array<out A>): Stream<A> =
            if(idx == seq.size)
                Nil
            else
                Cons(seq[idx]){-> recOf(1 + idx, seq)}

        return recOf(0, seq)
    }   // of

    /**
     * Convert a variable-length parameter series into an immutable stream.
     *   If no parameters are present then an empty stream is produced.
     *
     * Intermediate operation.
     *
     * Examples:
     *   of(1, 2, 3) = [1, 2, 3]
     *   of() = []
     *
     * @param seq                   variable-length parameter series
     * @return                      immutable stream of the given values
     */
    fun <A> from(vararg seq: A): Stream<A> {
        fun recFrom(idx: Int, seq: Array<out A>): Stream<A> =
            if(idx == seq.size)
                Nil
            else
                Cons(seq[idx]){-> recFrom(1 + idx, seq)}

        return recFrom(0, seq)
    }   // from

    /**
     * Convert a list into an immutable stream.
     *
     * Intermediate operation.
     *
     * Examples:
     *   from([1, 2, 3]) = [1, 2, 3]
     *   from([]) = []
     *
     * @param list                  list of elements
     * @return                      immutable stream of the given values
     */
    fun <A> from(list: List<A>): Stream<A> {
        return when (list) {
            is ListNil -> Nil
            is ListCons -> Cons(list.head()){-> from(list.tail())}
        }
    }   // from

    /**
     * Convert a java-based list into an immutable stream.
     *
     * Intermediate operation.
     *
     * Examples:
     *   from([1, 2, 3]) = [1, 2, 3]
     *   from([]) = []
     *
     * @param list                  java based list of elements
     * @return                      immutable stream of the given values
     */
    fun <A> from(list: kotlin.collections.List<A>): Stream<A> {
        fun recFrom(idx: Int, seq: kotlin.collections.List<A>): Stream<A> =
            if(idx == seq.size)
                Nil
            else
                Cons(seq[idx]){-> recFrom(1 + idx, seq)}

        return recFrom(0, list)
    }   // from

    /**
     * Convert a String into an immutable stream of characters.
     *
     * Intermediate operation.
     */
    fun from(str: String): Stream<Char> {
        fun recFrom(idx: Int, str: String): Stream<Char> =
            if(idx == str.length)
                Nil
            else
                Cons(str[idx]){-> recFrom(1 + idx, str)}

        return recFrom(0, str)
    }   // from

    /**
     * Return a stream of integers starting with the given from value and
     *   ending with the given to value (exclusive).
     *
     * Intermediate operation.
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
     * @param from                  the minimum value for the stream (inclusive)
     * @param to                    the maximum value for the stream (exclusive)
     * @param step                  increment
     * @return                      the stream of integers from => to (exclusive)
     */
    fun range(from: Int, to: Int, step: Int = 1): Stream<Int> {
        if (step == 0)
            throw StreamException("range: zero step disallowed: from: $from to: $to step: $step")
        if (step > 0 && from >= to)
            throw StreamException("range: positive step requires from < to: from: $from to: $to step: $step")
        if (step < 0 && from <= to)
            throw StreamException("range: negative step requires from > to: from: $from to: $to step: $step")

        fun recRange(from: Int, to: Int, step: Int): Stream<Int> {
            return if (step > 0 && from >= to)
                Nil
            else if (step < 0 && from <= to)
                Nil
            else
                Cons(from){-> recRange(from + step, to, step)}
        }   // recRange

        return recRange(from, to, step)
    }   // range

    /**
     * Returns a stream of integers starting with the given from value and
     *   ending with the given to value (inclusive).
     *
     * Intermediate operation.
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
     * @param from                  the minimum value for the stream (inclusive)
     * @param to                    the maximum value for the stream (inclusive)
     * @param step                  increment
     * @return                      the stream of integers from => to (inclusive)
     */
    fun closedRange(from: Int, to: Int, step: Int = 1): Stream<Int> {
        if (step == 0)
            throw StreamException("closedRange: zero step disallowed: from: $from to: $to step: $step")
        if (step > 0 && from > to)
            throw StreamException("closedRange: positive step requires from < to: from: $from to: $to step: $step")
        if (step < 0 && from < to)
            throw StreamException("closedRange: negative step requires from > to: from: $from to: $to step: $step")

        fun recClosedRange(from: Int, to: Int, step: Int): Stream<Int> {
            return if (step > 0 && from > to)
                Nil
            else if (step < 0 && from < to)
                Nil
            else
                Cons(from){-> recClosedRange(from + step, to, step)}
        }   // recClosedRange

        return recClosedRange(from, to, step)
    }   // closedRange

    /**
     * Returns a stream of doubles starting with the given from value and
     *   ending with the given to value (exclusive).
     *
     * Intermediate operation.
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
     * @param from                  the minimum value for the stream (inclusive)
     * @param to                    the maximum value for the stream (exclusive)
     * @param step                  increment
     * @return                      the stream of doubles from => to (exclusive)
     */
    fun range(from: Double, to: Double, step: Double = 1.0): Stream<Double> {
        if (Math.abs(step) < 1e-10)
            throw StreamException("range: zero step disallowed: from: $from to: $to step: $step")
        if (step > 0 && from >= to)
            throw StreamException("range: positive step requires from < to: from: $from to: $to step: $step")
        if (step < 0 && from <= to)
            throw StreamException("range: negative step requires from > to: from: $from to: $to step: $step")

        fun recRange(from: Double, to: Double, step: Double): Stream<Double> {
            return if (step > 0 && from >= to)
                Nil
            else if (step < 0 && from <= to)
                Nil
            else
                Cons(from){-> recRange(from + step, to, step)}
        }   // recRange

        return recRange(from, to, step)
    }   // range

    /**
     * Returns a stream of doubles starting with the given from value and
     *   ending with the given to value (exclusive).
     *
     * Intermediate operation.
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
     * @param from                  the minimum value for the stream (inclusive)
     * @param to                    the maximum value for the stream (inclusive)
     * @param step                  increment
     * @return                      the stream of doubles from => to (exclusive)
     */
    fun closedRange(from: Double, to: Double, step: Double = 1.0): Stream<Double> {
        if (Math.abs(step) < 1e-10)
            throw StreamException("closedRange: zero step disallowed: from: $from to: $to step: $step")
        if (step > 0 && from >= to)
            throw StreamException("closedRange: positive step requires from < to: from: $from to: $to step: $step")
        if (step < 0 && from <= to)
            throw StreamException("closedRange: negative step requires from > to: from: $from to: $to step: $step")

        fun recClosedRange(from: Double, to: Double, step: Double): Stream<Double> {
            return if (step > 0 && from > to)
                Nil
            else if (step < 0 && from < to)
                Nil
            else
                Cons(from){-> recClosedRange(from + step, to, step)}
        }   // recClosedRange

        return recClosedRange(from, to, step)
    }   // closedRange

    /**
     * Produce a stream with n copies of the element t. Throws a
     *   StreamException if the int argument is negative.
     *
     * Examples:
     *   replicate(4, 5) = [5, 5, 5, 5]
     *   replicate(0, 5) = []
     *
     * @param n                     number of copies required
     * @param a                     element to be copied
     * @return                      stream of the copied element
     */
    fun <A> replicate(n: Int, a: A): Stream<A> {
        fun recReplicate(n: Int, a: A): Stream<A> {
            return if (n == 0)
                Nil
            else
                Cons(a){-> replicate(n - 1, a)}
        }   // recReplicate

        return if (n < 0)
            throw StreamException("replicate: number is negative")
        else
            recReplicate(n, a)
    }   // replicate

    /**
     * Produce a list with n copies of the list xs. Throws a StreamException
     *   if the int argument is negative.
     *
     * Examples:
     *   replicate(3, [1, 2, 3]) = [1, 2, 3, 1, 2, 3, 1, 2, 3]
     *   replicate(0, [1, 2, 3]) = []
     *   replicate(5, []) = []
     */
    fun <A> replicate(n: Int, xs: Stream<A>): Stream<A> {
        tailrec
        fun recReplicate(n: Int, xs: Stream<A>, acc: Stream<A>): Stream<A> {
            return if (n == 0)
                acc
            else
                recReplicate(n - 1, xs, acc.append(xs))
        }   // recReplicate

        return if (n < 0)
            throw StreamException("replicate: number is negative")
        else
            recReplicate(n, xs, StreamF.empty())
    }   // replicate



    // Functor extension functions:

    /**
     * Lift a function into the Stream context.
     */
    fun <A, B> lift(f: (A) -> B): (Stream<A>) -> Stream<B> =
        {stream: Stream<A> -> stream.fmap(f)}



    // Applicative extension functions:

    /**
     * Lift a function to actions.
     */
    fun <A, B> liftA(f: (A) -> B): (Stream<A>) -> Stream<B> =
        {stream: Stream<A> ->
            stream.ap(singleton(f))
        }   // liftA

    /**
     * Lift a binary function to actions.
     *
     * Some functors support an implementation of lift2 that is more efficient than the
     *   default one. In particular, if fmap is an expensive operation, it is likely
     *   better to use liftA2 than to fmap over the structure and then use ap.
     */
    fun <A, B, C> liftA2(f: (A) -> (B) -> C): (Stream<A>) -> (Stream<B>) -> Stream<C> =
        {streamA: Stream<A> ->
            {streamB: Stream<B> ->
                streamB.ap(streamA.fmap(f))
            }
        }   // liftA2

    fun <A, B, C> liftA2(f: (A, B) -> C): (Stream<A>) -> (Stream<B>) -> Stream<C> =
        liftA2(C2(f))

    /**
     * Lift a ternary function to actions.
     */
    fun <A, B, C, D> liftA3(f: (A) -> (B) -> (C) -> D): (Stream<A>) -> (Stream<B>) -> (Stream<C>) -> Stream<D> =
        {streamA: Stream<A> ->
            {streamB: Stream<B> ->
                {streamC: Stream<C> ->
                    streamC.ap(streamB.ap(streamA.fmap(f)))
                }
            }
        }   // liftA3

    fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (Stream<A>) -> (Stream<B>) -> (Stream<C>) -> Stream<D> =
        liftA3(C3(f))

    /**
     * Execute a binary function.
     */
    fun <A, B, C> mapA2(streamA: Stream<A>, streamB: Stream<B>, f: (A) -> (B) -> C): Stream<C> =
        liftA2(f)(streamA)(streamB)

    /**
     * Execute a ternary function.
     */
    fun <A, B, C, D> mapA3(streamA: Stream<A>, streamB: Stream<B>, streamC: Stream<C>, f: (A) -> (B) -> (C) -> D): Stream<D> =
        liftA3(f)(streamA)(streamB)(streamC)



    // Monad extension functions:

    /**
     * Lift a function to a monad.
     */
    fun <A, B> liftM(f: (A) -> B): (Stream<A>) -> Stream<B> =
        {streamA: Stream<A> ->
            streamA.bind{a: A -> singleton(f(a))}
        }   // liftM

    /**
     * Lift a binary function to a monad.
     */
    fun <A, B, C> liftM2(f: (A) -> (B) -> C): (Stream<A>) -> (Stream<B>) -> Stream<C> =
        {streamA: Stream<A> ->
            {streamB: Stream<B> ->
                streamA.bind{a: A -> streamB.bind{b: B -> singleton(f(a)(b))}}
            }
        }   // liftM2

    fun <A, B, C> liftM2(f: (A, B) -> C): (Stream<A>) -> (Stream<B>) -> Stream<C> =
        liftM2(C2(f))

    /**
     * Lift a ternary function to a monad.
     */
    fun <A, B, C, D> liftM3(f: (A) -> (B) -> (C) -> D): (Stream<A>) -> (Stream<B>) -> (Stream<C>) -> Stream<D> =
        {streamA: Stream<A> ->
            {streamB: Stream<B> ->
                {streamC: Stream<C> ->
                    streamA.bind{a: A -> streamB.bind{b: B -> streamC.bind{c: C -> singleton(f(a)(b)(c))}}}
                }
            }
        }   // liftM3

    fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (Stream<A>) -> (Stream<B>) -> (Stream<C>) -> Stream<D> =
        liftM3(C3(f))

}   // StreamF
