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
import com.adt.kotlin.data.immutable.list.List.Nil
import com.adt.kotlin.data.immutable.list.List.Cons
import com.adt.kotlin.data.immutable.list.ListF.cons
import com.adt.kotlin.data.immutable.option.Option

import com.adt.kotlin.hkfp.fp.FunctionF.C2



class NonEmptyList<out A> internal constructor(val hd: A, val tl: List<A>) {

    internal constructor(list: List<A>) : this(list.head(), list.tail())



    /**
     * Collates this list into sub-lists of length size. The function throws
     *   an exception if either size or step is less than one.
     *
     * Examples:
     *   [1 :| 2, 3, 4, 5, 6].collate(2, 1) = [[1, 2] :| [2, 3], [3, 4], [4, 5], [5, 6]]
     *   [1 :| 2, 3, 4, 5, 6].collate(2, 3) = [[1, 2] :| [4, 5]]
     *   [1 :| 2, 3, 4, 5, 6].collate(2, 5) = [[1, 2]]
     *   [1 :| 2, 3, 4, 5, 6].collate(2, 6) = [[1, 2]]
     *   [1 :| 2, 3, 4, 5, 6].collate(3, 1) = [[1, 2, 3] :| [2, 3, 4], [3, 4, 5], [4, 5, 6]]
     *   [1 :| 2, 3, 4, 5, 6].collate(3, 3) = [[1, 2, 3] :| [4, 5, 6]]
     *   [1 :| 2, 3, 4, 5, 6].collate(3, 5) = [[1, 2, 3]]
     *   [1 :| 2, 3, 4, 5, 6].collate(3, 6) = [[1, 2, 3]]
     *   [1 :| 2, 3, 4, 5, 6].collate(6, 1) = [[1, 2, 3, 4, 5, 6]]
     *   [1 :| 2, 3, 4, 5, 6].collate(7, 1) = []
     *
     * @param size              the length of the sub-lists
     * @param step              stepping length
     * @return                  list of sub-lists each of the same size
     */
    fun collate(size: Int, step: Int): List<List<A>> = this.toList().collate(size, step)

    /**
     * Determine if this list contains the element determined by the predicate.
     *
     * Examples:
     *   [1 :| 2, 3, 4].contains{n -> (n == 4)} = true
     *   [1 :| 2, 3, 4].contains{n -> (n == 5)} = false
     *
     * @param predicate         search predicate
     * @return                  true if search element is present, false otherwise
     */
    fun contains(predicate: (A) -> Boolean): Boolean = this.toList().contains(predicate)

    /**
     * Count the number of times a value appears in this list matching the criteria.
     *
     * Examples:
     *   [1 :| 2, 3, 4].count{n -> (n == 2)} = 1
     *   [1 :| 2, 3, 4].count{n -> (n == 5)} = 0
     *   [1 :| 2, 1, 2, 2].count{n -> (n == 2)} == 3
     *
     * @param predicate         the search criteria
     * @return                  the number of occurrences
     */
    fun count(predicate: (A) -> Boolean): Int = this.toList().count(predicate)

    /**
     * Sorts all this list members into groups determined by the supplied mapping
     *   function and counts the group size.  The function should return the key that each
     *   item should be grouped by.  The returned List of Pairs (ala Map) will have an entry
     *   for each distinct key returned from the function, with each value being the frequency of
     *   items occurring for that group.
     *
     * Examples:
     *   [1 :| 3, 2, 4, 5].countBy{m -> (m % 2)} = [(1, 3) :| (0, 2)]
     *   [1 :| 3, 2, 4, 5].countBy{m -> (m % 3)} = [(2, 2) :| (1, 2), (0, 1)]
     *
     * @param mapping      		the mapping function
     * @return			        list of sub-lists as the counted groups
     */
    fun <K> countBy(mapping: (A) -> K): NonEmptyList<Pair<K, Int>> = NonEmptyList(this.toList().countBy(mapping))

    /**
     * Drop the first n elements from this list and return a list containing the
     *   remainder. If n is negative or zero then this list is returned. The size
     *   of the result list will not exceed the size of this list. The result list
     *   is a suffix of this list.
     *
     * Examples:
     *   [1 :| 2, 3, 4].drop(2) = [3, 4]
     *   [1 :| 2, 3, 4].drop(0) = [1, 2, 3, 4]
     *   [1 :| 2, 3, 4].drop(5) = []
     *
     * @param n                 number of elements to skip
     * @return                  new list of remaining elements
     */
    fun drop(n: Int): List<A> = this.toList().drop(n)

