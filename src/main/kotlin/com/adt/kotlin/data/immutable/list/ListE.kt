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

import com.adt.kotlin.data.immutable.either.Either
import com.adt.kotlin.data.immutable.either.EitherF
import com.adt.kotlin.data.immutable.either.EitherF.right

import com.adt.kotlin.data.immutable.list.List.Nil
import com.adt.kotlin.data.immutable.list.List.Cons

import com.adt.kotlin.data.immutable.nel.NonEmptyList
import com.adt.kotlin.data.immutable.nel.NonEmptyListF
import com.adt.kotlin.data.immutable.nel.bind

import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.OptionF.map2
import com.adt.kotlin.data.immutable.option.OptionF.some

import com.adt.kotlin.data.immutable.stream.Stream
import com.adt.kotlin.data.immutable.stream.StreamF
import com.adt.kotlin.data.immutable.stream.bind

import com.adt.kotlin.data.immutable.validation.Validation
import com.adt.kotlin.data.immutable.validation.ValidationF
import com.adt.kotlin.data.immutable.validation.ValidationF.success

import com.adt.kotlin.hkfp.fp.FunctionF
import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3
import com.adt.kotlin.hkfp.fp.FunctionF.compose
import com.adt.kotlin.hkfp.fp.bind

import com.adt.kotlin.hkfp.typeclass.Monoid

import java.util.*
import java.util.stream.Stream as JStream
import java.util.stream.StreamSupport



/**
 * Functions to support an applicative style of programming.
 *
 * Examples:
 *   {a: A -> ... B value} fmap listA ==> List<B>
 *   {a: A -> {b: B -> ... C value}} fmap listA ==> List<(B) -> C>
 *   {a: A -> {b: B -> ... C value}} fmap listA appliedOver listB ==> List<C>
 */
infix fun <A, B> ((A) -> B).fmap(list: List<A>): List<B> =
    list.fmap(this)

infix fun <A, B> List<(A) -> B>.appliedOver(list: List<A>): List<B> =
    list.ap(this)



// ---------- special lists -------------------------------

/**
 * Translate a list of characters into a string
 *
 * @return                      the resulting string
 */
fun List<Char>.charsToString(): String {
    val buffer: StringBuffer = this.foldLeft(StringBuffer()){res -> {ch -> res.append(ch)}}
    return buffer.toString()
}

/**
 * 'unlines' joins lines after appending a newline to each.
 *
 * @return                      joined lines
 */
fun List<String>.unlines(): String {
    val buffer: StringBuffer = this.foldLeft(StringBuffer()){res -> {str -> res.append(str).append('\n')}}
    return buffer.toString()
}

/**
 * 'unwords' joins words after appending a space to each.
 *
 * @return                      joined words
 */
fun List<String>.unwords(): String {
    val buf: StringBuffer = this.foldLeft(StringBuffer()){res -> {str -> res.append(str).append(' ')}}
    val buffer: StringBuffer = if (this.size() >= 1) buf.deleteCharAt(buf.length - 1) else buf
    return buffer.toString()
}

/**
 * 'and' returns the conjunction of a container of booleans.
 *
 * @return                      true, if all the elements are true
 */
fun List<Boolean>.and(): Boolean =
    this.forAll{bool -> (bool == true)}

/**
 * 'or' returns the disjunction of a container of booleans.
 *
 * @return                      true, if any of the elements is true
 */
fun List<Boolean>.or(): Boolean =
    this.thereExists{bool -> (bool == true)}

/**
 * The sum function computes the sum of the integers in a list.
 *
 * @return                      the sum of all the elements
 */
fun List<Int>.sum(): Int =
    this.foldLeft(0){n, m -> n + m}

/**
 * The sum function computes the sum of the doubles in a list.
 *
 * @return                      the sum of all the elements
 */
fun List<Double>.sum(): Double =
    this.foldLeft(0.0){x, y -> x + y}

/**
 * The product function computes the product of the integers in a list.
 *
 * @return                      the product of all the elements
 */
fun List<Int>.product(): Int =
    this.foldLeft(1){n, m -> n * m}

/**
 * The product function computes the product of the doubles in a list.
 *
 * @return                      the product of all the elements
 */
fun List<Double>.product(): Double =
    this.foldLeft(1.0){x, y -> x * y}

/**
 * Find the largest integer in a list of integers. Throws a
 *   ListException if the list is empty.
 *
 * @return                      the maximum integer in the list
 */
fun List<Int>.max(): Int {
    tailrec
    fun recMax(xs: List<Int>, acc: Int): Int {
        return if (xs.isEmpty())
            acc
        else
            recMax(xs.tail(), Math.max(acc, xs.head()))
    }   // recMax

    return if (this.isEmpty())
        throw ListException("max: empty list")
    else
        recMax(this.tail(), this.head())
}

/**
 * Find the smallest integer in a list of integers. Throws a
 *   ListException if the list is empty.
 *
 * @return                      the minumum integer in the list
 */
fun List<Int>.min(): Int {
    tailrec
    fun recMin(xs: List<Int>, acc: Int): Int {
        return if (xs.isEmpty())
            acc
        else
            recMin(xs.tail(), Math.min(acc, xs.head()))
    }   // recMin

    return if (this.isEmpty())
        throw ListException("max: empty list")
    else
        recMin(this.tail(), this.head())
}

/**
 * Find the largest double in a list of doubles. Throws a
 *   ListException if the list is empty.
 *
 * @return                      the maximum double in the list
 */
fun List<Double>.max(): Double {
    tailrec
    fun recMax(xs: List<Double>, acc: Double): Double {
        return if (xs.isEmpty())
            acc
        else
            recMax(xs.tail(), Math.max(acc, xs.head()))
    }   // recMax

    return if (this.isEmpty())
        throw ListException("max: empty list")
    else
        recMax(this.tail(), this.head())
}

/**
 * Find the smallest double in a list of doubles. Throws a
 *   ListException if the list is empty.
 *
 * @return                      the minumum double in the list
 */
fun List<Double>.min(): Double {
    tailrec
    fun recMin(xs: List<Double>, acc: Double): Double {
        return if (xs.isEmpty())
            acc
        else
            recMin(xs.tail(), Math.min(acc, xs.head()))
    }   // recMin

    return if (this.isEmpty())
        throw ListException("max: empty list")
    else
        recMin(this.tail(), this.head())
}



