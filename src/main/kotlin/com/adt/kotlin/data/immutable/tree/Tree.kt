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
import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.OptionF.none
import com.adt.kotlin.data.immutable.option.OptionF.some

import com.adt.kotlin.data.immutable.tree.TreeF.balance
import com.adt.kotlin.data.immutable.tree.TreeF.bin
import com.adt.kotlin.data.immutable.tree.TreeF.empty
import com.adt.kotlin.data.immutable.tree.TreeF.fromList
import com.adt.kotlin.data.immutable.tree.TreeF.glue
import com.adt.kotlin.data.immutable.tree.TreeF.hedgeDifference
import com.adt.kotlin.data.immutable.tree.TreeF.join
import com.adt.kotlin.data.immutable.tree.TreeF.merge
import com.adt.kotlin.data.immutable.tree.TreeF.splitLookup
import com.adt.kotlin.data.immutable.tree.TreeF.toList

import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.constant



sealed class Tree<A : Comparable<A>>(val size: Int) {



    object Tip : Tree<Nothing>(0)



    class Bin<A : Comparable<A>> internal constructor(size: Int, val value: A, val left: Tree<A>, val right: Tree<A>) : Tree<A>(size)



    /**
     * Determine if this tree contains the element determined by the predicate.
     *
     * Examples:
     *   {Jessie, John, Ken}.contains{name -> (name == John)} = true
     *   {Jessie, John, Ken}.contains{name -> (name == Irene)} = false
     *   {}.contains{name -> (name == John)} = false
     *
     * @param predicate         search predicate
     * @return                  true if search element is present, false otherwise
     */
    fun contains(predicate: (A) -> Boolean): Boolean {
        fun recContains(predicate: (A) -> Boolean, tree: Tree<A>): Boolean {
            return when(tree) {
                is Tip -> false
                is Bin -> {
                    if (predicate(tree.value))
                        true
                    else if (recContains(predicate, tree.left))
                        true
                    else
                        recContains(predicate, tree.right)
                }
            }
        }   // recContains

        return recContains(predicate, this)
    }   // contains

    /**
     * Determine if the tree contains the given element.
     *
     * Examples:
     *   {Jessie, John, Ken}.contains(John) = true
     *   {Jessie, John, Ken}.contains(Irene) = false
     *   {}.contains(John) = false
     *
     * @param a                 search element
     * @return                  true if the given element is in the tree
     */
    fun contains(a: A): Boolean = this.contains{b: A -> (b == a)}

    /**
     * Delete the value from the set. When the value is not a member
     *   of the tree, the original tree is returned.
     *
     * Examples:
     *   {Jessie, John, Ken}.delete(John) = {Jessie, Ken}
     *   {Jessie, John, Ken}.delete(Irene) = {Jessie, John, Ken}
     *
     * @param a                 existing element to remove
     * @result                  updated set
     */
    fun delete(a: A): Tree<A> {
        fun recDelete(a: A, tree: Tree<A>): Tree<A> {
            return when(tree) {
                is Tip -> tree
                is Bin -> {
                    if (a.compareTo(tree.value) < 0)
                        balance(tree.value, recDelete(a, tree.left), tree.right)
                    else if (a.compareTo(tree.value) > 0)
                        balance(tree.value, tree.left, recDelete(a, tree.right))
                    else
                        glue(tree.left, tree.right)
                }
            }
        }   // recDelete

        return recDelete(a, this)
    }   // delete

    /**
     * Difference two trees, ie all the elements in this tree that are
     *   not present in the given tree.
     *
     * Examples:
     *   {Jessie, John, Ken}.difference({Jessie, John, Ken}) = {}
     *   {Jessie, John, Ken}.difference({John, Ken}) = {Jessie}
     *   {Jessie, John, Ken}.difference({}) = {Jessie, John, Ken}
     *   {}.difference({Jessie, John, Ken}) = {}
     *
     * @param tree              existing tree
     * @return                  the difference of this tree and the given tree
     */
    fun difference(tree: Tree<A>): Tree<A> {
        return when(this) {
            is Tip -> empty<A>()
            is Bin -> {
                when(tree) {
                    is Tip -> this
                    is Bin -> hedgeDifference(constant(-1), constant(+1), this, tree)
                }
            }
        }
    }   // difference

