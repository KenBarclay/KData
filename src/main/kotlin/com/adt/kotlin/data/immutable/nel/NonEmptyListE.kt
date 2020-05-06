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

import com.adt.kotlin.data.immutable.either.Either
import com.adt.kotlin.data.immutable.either.EitherF
import com.adt.kotlin.data.immutable.list.*
import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.List.Nil
import com.adt.kotlin.data.immutable.list.List.Cons

import com.adt.kotlin.data.immutable.nel.NonEmptyListF.liftA2
import com.adt.kotlin.data.immutable.nel.NonEmptyListF.liftA3

import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.OptionF
import com.adt.kotlin.hkfp.fp.FunctionF
import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3
import com.adt.kotlin.hkfp.fp.bind

import com.adt.kotlin.hkfp.typeclass.Monoid
import kotlin.math.max
import kotlin.math.min


/**
 * Functions to support an applicative style of programming.
 *
 * Examples:
 *   {a: A -> ... B value} fmap listA ==> List<B>
 *   {a: A -> {b: B -> ... C value}} fmap listA ==> List<(B) -> C>
 *   {a: A -> {b: B -> ... C value}} fmap listA appliedOver listB ==> List<C>
 */
infix fun <A, B> ((A) -> B).fmap(list: NonEmptyList<A>): NonEmptyList<B> =
    list.fmap(this)

infix fun <A, B> NonEmptyList<(A) -> B>.appliedOver(list: NonEmptyList<A>): NonEmptyList<B> =
    list.ap(this)



// ---------- special lists -------------------------------

/**
 * Translate a list of characters into a string
 *
 * @return                      the resulting string
 */
fun NonEmptyList<Char>.charsToString(): String {
    val buffer: StringBuffer = this.foldLeft(StringBuffer()){res -> {ch -> res.append(ch)}}
    return buffer.toString()
}   // charsToString

/**
 * 'and' returns the conjunction of a container of booleans.
 *
 * @return                      true, if all the elements are true
 */
fun NonEmptyList<Boolean>.and(): Boolean =
    this.forAll{bool -> (bool == true)}

/**
 * 'or' returns the disjunction of a container of booleans.
 *
 * @return                      true, if any of the elements is true
 */
fun NonEmptyList<Boolean>.or(): Boolean =
    this.thereExists{bool -> (bool == true)}

/**
 * The sum function computes the sum of the integers in a list.
 *
 * @return                      the sum of all the elements
 */
fun NonEmptyList<Int>.sum(): Int =
    this.foldLeft(0){n, m -> n + m}

/**
 * The sum function computes the sum of the doubles in a list.
 *
 * @return                      the sum of all the elements
 */
fun NonEmptyList<Double>.sum(): Double =
    this.foldLeft(0.0){x, y -> x + y}

/**
 * The product function computes the product of the integers in a list.
 *
 * @return                      the product of all the elements
 */
fun NonEmptyList<Int>.product(): Int =
    this.foldLeft(1){n, m -> n * m}

/**
 * The product function computes the product of the doubles in a list.
 *
 * @return                      the product of all the elements
 */
fun NonEmptyList<Double>.product(): Double =
    this.foldLeft(1.0){x, y -> x * y}

/**
 * Find the largest integer in a list of integers.
 *
 * @return                      the maximum integer in the list
 */
fun NonEmptyList<Int>.max(): Int =
    max(this.head(), this.tail().max())

/**
 * Find the smallest integer in a list of integers.
 *
 * @return                      the minumum integer in the list
 */
fun NonEmptyList<Int>.min(): Int =
    min(this.head(), this.tail().min())

/**
 * Find the largest double in a list of doubles.
 *
 * @return                      the maximum double in the list
 */
fun NonEmptyList<Double>.max(): Double =
    max(this.head(), this.tail().max())

/**
 * Find the smallest double in a list of doubles.
 *
 * @return                      the minumum double in the list
 */
fun NonEmptyList<Double>.min(): Double =
    min(this.head(), this.tail().min())



