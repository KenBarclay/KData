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

import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.trie.vector.node.*

import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.hkfp.fp.FunctionF.C2



abstract class VectorAB<A> internal constructor(override val root: Node<A>) : VectorIF<A> {

    override fun iterator(): Iterator<A> = root.iterator()

    /**
     * Append a single element on to this vector.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].append(9) = [1, 2, 3, 4, 5, 6, 9]
     *   [].append(9) = [9]
     *
     * @param a                 new element
     * @return                  new vector with element at end
     */
    override fun append(a: A): VectorIF<A> = Vector(root.append(a))

    /**
     * Append the given vector on to this vector.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].append([]) = [1, 2, 3, 4, 5, 6]
     *   [].append([1, 2, 3, 4, 5, 6]) = [1, 2, 3, 4, 5, 6]
     *   [1, 2, 3, 4, 5, 6].append([1, 2, 3, 4, 5, 6]) = [1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6]
     *
     * @param vec               existing vector
     * @return                  new vector of appended elements
     */
    override fun append(vec: VectorIF<A>): VectorIF<A> = this.concatenate(vec)

    /**
     * Append the given vector on to this vector.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].concatenate([]) = [1, 2, 3, 4, 5, 6]
     *   [].concatenate([1, 2, 3, 4, 5, 6]) = [1, 2, 3, 4, 5, 6]
     *   [1, 2, 3, 4, 5, 6].concatenate([1, 2, 3, 4, 5, 6]) = [1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6]
     *
     * @param vec               existing vector
     * @return                  new vector of appended elements
     */
    override fun concatenate(vec: VectorIF<A>): VectorIF<A> = Vector(this.root.concatenate(vec.root))

    /**
     * Determine if this vector contains the element determined by the predicate.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].contains{x -> (x == 5)} = true
     *   [1, 2, 3, 4, 5, 6].contains{x -> (x == 99)} = false
     *   [].contains{x -> (x == 99)} = false
     *
     * @param predicate         search predicate
     * @return                  true if search element is present, false otherwise
     */
    override fun contains(predicate: (A) -> Boolean): Boolean = root.contains(predicate)

    /**
     * Determine if this vector contains the given element.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].contains(5) = true
     *   5 in [1, 2, 3, 4, 5, 6] = true
     *   [1, 2, 3, 4, 5, 6].contains(99) = false
     *   [].contains(99) = false
     *
     * @param a                 search element
     * @return                  true if search element is present, false otherwise
     */
    override operator fun contains(a: A): Boolean = root.contains(a)

    /**
     * Count the number of times a value appears in this vector matching the criteria.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].count{x -> (x == 5)} = 1
     *   [1, 2, 3, 4, 5, 6].count{x -> (x == 99)} = 0
     *   [1, 2, 3, 4, 2, 2].count{x -> (x == 2)} = 3
     *   [].count{x -> (x == 99)} = 0
     *
     * @param predicate         the search criteria
     * @return                  the number of occurrences
     */
    override fun count(predicate: (A) -> Boolean): Int = root.count(predicate)

    /**
     * Count the number of times the parameter appears in this vector.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].count(5) = 1
     *   [1, 2, 3, 4, 5, 6].count(99) = 0
     *   [1, 2, 3, 4, 2, 2].count(2) = 3
     *   [].count(99) = 0
     *
     * @param a                 the search value
     * @return                  the number of occurrences
     */
    override fun count(a: A): Int = root.count(a)

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param other             the other object
     * @return                  true if "equal", false otherwise
     */
    override fun equals(other: Any?): Boolean {
        return if (this === other)
            true
        else if (other == null || this::class.java != other::class.java)
            false
        else {
            @Suppress("UNCHECKED_CAST") val vec: VectorIF<A> = other as VectorIF<A>
            (root == vec.root)
        }
    }

    /**
     * Return the element at the specified position in this vector.
     *   Throws a VectorException if the index is out of bounds.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].get(0) = 1
     *   [1, 2, 3, 4, 5, 6].get(3) = 4
     *   [1, 2, 3, 4, 5, 6][3] = 4
     *   [1, 2, 3, 4, 5, 6].get(9) = exception
     *   [].get(2) = exception
     *
     * @param index             position in vector
     * @return                  the element at the specified position in the vector
     */
    override operator fun get(index: Int): A = root.get(index)

