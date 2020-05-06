package com.adt.kotlin.data.immutable.set

/**
 * A class hierarchy defining an immutable set collection. The algebraic data
 *   type declaration is:
 *
 * datatype Set[A] = Tip
 *                 | Bin of A * Set[A] * Set[A]
 *
 * Sets are implemented as size balanced binary trees. This implementation
 *   mirrors the Haskell implementation in Data.Set that, in turn, is based
 *   on an efficient balanced binary tree referenced in the sources.
 *
 * The Set class is defined generically in terms of the type parameter A.
 *
 * @author	                    Ken Barclay
 * @since                       October 2012
 */

import com.adt.kotlin.data.immutable.list.*
import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.ListF

import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.OptionF.none
import com.adt.kotlin.data.immutable.option.OptionF.some

import com.adt.kotlin.data.immutable.set.Set.Tip
import com.adt.kotlin.data.immutable.set.Set.Bin



object SetF {

    /**
     * Factory binding/function to create the base instances.
     */
    @Suppress("UNCHECKED_CAST")
    fun <A : Comparable<A>> tip(): Set<A> = Tip as Set<A>
    fun <A : Comparable<A>> bin(a: A, left: Set<A>, right: Set<A>): Set<A> = Bin(1 + left.size + right.size, a, left, right)

    /**
     * Create an empty set.
     */
    @Suppress("UNCHECKED_CAST")
    fun <A : Comparable<A>> empty(): Set<A> = Tip as Set<A>

    /**
     * Create a set with a single element.
     */
    fun <A : Comparable<A>> singleton(a: A): Set<A> = bin(a, tip(), tip())

    /**
     * Create a set with one, two, etc values.
     */
    fun <A : Comparable<A>> of(a1: A): Set<A> = singleton(a1)

    fun <A : Comparable<A>> of(a1: A, a2: A): Set<A> = singleton(a1).insert(a2)

    fun <A : Comparable<A>> of(a1: A, a2: A, a3: A): Set<A> = singleton(a1).insert(a2).insert(a3)

    fun <A : Comparable<A>> of(a1: A, a2: A, a3: A, a4: A): Set<A> = singleton(a1).insert(a2).insert(a3).insert(a4)

    fun <A : Comparable<A>> of(a1: A, a2: A, a3: A, a4: A, a5: A): Set<A> = singleton(a1).insert(a2).insert(a3).insert(a4).insert(a5)

    fun <A : Comparable<A>> of(vararg a: A): Set<A> = from(*a)



    /**
     * Convert a variable-length parameter series into a set.
     *
     * Examples:
     *   fromSequence(Jessie, John, Ken) = {Jessie, John, Ken}
     *   fromSequence() = {}
     *
     * @param seq                   variable-length parameter series
     * @return                      set of the given values
     */
    fun <A : Comparable<A>> from(vararg seq: A): Set<A> =
            seq.fold(empty()){set, a -> set.insert(a)}

    /**
     * Convert a variable-length immutable list into a set.
     *
     * Examples:
     *   fromList([Jessie, John, Ken]) = {Jessie, John, Ken}
     *   fromList([]) = {}
     *
     * @param ls                    variable-length list
     * @return                      set of the given values
     */
    fun <A : Comparable<A>> from(ls: List<A>): Set<A> = ls.foldRight(empty()){a -> {set: Set<A> -> set.insert(a)}}

    /**
     * Convert an immutable set to an immutable list.
     *
     * @param set                   existing immutable set
     * @return                      a list
     */
    fun <A : Comparable<A>> toList(set: Set<A>): List<A> {
        fun recToList(set: Set<A>): List<A> {
            return when(set) {
                is Tip -> ListF.empty()
                is Bin -> recToList(set.left).append(set.value).append(recToList(set.right))
            }
        }

        return recToList(set)
    }   // toList


    /**
     * Show the tree that implements the set. The tree is presented in
     *   a hanging format:
     *
     * John
     * |
     * +--Irene
     * |  |
     * |  +--Andrew
     * |  |
     * |  +--Jessie
     * |
     * +--Ken
     */
    fun <A: Comparable<A>> drawSet(set: Set<A>): String =
        drawSet(ListF.empty(), set)