// Contravariant extension functions:

/**
 * Append a single element on to this list. The size of the result list
 *   will be one more than the size of this list. The last element in the
 *   result list will equal the appended element. This list will be a prefix
 *   of the result list.
 *
 * Examples:
 *   [1, 2, 3, 4].append(5) = [1, 2, 3, 4, 5]
 *   [1, 2, 3, 4].append(5).size() = 1 + [1, 2, 3, 4].size()
 *   [1, 2, 3, 4].append(5).last() = 5
 *   [1, 2, 3, 4].isPrefix([1, 2, 3, 4].append(5)) = true
 *
 * @param element           new element
 * @return                  new list with element at end
 */
fun <A> List<A>.append(element: A): List<A> {
    tailrec
    fun recAppend(element: A, list: List<A>, acc: ListBufferIF<A>): List<A> {
        return when(list) {
            is Nil -> acc.append(element).toList()
            is Cons -> recAppend(element, list.tail(), acc.append(list.head()))
        }
    }   // recAppend

    return recAppend(element, this, ListBuffer())
}   // append

operator fun <A> List<A>.plus(element: A): List<A> = this.append(element)

/**
 * Append the given list on to this list. The size of the result list
 *   will equal the sum of the sizes of this list and the parameter
 *   list. This list will be a prefix of the result list and the
 *   parameter list will be a suffix of the result list.
 *
 * Examples:
 *   [1, 2, 3].append([4, 5]) = [1, 2, 3, 4, 5]
 *   [1, 2, 3].append([]) = [1, 2, 3]
 *   [].append([3, 4]) = [3, 4]
 *   [1, 2, 3].append([4, 5]).size() = [1, 2, 3].size() + [4, 5].size()
 *   [1, 2, 3].isPrefixOf([1, 2, 3].append([4, 5])) = true
 *   [4, 5].isSuffixOf([1, 2, 3].append([4, 5])) = true
 *
 * @param list              existing list
 * @return                  new list of appended elements
 */
fun <A> List<A>.append(list: List<A>): List<A> {
    tailrec
    fun recAppend(ps: List<A>, qs: List<A>, acc: ListBufferIF<A>): List<A> {
        return when(ps) {
            is Nil -> acc.prependTo(qs)
            is Cons -> recAppend(ps.tl, qs, acc.append(ps.hd))
        }
    }   // recAppend

    return recAppend(this, list, ListBuffer())
}   // append

operator fun <A> List<A>.plus(list: List<A>): List<A> = this.append(list)

/**
 * Append the given list on to this list. The size of the result list
 *   equals the sum of the size of this list and the list parameter.
 *   This list is a prefix of the result list and the parameter list
 *   is a suffix of the result list.
 *
 * Examples:
 *   [1, 2].concatenate([3, 4]) = [1, 2, 3, 4]
 *   [1, 2, 3, 4].concatenate([]) = [1, 2, 3, 4]
 *   [].concatenate([1, 2, 3, 4]) = [1, 2, 3, 4]
 *   [].concatenate([]) = []
 *
 * @param list              existing list
 * @return                  new list of appended elements
 */
fun <A> List<A>.concatenate(list: List<A>): List<A> = this.append(list)

/**
 * Determine if this list contains the given element.
 *
 * Examples:
 *   [1, 2, 3, 4].contains(4) = true
 *   [1, 2, 3, 4].contains(5) = false
 *   [].contains(4) = false
 *
 * @param element           search element
 * @return                  true if search element is present, false otherwise
 */
fun <A> List<A>.contains(element: A): Boolean = this.contains{x: A -> (x == element)}

/**
 * Count the number of times the parameter appears in this list.
 *
 * Examples:
 *   [1, 2, 3, 4].count(2) = 1
 *   [1, 2, 3, 4].count(5) = 0
 *   [].count(2) = 0
 *   [1, 2, 1, 2, 2].count(2) == 3
 *
 * @param element           the search value
 * @return                  the number of occurrences
 */
fun <A> List<A>.count(element: A): Int = this.count{x: A -> (x == element)}

/**
 * A variant of foldLeft that has no starting value argument, and thus must
 *   be applied to non-empty lists. The initial value is used as the start
 *   value. Throws a ListException on an empty list.
 *
 * Examples:
 *   [1, 2, 3, 4].foldLeft1{m -> {n -> m + n}} = 10
 *
 * @param f                 curried binary function:: A -> A -> A
 * @return                  folded result
 */
fun <A> List<A>.foldLeft1(f: (A) -> (A) -> A): A = when(this) {
    is Nil -> throw ListException("foldLeft1: empty list")
    is Cons -> this.tail().foldLeft(this.head(), f)
}   // foldLeft1

/**
 * A variant of foldLeft that has no starting value argument, and thus must
 *   be applied to non-empty lists. The initial value is used as the start
 *   value. Throws a ListException on an empty list.
 *
 * Examples:
 *   [1, 2, 3, 4].foldLeft1{m, n -> m + n} = 10
 *
 * @param f                 uncurried binary function:: A -> A -> A
 * @return                  folded result
 */
fun <A> List<A>.foldLeft1(f: (A, A) -> A): A = this.foldLeft1(C2(f))

/**
 * A variant of foldRight that has no starting value argument, and thus must
 *   be applied to non-empty lists. The initial value is used as the start
 *   value. Throws a ListException on an empty list.
 *
 * Examples:
 *   [1, 2, 3, 4].foldRight1{m -> {n -> m * n}} = 24
 *
 * @param f                 curried binary function:: A -> A -> A
 * @return                  folded result
 */
fun <A> List<A>.foldRight1(f: (A) -> (A) -> A): A = when(this) {
    is Nil -> throw ListException("foldRight1: empty list")
    is Cons -> this.tail().foldRight(this.head(), f)
}   // foldRight1

/**
 * A variant of foldRight that has no starting value argument, and thus must
 *   be applied to non-empty lists. The initial value is used as the start
 *   value. Throws a ListException on an empty list.
 *
 * Examples:
 *   [1, 2, 3, 4].foldRight1{m, n -> m * n} = 24
 *
 * @param f                 uncurried binary function:: A -> A -> A
 * @return                  folded result
 */
fun <A> List<A>.foldRight1(f: (A, A) -> A): A = this.foldRight1(C2(f))

