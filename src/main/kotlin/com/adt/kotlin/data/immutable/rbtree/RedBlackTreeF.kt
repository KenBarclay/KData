package com.adt.kotlin.data.immutable.rbtree

/**
 * A class hierarchy defining an immutable red-black tree. The algebraic data
 *   type declaration is:
 *
 * datatype RedBlackTree[K, V] = EmptyTree
 *                             | RedTree of K * V * RedBlackTree[K, V] * RedBlackTree[K, V]
 *                             | BlackTree of K * V * RedBlackTree[K, V] * RedBlackTree[K, V]
 *
 * Red-black trees are binary search trees obeying two key invariants:
 *
 *   (1) Any path from a root node to a leaf node contains the same number
 *       of black nodes (balancing condition).
 *
 *   (2) Red nodes always have black children.
 *
 * @param K                     the type of key elements in the tree
 * @param V                     the type of value elements in the tree
 *
 * @author	                    Ken Barclay
 * @since                       April 2017
 */

import com.adt.kotlin.data.immutable.rbtree.RedBlackTree.EmptyTree
import com.adt.kotlin.data.immutable.rbtree.RedBlackTree.RBTree
import com.adt.kotlin.data.immutable.rbtree.RedBlackTree.RBTree.BlackTree
import com.adt.kotlin.data.immutable.rbtree.RedBlackTree.RBTree.RedTree

import com.adt.kotlin.data.immutable.list.*
import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.List.Nil
import com.adt.kotlin.data.immutable.list.List.Cons
import com.adt.kotlin.data.immutable.list.ListF
import com.adt.kotlin.data.immutable.list.ListF.nil
import com.adt.kotlin.data.immutable.list.ListF.cons
import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3

import kotlin.collections.List as KList



object RedBlackTreeF {

    /**
     * Create an empty tree.
     *
     * @return    		            an empty tree
     */
    fun <K : Comparable<K>, V> empty(): RedBlackTree<K, V> = EmptyTree<K, V>()



    fun <K : Comparable<K>, V> of(key1: K, value1: V): RedBlackTree<K, V> =
        BlackTree(key1, value1, EmptyTree(), EmptyTree())

    fun <K : Comparable<K>, V> of(key1: K, value1: V, key2: K, value2: V): RedBlackTree<K, V> =
        BlackTree(key1, value1, EmptyTree(), EmptyTree()).update(key2, value2)

    fun <K : Comparable<K>, V> of(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V): RedBlackTree<K, V> =
        BlackTree(key1, value1, EmptyTree(), EmptyTree()).update(key2, value2).update(key3, value3)

    fun <K : Comparable<K>, V> of(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V, key4: K, value4: V): RedBlackTree<K, V> =
        BlackTree(key1, value1, EmptyTree(), EmptyTree()).update(key2, value2).update(key3, value3).update(key4, value4)

    fun <K : Comparable<K>, V> of(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V, key4: K, value4: V, key5: K, value5: V): RedBlackTree<K, V> =
        BlackTree(key1, value1, EmptyTree(), EmptyTree()).update(key2, value2).update(key3, value3).update(key4, value4).update(key5, value5)

    fun <K : Comparable<K>, V> of(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V, key4: K, value4: V, key5: K, value5: V, key6: K, value6: V): RedBlackTree<K, V> =
        BlackTree(key1, value1, EmptyTree(), EmptyTree()).update(key2, value2).update(key3, value3).update(key4, value4).update(key5, value5).update(key6, value6)

    fun <K : Comparable<K>, V> of(key1: K, value1: V, key2: K, value2: V, key3: K, value3: V, key4: K, value4: V, key5: K, value5: V, key6: K, value6: V, key7: K, value7: V): RedBlackTree<K, V> =
        BlackTree(key1, value1, EmptyTree(), EmptyTree()).update(key2, value2).update(key3, value3).update(key4, value4).update(key5, value5).update(key6, value6).update(key7, value7)



    fun <K: Comparable<K>, V> of(e1: Pair<K, V>): RedBlackTree<K, V> = BlackTree(e1.first, e1.second, EmptyTree(), EmptyTree())

    fun <K: Comparable<K>, V> of(e1: Pair<K, V>, e2: Pair<K, V>): RedBlackTree<K, V> =
        BlackTree(e1.first, e1.second, EmptyTree(), EmptyTree()).update(e2.first, e2.second)

    fun <K: Comparable<K>, V> of(e1: Pair<K, V>, e2: Pair<K, V>, e3: Pair<K, V>): RedBlackTree<K, V> =
        BlackTree(e1.first, e1.second, EmptyTree(), EmptyTree()).update(e2.first, e2.second).update(e3.first, e3.second)

    fun <K: Comparable<K>, V> of(e1: Pair<K, V>, e2: Pair<K, V>, e3: Pair<K, V>, e4: Pair<K, V>): RedBlackTree<K, V> =
        BlackTree(e1.first, e1.second, EmptyTree(), EmptyTree()).update(e2.first, e2.second).update(e3.first, e3.second).update(e4.first, e4.second)

