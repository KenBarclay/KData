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
import com.adt.kotlin.hkfp.fp.FunctionF
import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3
import com.adt.kotlin.hkfp.typeclass.Monoid


// Contravariant extension functions:

/**
 * Append a single element on to this stream. The size of the result stream
 *   will be one more than the size of this stream. The last element in the
 *   result stream will equal the appended element. This stream will be a prefix
 *   of the result stream.
 *
 * Intermediate operation.
 *
 * Examples:
 *   [1, 2, 3, 4].append(5) = [1, 2, 3, 4, 5]
 *   [1, 2, 3, 4].append(5).size() = 1 + [1, 2, 3, 4].size()
 *   [1, 2, 3, 4].append(5).last() = 5
 *   [1, 2, 3, 4].isPrefix([1, 2, 3, 4].append(5)) = true
 *
 * @param a                 new element
 * @return                  new stream with element at end
 */
fun <A> Stream<A>.append(a: A): Stream<A> {
    fun recAppend(stream: Stream<A>, a: A): Stream<A> {
        return when (stream) {
            is Nil -> Cons(a){-> Nil}
            is Cons -> Cons(stream.hd){-> recAppend(stream.tl(), a)}
        }
    }   // recAppend

    return recAppend(this, a)
}   // append

/**
 * Append the given stream on to this stream. The size of the result stream
 *   will equal the sum of the sizes of this stream and the parameter
 *   stream. This stream will be a prefix of the result stream and the
 *   parameter stream will be a suffix of the result stream.
 *
 * Intermediate operation.
 *
 * Examples:
 *   [1, 2, 3].append([4, 5]) = [1, 2, 3, 4, 5]
 *   [1, 2, 3].append([]) = [1, 2, 3]
 *   [].append([3, 4]) = [3, 4]
 *   [1, 2, 3].append([4, 5]).size() = [1, 2, 3].size() + [4, 5].size()
 *   [1, 2, 3].isPrefixOf([1, 2, 3].append([4, 5])) = true
 *   [4, 5].isSuffixOf([1, 2, 3].append([4, 5])) = true
 *
 * @param stream            existing stream
 * @return                  new stream of appended elements
 */
fun <A> Stream<A>.append(stream: Stream<A>): Stream<A> {
    fun recAppend(stream1: Stream<A>, stream2: Stream<A>): Stream<A> {
        return when (stream1) {
            is Nil -> stream2
            is Cons -> Cons(stream1.hd){-> stream1.tl().append(stream2)}
        }
    }   // recAppend

    return recAppend(this, stream)
}   // append

/**
 * Determine if this stream contains the given element.
 *
 * Terminal operation.
 *
 * Examples:
 *   [1, 2, 3, 4].contains(4) = true
 *   [1, 2, 3, 4].contains(5) = false
 *   [].contains(4) = false
 *
 * @param x                 search element
 * @return                  true if search element is present, false otherwise
 */
fun <A> Stream<A>.contains(x: A): Boolean = this.contains{y: A -> (y == x)}

/**
 * Count the number of times the parameter appears in this stream.
 *
 * Terminal operation.
 *
 * Examples:
 *   [1, 2, 3, 4].count(2) = 1
 *   [1, 2, 3, 4].count(5) = 0
 *   [].count(2) = 0
 *   [1, 2, 1, 2, 2].count(2) == 3
 *
 * @param x                 the search value
 * @return                  the number of occurrences
 */
fun <A> Stream<A>.count(x: A): Int = this.count{y: A -> (y == x)}

/**
 * A variant of foldLeft that has no starting value argument, and thus must
 *   be applied to non-empty streams. The initial value is used as the start
 *   value. Throws a StreamException on an empty stream.
 *
 * Terminal operation.
 *
 * Examples:
 *   [1, 2, 3, 4].foldLeft1{m -> {n -> m + n}} = 10
 *
 * @param f                 curried binary function:: A -> A -> A
 * @return                  folded result
 */
fun <A> Stream<A>.foldLeft1(f: (A) -> (A) -> A): A {
    return when (this) {
        is Nil -> throw StreamException("Stream.foldLeft1: empty stream")
        is Cons -> this.tl().foldLeft(this.hd, f)
    }
}   // foldLeft1