// Contravariant extension functions:

/**
 * Append a single element on to this list. The size of the result list
 *   will be one more than the size of this list. The last element in the
 *   result list will equal the appended element. This list will be a prefix
 *   of the result list.
 *
 * Examples:
 *   [1 :| 2, 3, 4].append(5) = [1, 2, 3, 4, 5]
 *   [1 :| 2, 3, 4].append(5).size() = 1 + [1, 2, 3, 4].size()
 *   [1 :| 2, 3, 4].append(5).last() = 5
 *
 * @param x                 new element
 * @return                  new list with element at end
 */
fun <A> NonEmptyList<A>.append(x: A): NonEmptyList<A> =
    NonEmptyList(this.toList().append(x))

/**
 * Append the given list on to this list. The size of the result list
 *   will equal the sum of the sizes of this list and the parameter
 *   list. This list will be a prefix of the result list and the
 *   parameter list will be a suffix of the result list.
 *
 * Examples:
 *   [1 :| 2, 3].append([4, 5]) = [1 :| 2, 3, 4, 5]
 *   [1 :| 2, 3].append([]) = [1 :| 2, 3]
 *   [1 :| 2, 3].append([4, 5]).size() = [1 :| 2, 3].size() + [4 :| 5].size()
 *   [1 :| 2, 3].isPrefixOf([1, 2, 3].append([4, 5])) = true
 *   [4 :| 5].isSuffixOf([1, 2, 3].append([4, 5])) = true
 *
 * @param xs                existing list
 * @return                  new list of appended elements
 */
fun <A> NonEmptyList<A>.append(list: List<A>): NonEmptyList<A> =
    NonEmptyList(hd, tl.append(list))

fun <A> NonEmptyList<A>.append(list: NonEmptyList<A>): NonEmptyList<A> =
    NonEmptyList(hd, tl.append(list.toList()))

/**
 * Append the given list on to this list. The size of the result list
 *   equals the sum of the size of this list and the list parameter.
 *   This list is a prefix of the result list and the parameter list
 *   is a suffix of the result list.
 *
 * Examples:
 *   [1 :| 2].concatenate([3, 4]) = [1 :| 2, 3, 4]
 *
 * @param xs                existing list
 * @return                  new list of appended elements
 */
fun <A> NonEmptyList<A>.concatenate(xs: NonEmptyList<A>): NonEmptyList<A> =
    this.append(xs)

fun <A> NonEmptyList<A>.concatenate(xs: List<A>): NonEmptyList<A> =
    this.append(xs)

/**
 * Determine if this list contains the given element.
 *
 * Examples:
 *   [1 :| 2, 3, 4].contains(4) = true
 *   [1 :| 2, 3, 4].contains(5) = false
 *
 * @param x                 search element
 * @return                  true if search element is present, false otherwise
 */
fun <A> NonEmptyList<A>.contains(x: A): Boolean =
    this.toList().contains(x)

/**
 * Count the number of times the parameter appears in this list.
 *
 * Examples:
 *   [1 :| 2, 3, 4].count(2) = 1
 *   [1 :| 2, 3, 4].count(5) = 0
 *   [1 :| 2, 1, 2, 2].count(2) == 3
 *
 * @param x                 the search value
 * @return                  the number of occurrences
 */
fun <A> NonEmptyList<A>.count(x: A): Int =
    this.toList().count(x)

/**
 * Find the index of the given value, or -1 if absent.
 *
 * Examples:
 *   [1 :| 2, 3, 4].indexOf(1) = 0
 *   [1 :| 2, 3, 4].indexOf(3) = 2
 *   [1 :| 2, 3, 4].indexOf(5) = -1
 *
 * @param x                 the search value
 * @return                  the index position
 */
fun <A> NonEmptyList<A>.indexOf(x: A): Int =
    this.toList().indexOf(x)

