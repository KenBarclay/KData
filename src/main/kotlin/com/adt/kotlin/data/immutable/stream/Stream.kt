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



import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.ListF

import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.OptionF.none
import com.adt.kotlin.data.immutable.option.OptionF.some

import com.adt.kotlin.hkfp.fp.FunctionF.C2


sealed class Stream<out A> {



    object Nil : Stream<Nothing>()



    class Cons<out A> internal constructor(val hd: A, val tl: () -> Stream<A>) : Stream<A>()



    /**
     * Determine if this stream contains the element determined by the predicate.
     *
     * Terminal operation.
     *
     * Examples:
     *   [1, 2, 3, 4].contains{n -> (n == 4)} = true
     *   [1, 2, 3, 4].contains{n -> (n == 5)} = false
     *   [].contains{n -> (n == 4)} = false
     *
     * @param predicate         search predicate
     * @return                  true if search element is present, false otherwise
     */
    fun contains(predicate: (A) -> Boolean): Boolean {
        return when (this) {
            is Nil -> false
            is Cons -> if (predicate(this.hd))
                true
            else
                this.tl().contains(predicate)
        }
    }   // contains

    /**
     * Count the number of times a value appears in this stream matching the criteria.
     *
     * Terminal operation.
     *
     * Examples:
     *   [1, 2, 3, 4].count{n -> (n == 2)} = 1
     *   [1, 2, 3, 4].count{n -> (n == 5)} = 0
     *   [].count{n -> (n == 2)} = 0
     *   [1, 2, 1, 2, 2].count{n -> (n == 2)} == 3
     *
     * @param predicate         the search criteria
     * @return                  the number of occurrences
     */
    fun count(predicate: (A) -> Boolean): Int {
        return when (this) {
            is Nil -> 0
            is Cons -> if (predicate(this.hd))
                1 + this.tl().count(predicate)
            else
                this.tl().count(predicate)
        }
    }   // count

    /**
     * Drop the first n elements from this stream and return a stream containing the
     *   remainder. If n is negative or zero then this stream is returned. The result
     *   stream is a suffix of this stream.
     *
     * Intermediate operation.
     *
     * Examples:
     *   [1, 2, 3, 4].drop(2) = [3, 4]
     *   [1, 2, 3, 4].drop(0) = [1, 2, 3, 4]
     *   [1, 2, 3, 4].drop(5) = []
     *   [].drop(2) = []
     *
     * @param n                 number of elements to skip
     * @return                  new stream of remaining elements
     */
    fun drop(n: Int): Stream<A> {
        return if (n == 0)
            this
        else when (this) {
            is Nil -> Nil
            is Cons -> this.tl().drop(n - 1)
        }
    }   // drop

    /**
     * Function dropUntil removes the leading elements from this stream until a match
     *   against the predicate. The result stream size will not exceed this stream size.
     *   The result stream is a suffix of this stream.
     *
     * Intermediate operation.
     *
     * Examples:
     *   [1, 2, 3, 4].dropUntil{n -> (n <= 2)} = [1, 2, 3, 4]
     *   [1, 2, 3, 4, 5].dropUntil{n -> (n > 3)} = [4, 5]
     *   [1, 2, 3, 4].dropUntil{n -> (n <= 5)} = [1, 2, 3, 4]
     *   [1, 2, 3, 4].dropUntil{n -> (n <= 0)} = []
     *   [].dropUntil{n -> (n <= 2)} = []
     *
     * @param predicate         criteria
     * @return                  new list of remaining elements
     */
    fun dropUntil(predicate: (A) -> Boolean): Stream<A> {
        return when (this) {
            is Nil -> Nil
            is Cons -> if (predicate(this.hd))
                this
            else
                this.tl().dropUntil(predicate)
        }
    }   // dropUntil

    /**
     * Function dropWhile removes the leading elements from this stream that matches
     *   some predicate. The result stream size will not exceed this stream size.
     *   The result stream is a suffix of this stream.
     *
     * Intermediate operation.
     *
     * Examples:
     *   [1, 2, 3, 4].dropWhile{n -> (n <= 2)} = [3, 4]
     *   [1, 2, 3, 4].dropWhile{n -> (n <= 5)} = []
     *   [1, 2, 3, 4].dropWhile{n -> (n <= 0)} = [1, 2, 3, 4]
     *   [].dropWhile{n -> (n <= 2)} = []
     *
     * @param predicate         criteria
     * @return                  new list of remaining elements
     */
    fun dropWhile(predicate: (A) -> Boolean): Stream<A> {
        return when (this) {
            is Nil -> Nil
            is Cons -> if (predicate(this.hd))
                this.tl().dropWhile(predicate)
            else
                this
        }
    }   // dropWhile

