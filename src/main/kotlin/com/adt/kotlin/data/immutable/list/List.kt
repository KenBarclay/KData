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

import com.adt.kotlin.data.immutable.list.ListF.empty

import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.stream.Stream
import com.adt.kotlin.data.immutable.stream.StreamF

import com.adt.kotlin.hkfp.fp.FunctionF.C2



sealed class List<out A> : Iterable<A> {



    object Nil : List<Nothing>() {

        override fun isEmpty(): Boolean = true

    }   // Nil



    class Cons<A> internal constructor(val hd: A, internal var tl : List<A>) : List<A>() {

        override fun isEmpty(): Boolean = false

    }   // Cons



    /**
     * Test whether this list is empty.
     *
     * Examples:
     *   [1, 2, 3, 4].isEmpty() = false
     *   [].isEmpty() = true
     *
     * @return                  true if list is empty; false otherwise
     */
    abstract fun isEmpty(): Boolean



    /**
     * Collates this list into sub-lists of length size. The function throws
     *   an exception if either size or step is less than one.
     *
     * Examples:
     *   [1, 2, 3, 4, 5, 6].collate(2, 1) = [[1, 2], [2, 3], [3, 4], [4, 5], [5, 6]]
     *   [1, 2, 3, 4, 5, 6].collate(2, 3) = [[1, 2], [4, 5]]
     *   [1, 2, 3, 4, 5, 6].collate(2, 5) = [[1, 2]]
     *   [1, 2, 3, 4, 5, 6].collate(2, 6) = [[1, 2]]
     *   [1, 2, 3, 4, 5, 6].collate(3, 1) = [[1, 2, 3], [2, 3, 4], [3, 4, 5], [4, 5, 6]]
     *   [1, 2, 3, 4, 5, 6].collate(3, 3) = [[1, 2, 3], [4, 5, 6]]
     *   [1, 2, 3, 4, 5, 6].collate(3, 5) = [[1, 2, 3]]
     *   [1, 2, 3, 4, 5, 6].collate(3, 6) = [[1, 2, 3]]
     *   [1, 2, 3, 4, 5, 6].collate(6, 1) = [[1, 2, 3, 4, 5, 6]]
     *   [1, 2, 3, 4, 5, 6].collate(7, 1) = []
     *
     * @param size              the length of the sub-lists
     * @param step              stepping length
     * @return                  list of sub-lists each of the same size
     */
    fun collate(size: Int, step: Int): List<List<A>> {
        tailrec
        fun recCollate(sz: Int, st: Int, ps: List<A>, acc: ListBufferIF<List<A>>): List<List<A>> {
            return if (ps.size() < sz)
                acc.toList()
            else
                recCollate(sz, st, ps.drop(st), acc.append(ps.take(sz)))
        }   // recCollate

        return if (size < 1)
            throw ListException("collate: incorrect size: ${size}")
        else if (step < 1)
            throw ListException("collate: incorrect step: ${step}")
        else
            recCollate(size, step, this, ListBuffer())
    }   // collate

    /**
     * Determine if this list contains the element determined by the predicate.
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
        tailrec
        fun recContains(predicate: (A) -> Boolean, ps: List<A>): Boolean {
            return when(ps) {
                is Nil -> false
                is Cons -> if (predicate(ps.head())) true else recContains(predicate, ps.tail())
            }
        }   // recContains

        return recContains(predicate, this)
    }   // contains

    /**
     * Count the number of times a value appears in this list matching the criteria.
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
        tailrec
        fun recCount(predicate: (A) -> Boolean, ps: List<A>, acc: Int): Int {
            return when(ps) {
                is Nil -> acc
                is Cons -> recCount(predicate, ps.tail(), if (predicate(ps.head())) 1 + acc else acc)
            }
        }   // recCount

        return recCount(predicate, this, 0)
    }   // count

    /**
     * Sorts all this list members into groups determined by the supplied mapping
     *   function and counts the group size.  The function should return the key that each
     *   item should be grouped by.  The returned List of Pairs (ala Map) will have an entry
     *   for each distinct key returned from the function, with each value being the frequency of
     *   items occurring for that group.
     *
     * Examples:
     *   [1, 3, 2, 4, 5].countBy{m -> (m % 2)} = [(1, 3), (0, 2)]
     *   [1, 3, 2, 4, 5].countBy{m -> (m % 3)} = [(2, 2), (1, 2), (0, 1)]
     *   [].countBy{m -> (m % 2)} = []
     *
     * @param mapping      		the mapping function
     * @return			        list of sub-lists as the counted groups
     */
    fun <K> countBy(mapping: (A) -> K): List<Pair<K, Int>> {
        fun count(k: K, bs: List<Pair<K, Int>>): List<Pair<K, Int>> {
            val opt: Option<Pair<K, Int>> = bs.find{ pr: Pair<K, Int> -> (k == pr.first)}
            return when(opt) {
                is Option.None -> Cons(Pair(k, 1), bs)
                is Option.Some<Pair<K, Int>> -> {
                    val pair: Pair<K, Int> = opt.get()
                    val bss: List<Pair<K, Int>> = bs.filter{pr: Pair<K, Int> -> (k != pr.first)}
                    Cons(Pair(k, 1 + pair.second), bss)
                }
            }
        }   // count

        tailrec
        fun recCountBy(mapping: (A) -> K, ps: List<A>, bs: List<Pair<K, Int>>): List<Pair<K, Int>> {
            return when(ps) {
                is Nil -> bs
                is Cons -> {
                    val x: A = ps.head()
                    val k: K = mapping(x)
                    val counted: List<Pair<K, Int>> = count(k, bs)
                    recCountBy(mapping, ps.tail(), counted)
                }
            }
        }   // recCountBy

        return recCountBy(mapping, this, Nil)
    }   // countBy