    /**
     * Extract the first element of this vector, which must be non-empty.
     *   Throws an Exception on an empty vector.
     *
     * Examples:
     *   [1, 2, 3, 4].head() = 1
     *   [5].head() = 5
     *
     * @return                  the element at the front of the traversable
     */
    override fun head(): A = root.get(0)

    /**
     * Find the index of the given value, or -1 if absent.
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
    override fun indexOf(predicate: (A) -> Boolean): Int = root.indexOf(predicate)

    /**
     * Find the index of the given value, or -1 if absent.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].indexOf(4) = 3
     *   [1, 2, 3, 4, 5, 6].indexOf(99) = -1
     *   [].indexOf(99) = -1
     *
     * @param a                 the search value
     * @return                  the index position
     */
    override fun indexOf(a: A): Int = root.indexOf(a)

    /**
     * Return all the elements of this vector except the last one. The vector must be non-empty.
     *   Throws an Exception on an empty vector.
     *
     * Examples:
     *   [1, 2, 3, 4].init() = [1, 2, 3]
     *   [5].init() = []
     *
     * @return                  new list of the initial elements
     */
    override fun init(): VectorIF<A> = Vector(root.take(root.length() - 1))

    /**
     * Test whether this vector is empty.
     *
     * Examples:
     *   [].isEmpty() = true
     *   [1, 2, 3, 4, 5, 6].isEmpty() = false
     *
     * @return                  true if this vector is empty
     */
    override fun isEmpty(): Boolean = root.isEmpty()

    /**
     * Extract the last element of this vector, which must be non-empty.
     *   Throws an Exception on an empty vector.
     *
     * Examples:
     *   [1, 2, 3, 4].last() = 4
     *   [5].last() = 5
     *
     * @return                  final element in the vector
     */
    override fun last(): A = root.get(root.length() - 1)

    /**
     * Obtains the length of this vector.
     *
     * Examples:
     *   [].length() = 0
     *   [5].length() = 1
     *   [1, 2, 3, 4, 5, 6].length() = 6
     *
     * @return                  number of elements in the vector
     */
    override fun length(): Int = root.length()

    /**
     * Obtains the length of this vector.
     *
     * Examples:
     *   [].size() = 0
     *   [5].size() = 1
     *   [1, 2, 3, 4, 5, 6].size() = 6
     *
     * @return                  number of elements in the vector
     */
    override fun size(): Int = root.size()

    /**
     * Sort the elements of this vector into ascending order and deliver
     *   the resulting vector. The elements are compared using the given
     *   comparator.
     *
     * Examples:
     *   [4, 3, 2, 1].sort{x, y -> if (x < y) -1 else if (x > y) +1 else 0} = [1, 2, 3, 4]
     *   [1, 2, 3, 4].sort{x, y -> if (x < y) -1 else if (x > y) +1 else 0} = [1, 2, 3, 4]
     *   [].sort{x, y -> if (x < y) -1 else if (x > y) +1 else 0} = []
     *   ["Ken", "John", "Jessie", "", ""].sort{str1, str2 -> str1.compareTo(str2)} = ["", "", "Jessie", "John", "Ken"]
     *
     * @param comparator        element comparison function
     * @return                  the sorted seq
     */
    override fun sort(comparator: (A, A) -> Int): VectorIF<A> = Vector(root.sort(comparator))

    /**
     * Extract the elements after the head of this vector, which must be non-empty.
     *   Throws an Exception on an empty vector. The size of the result vector
     *   will be one less than this vector. The result vector is a suffix of this
     *   vector.
     *
     * Examples:
     *   [1, 2, 3, 4].tail() = [2, 3, 4]
     *   [5].tail() = []
     *
     * @return                  new list of the tail elements
     */
    override fun tail(): VectorIF<A> = Vector(root.drop(1))

    /**
     * Present the trie as a graph revealing the subtrees.
     *
     * Examples:
     *   [].toGraph() = "Vector: Empty"
     *   [1, 2, 3, 4, 5, 6].toGraph() = "Vector: Root(tails: [5]; vecs: [])"
     *
     * @return                  the trie as a graph
     */
    override fun toGraph(): String = "Vector: ${root.toGraph()}"

    /**
     * Convert this vector to a list of key/value pairs
     *
     * @return                  list of key/value pairs
     */
    override fun toList(): List<A> = root.toList()