    internal fun <A: Comparable<A>> drawSet(bars: List<String>, set: Set<A>): String {
        println("drawSet: bars: $bars; set: $set")
        return if (set.isEmpty())
            "${drawBars(bars)}|\n"
        else {
            @Suppress("UNCHECKED_CAST") val bin: Bin<A> = set as Bin<A>
            if (bin.left.isEmpty() && bin.right.isEmpty())
                "${drawBars(bars)}${bin.value}\n"
            else {
                val text5: String = drawSet(withEmpty(bars), bin.right)
                val text4: String = drawWide(bars)
                val text3: String = drawSet(withBar(bars), bin.left)
                val text2: String = drawWide(bars)
                val text1: String = "${drawBars(bars)}${bin.value}\n"
                val text: String = "$text1$text2$text3$text4$text5"
                text
            }
        }
    }   // drawSet

    internal val node: String = "+--"

    internal fun drawBars(bars: List<String>): String =
        if (bars.isEmpty()) "" else "${bars.tail().reverse().foldLeft(""){acc, str -> "$acc$str"}}$node"
    internal fun drawWide(bars: List<String>): String =
        if (bars.isEmpty()) "|\n" else "${bars.reverse().foldLeft(""){acc, str -> "$acc$str"}}|\n"

    internal fun withBar(strings: List<String>): List<String> = ListF.cons("|  ", strings)
    internal fun withEmpty(strings: List<String>): List<String> = ListF.cons("   ", strings)
    /**********fun <A: Comparable<A>> drawSet(set: Set<A>): String =
        drawSet(ListF.empty(), set.map{el -> "$el"})

    internal fun drawSet(bars: List<String>, set: Set<String>): String {
        println("drawSet: bars: $bars; set: $set")
        return if (set.isEmpty())
            "${drawBars(bars)}|\n"
        else {
            @Suppress("UNCHECKED_CAST") val bin: Bin<String> = set as Bin<String>
            if (bin.left.isEmpty() && bin.right.isEmpty())
                "${drawBars(bars)}${bin.value}\n"
            else {
                val text5: String = drawSet(withEmpty(bars), bin.right)
                val text4: String = drawWide(bars)
                val text3: String = drawSet(withBar(bars), bin.left)
                val text2: String = drawWide(bars)
                val text1: String = "${drawBars(bars)}${bin.value}\n"
                val text: String = "$text1$text2$text3$text4$text5"
                text
            }
        }
    }   // drawSet

    internal val node: String = "+--"

    internal fun drawBars(bars: List<String>): String =
        if (bars.isEmpty()) "" else "${bars.tail().reverse().foldLeft(""){acc, str -> "$acc$str"}}$node"
    internal fun drawWide(bars: List<String>): String =
        if (bars.isEmpty()) "|\n" else "${bars.reverse().foldLeft(""){acc, str -> "$acc$str"}}|\n"

    internal fun withBar(strings: List<String>): List<String> = ListF.cons("|  ", strings)
    internal fun withEmpty(strings: List<String>): List<String> = ListF.cons("   ", strings)
    **********/




// ---------- implementation ------------------------------

    private val DELTA: Int = 4      // the maximal relative difference between the sizes of two sets
    private val RATIO: Int = 2      // is the ratio between an outer and inner sibling of the
                                    // heavier subtree in an unbalanced setting

    /**
     * Balance two sets with the value a. The sizes of the sets should balance
     *   after decreasing the size of one of them (a rotation).
     */
    internal fun <A : Comparable<A>> balance(a: A, left: Set<A>, right: Set<A>): Set<A> {
        return if(left.size() + right.size <= 1)
            bin(a, left, right)
        else if (right.size >= DELTA * left.size)
            rotateLeft(a, left, right)
        else if (left.size >= DELTA * right.size)
            rotateRight(a, left, right)
        else
            bin(a, left, right)
    }    // balance

    internal fun <A : Comparable<A>> rotateLeft(value: A, left: Set<A>, right: Set<A>): Set<A> {
        return when (right) {
            is Tip -> throw SetException("rotateLeft: right is Tip")
            is Bin -> {
                if (right.left.size < RATIO * right.right.size)
                    singleLeft(value, left, right)
                else
                    doubleLeft(value, left, right)
            }
        }
    }   // rotateLeft

    internal fun <A : Comparable<A>> rotateRight(value: A, left: Set<A>, right: Set<A>): Set<A> {
        return when (left) {
            is Tip -> throw SetException("rotateRight: left is Tip")
            is Bin -> {
                if (left.right.size < RATIO * left.left.size)
                    singleRight(value, left, right)
                else
                    doubleRight(value, left, right)
            }
        }
    }   // rotateRight