    /**
     * Are two streams equal?
     *
     * Terminal operation.
     *
     * @param other             the other stream
     * @return                  true if both streams are the same; false otherwise
     */
    override fun equals(other: Any?): Boolean {
        fun recEquals(stream1: Stream<A>, stream2: Stream<A>): Boolean {
            return when (stream1) {
                is Nil -> when (stream2) {
                    is Nil -> true
                    is Cons -> false
                }
                is Cons -> when (stream2) {
                    is Nil -> false
                    is Cons -> if (stream1.hd == stream2.hd)
                        recEquals(stream1.tl(), stream2.tl())
                    else
                        false
                }
            }
        }   // recEquals

        return if (this === other)
            true
        else if (other == null || this::class.java != other::class.java)
            false
        else {
            @Suppress("UNCHECKED_CAST") val otherStream: Stream<A> = other as Stream<A>
            recEquals(this, otherStream)
        }
    }   // equals

    /**
     * Function filter selects the items from this stream that match the
     *   criteria specified by the function parameter. This is known as the
     *   predicate function, and delivers a boolean result. The elements
     *   of the result stream are in the same order as the original.
     *
     * Intermediate operation.
     *
     * Examples:
     *   [].filter{n -> (n % 2 == 0} = []
     *   [1, 2, 3, 4, 5].filter{n -> (n % 2 == 0} = [2, 4]
     *   [1, 3, 5, 7].filter{n -> (n % 2 == 0} = []
     *
     * @param predicate         criteria
     * @return                  new stream of matching elements
     */
    fun filter(predicate: (A) -> Boolean): Stream<A> = when (this) {
        is Nil -> Nil
        is Cons -> if (predicate(this.hd))
            Cons(this.hd){ -> this.tl().filter(predicate)}
        else
            this.tl().filter(predicate)
    }   // filter

    /**
     * The find function takes a predicate and returns the first
     *   element in the stream matching the predicate wrapped in a Some,
     *   or None if there is no such element.
     *
     * Terminal operation.
     *
     * Examples:
     *   [1, 2, 3, 4].find{n -> (n > 2)} = Some(3)
     *   [1, 2, 3, 4].find{n -> (n > 5)} = None
     *   [].find{n -> (n > 2)} = None()
     *
     * @param predicate         criteria
     * @return                  matching element, if found
     */
    fun find(predicate: (A) -> Boolean): Option<A> {
        return when (this) {
            is Nil -> none()
            is Cons -> if (predicate(this.hd))
                some(this.hd)
            else
                this.tl().find(predicate)
        }
    }   // find

    /**
     * foldLeft is a higher-order function that folds a binary function into this
     *   stream of values. Effectively:
     *
     *   foldLeft(e, [x1, x2, ..., xn], f) = (...((e f x1) f x2) f...) f xn
     *
     * Terminal operation.
     *
     * Examples:
     *   [1, 2, 3, 4].foldLeft(0){m -> {n -> m + n}} = 10
     *   [].foldLeft(0){m -> {n -> m + n}} = 0
     *   [1, 2, 3, 4].foldLeft([]){list -> {elem -> list.append(elem)}} = [1, 2, 3, 4]
     *
     * @param e                 initial value
     * @param f                 curried binary function:: B -> A -> B
     * @return                  folded result
     */
    fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B {
        return when (this) {
            is Nil -> e
            is Cons -> this.tl().foldLeft(f(e)(this.hd), f)
        }
    }   // foldLeft

    fun <B> foldLeft(e: B, f: (B, A) -> B): B = this.foldLeft(e, C2(f))

    /**
     * foldRight is a higher-order function that folds a binary function into this
     *   stream of values. Fold functions can be the implementation for many other
     *   functions. Effectively:
     *
     *   foldRight(e, [x1, x2, ..., xn], f) = x1 f (x2 f ... (xn f e)...)
     *
     * Terminal operation.
     *
     * Examples:
     *   [1, 2, 3, 4].foldRight(1){m -> {n -> m * n}} = 24
     *   [].foldRight(1){m -> {n -> m * n}} = 1
     *   [1, 2, 3, 4].foldRight([]){elem -> {list -> cons(elem, list)}} = [1, 2, 3, 4]
     *
     * @param e                 initial value
     * @param f                 curried binary function:: A -> B -> B
     * @return                  folded result
     */
    fun <B> foldRight(e: B, f: (A) -> (B) -> B): B {
        return when (this) {
            is Nil -> e
            is Cons -> f(this.hd)(this.tl().foldRight(e, f))
        }
    }   // foldRight

    fun <B> foldRight(e: B, f: (A, B) -> B): B = this.foldRight(e, C2(f))