/**
 * Interleave this list and the given list, alternating elements from each list.
 *   If either list is empty then an empty list is returned. The first element is
 *   drawn from this list. The size of the result list will equal twice the size
 *   of the smaller list. The elements of the result list are in the same order as
 *   the two original.
 *
 * Examples:
 *   [1 :| 2].interleave([3 :| 4, 5]) = [1 :| 3, 2, 4]
 *
 * @param xs                other list
 * @return                  result list of alternating elements
 */
fun <A> NonEmptyList<A>.interleave(xs: NonEmptyList<A>): NonEmptyList<A> =
    NonEmptyList(this.toList().interleave(xs.toList()))

/**
 * The intersperse function takes an element and intersperses
 *   that element between the elements of this list. If this list
 *   is empty then an empty list is returned. If this list size is
 *   one then this list is returned.
 *
 * Examples:
 *   [1, 2, 3, 4].intersperse(0) = [1, 0, 2, 0, 3, 0, 4]
 *   [1].intersperse(0) = [1]
 *
 * @param separator         separator
 * @return                  new list of existing elements and separators
 */
fun <A> NonEmptyList<A>.intersperse(separator: A): NonEmptyList<A> =
    NonEmptyList(this.toList().intersperse(separator))

/**
 * The isInfixOf function returns true iff this list is a constituent of the argument.
 *
 * Examples:
 *   [2 :| 3].isInfixOf([1 :| 2, 3, 4]) = true
 *   [1 :| 2].isInfixOf([1 :| 2, 3, 4]) = true
 *   [3 :| 4].isInfixOf([1 :| 2, 3, 4]) = true
 *   [3 :| 2].isInfixOf([1 :| 2, 3, 4]) = false
 *   [1 :| 2, 3, 4, 5].isInfixOf([1 :| 2, 3, 4]) = false
 *
 * @param xs                existing list
 * @return                  true if this list is constituent of second list
 */
fun <A> NonEmptyList<A>.isInfixOf(xs: NonEmptyList<A>): Boolean =
    this.toList().isInfixOf(xs.toList())

/**
 * Return true if this list has the same content as the given list, respecting
 *   the order.
 *
 * Examples:
 *   [1 :| 2, 3, 4].isOrderedPermutationOf([1 :| 2, 3, 4]) = true
 *   [1 :| 4].isOrderedPermutationOf([1 :| 2, 3, 4]) = true
 *   [1 :| 2, 3].isOrderedPermutationOf([1 :| 1, 2, 1, 2, 4, 3, 4]) = true
 *   [1 :| 2, 3].isOrderedPermutationOf([1 :| 1, 3, 1, 4, 3, 3, 4]) = false
 *
 * @param ys                comparison list
 * @return                  true if this list has the same content as the given list; otherwise false
 */
fun <A> NonEmptyList<A>.isOrderedPermutationOf(ys: NonEmptyList<A>): Boolean =
    this.toList().isOrderedPermutationOf(ys.toList())

/**
 * Return true if this list has the same content as the given list, regardless
 *   of order.
 *
 * Examples:
 *   [1 :| 2, 3, 4].isPermutationOf([1 :| 2, 3, 4]) = true
 *   [1 :| 2, 3, 4].isPermutationOf([5 :| 4, 3, 2, 1]) = true
 *   [5 :| 4, 3, 2, 1].isPermutationOf([1 :| 2, 3, 4]) = false
 *
 * @param ys                comparison list
 * @return                  true if this list has the same content as the given list; otherwise false
 */
fun <A> NonEmptyList<A>.isPermutationOf(ys: NonEmptyList<A>): Boolean =
    this.toList().isPermutationOf(ys.toList())

/**
 * The isPrefixOf function returns true iff this list is a prefix of the second.
 *
 * Examples:
 *   [1 :| 2].isPrefixOf([1 :| 2, 3, 4]) = true
 *   [1 :| 2, 3, 4].isPrefixOf([1 :| 2, 3, 4]) = true
 *   [1 :| 2].isPrefixOf([2 :| 3, 4]) = false
 *
 * @param xs                existing list
 * @return                  true if this list is prefix of given list
 */
