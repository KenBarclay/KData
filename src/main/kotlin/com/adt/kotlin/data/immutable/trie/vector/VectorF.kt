package com.adt.kotlin.data.immutable.trie.vector

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

import com.adt.kotlin.data.immutable.trie.vector.node.*

import com.adt.kotlin.data.immutable.list.*
import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.ListF

import kotlin.collections.List as KList



object VectorF {

    /**
     * The empty vector.
     *
     * @return                  an empty vector
     */
    fun <A> empty(): VectorIF<A> = Vector(NodeF.empty())

    /**
     * Make a vector with one element.
     *
     * @param a                     new element
     * @return                      new vector with that one element
     */
    fun <A> singleton(a: A): VectorIF<A> = Vector(NodeF.singleton(a))



    fun <A> of(): VectorIF<A> = empty()

    fun <A> of(a0: A): VectorIF<A> = singleton(a0)

    fun <A> of(a0: A, a1: A): VectorIF<A> = empty<A>().append(a0).append(a1)

    fun <A> of(a0: A, a1: A, a2: A): VectorIF<A> = empty<A>().append(a0).append(a1).append(a2)

    fun <A> of(a0: A, a1: A, a2: A, a3: A): VectorIF<A> = empty<A>().append(a0).append(a1).append(a2).append(a3)

    fun <A> of(a0: A, a1: A, a2: A, a3: A, a4: A): VectorIF<A> = empty<A>().append(a0).append(a1).append(a2).append(a3).append(a4)

    fun <A> of(vararg seq: A): VectorIF<A> = fromSequence(*seq)



    /**
     * Convert a variable-length parameter series into a vector.
     *
     * @param seq                   variable-length parameter series
     * @return                      vector of the given values
     */
    fun <A> fromSequence(vararg seq: A): VectorIF<A> = Vector(NodeF.fromSequence(*seq))

    /**
     * Convert an array into a vector.
     *
     * @param array                 array of values
     * @return                      immutable vector of the given values
     */
    fun <A> from(array: Array<A>): VectorIF<A> = Vector(NodeF.from(array))

    /**
     * Convert an array list into a vector.
     *
     * @param xs                    list of values
     * @return                      immutable vector of the given values
     */
    fun <A> from(xs: KList<A>): VectorIF<A> = Vector(NodeF.from(xs))

    /**
     * Convert an immutable list into a vector.
     *
     * @param xs                    immutable list of values
     * @return                      immutable vector of the given values
     */
    fun <A> from(xs: List<A>): VectorIF<A> = Vector(NodeF.from(xs))

    /**
     * Convert an immutable vector to aa array.
     *
     * @param vec                   existing immutable vector
     * @return                      an array
     */
    inline fun <reified A> toArray(vec: VectorIF<A>): Array<A> =
            vec.foldLeft(arrayOf<A>()){ar, a -> ar + a}

    /**
     * Convert an immutable vector to a list.
     *
     * @param vec                   existing immutable vector
     * @return                      a list
     */
    fun <A> toList(vec: VectorIF<A>): KList<A> =
            vec.foldLeft(listOf()){xs, v -> xs + v}

    /**
     * Convert an immutable vector to an immutable list.
     *
     * @param vec                   existing immutable vector
     * @return                      an immutable list
     */
    fun <A> toKList(vec: VectorIF<A>): List<A> =
            vec.foldLeft(ListF.empty()){xs, a -> xs.append(a)}

    /**
     * Returns a vector of integers starting with the given from value and
     *   ending with the given to value (exclusive).
     *
     * Examples:
     *   range(1, 7) = [1, 2, 3, 4, 5, 6]
     *   range(1, 7, 0) = exception
     *   range(7, 1, 1) = exception
     *   range(1, 7, -1) = exception
     *
     * @param from                  the minimum value for the vector (inclusive)
     * @param to                    the maximum value for the vector (exclusive)
     * @param step                  increment
     * @return                      the vector of integers from => to (exclusive)
     */
    fun range(from: Int, to: Int, step: Int = 1): VectorIF<Int> {
        if (step == 0)
            throw VectorException("Zero step disallowed: from: $from to: $to step: $step")
        if (step > 0 && from >= to)
            throw VectorException("Positive step requires from >= to: from: $from to: $to step: $step")
        if (step < 0 && from <= to)
            throw VectorException("Negative step requires from <= to: from: $from to: $to step: $step")

        tailrec
        fun recRange(from: Int, to: Int, step: Int, acc: VectorIF<Int>): VectorIF<Int> {
            return if (step >0 && from >= to)
                acc
            else if (step < 0 && from <= to)
                acc
            else
                recRange(from + step, to, step, acc.append(from))
        }   // recRange

        return recRange(from, to, step, empty())
    }   // range