    /**
     * Produce a string representation of a vector.
     *
     * Examples:
     *   [].toString() = "[]"
     *   [1, 2, 3, 4, 5, 6].toString() = "[1, 2, 3, 4, 5, 6]"
     *
     * @return                  string as [ ... ]
     */
    override fun toString(): String = "[${root}]"

    /**
     * Update the element at the given index this vector. If the index is
     *   outwith the bounds of the vector then this is a no-op.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].update(3, 33) = [1, 2, 3, 33, 5, 6]
     *   [1, 2, 3, 4, 5, 6].update(9, 33) = [1, 2, 3, 4, 5, 6]
     *
     * @param index             update position
     * @param a                 new element
     * @return                  new vector with element at given index position
     */
    override fun update(index: Int, a: A): VectorIF<A> = Vector(root.update(index, a))



// ---------- list transformations ------------------------

    /**
     * Function map applies the function parameter to each item in this vector, delivering
     *   a new vector.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].map{x -> (1 + x)} = [2, 3, 4, 5, 6, 7]
     *   [].map{x -> (1 + x)} = []
     *
     * @param f                 pure function:: A -> B
     * @return                  new vector of transformed values
     */
    override fun <B> map(f: (A) -> B): VectorIF<B> = Vector(root.map(f))

    /**
     * Reverses the content of this vector into a new vector.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].reverse() = [6, 5, 4, 3, 2, 1]
     *   [].reverse() = []
     *
     * @return                  new vector of elements reversed
     */
    override fun reverse(): VectorIF<A> = Vector(root.reverse())



// ---------- reducing vectors (folds) --------------------

    /**
     * foldLeft is a higher-order function that folds a binary function into this
     *   vector of values.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].foldLeft(0){m -> {n -> m + n}} = 21
     *   [].foldLeft(0){m -> {n -> m + n}} = 0
     *   [1, 2, 3, 4, 5, 6].foldLeft([]){vec -> {x -> singleton(x).append(vec)}} = [6, 5, 4, 3, 2, 1]
     *
     * @param e                 initial value
     * @param f                 curried binary function:: T -> V -> T
     * @return                  folded result
     */
    override fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B = root.foldLeft(e, f)

    /**
     * foldLeft is a higher-order function that folds a binary function into this
     *   vector of values.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].foldLeft(0){m, n -> m + n} = 21
     *   [].foldLeft(0){m, n -> m + n} = 0
     *   [1, 2, 3, 4, 5, 6].foldLeft([]){vec, x -> singleton(x).append(vec)} = [6, 5, 4, 3, 2, 1]
     *
     * @param e                 initial value
     * @param f                 binary function:: B * A -> B
     * @return                  folded result
     */
    override fun <B> foldLeft(e: B, f: (B, A) -> B): B = root.foldLeft(e, f)

    /**
     * A variant of foldLeft that has no starting value argument, and thus must
     *   be applied to non-empty vectors. The initial value is used as the start
     *   value. Throws a VectorException on an empty vector.
     *
     * Examples:
     *   [1, 2, 3, 4].foldLeft1{m -> {n -> m + n}} = 10
     *
     * @param f                 curried binary function:: A -> A -> A
     * @return                  folded result
     */
    override fun foldLeft1(f: (A) -> (A) -> A): A =
            if (this.isEmpty())
                throw VectorException("foldLeft1: empty vector")
            else
                this.tail().foldLeft(this.head(), f)

    /**
     * A variant of foldLeft that has no starting value argument, and thus must
     *   be applied to non-empty vectors. The initial value is used as the start
     *   value. Throws a VectorException on an empty vector.
     *
     * Examples:
     *   [1, 2, 3, 4].foldLeft1{m, n -> m + n} = 10
     *
     * @param f                 uncurried binary function:: A -> A -> A
     * @return                  folded result
     */
    override fun foldLeft1(f: (A, A) -> A): A = this.foldLeft1(C2(f))

    /**
     * foldRight is a higher-order function that folds a binary function into this
     *   vector of values. Fold functions can be the implementation for many other
     *   functions.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].foldRight(1){m -> {n -> m * n}} = 720
     *   [].foldRight(1){m -> {n -> m * n}} = 1
     *   [1, 2, 3, 4, 5, 6].foldRight([]){m -> {vec -> vec.append(m)}} = [6, 5, 4, 3, 2, 1]
     *
     * @param e                 initial value
     * @param f                 curried binary function:: A -> B -> B
     * @return                  folded result
     */
    override fun <B> foldRight(e: B, f: (A) -> (B) -> B): B = root.foldRight(e, f)