    fun <K: Comparable<K>, V> of(e1: Pair<K, V>, e2: Pair<K, V>, e3: Pair<K, V>, e4: Pair<K, V>, e5: Pair<K, V>): RedBlackTree<K, V> =
        BlackTree(e1.first, e1.second, EmptyTree(), EmptyTree()).update(e2.first, e2.second).update(e3.first, e3.second).update(e4.first, e4.second).update(e5.first, e5.second)



    /**
     * A tree with a single element.
     */
    fun <K : Comparable<K>, V> singleton(key: K, value: V): RedBlackTree<K, V> = BlackTree(key, value, EmptyTree(), EmptyTree())

    /**
     * Convert a variable-length parameter series into a tree.
     *
     * @param seq                   variable-length parameter series
     * @return                      map of the given values
     */
    fun <K : Comparable<K>, V> fromSeq(vararg seq: Pair<K, V>): RedBlackTree<K, V> =
        seq.fold(empty()){tree, pair -> tree.update(pair.first, pair.second)}

    /**
     *  Build a tree from a list of key/value pairs with a combining function.
     *
     * @param f			        combining function over the values
     * @param seq   	        series of key/value pairs
     * @return			        the required tree
     */
    fun <K : Comparable<K>, V> fromSeqWith(vararg seq: Pair<K, V>, f: (V) -> (V) -> V): RedBlackTree<K, V> =
        fromSeqWithKey(*seq){_ -> {v1 -> {v2 -> f(v1)(v2)}}}

    fun <K : Comparable<K>, V> fromSeqWith(vararg seq: Pair<K, V>, f: (V, V) -> V): RedBlackTree<K, V> = fromSeqWith(*seq){v1 -> {v2 -> f(v1, v2)}}

    /**
     *  Build a tree from a list of key/value pairs with a combining function.
     *
     * @param f			        combining function
     * @param seq		        series of key/value pairs
     * @return			        the required tree
     */
    fun <K : Comparable<K>, V> fromSeqWithKey(vararg seq: Pair<K, V>, f: (K) -> (V) -> (V) -> V): RedBlackTree<K, V> {
        val insertC: (RedBlackTree<K, V>) -> (Pair<K, V>) -> RedBlackTree<K, V> = {tree -> {pair -> tree.insertWithKey(pair.first, pair.second, f)}}
        val pairs: List<Pair<K, V>> = seq.fold(ListF.empty<Pair<K, V>>()){list, pair -> ListF.cons(pair, list)}
        return pairs.foldLeft(empty<K, V>(), insertC)
    }   // fromWithKey

    fun <K : Comparable<K>, V> fromSeqWithKey(vararg seq: Pair<K, V>, f: (K, V, V) -> V): RedBlackTree<K, V> = fromSeqWithKey(*seq){v1 -> {v2 -> {v3 -> f(v1, v2, v3)}}}

    /**
     * Convert a variable-length list into a tree.
     *
     * @param xs                variable-length list
     * @return                  tree of the given values
     */
    fun <K : Comparable<K>, V> fromList(xs: KList<Pair<K, V>>): RedBlackTree<K, V> =
        xs.fold(empty()){tree, pair -> tree.update(pair.first, pair.second)}

    /**
     * Convert a variable-length list into a tree.
     *
     * @param xs                variable-length list
     * @return                  tree of the given values
     */
    fun <K : Comparable<K>, V> fromList(xs: List<Pair<K, V>>): RedBlackTree<K, V> =
        xs.foldLeft(empty<K, V>()){tree -> {pair -> tree.update(pair.first, pair.second)}}

    /**
     *  Build a tree from a list of key/value pairs with a combining function.
     *
     * @param xs		        list of key/value pairs
     * @param f			        curried combining function
     * @return			        the required tree
     */
    fun <K : Comparable<K>, V> fromListWith(xs: List<Pair<K, V>>, f: (V) -> (V) -> V): RedBlackTree<K, V> =
        fromListWithKey(xs){_ -> {v1 -> {v2 -> f(v1)(v2)}}}

    /**
     *  Build a tree from a list of key/value pairs with a combining function.
     *
     * @param xs		        list of key/value pairs
     * @param f			        combining function
     * @return			        the required tree
     */
    fun <K : Comparable<K>, V> fromListWith(xs: List<Pair<K, V>>, f: (V, V) -> V): RedBlackTree<K, V> = fromListWith(xs, C2(f))

    /**
     *  Build a tree from a list of key/value pairs with a combining function.
     *
     * @param xs		        list of key/value pairs
     * @param f			        curried combining function
     * @return			        the required tree
     */
    fun <K : Comparable<K>, V> fromListWithKey(xs: List<Pair<K, V>>, f: (K) -> (V) -> (V) -> V): RedBlackTree<K, V> {
        val insertC: (RedBlackTree<K, V>) -> (Pair<K, V>) -> RedBlackTree<K, V> = {tree -> {pair -> tree.insertWithKey(pair.first, pair.second, f)}}
        return xs.foldLeft(empty<K, V>(), insertC)
    }   // fromWithKey

    /**
     *  Build a tree from a list of key/value pairs with a combining function.
     *
     * @param xs		        list of key/value pairs
     * @param f			        combining function
     * @return			        the required tree
     */
    fun <K : Comparable<K>, V> fromListWithKey(xs: List<Pair<K, V>>, f: (K, V, V) -> V): RedBlackTree<K, V> = fromListWithKey(xs, C3(f))