    /**
     * Returns a vector of doubles starting with the given from value and
     *   ending with the given to value (exclusive).
     *
     * Examples:
     *   range(1.0, 7.0) = [1.0, 2.0, 3.0, 4.0, 5.0, 6.0]
     *   range(1, 7, 0) = exception
     *   range(7, 1, 1) = exception
     *   range(1, 7, -1) = exception
     *
     * @param from                  the minimum value for the vector (inclusive)
     * @param to                    the maximum value for the vector (exclusive)
     * @param step                  increment
     * @return                      the vector of integers from => to (exclusive)
     */
    fun range(from: Double, to: Double, step: Double = 1.0): VectorIF<Double> {
        if (Math.abs(step) < 1e-10)
            throw VectorException("Zero step disallowed: from: $from to: $to step: $step")
        if (step > 0 && from >= to)
            throw VectorException("Positive step requires from >= to: from: $from to: $to step: $step")
        if (step < 0 && from <= to)
            throw VectorException("Negative step requires from <= to: from: $from to: $to step: $step")

        tailrec
        fun recRange(from: Double, to: Double, step: Double, acc: VectorIF<Double>): VectorIF<Double> {
            return if (step >0 && from >= to)
                acc
            else if (step < 0 && from <= to)
                acc
            else
                recRange(from + step, to, step, acc.append(from))
        }   // recRange

        return recRange(from, to, step, empty())
    }   // range