    /**
     * Compose a new set:
     *
     * singleLeft(x1, t1, Bin(_, x2, t2, t3)) = bin(x2, bin(x1, t1, t2), t3)
     */
    internal fun <A : Comparable<A>> singleLeft(value: A, left: Set<A>, right: Set<A>): Set<A> {
        return when(right) {
            is Tip -> throw SetException("singleLeft: right is Tip")
            is Bin -> bin(right.value, bin(value, left, right.left), right.right)
        }
    }   // singleLeft

    /**
     * Compose a new set:
     *
     * singleRight(x1, Bin(_, x2, t1, t2), t3) = bin(x2, t1, bin(x1, t2, t3))
     */
    internal fun <A : Comparable<A>> singleRight(value: A, left: Set<A>, right: Set<A>): Set<A> {
        return when(left) {
            is Tip -> throw SetException("singleRight: left is Tip")
            is Bin -> bin(left.value, left.left, bin(value, left.right, right))
        }
    }   // singleRight

    /**
     * Compose a new set:
     *
     * doubleLeft(x1, t1, Bin(_, x2, Bin(_, x3, t2, t3), t4)) = bin(x3, bin(x1, t1, t2), bin(x2, t3, t4))
     */
    internal fun <A : Comparable<A>> doubleLeft(value: A, left: Set<A>, right: Set<A>): Set<A> {
        return when(right) {
            is Tip -> throw SetException("doubleLeft: right is Tip")
            is Bin -> {
                val rightLeft: Set<A> = right.left
                when (rightLeft) {
                    is Tip -> throw SetException("doubleLeft: right.left is Tip")
                    is Bin -> bin(rightLeft.value, bin(value, left, rightLeft.left), bin(right.value, rightLeft.right, right.right))
                }
            }
        }
    }   // doubleLeft

    /**
     * Compose a new set:
     *
     * doubleRight(x1, Bin(_, x2, t1, Bin(_, x3, t2, t3)), t4) = bin(x3, bin(x2, t1, t2), bin(x1, t3, t4))
     */
    internal fun <A : Comparable<A>> doubleRight(value: A, left: Set<A>, right: Set<A>): Set<A> {
        return when (left) {
            is Tip -> throw SetException("doubleRight: left is Tip")
            is Bin -> {
                val leftRight: Set<A> = left.right
                when (leftRight) {
                    is Tip -> throw SetException("doubleRight: left.right is Tip")
                    is Bin -> bin(leftRight.value, bin(left.value, left.left, leftRight.left), bin(value, leftRight.right, right))
                }
            }
        }
    }   // doubleRight

    /**
     * Glue the two sets together. Assumes that left and right are already balanced with respect to each other.
     *
     * glue Tip r = r
     * glue l Tip = l
     * glue l r
     *   | size l > size r = let (m,l') = deleteFindMax l in balanceR m l' r
     *   | otherwise       = let (m,r') = deleteFindMin r in balanceL m l r'
     */
    internal fun <A : Comparable<A>> glue(left: Set<A>, right: Set<A>): Set<A> {
        return when(left) {
            is Tip -> right
            is Bin -> {
                when(right) {
                    is Tip -> left
                    is Bin -> {
                        if (left.size > right.size) {
                            val dfm: Pair<A, Set<A>> = deleteFindMax(left)
                            balance(dfm.first, dfm.second, right)
                        } else {
                            val dfm: Pair<A, Set<A>> = deleteFindMin(right)
                            balance(dfm.first, left, dfm.second)
                        }
                    }
                }
            }
        }
    }   // glue

    /**
     * Delete and find the minimal element.
     *
     * deleteFindMin t
     *   = case t of
     *       Bin _ x Tip r -> (x,r)
     *       Bin _ x l r   -> let (xm,l') = deleteFindMin l in (xm,balance x l' r)
     *       Tip           -> error
     */
    internal fun <A : Comparable<A>> deleteFindMin(set: Set<A>): Pair<A, Set<A>> {
        return when(set) {
            is Tip -> throw SetException("deleteFindMin: empty tree")
            is Bin -> {
                val setLeft: Set<A> = set.left
                when(setLeft) {
                    is Tip -> Pair(set.value, set.right)
                    is Bin -> {
                        val dfm: Pair<A, Set<A>> = deleteFindMin(set.left)
                        Pair(dfm.first, balance(set.value, dfm.second, set.right))
                    }
                }
            }
        }
    }   // deleteFindMin