    /**
     * foldRight is a higher-order function that folds a binary function into this
     *   vector of values. Fold functions can be the implementation for many other
     *   functions.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].foldRight(1){m, n -> m * n} = 720
     *   [].foldRight(1){m, n -> m * n} = 1
     *   [1, 2, 3, 4, 5, 6].foldRight([]){m, vec -> vec.append(m)} = [6, 5, 4, 3, 2, 1]
     *
     * @param e                 initial value
     * @param f                 binary function:: A * B -> B
     * @return                  folded result
     */
    override fun <B> foldRight(e: B, f: (A, B) -> B): B = root.foldRight(e, f)

    /**
     * A variant of foldRight that has no starting value argument, and thus must
     *   be applied to non-empty vectors. The initial value is used as the start
     *   value. Throws a VectorException on an empty vector.
     *
     * Examples:
     *   [1, 2, 3, 4].foldRight1{m -> {n -> m * n}} = 24
     *
     * @param f                 curried binary function:: A -> A -> A
     * @return                  folded result
     */
    override fun foldRight1(f: (A) -> (A) -> A): A =
            if (this.isEmpty())
                throw VectorException("foldRight1: empty vector")
            else
                this.tail().foldRight(this.head(), f)

    /**
     * A variant of foldRight that has no starting value argument, and thus must
     *   be applied to non-empty vectors. The initial value is used as the start
     *   value. Throws a VectorException on an empty vector.
     *
     * Examples:
     *   [1, 2, 3, 4].foldRight1{m, n -> m * n} = 24
     *
     * @param f                 uncurried binary function:: A -> A -> A
     * @return                  folded result
     */
    override fun foldRight1(f: (A, A) -> A): A = this.foldRight1(C2(f))



// ---------- special folds -------------------------------

    /**
     * All the elements of this vector meet some criteria. If the vector is empty then
     *   true is returned.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].folAll{m -> (m < 7)} = true
     *   [1, 2, 3, 4, 5, 6].forAll{m -> (m < 6)} = false
     *   [].forAll{m -> (m % 2 == 0)} = true
     *
     * @param predicate         criteria
     * @return                  true if all elements match criteria
     */
    override fun forAll(predicate: (A) -> Boolean): Boolean = root.forAll(predicate)

    /**
     * There exists at least one element of this vector that meets some criteria. If
     *   the vector is empty then false is returned.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].thereExists{m -> (m == 3)} = true
     *   [1, 2, 3, 4, 5, 6].thereExists{m -> (m < 0)} = false
     *   [].thereExists{m -> (m == 3)} = false
     *
     * @param predicate         criteria
     * @return                  true if at least one element matches the criteria
     */
    override fun thereExists(predicate: (A) -> Boolean): Boolean = root.thereExists(predicate)

    /**
     * There exists only one element of this vector that meets some criteria. If the
     *   vector is empty then false is returned.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].thereExistsUnique{m -> (m == 5)} = true
     *   [1, 2, 3, 4, 5, 5].thereExistsUnique{m -> (m == 5)} = false
     *   [].thereExistsUnique{m -> (m == 5)} = false
     *
     * @param predicate         criteria
     * @return                  true if only one element matches the criteria
     */
    override fun thereExistsUnique(predicate: (A) -> Boolean): Boolean = root.thereExistsUnique(predicate)



// ---------- building vectors ----------------------------

    /**
     * scanLeft is similar to foldLeft, but returns a vector of successively
     *   reduced values from the left.
     *
     * Examples:
     *   [4, 2, 4].scanLeft(64){m -> {n -> m / y}} = [64, 16, 8, 2]
     *   [].scanLeft(3){m -> {n -> m / y}} = [3]
     *   [1, 2, 3, 4].scanLeft(5){m -> {n -> if (m > n) m else n}} = [5, 5, 5, 5, 5]
     *   [1, 2, 3, 4, 5, 6, 7].scanLeft(5){m -> {n -> if (m > n) m else n}} = [5, 5, 5, 5, 5, 5, 6, 7]
     *
     * @param f                 curried binary function
     * @param e                 initial value
     * @return                  new vector
     */
    override fun <B> scanLeft(e: B, f: (B) -> (A) -> B): VectorIF<B> = Vector(root.scanLeft(e, f))