    /**
     * Function dropUntil removes the leading elements from this list until a match
     *   against the predicate. The result list size will not exceed this list size.
     *
     * Examples:
     *   [1 :| 2, 3, 4].dropUntil{n -> (n <= 2)} = [1, 2, 3, 4]
     *   [1 :| 2, 3, 4].dropUntil{n -> (n > 3)} = [4]
     *   [1 :| 2, 3, 4].dropUntil{n -> (n <= 5)} = [1, 2, 3, 4]
     *   [1 :| 2, 3, 4].dropUntil{n -> (n <= 0)} = []
     *
     * @param predicate         criteria
     * @return                  new list of remaining elements
     */
    fun dropUntil(predicate: (A) -> Boolean): List<A> = this.toList().dropUntil(predicate)

    /**
     * Function dropWhile removes the leading elements from this list that matches
     *   some predicate. The result list size will not exceed this list size.
     *   The result list is a suffix of this list.
     *
     * Examples:
     *   [1 :| 2, 3, 4].dropWhile{n -> (n <= 2)} = [3, 4]
     *   [1 :| 2, 3, 4].dropWhile{n -> (n <= 5)} = []
     *   [1 :| 2, 3, 4].dropWhile{n -> (n <= 0)} = [1, 2, 3, 4]
     *
     * @param predicate         criteria
     * @return                  new list of remaining elements
     */
    fun dropWhile(predicate: (A) -> Boolean): List<A> = this.toList().dropWhile(predicate)

    /**
     * Drop the last n elements from this list and return a list containing the
     *   remainder. If n is negative or zero then this list is returned. The size
     *   of the result list will not exceed the size of this list. The result list
     *   is a prefix of this list.
     *
     * Examples:
     *   [1 :| 2, 3, 4].dropRight(2) = [1, 2]
     *   [1 :| 2, 3, 4].dropRight(0) = [1, 2, 3, 4]
     *   [1 :| 2, 3, 4].dropRight(5) = []
     *
     * @param n                 number of elements to skip
     * @return                  new list of remaining elements
     */
    fun dropRight(n: Int): List<A> = this.toList().dropRight(n)

    /**
     * Are two lists equal?
     *
     * @param other             the other list
     * @return                  true if both lists are the same; false otherwise
     */
    override fun equals(other: Any?): Boolean {
        tailrec
        fun recEquals(ps: List<A>, qs: List<A>): Boolean {
            return when(ps) {
                is Nil -> {
                    when(qs) {
                        is Nil -> true
                        is Cons -> false
                    }
                }
                is Cons -> {
                    when(qs) {
                        is Nil -> false
                        is Cons -> if (ps.head() != qs.head()) false else recEquals(ps.tail(), qs.tail())
                    }
                }
            }
        }   // recEquals

        return if (this === other)
            true
        else if (other == null || this::class.java != other::class.java)
            false
        else {
            @Suppress("UNCHECKED_CAST") val otherNEL: NonEmptyList<A> = other as NonEmptyList<A>
            (this.hd == otherNEL.hd) && recEquals(this.tl, otherNEL.tl)
        }
    }   // equals

    /**
     * Function filter selects the items from this list that match the criteria specified
     *   by the function parameter. This is known as a predicate function, and
     *   delivers a boolean result. The result list size will be no greater than
     *   this list. The elements of the result list are in the same order as the
     *   original.
     *
     * Examples:
     *   [1 :| 2, 3, 4, 5].filter{n -> (n % 2 == 0} = [2, 4]
     *   [1 :| 3, 5, 7].filter{n -> (n % 2 == 0} = []
     *
     * @param predicate         criteria
     * @return                  new list of matching elements
     */
    fun filter(predicate: (A) -> Boolean): List<A> = this.toList().filter(predicate)

    /**
     * The find function takes a predicate and returns the first
     *   element in the list matching the predicate wrapped in a Some,
     *   or None if there is no such element.
     *
     * Examples:
     *   [1 :| 2, 3, 4].find{n -> (n > 2)} = Some(3)
     *   [1 :| 2, 3, 4].find{n -> (n > 5)} = None
     *
     * @param predicate         criteria
     * @return                  matching element, if found
     */
    fun find(predicate: (A) -> Boolean): Option<A> = this.toList().find(predicate)