    /**
     * Are two trees equal?
     *
     * @param other             the other tree
     * @return                  true if both trees are the same; false otherwise
     */
    override fun equals(other: Any?): Boolean {
        return if (this === other)
            true
        else if (other == null || this::class.java != other::class.java)
            false
        else {
            @Suppress("UNCHECKED_CAST") val otherTree: Tree<A> = other as Tree<A>
            (this.size == otherTree.size) && (this.toAscendingList() == otherTree.toAscendingList())
        }
    }   // equals

    /**
     * Filter all elements that satisfy the predicate.
     *
     * Examples:
     *   {Jessie, John, Ken}.filter{name -> name.startsWith(J)} = {Jessie, John}
     *   {Jessie, John, Ken}.filter{name -> name.charAt(0) >= A} = {Jessie, John, Ken}
     *   {Jessie, John, Ken}.filter{name -> name.charAt(0) >= Z} = {}
     *   {}.filter{name -> name.startsWith(J)} = {}
     *
     * @param predicate         criteria
     * @return                  tree comprising those elements from this tree that match criteria
     */
    fun filter(predicate: (A) -> Boolean): Tree<A> {
        fun <B : Comparable<B>> recFilter(predicate: (B) -> Boolean, tree: Tree<B>): Tree<B> {
            return when(tree) {
                is Tip -> tree
                is Bin -> {
                    if (predicate(tree.value))
                        join(tree.value, recFilter(predicate, tree.left), recFilter(predicate, tree.right))
                    else
                        merge(recFilter(predicate, tree.left), recFilter(predicate, tree.right))
                }
            }
        }   // recFilter

        return recFilter(predicate, this)
    }   // filter

    /**
     * The find function takes a predicate and a tree and returns the first
     *   element in the tree matching the predicate, or none if there is no
     *   such element.
     *
     * Examples:
     *   {Jessie, John, Ken}.find{name -> name.startsWith(J)} = some(John)
     *   {Jessie, John, Ken}.find{name -> name.charAt(0) >= A} = some(John)
     *   {Jessie, John, Ken}.find{name -> name.charAt(0) >= Z} = none
     *   {}.find{name -> name.startsWith(J)} = none
     *
     * @param predicate         criteria
     * @return                  matching element, if found
     */
    fun find(predicate: (A) -> Boolean): Option<A> {
        fun recFind(predicate: (A) -> Boolean, tree: Tree<A>): Option<A> {
            return when(tree) {
                is Tip -> none()
                is Bin -> {
                    if (predicate(tree.value))
                        some(tree.value)
                    else {
                        val opt: Option<A> = recFind(predicate, tree.left)
                        if (opt.isEmpty())
                            recFind(predicate, tree.right)
                        else
                            opt
                    }
                }
            }
        }   // recFind

        return recFind(predicate, this)
    }   // find

    fun find(a: A): Option<A> = find{b -> (b == a)}

    /**
     * foldLeft is a higher-order function that folds a left associative binary
     *   function into the values of a tree.
     *
     * Examples:
     *   ////<[Jessie: 22, John: 31, Ken: 25]>.foldLeft(0){res -> {age -> res + age}} = 78
     *   ////<[]>.foldLeft(0){res -> {age -> res + age}} = 0
     *   ////<[Jessie: 22, John: 31, Ken: 25]>.foldLeft([]){res -> {age -> res.append(age)}} = [22, 31, 25]
     *
     * @param e           	    initial value
     * @param f         		curried binary function:: W -> V -> W
     * @return            	    folded result
     */
    fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B {
        fun <B> recFoldLeft(e: B, tree: Tree<A>, f: (B) -> (A) -> B): B {
            return when(tree) {
                is Tip -> e
                is Bin -> recFoldLeft(f(recFoldLeft(e, tree.left, f))(tree.value), tree.right, f)
            }
        }

        return recFoldLeft(e, this, f)
    }   // foldLeft