fun <A> NonEmptyList<A>.isPrefixOf(xs: NonEmptyList<A>): Boolean =
    this.toList().isPrefixOf(xs.toList())

/**
 * The isSuffixOf function takes returns true iff the this list is a suffix of the second.
 *
 * Examples:
 *   [3 :| 4].isSuffixOf([1 :| 2, 3, 4]) = true
 *   [1 :| 2, 3, 4].isSuffixOf([1 :| 2, 3, 4]) = true
 *   [3 :| 4].isSuffixOf([1 :| 2, 3]) = false
 *
 * @param xs                existing list
 * @return                  true if this list is suffix of given list
 */
fun <A> NonEmptyList<A>.isSuffixOf(xs: NonEmptyList<A>): Boolean =
    this.toList().isSuffixOf(xs.toList())

/**
 * Remove the first occurrence of the given element from this list. The result list
 *   will either have the same size as this list (if no such element is present) or
 *   will have the size of this list less one.
 *
 * Examples:
 *   [1 :| 2, 3, 4].remove(4) = [1, 2, 3]
 *   [1 :| 2, 3, 4].remove(5) = [1, 2, 3, 4]
 *   [4 :| 4, 4, 4].remove(4) = [4, 4, 4]
 *
 * @param x                 element to be removed
 * @return                  new list with element deleted
 */
fun <A> NonEmptyList<A>.remove(x: A): List<A> =
    this.toList().remove(x)

/**
 * The stripPrefix function drops this prefix from the given list. It returns
 *   None if the list did not start with this prefix, or Some the
 *   list after the prefix, if it does.
 *
 * Examples:
 *   [1 :| 2].stripPrefix([1 :| 2, 3, 4]) = Some([3 :| 4])
 *   [2 :| 3, 4].stripPrefix([1 :| 2]) = None
 *
 * @param xs                existing list of possible prefix
 * @return                  new list of prefix
 */
fun <A> NonEmptyList<A>.stripPrefix(xs: NonEmptyList<A>): Option<List<A>> =
    this.toList().stripPrefix(xs.toList())



// Functor extension functions:

/**
 * Apply the function to the content(s) of the List context.
 * Function map applies the function parameter to each item in this list, delivering
 *   a new list. The result list has the same size as this list.
 *
 * Examples:
 *   [1 :| 2, 3, 4].fmap{n -> n + 1} = [2 :| 3, 4, 5]
 */
fun <A, B> NonEmptyList<A>.fmap(f: (A) -> B): NonEmptyList<B> =
    this.map(f)

/**
 * An infix symbol for fmap.
 */
infix fun <A, B> ((A) -> B).dollar(nela: NonEmptyList<A>): NonEmptyList<B> =
    nela.fmap(this)

/**
 * Replace all locations in the input with the given value.
 *
 * Examples:
 *   [1 :| 2, 3, 4].replaceAll(5) = [5 :| 5, 5, 5]
 */
fun <A, B> NonEmptyList<A>.replaceAll(b: B): NonEmptyList<B> =
    NonEmptyList(b, this.tl.map{_ -> b})

/**
 * Distribute the List<(A, B)> over the pair to get (List<A>, List<B>).
 *
 * Examples:
 *   [(1, 2) :| (3, 4), (5, 6)].distribute() = ([1 :| 3, 5], [2 :| 4, 6])
 */
fun <A, B> NonEmptyList<Pair<A, B>>.distribute(): Pair<NonEmptyList<A>, NonEmptyList<B>> {
    fun recDistribute(list: List<Pair<A, B>>, accFirst: ListBufferIF<A>, accSecond: ListBufferIF<B>): Pair<NonEmptyList<A>, NonEmptyList<B>> {
        return when(list) {
            is Nil -> Pair(NonEmptyList(accFirst.toList()), NonEmptyList(accSecond.toList()))
            is Cons -> {
                val head: Pair<A, B> = list.head()
                recDistribute(list.tail(), accFirst.append(head.first), accSecond.append(head.second))
            }
        }
    }   // recDistribute

    return recDistribute(this.toList(), ListBuffer(), ListBuffer())
}   // distribute