    /**
     * All the elements of this stream meet some criteria. If the stream is empty then
     *    true is returned.
     *
     *  Terminal operation.
     *
     * Examples:
     *   [1, 2, 3, 4].forAll{m -> (m > 0)} = true
     *   [1, 2, 3, 4].forAll{m -> (m > 2)} = false
     *   [1, 2, 3, 4].forAll{m -> true} = true
     *   [1, 2, 3, 4].forAll{m -> false} = false
     *   [].forAll{m -> false} = true
     *   [].forAll{m -> (m > 0)} = true
     *
     * @param predicate         criteria
     * @return                  true if all elements match criteria
     */
    fun forAll(predicate: (A) -> Boolean): Boolean {
        return when (this) {
            is Nil -> true
            is Cons -> if (predicate(this.hd))
                this.tl().forAll(predicate)
            else
                false
        }
    }   // forAll

    /**
     * Apply the block to each element in the stream.
     *
     * Terminal operation.
     *
     * @param block                 body of program block
     */
    fun forEach(block: (A) -> Unit): Unit {
        when (this) {
            is Nil -> Unit
            is Cons -> {
                block(this.hd)
                this.tl().forEach(block)
            }
        }
    }   // forEach

    /**
     * Return the element at the specified position in this stream, where
     *   index 0 denotes the first element.
     * Throws a StreamException if the index is out of bounds, i.e. if
     *   index does not satisfy 0 <= index < length.
     *
     * Terminal operation.
     *
     * Examples:
     *   [1, 2, 3, 4].get(0) = 1
     *   [1, 2, 3, 4].get(3) = 4
     *   [1, 2, 3, 4][2] = 3
     *
     * @param index             position in stream
     * @return                  the element at the specified position in the stream
     */
    operator fun get(index: Int): A {
        tailrec
        fun recGet(stream: Stream<A>, idx: Int): A {
            return if (idx < 0)
                throw StreamException("Stream.get: negative index: $idx")
            else when (stream) {
                is Nil -> throw StreamException("Stream.get: empty stream")
                is Cons -> if (idx == 0)
                    stream.hd
                else
                    recGet(stream.tl(), idx - 1)
            }
        }   // recGet

        return recGet(this, index)
    }   // get

    /**
     * Extract the first element of this stream, which must be non-empty.
     *   Throws a StreamException on an empty stream.
     *
     * Terminal operation.
     *
     * Examples:
     *   [1, 2, 3, 4].head() = 1
     *   [5].head() = 5
     *
     * @return                  the element at the front of the stream
     */
    fun head(): A = when (this) {
        is Nil -> throw StreamException("Stream.head: empty stream")
        is Cons -> this.hd
    }   // head

    /**
     * Find the index of the first occurrence of the given value, or -1 if absent.
     *
     * Terminal operation.
     *
     * Examples:
     *   [1, 2, 3, 4].indexOf{n -> (n == 1)} = 0
     *   [1, 2, 3, 4].indexOf{n -> (n == 3)} = 2
     *   [1, 2, 3, 4].indexOf{n -> (n == 5)} = -1
     *   [].indexOf{n -> (n == 2)} = -1
     *
     * @param predicate         the search predicate
     * @return                  the index position
     */
    fun indexOf(predicate: (A) -> Boolean): Int {
        fun recIndexOf(stream: Stream<A>, predicate: (A) -> Boolean, acc: Int): Int {
            return when (stream) {
                is Nil -> -1
                is Cons -> if (predicate(stream.hd)) acc else recIndexOf(stream.tl(), predicate, 1 + acc)
            }
        }   // recIndexOf

        return recIndexOf(this, predicate, 0)
    }   // indexOf

    /**
     * Return all the elements of this stream except the last one. The stream must be non-empty.
     *   Throws a StreamException on an empty stream.
     *
     * Intermediate operation.
     *
     * Examples:
     *   [1, 2, 3, 4].init() = [1, 2, 3]
     *   [5].init() = []
     *
     * @return                  new stream of the initial elements
     */
    fun init(): Stream<A> {
        return when (this) {
            is Nil -> throw StreamException("Stream.init: empty stream")
            is Cons -> if (this.tl().isEmpty())
                Nil
            else
                Cons(this.hd){-> this.tl().init()}
        }
    }   // init