    fun <B> foldLeft(e: B, f: (B, A) -> B): B = this.foldLeft(e, C2(f))

    /**
     * foldRight is a higher-order function that folds a right associative binary
     *   function into the values of a tree.
     *
     * Examples:
     *   ////<[Jessie: 22, John: 31, Ken: 25]>.foldRight(0){age -> {res -> res + age}} = 78
     *   ////<[]>.foldRight(0){age -> {res -> res + age}} = 0
     *   ////<[Jessie: 22, John: 31, Ken: 25]>.foldRight([]){age -> {res -> res.append(age)}} = [25, 31, 22]
     *
     * @param e           	    initial value
     * @param f         		curried binary function:: V -> W -> W
     * @return            	    folded result
     */
    fun <B> foldRight(e: B, f: (A) -> (B) -> B): B {
        fun <B> recFoldRight(e: B, tree: Tree<A>, f: (A) -> (B) -> B) : B {
            return when(tree) {
                is Tip -> e
                is Bin -> recFoldRight(f(tree.value)(recFoldRight(e, tree.right, f)), tree.left, f)
            }
        }

        return recFoldRight(e, this, f)
    }   // foldRight

    fun <B> foldRight(e: B, f: (A, B) -> B): B = this.foldRight(e, C2(f))

    /**
     * All the elements of this tree meet some criteria. If the tree is empty then
     *    true is returned.
     *
     * Examples:
     *   {1, 2, 3, 4}.forAll{m -> (m > 0)} = true
     *   {1, 2, 3, 4}.forAll{m -> (m > 2)} = false
     *   {1, 2, 3, 4}.forAll{m -> true} = true
     *   {1, 2, 3, 4}.forAll{m -> false} = false
     *   {}.forAll{m -> false} = true
     *   {}.forAll{m -> (m > 0)} = true
     *
     * @param predicate         criteria
     * @return                  true if all elements match criteria
     */
    fun forAll(predicate: (A) -> Boolean): Boolean {
        fun recForAll(predicate: (A) -> Boolean, tree: Tree<A>): Boolean {
            return when (tree) {
                is Tip -> true
                is Bin -> if (!predicate(tree.value))
                    false
                else if (!recForAll(predicate, tree.left))
                    false
                else
                    recForAll(predicate, tree.right)
            }
        }   // recForall

        return recForAll(predicate, this)
    }   // forAll

    /**
     * Insert a new value in the tree. If the value is already present in
     *   the tree, then no action is taken.
     *
     * Examples:
     *   {Jessie, John, Ken}.insert(Irene) = {Irene, Jessie, John, Ken}
     *   {Jessie, John, Ken}.insert(John) = {Jessie, John, Ken}
     *
     * @param a                 new element to be added
     * @return                  updated tree
     */
    fun insert(a: A): Tree<A> {
        fun recInsert(a: A, tree: Tree<A>): Tree<A> {
            return when (tree) {
                is Tip -> bin(a, empty(), empty())
                is Bin -> {
                    if (a.compareTo(tree.value) < 0)
                        balance(tree.value, recInsert(a, tree.left), tree.right)
                    else if (a.compareTo(tree.value) > 0)
                        balance(tree.value, tree.left, recInsert(a, tree.right))
                    else
                        tree
                }
            }
        }   // recInsert

        return recInsert(a, this)
    }   // insert