    /**
     * Delete and find the maximal element.
     *
     * deleteFindMax t
     *   = case t of
     *       Bin _ x l Tip -> (x,l)
     *       Bin _ x l r   -> let (xm,r') = deleteFindMax r in (xm,balance x l r')
     *       Tip           -> error
     */
    internal fun <A : Comparable<A>> deleteFindMax(set: Set<A>): Pair<A, Set<A>> {
        return when(set) {
            is Tip -> throw SetException("deleteFindMax: empty tree")
            is Bin -> {
                val setRight: Set<A> = set.right
                when(setRight) {
                    is Tip -> Pair(set.value, set.left)
                    is Bin -> {
                        val dfm: Pair<A, Set<A>> = deleteFindMax(set.right)
                        Pair(dfm.first, balance(set.value, set.left, dfm.second))
                    }
                }
            }
        }
    }   // deleteFindMax

    /**
     * Restore balance and size.
     *
     * join x Tip r  = insertMin x r
     * join x l Tip  = insertMax x l
     * join x l@(Bin sizeL y ly ry) r@(Bin sizeR z lz rz)
     *   | delta*sizeL < sizeR  = balanceL z (join x l lz) rz
     *   | delta*sizeR < sizeL  = balanceR y ly (join x ry r)
     *   | otherwise            = bin x l r
     */
    internal fun <A : Comparable<A>> join(value: A, left: Set<A>, right: Set<A>): Set<A> {
        return when(left) {
            is Tip -> insertMin(value, right)
            is Bin -> {
                when(right) {
                    is Tip -> insertMax(value, left)
                    is Bin -> {
                        if (DELTA * left.size <= right.size)
                            balance(right.value, join(value, left, right.left), right.right)
                        else if (DELTA * right.size <= left.size)
                            balance(left.value, left.left, join(value, left.right, right))
                        else
                            bin(value, left, right)
                    }
                }
            }
        }
    }   // join

    /**
     * insertMax x t
     *   = case t of
     *       Tip -> singleton x
     *       Bin _ y l r
     *           -> balance y l (insertMax x r)
     */
    internal fun <A : Comparable<A>> insertMax(value: A, set: Set<A>): Set<A> {
        return when(set) {
            is Tip -> singleton(value)
            is Bin -> balance(set.value, set.left, insertMax(value, set.right))
        }
    }   // insertMax

    /**
     * insertMin x t
     *   = case t of
     *       Tip -> singleton x
     *       Bin _ y l r
     *           -> balance y (insertMin x l) r
     */
    internal fun <A : Comparable<A>> insertMin(value: A, set: Set<A>): Set<A> {
        return when(set) {
            is Tip -> singleton(value)
            is Bin -> balance(set.value, insertMin(value, set.left), set.right)
        }
    }   // insertMin

    /**
     * Merge two trees.
     *
     * merge Tip r   = r
     * merge l Tip   = l
     * merge l@(Bin sizeL x lx rx) r@(Bin sizeR y ly ry)
     *   | delta*sizeL < sizeR = balanceL y (merge l ly) ry
     *   | delta*sizeR < sizeL = balanceR x lx (merge rx r)
     *   | otherwise           = glue l r
     */
    internal fun <A : Comparable<A>> merge(left: Set<A>, right: Set<A>): Set<A> {
        return when(left) {
            is Tip -> right
            is Bin -> {
                when(right) {
                    is Tip -> left
                    is Bin -> {
                        if (DELTA * left.size <= right.size)
                            balance(right.value, merge(left, right.left), right.right)
                        else if (DELTA * right.size <= left.size)
                            balance(left.value, left.left, merge(left.right, right))
                        else
                            glue(left, right)
                    }
                }
            }
        }
    }   // merge

    /**
     * The union of two trees, preferring the first tree when equal elements are encountered.
     *   The implementation uses the efficient 'hedge-union' algorithm.
     */
    internal fun <A : Comparable<A>> hedgeUnion(cmpLo: (A) -> Int, cmpHi: (A) -> Int, left: Set<A>, right: Set<A>): Set<A> {
        return when(right) {
            is Tip -> left
            is Bin -> {
                when(left) {
                    is Tip -> join(right.value, filterGT(cmpLo, right.left), filterLT(cmpHi, right.right))
                    is Bin -> {
                        val cmpT: (A) -> Int = {a: A -> left.value.compareTo(a)}
                        join(left.value, hedgeUnion(cmpLo, cmpT, left.left, trim(cmpLo, cmpT, right)), hedgeUnion(cmpT, cmpHi, left.right, trim(cmpT, cmpHi, right)))
                    }
                }
            }
        }
    }   // hedgeUnion

