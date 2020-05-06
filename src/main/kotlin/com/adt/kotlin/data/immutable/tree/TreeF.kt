package com.adt.kotlin.data.immutable.tree

/**
 * A class hierarchy defining an immutable tree collection. The algebraic data
 *   type declaration is:
 *
 * datatype Tree[A] = Tip
 *                 | Bin of A * Tree[A] * Tree[A]
 *
 * Trees are implemented as size balanced binary trees. This implementation
 *   mirrors the Haskell implementation in Data.Set that, in turn, is based
 *   on an efficient balanced binary tree referenced in the sources.
 *
 * The Tree class is defined generically in terms of the type parameter A.
 *
 * @author	                    Ken Barclay
 * @since                       October 2012
 */

import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.ListF
import com.adt.kotlin.data.immutable.list.append
import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.OptionF.none
import com.adt.kotlin.data.immutable.option.OptionF.some

import com.adt.kotlin.data.immutable.tree.Tree.Tip
import com.adt.kotlin.data.immutable.tree.Tree.Bin



object TreeF {

    /**
     * Factory binding/function to create the base instances.
     */
    @Suppress("UNCHECKED_CAST")
    fun <A : Comparable<A>> tip(): Tree<A> = Tip as Tree<A>
    fun <A : Comparable<A>> bin(a: A, left: Tree<A>, right: Tree<A>): Tree<A> = Bin(1 + left.size + right.size, a, left, right)

    /**
     * Create an empty tree.
     */
    @Suppress("UNCHECKED_CAST")
    fun <A : Comparable<A>> empty(): Tree<A> = Tip as Tree<A>

    /**
     * Create a tree with a single element.
     */
    fun <A : Comparable<A>> singleton(a: A): Tree<A> = bin(a, tip(), tip())

    fun <A : Comparable<A>> of(a1: A): Tree<A> = singleton(a1)

    fun <A : Comparable<A>> of(a1: A, a2: A): Tree<A> = singleton(a1).insert(a2)

    fun <A : Comparable<A>> of(a1: A, a2: A, a3: A): Tree<A> = singleton(a1).insert(a2).insert(a3)

    fun <A : Comparable<A>> of(a1: A, a2: A, a3: A, a4: A): Tree<A> = singleton(a1).insert(a2).insert(a3).insert(a4)

    fun <A : Comparable<A>> of(a1: A, a2: A, a3: A, a4: A, a5: A): Tree<A> = singleton(a1).insert(a2).insert(a3).insert(a4).insert(a5)

    fun <A : Comparable<A>> of(vararg a: A): Tree<A> = fromSeq(*a)



    /**
     * Convert a variable-length parameter series into a tree.
     *
     * Examples:
     *   fromSequence(Jessie, John, Ken) = {Jessie, John, Ken}
     *   fromSequence() = {}
     *
     * @param seq                   variable-length parameter series
     * @return                      tree of the given values
     */
    fun <A : Comparable<A>> fromSeq(vararg seq: A): Tree<A> =
            seq.fold(empty()){tree, a -> tree.insert(a)}

    /**
     * Convert a variable-length immutable list into a tree.
     *
     * Examples:
     *   fromList([Jessie, John, Ken]) = {Jessie, John, Ken}
     *   fromList([]) = {}
     *
     * @param ls                    variable-length list
     * @return                      tree of the given values
     */
    fun <A : Comparable<A>> fromList(ls: List<A>): Tree<A> {
        return ls.foldRight(empty()) { a -> { tree: Tree<A> -> tree.insert(a) } }
    }

    /**
     * Convert an immutable tree to an immutable list.
     *
     * @param set                   existing immutable tree
     * @return                      a list
     */
    fun <A : Comparable<A>> toList(tree: Tree<A>): List<A> {
        fun recToList(tree: Tree<A>): List<A> {
            return when(tree) {
                is Tip -> ListF.empty()
                is Bin -> recToList(tree.left).append(tree.value).append(recToList(tree.right))
            }
        }

        return recToList(tree)
    }   // toList



    /**
     * Show the tree. The tree is presented in
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
    fun <A: Comparable<A>> drawTree(tree: Tree<A>): String =
        drawTree(ListF.empty(), tree)

    internal fun <A: Comparable<A>> drawTree(bars: List<String>, tree: Tree<A>): String {
        return if (tree.isEmpty())
            "${drawBars(bars)}|\n"
        else {
            @Suppress("UNCHECKED_CAST") val bin: Bin<A> = tree as Bin<A>
            if (bin.left.isEmpty() && bin.right.isEmpty())
                "${drawBars(bars)}${bin.value}\n"
            else {
                val text5: String = drawTree(withEmpty(bars), bin.right)
                val text4: String = drawWide(bars)
                val text3: String = drawTree(withBar(bars), bin.left)
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



// ---------- implementation ------------------------------

    private val DELTA: Int = 4      // the maximal relative difference between the sizes of two trees
    private val RATIO: Int = 2      // is the ratio between an outer and inner sibling of the
                                    // heavier subtree in an unbalanced setting

    /**
     * Balance two sets with the value a. The sizes of the trees should balance
     *   after decreasing the size of one of them (a rotation).
     */
    internal fun <A : Comparable<A>> balance(a: A, left: Tree<A>, right: Tree<A>): Tree<A> {
        return if(left.size() + right.size <= 1)
            bin(a, left, right)
        else if (right.size >= DELTA * left.size)
            rotateLeft(a, left, right)
        else if (left.size >= DELTA * right.size)
            rotateRight(a, left, right)
        else
            bin(a, left, right)
    }    // balance