    /**
     * foldLeft is a higher-order function that folds a binary function into this
     *   list of values. Effectively:
     *
     *   foldLeft(e, [x1, x2, ..., xn], f) = (...((e f x1) f x2) f...) f xn
     *
     * Examples:
     *   [1 :| 2, 3, 4].foldLeft(0){m -> {n -> m + n}} = 10
     *   [1 :| 2, 3, 4].foldLeft([]){list -> {elem -> list.append(elem)}} = [1, 2, 3, 4]
     *
     * @param e                 initial value
     * @param f                 curried binary function:: B -> A -> B
     * @return                  folded result
     */
    fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B = tl.foldLeft(f(e)(hd), f)

    fun <B> foldLeft(e: B, f: (B, A) -> B): B = this.foldLeft(e, C2(f))

    /**
     * foldRight is a higher-order function that folds a binary function into this
     *   list of values. Fold functions can be the implementation for many other
     *   functions. Effectively:
     *
     *   foldRight(e, [x1, x2, ..., xn], f) = x1 f (x2 f ... (xn f e)...)
     *
     * Examples:
     *   [1 :| 2, 3, 4].foldRight(1){m -> {n -> m * n}} = 24
     *   [1 :| 2, 3, 4].foldRight([]){elem -> {list -> cons(elem, list)}} = [1, 2, 3, 4]
     *
     * @param e                 initial value
     * @param f                 curried binary function:: A -> B -> B
     * @return                  folded result
     */
    fun <B> foldRight(e: B, f: (A) -> (B) -> B): B = f(hd)(tl.foldRight(e, f))

    fun <B> foldRight(e: B, f: (A, B) -> B): B = this.foldRight(e, C2(f))

    /**
     * All the elements of this list meet some criteria.
     *
     * Examples:
     *   [1 :| 2, 3, 4].forAll{m -> (m > 0)} = true
     *   [1 :| 2, 3, 4].forAll{m -> (m > 2)} = false
     *   [1 :| 2, 3, 4].forAll{m -> true} = true
     *   [1 :| 2, 3, 4].forAll{m -> false} = false
     *
     * @param predicate         criteria
     * @return                  true if all elements match criteria
     */
    fun forAll(predicate: (A) -> Boolean): Boolean = this.toList().forAll(predicate)

    /**
     * Return the element at the specified position in this list, where
     *   index 0 denotes the first element.
     * Throws a ListException if the index is out of bounds, i.e. if
     *   index does not satisfy 0 <= index < length.
     *
     * Examples:
     *   [1 :| 2, 3, 4].get(0) = 1
     *   [1 :| 2, 3, 4].get(3) = 4
     *   [1 :| 2, 3, 4][2] = 3
     *
     * @param index             position in list
     * @return                  the element at the specified position in the list
     */
    operator fun get(index: Int): A = this.toList().get(index)

    /**
     * The group function takes this list and returns a list of lists such that the
     *   concatenation of the result is equal to this list.  Moreover, each
     *   sublist in the result contains only equal elements.
     *
     * Examples:
     *   [1 :| 2, 2, 1, 1, 1, 2, 2, 2, 1].group() = [[1], [2, 2], [1, 1, 1], [2, 2, 2], [1]]
     *
     * @return			        list of sub-lists as the groups
     */
    fun group(): List<List<A>> = this.toList().group()

    /**
     * Sorts all this list members into groups determined by the supplied mapping
     *   function.  The function should return the key that each item should be grouped by.
     *   The returned List of Pairs (ala Map) will have an entry for each distinct key returned
     *   from the function.
     *
     * Examples:
     *   [1 :| 2, 3, 4, 5].groupBy{m -> (m % 2)} = [(1, [5, 3, 1]), (0, [4, 2])]
     *   [1 :| 2, 3, 4, 5].groupBy{m -> (m % 3)} = [(2, [5, 2]), (1, [4, 1]), (0, [3])]
     *
     * @param mapping		    curried grouping condition
     * @return			        list of sub-lists as the groups
     */
    fun <K> groupBy(mapping: (A) -> K): List<Pair<K, List<A>>> = this.toList().groupBy(mapping)

    /**
     * Extract the first element of this list.
     *
     * Examples:
     *   [1 :| 2, 3, 4].head() = 1
     *   [5 :|].head() = 5
     *
     * @return                  the element at the front of the list
     */
    fun head(): A = hd