    /**
     * The inits function returns all initial segments of this stream,
     *   shortest first. The result stream size will exceed this stream
     *   size by one. The first element of the result stream is guaranteed
     *   to be the empty sub-stream; the final element of the result stream
     *   is guaranteed to be the same as the original stream. All sub-streams
     *   of the result stream are a prefix to the original.
     *
     * Terminal operation.
     *
     * Expensive operation on large stream.
     *
     * Examples:
     *   [1, 2, 3].inits() = [[], [1], [1, 2], [1, 2, 3]]
     *   [].inits() = [[]]
     *   [1, 2, 3].inits().size() = 1 + [1, 2, 3].size()
     *   [1, 2, 3].inits().head() = []
     *   [1, 2, 3].inits().last() = [1, 2, 3]
     *
     * @return                  new list of initial segment sub-lists
     */
    fun inits(): Stream<Stream<A>> {
        val nil: Stream<Stream<A>> = Cons(StreamF.empty()){-> Nil}
        return when (this) {
            is Nil -> nil
            is Cons -> nil.append(this.tl().inits().map{stream -> Cons(this.head()){-> stream}})
        }
    }   // inits

    /**
     * Return true if all the elements differ, otherwise false.
     *
     * Expensive operation on large stream.
     *
     * Terminal operation.
     *
     * Examples:
     *   [1, 2, 3, 4].isDistinct() = true
     *   [].isDistinct() = true
     *   [1, 2, 3, 4, 1].isDistinct() = false
     *
     * @return                  true if all the elements are distinct
     */
    fun isDistinct(): Boolean {
        return when (this) {
            is Nil -> true
            is Cons -> {
                val head: A = this.hd
                val tail: Stream<A> = this.tl()
                if (tail.contains(head))
                    false
                else
                    tail.isDistinct()
            }
        }
    }   // isDistinct

    /**
     * Test whether this stream is empty.
     *
     * Terminal operation.
     *
     * Examples:
     *   [1, 2, 3, 4].isEmpty() = false
     *   [].isEmpty() = true
     *
     * @return                  true if stream is empty; false otherwise
     */
    fun isEmpty(): Boolean =
        when (this) {
            is Nil -> true
            is Cons -> false
        }   // isEmpty

    /**
     * Extract the last element of this stream, which must be non-empty.
     *   Throws a StreamException on an empty stream.
     *
     * Terminal operation.
     *
     * Examples:
     *   [1, 2, 3, 4].last() = 4
     *   [5].last() = 5
     *
     * @return                  final element in the stream
     */
    fun last(): A {
        return when (this) {
            is Nil -> throw StreamException("Stream.last: empty stream")
            is Cons -> {
                val head: A = this.hd
                val tail: Stream<A> = this.tl()
                when (tail) {
                    is Nil -> head
                    is Cons -> tail.last()
                }
            }
        }
    }   // last

    /**
     * Obtain the length of this stream.
     *
     * Terminal operation.
     *
     * Examples:
     *   [1, 2, 3, 4].length() == 4
     *   [].length() = 0
     *
     * @return                  number of elements in the stream
     */
    fun length(): Int {
        return when (this) {
            is Nil -> 0
            is Cons -> 1 + this.tl().length()
        }
    }   // length

    /**
     * Compose all the elements of this stream as a string using the separator, prefix, postfix, etc.
     *
     * Terminal operation.
     *
     * Examples:
     *   [1, 2, 3, 4].makeString() = "1, 2, 3, 4"
     *   [1, 2, 3, 4].makeString(", ", "[", "]") = "[1, 2, 3, 4]"
     *   [1, 2, 3, 4].makeString(", ", "[", "]", 2) = "[1, 2, ...]"
     *
     * @param separator         the separator between each element
     * @param prefix            the leading content
     * @param postfix           the trailing content
     * @param limit             constrains the output to the fist limit elements
     * @param truncated         indicator that the output has been limited
     * @return                  the stream content
     */
    fun makeString(separator: String = ", ", prefix: String = "", postfix: String = "", limit: Int = -1, truncated: String = "..."): String {
        val buffer: StringBuffer = StringBuffer()
        var count = 0
        var skip: Boolean = false

        buffer.append(prefix)
        this.forEach {elem ->
            val element: A = elem
            if (++count > 1 && !skip) buffer.append(separator)
            if ((limit < 0 || count <= limit) && !skip) {
                val text: String = if (element == null) "null" else element.toString()
                buffer.append(text)
            } else
                skip = true
        }
        if (limit in 0 until count) buffer.append(truncated)
        buffer.append(postfix)
        return buffer.toString()
    }   // makeString

    /**
     * Function map applies the function parameter to each item in this stream, delivering
     *   a new stream. The result stream has the same size as this stream.
     *
     * Intermediate operation.
     *
     * Examples:
     *   [1, 2, 3, 4].map{n -> n + 1} = [2, 3, 4, 5]
     *   [].map{n -> n + 1} = []
     *
     * @param f                 pure function:: A -> B
     * @return                  new list of transformed values
     */
    fun <B> map(f: (A) -> B): Stream<B> = when (this) {
        is Nil -> Nil
        is Cons -> Cons(f(this.hd)){-> this.tl().map(f)}
    }   // map