fun <A> Stream<A>.foldLeft1(f: (A, A) -> A): A = this.foldLeft1(C2(f))

/**
 * A variant of foldRight that has no starting value argument, and thus must
 *   be applied to non-empty streams. The initial value is used as the start
 *   value. Throws a StreamException on an empty stream.
 *
 * Terminal operation.
 *
 * Examples:
 *   [1, 2, 3, 4].foldRight1{m -> {n -> m * n}} = 24
 *
 * @param f                 curried binary function:: A -> A -> A
 * @return                  folded result
 */
fun <A> Stream<A>.foldRight1(f: (A) -> (A) -> A): A {
    return when (this) {
        is Nil -> throw StreamException("Stream.foldRight1: empty stream")
        is Cons -> this.tl().foldRight(this.hd, f)
    }
}   // foldRight1

fun <A> Stream<A>.foldRight1(f: (A, A) -> A): A = this.foldRight1(C2(f))

/**
 * Find the index of the given value, or -1 if absent.
 *
 * Examples:
 *   [1, 2, 3, 4].indexOf(1) = 0
 *   [1, 2, 3, 4].indexOf(3) = 2
 *   [1, 2, 3, 4].indexOf(5) = -1
 *   [].indexOf(2) = -1
 *
 * @param x                 the search value
 * @return                  the index position
 */
fun <A> Stream<A>.indexOf(x: A): Int = this.indexOf{y -> (y == x)}

/**
 * Interleave this stream and the given stream, alternating elements from each stream.
 *   If either stream is empty then an empty stream is returned. The first element is
 *   drawn from this stream. The size of the result stream will equal twice the size
 *   of the smaller stream. The elements of the result stream are in the same order as
 *   the two original.
 *
 * Intermediate operation.
 *
 * Examples:
 *   [].interleave([]) = []
 *   [].interleave([3, 4, 5]) = []
 *   [1, 2].interleave([]) = []
 *   [1, 2].interleave([3, 4, 5]) = [1, 3, 2, 4]
 *
 * @param stream            other stream
 * @return                  result stream of alternating elements
 */
fun <A> Stream<A>.interleave(stream: Stream<A>): Stream<A> {
    return when (this) {
        is Nil -> Nil
        is Cons -> when (stream) {
            is Nil -> Nil
            is Cons -> Cons(this.hd){-> Cons(stream.hd){-> this.tl().interleave(stream.tl())}}
        }
    }
}   // interleave

/**
 * The intersperse function takes an element and intersperses
 *   that element between the elements of this stream. If this stream
 *   is empty then an empty stream is returned. If this stream size is
 *   one then this stream is returned.
 *
 * Intermediate operation.
 *
 * Examples:
 *   [1, 2, 3, 4].intersperse(0) = [1, 0, 2, 0, 3, 0, 4]
 *   [1].intersperse(0) = [1]
 *   [].intersperse(0) = []
 *
 * @param separator         separator
 * @return                  new stream of existing elements and separators
 */
fun <A> Stream<A>.intersperse(separator: A): Stream<A> {
    return when (this) {
        is Nil -> Nil
        is Cons -> Cons(this.hd){->
            val tail: Stream<A> = this.tl()
            when (tail) {
                is Nil -> Nil
                is Cons -> Cons(separator){-> tail.intersperse(separator)}
            }
        }
    }
}   // intersperse

/**
 * The isInfixOf function returns true iff this stream is a constituent of the argument.
 *
 * Terminal operation.
 *
 * Examples:
 *   [2, 3].isInfixOf([]) = false
 *   [2, 3].isInfixOf([1, 2, 3, 4]) = true
 *   [1, 2].isInfixOf([1, 2, 3, 4]) = true
 *   [3, 4].isInfixOf([1, 2, 3, 4]) = true
 *   [].isInfixOf([1, 2, 3, 4]) = true
 *   [3, 2].isInfixOf([1, 2, 3, 4]) = false
 *   [1, 2, 3, 4, 5].isInfixOf([1, 2, 3, 4]) = false
 *
 * @param stream            existing stream
 * @return                  true if this stream is constituent of second stream
 */