    /**
     * Find the index of the first occurrence of the given value, or -1 if absent.
     *
     * Examples:
     *   [1 :| 2, 3, 4].indexOf{n -> (n == 1)} = 0
     *   [1 :| 2, 3, 4].indexOf{n -> (n == 3)} = 2
     *   [1 :| 2, 3, 4].indexOf{n -> (n == 5)} = -1
     *
     * @param predicate         the search predicate
     * @return                  the index position
     */
    fun indexOf(predicate: (A) -> Boolean): Int = this.toList().indexOf(predicate)

    /**
     * Return all the elements of this list except the last one.
     *
     * Examples:
     *   [1 :| 2, 3, 4].init() = [1, 2, 3]
     *   [5 :|].init() = []
     *
     * @return                  new list of the initial elements
     */
    fun init(): List<A> = this.toList().init()

    /**
     * The inits function returns all initial segments of this list,
     *   shortest first. The result list size will exceed this list
     *   size by one. The first element of the result list is guaranteed
     *   to be the empty sub-list; the final element of the result list
     *   is guaranteed to be the same as the original list. All sub-lists
     *   of the result list are a prefix to the original.
     *
     * Expensive operation on large lists.
     *
     * Examples:
     *   [1 :| 2, 3].inits() = [[] :| [1], [1, 2], [1, 2, 3]]
     *   [1 :| 2, 3].inits().size() = 1 + [1, 2, 3].size()
     *   [1 :| 2, 3].inits().head() = []
     *   [1 :| 2, 3].inits().last() = [1, 2, 3]
     *
     * @return                  new list of initial segment sub-lists
     */
    fun inits(): List<List<A>> = this.toList().inits()

    /**
     * Return true if all the elements differ, otherwise false.
     *
     * Expensive operation on large lists.
     *
     * Examples:
     *   [1 :| 2, 3, 4].isDistinct() = true
     *   [1 :| 2, 3, 4, 1].isDistinct() = false
     *
     * @return                  true if all the elements are distinct
     */
    fun isDistinct(): Boolean = this.toList().isDistinct()

    /**
     * Extract the last element of this list.
     *
     * Examples:
     *   [1 :| 2, 3, 4].last() = 4
     *   [5].last() = 5
     *
     * @return                  final element in the list
     */
    fun last(): A = if (tl.isEmpty()) hd else tl.last()

    /**
     * Obtain the length of this list.
     *
     * Examples:
     *   [1 :| 2, 3, 4].length() == 4
     *
     * @return                  number of elements in the list
     */
    fun length(): Int = 1 + tl.length()

    /**
     * Compose all the elements of this list as a string using the separator, prefix, postfix, etc.
     *
     * Examples:
     *   [1 :| 2, 3, 4].makeString() = "1, 2, 3, 4"
     *   [1 :| 2, 3, 4].makeString(", ", "[", "]") = "[1, 2, 3, 4]"
     *   [1 :| 2, 3, 4].makeString(", ", "[", "]", 2) = "[1, 2, ...]"
     *
     * @param separator         the separator between each element
     * @param prefix            the leading content
     * @param postfix           the trailing content
     * @param limit             constrains the output to the fist limit elements
     * @param truncated         indicator that the output has been limited
     * @return                  the list content
     */
    fun makeString(separator: String = ", ", prefix: String = "", postfix: String = "", limit: Int = -1, truncated: String = "..."): String {
        val tlString: String = tl.makeString(separator, "", "", limit - 1, truncated)
        return "$prefix$hd :| $tlString$postfix"
    }   // makeString

    /**
     * Function map applies the function parameter to each item in this list, delivering
     *   a new list. The result list has the same size as this list.
     *
     * Examples:
     *   [1 :| 2, 3, 4].map{n -> n + 1} = [2 :| 3, 4, 5]
     *
     * @param f                 pure function:: A -> B
     * @return                  new list of transformed values
     */
    fun <B> map(f: (A) -> B): NonEmptyList<B> = NonEmptyList(f(hd), tl.map(f))

    /**
     * Return a list of pairs of the adjacent elements from this list.
     *
     * Examples:
     *   [1 :| 2, 3, 4].pairwise() = [(1, 2) :| (2, 3), (3, 4)]
     *   [3 :| 4].pairwise() = [(3, 4)]
     *   [5 :| ].pairwise = []
     *
     * @return                  list of adjacent pairs
     */
    fun pairwise(): List<Pair<A, A>> = this.toList().pairwise()