    internal fun <A : Comparable<A>> rotateLeft(value: A, left: Tree<A>, right: Tree<A>): Tree<A> {
        return when (right) {
            is Tip -> throw TreeException("rotateLeft: right is Tip")
            is Bin -> {
                if (right.left.size < RATIO * right.right.size)
                    singleLeft(value, left, right)
                else
                    doubleLeft(value, left, right)
            }
        }
    }   // rotateLeft

    internal fun <A : Comparable<A>> rotateRight(value: A, left: Tree<A>, right: Tree<A>): Tree<A> {
        return when (left) {
            is Tip -> throw TreeException("rotateRight: left is Tip")
            is Bin -> {
                if (left.right.size < RATIO * left.left.size)
                    singleRight(value, left, right)
                else
                    doubleRight(value, left, right)
            }
        }
    }   // rotateRight

    /**
     * Compose a new tree:
     *
     * singleLeft(x1, t1, Bin(_, x2, t2, t3)) = bin(x2, bin(x1, t1, t2), t3)
     */
    internal fun <A : Comparable<A>> singleLeft(value: A, left: Tree<A>, right: Tree<A>): Tree<A> {
        return when(right) {
            is Tip -> throw TreeException("singleLeft: right is Tip")
            is Bin -> bin(right.value, bin(value, left, right.left), right.right)
        }
    }   // singleLeft

    /**
     * Compose a new tree:
     *
     * singleRight(x1, Bin(_, x2, t1, t2), t3) = bin(x2, t1, bin(x1, t2, t3))
     */
    internal fun <A : Comparable<A>> singleRight(value: A, left: Tree<A>, right: Tree<A>): Tree<A> {
        return when(left) {
            is Tip -> throw TreeException("singleRight: left is Tip")
            is Bin -> bin(left.value, left.left, bin(value, left.right, right))
        }
    }   // singleRight

    /**
     * Compose a new tree:
     *
     * doubleLeft(x1, t1, Bin(_, x2, Bin(_, x3, t2, t3), t4)) = bin(x3, bin(x1, t1, t2), bin(x2, t3, t4))
     */
    internal fun <A : Comparable<A>> doubleLeft(value: A, left: Tree<A>, right: Tree<A>): Tree<A> {
        return when(right) {
            is Tip -> throw TreeException("doubleLeft: right is Tip")
            is Bin -> {
                val rightLeft: Tree<A> = right.left
                when (rightLeft) {
                    is Tip -> throw TreeException("doubleLeft: right.left is Tip")
                    is Bin -> bin(rightLeft.value, bin(value, left, rightLeft.left), bin(right.value, rightLeft.right, right.right))
                }
            }
        }
    }   // doubleLeft

    /**
     * Compose a new tree:
     *
     * doubleRight(x1, Bin(_, x2, t1, Bin(_, x3, t2, t3)), t4) = bin(x3, bin(x2, t1, t2), bin(x1, t3, t4))
     */
    internal fun <A : Comparable<A>> doubleRight(value: A, left: Tree<A>, right: Tree<A>): Tree<A> {
        return when (left) {
            is Tip -> throw TreeException("doubleRight: left is Tip")
            is Bin -> {
                val leftRight: Tree<A> = left.right
                when (leftRight) {
                    is Tip -> throw TreeException("doubleRight: left.right is Tip")
                    is Bin -> bin(leftRight.value, bin(left.value, left.left, leftRight.left), bin(value, leftRight.right, right))
                }
            }
        }
    }   // doubleRight