/**
 * Find the index of the given value, or -1 if absent.
 *
 * Examples:
 *   [1, 2, 3, 4].indexOf(1) = 0
 *   [1, 2, 3, 4].indexOf(3) = 2
 *   [1, 2, 3, 4].indexOf(5) = -1
 *   [].indexOf(2) = -1
 *
 * @param element           the search value
 * @return                  the index position
 */
fun <A> List<A>.indexOf(element: A): Int = this.indexOf{x -> (x == element)}

/**
 * Insert a new element at the given index position. The new element
 *   can be inserted at the end of the list. Throws an exception
 *   if an illegal index is used.
 *
 * @param index             index position for the new element
 * @param element           the new element to insert
 * @return                  a new list
 */
fun <A> List<A>.insert(index: Int, element: A): List<A> {
    fun recInsert(list: List<A>, index: Int, element: A): List<A> {
        return when (list) {
            is Nil -> if (index == 0) Cons(element, Nil) else throw ListException("List.insert: index longer than list")
            is Cons -> if (index == 0) Cons(element, list) else Cons(list.hd, recInsert(list.tl, index - 1, element))
        }
    }   // recInsert

    return if (index < 0)
        throw ListException("List.insert: invalid index: $index")
    else
        recInsert(this, index, element)
}   // insert

/**
 * Insert the elements at the given index position. The new elements
 *   can be inserted at the end of the list. Throws an exception
 *   if an illegal index is used.
 *
 * @param index             index position for the new elements
 * @param elements          the new elements to insert
 * @return                  a new list
 */
fun <A> List<A>.insert(index: Int, elements: List<A>): List<A> {
    fun recInsert(list: List<A>, index: Int, elements: List<A>): List<A> {
        return when (list) {
            is Nil -> if (index == 0) elements else throw ListException("List.insert: index longer than list")
            is Cons -> if (index == 0) elements.append(list) else Cons(list.hd, recInsert(list.tl, index - 1, elements))
        }
    }   // recInsert

    return if (index < 0)
        throw ListException("List.insert: invalid index: $index")
    else
        recInsert(this, index, elements)
}   // insert

/**
 * Interleave this list and the given list, alternating elements from each list.
 *   If either list is empty then an empty list is returned. The first element is
 *   drawn from this list. The size of the result list will equal twice the size
 *   of the smaller list. The elements of the result list are in the same order as
 *   the two original.
 *
 * Examples:
 *   [].interleave([]) = []
 *   [].interleave([3, 4, 5]) = []
 *   [1, 2].interleave([]) = []
 *   [1, 2].interleave([3, 4, 5]) = [1, 3, 2, 4]
 *
 * @param xs                other list
 * @return                  result list of alternating elements
 */
fun <A> List<A>.interleave(xs: List<A>): List<A> {
    tailrec
    fun recInterleave(ps: List<A>, qs: List<A>, acc: ListBufferIF<A>): List<A> {
        return when(ps) {
            is Nil -> acc.toList()
            is Cons -> {
                when(qs) {
                    is List.Nil -> acc.toList()
                    is List.Cons -> recInterleave(ps.tail(), qs.tail(), acc.append(ps.head()).append(qs.head()))
                }
            }
        }
    }   // recInterleave

    return recInterleave(this, xs, ListBuffer<A>())
}   // interleave

/**
 * The intersperse function takes an element and intersperses
 *   that element between the elements of this list. If this list
 *   is empty then an empty list is returned. If this list size is
 *   one then this list is returned.
 *
 * Examples:
 *   [1, 2, 3, 4].intersperse(0) = [1, 0, 2, 0, 3, 0, 4]
 *   [1].intersperse(0) = [1]
 *   [].intersperse(0) = []
 *
 * @param separator         separator
 * @return                  new list of existing elements and separators
 */
fun <A> List<A>.intersperse(separator: A): List<A> {
    tailrec
    fun recIntersperse(sep: A, ps: List<A>, acc: ListBufferIF<A>): List<A> {
        return when(ps) {
            is Nil -> acc.toList()
            is Cons -> recIntersperse(sep, ps.tail(), acc.append(sep).append(ps.head()))
        }
    }   // recIntersperse

    return when(this) {
        is Nil -> Nil
        is Cons -> if (this.size() == 1) Cons(this.head(), Nil) else Cons(this.head(), recIntersperse(separator, this.tail(), ListBuffer()))
    }
}   // intersperse

/**
 * The isInfixOf function returns true iff this list is a constituent of the argument.
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
 * @param xs                existing list
 * @return                  true if this list is constituent of second list
 */
fun <A> List<A>.isInfixOf(xs: List<A>): Boolean {
    val isPrefix: (List<A>) -> (List<A>) -> Boolean = {ps -> {qs -> ps.isPrefixOf(qs)}}
    return xs.tails().thereExists(isPrefix(this))
}   // isInfixOf

/**
 * Return true if this list has the same content as the given list, regardless
 *   of order.
 *
 * Examples:
 *   [1, 2, 3, 4].isPermutationOf([1, 2, 3, 4]) = true
 *   [].isPermutationOf([1, 2, 3, 4]) = true
 *   [].isPermutationOf([]) = true
 *   [1, 2, 3, 4].isPermutationOf([]) = false
 *   [1, 2, 3, 4].isPermutationOf([5, 4, 3, 2, 1]) = true
 *   [5, 4, 3, 2, 1].isPermutationOf([1, 2, 3, 4]) = false
 *
 * @param ys                comparison list
 * @return                  true if this list has the same content as the given list; otherwise false
 */
fun <A> List<A>.isPermutationOf(ys: List<A>): Boolean = this.forAll{x -> ys.contains(x)}

/**
 * The isPrefixOf function returns true iff this list is a prefix of the second.
 *
 * Examples:
 *   [1, 2].isPrefixOf([1, 2, 3, 4]) = true
 *   [1, 2, 3, 4].isPrefixOf([1, 2, 3, 4]) = true
 *   [1, 2].isPrefixOf([2, 3, 4]) = false
 *   [1, 2].isPrefixOf([]) = false
 *   [].isPrefixOf([1, 2]) = true
 *   [].isPrefixOf([]) = true
 *
 * @param xs                existing list
 * @return                  true if this list is prefix of given list
 */