    /**
     * scanRight is the right-to-left dual of scanLeft.
     *
     * Examples:
     *   [1, 2, 3, 4].scanRight(5){m -> {n -> m + n}} = [15, 14, 12, 9, 5]
     *   [8, 12, 24, 4].scanRight(2){m -> {n -> m / n}} = [8, 1, 12, 2, 2]
     *   [].scanRight(3){m -> {n -> m / n}} = [3]
     *   [3, 6, 12, 4, 55, 11].scanRight(18){m -> {n -> if (m > n) m else n}} = [55, 55, 55, 55, 55, 18, 18]
     *
     * @param e                 initial value
     * @param f                 curried binary function
     * @return                  new vector
     */
    override fun <B> scanRight(e: B, f: (A) -> (B) -> B): VectorIF<B> = Vector(root.scanRight(e, f))



// ---------- extracting sublists -------------------------

    /**
     * Return a new vector containing the first n elements from this vector. If n
     *   exceeds the size of this vector, then a copy is returned. If n is
     *   negative or zero, then an empty vector is delivered.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].take(4) = [1, 2, 3, 4]
     *   [1, 2, 3, 4, 5, 6].take(10) = [1, 2, 3, 4, 5, 6]
     *   [1, 2, 3, 4, 5, 6].take(0) = []
     *
     * @param n                 number of elements to extract
     * @return                  new vector of first n elements
     */
    override fun take(n: Int): VectorIF<A> = Vector(root.take(n))

    /**
     * Drop the first n elements from this vector and return a vector containing the
     *   remainder. If n is negative or zero then this vector is returned. If n exceeds
     *   the size of this vector, then an empty vector is delivered.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].drop(4) = [5, 6]
     *   [1, 2, 3, 4, 5, 6].drop(10) = []
     *   [1, 2, 3, 4, 5, 6].drop(0) = [1, 2, 3, 4, 5, 6]
     *
     * @param n                 number of elements to skip
     * @return                  new vector of remaining elements
     */
    override fun drop(n: Int): VectorIF<A> = Vector(root.drop(n))

    /**
     * Return a slice of this vector of the given length starting at the given start
     *   position. A slice of negative or zero length is the empty vector. A slice of
     *   length that exceeds the number of remaining elements returns those remaining
     *   elements. A slice that starts after the final element returns the empty vector.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].slice(2, 3) = [3, 4, 5]
     *   [1, 2, 3, 4, 5, 6].slice(2, 0) = []
     *   [1, 2, 3, 4, 5, 6].slice(2, 10) = [3, 4, 5, 6]
     *   [1, 2, 3, 4, 5, 6].slice(10, 3) = []
     *
     * @param start             start index for the slice
     * @param length            length of the slice
     * @return                  slice of this vector
     */
    override fun slice(from: Int, length: Int): VectorIF<A> = Vector(root.slice(from, length))

    /**
     * Delivers a tuple where first element is prefix of this vector of length n and
     *   second element is the remainder of the vector. If the split is at the first
     *   elements then the first vector is empty and the second vector is the same
     *   as the original. If the split is after the final element, then the first
     *   vector is the original and the second vector is empty.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].splitAt(4) = ([1, 2, 3, 4], [5, 6])
     *   [1, 2, 3, 4, 5, 6].splitAt(0) = ([], [1, 2, 3, 4, 5, 6])
     *   [1, 2, 3, 4, 5, 6].splitAt(10) = ([1, 2, 3, 4, 5, 6], [])
     *
     * @param n                 number of elements into first result vector
     * @return                  pair of two new vectors
     */
    override fun splitAt(n: Int): Pair<VectorIF<A>, VectorIF<A>> = Pair(this.take(n), this.drop(n))

    /**
     * Function takeWhile takes the leading elements from this vector that matches
     *   some predicate.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].takeWhile{n -> (n < 4)} = [1, 2, 3]
     *   [1, 2, 3, 4, 5, 6].takeWhile{n -> (n % 2 == 0)} = []
     *   [1, 2, 3, 4, 5, 6].takeWhile{n -> (n % 2 != 0)} = [1]
     *
     * @param predicate         criteria
     * @return                  new vector of leading elements matching criteria
     */
    override fun takeWhile(predicate: (A) -> Boolean): VectorIF<A> = Vector(root.takeWhile(predicate))