/**
 * Inject a to the left of the b's in this list.
 */
fun <A, B> NonEmptyList<B>.injectLeft(a: A): NonEmptyList<Pair<A, B>> =
    this.fmap{b: B -> Pair(a, b)}

/**
 * Inject b to the right of the a's in this list
 */
fun <A, B> NonEmptyList<A>.injectRight(b: B): NonEmptyList<Pair<A, B>> =
    this.fmap{a: A -> Pair(a, b)}

/**
 * Twin all the a's in this list with itself.
 */
fun <A> NonEmptyList<A>.pair(): NonEmptyList<Pair<A, A>> =
    this.fmap{a: A -> Pair(a, a)}

/**
 * Pair all the a's in this list with the result of the function application.
 */
fun <A, B> NonEmptyList<A>.product(f: (A) -> B): NonEmptyList<Pair<A, B>> =
    this.fmap{a: A -> Pair(a, f(a))}



// Applicative extension functions:

/**
 * Apply the function wrapped in a context to the content of the
 *   value also wrapped in a matching context.
 *
 * Examples:
 *   [1 :| 2, 3, 4].ap([{n -> (n % 2 == 0)}]) = [false :| true, false, true]
 */
fun <A, B> NonEmptyList<A>.ap(f: NonEmptyList<(A) -> B>): NonEmptyList<B> {
    tailrec
    fun recAp(vs: List<A>, fs: List<(A) -> B>, acc: ListBufferIF<B>): List<B> {
        return when (fs) {
            is Nil -> acc.toList()
            is Cons -> recAp(vs, fs.tl, acc.append(vs.map(fs.hd)))
        }
    }   // recApp

    val vList: List<A> = this.toList()
    val fList: List<(A) -> B> = f.toList()
    return NonEmptyList(recAp(vList, fList, ListBuffer()))
}   // ap

/**
 * An infix symbol for ap.
 */
infix fun <A, B> NonEmptyList<(A) -> B>.apply(v: NonEmptyList<A>): NonEmptyList<B> =
    v.ap(this)

/**
 * Sequence actions, discarding the value of the first argument.
 *
 * Examples:
 *   [0 :| 1, 2, 3].sDF(["Ken" :| "John", "Jessie", "Irene"]) = ["Ken" :| "Ken", "Ken", "Ken", "John", "John", "John", "John", "Jessie", "Jessie", "Jessie", "Jessie", "Irene", "Irene", "Irene", "Irene"]
 *   [5].sDF(["Ken" :| "John", "Jessie", "Irene"]) = ["Ken" :| "John", "Jessie", "Irene"]
 */
fun <A, B> NonEmptyList<A>.sDF(nelb: NonEmptyList<B>): NonEmptyList<B> {
    fun constant(b: B): (A) -> B = {_: A -> b}
    return liftA2<B, A, B>(::constant)(nelb)(this)
}   // sDF

/**
 * Sequence actions, discarding the value of the second argument.
 *
 * Examples:
 *   [0 :| 1, 2, 3].sDS(["Ken" :| "John", "Jessie", "Irene"]) = [0 :| 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3]
 *   [0 :| 1, 2, 3].sDS(["Ken"]) = [0 :| 1, 2, 3]
 */
fun <A, B> NonEmptyList<A>.sDS(nelb: NonEmptyList<B>): NonEmptyList<A> {
    fun constant(a: A): (B) -> A = {_: B -> a}
    return liftA2<A, B, A>(::constant)(this)(nelb)
}   // sDS

/**
 * The product of two applicatives.
 *
 * Examples:
 *   [1 :| 2].product2(["John" :| "Ken", "Jessie"]) = [(1, "John") :| (1, "Ken"), (1, "Jessie), (2, "John"), (2, "Ken"), (2, "Jessie)]
 */