fun <A> List<A>.isPrefixOf(xs: List<A>): Boolean {
    tailrec
    fun recIsPrefixOf(ps: List<A>, qs: List<A>): Boolean {
        return when(ps) {
            is Nil -> true
            is Cons -> {
                when(qs) {
                    is Nil -> false
                    is Cons -> if (ps.head() != qs.head()) false else recIsPrefixOf(ps.tail(), qs.tail())
                }
            }
        }
    }   // recIsPrefixOf

    return recIsPrefixOf(this, xs)
}   // isPrefixOf

/**
 * Return true if this list has the same content as the given list, respecting
 *   the order.
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
 * @param ys                comparison list
 * @return                  true if this list has the same content as the given list; otherwise false
 */
fun <A> List<A>.isOrderedPermutationOf(ys: List<A>): Boolean {
    tailrec
    fun recIsOrderedPermutationOf(xs: List<A>, ys: List<A>): Boolean {
        return when(xs) {
            is Nil -> true
            is Cons -> {
                val xHead: A = xs.head()
                val xTail: List<A> = xs.tail()
                val index: Int = ys.indexOf(xHead)
                if (index < 0)
                    false
                else
                    recIsOrderedPermutationOf(xTail, ys.drop(1 + index))
            }
        }
    }   // recIsOrderedPermutationOf

    return recIsOrderedPermutationOf(this, ys)
}   // isOrderedPermutationOf

/**
 * The isSuffixOf function takes returns true iff this list is a suffix of the second.
 *
 * Examples:
 *   [3, 4].isSuffixOf([1, 2, 3, 4]) = true
 *   [1, 2, 3, 4].isSuffixOf([1, 2, 3, 4]) = true
 *   [3, 4].isSuffixOf([1, 2, 3]) = false
 *   [].isSuffixOf([1, 2, 3, 4]) = true
 *   [1, 2, 3, 4].isSuffixOf([]) = false
 *
 * @param xs                existing list
 * @return                  true if this list is suffix of given list
 */
fun <A> List<A>.isSuffixOf(xs: List<A>): Boolean {
    return this.reverse().isPrefixOf(xs.reverse())
}   // isSuffixOf

/**
 * Find the last index of the given value, or -1 if absent.
 *
 * Examples:
 *   [1, 2, 3, 4].lastIndexOf(1) = 0
 *   [1, 2, 3, 4].lastIndexOf(3) = 2
 *   [1, 2, 3, 4].lastIndexOf(5) = -1
 *   [].lastIndexOf(2) = -1
 *
 * @param element           the search value
 * @return                  the index position
 */
fun <A> List<A>.lastIndexOf(element: A): Int = this.lastIndexOf{x -> (x == element)}

/**
 * Remove the first occurrence of the given element from this list. The result list
 *   will either have the same size as this list (if no such element is present) or
 *   will have the size of this list less one.
 *
 * Examples:
 *   [1, 2, 3, 4].remove(4) = [1, 2, 3]
 *   [1, 2, 3, 4].remove(5) = [1, 2, 3, 4]
 *   [4, 4, 4, 4].remove(4) = [4, 4, 4]
 *   [].remove(4) = []
 *
 * @param x                 element to be removed
 * @return                  new list with element deleted
 */
fun <A> List<A>.remove(x: A): List<A> = this.remove{a: A -> (x == a)}

/**
 * The removeAll function removes all the elements from this list that match
 *   a given value. The result list size will not exceed this list size.
 *
 * Examples:
 *   [1, 2, 3, 4].removeAll(2) = [1, 3, 4]
 *   [1, 2, 3, 4].removeAll(5) = [1, 2, 3, 4]
 *   [].removeAll(4) = []
 *   [1, 4, 2, 3, 4].removeAll(4) = [1, 2, 3]
 *   [4, 4, 4, 4, 4].removeAll(4) = []
 *
 * @param predicate		    criteria
 * @return          		new list with all matching elements removed
 */
fun <A> List<A>.removeAll(x: A): List<A> = this.removeAll{a: A -> (x == a)}

/**
 * scanLeft1 is a variant of scanLeft that has no starting value argument.
 *   The initial value in the list is used as the starting value. An empty list
 *   returns an empty list.
 *
 * Examples:
 *  [1, 2, 3, 4].scanLeft1{m -> {n -> m + n}} = [1, 3, 6, 10]
 *  [64, 4, 2, 8].scanLeft1{m -> {n -> m / n}} = [64, 16, 8, 1]
 *  [12].scanLeft1{m -> {n -> m / n}} = [12]
 *  [3, 6, 12, 4, 55, 11].scanLeft{m -> {n -> if (m > n) m else n}} = [3, 6, 12, 12, 55, 55]
 *
 * @param f                 curried binary function
 * @return                  new list
 */
fun <A> List<A>.scanLeft1(f: (A) -> (A) -> A): List<A> = when(this) {
    is Nil -> List.Nil
    is Cons -> this.tail().scanLeft(this.head(), f)
}   // scanLeft1

/**
 * scanLeft1 is a variant of scanLeft that has no starting value argument.
 *   The initial value in the list is used as the starting value. An empty list
 *   returns an empty list.
 *
 * Examples:
 *  [1, 2, 3, 4].scanLeft1{m, n -> m + n} = [1, 3, 6, 10]
 *  [64, 4, 2, 8].scanLeft1{m, n -> m / n} = [64, 16, 8, 1]
 *  [12].scanLeft1{m, n -> m / n} = [12]
 *  [3, 6, 12, 4, 55, 11].scanLeft{m, n -> if (m > n) m else n} = [3, 6, 12, 12, 55, 55]
 *
 * @param f                 binary function
 * @return                  new list
 */
fun <A> List<A>.scanLeft1(f: (A, A) -> A): List<A> = this.scanLeft1(C2(f))

/**
 * scanRight1 is a variant of scanRight that has no starting value argument.
 *   The initial value in the list is used as the starting value. An empty list
 *   returns an empty list.
 *
 * Examples:
 *   [1, 2, 3, 4].scanRight1{m -> {n -> m + n}} = [10, 9, 7, 4]
 *   [8, 12, 24, 2].scanRight1{m -> {n -> m / n}} = [8, 1, 12, 2]
 *   [12].scanRight1{m -> {n -> m / n}} = [12]
 *   [3, 6, 12, 4, 55, 11].scanRight1{m -> {n -> if (m > n) m else n}} = [55, 55, 55, 55, 55, 11]
 *
 * @param f                 curried binary function
 * @return                  new list
 */