    /**
     * Convert a tree into an array list of key/value pairs.
     *
     * @param tree              the tree to convert
     * @return                  the array list of key/value pairs
     */
    fun <K : Comparable<K>, V> toKList(tree: RedBlackTree<K, V>): KList<Pair<K, V>> {
        fun <K : Comparable<K>, V> recToList(tree: RedBlackTree<K, V>, acc: KList<Pair<K, V>>): KList<Pair<K, V>> {
            return when(tree) {
                is EmptyTree -> acc
                is RBTree -> {
                    val leftMap: KList<Pair<K, V>> = recToList(tree.left, acc)
                    val rightMap: KList<Pair<K, V>> = recToList(tree.right, leftMap + Pair(tree.key, tree.value))
                    rightMap
                }
            }
        }   // recToList

        return recToList(tree, arrayListOf<Pair<K, V>>())
    }   // toKList

    /**
     * Convert a tree into a list of key/value pairs.
     *
     * @param tree              the tree to convert
     * @return                  the list of key/value pairs
     */
    fun <K : Comparable<K>, V> toList(tree: RedBlackTree<K, V>): List<Pair<K, V>> {
        fun recToKList(tree: RedBlackTree<K, V>): List<Pair<K, V>> {
            return when(tree) {
                is EmptyTree -> ListF.empty()
                is RBTree -> {
                    val front: List<Pair<K, V>> = recToKList(tree.left)
                    val rear: List<Pair<K, V>> = recToKList(tree.right)
                    val middle: Pair<K, V> = Pair(tree.key, tree.value)
                    front.append(middle).append(rear)
                }
            }
        }   // recToKList

        return recToKList(tree)
    }   // toList



// ---------- implementation ------------------------------

    internal fun <K : Comparable<K>, V> lookUpTree(tree: RedBlackTree<K, V>, key: K): RedBlackTree<K, V> {
        tailrec
        fun recLookUpTree(tree: RedBlackTree<K, V>, key: K): RedBlackTree<K, V> {
            return when(tree) {
                is EmptyTree -> tree
                is RBTree -> {
                    val cmp: Int = key.compareTo(tree.key)
                    if (cmp < 0)
                        recLookUpTree(tree.left, key)
                    else if (cmp > 0)
                        recLookUpTree(tree.right, key)
                    else
                        tree
                }
            }
        }   // recLookUpTree

        return recLookUpTree(tree, key)
    }   // lookUpTree

    internal fun <K : Comparable<K>, V> lookUpTree(tree: RedBlackTree<K, V>, predicate: (K) -> Boolean): RedBlackTree<K, V> {
        fun recLookUpTree(tree: RedBlackTree<K, V>, predicate: (K) -> Boolean): RedBlackTree<K, V> {
            return when(tree) {
                is EmptyTree -> tree
                is RBTree -> {
                    val cmp: Boolean = predicate(tree.key)
                    if (cmp)
                        tree
                    else {
                        val left: RedBlackTree<K, V> = recLookUpTree(tree.left, predicate)
                        when(left) {
                            is EmptyTree -> recLookUpTree(tree.right, predicate)
                            is RBTree -> left
                        }
                    }
                }
            }
        }   // recLookUpTree

        return recLookUpTree(tree, predicate)
    }   // lookUpTree



    internal fun <K : Comparable<K>, V> treeCount(tree: RedBlackTree<K, V>): Int =
        when(tree) {
            is EmptyTree -> 0
            is RBTree -> 1 + treeCount(tree.left) + treeCount(tree.right)
        }   // treeCount



    internal fun <K : Comparable<K>, V> isRedTree(tree: RedBlackTree<K, V>): Boolean {
        return when(tree) {
            is EmptyTree -> false
            is RedTree -> true
            is BlackTree -> false
        }
    }   // isRedTree

    internal fun <K : Comparable<K>, V> isBlackTree(tree: RedBlackTree<K, V>): Boolean {
        return when(tree) {
            is EmptyTree -> false
            is RedTree -> false
            is BlackTree -> true
        }
    }   // isBlackTree



    internal fun <K : Comparable<K>, V> blacken(tree: RedBlackTree<K, V>): RedBlackTree<K, V> {
        return when(tree) {
            is EmptyTree -> tree
            is RBTree -> tree.black
        }
    }   // blacken

    internal fun <K : Comparable<K>, V> redden(tree: RedBlackTree<K, V>): RedBlackTree<K, V> {
        return when(tree) {
            is EmptyTree -> tree
            is RBTree -> tree.red
        }
    }   // redden


    /**
     * Make a red or black tree with the given values.
     *
     * @param isBlack           if true make a black tree, otherwise make a red tree
     * @param key               the key for the node
     * @param value             the value for the node
     * @param left              the left sub-tree
     * @param right             the right sub-tree
     * @return                  the new tree
     */
    internal fun <K : Comparable<K>, V> makeTree(isBlack: Boolean, key: K, value: V, left: RedBlackTree<K, V>, right: RedBlackTree<K, V>): RedBlackTree<K, V> =
        if (isBlack)
            BlackTree(key, value, left, right)
        else
            RedTree(key, value, left, right)