    /**
     * The intersection of two sets, ie all the elements that are
     *   present in both trees.
     *
     * Examples:
     *   {Jessie, John, Ken}.intersection({Jessie, John, Ken}) = {Jessie, John, Ken}
     *   {Jessie, John, Ken}.intersection({Jessie, John}) = {Jessie, John}
     *   {Jessie, John, Ken}.intersection({Dawn, Irene}) = {}
     *   {Jessie, John, Ken}.intersection({}) = {}
     *   {}.intersection({Jessie, John, Ken}) = {}
     *
     * @param set               existing set
     * @return                  the intersection of the two trees
     */
    fun intersection(tree: Tree<A>): Tree<A> {
        fun recIntersection(left: Tree<A>, right: Tree<A>): Tree<A> {
            return when(left) {
                is Tip -> empty<A>()
                is Bin -> {
                    when(right) {
                        is Tip -> empty<A>()
                        is Bin -> {
                            if (left.value.compareTo(right.value) >= 0) {
                                val split: Triple<Tree<A>, Option<A>, Tree<A>> = splitLookup(right.value, left)
                                val leftSet: Tree<A> = recIntersection(split.first, right.left)
                                val rightSet: Tree<A> = recIntersection(split.third, right.right)
                                if (split.second.isEmpty())
                                    merge(leftSet, rightSet)
                                else
                                    join(split.second.get(), leftSet, rightSet)
                            } else {
                                val split: Triple<Tree<A>, Boolean, Tree<A>> = right.splitMember(left.value)
                                val leftSet: Tree<A> = recIntersection(left.left, split.first)
                                val rightSet: Tree<A> = recIntersection(left.right, split.third)
                                if (split.second)
                                    join(left.value, leftSet, rightSet)
                                else
                                    merge(leftSet, rightSet)
                            }
                        }
                    }
                }
            }
        }   // recIntersection

        return recIntersection(this, tree)
    }   // intersection

    /**
     * Test whether the tree is empty.
     *
     * Examples:
     *   {Jessie, John, Ken}.isEmpty() = false
     *   {}.isEmpty() = true
     *
     * @return                  true if the tree contains no elements
     */
    fun isEmpty(): Boolean =
            when (this) {
                is Tip -> true
                is Bin -> false
            }   // isEmpty

    /**
     * Is this a proper subset of the given tree? (ie. a subset but not equal).
     *
     * Examples:
     *   {Jessie, John, Ken}.isProperSubsetOf({Jessie, John, Ken}) = false
     *   {Jessie, John}.isProperSubsetOf({Jessie, John, Ken}) = true
     *   {Jessie, John, Ken}.isProperSubsetOf({John, Ken}) = false
     *   {}.isProperSubsetOf({Jessie, John, Ken}) = true
     *   {Jessie, John, Ken}.isProperSubsetOf({}) = false
     *   {}.isProperSubsetOf({}) = false
     *
     * @param tree              existing tree
     * @return                  true if this tree is a proper subset of the given tree
     */
    fun isProperSubsetOf(tree: Tree<A>): Boolean = (this.size() < tree.size()) && this.isSubsetOf(tree)

    /**
     * Is this a subset of the given tree?, ie. are all the elements
     *   of this tree also elements of the given tree?
     *
     * Examples:
     *   {Jessie, John, Ken}.isSubsetOf({Jessie, John, Ken}) = true
     *   {Jessie, John}.isSubsetOf({Jessie, John, Ken}) = true
     *   {Jessie, John, Ken}.isSubsetOf({John, Ken}) = false
     *   {}.isSubsetOf({Jessie, John, Ken}) = true
     *   {Jessie, John, Ken}.isSubsetOf({}) = false
     *   {}.isSubsetOf({}) = true
     *
     * @param set               existing tree
     * @return                  true if this tree is a subset of the given tree
     */
    fun isSubsetOf(set: Tree<A>): Boolean {
        fun recIsSubsetOf(left: Tree<A>, right: Tree<A>): Boolean {
            return when(left) {
                is Tip -> true
                is Bin -> {
                    when(right) {
                        is Tip -> false
                        is Bin -> {
                            val (lt: Tree<A>, found: Boolean, gt: Tree<A>) = right.splitMember(left.value)
                          found && (left.left.size() <= lt.size()) && (left.right.size() <= gt.size()) &&
                                  recIsSubsetOf(left.left, lt) && recIsSubsetOf(left.right, gt)
                            //val split: Triple<Tree<A>, Boolean, Tree<A>> = right.splitMember(left.value)
                            //split.second && recIsSubsetOf(left.left, split.first) && recIsSubsetOf(left.right, split.third)
                        }
                    }
                }
            }
        }   // recIsSubsetOf

        return recIsSubsetOf(this, set)
    }   // isSubsetOf