    /**
     * The partition function takes a predicate and returns the pair
     *   of lists of elements which do and do not satisfy the predicate.
     *   The sum of the sizes of the two result lists will equal the size
     *   of the original list. Both sub-lists are ordered permutations of
     *   this list.
     *
     * Examples:
     *   [1 :| 2, 3, 4, 5].partition{n -> (n % 2 == 0)} = ([2, 4], [1, 3, 5])
     *   [2 :| 4, 6, 8].partition{n -> (n % 2 == 0)} = ([2, 4, 6, 8], [])
     *   [1 :| 3, 5, 7].partition{n -> (n % 2 == 0)} = ([], [2, 4, 6, 8])
     *
     * @param predicate         criteria
     * @return                  pair of new lists
     */
    fun partition(predicate: (A) -> Boolean): Pair<List<A>, List<A>> = this.toList().partition(predicate)

    /**
     * Remove the first occurrence of the matching element from this list. The result list
     *   will either have the same size as this list (if no such element is present) or
     *   will have the size of this list less one.
     *
     * Examples:
     *   [1 :| 2, 3, 4].remove{n -> (n == 4)} = [1, 2, 3]
     *   [1 :| 2, 3, 4].remove{n -> (n == 5)} = [1, 2, 3, 4]
     *   [4 :| 4, 4, 4].remove{n -> (n == 4)} = [4, 4, 4]
     *   [4 :| ].remove{n -> (n == 4)} = []
     *
     * @param predicate         search predicate
     * @return                  new list with element deleted
     */
    fun remove(predicate: (A) -> Boolean): List<A> = this.toList().remove(predicate)

    /**
     * The removeAll function removes all the elements from this list that match
     *   a given criteria. The result list size will not exceed this list size.
     *
     * Examples:
     *   [1 :| 2, 3, 4].removeAll{n -> (n % 2 == 0)} = [1, 3]
     *   [1 :| 2, 3, 4].removeAll{n -> (n > 4)} = [1, 2, 3, 4]
     *   [1 :| 2, 3, 4].removeAll{n -> (n > 0)} = []
     *   [1 :| 4, 2, 3, 4].removeAll{n -> (n == 4)} = [1, 2, 3]
     *   [4 :| 4, 4, 4, 4].removeAll{n -> (n == 4)} = []
     *
     * @param predicate		    criteria
     * @return          		new list with all matching elements removed
     */
    fun removeAll(predicate: (A) -> Boolean): List<A> = this.toList().removeAll(predicate)

    /**
     * The removeDuplicates function removes duplicate elements from this list.
     *   In particular, it keeps only the first occurrence of each element. The
     *   size of the result list is either less than or equal to the original.
     *   The elements in the result list are all drawn from the original. The
     *   elements in the result list are in the same order as found in the original.
     *
     * Expensive operation on large lists.
     *
     * Examples:
     *   [1 :| 2, 1, 2, 3].removeDuplicates = [1, 2, 3]
     *   [1 :| 1, 3, 2, 1, 3, 2, 4].removeDuplicates = [1, 3, 2, 4]
     *   [4 :| 4, 4, 4].removeDuplicates() = [4]
     *
     * @return                  new list with all duplicates removed
     */
    fun removeDuplicates(): List<A> = this.toList().removeDuplicates()

    /**
     * Reverses the content of this list into a new list. The size of the result list
     *   is the same as this list.
     *
     * Examples:
     *   [1 :| 2, 3, 4].reverse() = [4 :| 3, 2, 1]
     *   [1 :| ].reverse() = [1 :| ]
     *
     * @return                  new list of elements reversed
     */
    fun reverse(): NonEmptyList<A> = NonEmptyList(this.toList().reverse())

    /**
     * scanLeft is similar to foldLeft, but returns a list of successively
     *   reduced values from the left.
     *
     * Examples:
     *   [4 :| 2, 4].scanLeft(64){m -> {n -> m / y}} = [64 :| 16, 8, 2]
     *   [1 :| 2, 3, 4].scanLeft(5){m -> {n -> if (m > n) m else n}} = [5 :| 5, 5, 5, 5]
     *   [1 :| 2, 3, 4, 5, 6, 7].scanLeft(5){m -> {n -> if (m > n) m else n}} = [5 :| 5, 5, 5, 5, 5, 6, 7]
     *
     * @param f                 curried binary function
     * @param e                 initial value
     * @return                  new list
     */
    fun <B> scanLeft(e: B, f: (B) -> (A) -> B): NonEmptyList<B> = NonEmptyList(this.toList().scanLeft(e, f))