fun <A> List<A>.scanRight1(f: (A) -> (A) -> A): List<A> = when(this) {
    is Nil -> List.Nil
    is Cons -> this.init().scanRight(this.last(), f)
}   // scanRight1

/**
 * scanRight1 is a variant of scanRight that has no starting value argument.
 *   The initial value in the list is used as the starting value. An empty list
 *   returns an empty list.
 *
 * Examples:
 *   [1, 2, 3, 4].scanRight1{m, n -> m + n} = [10, 9, 7, 4]
 *   [8, 12, 24, 2].scanRight1{m, n -> m / n} = [8, 1, 12, 2]
 *   [12].scanRight1{m, n -> m / n} = [12]
 *   [3, 6, 12, 4, 55, 11].scanRight1{m, n -> if (m > n) m else n} = [55, 55, 55, 55, 55, 11]
 *
 * @param f                 uncurried binary function
 * @return                  new list
 */
fun <A> List<A>.scanRight1(f: (A, A) -> A): List<A> = this.scanRight1(C2(f))

/**
 * The stripPrefix function drops this prefix from the given list. It returns
 *   None if the list did not start with this prefix, or Some the
 *   list after the prefix, if it does.
 *
 * Examples:
 *   [1, 2].stripPrefix([1, 2, 3, 4]) = Some([3, 4])
 *   [2, 3, 4].stripPrefix([1, 2]) = None
 *   [].stripPrefix([1, 2, 3, 4]) = Some([1, 2, 3, 4])
 *   [1, 2, 3, 4].stripPrefix([]) = None
 *
 * @param xs                existing list of possible prefix
 * @return                  new list of prefix
 */
fun <A> List<A>.stripPrefix(xs: List<A>): Option<List<A>> {
    tailrec
    fun recStripPrefix(ps: List<A>, qs: List<A>): Option<List<A>> {
        return when(ps) {
            is Nil -> Option.Some(qs)
            is Cons -> {
                when(qs) {
                    is Nil -> Option.None
                    is Cons -> if (ps.head() != qs.head()) Option.None else recStripPrefix(ps.tail(), qs.tail())
                }
            }
        }
    }   // recStripPrefix

    return recStripPrefix(this, xs)
}   // stripPrefix

/**
 * Return a sequence over the elements of this list.
 *
 * Examples:
 *   [1, 2, 3, 4].sequence().count() = 4
 */
fun <A> List<A>.sequence(): Sequence<A> {
    val iterator: Iterator<A> = this.iterator()
    val sequence: Sequence<A> = Sequence{ -> iterator}
    return sequence
}   // sequence


/**
 * Return a stream over the elements of this list.
 *
 * Examples:
 *   [1, 2, 3, 4].stream().count() = 4
 */
fun <A> List<A>.stream(): JStream<A> {
    val iterator: Iterator<A> = this.iterator()
    val stream: JStream<A> = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
    return stream
}   // stream




// Functor extension functions:

/**
 * Apply the function to the content(s) of the List context.
 *   Function map applies the function parameter to each item in this list, delivering
 *   a new list. The result list has the same size as this list.
 *
 * Examples:
 *   [1, 2, 3, 4].fmap{n -> n + 1} = [2, 3, 4, 5]
 *   [].fmap{n -> n + 1} = []
 */
fun <A, B> List<A>.fmap(f: (A) -> B): List<B> = this.map(f)

/**
 * An infix symbol for fmap.
 */
infix fun <A, B> ((A) -> B).dollar(v: List<A>): List<B> = v.fmap(this)

/**
 * Replace all locations in the input with the given value.
 *
 * Examples:
 *   [1, 2, 3, 4].replaceAll(5) = [5, 5, 5, 5]
 *   [].replaceAll(5) = []
 */
fun <A, B> List<A>.replaceAll(b: B): List<B> = this.fmap{_ -> b}

/**
 * Distribute the List<(A, B)> over the pair to get (List<A>, List<B>).
 *
 * Examples:
 *   [(1, 2), (3, 4), (5, 6)].distribute() = ([1, 3, 5], [2, 4, 6])
 *   [].distribute() = ([], [])
 */
fun <A, B> List<Pair<A, B>>.distribute(): Pair<List<A>, List<B>> {
    fun recDistribute(list: List<Pair<A, B>>, accFirst: ListBufferIF<A>, accSecond: ListBufferIF<B>): Pair<List<A>, List<B>> {
        return when(list) {
            is Nil -> Pair(accFirst.toList(), accSecond.toList())
            is Cons -> {
                val head: Pair<A, B> = list.head()
                recDistribute(list.tail(), accFirst.append(head.first), accSecond.append(head.second))
            }
        }
    }   // recDistribute

    return recDistribute(this, ListBuffer(), ListBuffer())
}   // distribute

/**
 * Inject a to the left of the b's in this list.
 */
fun <A, B> List<B>.injectLeft(a: A): List<Pair<A, B>> = this.fmap{b: B -> Pair(a, b)}

/**
 * Inject b to the right of the a's in this list.
 */
fun <A, B> List<A>.injectRight(b: B): List<Pair<A, B>> = this.fmap{a: A -> Pair(a, b)}

/**
 * Twin all the a's in this list with itself.
 */
fun <A> List<A>.pair(): List<Pair<A, A>> = this.fmap{a: A -> Pair(a, a)}

/**
 * Pair all the a's in this list with the result of the function application.
 */
fun <A, B> List<A>.product(f: (A) -> B): List<Pair<A, B>> = this.fmap{a: A -> Pair(a, f(a))}



// Applicative extension functions:

/**
 * Apply the function wrapped in a context to the content of the
 *   value also wrapped in a matching context.
 *
 * Examples:
 *   [1, 2, 3, 4].ap([{n -> (n % 2 == 0)}]) = [false, true, false, true]
 *   [].ap([{n -> (n % 2 == 0)}]) = []
 */
fun <A, B> List<A>.ap(f: List<(A) -> B>): List<B> {
    tailrec
    fun recAp(list: List<A>, fs: List<(A) -> B>, acc: ListBufferIF<B>): List<B> {
        return when (fs) {
            is Nil -> acc.toList()
            is Cons -> recAp(list, fs.tl, acc.append(list.map(fs.hd)))
        }
    }   // recApp

    return recAp(this, f, ListBuffer())
}   // ap

/**
 * An infix symbol for ap.
 */
infix fun <A, B> List<(A) -> B>.apply(v: List<A>): List<B> = v.ap(this)