    /**
     * Return a stream of pairs of the adjacent elements from this stream.
     *
     * Examples:
     *   [1, 2, 3, 4].pairwise() = [(1, 2), (2, 3), (3, 4)]
     *   [3, 4].pairwise() = [(3, 4)]
     *   [4].pairwise() = []
     *   [].pairwise() = []
     *
     * @return                  stream of adjacent pairs
     */
    fun pairwise(): Stream<Pair<A, A>> = when(this) {
        is Nil -> Nil
        is Cons -> this.zip(this.tail())
    }   // pairwise

    /**
     * Remove the first occurrence of the matching element from this stream. The result stream
     *   will either have the same size as this stream (if no such element is present) or
     *   will have the size of this stream less one.
     *
     * Intermediate operation.
     *
     * Examples:
     *   [1, 2, 3, 4].remove{n -> (n == 4)} = [1, 2, 3]
     *   [1, 2, 3, 4].remove{n -> (n == 5)} = [1, 2, 3, 4]
     *   [4, 4, 4, 4].remove{n -> (n == 4)} = [4, 4, 4]
     *   [].remove{n -> (n == 4)} = []
     *
     * @param predicate         search predicate
     * @return                  new stream with element deleted
     */
    fun remove(predicate: (A) -> Boolean): Stream<A> {
        return when (this) {
            is Nil -> Nil
            is Cons -> if (predicate(this.hd))
                this.tl()
            else
                Cons(this.hd){-> this.tl().remove(predicate)}
        }
    }   // remove

    /**
     * The removeAll function removes all the elements from this stream that match
     *   a given criteria. The result stream size will not exceed this stream size.
     *
     * Intermediate operation.
     *
     * Examples:
     *   [1, 2, 3, 4].removeAll{n -> (n % 2 == 0)} = [1, 3]
     *   [1, 2, 3, 4].removeAll{n -> (n > 4)} = [1, 2, 3, 4]
     *   [1, 2, 3, 4].removeAll{n -> (n > 0)} = []
     *   [].removeAll{n -> (n % 2 == 0)} = []
     *   [1, 4, 2, 3, 4].removeAll{n -> (n == 4)} = [1, 2, 3]
     *   [4, 4, 4, 4, 4].removeAll{n -> (n == 4)} = []
     *
     * @param predicate		    criteria
     * @return          		new stream with all matching elements removed
     */
    fun removeAll(predicate: (A) -> Boolean): Stream<A> = this.filter{x: A -> !predicate(x)}

    /**
     * The removeDuplicates function removes duplicate elements from this stream.
     *   In particular, it keeps only the first occurrence of each element. The
     *   size of the result stream is either less than or equal to the original.
     *   The elements in the result stream are all drawn from the original. The
     *   elements in the result stream are in the same order as found in the original.
     *
     * Intermediate operation.
     * Expensive operation on large stream.
     *
     * Examples:
     *   [1, 2, 1, 2, 3].removeDuplicates = [1, 2, 3]
     *   [1, 1, 3, 2, 1, 3, 2, 4].removeDuplicates = [1, 3, 2, 4]
     *   [].removeDuplicates = []
     *
     * @return                  new stream with all duplicates removed
     */
    fun removeDuplicates(): Stream<A> {
        fun recRemoveDuplicates(stream: Stream<A>, acc: Stream<A>): Stream<A> {
            return when (stream) {
                is Nil -> acc
                is Cons -> if (acc.contains(stream.hd))
                    recRemoveDuplicates(stream.tl(), acc)
                else
                    recRemoveDuplicates(stream.tl(), Cons(stream.hd){-> acc})
            }
        }   //recRemoveDuplicates

        return recRemoveDuplicates(this, StreamF.empty()).reverse()
    }   // removeDuplicates

    /**
     * Reverses the content of this stream into a new stream. The size of the result stream
     *   is the same as this stream.
     *
     * Intermediate operation.
     *
     * Examples:
     *   [1, 2, 3, 4].reverse() = [4, 3, 2, 1]
     *   [1].reverse() = [1]
     *   [].reverse() = []
     *
     * @return                  new list of elements reversed
     */
    fun reverse(): Stream<A> =
        this.foldLeft(StreamF.empty()){stream -> {el -> Cons(el){-> stream}}}

    /**
     * Obtain the size of this stream; equivalent to length.
     *
     * Terminal operation.
     *
     * Examples:
     *   [1, 2, 3, 4].size() == 4
     *   [].size() == 0
     *
     * @return                  number of elements in the stream
     */
    fun size(): Int = this.length()