fun <A, B> NonEmptyList<A>.product2(nelb: NonEmptyList<B>): NonEmptyList<Pair<A, B>> =
    nelb.ap(this.fmap{a: A -> {b: B -> Pair(a, b)}})

/**
 * The product of three applicatives.
 *
 * Examples:
 *   [1 :| 2].product3(["John" :| "Ken"], [false :| true]) = [(1, "John", false) :| (1, "John", true), (1, "Ken", false), (1, "Ken", true), (2, "John", false), ...]
 */
fun <A, B, C> NonEmptyList<A>.product3(nelb: NonEmptyList<B>, nelc: NonEmptyList<C>): NonEmptyList<Triple<A, B, C>> {
    val nelab: NonEmptyList<Pair<A, B>> = this.product2(nelb)
    return nelab.product2(nelc).fmap{t2 -> Triple(t2.first.first, t2.first.second, t2.second)}
}   // product3

/**
 * fmap2 is a binary version of fmap.
 *
 * Examples:
 *   [1 :| 2, 3, 4].fmap2([5 :| 6, 7]){m -> {n -> m + n}} = [6 :| 7, 8, 7, 8, 9, 8, 9, 10, 9, 10, 11]
 */
fun <A, B, C> NonEmptyList<A>.fmap2(nelb: NonEmptyList<B>, f: (A) -> (B) -> C): NonEmptyList<C> =
    liftA2(f)(this)(nelb)

fun <A, B, C> NonEmptyList<A>.fmap2(nelb: NonEmptyList<B>, f: (A, B) -> C): NonEmptyList<C> =
    this.fmap2(nelb, C2(f))

/**
 * fmap3 is a ternary version of fmap.
 *
 * Examples:
 *   [1 :| 2].fmap3([3 :| 4], [5 :| 6]){m -> {n -> {o -> m + n + o}}} = [9 :| 10, 10, 11, 10, 11, 11, 12]
 */
fun <A, B, C, D> NonEmptyList<A>.fmap3(nelb: NonEmptyList<B>, nelc: NonEmptyList<C>, f: (A) -> (B) -> (C) -> D): NonEmptyList<D> =
    liftA3(f)(this)(nelb)(nelc)

fun <A, B, C, D> NonEmptyList<A>.fmap3(nelb: NonEmptyList<B>, nelc: NonEmptyList<C>, f: (A, B, C) -> D): NonEmptyList<D> =
    this.fmap3(nelb, nelc, C3(f))

/**
 * ap2 is a binary version of ap, defined in terms of ap.
 *
 * Examples:
 *   [1 :| 2].ap2([3 :| 4], [{m -> {n -> m + n}} :| {m -> {n -> m * n}}]) = [4 :| 5, 5, 6, 3, 4, 6, 8]
 */
fun <A, B, C> NonEmptyList<A>.ap2(nelb: NonEmptyList<B>, f: NonEmptyList<(A) -> (B) -> C>): NonEmptyList<C> =
    nelb.ap(this.ap(f))

/**
 * ap3 is a ternary version of ap, defined in terms of ap.
 *
 * Examples:
 *   [1 :| 2].ap3([3 :| 4], [5 :| 6], [{m -> {n -> {o -> m + n + o}}} :|]) = [9 :| 10, 10, 11, 10, 11, 11, 12]
 */
fun <A, B, C, D> NonEmptyList<A>.ap3(nelb: NonEmptyList<B>, nelc: NonEmptyList<C>, f: NonEmptyList<(A) -> (B) -> (C) -> D>): NonEmptyList<D> =
    nelc.ap(nelb.ap(this.ap(f)))



// Monad extension functions:

/**
 * Sequentially compose two actions, passing any value produced by the first
 *   as an argument to the second.
 *
 * Examples:
 *   [0 :| 1, 2, 3].bind{n -> [n, n + 1]} = [0 :| 1, 1, 2, 2, 3, 3, 4]
 */