    /**
     * Difference of two trees. The implementation uses an efficient 'hedge' algorithm comparable
     *   with 'hedge-union'.
     */
    internal fun <A : Comparable<A>> hedgeDifference(cmpLo: (A) -> Int, cmpHi: (A) -> Int, left: Set<A>, right: Set<A>): Set<A> {
        return when(left) {
            is Tip -> empty()
            is Bin -> {
                when(right) {
                    is Tip -> join(left.value, filterGT(cmpLo, left.left), filterLT(cmpHi, left.right))
                    is Bin -> {
                        val cmpT: (A) -> Int = {a: A -> right.value.compareTo(a)}
                        merge(hedgeDifference(cmpLo, cmpT, trim(cmpLo, cmpT, left), right.left), hedgeDifference(cmpT, cmpHi, trim(cmpT, cmpHi, left), right.right))
                    }
                }
            }
        }
    }   // hedgeDifference

    /**
     * Filter all values that deliver +1 when applied to the cmp from the given tree.
     */
    internal fun <A : Comparable<A>> filterGT(cmp: (A) -> Int, set: Set<A>): Set<A> {
        return when(set) {
            is Tip -> empty()
            is Bin -> {
                if (cmp(set.value) < 0)
                    join(set.value, filterGT(cmp, set.left), set.right)
                else if (cmp(set.value) > 0)
                    filterGT(cmp, set.right)
                else
                    set.right
            }
        }
    }   // filterGT

    /**
     * Filter all values that deliver -1 when applied to the cmp from the given tree.
     */
    internal fun <A : Comparable<A>> filterLT(cmp: (A) -> Int, set: Set<A>): Set<A> {
        return when(set) {
            is Tip -> empty()
            is Bin -> {
                if (cmp(set.value) < 0)
                    filterLT(cmp, set.left)
                else if (cmp(set.value) > 0)
                    join(set.value, set.left, filterLT(cmp, set.right))
                else
                    set.left
            }
        }
    }   // filterLT

    /**
     * Trim away all subtrees that surely contain no values between the range lo to hi as delivered
     *   by the cmpLo and cmpHi functions. The returned tree is either empty or the key of the root
     *   is between lo and hi.
     */
    internal fun <A : Comparable<A>> trim(cmpLo: (A) -> Int, cmpHi: (A) -> Int, set: Set<A>): Set<A> {
        return when(set) {
            is Tip -> empty()
            is Bin -> {
                if (cmpLo(set.value) < 0) {
                    if(cmpHi(set.value) > 0)
                        set
                    else
                        trim(cmpLo, cmpHi, set.left)
                } else
                    trim(cmpLo, cmpHi, set.right)
            }
        }
    }   // trim
    /**
     * Perform a split but also returns the pivot element that was found in the original tree.
     *
     * splitLookup _ Tip = (Tip,Nothing,Tip)
     * splitLookup x (Bin _ y l r)
     *    = case compare x y of
     *        LT -> let (lt,found,gt) = splitLookup x l in (lt,found,join y gt r)
     *        GT -> let (lt,found,gt) = splitLookup x r in (join y l lt,found,gt)
     *        EQ -> (l,Just y,r)
     */
    internal fun <A : Comparable<A>> splitLookup(value: A, set: Set<A>): Triple<Set<A>, Option<A>, Set<A>> {
        return when(set) {
            is Tip -> Triple(empty(), none(), empty())
            is Bin -> {
                if(value.compareTo(set.value) < 0) {
                    val split: Triple<Set<A>, Option<A>, Set<A>> = splitLookup(value, set.left)
                    Triple(split.first, split.second, join(set.value, split.third, set.right))
                } else if (value.compareTo(set.value) > 0) {
                    val split: Triple<Set<A>, Option<A>, Set<A>> = splitLookup(value, set.right)
                    Triple(join(set.value, set.left, split.first), split.second, split.third)
                } else
                    Triple(set.left, some(set.value), set.right)
            }
        }
    }   // splitLookup

    /**
     *
     */
    internal fun <A : Comparable<A>> isSubsetOf(set1: Set<A>, set2: Set<A>): Boolean {
        return when (set1) {
            is Tip -> true
            is Bin -> when (set2) {
                is Tip -> false
                is Bin -> {
                    if (set1.size == 1)
                        set2.contains(set1.value)
                    else {
                        val (lt: Set<A>, found: Boolean, gt: Set<A>) = set2.splitMember(set1.value)
                        found && (set1.left.size() <= lt.size()) && (set1.right.size() <= gt.size()) &&
                                isSubsetOf(set1.left, lt) && isSubsetOf(set1.right, gt)
                    }
                }
            }
        }
    }   // isSubsetOf

}   // SetF