    fun <B> scanLeft(e: B, f: (B, A) -> B): NonEmptyList<B> = this.scanLeft(e, C2(f))

    /**
     * scanRight is the right-to-left dual of scanLeft.
     *
     * Examples:
     *   [1 :| 2, 3, 4].scanRight(5){m -> {n -> m + n}} = [15 :| 14, 12, 9, 5]
     *   [8 :| 12, 24, 4].scanRight(2){m -> {n -> m / n}} = [8 :| 1, 12, 2, 2]
     *   [3 :| 6, 12, 4, 55, 11].scanRight(18){m -> {n -> if (m > n) m else n}} = [55 :| 55, 55, 55, 55, 18, 18]
     *
     * @param e                 initial value
     * @param f                 curried binary function
     * @return                  new list
     */
    fun <B> scanRight(e: B, f: (A) -> (B) -> B): NonEmptyList<B> = NonEmptyList(this.toList().scanRight(e, f))

    fun <B> scanRight(e: B, f: (A, B) -> B): NonEmptyList<B> = this.scanRight(e, C2(f))

    /**
     * Obtain the size of this list; equivalent to length.
     *
     * Examples:
     *   [1 :| 2, 3, 4].size() == 4
     *
     * @return                  number of elements in the list
     */
    fun size(): Int = this.length()

    /**
     * Return a new list that is a sub-list of this list. The sub-list begins at
     *   the specified from and extends to the element at index to - 1. Thus the
     *   length of the sub-list is to-from. Degenerate slice indices are handled
     *   gracefully: an index that is too large is replaced by the list size, an
     *   upper bound smaller than the lower bound returns an empty list. The size
     *   of the result list does not exceed the size of this list. The result list
     *   is an infix of this list.
     *
     * Examples:
     *   [1 :| 2, 3, 4].slice(0, 2) = [1, 2]
     *   [1 :| 2, 3, 4].slice(2, 2) = []
     *   [1 :| 2, 3, 4].slice(2, 0) = []
     *   [1 :| 2, 3, 4].slice(0, 7) = [1, 2, 3, 4]
     *
     * @param from              the start index, inclusive
     * @param to                the end index, exclusive
     * @return                  the sub-list of this list
     */
    fun slice(from: Int, to: Int): List<A> = this.toList().slice(from, to)

    /**
     * Sort the elements of this list into ascending order and deliver
     *   the resulting list. The elements are compared using the given
     *   comparator.
     *
     * Examples:
     *   [4 :| 3, 2, 1].sort{x, y -> if (x < y) -1 else if (x > y) +1 else 0} = [1 :| 2, 3, 4]
     *   [1 :| 2, 3, 4].sort{x, y -> if (x < y) -1 else if (x > y) +1 else 0} = [1 :| 2, 3, 4]
     *   ["Ken" :| "John", "Jessie", "", ""].sort{str1, str2 -> str1.compareTo(str2)} = ["" :| "", "Jessie", "John", "Ken"]
     *
     * @param comparator        element comparison function
     * @return                  the sorted list
     */
    fun sort(comparator: (A, A) -> Int): NonEmptyList<A> = NonEmptyList(this.toList().sort(comparator))

    /**
     * span applied to a predicate and a list xs, returns a tuple where
     *   the first element is longest prefix (possibly empty) of xs of elements
     *   that satisfy predicate and second element is the remainder of the list.
     *   The sum of the sizes of the two result lists equals the size of this list.
     *   The first result list is a prefix of this list and the second result list
     *   is a suffix of this list.
     *
     * Examples:
     *   [1 :| 2, 3, 4, 1, 2, 3, 4].span{n -> (n < 3)} = ([1, 2], [3, 4, 1, 2, 3, 4])
     *   [1 :| 2, 3].span{n -> (n < 9)} = ([1, 2, 3], [])
     *   [1 :| 2, 3].span{n -> (n < 0)} = ([], [1, 2, 3])
     *
     * @param predicate         criteria
     * @return                  pair of two new lists
     */
    fun span(predicate: (A) -> Boolean): Pair<List<A>, List<A>> = this.toList().span(predicate)