/**
 * An infix symbol for ap.
 */
infix fun <A, B> ((A) -> B).apply(v: List<A>): List<B> = v.ap(ListF.singleton(this))

/**
 * Sequence actions, discarding the value of the first argument.
 *
 * Examples:
 *   [0, 1, 2, 3].sDF(["Ken", "John", "Jessie", "Irene"]) = ["Ken", "Ken", "Ken", "Ken", "John", "John", "John", "John", "Jessie", "Jessie", "Jessie", "Jessie", "Irene", "Irene", "Irene", "Irene"]
 *   [5].sDF(["Ken", "John", "Jessie", "Irene"]) = ["Ken", "John", "Jessie", "Irene"]
 *   [].sDF(["Ken", "John", "Jessie", "Irene"]) = []
 */
fun <A, B> List<A>.sDF(lb: List<B>): List<B> {
    fun constant(b: B): (A) -> B = {_: A -> b}
    return ListF.liftA2(::constant)(lb)(this)
}   // sDF

/**
 * Sequence actions, discarding the value of the second argument.
 *
 * Examples:
 *   [0, 1, 2, 3].sDS(["Ken", "John", "Jessie", "Irene"]) = [0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3]
 *   [0, 1, 2, 3].sDS(["Ken"]) = [0, 1, 2, 3]
 *   [].sDS(["Ken"]) = []
 */
fun <A, B> List<A>.sDS(lb: List<B>): List<A> {
    val const: (A) -> (B) -> A = FunctionF::constant
    return ListF.liftA2(const)(this)(lb)
}   // sDS

/**
 * The product of two applicatives.
 *
 * ["Ken", "John", "Jessie", "Irene"].product2([1, 2, 3]) = [("Ken", 1), ("Ken", 2), ("Ken", 3), ("John", 1), ...]
 * ["Ken", "John", "Jessie", "Irene"].product2([]) = []
 * [].product2([1, 2, 3]) = []
 */
fun <A, B> List<A>.product2(lb: List<B>): List<Pair<A, B>> {
    fun <X, Y> recProduct2(listX: List<X>, listY: List<Y>, acc: ListBufferIF<Pair<X, Y>>): List<Pair<X, Y>> {
        fun recInnerProduct2(x: X, listY: List<Y>, acc: ListBufferIF<Pair<X, Y>>): ListBufferIF<Pair<X, Y>> {
            return when(listY) {
                is Nil -> acc
                is Cons -> recInnerProduct2(x, listY.tail(), acc.append(Pair(x, listY.head())))
            }
        }   // recInnerProduct2

        return when(listX) {
            is Nil -> acc.toList()
            is Cons -> recProduct2(listX.tail(), listY, recInnerProduct2(listX.head(), listY, acc))
        }
    }   // recProduct2

    return recProduct2(this, lb, ListBuffer())
}   // product2

/**
 * The product of three applicatives.
 *
 * Examples:
 *   ["Ken", "John"].product3([1, 2], [false, true]) = [("Ken", 1, false), ("Ken", 1, true), ("John", 1, false), ...]
 *   ["Ken", "John"].product3([], [false, true]) = []
 *   ["Ken", "John"].product3([1, 2], []) = []
 *   [].product3([1, 2], [false, true]) = []
 */
fun <A, B, C> List<A>.product3(lb: List<B>, lc: List<C>): List<Triple<A, B, C>> {
    fun <X, Y> recProduct2(listX: List<X>, listY: List<Y>, acc: ListBufferIF<Pair<X, Y>>): List<Pair<X, Y>> {
        fun recInnerProduct2(x: X, listY: List<Y>, acc: ListBufferIF<Pair<X, Y>>): ListBufferIF<Pair<X, Y>> {
            return when(listY) {
                is Nil -> acc
                is Cons -> recInnerProduct2(x, listY.tail(), acc.append(Pair(x, listY.head())))
            }
        }   // recInnerProduct2

        return when(listX) {
            is Nil -> acc.toList()
            is Cons -> recProduct2(listX.tail(), listY, recInnerProduct2(listX.head(), listY, acc))
        }
    }   // recProduct2

    fun <X, Y, Z> recProduct3(listX: List<X>, listYZ: List<Pair<Y, Z>>, acc: ListBufferIF<Triple<X, Y, Z>>): List<Triple<X, Y, Z>> {
        fun recInnerProduct3(x: X, listYZ: List<Pair<Y, Z>>, acc: ListBufferIF<Triple<X, Y, Z>>): ListBufferIF<Triple<X, Y, Z>> {
            return when (listYZ) {
                is Nil -> acc
                is Cons -> {
                    val (y: Y, z: Z) = listYZ.head()
                    recInnerProduct3(x, listYZ.tail(), acc.append(Triple(x, y, z)))
                }
            }
        }   // recInnerProduct3

        return when(listX) {
            is Nil -> acc.toList()
            is Cons -> recProduct3(listX.tail(), listYZ, recInnerProduct3(listX.head(), listYZ, acc))
        }
    }   // recProduct3

    return recProduct3(this, recProduct2(lb, lc, ListBuffer()), ListBuffer())
}   // product3

/**
 * fmap2 is a binary version of fmap.
 *
 * Examples:
 *   [1, 2, 3, 4].fmap2([5, 6, 7]){m -> {n -> m + n}} = [6, 7, 8, 7, 8, 9, 8, 9, 10, 9, 10, 11]
 *   [1, 2, 3, 4].fmap2([]){m -> {n -> m + n}} = []
 *   [].fmap2([5, 6, 7]){m -> {n -> m + n}} = []
 */
fun <A, B, C> List<A>.fmap2(lb: List<B>, f: (A) -> (B) -> C): List<C> =
    ListF.liftA2(f)(this)(lb)

fun <A, B, C> List<A>.fmap2(lb: List<B>, f: (A, B) -> C): List<C> =
    this.fmap2(lb, C2(f))

/**
 * fmap3 is a ternary version of fmap.
 *
 * Examples:
 *   [1, 2].fmap3([3, 4], [5, 6]){m -> {n -> {o -> m + n + o}} = [9, 10, 10, 11, 10, 11, 11, 12]
 *   [1, 2].fmap3([], [5, 6]){m -> {n -> {o -> m + n + o}} = []
 *   [1, 2].fmap3([3, 4], []){m -> {n -> {o -> m + n + o}} = []
 *   [].fmap3([3, 4], [5, 6]){m -> {n -> {o -> m + n + o}} = []
 */