    /**
     * Return a new stream that is a sub-stream of this stream. The sub-stream begins at
     *   the specified from and extends to the element at index to - 1. Thus the
     *   length of the sub-stream is to-from. Degenerate slice indices are handled
     *   gracefully: an index that is too large is replaced by the stream size, an
     *   upper bound smaller than the lower bound returns an empty stream. The size
     *   of the result stream does not exceed the size of this stream. The result stream
     *   is an infix of this stream (or equivalently, a permutation of the stream).
     *
     * Examples:
     *   [1, 2, 3, 4].slice(0, 2) = [1, 2]
     *   [1, 2, 3, 4].slice(2, 2) = []
     *   [1, 2, 3, 4].slice(2, 0) = []
     *   [1, 2, 3, 4].slice(0, 7) = [1, 2, 3, 4]
     *   [].slice(0, 2) = []
     *
     * @param from              the start index, inclusive
     * @param to                the end index, exclusive
     * @return                  the sub-stream of this stream
     */
    fun slice(from: Int, to: Int): Stream<A>  = this.drop(from).take(to - from)

    /**
     * Sort the elements of this stream into ascending order and deliver
     *   the resulting stream. The elements are compared using the given
     *   comparator.
     *
     * Examples:
     *   [4, 3, 2, 1].sort{x, y -> if (x < y) -1 else if (x > y) +1 else 0} = [1, 2, 3, 4]
     *   [1, 2, 3, 4].sort{x, y -> if (x < y) -1 else if (x > y) +1 else 0} = [1, 2, 3, 4]
     *   [].sort{x, y -> if (x < y) -1 else if (x > y) +1 else 0} = []
     *   ["Ken", "John", "Jessie", "", ""].sort{str1, str2 -> str1.compareTo(str2)} = ["", "", "Jessie", "John", "Ken"]
     *
     * @param comparator        element comparison function
     * @return                  the sorted stream
     */
    fun sort(comparator: (A, A) -> Int): Stream<A> {
        fun merge(stream1: Stream<A>, stream2: Stream<A>): Stream<A> {  // merge two individually sorted streams
            return when (stream1) {
                is Nil -> stream2
                is Cons -> when (stream2) {
                    is Nil -> stream1
                    is Cons -> {
                        val head1: A = stream1.hd
                        val head2: A = stream2.hd
                        if (comparator(head1, head2) > 0)
                            Cons(head2){-> merge(stream1, stream2.tl())}
                        else
                            Cons(head1){-> merge(stream1.tl(), stream2)}
                    }
                }
            }
        }   // merge

        fun mergePairs(streams: Stream<Stream<A>>): Stream<Stream<A>> { // merges individually sorted streams two at a time
            return if (streams.isEmpty() || streams.tail().isEmpty())
                streams
            else {
                val tail: Stream<Stream<A>> = streams.tail()
                Cons(merge(streams.head(), tail.head())){-> mergePairs(tail.tail())}
            }
        }   // mergePairs

        fun mergeSort(streams: Stream<Stream<A>>): Stream<A> {
            return when (streams) {
                is Nil -> Nil
                is Cons -> {
                    var ss: Stream<Stream<A>> = streams
                    while (!ss.tail().isEmpty()) {
                        ss = mergePairs(ss)
                    }
                    ss.head()
                }
            }
        }   // mergeSort

        return mergeSort(this.map{a: A -> Cons(a){-> Nil}})
    }   // sort

    /**
     * span applied to a predicate and a stream xs, returns a tuple where
     *   the first element is longest prefix (possibly empty) of xs of elements
     *   that satisfy predicate and second element is the remainder of the stream.
     *   The sum of the sizes of the two result streams equals the size of this stream.
     *   The first result stream is a prefix of this stream and the second result stream
     *   is a suffix of this stream.
     *
     * Examples:
     *   [1, 2, 3, 4, 1, 2, 3, 4].span{n -> (n < 3)} = ([1, 2], [3, 4, 1, 2, 3, 4])
     *   [1, 2, 3].span{n -> (n < 9)} = ([1, 2, 3], [])
     *   [1, 2, 3].span{n -> (n < 0)} = ([], [1, 2, 3])
     *
     * @param predicate         criteria
     * @return                  pair of two new lists
     */
    fun span(predicate: (A) -> Boolean): Pair<Stream<A>, Stream<A>> {
        return when (this) {
            is Nil -> Pair(Nil, Nil)
            is Cons -> if (predicate(this.hd)) {
                val pair: Pair<Stream<A>, Stream<A>> = this.tl().span(predicate)
                return Pair(Cons(this.hd){-> pair.first}, pair.second)
            } else
                Pair(Nil, this)
        }
    }   // span