    /**
     * Balance the left sub-tree.
     *
     * balanceLeft(b, k, v, EmptyTree, r) = makeTree(b, k, v, EmptyTree, r)
     * balanceLeft(b, k, v, RedTree(k', v', RedTree(k'', v'', ll'', lr''), lr'), r) = RedTree(k', v', BlackTree(k'', v'', ll'', lr''), BlackTree(k, v, lr', r))
     * balanceLeft(b, k, v, RedTree(k', v', l', RedTree(k'', v'', rl'', rr'')), r) = RedTree(k'', v'', BlackTree(k', v', l', rl''), BlackTree(k, v, rr'', r))
     * balanceLeft(b, k, v, l@RedTree(k', v', l', r'), r) = makeTree(b, k, v, l, r)
     * balanceLeft(b, k, v, l@BlackTree(k', v', ll', lr'), r) = makeTree(b, k, v, l, r)
     *
     * @param isBlack           if true make a black tree, otherwise make a red tree
     * @param key               the key for the balanced tree
     * @param value             the value for the balanced tree
     * @param left              the left sub-tree
     * @param right             the right sub-tree
     * @return                  the new balanced tree
     */
    internal fun <K : Comparable<K>, V> balanceLeft(isBlack: Boolean, key: K, value: V, left: RedBlackTree<K, V>, right: RedBlackTree<K, V>): RedBlackTree<K, V> {
        return when(left) {
            is EmptyTree -> makeTree(isBlack, key, value, left, right)
            is RedTree -> {
                when(left.left) {
                    is RedTree -> RedTree(left.key, left.value, BlackTree(left.left.key, left.left.value, left.left.left, left.left.right), BlackTree(key, value, left.right, right))
                    else -> when(left.right) {
                        is RedTree -> RedTree(left.right.key, left.right.value, BlackTree(left.key, left.value, left.left, left.right.left), BlackTree(key, value, left.right.right, right))
                        else -> makeTree(isBlack, key, value, left, right)
                    }
                }
            }
            is BlackTree -> makeTree(isBlack, key, value, left, right)
        }
    }   // balanceLeft

    internal fun <K : Comparable<K>, V> balanceRight(isBlack: Boolean, key: K, value: V, left: RedBlackTree<K, V>, right: RedBlackTree<K, V>): RedBlackTree<K, V> {
        return when(right) {
            is EmptyTree -> makeTree(isBlack, key, value, left, right)
            is RedTree -> {
                when(right.left) {
                    is RedTree -> RedTree(right.left.key, right.left.value, BlackTree(key, value, left, right.left.left), BlackTree(right.key, right.value, right.left.right, right.right))
                    else -> when(right.right) {
                        is RedTree -> RedTree(right.key, right.value, BlackTree(key, value, left, right.left), BlackTree(right.right.key, right.right.value, right.right.left, right.right.right))
                        else -> makeTree(isBlack, key, value, left, right)
                    }
                }
            }
            is BlackTree -> makeTree(isBlack, key, value, left, right)
        }
    }   // balanceRight


    /**
     * Optionally update the tree with the new key/value pair.
     *
     * @param tree              the tree to update
     * @param key               the new key
     * @param value             the new value
     * @param overwrite         overwrite on update
     * @return                  new updated tree
     */
    internal fun <K : Comparable<K>, V> upd(tree: RedBlackTree<K, V>, key: K, value: V, overwrite: Boolean): RedBlackTree<K, V> {
        return when(tree) {
            is EmptyTree -> RedTree(key, value, EmptyTree(), EmptyTree())
            is RBTree -> {
                val cmp: Int = key.compareTo(tree.key)
                if (cmp < 0)
                    balanceLeft(isBlackTree(tree), tree.key, tree.value, upd(tree.left, key, value, overwrite), tree.right)
                else if (cmp > 0)
                    balanceRight(isBlackTree(tree), tree.key, tree.value, tree.left, upd(tree.right, key, value, overwrite))
                else if (overwrite)
                    makeTree(isBlackTree(tree), key, value, tree.left, tree.right)
                else
                    tree
            }
        }
    }   // upd