fun <A, B, C, D> List<A>.fmap3(lb: List<B>, lc: List<C>, f: (A) -> (B) -> (C) -> D): List<D> =
    ListF.liftA3(f)(this)(lb)(lc)

fun <A, B, C, D> List<A>.fmap3(lb: List<B>, lc: List<C>, f: (A, B, C) -> D): List<D> =
    this.fmap3(lb, lc, C3(f))

/**
 * ap2 is a binary version of ap, defined in terms of ap.
 *
 * Examples:
 *   [1, 2].ap2([3, 4], [{m -> {n -> m + n}}, {m -> {n -> m * n}}]) = [4, 5, 5, 6, 3, 4, 6, 8]
 *   [1, 2].ap2([], [{m -> {n -> m + n}}, {m -> {n -> m * n}}]) = []
 *   [].ap2([3, 4], [{m -> {n -> m + n}}, {m -> {n -> m * n}}]) = []
 */
fun <A, B, C> List<A>.ap2(lb: List<B>, f: List<(A) -> (B) -> C>): List<C> =
    lb.ap(this.ap(f))

/**
 * ap3 is a ternary version of ap, defined in terms of ap.
 *
 * Examples:
 *   [1, 2].ap3([3, 4], [5, 6], [{m -> {n -> {o -> m + n + o}}}]) = [9, 10, 10, 11, 10, 11, 11, 12]
 *   [1, 2].ap3([], [5, 6], [{m -> {n -> {o -> m + n + o}}}]) = []
 *   [1, 2].ap3([3, 4], [], [{m -> {n -> {o -> m + n + o}}}]) = []
 *   [].ap3([3, 4], [5, 6], [{m -> {n -> {o -> m + n + o}}}]) = []
 */
fun <A, B, C, D> List<A>.ap3(lb: List<B>, lc: List<C>, f: List<(A) -> (B) -> (C) -> D>): List<D> =
    lc.ap(lb.ap(this.ap(f)))



// Monad extension functions:

/**
 * Sequentially compose two actions, passing any value produced by the first
 *   as an argument to the second.
 *
 * Examples:
 *   [0, 1, 2, 3].bind{n -> [n, n + 1]} = [0, 1, 1, 2, 2, 3, 3, 4]
 *   [].bind{n -> [n, n + 1]} = []
 */
fun <A, B> List<A>.bind(f: (A) -> List<B>): List<B> {
    tailrec
    fun recBind(vs: List<A>, f: (A) -> List<B>, acc: ListBufferIF<B>): List<B> {
        return when (vs) {
            is Nil -> acc.toList()
            is Cons -> recBind(vs.tl, f, acc.append(f(vs.hd)))
        }
    }   // recBind

    val g: (A) -> List<B> = {a: A -> f(a)}
    return recBind(this, g, ListBuffer())
}   // bind

fun <A, B> List<A>.flatMap(f: (A) -> List<B>): List<B> = this.bind(f)

/**
 * Sequentially compose two actions, discarding any value produced by the first,
 *   like sequencing operators (such as the semicolon) in imperative languages.
 *
 * Examples:
 *   [0, 1, 2, 3].then(["Ken", "John"]) = ["Ken", "John", "Ken", "John", "Ken", "John", "Ken", "John"]
 *   [].then(["Ken", "John"]) = []
 */
fun <A, B> List<A>.then(lb: List<B>): List<B> = this.bind{_ -> lb}



// Traversable extension functions:

/**
 * Apply the function to the list and pull the wrapper type Either through
 *   to the outside.
 *
 * Examples:
 *   [1, 2, 3, 4].traverseEither{n -> right(n % 2 == 0)} = right([false, true, false, true])
 *   [2, 4, 6, 8].traverseEither{n -> if (n % 2 == 0) right(n / 2) else left(n.toString())} = right([1, 2, 3, 4])
 *   [1, 2, 3, 4].traverseEither{n -> if (n % 2 == 0) right(n / 2) else left(n.toString())} = left("1")
 *   [2, 3, 4, 5].traverseEither{n -> if (n % 2 == 0) right(n / 2) else left(n.toString())} = left("3")
 */
fun <A, B, C> List<A>.traverseEither(f: (A) -> Either<B, C>): Either<B, List<C>> =
    this.foldRight(right<B, List<C>>(ListF.empty())){a: A ->
        {eibc: Either<B, List<C>> ->
            EitherF.map2(f(a), eibc) {c: C ->
                {cc: List<C> ->
                    Cons(c, cc)
                }
            }
        }
    }   // traverseEither

/**
 * Combine a List<Either<A, B>> into an Either<A, List<B>>. The result is a
 *   Right<A, List<B>> if all the elements of this list are Right instances
 *   otherwise, the result is a Left<A> if there is at least one Left in this list.
 * Examples:
 *   [right(1), right(2), right(3), right(4)].sequenceEither() = right([1, 2, 3, 4])
 *   [right(1), right(2), left("bad"), right(4)].sequenceEither() = left("bad")
 */
fun <A, B> List<Either<A, B>>.sequenceEither(): Either<A, List<B>> =
    this.traverseEither{op: Either<A, B> -> op}


/**
 * Apply the function to the list and pull the wrapper type Function through
 *   to the outside.
 *
 * Examples:
 *   [1, 2, 3, 4].traverseFunction{n -> {m -> n * m}}(5) = [5, 10, 15, 20]
 *   [2, 3, 4, 5].traverseFunction{n -> {m -> (n % m == 0)}}(2) = [true, false, true, false]
 */
fun <A, B, C> List<A>.traverseFunction(f: (A) -> (B) -> C): (B) -> List<C> {
    fun <X, Y> constant(y: Y): (X) -> Y = {_: X -> y}
    fun <X> cons(xs: List<X>): (X) -> List<X> = {x -> Cons(x, xs)}
    return this.foldRight(constant(ListF.empty())){a: A ->
        {gbc: (B) -> List<C> ->
            gbc.bind{bs -> compose(cons(bs), f(a))}
        }
    }
}   // traverseFunction

/**
 * Examples:
 *   [{n -> n + 1}, {n -> n - 1}, {n -> n * n}].sequenceFunction()(5) = [6, 4, 25]
 */
fun <A, B> List<(A) -> B>.sequenceFunction(): (A) -> List<B> =
    this.traverseFunction{f -> f}