    /**
     * Glue the two trees together. Assumes that left and right are already balanced with respect to each other.
     *
     * glue Tip r = r
     * glue l Tip = l
     * glue l r
     *   | size l > size r = let (m,l') = deleteFindMax l in balanceR m l' r
     *   | otherwise       = let (m,r') = deleteFindMin r in balanceL m l r'
     */
    internal fun <A : Comparable<A>> glue(left: Tree<A>, right: Tree<A>): Tree<A> {
        return when(left) {
            is Tip -> right
            is Bin -> {
                when(right) {
                    is Tip -> left
                    is Bin -> {
                        if (left.size > right.size) {
                            val dfm: Pair<A, Tree<A>> = deleteFindMax(left)
                            balance(dfm.first, dfm.second, right)
                        } else {
                            val dfm: Pair<A, Tree<A>> = deleteFindMin(right)
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
    internal fun <A : Comparable<A>> deleteFindMin(tree: Tree<A>): Pair<A, Tree<A>> {
        return when(tree) {
            is Tip -> throw TreeException("deleteFindMin: empty tree")
            is Bin -> {
                val setLeft: Tree<A> = tree.left
                when(setLeft) {
                    is Tip -> Pair(tree.value, tree.right)
                    is Bin -> {
                        val dfm: Pair<A, Tree<A>> = deleteFindMin(tree.left)
                        Pair(dfm.first, balance(tree.value, dfm.second, tree.right))
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
    internal fun <A : Comparable<A>> deleteFindMax(tree: Tree<A>): Pair<A, Tree<A>> {
        return when(tree) {
            is Tip -> throw TreeException("deleteFindMax: empty tree")
            is Bin -> {
                val setRight: Tree<A> = tree.right
                when(setRight) {
                    is Tip -> Pair(tree.value, tree.left)
                    is Bin -> {
                        val dfm: Pair<A, Tree<A>> = deleteFindMax(tree.right)
                        Pair(dfm.first, balance(tree.value, tree.left, dfm.second))
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
    internal fun <A : Comparable<A>> join(value: A, left: Tree<A>, right: Tree<A>): Tree<A> {
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
    internal fun <A : Comparable<A>> insertMax(value: A, tree: Tree<A>): Tree<A> {
        return when(tree) {
            is Tip -> singleton(value)
            is Bin -> balance(tree.value, tree.left, insertMax(value, tree.right))
        }
    }   // insertMax

    /**
     * insertMin x t
     *   = case t of
     *       Tip -> singleton x
     *       Bin _ y l r
     *           -> balance y (insertMin x l) r
     */
    internal fun <A : Comparable<A>> insertMin(value: A, tree: Tree<A>): Tree<A> {
        return when(tree) {
            is Tip -> singleton(value)
            is Bin -> balance(tree.value, insertMin(value, tree.left), tree.right)
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
    internal fun <A : Comparable<A>> merge(left: Tree<A>, right: Tree<A>): Tree<A> {
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
    internal fun <A : Comparable<A>> hedgeUnion(cmpLo: (A) -> Int, cmpHi: (A) -> Int, left: Tree<A>, right: Tree<A>): Tree<A> {
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
    internal fun <A : Comparable<A>> hedgeDifference(cmpLo: (A) -> Int, cmpHi: (A) -> Int, left: Tree<A>, right: Tree<A>): Tree<A> {
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
    internal fun <A : Comparable<A>> filterGT(cmp: (A) -> Int, tree: Tree<A>): Tree<A> {
        return when(tree) {
            is Tip -> empty()
            is Bin -> {
                if (cmp(tree.value) < 0)
                    join(tree.value, filterGT(cmp, tree.left), tree.right)
                else if (cmp(tree.value) > 0)
                    filterGT(cmp, tree.right)
                else
                    tree.right
            }
        }
    }   // filterGT

    /**
     * Filter all values that deliver -1 when applied to the cmp from the given tree.
     */
    internal fun <A : Comparable<A>> filterLT(cmp: (A) -> Int, tree: Tree<A>): Tree<A> {
        return when(tree) {
            is Tip -> empty()
            is Bin -> {
                if (cmp(tree.value) < 0)
                    filterLT(cmp, tree.left)
                else if (cmp(tree.value) > 0)
                    join(tree.value, tree.left, filterLT(cmp, tree.right))
                else
                    tree.left
            }
        }
    }   // filterLT

    /**
     * Trim away all subtrees that surely contain no values between the range lo to hi as delivered
     *   by the cmpLo and cmpHi functions. The returned tree is either empty or the key of the root
     *   is between lo and hi.
     */
    internal fun <A : Comparable<A>> trim(cmpLo: (A) -> Int, cmpHi: (A) -> Int, tree: Tree<A>): Tree<A> {
        return when(tree) {
            is Tip -> empty()
            is Bin -> {
                if (cmpLo(tree.value) < 0) {
                    if(cmpHi(tree.value) > 0)
                        tree
                    else
                        trim(cmpLo, cmpHi, tree.left)
                } else
                    trim(cmpLo, cmpHi, tree.right)
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
    internal fun <A : Comparable<A>> splitLookup(value: A, set: Tree<A>): Triple<Tree<A>, Option<A>, Tree<A>> {
        return when(set) {
            is Tip -> Triple(empty(), none(), empty())
            is Bin -> {
                if(value.compareTo(set.value) < 0) {
                    val split: Triple<Tree<A>, Option<A>, Tree<A>> = splitLookup(value, set.left)
                    Triple(split.first, split.second, join(set.value, split.third, set.right))
                } else if (value.compareTo(set.value) > 0) {
                    val split: Triple<Tree<A>, Option<A>, Tree<A>> = splitLookup(value, set.right)
                    Triple(join(set.value, set.left, split.first), split.second, split.third)
                } else
                    Triple(set.left, some(set.value), set.right)
            }
        }
    }   // splitLookup

}   // TreeF