    /**
     * Function dropWhile removes the leading elements from this vector that matches
     *   some predicate.
     *
     * @param predicate         criteria
     * @return                  new vector of remaining elements
     */
    override fun dropWhile(predicate: (A) -> Boolean): VectorIF<A> = Vector(root.dropWhile(predicate))

    /**
     * span applied to a predicate and a vector, returns a tuple where
     *   the first element is longest prefix (possibly empty) of elements
     *   that satisfy predicate and second element is the remainder of the vector.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].span{n -> (n <= 4)} = ([1, 2, 3, 4], [5, 6])
     *   [1, 2, 3, 4, 5, 6].span{n -> (n <= 10)} = ([1, 2, 3, 4, 5, 6], [])
     *   [1, 2, 3, 4, 5, 6].span{n -> (n < 0)} = ([], [1, 2, 3, 4, 5, 6])
     *
     * @param predicate         criteria
     * @return                  pair of two new vectors
     */
    override fun span(predicate: (A) -> Boolean): Pair<VectorIF<A>, VectorIF<A>> {
        val pair: Pair<Node<A>, Node<A>> = root.span(predicate)
        return Pair(Vector(pair.first), Vector(pair.second))
    }



// ---------- predicates ----------------------------------

    /**
     * The isPrefixOf function returns true iff this vector is a prefix of the parameter vector.
     *
     * Examples:
     *   [1, 2, 3, 4].isPrefixOf([1, 2, 3, 4, 5, 6]) = true
     *   [1, 2, 3, 4, 5, 6].isPrefixOf([1, 2, 3, 4, 5, 6]) = true
     *   [].isPrefixOf([1, 2, 3, 4, 5, 6]) = true
     *   [5, 6].isPrefixOf([1, 2, 3, 4, 5, 6]) = false
     *
     * @param xs                existing vector
     * @return                  true if this vector is prefix of given vector
     */
    override fun isPrefixOf(xs: VectorIF<A>): Boolean = root.isPrefixOf(xs.root)

    /**
     * The isSuffixOf function returns true iff the this vector is a suffix of the second.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].isSuffixOf([1, 2, 3, 4, 5, 6]) = true
     *   [5, 6].isSuffixOf([1, 2, 3, 4, 5, 6]) = true
     *   [].isSuffixOf([1, 2, 3, 4, 5, 6]) = true
     *   [1, 2, 3, 4].isSuffixOf([1, 2, 3, 4, 5, 6]) = false
     *
     * @param xs                existing vector
     * @return                  true if this vector is suffix of given vector
     */
    override fun isSuffixOf(xs: VectorIF<A>): Boolean = root.isSuffixOf(xs.root)

    /**
     * The isInfixOf function returns true iff the this vector is a constituent of the argument.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].isInfixOf([1, 2, 3, 4, 5, 6]) = true
     *   [3, 4, 5].isInfixOf([1, 2, 3, 4, 5, 6]) = true
     *   [7, 8].isInfixOf([1, 2, 3, 4, 5, 6]) = false
     *   [].isInfixOf([1, 2, 3, 4, 5, 6]) = true
     *
     * @param xs                existing vector
     * @return                  true if this vector is constituent of second vector
     */
    override fun isInfixOf(xs: VectorIF<A>): Boolean = root.isInfixOf(xs.root)



// ---------- searching with a predicate ------------------

    /**
     * The find function takes a predicate and returns the first
     *   element in the vector matching the predicate, or None if there is no
     *   such element.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].find{n -> (n > 3)} = Some(4)
     *   [1, 2, 3, 4, 5, 6].find{n -> (n % 2 == 0)} = Some(2)
     *   [1, 2, 3, 4, 5, 6].find{n -> (n < 0)} = None
     *
     * @param predicate         criteria
     * @return                  matching element, if found
     */
    override fun find(predicate: (A) -> Boolean): Option<A> = root.find(predicate)