    /**
     * Delivers a tuple where first element is prefix of this list of length n and
     *   second element is the remainder of the list. The sum of the sizes of the
     *   two result lists equal the size of this list. The first result list is a
     *   prefix of this list. The second result list is a suffix of this list. The
     *   second result list appended on to the first result list is equal to this
     *   list.
     *
     * Examples:
     *   [1 :| 2, 3, 4].splitAt(2) = ([1, 2], [3, 4])
     *   [1 :| 2, 3, 4].splitAt(0) = ([], [1, 2, 3, 4])
     *   [1 :| 2, 3, 4].splitAt(5) = ([1, 2, 3, 4], [])
     *
     * @param n                 number of elements into first result list
     * @return                  pair of two new lists
     */
    fun splitAt(n: Int): Pair<List<A>, List<A>> = this.toList().splitAt(n)

    /**
     * Extract the possibly empty tail of the list.
     *
     * Examples:
     *   [1 :| 2, 3, 4].tail() = [2, 3, 4]
     *   [5].tail() = []
     *
     * @return                  new list of the tail elements
     */
    fun tail(): List<A> = tl

    /**
     * The tails function returns all final segments of this list,
     *   longest first. The result list size will exceed this list
     *   size by one. The first element of the result list is guaranteed
     *   to be the same as the original list; the final element of the
     *   result list is guaranteed to be the empty sub-list. All sub-lists
     *   of the result list are a prefix to the original.
     *
     * Examples:
     *   [1 :| 2, 3].tails() = [[1, 2, 3] :| [2, 3], [3], []]
     *   [1 :| 2, 3].tails().size() = 1 + [1, 2, 3].size()
     *   [1 :| 2, 3].tails().head() = [1, 2, 3]
     *   [1 :| 2, 3].tails().last() = []
     *
     * @return                  new list of final segments sub-lists
     */
    fun tails(): NonEmptyList<List<A>> = NonEmptyList(this.toList().tails())

    /**
     * Return a new list containing the first n elements from this list. If n
     *    exceeds the size of this list, then a copy is returned. If n is
     *    negative or zero, then an empty list is delivered. The size of
     *    the result list will not exceed the size of this list. The
     *    result list is a prefix of this list.
     *
     *  Examples:
     *    [1 :| 2, 3, 4].take(2) = [1, 2]
     *    [1 :| 2, 3, 4].take(0) = []
     *    [1 :| 2, 3, 4].take(5) = [1, 2, 3, 4]
     *
     * @param n                 number of elements to extract
     * @return                  new list of first n elements
     */
    fun take(n: Int): List<A> = this.toList().take(n)

    /**
     * Return a new list containing the last n elements from this list. If n
     *    exceeds the size of this list, then a copy is returned. If n is
     *    negative or zero, then an empty list is delivered. The size of
     *    the result list will not exceed the size of this list. The
     *    result list is a prefix of this list.
     *
     *  Examples:
     *    [1 :| 2, 3, 4].takeRight(2) = [3, 4]
     *    [1 :| 2, 3, 4].takeRight(0) = []
     *    [1 :| 2, 3, 4].takeRight(5) = [1, 2, 3, 4]
     *
     * @param n                 number of elements to extract
     * @return                  new list of first n elements
     */
    fun takeRight(n: Int): List<A> = this.toList().takeRight(n)

    /**
     * Function takeUntil retrieves the leading elements from this list that match
     *   some predicate. The result list size will not exceed this list size.
     *   The result list is a prefix of this list.
     *
     * Examples:
     *   [1 :| 2, 3, 4].takeUntil{n -> (n <= 2)} = []
     *   [1 :| 2, 3, 4].takeUntil{n -> (n > 5)} = [1, 2, 3, 4]
     *   [1 :| 2, 3, 4].takeUntil{n -> (n > 3)} = [1, 2, 3]
     *
     * @param predicate         criteria
     * @return                  new list of trailing elements matching criteria
     */
    fun takeUntil(predicate: (A) -> Boolean): List<A> = this.toList().takeUntil(predicate)