    /**
     * Obtains the size of a tree.
     *
     * Examples:
     *   {Jessie, John, Ken}.length() = 3
     *   {}.length() = 0
     *
     * @return                  the number of elements in the tree
     */
    fun length(): Int = size

    /**
     * Function map applies the function parameter to each item in the tree, delivering
     *   a new tree.
     *
     * Examples:
     *   {Jessie, John, Ken}.map{name -> name.charAt(0)} = {J, K}
     *   {}.map{name -> charAt(0)} = {}
     *
     * @param f                 transformation function
     * @return                  tree with the elements transformed
     */
    fun <B : Comparable<B>> map(f: (A) -> B): Tree<B> {
        val treeList: List<A> = toList(this)
        val mappedList: List<B> = treeList.map(f)
        return fromList(mappedList)
    }   // map

    /**
     * Difference two trees (as an operator), ie all the elements in this tree that are
     *   not present in the given tree.
     *
     * Examples:
     *   {Jessie, John, Ken} - {Jessie, John, Ken} = {}
     *   {Jessie, John, Ken} - {John, Ken} = {Jessie}
     *   {Jessie, John, Ken} - {} = {Jessie, John, Ken}
     *   {} - {Jessie, John, Ken} = {}
     *
     * @param set               existing tree
     * @return                  the difference of this tree and the given tree
     */
    operator fun minus(tree: Tree<A>): Tree<A> = this.difference(tree)

    /**
     * Partition the tree into two trees, one with all elements that satisfy
     *   the predicate and one with all elements that don't satisfy the predicate.
     *
     * Examples:
     *   {Jessie, John, Ken}.partition{name -> name.startsWith(J)} = ({Jessie, John}, {Ken})
     *
     * @param predicate         criteria
     * @return                  pair of trees
     */
    fun partition(predicate: (A) -> Boolean): Pair<Tree<A>, Tree<A>> {
        fun recPartition(predicate: (A) -> Boolean, set: Tree<A>): Pair<Tree<A>, Tree<A>> {
            return when(set) {
                is Tip -> Pair(set, set)
                is Bin -> {
                    if (predicate(set.value)) {
                        val leftPair: Pair<Tree<A>, Tree<A>> = recPartition(predicate, set.left)
                        val rightPair: Pair<Tree<A>, Tree<A>> = recPartition(predicate, set.right)
                        Pair(join(set.value, leftPair.first, rightPair.first), merge(leftPair.second, rightPair.second))
                    } else {
                        val leftPair: Pair<Tree<A>, Tree<A>> = recPartition(predicate, set.left)
                        val rightPair: Pair<Tree<A>, Tree<A>> = recPartition(predicate, set.right)
                        Pair(merge(leftPair.first, rightPair.first), join(set.value, leftPair.second, rightPair.second))
                    }
                }
            }
        }   // recPartition

        return recPartition(predicate, this)
    }   // partition

    /**
     * The union of two trees (as an operator), ie all the elements from this tree and
     *   from the given tree.
     *
     * Examples:
     *   {Jessie, John, Ken} + {Dawn, Irene} = {Dawn, Irene, Jessie, John, Ken}
     *   {Jessie, John, Ken} + {Jessie, Irene} = {Irene, Jessie, John, Ken}
     *   {Jessie, John, Ken} + {} = {Jessie, John, Ken}
     *   {} + {Dawn, Irene} = {Dawn, Irene}
     *
     * @param set               existing tree
     * @return                  the union of the two trees
     */
    operator fun plus(tree: Tree<A>): Tree<A> = this.union(tree)

    /**
     * Obtain the size of the tree, a synonym for length.
     *
     * Examples:
     *   {Jessie, John, Ken}.size() = 3
     *   {}.size() = 0
     *
     * @return                  the number of elements in the tree
     */
    fun size(): Int = size