fun <A> Stream<A>.isInfixOf(stream: Stream<A>): Boolean {
    val isPrefix: (Stream<A>) -> (Stream<A>) -> Boolean = {ps -> {qs -> ps.isPrefixOf(qs)}}
    return stream.tails().thereExists(isPrefix(this))
}   // isInfixOf

/**
 * Return true if this stream has the same content as the given stream, regardless
 *   of order.
 *
 * Terminal operation.
 *
 * Examples:
 *   [1, 2, 3, 4].isPermutationOf([1, 2, 3, 4]) = true
 *   [].isPermutationOf([1, 2, 3, 4]) = true
 *   [].isPermutationOf([]) = true
 *   [1, 2, 3, 4].isPermutationOf([]) = false
 *   [1, 2, 3, 4].isPermutationOf([5, 4, 3, 2, 1]) = true
 *   [5, 4, 3, 2, 1].isPermutationOf([1, 2, 3, 4]) = false
 *
 * @param stream            comparison stream
 * @return                  true if this stream has the same content as the given stream; otherwise false
 */
fun <A> Stream<A>.isPermutationOf(stream: Stream<A>): Boolean = this.forAll{x -> stream.contains(x)}

/**
 * The isPrefixOf function returns true iff this stream is a prefix of the second.
 *
 * Terminal operation.
 *
 * Examples:
 *   [1, 2].isPrefixOf([1, 2, 3, 4]) = true
 *   [1, 2, 3, 4].isPrefixOf([1, 2, 3, 4]) = true
 *   [1, 2].isPrefixOf([2, 3, 4]) = false
 *   [1, 2].isPrefixOf([]) = false
 *   [].isPrefixOf([1, 2]) = true
 *   [].isPrefixOf([]) = true
 *
 * @param stream            existing stream
 * @return                  true if this stream is prefix of given stream
 */
fun <A> Stream<A>.isPrefixOf(stream: Stream<A>): Boolean {
    return when (this) {
        is Nil -> true
        is Cons -> when (stream) {
            is Nil -> false
            is Cons -> if (this.hd == stream.hd)
                this.tl().isPrefixOf(stream.tl())
            else
                false
        }
    }
}   // isPrefixOf

/**
 * Return true if this stream has the same content as the given stream, respecting
 *   the order.
 *
 * Terminal operation.
 *
 * Examples:
 *   [1, 2, 3, 4].isOrderedPermutationOf([1, 2, 3, 4]) = true
 *   [1, 2, 3, 4].isOrderedPermutationOf([]) = false
 *   [].isOrderedPermutationOf([1, 2, 3, 4]) = true
 *   [].isOrderedPermutationOf([]) = true
 *   [1, 4].isOrderedPermutationOf([1, 2, 3, 4]) = true
 *   [1, 2, 3].isOrderedPermutationOf([1, 1, 2, 1, 2, 4, 3, 4]) = true
 *   [1, 2, 3].isOrderedPermutationOf([1, 1, 3, 1, 4, 3, 3, 4]) = false
 *
 * @param stream            comparison stream
 * @return                  true if this stream has the same content as the given stream; otherwise false
 */
fun <A> Stream<A>.isOrderedPermutationOf(stream: Stream<A>): Boolean {
    return when (this) {
        is Nil -> true
        is Cons -> {
            val head: A = this.hd
            val tail: Stream<A> = this.tl()
            val index: Int = stream.indexOf(head)
            if (index < 0)
                false
            else
                tail.isOrderedPermutationOf(stream.drop(1 + index))
        }
    }
}   // isOrderedPermutationOf

/**
 * The isSuffixOf function takes returns true iff this stream is a suffix of the second.
 *
 * Terminal operation.
 *
 * Examples:
 *   [3, 4].isSuffixOf([1, 2, 3, 4]) = true
 *   [1, 2, 3, 4].isSuffixOf([1, 2, 3, 4]) = true
 *   [3, 4].isSuffixOf([1, 2, 3]) = false
 *   [].isSuffixOf([1, 2, 3, 4]) = true
 *   [1, 2, 3, 4].isSuffixOf([]) = false
 *
 * @param stream            existing stream
 * @return                  true if this stream is suffix of given stream
 */
fun <A> Stream<A>.isSuffixOf(stream: Stream<A>): Boolean =
    this.reverse().isPrefixOf(stream.reverse())