    /**
     * Delivers a tuple where first element is prefix of this stream of length n and
     *   second element is the remainder of the stream. The sum of the sizes of the
     *   two result streams equal the size of this stream. The first result stream is a
     *   prefix of this stream. The second result stream is a suffix of this stream. The
     *   second result stream appended on to the first result stream is equal to this
     *   stream.
     *
     * Examples:
     *   [1, 2, 3, 4].splitAt(2) = ([1, 2], [3, 4])
     *   [1, 2, 3, 4].splitAt(0) = ([], [1, 2, 3, 4])
     *   [1, 2, 3, 4].splitAt(5) = ([1, 2, 3, 4], [])
     *   [].splitAt(2) = ([], [])
     *
     * @param n                 number of elements into first result stream
     * @return                  pair of two new streams
     */
    fun splitAt(n: Int): Pair<Stream<A>, Stream<A>> = Pair(this.take(n), this.drop(n))

    /**
     * Extract the elements after the head of this stream, which must be non-empty.
     *   Throws a StreamException on an empty stream. The size of the result stream
     *   will be one less than this stream. The result stream is a suffix of this
     *   stream.
     *
     * Intermediate operation.
     *
     * Examples:
     *   [1, 2, 3, 4].tail() = [2, 3, 4]
     *   [5].tail() = []
     *
     * @return                  new stream of the tail elements
     */
    fun tail(): Stream<A> = when (this) {
        is Nil -> throw StreamException("StreamException.tail: empty stream")
        is Cons -> this.tl()
    }   // tail

    /**
     * The tails function returns all final segments of this stream,
     *   longest first. The result stream size will exceed this stream
     *   size by one. The first element of the result stream is guaranteed
     *   to be the same as the original stream; the final element of the
     *   result stream is guaranteed to be the empty sub-stream. All sub-stream
     *   of the result stream are a prefix to the original.
     *
     * Examples:
     *   [1, 2, 3].tails() = [[1, 2, 3], [2, 3], [3], []]
     *   [].tails() = [[]]
     *   [1, 2, 3].tails().size() = 1 + [1, 2, 3].size()
     *   [1, 2, 3].tails().head() = [1, 2, 3]
     *   [1, 2, 3].tails().last() = []
     *
     * @return                  new list of final segments sub-stream
     */
    fun tails(): Stream<Stream<A>> {
        return when (this) {
            is Nil -> Cons(StreamF.empty()){-> Nil}
            is Cons -> Cons(this){-> this.tl().tails()}
        }
    }   // tails

    /**
     * Return a new stream containing the first n elements from this stream.
     *   If n exceeds the size of this stream, then a copy is returned. If n is
     *    negative or zero, then an empty stream is delivered. The result stream
     *    is a prefix of this stream.
     *
     * Intermediate operation.
     *
     *  Examples:
     *    [1, 2, 3, 4].take(2) = [1, 2]
     *    [1, 2, 3, 4].take(0) = []
     *    [1, 2, 3, 4].take(5) = [1, 2, 3, 4]
     *    [].take(2) = []
     *
     * @param n                 number of elements to extract
     * @return                  new stream of first n elements
     */
    fun take(n: Int): Stream<A> {
        return if (n == 0)
            Nil
        else when (this) {
            is Nil -> Nil
            is Cons -> Cons(this.hd){-> this.tl().take(n - 1)}
        }
    }   // take

    /**
     * Function takeUntil retrieves the leading elements from this stream that match
     *   some predicate. The result stream size will not exceed this stream size.
     *   The result stream is a prefix of this stream.
     *
     * Intermediate operation.
     *
     * Examples:
     *   [1, 2, 3, 4].takeUntil{n -> (n <= 2)} = []
     *   [1, 2, 3, 4].takeUntil{n -> (n > 5)} = [1, 2, 3, 4]
     *   [1, 2, 3, 4].takeUntil{n -> (n > 3)} = [1, 2, 3]
     *   [].takeUntil{n -> (n <= 2)} = []
     *
     * @param predicate         criteria
     * @return                  new list of trailing elements matching criteria
     */
    fun takeUntil(predicate: (A) -> Boolean): Stream<A> = this.takeWhile{a -> !predicate(a)}

    /**
     * Function takeWhile takes the leading elements from this stream that matches
     *   some predicate. The result stream size will not exceed this stream size.
     *   The result stream is a prefix of this stream.
     *
     * Intermediate operation.
     *
     * Examples:
     *   [1, 2, 3, 4].takeWhile{n -> (n <= 2)} = [1, 2]
     *   [1, 2, 3, 4].takeWhile{n -> (n <= 5)} = [1, 2, 3, 4]
     *   [1, 2, 3, 4].takeWhile{n -> (n <= 0)} = []
     *   [].takeWhile{n -> (n <= 2)} = []
     *
     * @param predicate         criteria
     * @return                  new stream of leading elements matching criteria
     */
    fun takeWhile(predicate: (A) -> Boolean): Stream<A> {
        return when (this) {
            is Nil -> Nil
            is Cons -> if (predicate(this.hd))
                Cons(this.hd){-> this.tl().takeWhile(predicate)}
            else
                Nil
        }
    }   // takeWhile