    internal fun <K : Comparable<K>, V> del(tree: RedBlackTree<K, V>, key: K): RedBlackTree<K, V> {
        fun balance(key: K, value: V, left: RedBlackTree<K, V>, right: RedBlackTree<K, V>): RedBlackTree<K, V> {
            return if (isRedTree(left)) {
                val redLeft: RedTree<K, V> = left as RedTree<K, V>
                if (isRedTree(right)) {
                    val redRight: RedTree<K, V> = right as RedTree<K, V>
                    RedTree(key, value, redLeft.black, redRight.black)
                } else if (isRedTree(redLeft.left)) {
                    val redLeftLeft: RedTree<K, V> = redLeft.left as RedTree<K, V>
                    RedTree(redLeft.key, redLeft.value, redLeftLeft.black, BlackTree(key, value, redLeft.right, right))
                } else if (isRedTree(redLeft.right)) {
                    val redLeftRight: RedTree<K, V> = redLeft.right as RedTree<K, V>
                    RedTree(redLeftRight.key, redLeftRight.value, BlackTree(redLeft.key, redLeft.value, redLeft.left, redLeftRight.left), BlackTree(key, value, redLeftRight.right, right))
                } else
                    BlackTree(key, value, left, right)
            } else if (isRedTree(right)) {
                val redRight: RedTree<K, V> = right as RedTree<K, V>
                if (isRedTree(redRight.right)) {
                    val redRightRight: RedTree<K, V> = redRight.right as RedTree<K, V>
                    RedTree(redRight.key, redRight.value, BlackTree(key, value, left, redRight.left), redRightRight.black)
                } else if (isRedTree(redRight.left)) {
                    val redRightLeft: RedTree<K, V> = redRight.left as RedTree<K, V>
                    RedTree(redRightLeft.key, redRightLeft.value, BlackTree(key, value, left, redRightLeft.left), BlackTree(redRight.key, redRight.value, redRightLeft.right, redRight.right))
                } else
                    BlackTree(key, value, left, right)
            } else
                BlackTree(key, value, left, right)
        }   // balance

        fun subl(tree: RedBlackTree<K, V>): RedBlackTree<K, V> {
            return when(tree) {
                is BlackTree -> tree.red
                else -> throw RedBlackTreeException("RedBlackTree.del/subl: invariance violation: expected black, got: ${tree}")
            }
        }   // subl

        fun balLeft(key: K, value: V, left: RedBlackTree<K, V>, right: RedBlackTree<K, V>): RedBlackTree<K, V> {
            return when(left) {
                is RedTree -> RedTree(key, value, left.black, right)
                else -> {
                    when(right) {
                        is BlackTree -> balance(key, value, left, right.red)
                        is RedTree -> {
                            when(right.left) {
                                is BlackTree -> RedTree(right.left.key, right.left.value, BlackTree(key, value, left, right.left.left), balance(right.key, right.value, right.left.right, subl(right.right)))
                                else -> throw RedBlackTreeException("RedBlackTree.del/balLeft: invariance violation")
                            }
                        }
                        else -> throw RedBlackTreeException("RedBlackTree.del/balLeft: invariance violation")
                    }
                }
            }
        }   // balLeft

        fun balRight(key: K, value: V, left: RedBlackTree<K, V>, right: RedBlackTree<K, V>): RedBlackTree<K, V> {
            return when(right) {
                is RedTree -> RedTree(key, value, left, right.black)
                else -> {
                    when(left) {
                        is BlackTree -> balance(key, value, left.red, right)
                        is RedTree -> {
                            when(left.right) {
                                is BlackTree -> RedTree(left.right.key, left.right.value, balance(left.key, left.value, subl(left.left), left.right.left), BlackTree(key, value, left.right.right, right))
                                else -> throw RedBlackTreeException("RedBlackTree.del/balRight: invariance violation")
                            }
                        }
                        else -> throw RedBlackTreeException("RedBlackTree.del/balRight: invariance violation")
                    }
                }
            }
        }   // balRight

        fun delLeft(tree: RBTree<K, V>, key: K): RedBlackTree<K, V> =
            when(tree.left) {
                is BlackTree -> balLeft(tree.key, tree.value, del(tree.left, key), tree.right)
                else -> RedTree(tree.key, tree.value, del(tree.left, key), tree.right)
            }

        fun delRight(tree: RBTree<K, V>, key: K): RedBlackTree<K, V> =
            when(tree.right) {
                is BlackTree -> balRight(tree.key, tree.value, tree.left, del(tree.right, key))
                else -> RedTree(tree.key, tree.value, tree.left, del(tree.right, key))
            }

        fun append(left: RedBlackTree<K, V>, right: RedBlackTree<K, V>): RedBlackTree<K, V> {
            return if (left is EmptyTree<K, V>)
                right
            else if (right is EmptyTree<K, V>)
                left
            else if (isRedTree(left) && isRedTree(right)) {
                val redLeft: RedTree<K, V> = left as RedTree<K, V>
                val redRight: RedTree<K, V> = right as RedTree<K, V>
                val bc: RedBlackTree<K, V> = append(redLeft.right, redRight.left)
                if (isRedTree(bc)) {
                    val redBC: RedTree<K, V> = bc as RedTree<K, V>
                    RedTree(redBC.key, redBC.value, RedTree(redLeft.key, redLeft.value, redLeft.left, redBC.left), RedTree(redRight.key, redRight.value, redBC.right, redRight.right))
                } else
                    RedTree(redLeft.key, redLeft.value, redLeft.left, RedTree(redRight.key, redRight.value, bc, redRight.right))
            } else if (isBlackTree(left) && isBlackTree(right)) {
                val blackLeft: BlackTree<K, V> = left as BlackTree<K, V>
                val blackRight: BlackTree<K, V> = right as BlackTree<K, V>
                val bc: RedBlackTree<K, V> = append(blackLeft.right, blackRight.left)
                if (isRedTree(bc)) {
                    val redBC: RedTree<K, V> = bc as RedTree<K, V>
                    RedTree(redBC.key, redBC.value, BlackTree(blackLeft.key, blackLeft.value, blackLeft.left, redBC.left), BlackTree(blackRight.key, blackRight.value, redBC.right, blackRight.right))
                } else
                    balance(blackLeft.key, blackLeft.value, blackLeft.left, BlackTree(blackRight.key, blackRight.value, bc, blackRight.right))
            } else if (isRedTree(right)) {
                val redRight: RedTree<K, V> = right as RedTree<K, V>
                RedTree(redRight.key, redRight.value, append(left, redRight.left), redRight.right)
            } else if (isRedTree(left)) {
                val redLeft: RedTree<K, V> = left as RedTree<K, V>
                RedTree(redLeft.key, redLeft.value, redLeft.left, append(redLeft.right, right))
            } else
                throw RedBlackTreeException("RedBlackTree.del/append: unmatched tree: ${left} and ${right}")
        }   // append

        return if (tree is EmptyTree<K, V>)
            tree
        else {
            val rbTree: RBTree<K, V> = tree as RBTree<K, V>
            val cmp: Int = key.compareTo(rbTree.key)
            if (cmp < 0)
                delLeft(rbTree, key)
            else if (cmp > 0)
                delRight(rbTree, key)
            else
                append(rbTree.left, rbTree.right)
        }
    }   // del