/**
 * Remove the first occurrence of the given element from this stream. The result stream
 *   will either have the same size as this stream (if no such element is present) or
 *   will have the size of this stream less one.
 *
 * Intermediate operation.
 *
 * Examples:
 *   [1, 2, 3, 4].remove(4) = [1, 2, 3]
 *   [1, 2, 3, 4].remove(5) = [1, 2, 3, 4]
 *   [4, 4, 4, 4].remove(4) = [4, 4, 4]
 *   [].remove(4) = []
 *
 * @param x                 element to be removed
 * @return                  new stream with element deleted
 */
fun <A> Stream<A>.remove(x: A): Stream<A> = this.remove{a: A -> (x == a)}

/**
 * The removeAll function removes all the elements from this stream that match
 *   a given value. The result stream size will not exceed this stream size.
 *
 * Intermediate operation.
 *
 * Examples:
 *   [1, 2, 3, 4].removeAll(2) = [1, 3, 4]
 *   [1, 2, 3, 4].removeAll(5) = [1, 2, 3, 4]
 *   [].removeAll(4) = []
 *   [1, 4, 2, 3, 4].removeAll(4) = [1, 2, 3]
 *   [4, 4, 4, 4, 4].removeAll(4) = []
 *
 * @param predicate		    criteria
 * @return          		new stream with all matching elements removed
 */
fun <A> Stream<A>.removeAll(x: A): Stream<A> = this.removeAll{a: A -> (x == a)}



// Functor extension functions:

/**
 * Apply the function to the content(s) of the stream context. Function
 *   fmap applies the function parameter to each item in this stream, delivering
 *   a new stream. The result stream has the same size as this stream.
 *
 * Intermediate operation.
 *
 * Examples:
 *   [1, 2, 3, 4].fmap{n -> n + 1} = [2, 3, 4, 5]
 *   [].fmap{n -> n + 1} = []
 */
fun <A, B> Stream<A>.fmap(f: (A) -> B): Stream<B> = this.map(f)

/**
 * An infix symbol for fmap.
 *
 * Intermediate operation.
 */
infix fun <A, B> ((A) -> B).dollar(v: Stream<A>): Stream<B> = v.fmap(this)

/**
 * Replace all locations in the input with the given value.
 *
 * Intermediate operation.
 *
 * Examples:
 *   [1, 2, 3, 4].replaceAll(5) = [5, 5, 5, 5]
 *   [].replaceAll(5) = []
 */
fun <A, B> Stream<A>.replaceAll(b: B): Stream<B> = this.fmap{_ -> b}

/**
 * Distribute the Stream<(A, B)> over the pair to get (Stream<A>, Stream<B>).
 *
 * Examples:
 *   [(1, 2), (3, 4), (5, 6)].distribute() = ([1, 3, 5], [2, 4, 6])
 *   [].distribute() = ([], [])
 */
fun <A, B> Stream<Pair<A, B>>.distribute(): Pair<Stream<A>, Stream<B>> {
    fun recDistribute(stream: Stream<Pair<A, B>>, streamA: Stream<A>, streamB: Stream<B>): Pair<Stream<A>, Stream<B>> {
        return when (stream) {
            is Nil -> Pair(streamA, streamB)
            is Cons -> recDistribute(stream.tl(), streamA.append(stream.hd.first), streamB.append(stream.hd.second))
        }
    }   // recDistribute

    return recDistribute(this, StreamF.empty(), StreamF.empty())
}   // distribute

/**
 * Inject a to the left of the b's in this stream.
 */
fun <A, B> Stream<B>.injectL(a: A): Stream<Pair<A, B>> = this.fmap{b: B -> Pair(a, b)}

/**
 * Inject b to the right of the a's in this stream.
 */
fun <A, B> Stream<A>.injectR(b: B): Stream<Pair<A, B>> = this.fmap{a: A -> Pair(a, b)}

/**
 * Twin all the a's in this stream with itself.
 */
fun <A> Stream<A>.pair(): Stream<Pair<A, A>> = this.fmap{a: A -> Pair(a, a)}

/**
 * Pair all the a's in this stream with the result of the function application.
 */