    /**
     * Function takeWhile takes the leading elements from this list that matches
     *   some predicate. The result list size will not exceed this list size.
     *   The result list is a prefix of this list.
     *
     * Examples:
     *   [1 :| 2, 3, 4].takeWhile{n -> (n <= 2)} = [1, 2]
     *   [1 :| 2, 3, 4].takeWhile{n -> (n <= 5)} = [1, 2, 3, 4]
     *   [1 :| 2, 3, 4].takeWhile{n -> (n <= 0)} = []
     *
     * @param predicate         criteria
     * @return                  new list of leading elements matching criteria
     */
    fun takeWhile(predicate: (A) -> Boolean): List<A> = this.toList().takeWhile(predicate)

    /**
     * There exists at least one element of this list that meets some criteria. If
     *   the list is empty then false is returned.
     *
     * Examples:
     *   [1 :| 2, 3, 4].thereExists{m -> (m > 0)} = true
     *   [1 :| 2, 3, 4].thereExists{m -> (m > 2)} = true
     *   [1 :| 2, 3, 4].thereExists{m -> (m > 4)} = false
     *   [1 :| 2, 3, 4].thereExists{m -> true} = true
     *   [1 :| 2, 3, 4].thereExists{m -> false} = false
     *
     * @param predicate         criteria
     * @return                  true if at least one element matches the criteria
     */
    fun thereExists(predicate: (A) -> Boolean): Boolean = this.toList().thereExists(predicate)

    /**
     * There exists only one element of this list that meets some criteria. If the
     *   list is empty then false is returned.
     *
     * Examples:
     *   [1 :| 2, 3, 4].thereExistsUnique{m -> (m == 2)} = true
     *   [1 :| 2, 3, 4].thereExistsUnique{m -> (m == 5)} = false
     *   [1 :| 2, 3, 4].thereExistsUnique{m -> true} = false
     *   [1 :| 2, 3, 4].thereExistsUnique{m -> false} = false
     *
     * @param predicate         criteria
     * @return                  true if only one element matches the criteria
     */
    fun thereExistsUnique(predicate: (A) -> Boolean): Boolean = this.toList().thereExistsUnique(predicate)

    /**
     * Convert this to an immutable list.
     */
    fun toList(): List<A> = cons(hd, tl)

    /**
     * Produce a string representation of a list.
     *
     * @return                  string as <[ ... ]>
     */
    override fun toString(): String = this.makeString(", ", "<[", "]>")

    /**
     * zip returns a list of corresponding pairs from this list and the argument list.
     *   If one input list is shorter, excess elements of the longer list are discarded.
     *
     * Examples:
     *   [1 :| 2, 3].zip([4 :| 5, 6]) = [(1, 4) :| (2, 5), (3, 6)]
     *   [1 :| 2].zip([4 :| 5, 6]) = [(1, 4) :| (2, 5)]
     *   [1 :| 2, 3].zip([4 :| 5]) = [(1, 4) :| (2, 5)]
     *
     * @param xs                existing list
     * @return                  new list of pairs
     */
    fun <B> zip(xs: NonEmptyList<B>): NonEmptyList<Pair<A, B>> = NonEmptyList(this.toList().zip(xs.toList()))

    /**
     * zipWith generalises zip by zipping with the function given as the first argument,
     *   instead of a tupling function. For example, zipWith (+) is applied to two lists
     *   to produce the list of corresponding sums. The size of the resulting list will
     *   equal the size of the smaller two lists.
     *
     * Examples:
     *   [1 :| 2, 3].zipWith([4 :| 5, 6]){m -> {n -> m + n}} = [5 :| 7, 9]
     *
     * @param xs                existing list
     * @param f                 curried binary function
     * @return                  new list of function results
     */
    fun <B, C> zipWith(xs: NonEmptyList<B>, f: (A) -> (B) -> C): NonEmptyList<C> = NonEmptyList(this.toList().zipWith(xs.toList(), f))

    fun <B, C> zipWith(xs: NonEmptyList<B>, f: (A, B) -> C): NonEmptyList<C> = this.zipWith(xs, C2(f))

    /**
     * Zips this list with the index of its element as a pair. The result list
     *   has the same size as this list.
     *
     * Examples:
     *   [1 :| 2, 3, 4].zipWithIndex() = [(1, 0) :| (2, 1), (3, 2), (4, 3)]
     *
     * @return                  a new list with the same length as this list
     */
    fun zipWithIndex(): NonEmptyList<Pair<A, Int>> = NonEmptyList(this.toList().zipWithIndex())

}   // NonEmptyList