/**
 * Apply the function to the list and pull the wrapper type Option through
 *   to the outside.
 *
 * Examples:
 *   [1, 2, 3, 4].traverseList{n -> ListF.of(n % 2 == 0)} = [[false, true, false, true]]
 */
fun <A, B> List<A>.traverseList(f: (A) -> List<B>): List<List<B>> =
    this.foldRight(ListF.singleton(ListF.empty<B>())){a: A ->
        {listListB: List<List<B>> ->
            f(a).bind{b: B -> listListB.map{bs -> Cons(b, bs)}}
        }
    }   // traverseList

fun <A> List<List<A>>.sequenceList(): List<List<A>> =
    this.traverseList{ls ->ls}


/**
 * Apply the function to the list and pull the wrapper type Option through
 *   to the outside.
 *
 * Examples:
 *   [1, 2].traverseNonEmptyList{n -> Nel[n % 2 == 0]} = Nel[[false, true]]
 *   [1, 2].traverseNonEmptyList{n -> Nel[n, n * n]} = Nel[[1, 2], [1, 4], [1, 2], [1, 4]]
 */
fun <A, B> List<A>.traverseNonEmptyList(f: (A) -> NonEmptyList<B>): NonEmptyList<List<B>> =
    this.foldRight(NonEmptyListF.singleton(ListF.empty<B>())){a: A ->
        {nelListB: NonEmptyList<List<B>> ->
            f(a).bind{b: B -> nelListB.map{bs -> Cons(b, bs)}}
        }
    }   // traverseNonEmptyList

/**
 * Examples:
 *   [Nel[1, 2], Nel[3, 4]].sequenceNonEmptyList() = Nel[[1, 3], [1, 4], [2, 3], [2, 4]]
 */
fun <A> List<NonEmptyList<A>>.sequenceNonEmptyList(): NonEmptyList<List<A>> =
    this.traverseNonEmptyList{nel -> nel}


/**
 * Apply the function to the list and pull the wrapper type Option through
 *   to the outside.
 *
 * Examples:
 *   [1, 2, 3, 4].traverseOption{n -> some(n % 2 == 0)} = some([false, true, false, true])
 *   [2, 4, 6, 8, 10].traverseOption{n -> if (n % 2 == 0) some(n / 2) else none()} = some([1, 2, 3, 4, 5])
 *   [1, 2, 3, 4, 5].traverseOption{n -> if (n % 2 == 0) some(n / 2) else none()} = none()
 */
fun <A, B> List<A>.traverseOption(f: (A) -> Option<B>): Option<List<B>> =
    this.foldRight(some(ListF.empty<B>())){a: A ->
        {op: Option<List<B>> ->
            map2(f(a), op) {b: B ->
                {bb: List<B> ->
                    Cons(b, bb)
                }
            }
        }
    }   // traverseOption

/**
 * Combine a List<Option<A>> into an Option<List<A>>. The result is a
 *   Some<List<A>> if all the elements of this list are Some instances
 *   otherwise, the result is a None<List<A>> if there is at least one
 *   None in this list.
 *
 * Examples:
 *   [some(3), some(4), some(5), some(6)].sequenceOption() = some([3, 4, 5, 6])
 *   [some(3), some(4), none(), some(6)].sequenceOption() = none()
 */
fun <A> List<Option<A>>.sequenceOption(): Option<List<A>> =
    this.traverseOption{op: Option<A> -> op}


/**
 * Apply the function to the list and pull the wrapper type Stream through
 *   to the outside.
 */
fun <A, B> List<A>.traverseStream(f: (A) -> Stream<B>): Stream<List<B>> =
    this.foldRight(StreamF.empty()){a: A ->
        {str: Stream<List<B>> ->
            f(a).bind{b: B ->
                str.map{list: List<B> -> Cons(b, list)}
            }
        }
    }   // traverseStream

/**
 * Combine a List<Stream<A>> into an Stream<List<A>>
 */
fun <A> List<Stream<A>>.sequenceStream(): Stream<List<A>> =
    this.traverseStream{str -> str}


/**
 * Examples:
 *   [1, 2, 3, 4].traverseValidation(stringMonoid){n -> success(n % 2 == 0)} = success([false, true, false, true])
 *
 *   fun half(n: Int): Validation<String, Int> = if (n % 2 == 0) success(n / 2) else failure(n.toString())
 *   [2, 4, 6, 8].traverseValidation(stringMonoid, ::half) = success([1, 2, 3, 4])
 *   [1, 2, 3, 4].traverseValidation(stringMonoid, ::half) = false("")
 */
fun <A, B, C> List<A>.traverseValidation(mb: Monoid<B>, f: (A) -> Validation<B, C>): Validation<B, List<C>> =
    this.foldRight(success(ListF.empty())){a: A ->
        {vbc: Validation<B, List<C>> ->
            ValidationF.map2(mb, f(a), vbc) {c: C ->
                {cc: List<C> ->
                    ListF.cons(c, cc)
                }
            }
        }
    }   // traverseValidation

/**
 * Examples:
 *   [success(1), success(2), success(3), success(4)].sequenceValidation(stringMonoid) = success([1, 2, 3, 4])
 *   [success(1), success(2), failure("bad"), success(4)].sequenceValidation(stringMonoid) = false("")
 */
fun <A, B> List<Validation<A, B>>.sequenceValidation(ma: Monoid<A>): Validation<A, List<B>> =
    this.traverseValidation(ma){op: Validation<A, B> -> op}




// Foldable extension functions:

/**
 * Combine the elements of a structure using a monoid.
 *
 * Examples:
 *   [1, 2, 3, 4].fold(intAddMonoid) = 10
 *   [].fold(intAddMonoid) = 0
 */
fun <A> List<A>.fold(md: Monoid<A>): A =
    this.foldLeft(md.empty){a: A -> {b: A -> md.combine(a, b)}}

/**
 * Map each element of the structure to a monoid, and combine the results.
 *
 * Examples:
 *   [1, 2, 3, 4].foldMap(intAddMonoid){n -> n + 1} = 14
 *   [].foldMap(intAddMonoid){n -> n + 1} = 0
 */
fun <A, B> List<A>.foldMap(md: Monoid<B>, f: (A) -> B): B =
    this.foldLeft(md.empty){b: B -> {a: A -> md.combine(b, f(a))}}