    /**
     * There exists at least one element of this stream that meets some criteria. If
     *   the stream is empty then false is returned.
     *
     * Terminal operation.
     *
     * Examples:
     *   [1, 2, 3, 4].thereExists{m -> (m > 0)} = true
     *   [1, 2, 3, 4].thereExists{m -> (m > 2)} = true
     *   [1, 2, 3, 4].thereExists{m -> (m > 4)} = false
     *   [1, 2, 3, 4].thereExists{m -> true} = true
     *   [1, 2, 3, 4].thereExists{m -> false} = false
     *   [].thereExists{m -> (m > 0)} = false
     *
     * @param predicate         criteria
     * @return                  true if at least one element matches the criteria
     */
    fun thereExists(predicate: (A) -> Boolean): Boolean {
        return when (this) {
            is Nil -> false
            is Cons -> if (predicate(this.hd))
                true
            else
                this.tl().thereExists(predicate)
        }
    }   // thereExists

    /**
     * There exists only one element of this stream that meets some criteria. If the
     *   stream is empty then false is returned.
     *
     * Terminal operation.
     *
     * Examples:
     *   [1, 2, 3, 4].thereExistsUnique{m -> (m == 2)} = true
     *   [1, 2, 3, 4].thereExistsUnique{m -> (m == 5)} = false
     *   [1, 2, 3, 4].thereExistsUnique{m -> true} = false
     *   [1, 2, 3, 4].thereExistsUnique{m -> false} = false
     *   [4].thereExistsUnique{m -> true} = true
     *   [4].thereExistsUnique{m -> false} = false
     *   [].thereExistsUnique{m -> (m == 2)} = false
     *
     * @param predicate         criteria
     * @return                  true if only one element matches the criteria
     */
    fun thereExistsUnique(predicate: (A) -> Boolean): Boolean = (count(predicate) == 1)

    /**
     * Convert this stream into a list.
     *
     * Terminal operation.
     */
    fun toList(): List<A> {
        fun recToList(stream: Stream<A>): List<A> {
            return when (stream) {
                is Nil -> ListF.empty()
                is Cons -> ListF.cons(stream.hd, stream.tl().toList())
            }
        }   // recToList

        return recToList(this)
    }   // toList

    /**
     * Produce a string representation of a list.
     *
     * Terminal operation.
     *
     * @return                  string as <[ ... ]>
     */
    override fun toString(): String = this.makeString(", ", "<[", "]>")

    /**
     * zip returns a stream of corresponding pairs from this stream and the argument stream.
     *   If one input stream is shorter, excess elements of the longer stream are discarded.
     *
     * Intermediate operation.
     *
     * Examples:
     *   [1, 2, 3].zip([4, 5, 6]) = [(1, 4), (2, 5), (3, 6)]
     *   [1, 2].zip([4, 5, 6]) = [(1, 4), (2, 5)]
     *   [1, 2, 3].zip([4, 5]) = [(1, 4), (2, 5)]
     *   [1, 2, 3].zip([]) = []
     *   [].zip([4, 5]) = []
     *
     * @param stream            existing stream
     * @return                  new stream of pairs
     */
    fun <B> zip(stream: Stream<B>): Stream<Pair<A, B>> {
        return when (this) {
            is Nil -> Nil
            is Cons -> when (stream) {
                is Nil -> Nil
                is Cons -> Cons(Pair(this.hd, stream.hd)){-> this.tl().zip(stream.tl())}
            }
        }
    }   // zip

    /**
     * zipWith generalises zip by zipping with the function given as the second argument,
     *   instead of a tupling function. For example, zipWith (+) is applied to two streams
     *   to produce the stream of corresponding sums. The size of the resulting stream will
     *   equal the size of the smaller two streams.
     *
     * Intermediate operation.
     *
     * Examples:
     *   [1, 2, 3].zipWith([4, 5, 6]){m -> {n -> m + n}} = [5, 7, 9]
     *   [].zipWith([4, 5, 6]){m -> {n -> m + n}} = []
     *   [1, 2, 3].zipWith([]){m -> {n -> m + n}} = []
     *
     * @param stream            existing stream
     * @param f                 curried binary function
     * @return                  new stream of function results
     */
    fun <B, C> zipWith(stream: Stream<B>, f: (A) -> (B) -> C): Stream<C> {
        return when (this) {
            is Nil -> Nil
            is Cons -> when (stream) {
                is Nil -> Nil
                is Cons -> Cons(f(this.hd)(stream.hd)){-> this.tl().zipWith(stream.tl(), f)}
            }
        }
    }   // zipWith

}   // Stream