    /**
     * Function filter selects the items from this vector that match the criteria specified
     *   by the function parameter. This is known as a predicate function, and
     *   delivers a boolean result.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].filter{n -> (n % 2 == 0)} = [2, 4, 6]
     *   [].filter{n -> (n % 2 == 0)} = []
     *
     * @param predicate         criteria
     * @return                  new vector of matching elements
     */
    override fun filter(predicate: (A) -> Boolean): VectorIF<A> = Vector(root.filter(predicate))

    /**
     * The partition function takes a predicate and returns the pair
     *   of vector of elements which do and do not satisfy the predicate.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].partition{n -> (n % 2 == 0)} = ([2, 4, 6], [1, 3, 5])
     *   [1, 2, 3, 4, 5, 6].partition{n -> (n < 10)} = ([1, 2, 3, 4, 5, 6], [])
     *   [1, 2, 3, 4, 5, 6].partition{n -> (n < 0)} = ([], [1, 2, 3, 4, 5, 6])
     *
     * @param predicate         criteria
     * @return                  pair of new vectors
     */
    override fun partition(predicate: (A) -> Boolean): Pair<VectorIF<A>, VectorIF<A>> {
        val pair: Pair<Node<A>, Node<A>> = root.partition(predicate)
        return Pair(Vector(pair.first), Vector(pair.second))
    }



// ---------- zipping -------------------------------------

    /**
     * zip returns a vector of corresponding pairs from this vector and the argument vector.
     *   If one input vector is shorter, excess elements of the longer vector are discarded.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].zip([6, 5, 4, 3, 2, 1]) = [(1, 6), (2, 5), (3, 4), (4, 3), (5, 2), (6, 1)]
     *   [1, 2, 3, 4, 5, 6].zip([7, 8]) = [(1, 7), (2, 8)]
     *   [7, 8, 9].zip([1, 2, 3, 4, 5, 6]) = [(7, 1), (8, 2), (9, 3)]
     *   [1, 2, 3, 4, 5, 6].zip([]) = []
     *   [].zip([1, 2, 3, 4, 5, 6]) = []
     *
     * @param xs                existing vector
     * @return                  new vector of pairs
     */
    override fun <B> zip(xs: VectorIF<B>): VectorIF<Pair<A, B>> = Vector(root.zip(xs.root))

    /**
     * zipWith generalises zip by zipping with the function given as the final argument,
     *   instead of a tupling function. For example, zipWith (curried +) is applied to two vectors
     *   to produce the vectors of corresponding sums.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].zipWith([1, 2, 3, 4, 5, 6]){n -> {m -> n + m}} = [2, 4, 6, 8, 10, 12]
     *   [1, 2, 3, 4, 5, 6].zipWith([7, 8]){n -> {m -> n + m}} = [8, 10]
     *   [7, 8, 9].zipWith([1, 2, 3, 4, 5, 6]){n -> {m -> n + m}} = [8, 10, 12]
     *
     * @param xs                existing list
     * @param f                 curried binary function
     * @return                  new list of function results
     */
    override fun <B, C> zipWith(xs: VectorIF<B>, f: (A) -> (B) -> C): VectorIF<C> = Vector(root.zipWith(xs.root, f))

    /**
     * zipWith generalises zip by zipping with the function given as the first argument,
     *   instead of a tupling function. For example, zipWith (uncurried +) is applied to two vectors
     *   to produce the vectors of corresponding sums.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].zipWith([1, 2, 3, 4, 5, 6]){n, m -> n + m} = [2, 4, 6, 8, 10, 12]
     *   [1, 2, 3, 4, 5, 6].zipWith([7, 8]){n, m -> n + m} = [8, 10]
     *   [7, 8, 9].zipWith([1, 2, 3, 4, 5, 6]){n, m -> n + m} = [8, 10, 12]
     *
     * @param xs                existing list
     * @param f                 binary function
     * @return                  new list of function results
     */
    override fun <B, C> zipWith(xs: VectorIF<B>, f: (A, B) -> C): VectorIF<C> = Vector(root.zipWith(xs.root, f))

    /**
     * Zips this vector with the index of its element as a pair.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].zipWithIndex() = [(1, 0), (2, 1), (3, 2), (4, 3), (5, 4), (6, 5)]
     *   [].zipWithIndex() = []
     *
     * @return                  a new vector with the same length as this vector
     */
    override fun zipWithIndex(): VectorIF<Pair<A, Int>> = Vector(root.zipWithIndex())

}   // VectorAB