fun <A, B> Stream<A>.product(f: (A) -> B): Stream<Pair<A, B>> = this.fmap{a: A -> Pair(a, f(a))}



// Applicative extension functions:

/**
 * Apply the function wrapped in a context to the content of the
 *   value also wrapped in a matching context.
 *
 * Examples:
 *   [1, 2, 3, 4].ap([{n -> (n % 2 == 0)}]) = [false, true, false, true]
 *   [].ap([{n -> (n % 2 == 0)}]) = []
 */
fun <A, B> Stream<A>.ap(fs: Stream<(A) -> B>): Stream<B> {
    fun recAp(stream: Stream<A>, fs: Stream<(A) -> B>, acc: Stream<B>): Stream<B> {
        return when (fs) {
            is Nil -> acc
            is Cons -> recAp(stream, fs.tl(), acc.append(stream.map(fs.hd)))
        }
    }   // recAp

    return recAp(this, fs, StreamF.empty())
}   // ap

/**
 * An infix symbol for ap.
 */
infix fun <A, B> Stream<(A) -> B>.apply(v: Stream<A>): Stream<B> = v.ap(this)

/**
 * An infix symbol for ap.
 */
infix fun <A, B> ((A) -> B).apply(v: Stream<A>): Stream<B> = v.ap(StreamF.singleton(this))

/**
 * Sequence actions, discarding the value of the first argument.
 *
 * Intermediate operation.
 *
 * Examples:
 *   [0, 1, 2, 3].sDF(["Ken", "John", "Jessie", "Irene"]) = ["Ken", "Ken", "Ken", "Ken", "John", "John", "John", "John", "Jessie", "Jessie", "Jessie", "Jessie", "Irene", "Irene", "Irene", "Irene"]
 *   [5].sDF(["Ken", "John", "Jessie", "Irene"]) = ["Ken", "John", "Jessie", "Irene"]
 *   [].sDF(["Ken", "John", "Jessie", "Irene"]) = []
 */
fun <A, B> Stream<A>.sDF(streamB: Stream<B>): Stream<B> {
    fun constant(b: B): (A) -> B = {_: A -> b}
    return StreamF.liftA2(::constant)(streamB)(this)
}   // sDF

/**
 * Sequence actions, discarding the value of the second argument.
 *
 * Intermediate operation.
 *
 * Examples:
 *   [0, 1, 2, 3].sDS(["Ken", "John", "Jessie", "Irene"]) = [0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3]
 *   [0, 1, 2, 3].sDS(["Ken"]) = [0, 1, 2, 3]
 *   [].sDS(["Ken"]) = []
 */
fun <A, B> Stream<A>.sDS(streamB: Stream<B>): Stream<A> {
    val const: (A) -> (B) -> A = FunctionF::constant
    return StreamF.liftA2(const)(this)(streamB)
}   // sDS

/**
 * The product of two applicatives.
 *
 * Terminal operation.
 *
 * ["Ken", "John", "Jessie", "Irene"].product2([1, 2, 3]) = [("Ken", 1), ("Ken", 2), ("Ken", 3), ("John", 1), ...]
 * ["Ken", "John", "Jessie", "Irene"].product2([]) = []
 * [].product2([1, 2, 3]) = []
 */
fun <A, B> Stream<A>.product2(streamB: Stream<B>): Stream<Pair<A, B>> {
    fun <X, Y> recProduct2(streamX: Stream<X>, streamY: Stream<Y>, acc: Stream<Pair<X, Y>>): Stream<Pair<X, Y>> {
        fun recInnerProduct2(x: X, streamY: Stream<Y>, acc: Stream<Pair<X, Y>>): Stream<Pair<X, Y>> {
            return when(streamY) {
                is Nil -> acc
                is Cons -> recInnerProduct2(x, streamY.tl(), acc.append(Pair(x, streamY.hd)))
            }
        }   // recInnerProduct2

        return when(streamX) {
            is Nil -> acc
            is Cons -> recProduct2(streamX.tl(), streamY, recInnerProduct2(streamX.hd, streamY, acc))
        }
    }   // recProduct2

    return recProduct2(this, streamB, StreamF.empty())
}   // product2