    /**
     * The expression split x tree is a pair (tree1, tree2) where tree1 comprises
     *   the elements of tree less than x and tree2 comprises the elements of
     *   tree greater than x.
     *
     * Examples:
     *   {Jessie, John, Ken}.split(John) = ({Jessie}, {Ken})
     *   {Jessie, John, Ken}.split(Linda) = ({Jessie, John, Ken}, {})
     *
     * @param a                 the pivot element
     * @return                  pair of trees
     */
    fun split(a: A): Pair<Tree<A>, Tree<A>> {
        fun recSplit(a: A, tree: Tree<A>): Pair<Tree<A>, Tree<A>> {
            return when(tree) {
                is Tip -> Pair(tree, tree)
                is Bin -> {
                    if (a.compareTo(tree.value) < 0) {
                        val leftSplit: Pair<Tree<A>, Tree<A>> = recSplit(a, tree.left)
                        Pair(leftSplit.first, join(tree.value, leftSplit.second, tree.right))
                    } else if (a.compareTo(tree.value) > 0) {
                        val rightSplit: Pair<Tree<A>, Tree<A>> = recSplit(a, tree.right)
                        Pair(join(tree.value, tree.left, rightSplit.first), rightSplit.second)
                    } else
                        Pair(tree.left, tree.right)
                }
            }
        }   // recSplit

        return recSplit(a, this)
    }   // split

    /**
     * Performs a split but also returns whether the pivot element was found
     *   in the original tree.
     *
     * Examples:
     *   {Jessie, John, Ken}.splitMember(John) = ({Jessie}, true, {Ken})
     *   {Jessie, John, Ken}.splitMember(Linda) = ({Jessie, John, Ken}, false, {})
     *
     * @param a                 the pivot element
     * @return                  triple of the two trees and if the pivot was present
     */
    fun splitMember(a: A): Triple<Tree<A>, Boolean, Tree<A>> {
        val split: Triple<Tree<A>, Option<A>, Tree<A>> = splitLookup(a, this)
        return Triple(split.first, split.second.fold({ -> false}, {_ -> true}), split.third)
    }   // splitMember

    /**
     * The intersection of two trees (as an operator), ie all the elements that are
     *   present in both trees.
     *
     * Examples:
     *   {Jessie, John, Ken} * {Jessie, John, Ken} = {Jessie, John, Ken}
     *   {Jessie, John, Ken} * {Jessie, John} = {Jessie, John}
     *   {Jessie, John, Ken} * {Dawn, Irene} = {}
     *   {Jessie, John, Ken} * {} = {}
     *   {} * {Jessie, John, Ken} = {}
     *
     * @param tree              existing tree
     * @return                  the intersection of the two trees
     */
    operator fun times(tree: Tree<A>): Tree<A> = this.intersection(tree)

    /**
     * Convert the tree to a list of values where the values are in ascending order.
     */
    fun toAscendingList(): List<A> = foldRight(ListF.empty()){a, xs -> ListF.cons(a, xs)}

    /**
     * Produce a string representation of a tree.
     *
     * @return                  string
     */
    override fun toString(): String {
        fun recToString(tree: Tree<A>): String {
            return when (tree) {
                is Tip -> "Tip"
                is Bin -> "Bin(${tree.size}, ${tree.value}, ${tree.left}, ${tree.right})"
            }
        }   // recToString

        return recToString(this)
    }   // = TreeF.drawTree(this)

    /**
     * The union of two trees, ie all the elements from this tree and
     *   from the given tree.
     *
     * Examples:
     *   {Jessie, John, Ken}.union({Dawn, Irene}) = {Dawn, Irene, Jessie, John, Ken}
     *   {Jessie, John, Ken}.union({Jessie, Irene}) = {Irene, Jessie, John, Ken}
     *   {Jessie, John, Ken}.union({}) = {Jessie, John, Ken}
     *   {}.union({Dawn, Irene}) = {Dawn, Irene}
     *
     * @param tree              existing tree
     * @return                  the union of the two trees
     */
    fun union(tree: Tree<A>): Tree<A> {
        return when(this) {
            is Tip -> tree
            is Bin -> {
                when(tree) {
                    is Tip -> this
                    is Bin -> TreeF.hedgeUnion(constant(-1), constant(+1), this, tree)
                }
            }
        }
    }   // union

}   // Tree