    /**
     * Drop the first n elements from this list and return a list containing the
     *   remainder. If n is negative or zero then this list is returned. The size
     *   of the result list will not exceed the size of this list. The result list
     *   is a suffix of this list.
     *
     * Examples:
     *   [1, 2, 3, 4].drop(2) = [3, 4]
     *   [1, 2, 3, 4].drop(0) = [1, 2, 3, 4]
     *   [1, 2, 3, 4].drop(5) = []
     *   [].drop(2) = []
     *
     * @param n                 number of elements to skip
     * @return                  new list of remaining elements
     */
    fun drop(n: Int): List<A> {
        tailrec
        fun recDrop(m: Int, xs: List<A>): List<A> {
            return if (m <= 0)
                xs
            else when(xs) {
                is Nil -> xs
                is Cons -> recDrop(m - 1, xs.tail())
            }
        }   // recDrop

        return recDrop(n, this)
    }   // drop

    /**
     * Function dropUntil removes the leading elements from this list until a match
     *   against the predicate. The result list size will not exceed this list size.
     *   The result list is a suffix of this list.
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
    fun dropUntil(predicate: (A) -> Boolean): List<A> {
        tailrec
        fun recDropUntil(predicate: (A) -> Boolean, xs: List<A>): List<A> {
            return when (xs) {
                is Nil -> Nil
                is Cons -> if (predicate(xs.head())) xs else recDropUntil(predicate, xs.tail())
            }
        }   // recDropUntil

        return recDropUntil(predicate, this)
    }   // dropUntil

    /**
     * Function dropWhile removes the leading elements from this list that matches
     *   some predicate. The result list size will not exceed this list size.
     *   The result list is a suffix of this list.
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
    fun dropWhile(predicate: (A) -> Boolean): List<A> {
        tailrec
        fun recDropWhile(predicate: (A) -> Boolean, xs: List<A>): List<A> {
            return when(xs) {
                is Nil -> xs
                is Cons -> if(!predicate(xs.head())) xs else recDropWhile(predicate, xs.tail())
            }
        }   // recDropWhile

        return recDropWhile(predicate, this)
    }   // dropWhile

    /**
     * Drop the last n elements from this list and return a list containing the
     *   remainder. If n is negative or zero then this list is returned. The size
     *   of the result list will not exceed the size of this list. The result list
     *   is a prefix of this list.
     *
     * Examples:
     *   [1, 2, 3, 4].dropRight(2) = [1, 2]
     *   [1, 2, 3, 4].dropRight(0) = [1, 2, 3, 4]
     *   [1, 2, 3, 4].dropRight(5) = []
     *   [].dropRight(2) = []
     *
     * @param n                 number of elements to skip
     * @return                  new list of remaining elements
     */
    fun dropRight(n: Int): List<A> =
        this.take(this.length() - n)

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
            @Suppress("UNCHECKED_CAST") val otherList: List<A> = other as List<A>
            recEquals(this, otherList)
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
     *   [].filter{n -> (n % 2 == 0} = []
     *   [1, 2, 3, 4, 5].filter{n -> (n % 2 == 0} = [2, 4]
     *   [1, 3, 5, 7].filter{n -> (n % 2 == 0} = []
     *
     * @param predicate         criteria
     * @return                  new list of matching elements
     */
    fun filter(predicate: (A) -> Boolean): List<A> {
        tailrec
        fun recFilter(predicate: (A) -> Boolean, ps: List<A>, acc: ListBufferIF<A>): List<A> {
            return when(ps) {
                is Nil -> acc.toList()
                is Cons -> if (!predicate(ps.head())) recFilter(predicate, ps.tail(), acc) else recFilter(predicate, ps.tail(), acc.append(ps.head()))
            }
        }   // recFilter

        return recFilter(predicate, this, ListBuffer())
    }   // filter

    /**
     * The find function takes a predicate and returns the first
     *   element in the list matching the predicate wrapped in a Some,
     *   or None if there is no such element.
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
        tailrec
        fun recFind(predicate: (A) -> Boolean, ps: List<A>): Option<A> {
            return when(ps) {
                is Nil -> Option.None
                is Cons -> if (predicate(ps.head())) Option.Some(ps.head()) else recFind(predicate, ps.tail())
            }
        }   // recFind

        return recFind(predicate, this)
    }   // find

    /**
     * foldLeft is a higher-order function that folds a binary function into this
     *   list of values. Effectively:
     *
     *   foldLeft(e, [x1, x2, ..., xn], f) = (...((e f x1) f x2) f...) f xn
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
        tailrec
        fun recFoldLeft(list: List<A>, e: B, f: (B) -> (A) -> B): B {
            return when (list) {
                is Nil -> e
                is Cons -> recFoldLeft(list.tl, f(e)(list.hd), f)
            }
        }   // recFoldLeft

        return recFoldLeft(this, e, f)
    }   // foldLeft

    fun <B> foldLeft(e: B, f: (B, A) -> B): B = this.foldLeft(e, C2(f))

    /**
     * foldRight is a higher-order function that folds a binary function into this
     *   list of values. Fold functions can be the implementation for many other
     *   functions. Effectively:
     *
     *   foldRight(e, [x1, x2, ..., xn], f) = x1 f (x2 f ... (xn f e)...)
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
    fun <B> foldRight(e: B, f: (A) -> (B) -> B): B = this.reverse().foldLeft(e){b: B -> {a: A -> f(a)(b)}}    /*****{
    fun recFoldRight(list: List<A>, e: B, f: (A) -> (B) -> B): B {
    return when (list) {
    is Nil -> e
    is Cons -> f(list.hd)(recFoldRight(list.tl, e, f))
    }
    }   // recFoldRight

    return recFoldRight(this, e, f)
    }   // foldRight
     *****/

    fun <B> foldRight(e: B, f: (A, B) -> B): B = this.foldRight(e, C2(f))

    /**
     * All the elements of this list meet some criteria. If the list is empty then
     *    true is returned.
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
        tailrec
        fun recForAll(predicate: (A) -> Boolean, ps: List<A>): Boolean {
            return when(ps) {
                is Nil -> true
                is Cons -> if (!predicate(ps.head())) false else recForAll(predicate, ps.tail())
            }
        }   // recForAll

        return recForAll(predicate, this)
    }   // forAll

    /**
     * Apply the block to each element in the list.
     *
     * @param block                 body of program block
     */
    fun forEach(block: (A) -> Unit): Unit {
        fun recForEach(list: List<A>, block: (A) -> Unit) {
            when (list) {
                is Nil -> Unit
                is Cons -> {
                    block(list.head())
                    recForEach(list.tail(), block)
                }
            }
        }   // recForEach

        recForEach(this, block)
        ////for(a: A in this.iterator())
        ////    block(a)
    }   // forEach

    /**
     * Return the element at the specified position in this list, where
     *   index 0 denotes the first element.
     * Throws a ListException if the index is out of bounds, i.e. if
     *   index does not satisfy 0 <= index < length.
     *
     * Examples:
     *   [1, 2, 3, 4].get(0) = 1
     *   [1, 2, 3, 4].get(3) = 4
     *   [1, 2, 3, 4][2] = 3
     *
     * @param index             position in list
     * @return                  the element at the specified position in the list
     */
    operator fun get(index: Int): A {
        tailrec
        fun recGet(idx: Int, ps: List<A>): A {
            return if (idx < 0)
                throw ListException("get: negative index: $idx")
            else if (ps.isEmpty())
                throw ListException("get: empty list")
            else if (idx == 0)
                ps.head()
            else
                recGet(idx - 1, ps.tail())
        }   // recGet

        return recGet(index, this)
    }   // get

    /**
     * The group function takes this list and returns a list of lists such that the
     *   concatenation of the result is equal to this list.  Moreover, each
     *   sublist in the result contains only equal elements.
     *
     * Examples:
     *   [1, 2, 2, 1, 1, 1, 2, 2, 2, 1].group() = [[1], [2, 2], [1, 1, 1], [2, 2, 2], [1]]
     *   [].group() = []
     *
     * @return			        list of sub-lists as the groups
     */
    fun group(): List<List<A>> {
        tailrec
        fun recGroup(ps: List<A>, buf: ListBufferIF<A>, acc: ListBufferIF<List<A>>): List<List<A>> {
            return when(ps) {
                is Nil -> if (buf.length() == 0) acc.toList() else acc.append(buf.toList()).toList()
                is Cons -> {
                    if (buf.length() == 0)
                        recGroup(ps.tail(), buf.append(ps.head()), acc)
                    else if (buf.contains(ps.head()))
                        recGroup(ps.tail(), buf.append(ps.head()), acc)
                    else {
                        val bufList = buf.toList()
                        buf.clear()
                        recGroup(ps.tail(), buf.append(ps.head()), acc.append(bufList))
                    }
                }
            }
        }   // recGroup

        return recGroup(this, ListBuffer(), ListBuffer())
    }   // group

    /**
     * Sorts all this list members into groups determined by the supplied mapping
     *   function.  The function should return the key that each item should be grouped by.
     *   The returned List of Pairs (ala Map) will have an entry for each distinct key returned
     *   from the function.
     *
     * Examples:
     *   [1, 2, 3, 4, 5].groupBy{m -> (m % 2)} = [(1, [5, 3, 1]), (0, [4, 2])]
     *   [1, 2, 3, 4, 5].groupBy{m -> (m % 3)} = [(2, [5, 2]), (1, [4, 1]), (0, [3])]
     *   [].groupBy{m -> (m % 2)} = []
     *
     * @param mapping		    curried grouping condition
     * @return			        list of sub-lists as the groups
     */
    fun <K> groupBy(mapping: (A) -> K): List<Pair<K, List<A>>> {
        tailrec
        fun recGroupBy(mapping: (A) -> K, xs: List<A>, acc: List<Pair<K, List<A>>>): List<Pair<K, List<A>>> {
            fun List<Pair<K, List<A>>>.update(k: K, a: A): List<Pair<K, List<A>>> {
                tailrec
                fun recUpdate(k: K, a: A, xs: List<Pair<K, List<A>>>, acc: List<Pair<K, List<A>>>): List<Pair<K, List<A>>> {
                    return when(xs) {
                        is Nil -> Cons(Pair(k, Cons(a, Nil)), acc)
                        is Cons -> {
                            val hd: Pair<K, List<A>> = xs.head()
                            val tl: List<Pair<K, List<A>>> = xs.tail()
                            if (k == hd.first)
                                Cons(Pair(k, Cons(a, hd.second)), acc).append(tl)
                            else
                                recUpdate(k, a, tl, Cons(hd, acc))
                        }
                    }
                }   // recUpdate

                return recUpdate(k, a, this, Nil)
            }   // update

            return when(xs) {
                is Nil -> acc
                is Cons -> {
                    val tl: List<A> = xs.tail()
                    val hd: A = xs.head()
                    val k: K = mapping(hd)
                    recGroupBy(mapping, tl, acc.update(k, hd))
                }
            }
        }   // recGroupBy

        return recGroupBy(mapping, this, Nil)
    }   // groupBy

    /**
     * Extract the first element of this list, which must be non-empty.
     *   Throws a ListException on an empty list.
     *
     * Examples:
     *   [1, 2, 3, 4].head() = 1
     *   [5].head() = 5
     *
     * @return                  the element at the front of the list
     */
    fun head(): A = when(this) {
        is Nil -> throw ListException("head: empty list")
        is Cons -> this.hd
    }   // head

    /**
     * Find the index of the first occurrence of the given value, or -1 if absent.
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
        tailrec
        fun recIndexOf(predicate: (A) -> Boolean, ps: List<A>, acc: Int): Int {
            return when(ps) {
                is Nil -> -1
                is Cons -> if (predicate(ps.head())) acc else recIndexOf(predicate, ps.tail(), 1 + acc)
            }
        }   // recIndexOf

        return recIndexOf(predicate, this, 0)
    }   // indexOf

    /**
     * Return all the elements of this list except the last one. The list must be non-empty.
     *   Throws a ListException on an empty list.
     *
     * Examples:
     *   [1, 2, 3, 4].init() = [1, 2, 3]
     *   [5].init() = []
     *
     * @return                  new list of the initial elements
     */
    fun init(): List<A> {
        tailrec
        fun recInit(ps: List<A>, acc: ListBufferIF<A>): List<A> {
            return when(ps) {
                is Nil -> throw ListException("init: empty list")
                is Cons -> if (ps.tail().isEmpty()) acc.toList() else recInit(ps.tail(), acc.append(ps.head()))
            }
        }   // recInit

        return recInit(this, ListBuffer())
    }   // init

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
     *   [1, 2, 3].inits() = [[], [1], [1, 2], [1, 2, 3]]
     *   [].inits() = [[]]
     *   [1, 2, 3].inits().size() = 1 + [1, 2, 3].size()
     *   [1, 2, 3].inits().head() = []
     *   [1, 2, 3].inits().last() = [1, 2, 3]
     *
     * @return                  new list of initial segment sub-lists
     */
    fun inits(): List<List<A>> {
        tailrec
        fun recInits(ps: List<A>, buf: ListBufferIF<A>, acc: ListBufferIF<List<A>>): List<List<A>> {
            return when(ps) {
                is Nil -> Cons(Nil, acc.toList())
                is Cons -> {
                    val buff: ListBufferIF<A> = ListBuffer<A>().append(buf.toList().append(ps.head()))
                    recInits(ps.tail(), buff, acc.append(buff.toList()))
                }
            }
        }   // recInits

        return recInits(this, ListBuffer(), ListBuffer())
    }   // inits

    /**
     * Return true if all the elements differ, otherwise false.
     *
     * Expensive operation on large lists.
     *
     * Examples:
     *   [1, 2, 3, 4].isDistinct() = true
     *   [].isDistinct() = true
     *   [1, 2, 3, 4, 1].isDistinct() = false
     *
     * @return                  true if all the elements are distinct
     */
    fun isDistinct(): Boolean {
        tailrec
        fun recIsDistinct(ps: List<A>): Boolean {
            return when(ps) {
                is Nil -> true
                is Cons -> {
                    val hd: A = ps.head()
                    val tl: List<A> = ps.tail()
                    if(tl.contains(hd))
                        false
                    else
                        recIsDistinct(tl)
                }

            }
        }   // recIsDistinct

        return recIsDistinct(this)
    }   // isDistinct

    /**
     * Return an iterator over elements of type A.
     */
    override fun iterator(): Iterator<A> = ListIterator(this)

    /**
     * Extract the last element of this list, which must be non-empty.
     *   Throws a ListException on an empty list.
     *
     * Examples:
     *   [1, 2, 3, 4].last() = 4
     *   [5].last() = 5
     *
     * @return                  final element in the list
     */
    fun last(): A {
        tailrec
        fun recLast(ps: List<A>): A {
            return when(ps) {
                is Nil -> throw ListException("last: empty list")
                is Cons -> if (ps.tail().isEmpty()) ps.head() else recLast(ps.tail())
            }
        }   // recLast

        return recLast(this)
    }   // last

    /**
     * Find the last index of the first occurrence of the given value, or -1 if absent.
     *
     * Examples:
     *   [1, 2, 3, 4].lastIndexOf{n -> (n == 1)} = 0
     *   [1, 2, 3, 4].lastIndexOf{n -> (n == 3)} = 2
     *   [1, 2, 3, 4].lastIndexOf{n -> (n == 5)} = -1
     *   [].lastIndexOf{n -> (n == 2)} = -1
     *
     * @param predicate         the search predicate
     * @return                  the index position
     */
    fun lastIndexOf(predicate: (A) -> Boolean): Int {
        fun recLastIndexOf(list: List<A>, predicate: (A) -> Boolean, index: Int, acc: Int): Int {
            return when (list) {
                is Nil -> acc
                is Cons -> if (predicate(list.hd))
                    recLastIndexOf(list.tl, predicate, index + 1, index)
                else
                    recLastIndexOf(list.tl, predicate, index + 1, acc)
            }
        }   // recLastIndexOf

        return recLastIndexOf(this, predicate, 0, -1)
    }   // lastIndexOf

    /**
     * Obtain the length of this list.
     *
     * Examples:
     *   [1, 2, 3, 4].length() == 4
     *   [].length() = 0
     *
     * @return                  number of elements in the list
     */
    fun length(): Int {
        tailrec
        fun recLength(ps: List<A>, acc: Int): Int {
            return when(ps) {
                is Nil -> acc
                is Cons -> recLength(ps.tail(), 1 + acc)
            }
        }   // recLength

        return recLength(this, 0)
    }   // length

    /**
     * Compose all the elements of this list as a string using the separator, prefix, postfix, etc.
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
     * @return                  the list content
     */
    fun makeString(separator: String = ", ", prefix: String = "", postfix: String = "", limit: Int = -1, truncated: String = "..."): String {
        fun recMakeString(list: List<A>, builder: StringBuilder, separator: String, limit: Int, truncated: String, count: Int): String {
            return when (list) {
                is Nil -> builder.toString()
                is Cons -> {
                    val hd: A = list.head()
                    val tl: List<A> = list.tail()
                    if (count == 0) {
                        val text: String = if (builder.length == 0) truncated else "$separator$truncated"
                        builder.append(text).toString()
                    } else {
                        val text: String = if (builder.length == 0) "$hd" else "$separator$hd"
                        recMakeString(tl, builder.append(text), separator, limit, truncated, count - 1)
                    }
                }
            }
        }   // recMakeString

        val str: String = recMakeString(this, StringBuilder(), separator, limit, truncated, limit)
        return "$prefix$str$postfix"
    }   // makeString

    /**
     * Function map applies the function parameter to each item in this list, delivering
     *   a new list. The result list has the same size as this list.
     *
     * Examples:
     *   [1, 2, 3, 4].map{n -> n + 1} = [2, 3, 4, 5]
     *   [].map{n -> n + 1} = []
     *
     * @param f                 pure function:: A -> B
     * @return                  new list of transformed values
     */
    fun <B> map(f: (A) -> B): List<B> {
        tailrec
        fun recMap(list: List<A>, f: (A) -> B, acc: ListBufferIF<B>): List<B> {
            return when (list) {
                is Nil -> acc.toList()
                is Cons -> recMap(list.tl, f, acc.append(f(list.hd)))
            }
        }   // recMap

        return recMap(this, f, ListBuffer())
    }   // map

    /**
     * Return a list of pairs of the adjacent elements from this list.
     *
     * Examples:
     *   [1, 2, 3, 4].pairwise() = [(1, 2), (2, 3), (3, 4)]
     *   [3, 4].pairwise() = [(3, 4)]
     *   [4].pairwise() = []
     *   [].pairwise() = []
     *
     * @return                  list of adjacent pairs
     */
    fun pairwise(): List<Pair<A, A>> = when(this) {
        is Nil -> Nil
        is Cons -> this.zip(this.tail())
    }   // pairwise

    /**
     * The partition function takes a predicate and returns the pair
     *   of lists of elements which do and do not satisfy the predicate.
     *   The sum of the sizes of the two result lists will equal the size
     *   of the original list. Both sub-lists are ordered permutations of
     *   this list.
     *
     * Examples:
     *   [1, 2, 3, 4, 5].partition{n -> (n % 2 == 0)} = ([2, 4], [1, 3, 5])
     *   [2, 4, 6, 8].partition{n -> (n % 2 == 0)} = ([2, 4, 6, 8], [])
     *   [2, 4, 6, 8].partition{n -> (n % 2 != 0)} = ([], [2, 4, 6, 8])
     *   [].partition{n -> (n % 2 == 0)} = ([], [])
     *
     * @param predicate         criteria
     * @return                  pair of new lists
     */
    fun partition(predicate: (A) -> Boolean): Pair<List<A>, List<A>> {
        tailrec
        fun recPartition(ps: List<A>, predicate: (A) -> Boolean, acc1: ListBufferIF<A>, acc2: ListBufferIF<A>): Pair<List<A>, List<A>> {
            return when(ps) {
                is Nil -> Pair(acc1.toList(), acc2.toList())
                is Cons -> if (predicate(ps.head()))
                    recPartition(ps.tail(), predicate, acc1.append(ps.head()), acc2)
                else
                    recPartition(ps.tail(), predicate, acc1, acc2.append(ps.head()))
            }
        }   // recPartition

        return recPartition(this, predicate, ListBuffer(), ListBuffer())
    }   // partition

    /**
     * Remove the first occurrence of the matching element from this list. The result list
     *   will either have the same size as this list (if no such element is present) or
     *   will have the size of this list less one.
     *
     * Examples:
     *   [1, 2, 3, 4].remove{n -> (n == 4)} = [1, 2, 3]
     *   [1, 2, 3, 4].remove{n -> (n == 5)} = [1, 2, 3, 4]
     *   [4, 4, 4, 4].remove{n -> (n == 4)} = [4, 4, 4]
     *   [].remove{n -> (n == 4)} = []
     *
     * @param predicate         search predicate
     * @return                  new list with element deleted
     */
    fun remove(predicate: (A) -> Boolean): List<A> {
        tailrec
        fun recRemove(predicate: (A) -> Boolean, ps: List<A>, acc: ListBufferIF<A>): List<A> {
            return when(ps) {
                is Nil -> acc.toList()
                is Cons -> if (predicate(ps.head())) acc.prependTo(ps.tail()) else recRemove(predicate, ps.tail(), acc.append(ps.head()))
            }
        }   // recRemove

        return recRemove(predicate, this, ListBuffer())
    }   // remove

    /**
     * The removeAll function removes all the elements from this list that match
     *   a given criteria. The result list size will not exceed this list size.
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
     * @return          		new list with all matching elements removed
     */
    fun removeAll(predicate: (A) -> Boolean): List<A> = this.filter{x: A -> !predicate(x)}

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
     *   [1, 2, 1, 2, 3].removeDuplicates = [1, 2, 3]
     *   [1, 1, 3, 2, 1, 3, 2, 4].removeDuplicates = [1, 3, 2, 4]
     *   [].removeDuplicates = []
     *
     * @return                  new list with all duplicates removed
     */
    fun removeDuplicates(): List<A> {
        tailrec
        fun recRemoveDuplicates(ps: List<A>, acc: ListBufferIF<A>): List<A> {
            return when(ps) {
                is Nil -> acc.toList()
                is Cons -> {
                    if (acc.contains(ps.head()))
                        recRemoveDuplicates(ps.tail(), acc)
                    else
                        recRemoveDuplicates(ps.tail(), acc.append(ps.head()))
                }
            }
        }   // recRemoveDuplicates

        return recRemoveDuplicates(this, ListBuffer())
    }   // removeDuplicates

    /**
     * Reverses the content of this list into a new list. The size of the result list
     *   is the same as this list.
     *
     * Examples:
     *   [1, 2, 3, 4].reverse() = [4, 3, 2, 1]
     *   [1].reverse() = [1]
     *   [].reverse() = []
     *
     * @return                  new list of elements reversed
     */
    fun reverse(): List<A> {
        tailrec
        fun recReverse(ps: List<A>, acc: ListBufferIF<A>): List<A> {
            return when(ps) {
                is Nil -> acc.toList()
                is Cons -> recReverse(ps.tail(), acc.prepend(ps.head()))
            }
        }   // recReverse

        return recReverse(this, ListBuffer())
    }   // reverse

    /**
     * Rotate the elements of this list left by the number of places.
     *   If paces is negative then throw an exception.
     *
     * Examples:
     *   [1, 2, 3, 4].rotateLeft(3) == [4, 1, 2, 3]
     *   [1, 2, 3, 4].rotateLeft(0) == [1, 2, 3, 4]
     *
     * @param places            the number of places to left shift the list
     * @return                  the new list
     */
    fun rotateLeft(places: Int): List<A> {
        tailrec
        fun recRotateLeft(list: List<A>, places: Int): List<A> {
            return if (places == 0)
                list
            else {
                val hd: A = list.head()
                val tl: List<A> = list.tail()
                recRotateLeft(tl.append(hd), places - 1)
            }
        }   // recRotateLeft

        return if (places < 0)
            throw ListException("List.rotateLeft: places: $places")
        else
            recRotateLeft(this, places)
    }   // rotateLeft

    /**
     * Rotate the elements of this list right by the number of places.
     *   If paces is negative then throw an exception.
     *
     * Examples:
     *   [1, 2, 3, 4].rotateRight(3) == [2, 3, 4, 1]
     *   [1, 2, 3, 4].rotateRight(0) == [1, 2, 3, 4]
     *
     * @param places            the number of places to right shift the list
     * @return                  the new list
     */
    fun rotateRight(places: Int): List<A> {
        fun recRotateRight(list: List<A>, places: Int): List<A> {
            return if (places == 0)
                list
            else {
                val hds: List<A> = list.init()
                val last: A = list.last()
                recRotateRight(Cons(last, hds), places - 1)
            }
        }   // recRotateRight

        return if (places < 0)
            throw ListException("List.rotateRight: places: $places")
        else
            recRotateRight(this, places)
    }   // rotateRight

    /**
     * scanLeft is similar to foldLeft, but returns a list of successively
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
     * @return                  new list
     */
    fun <B> scanLeft(e: B, f: (B) -> (A) -> B): List<B> {
        tailrec
        fun recScanLeft(b: B, g: (B) -> (A) -> B, ps: List<A>, acc: ListBufferIF<B>): List<B> {
            return when(ps) {
                is Nil -> acc.toList()
                is Cons -> {
                    val bb: B = g(b)(ps.head())
                    recScanLeft(bb, g, ps.tail(), acc.append(bb))
                }
            }
        }   // recScanLeft

        return Cons(e, recScanLeft(e, f, this, ListBuffer<B>()))
    }   // scanLeft

    /**
     * scanLeft is similar to foldLeft, but returns a list of successively
     *   reduced values from the left.
     *
     * Examples:
     *   [4, 2, 4].scanLeft(64){m, n -> m / y} = [64, 16, 8, 2]
     *   [].scanLeft(3){m, n -> m / y} = [3]
     *   [1, 2, 3, 4].scanLeft(5){m, n -> if (m > n) m else n} = [5, 5, 5, 5, 5]
     *   [1, 2, 3, 4, 5, 6, 7].scanLeft(5){m, n -> if (m > n) m else n} = [5, 5, 5, 5, 5, 5, 6, 7]
     *
     * @param f                 uncurried binary function
     * @param e                 initial value
     * @return                  new list
     */
    fun <B> scanLeft(e: B, f: (B, A) -> B): List<B> = this.scanLeft(e, C2(f))

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
     * @return                  new list
     */
    fun <B> scanRight(e: B, f: (A) -> (B) -> B): List<B> {
        fun scanR(b: B, f: (A) -> (B) -> B, psReverse: List<A>): List<B> {
            var scanned: List<B> = Cons(b, Nil)
            var eb: B = b
            for(a: A in psReverse){
                eb = f(a)(eb)
                scanned = Cons(eb, scanned)
            }
            return scanned
        }   // scanR

        return scanR(e, f, this.reverse())
    }   // scanRight

    /**
     * scanRight is the right-to-left dual of scanLeft.
     *
     * Examples:
     *   [1, 2, 3, 4].scanRight(5){m, n -> m + n} = [15, 14, 12, 9, 5]
     *   [8, 12, 24, 4].scanRight(2){m, n -> m / n} = [8, 1, 12, 2, 2]
     *   [].scanRight(3){m, n -> m / n} = [3]
     *   [3, 6, 12, 4, 55, 11].scanRight(18){m, n -> if (m > n) m else n} = [55, 55, 55, 55, 55, 18, 18]
     *
     * @param e                 initial value
     * @param f                 uncurried binary function
     * @return                  new list
     */
    fun <B> scanRight(e: B, f: (A, B) -> B): List<B> = this.scanRight(e, C2(f))

    /**
     * Obtain the size of this list; equivalent to length.
     *
     * Examples:
     *   [1, 2, 3, 4].size() == 4
     *   [].size() == 0
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
     *   is an infix of this list (or equivalently, a permutation of the list).
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
     * @return                  the sub-list of this list
     */
    fun slice(from: Int, to: Int): List<A>  = this.drop(from).take(to - from)

    /**
     * Sort the elements of this list into ascending order and deliver
     *   the resulting list. The elements are compared using the given
     *   comparator.
     *
     * Examples:
     *   [4, 3, 2, 1].sort{x, y -> if (x < y) -1 else if (x > y) +1 else 0} = [1, 2, 3, 4]
     *   [1, 2, 3, 4].sort{x, y -> if (x < y) -1 else if (x > y) +1 else 0} = [1, 2, 3, 4]
     *   [].sort{x, y -> if (x < y) -1 else if (x > y) +1 else 0} = []
     *   ["Ken", "John", "Jessie", "", ""].sort{str1, str2 -> str1.compareTo(str2)} = ["", "", "Jessie", "John", "Ken"]
     *
     * @param comparator        element comparison function
     * @return                  the sorted list
     */
    fun sort(comparator: (A, A) -> Int): List<A> {
        fun recSort(xs: List<A>, comp: (A, A) -> Int): List<A> {
            return if(xs.size() <= 1)
                xs
            else {
                val item: A = xs.head()
                val equalItems: List<A> = xs.filter{x -> (comp(x, item) == 0)}
                val smallerItems: List<A> = xs.filter{x -> (comp(x, item) < 0)}
                val largerItems: List<A> = xs.filter{x -> (comp(x, item) > 0)}
                recSort(smallerItems, comp).append(equalItems).append(recSort(largerItems, comp))
            }
        }   // recSort

        return recSort(this, comparator)
    }   // sort

    /**
     * span applied to a predicate and a list xs, returns a tuple where
     *   the first element is longest prefix (possibly empty) of xs of elements
     *   that satisfy predicate and second element is the remainder of the list.
     *   The sum of the sizes of the two result lists equals the size of this list.
     *   The first result list is a prefix of this list and the second result list
     *   is a suffix of this list.
     *
     * Examples:
     *   [1, 2, 3, 4, 1, 2, 3, 4].span{n -> (n < 3)} = ([1, 2], [3, 4, 1, 2, 3, 4])
     *   [1, 2, 3].span{n -> (n < 9)} = ([1, 2, 3], [])
     *   [1, 2, 3].span{n -> (n < 0)} = ([], [1, 2, 3])
     *
     * @param predicate         criteria
     * @return                  pair of two new lists
     */
    fun span(predicate: (A) -> Boolean): Pair<List<A>, List<A>> {
        tailrec
        fun recSpan(predicate: (A) -> Boolean, ps: List<A>, acc: ListBufferIF<A>): Pair<List<A>, List<A>> {
            return when(ps) {
                is Nil -> Pair(acc.toList(), ps)
                is Cons -> if (!predicate(ps.head())) Pair(acc.toList(), ps) else recSpan(predicate, ps.tail(), acc.append(ps.head()))
            }
        }   // recSpan

        return recSpan(predicate, this, ListBuffer<A>())
    }   // span

    /**
     * Delivers a tuple where first element is prefix of this list of length n and
     *   second element is the remainder of the list. The sum of the sizes of the
     *   two result lists equal the size of this list. The first result list is a
     *   prefix of this list. The second result list is a suffix of this list. The
     *   second result list appended on to the first result list is equal to this
     *   list.
     *
     * Examples:
     *   [1, 2, 3, 4].splitAt(2) = ([1, 2], [3, 4])
     *   [1, 2, 3, 4].splitAt(0) = ([], [1, 2, 3, 4])
     *   [1, 2, 3, 4].splitAt(5) = ([1, 2, 3, 4], [])
     *   [].splitAt(2) = ([], [])
     *
     * @param n                 number of elements into first result list
     * @return                  pair of two new lists
     */
    fun splitAt(n: Int): Pair<List<A>, List<A>> = Pair(this.take(n), this.drop(n))

    /**
     * Extract the elements after the head of this list, which must be non-empty.
     *   Throws a ListException on an empty list. The size of the result list
     *   will be one less than this list. The result list is a suffix of this
     *   list.
     *
     * Examples:
     *   [1, 2, 3, 4].tail() = [2, 3, 4]
     *   [5].tail() = []
     *
     * @return                  new list of the tail elements
     */
    fun tail(): List<A> = when(this) {
        is Nil -> throw ListException("tail: empty list")
        is Cons -> this.tl
    }   // tail

    /**
     * The tails function returns all final segments of this list,
     *   longest first. The result list size will exceed this list
     *   size by one. The first element of the result list is guaranteed
     *   to be the same as the original list; the final element of the
     *   result list is guaranteed to be the empty sub-list. All sub-lists
     *   of the result list are a prefix to the original.
     *
     * Examples:
     *   [1, 2, 3].tails() = [[1, 2, 3], [2, 3], [3], []]
     *   [].tails() = [[]]
     *   [1, 2, 3].tails().size() = 1 + [1, 2, 3].size()
     *   [1, 2, 3].tails().head() = [1, 2, 3]
     *   [1, 2, 3].tails().last() = []
     *
     * @return                  new list of final segments sub-lists
     */
    fun tails(): List<List<A>> {
        tailrec
        fun recTails(ps: List<A>, acc: ListBufferIF<List<A>>): List<List<A>> {
            return when(ps) {
                is Nil -> acc.append(empty<A>()).toList()
                is Cons -> recTails(ps.tail(), acc.append(ps))
            }
        }   // recTails

        return recTails(this, ListBuffer())
    }   // tails

    /**
     * Return a new list containing the first n elements from this list. If n
     *    exceeds the size of this list, then a copy is returned. If n is
     *    negative or zero, then an empty list is delivered. The size of
     *    the result list will not exceed the size of this list. The
     *    result list is a prefix of this list.
     *
     *  Examples:
     *    [1, 2, 3, 4].take(2) = [1, 2]
     *    [1, 2, 3, 4].take(0) = []
     *    [1, 2, 3, 4].take(5) = [1, 2, 3, 4]
     *    [].take(2) = []
     *
     * @param n                 number of elements to extract
     * @return                  new list of first n elements
     */
    fun take(n: Int): List<A> {
        tailrec
        fun recTake(m: Int, ps: List<A>, acc: ListBufferIF<A>): List<A> {
            return if (m <= 0)
                acc.toList()
            else when(ps) {
                is Nil -> acc.toList()
                is Cons -> recTake(m - 1, ps.tail(), acc.append(ps.head()))
            }
        }   // recTake

        return recTake(n, this, ListBuffer())
    }   // take

    /**
     * Function takeUntil retrieves the leading elements from this list that match
     *   some predicate. The result list size will not exceed this list size.
     *   The result list is a prefix of this list.
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
    fun takeUntil(predicate: (A) -> Boolean): List<A> = this.takeWhile{a -> !predicate(a)}

    /**
     * Function takeWhile takes the leading elements from this list that matches
     *   some predicate. The result list size will not exceed this list size.
     *   The result list is a prefix of this list.
     *
     * Examples:
     *   [1, 2, 3, 4].takeWhile{n -> (n <= 2)} = [1, 2]
     *   [1, 2, 3, 4].takeWhile{n -> (n <= 5)} = [1, 2, 3, 4]
     *   [1, 2, 3, 4].takeWhile{n -> (n <= 0)} = []
     *   [].takeWhile{n -> (n <= 2)} = []
     *
     * @param predicate         criteria
     * @return                  new list of leading elements matching criteria
     */
    fun takeWhile(predicate: (A) -> Boolean): List<A> {
        tailrec
        fun recTakeWhile(pred: (A) -> Boolean, ps: List<A>, acc: ListBufferIF<A>): List<A> {
            return when(ps) {
                is Nil -> acc.toList()
                is Cons -> if (pred(ps.head())) recTakeWhile(pred, ps.tail(), acc.append(ps.head())) else acc.toList()
            }
        }   // recTakeWhile

        return recTakeWhile(predicate, this, ListBuffer<A>())
    }   // takeWhile

    /**
     * Return a new list containing the last n elements from this list. If n
     *    exceeds the size of this list, then a copy is returned. If n is
     *    negative or zero, then an empty list is delivered. The size of
     *    the result list will not exceed the size of this list. The
     *    result list is a suffix of this list.
     *
     *  Examples:
     *    [1, 2, 3, 4].takeRight(2) = [3, 4]
     *    [1, 2, 3, 4].takeRight(0) = []
     *    [1, 2, 3, 4].takeRight(5) = [1, 2, 3, 4]
     *    [].takeRight(2) = []
     *
     * @param n                 number of elements to extract
     * @return                  new list of first n elements
     */
    fun takeRight(n: Int): List<A> = this.drop(this.length() - n)

    /**
     * There exists at least one element of this list that meets some criteria. If
     *   the list is empty then false is returned.
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
        tailrec
        fun recThereExists(predicate: (A) -> Boolean, ps: List<A>): Boolean {
            return when(ps) {
                is Nil -> false
                is Cons -> if (predicate(ps.head())) true else recThereExists(predicate, ps.tail())
            }
        }   // recThereExists

        return recThereExists(predicate, this)
    }   // thereExists

    /**
     * There exists only one element of this list that meets some criteria. If the
     *   list is empty then false is returned.
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
     * Convert this list into an immutable stream.
     *
     * Examples:
     *   [1, 2, 3].toStream() = [1, 2, 3]
     *   [].toStream() = []
     *
     * @return                      immutable stream of the values drawn from this list
     */
    fun toStream(): Stream<A> = StreamF.from(this)

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
     *   [1, 2, 3].zip([4, 5, 6]) = [(1, 4), (2, 5), (3, 6)]
     *   [1, 2].zip([4, 5, 6]) = [(1, 4), (2, 5)]
     *   [1, 2, 3].zip([4, 5]) = [(1, 4), (2, 5)]
     *   [1, 2, 3].zip([]) = []
     *   [].zip([4, 5]) = []
     *
     * @param xs                existing list
     * @return                  new list of pairs
     */
    fun <B> zip(xs: List<B>): List<Pair<A, B>> {
        tailrec
        fun recZip(list: List<A>, xs: List<B>, acc: ListBufferIF<Pair<A, B>>): List<Pair<A, B>> {
            return when (list) {
                is Nil -> acc.toList()
                is Cons -> {
                    when (xs) {
                        is Nil -> acc.toList()
                        is Cons -> recZip(list.tl, xs.tl, acc.append(Pair(list.hd, xs.hd)))
                    }
                }
            }
        }   // recZip

        return recZip(this, xs, ListBuffer())
    }   // zip

    /**
     * zipWith generalises zip by zipping with the function given as the second argument,
     *   instead of a tupling function. For example, zipWith (+) is applied to two lists
     *   to produce the list of corresponding sums. The size of the resulting list will
     *   equal the size of the smaller two lists.
     *
     * Examples:
     *   [1, 2, 3].zipWith([4, 5, 6]){m -> {n -> m + n}} = [5, 7, 9]
     *   [].zipWith([4, 5, 6]){m -> {n -> m + n}} = []
     *   [1, 2, 3].zipWith([]){m -> {n -> m + n}} = []
     *
     * @param xs                existing list
     * @param f                 curried binary function
     * @return                  new list of function results
     */
    fun <B, C> zipWith(xs: List<B>, f: (A) -> (B) -> C): List<C> {
        tailrec
        fun recZipWith(ps:List<A>, qs: List<B>, acc: ListBufferIF<C>, g: (A) -> (B) -> C): List<C> {
            return when(ps) {
                is Nil -> acc.toList()
                is Cons -> {
                    when(qs) {
                        is Nil -> acc.toList()
                        is Cons -> recZipWith(ps.tail(), qs.tail(), acc.append(g(ps.head())(qs.head())), g)
                    }
                }
            }
        }   // recZipWith

        return recZipWith(this, xs, ListBuffer<C>(), f)
    }   // zipWith

    /**
     * zipWith generalises zip by zipping with the function given as the first argument,
     *   instead of a tupling function. For example, zipWith (+) is applied to two lists
     *   to produce the list of corresponding sums. The size of the resulting list will
     *   equal the size of the smaller two lists.
     *
     * Examples:
     *   [1, 2, 3].zipWith([4, 5, 6]){m, n -> m + n} = [5, 7, 9]
     *   [].zipWith([4, 5, 6]){m, n -> m + n} = []
     *   [1, 2, 3].zipWith([]){m, n -> m + n} = []
     *
     * @param xs                existing list
     * @param f                 uncurried binary function
     * @return                  new list of function results
     */
    fun <B, C> zipWith(xs: List<B>, f: (A, B) -> C): List<C> = this.zipWith(xs, C2(f))

    /**
     * Zips this list with the index of its element as a pair. The result list
     *   has the same size as this list.
     *
     * Examples:
     *   [1, 2, 3, 4].zipWithIndex() = [(1, 0), (2, 1), (3, 2), (4, 3)]
     *   [].zipWithIndex() = []
     *
     * @return                  a new list with the same length as this list
     */
    fun zipWithIndex(): List<Pair<A, Int>> = if (this.size() == 0) Nil else this.zip(ListF.range(0, this.size()))

}   // List