/**
 * The product of three applicatives.
 *
 * Terminal operation.
 *
 * Examples:
 *   ["Ken", "John"].product3([1, 2], [false, true]) = [("Ken", 1, false), ("Ken", 1, true), ("John", 1, false), ...]
 *   ["Ken", "John"].product3([], [false, true]) = []
 *   ["Ken", "John"].product3([1, 2], []) = []
 *   [].product3([1, 2], [false, true]) = []
 */
fun <A, B, C> Stream<A>.product3(streamB: Stream<B>, streamC: Stream<C>): Stream<Triple<A, B, C>> {
    fun <X, Y> recProduct2(streamX: Stream<X>, streamY: Stream<Y>, acc: Stream<Pair<X, Y>>): Stream<Pair<X, Y>> {
        fun recInnerProduct2(x: X, streamY: Stream<Y>, acc: Stream<Pair<X, Y>>): Stream<Pair<X, Y>> {
            return when(streamY) {
                is Nil -> acc
                is Cons -> recInnerProduct2(x, streamY.tl(), acc.append(Pair(x, streamY.hd)))
            }
        }   // recInnerProduct2

        return when(streamX) {
            is Nil -> acc
            is Cons -> recProduct2(streamX.tl(), streamY, recInnerProduct2(streamX.hd, streamY, acc))
        }
    }   // recProduct2

    fun <X, Y, Z> recProduct3(streamX: Stream<X>, streamYZ: Stream<Pair<Y, Z>>, acc: Stream<Triple<X, Y, Z>>): Stream<Triple<X, Y, Z>> {
        fun recInnerProduct3(x: X, streamYZ: Stream<Pair<Y, Z>>, acc: Stream<Triple<X, Y, Z>>): Stream<Triple<X, Y, Z>> {
            return when (streamYZ) {
                is Nil -> acc
                is Cons -> {
                    val (y: Y, z: Z) = streamYZ.hd
                    recInnerProduct3(x, streamYZ.tl(), acc.append(Triple(x, y, z)))
                }
            }
        }   // recInnerProduct3

        return when(streamX) {
            is Nil -> acc
            is Cons -> recProduct3(streamX.tl(), streamYZ, recInnerProduct3(streamX.hd, streamYZ, acc))
        }
    }   // recProduct3

    return recProduct3(this, recProduct2(streamB, streamC, StreamF.empty()), StreamF.empty())
}   // product3

/**
 * fmap2 is a binary version of fmap.
 *
 * Intermediate operation.
 *
 * Examples:
 *   [1, 2, 3, 4].fmap2([5, 6, 7]){m -> {n -> m + n}} = [6, 7, 8, 7, 8, 9, 8, 9, 10, 9, 10, 11]
 *   [1, 2, 3, 4].fmap2([]){m -> {n -> m + n}} = []
 *   [].fmap2([5, 6, 7]){m -> {n -> m + n}} = []
 */
fun <A, B, C> Stream<A>.fmap2(streamB: Stream<B>, f: (A) -> (B) -> C): Stream<C> =
    StreamF.liftA2(f)(this)(streamB)

fun <A, B, C> Stream<A>.fmap2(streamB: Stream<B>, f: (A, B) -> C): Stream<C> =
    this.fmap2(streamB, C2(f))

/**
 * fmap3 is a ternary version of fmap.
 *
 * Intermediate operation.
 *
 * Examples:
 *   [1, 2].fmap3([3, 4], [5, 6]){m -> {n -> {o -> m + n + o}} = [9, 10, 10, 11, 10, 11, 11, 12]
 *   [1, 2].fmap3([], [5, 6]){m -> {n -> {o -> m + n + o}} = []
 *   [1, 2].fmap3([3, 4], []){m -> {n -> {o -> m + n + o}} = []
 *   [].fmap3([3, 4], [5, 6]){m -> {n -> {o -> m + n + o}} = []
 */
fun <A, B, C, D> Stream<A>.fmap3(streamB: Stream<B>, streamC: Stream<C>, f: (A) -> (B) -> (C) -> D): Stream<D> =
    StreamF.liftA3(f)(this)(streamB)(streamC)

fun <A, B, C, D> Stream<A>.fmap3(streamB: Stream<B>, streamC: Stream<C>, f: (A, B, C) -> D): Stream<D> =
    this.fmap3(streamB, streamC, C3(f))