    data class ZResult<K : Comparable<K>, V>(val zipper: List<RBTree<K, V>>, val levelled: Boolean, val leftMost: Boolean, val depth: Int)

    internal fun <K : Comparable<K>, V> compareDepth(left: RedBlackTree<K, V>, right: RedBlackTree<K, V>): ZResult<K, V> {
        fun unzip(zipper: List<RBTree<K, V>>, leftMost: Boolean): List<RBTree<K, V>> {
            val next: RedBlackTree<K, V> = if (leftMost) zipper.head().left else zipper.head().right
            return if (next is EmptyTree<K, V>)
                zipper
            else {
                val rbNext: RBTree<K, V> = next as RBTree<K, V>
                unzip(cons(rbNext, zipper), leftMost)
            }
        }   // unzip

        fun unzipBoth(left: RedBlackTree<K, V>, right: RedBlackTree<K, V>, leftZipper: List<RBTree<K, V>>, rightZipper: List<RBTree<K, V>>, smallerDepth: Int): ZResult<K, V> {
            return if (isBlackTree(left) && isBlackTree(right)) {
                val leftBlack: BlackTree<K, V> = left as BlackTree<K, V>
                val rightBlack: BlackTree<K, V> = right as BlackTree<K, V>
                unzipBoth(leftBlack.right, rightBlack.left, cons(leftBlack, leftZipper), cons(rightBlack, rightZipper), smallerDepth + 1)
            } else if (isRedTree(left) && isRedTree(right)) {
                val redLeft: RedTree<K, V> = left as RedTree<K, V>
                val redRight: RedTree<K, V> = right as RedTree<K, V>
                unzipBoth(redLeft.right, redRight.left, cons(redLeft, leftZipper), cons(redRight, rightZipper), smallerDepth)
            } else if (isRedTree(right)) {
                val redRight: RedTree<K, V> = right as RedTree<K, V>
                unzipBoth(left, redRight.left, leftZipper, cons(redRight, rightZipper), smallerDepth)
            } else if (isRedTree(left)) {
                val redLeft: RedTree<K, V> = left as RedTree<K, V>
                unzipBoth(redLeft.right, right, cons(redLeft, leftZipper), rightZipper, smallerDepth)
            } else if (left is EmptyTree<K, V> && right is EmptyTree<K, V>)
                ZResult(ListF.empty(), true, false, smallerDepth)
            else if (left is EmptyTree<K, V> && isBlackTree(right)) {
                val leftMost: Boolean = true
                val rightBlack: BlackTree<K, V> = right as BlackTree<K, V>
                ZResult(unzip(cons(rightBlack, rightZipper), leftMost), false, leftMost, smallerDepth)
            } else if (isBlackTree(left) && right is EmptyTree<K, V>) {
                val leftMost: Boolean = false
                val leftBlack: BlackTree<K, V> = left as BlackTree<K, V>
                ZResult(unzip(cons(leftBlack, leftZipper), leftMost), false, leftMost, smallerDepth)
            } else
                throw RedBlackTreeException("RedBlackTree.del/unzipBoth: unmatched trees: ${left} and ${right}")
        }   // unzipBoth

        return unzipBoth(left, right, ListF.empty(), ListF.empty(), 0)
    }   // compareDepth