fun <A, B> NonEmptyList<A>.bind(f: (A) -> NonEmptyList<B>): NonEmptyList<B> {
    tailrec
    fun recBind(vs: List<A>, f: (A) -> List<B>, acc: ListBufferIF<B>): List<B> {
        return when (vs) {
            is Nil -> acc.toList()
            is Cons -> recBind(vs.tl, f, acc.append(f(vs.hd)))
        }
    }   // recBind

    val g: (A) -> List<B> = {a: A -> f(a).toList()}
    return NonEmptyList(recBind(this.toList(), g, ListBuffer()))
}   // bind

fun <A, B> NonEmptyList<A>.flatMap(f: (A) -> NonEmptyList<B>): NonEmptyList<B> = this.bind(f)

/**
 * Sequentially compose two actions, discarding any value produced by the first,
 *   like sequencing operators (such as the semicolon) in imperative languages.
 *
 * Examples:
 *   [0 :| 1, 2, 3].then(["Ken" :| "John"]) = ["Ken" :| "John", "Ken", "John", "Ken", "John", "Ken", "John"]
 */
fun <A, B> NonEmptyList<A>.then(nelb: NonEmptyList<B>): NonEmptyList<B> = this.bind{_ -> nelb}



// Traversable extension functions:

/**********fun <A, B, C> NonEmptyList<A>.traverseEither(f: (A) -> Either<B, C>): Either<B, NonEmptyList<C>> {
    val ctor: (C) -> (List<C>) -> NonEmptyList<C> = {c: C -> {list: List<C> -> NonEmptyList(c, list)}}
    val liftCtor: (Either<B, C>) -> (Either<B, List<C>>) -> Either<B, NonEmptyList<C>> = EitherF.liftA2(ctor)
    return liftCtor(f(this.head()))(this.tail().traverseEither(f))
}   // traverseEither

fun <A, B> NonEmptyList<Either<A, B>>.sequenceEither(): Either<A, NonEmptyList<B>> =
    this.traverseEither{op: Either<A, B> -> op}


fun <A, B, C> NonEmptyList<A>.traverseFunction(f: (A) -> (B) -> C): (B) -> NonEmptyList<C> {
    fun <X, Y> constant(y: Y): (X) -> Y = {_: X -> y}
    fun <X> cons(xs: kotlin.collections.List<X>): (X) -> kotlin.collections.List<X> = { x -> ListF.cons(x, xs)}
    return this.foldRight(constant(ListF.empty())){a: A ->
        {gbc: (B) -> kotlin.collections.List<C> ->
            gbc.bind{bs -> FunctionF.compose(cons(bs), f(a)) }
        }
    }
}   // traverseFunction


fun <A, B> NonEmptyList<A>.traverseOption(f: (A) -> Option<B>): Option<NonEmptyList<B>> {
    val ctor: (B) -> (List<B>) -> NonEmptyList<B> = {b: B -> {list: List<B> -> NonEmptyList(b, list)}}
    val liftCtor: (Option<B>) -> (Option<List<B>>) -> Option<NonEmptyList<B>> = OptionF.liftA2(ctor)
    return liftCtor(f(this.head()))(this.tail().traverseOption(f))
}   // traverseOption

fun <A> NonEmptyList<Option<A>>.sequenceOption(): Option<NonEmptyList<A>> =
    this.traverseOption{op: Option<A> -> op}
**********/



// Foldable extension functions:

/**
 * Combine the elements of a structure using a monoid.
 *
 * Examples:
 *   [1 :| 2, 3, 4].fold(intAddMonoid) = 10
 */
fun <A> NonEmptyList<A>.fold(md: Monoid<A>): A =
    this.foldLeft(md.empty){b -> {a -> md.combine(b, a)}}

/**
 * Map each element of the structure to a monoid, and combine the results.
 *
 * Examples:
 *   [1 :| 2, 3, 4].foldMap(intAddMonoid){n -> n + 1} = 14
 */
fun <A, B> NonEmptyList<A>.foldMap(md: Monoid<B>, f: (A) -> B): B =
    this.foldLeft(md.empty){b -> {a -> md.combine(b, f(a))}}