/**
 * ap2 is a binary version of ap, defined in terms of ap.
 *
 * Terminal operation.
 *
 * Examples:
 *   [1, 2].ap2([3, 4], [{m -> {n -> m + n}}, {m -> {n -> m * n}}]) = [4, 5, 5, 6, 3, 4, 6, 8]
 *   [1, 2].ap2([], [{m -> {n -> m + n}}, {m -> {n -> m * n}}]) = []
 *   [].ap2([3, 4], [{m -> {n -> m + n}}, {m -> {n -> m * n}}]) = []
 */
fun <A, B, C> Stream<A>.ap2(streamB: Stream<B>, f: Stream<(A) -> (B) -> C>): Stream<C> =
    streamB.ap(this.ap(f))

/**
 * ap3 is a ternary version of ap, defined in terms of ap.
 *
 * Terminal operation.
 *
 * Examples:
 *   [1, 2].ap3([3, 4], [5, 6], [{m -> {n -> {o -> m + n + o}}}]) = [9, 10, 10, 11, 10, 11, 11, 12]
 *   [1, 2].ap3([], [5, 6], [{m -> {n -> {o -> m + n + o}}}]) = []
 *   [1, 2].ap3([3, 4], [], [{m -> {n -> {o -> m + n + o}}}]) = []
 *   [].ap3([3, 4], [5, 6], [{m -> {n -> {o -> m + n + o}}}]) = []
 */
fun <A, B, C, D> Stream<A>.ap3(streamB: Stream<B>, streamC: Stream<C>, f: Stream<(A) -> (B) -> (C) -> D>): Stream<D> =
    streamC.ap(streamB.ap(this.ap(f)))



// Monad extension functions:

/**
 * Sequentially compose two actions, passing any value produced by the first
 *   as an argument to the second.
 *
 * Terminal operation.
 *
 * Examples:
 *   [0, 1, 2, 3].bind{n -> [n, n + 1]} = [0, 1, 1, 2, 2, 3, 3, 4]
 *   [].bind{n -> [n, n + 1]} = []
 */
fun <A, B> Stream<A>.bind(f: (A) -> Stream<B>): Stream<B> {
    tailrec
    fun recBind(vs: Stream<A>, f: (A) -> Stream<B>, acc: Stream<B>): Stream<B> {
        return when (vs) {
            is Nil -> acc
            is Cons -> recBind(vs.tl(), f, acc.append(f(vs.hd)))
        }
    }   // recBind

    val g: (A) -> Stream<B> = {a: A -> f(a)}
    return recBind(this, g, StreamF.empty())
}   // bind

fun <A, B> Stream<A>.flatMap(f: (A) -> Stream<B>): Stream<B> = this.bind(f)

/**
 * Sequentially compose two actions, discarding any value produced by the first,
 *   like sequencing operators (such as the semicolon) in imperative languages.
 *
 * Terminal operation.
 *
 * Examples:
 *   [0, 1, 2, 3].then(["Ken", "John"]) = ["Ken", "John", "Ken", "John", "Ken", "John", "Ken", "John"]
 *   [].then(["Ken", "John"]) = []
 */
fun <A, B> Stream<A>.then(streamB: Stream<B>): Stream<B> = this.bind{_ -> streamB}



// Foldable extension functions:

/**
 * Combine the elements of a structure using a monoid.
 *
 * Terminal operation.
 *
 * Examples:
 *   [1, 2, 3, 4].fold(intAddMonoid) = 10
 *   [].fold(intAddMonoid) = 0
 */
fun <A> Stream<A>.fold(md: Monoid<A>): A =
    this.foldLeft(md.empty){a: A -> {b: A -> md.combine(a, b)}}

/**
 * Map each element of the structure to a monoid, and combine the results.
 *
 * Terminal operation.
 *
 * Examples:
 *   [1, 2, 3, 4].foldMap(intAddMonoid){n -> n + 1} = 14
 *   [].foldMap(intAddMonoid){n -> n + 1} = 0
 */
fun <A, B> Stream<A>.foldMap(md: Monoid<B>, f: (A) -> B): B =
    this.foldLeft(md.empty){b: B -> {a: A -> md.combine(b, f(a))}}