    internal fun <K : Comparable<K>, V> rebalance(tree: RBTree<K, V>, newLeft: RedBlackTree<K, V>, newRight: RedBlackTree<K, V>): RedBlackTree<K, V> {
        fun findDepth(zipper: List<RBTree<K, V>>, depth: Int): List<RBTree<K, V>> {
            return when(zipper) {
                is Nil -> throw RedBlackTreeException("RedBlackTree.rebalance/findDepth: unexpected empty zipper")
                is Cons -> {
                    if (isBlackTree(zipper.head())) {
                        if (depth == 1) zipper else findDepth(zipper.tail(), depth - 1)
                    } else
                        findDepth(zipper.tail(), depth)
                }
            }
        }   // findDepth

        val blackNewLeft: RedBlackTree<K, V> = blacken(newLeft)
        val blackNewRight: RedBlackTree<K, V> = blacken(newRight)
        val (zipper, levelled, leftMost, smallerDepth) = compareDepth(blackNewLeft, blackNewRight)

        return if (levelled)
            BlackTree(tree.key, tree.value, blackNewLeft, blackNewRight)
        else {
            val zipFrom: List<RBTree<K, V>> = findDepth(zipper, smallerDepth)
            val union: RedBlackTree<K, V> = if (leftMost) RedTree(tree.key, tree.value, blackNewLeft, zipFrom.head()) else RedTree(tree.key, tree.value, zipFrom.head(), blackNewRight)
            val zippedTree: RedBlackTree<K, V> = zipFrom.tail().foldLeft(union) { tr: RedBlackTree<K, V>, node: RBTree<K, V> ->
                if (leftMost) balanceLeft(isBlackTree(node), node.key, node.value, tr, node.right) else balanceRight(isBlackTree(node), node.key, node.value, node.left, tr)
            }
            zippedTree
        }
    }   // rebalance



    internal fun <K : Comparable<K>, V> updNth(tree: RedBlackTree<K, V>, idx: Int, key: K, value: V, overwrite: Boolean): RedBlackTree<K, V> {
        return when(tree) {
            is EmptyTree -> RedTree(key, value, EmptyTree(), EmptyTree())
            is RBTree -> {
                val rank: Int = treeCount(tree.left) + 1
                if (idx < rank)
                    balanceLeft(isBlackTree(tree), tree.key, tree.value, updNth(tree.left, idx, key, value, overwrite), tree.right)
                else if (idx > rank)
                    balanceRight(isBlackTree(tree), tree.key, tree.value, tree.left, updNth(tree.right, idx - rank, key, value, overwrite))
                else if (overwrite)
                    makeTree(isBlackTree(tree), key, value, tree.left, tree.right)
                else
                    tree
            }
        }
    }   // updNth



    /**
     * Deliver the tree with the first n elements visited pre-order. If n is zero
     *   or less then deliver the empty tree.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.take(3) = <[Jessie: 22, John: 31, Ken: 25]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.take(2) = <[Jessie: 22, John: 31]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.take(0) = <[]>
     *
     * @param tree              the source tree
     * @param n                 the number of elements to extract
     * @return                  the new tree
     */
    internal fun <K : Comparable<K>, V> take(tree: RedBlackTree<K, V>, n: Int): RedBlackTree<K, V> {
        return if (n <= 0)
            EmptyTree()
        else if (n >= treeCount(tree))
            tree
        else {
            val rbTree: RBTree<K, V> = tree as RBTree<K, V>
            val count: Int = treeCount(rbTree.left)
            if (n <= count)
                take(rbTree.left, n)
            else {
                val newRight: RedBlackTree<K, V> = take(rbTree.right, n - count - 1)
                if (newRight == rbTree.right)
                    tree
                else if (newRight is EmptyTree)
                    updNth(rbTree.left, n, rbTree.key, rbTree.value, false)
                else
                    rebalance(rbTree, rbTree.left, newRight)
            }
        }
    }   // take

    /**
     * Drop the smaller n elements from the tree. If n is negative, then return
     *   the original tree. If n exceeds or equals the number of elements in the
     *   tree, then return an empty tree.
     *
     * @param tree              the tree from which to drop the elements
     * @param n                 the number of elements to drop
     * @return                  the new tree
     */
    internal fun <K : Comparable<K>, V> drop(tree: RedBlackTree<K, V>, n: Int): RedBlackTree<K, V> {
        return if (n <= 0)
            tree
        else if (n >= treeCount(tree))
            EmptyTree()
        else {
            val rbTree: RBTree<K, V> = tree as RBTree<K, V>
            val count: Int = treeCount(rbTree.left)
            if (n > count)
                drop(rbTree.right, n - count - 1)
            else {
                val newLeft: RedBlackTree<K, V> = drop(rbTree.left, n)
                if (newLeft == rbTree.left)
                    tree
                else if (newLeft is EmptyTree)
                    updNth(rbTree.right, n - count - 1, rbTree.key, rbTree.value, false)
                else
                    rebalance(rbTree, newLeft, rbTree.right)
            }
        }
    }   // drop

    /**
     * Deliver the tree with the elements visited pre-order starting at from (inclusive)
     *   to until (exclusive). If until exceeds the size of the tree then all the elements
     *   from to the end are returned. If from is negative then all the elements to until
     *   are included. If until is the same or less than from then null is returned.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.slice(0, 2) = <[Jessie: 22, John: 31]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.slice(1, 2) = <[John: 31]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.slice(1, 0) = null
     *
     * @param tree              the tree from which to take the slice
     * @param from              the start index (inclusive)
     * @param until             the end index (exclusive)
     * @return                  the new tree
     */
    internal fun <K : Comparable<K>, V> slice(tree: RedBlackTree<K, V>, from: Int, until: Int): RedBlackTree<K, V> {
        return if (tree is EmptyTree)
            tree
        else {
            val rbTree: RBTree<K, V> = tree as RBTree<K, V>
            val count: Int = treeCount(rbTree.left)
            if (from > count)
                slice(rbTree.right, from - count - 1, until - count - 1)
            else if (until <= count)
                slice(rbTree.left, from, until)
            else {
                val newLeft: RedBlackTree<K, V> = drop(rbTree.left, from)
                val newRight: RedBlackTree<K, V> = take(rbTree.right, until - count - 1)
                if (newLeft == rbTree.left && newRight == rbTree.right)
                    rbTree
                else if (newLeft is EmptyTree)
                    updNth(newRight, from - count - 1, rbTree.key, rbTree.value, false)
                else if (newRight is EmptyTree)
                    updNth(newLeft, until, rbTree.key, rbTree.value, false)
                else
                    rebalance(rbTree, newLeft, newRight)
            }
        }
    }   // slice