    /**
     * Produce a vector with n copies of the element v. Throws a
     *   VectorException if the copies argument is negative.
     *
     * Examples:
     *   replicate(5, 7) = [7, 7, 7, 7, 7]
     *   replicate(0, 7) = []
     *   replicate(-9, 7) = exception
     *
     * @param n                     number of copies required
     * @param a                     element to be copied
     * @return                      vector of the copied element
     */
    fun <A> replicate(n: Int, a: A): VectorIF<A> {
        tailrec
        fun recReplicate(m: Int, a: A, acc: VectorIF<A>): VectorIF<A> {
            return if (m == 0)
                acc
            else
                recReplicate(m - 1, a, acc.append(a))
        }   // recReplicate

        return if (n < 0)
            throw VectorException("replicate: number is negative")
        else
            recReplicate(n, a, empty<A>())
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
    fun <A> replicate(n: Int, xs: VectorIF<A>): VectorIF<A> {
        tailrec
        fun recReplicate(n: Int, xs: List<A>, acc: ListBufferIF<A>): List<A> {
            return if (n == 0)
                acc.toList()
            else
                recReplicate(n - 1, xs, acc.append(xs))
        }   // recReplicate

        return if (n < 0)
            throw VectorException("replicate: number is negative")
        else
            from(recReplicate(n, xs.toList(), ListBuffer()))
    }   // replicate

    /**
     * shallowFlatten is used to flatten a nested vector structure. The
     *   shallowFlatten does not recurse into sub-vectors, eg:
     *
     *   shallowFlatten([[1, 2, 3], [4, 5]]) = [1, 2, 3, 4, 5]
     *   shallowFlatten([[[1, 2], [3]], [[4, 5]]]) = [[1, 2], [3], [4, 5]]
     *
     * shallowFlatten:: Vector[Vector[A]] -> Vector[A]
     *
     * @param xss                   existing list of lists
     * @return                      new list of flattened list
     */
    fun <A> shallowFlatten(xss: VectorIF<VectorIF<A>>): VectorIF<A> {
        val appendC: (VectorIF<A>) -> (VectorIF<A>) -> VectorIF<A> = {xs: VectorIF<A> -> {ys: VectorIF<A> -> xs.append(ys)}}
        return xss.foldLeft(empty<A>(), appendC)
    }

    /**
     * Transform a vector of pairs into a vector of first components and a vector of second components.
     *
     * Examples:
     *   unzip([(1, 2), (3, 4), (5, 6)]) = ([1, 3, 5], [2, 4, 6])
     *
     * @param xs                    vector of pairs
     * @return                      pair of vectors
     */
    fun <A, B> unzip(xs: VectorIF<Pair<A, B>>): Pair<VectorIF<A>, VectorIF<B>> =
            xs.foldLeft(Pair(empty<A>(), empty<B>())){pair: Pair<VectorIF<A>, VectorIF<B>>, pr: Pair<A, B> -> Pair(pair.first.append(pr.first), pair.second.append(pr.second))}



    // Functor extension functions:

    /**
     * Lift a function into the List context.
     */
    fun <A, B> lift(f: (A) -> B): (VectorIF<A>) -> VectorIF<B> =
        {va: VectorIF<A> ->
            va.fmap(f)
        }




    // Applicative extension functions:

    /**
     * Lift a function to actions.
     */
    fun <A, B> liftA(f: (A) -> B): (VectorIF<A>) -> VectorIF<B> =
        {va: VectorIF<A> ->
            va.ap(singleton((f)))
        }   // liftA

    /**
     * Lift a binary function to actions.
     *
     * Some functors support an implementation of lift2 that is more efficient than the
     *   default one. In particular, if fmap is an expensive operation, it is likely
     *   better to use liftA2 than to fmap over the structure and then use ap.
     */
    fun <A, B, C> liftA2(f: (A) -> (B) -> C): (VectorIF<A>) -> (VectorIF<B>) -> VectorIF<C> =
        {va: VectorIF<A> ->
            {vb: VectorIF<B> ->
                vb.ap(va.fmap(f))
            }
        }   // liftA2

    /**
     * Lift a ternary function to actions.
     */
    fun <A, B, C, D> liftA3(f: (A) -> (B) -> (C) -> D): (VectorIF<A>) -> (VectorIF<B>) -> (VectorIF<C>) -> VectorIF<D> =
        {va: VectorIF<A> ->
            {vb: VectorIF<B> ->
                {vc: VectorIF<C> ->
                    vc.ap(vb.ap(va.fmap(f)))
                }
            }
        }   // liftA3

    /**
     * Execute a binary function.
     */
    fun <A, B, C> mapA2(va: VectorIF<A>, vb: VectorIF<B>, f: (A) -> (B) -> C): VectorIF<C> =
        liftA2(f)(va)(vb)

    /**
     * Execute a ternary function.
     */
    fun <A, B, C, D> mapA3(va: VectorIF<A>, vb: VectorIF<B>, vc: VectorIF<C>, f: (A) -> (B) -> (C) -> D): VectorIF<D> =
        liftA3(f)(va)(vb)(vc)



    // Monad extension functions:

    /**
     * Lift a function to a monad.
     */
    fun <A, B> liftM(f: (A) -> B): (VectorIF<A>) -> VectorIF<B> =
        {va: VectorIF<A> ->
            va.bind{a: A -> singleton(f(a))}
        }   // liftM

    /**
     * Lift a binary function to a monad.
     */
    fun <A, B, C> liftM2(f: (A) -> (B) -> C): (VectorIF<A>) -> (VectorIF<B>) -> VectorIF<C> =
        {va: VectorIF<A> ->
            {vb: VectorIF<B> ->
                va.bind{a: A -> vb.bind{b: B -> singleton(f(a)(b))}}
            }
        }   // liftM2

    /**
     * Lift a ternary function to a monad.
     */
    fun <A, B, C, D> liftM3(f: (A) -> (B) -> (C) -> D): (VectorIF<A>) -> (VectorIF<B>) -> (VectorIF<C>) -> VectorIF<D> =
        {va: VectorIF<A> ->
            {vb: VectorIF<B> ->
                {vc: VectorIF<C> ->
                    va.bind{a: A -> vb.bind{b: B -> vc.bind{c: C -> singleton(f(a)(b)(c))}}}
                }
            }
        }   // liftM3

}   // VectorF