    /**
     * Deliver the (sub-) tree starting at or after the given key. If the given key
     *   is later than the final element then empty tree is returned.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.from("John") = <[John: 31, Ken: 25]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.from("Kath") = <[Ken: 25]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.from("Stuart") = <[]>
     *
     * @param tree              the source tree
     * @param fromKey           the start key
     * @return                  the new sub-tree
     */
    internal fun <K : Comparable<K>, V> from(tree: RedBlackTree<K, V>, fromKey: K): RedBlackTree<K, V> {
        return when(tree) {
            is EmptyTree -> tree
            is RBTree -> {
                if (tree.key.compareTo(fromKey) < 0)
                    from(tree.right, fromKey)
                else {
                    val newLeft: RedBlackTree<K, V> = from(tree.left, fromKey)
                    if (newLeft == tree.left)
                        tree
                    else if (newLeft is EmptyTree)
                        upd(tree.right, tree.key, tree.value, false)
                    else
                        rebalance(tree, newLeft, tree.right)
                }
            }
        }
    }   // from

    /**
     * Deliver the (sub-) tree extending as far as and including the given key.
     *   If the given key is before the first element then the empty tree is returned.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.to("John") = <[Jessie: 22, John: 31]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.to("Kath") = <[Jessie: 22, John: 31]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.to("Dave") = <[]>
     *
     * @param tree              the source tree
     * @param toKey             the end key (inclusive)
     * @return                  the new tree
     */
    internal fun <K : Comparable<K>, V> to(tree: RedBlackTree<K, V>, toKey: K): RedBlackTree<K, V> {
        return when(tree) {
            is EmptyTree -> tree
            is RBTree -> {
                if (toKey.compareTo(tree.key) < 0)
                    to(tree.left, toKey)
                else {
                    val newRight: RedBlackTree<K, V> = to(tree.right, toKey)
                    if (newRight == tree.right)
                        tree
                    else if (newRight is EmptyTree)
                        upd(tree.left, tree.key, tree.value, false)
                    else
                        rebalance(tree, tree.left, newRight)
                }
            }
        }
    }   // to

    /**
     * Deliver the (sub-) tree extending as far as but not including the given key.
     *   If the given key is before or the same as the first element then the empty tree is returned.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.until("Ken") = <[Jessie: 22, John: 31]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.until("Jessie") = <[]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.until("Dave") = <[]>
     *
     * @praam tree              the source tree
     * @param untilKey          the end key (exclusive)
     * @return                  the new tree
     */
    internal fun <K : Comparable<K>, V> until(tree: RedBlackTree<K, V>, untilKey: K): RedBlackTree<K, V> {
        return when(tree) {
            is EmptyTree -> tree
            is RBTree -> {
                if (untilKey.compareTo(tree.key) <= 0)
                    until(tree.left, untilKey)
                else {
                    val newRight: RedBlackTree<K, V> = until(tree.right, untilKey)
                    if (newRight == tree.right)
                        tree
                    else if (newRight is EmptyTree)
                        upd(tree.left, tree.key, tree.value, false)
                    else
                        rebalance(tree, tree.left, newRight)
                }
            }
        }
    }   // until

    /**
     * Deliver the (sub-) tree extending from to as far as but not including the until key.
     *   If the given until key is before or the same as the from key then the empty tree is returned.
     *
     * @param tree              the tree from which to extract entries
     * @param fromKey           the smaller key from which to start extraction
     * @param untilKey          the larger key in which to stop extraction (exclusive)
     * @return                  new tree
     */
    internal fun <K : Comparable<K>, V> range(tree: RedBlackTree<K, V>, fromKey: K, untilKey: K): RedBlackTree<K, V> {
        return when(tree) {
            is EmptyTree -> tree
            is RBTree -> {
                if (tree.key.compareTo(fromKey) < 0)
                    range(tree.right, fromKey, untilKey)
                else if (untilKey.compareTo(tree.key) <= 0)
                    range(tree.left, fromKey, untilKey)
                else {
                    val newLeft: RedBlackTree<K, V> = from(tree.left, fromKey)
                    val newRight: RedBlackTree<K, V> = until(tree.right, untilKey)
                    if (newLeft == tree.left && newRight == tree.right)
                        tree
                    else if (newLeft is EmptyTree)
                        upd(newRight, tree.key, tree.value, false)
                    else if (newRight is EmptyTree)
                        upd(newLeft, tree.key, tree.value, false)
                    else
                        rebalance(tree, newLeft, newRight)
                }
            }
        }
    }   // range

}   // RedBlackTreeF
