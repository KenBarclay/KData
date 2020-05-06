package com.adt.kotlin.data.immutable.map

/**
 * A class hierarchy defining an immutable map collection. The algebraic data
 *   type declaration is:
 *
 * datatype Map[K, V] = Tip
 *                    | Bin of K * V * Map[K, V] * Map[K, V]
 *
 * Maps are implemented as size balanced binary trees. This implementation
 *   mirrors the Haskell implementation in Data.Map that, in turn, is based
 *   on an efficient balanced binary tree referenced in the sources.
 *
 * This code duplicates much of the implementation for the immutable Set.
 *   Both are based on sized balanced binary trees and a generic Tree should
 *   be developed for both.
 *
 * The type denoted by Map[K, V] is a map of key/value pairs.
 *
 * The Map class is defined generically in terms of the type parameters K and V.
 *
 * @param K                     the type of keys in the map
 * @param V                     the type of values in the map
 *
 * @author	                    Ken Barclay
 * @since                       October 2019
 */

import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.ListF
import com.adt.kotlin.data.immutable.list.append
import com.adt.kotlin.data.immutable.map.Map.Tip
import com.adt.kotlin.data.immutable.map.Map.Bin

import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.OptionF.none
import com.adt.kotlin.data.immutable.option.OptionF.some

import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3

import kotlin.collections.List as KList


object MapF {

    /**
     * Factory binding/function to create the base instances.
     */
    val tip: Tip = Tip
    fun <K : Comparable<K>, V> bin(key: K, value: V, left: Map<K, V>, right: Map<K, V>): Map<K, V> = Bin(1 + left.size + right.size, key, value, left, right)

    /**
     * Create an empty map.
     *
     * @return    		            an empty map
     */
    fun <K : Comparable<K>, V> empty(): Map<K, V> {
        @Suppress("UNCHECKED_CAST") val emptyMap: Map<K, V> = Tip as Map<K, V>
        return emptyMap
    }

    fun <K: Comparable<K>, V> of(): Map<K, V> = empty()

    fun <K: Comparable<K>, V> of(k1: K, v1: V): Map<K, V> = empty<K, V>().insert(k1, v1)

    fun <K: Comparable<K>, V> of(k1: K, v1: V, k2: K, v2: V): Map<K, V> = empty<K, V>().insert(k1, v1).insert(k2, v2)

    fun <K: Comparable<K>, V> of(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V): Map<K, V> = empty<K, V>().insert(k1, v1).insert(k2, v2).insert(k3, v3)

    fun <K: Comparable<K>, V> of(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V): Map<K, V> =
        empty<K, V>().insert(k1, v1).insert(k2, v2).insert(k3, v3).insert(k4, v4)

    fun <K: Comparable<K>, V> of(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V, k5: K, v5: V): Map<K, V> =
        empty<K, V>().insert(k1, v1).insert(k2, v2).insert(k3, v3).insert(k4, v4).insert(k5, v5)



    fun <K: Comparable<K>, V> of(e1: Pair<K, V>): Map<K, V> = empty<K, V>().insert(e1)

    fun <K: Comparable<K>, V> of(e1: Pair<K, V>, e2: Pair<K, V>): Map<K, V> = empty<K, V>().insert(e1).insert(e2)

    fun <K: Comparable<K>, V> of(e1: Pair<K, V>, e2: Pair<K, V>, e3: Pair<K, V>): Map<K, V> = empty<K, V>().insert(e1).insert(e2).insert(e3)

    fun <K: Comparable<K>, V> of(e1: Pair<K, V>, e2: Pair<K, V>, e3: Pair<K, V>, e4: Pair<K, V>): Map<K, V> =
        empty<K, V>().insert(e1).insert(e2).insert(e3).insert(e4)

    fun <K: Comparable<K>, V> of(e1: Pair<K, V>, e2: Pair<K, V>, e3: Pair<K, V>, e4: Pair<K, V>, e5: Pair<K, V>): Map<K, V> =
        empty<K, V>().insert(e1).insert(e2).insert(e3).insert(e4).insert(e5)

    fun <K: Comparable<K>, V> of(vararg seq: Pair<K, V>): Map<K, V> = fromSequence(*seq)



    /**
     * A map with a single element.
     */
    fun <K : Comparable<K>, V> singleton(key: K, value: V): Map<K, V> = Bin(1, key, value, empty(), empty())

    /**
     * Convert a variable-length parameter series into a map.
     *
     * @param seq                   variable-length parameter series
     * @return                      map of the given values
     */
    fun <K : Comparable<K>, V> fromSequence(vararg seq: Pair<K, V>): Map<K, V> =
        seq.fold(empty()){map, pair -> map.insert(pair.first, pair.second)}

    /**
     *  Build a map from a list of key/value pairs with a combining function.
     *
     * @param f			combining function over the values
     * @param seq   	series of key/value pairs
     * @return			the required map
     */
    fun <K : Comparable<K>, V> fromSequenceWith(f: (V) -> (V) -> V, vararg seq: Pair<K, V>): Map<K, V> =
        fromSequenceWithKey({_ -> {v1 -> {v2 -> f(v1)(v2)}}}, *seq)

    fun <K : Comparable<K>, V> fromSequenceWith(f: (V, V) -> V, vararg seq: Pair<K, V>): Map<K, V> = fromSequenceWith(C2(f), *seq)

    /**
     *  Build a map from a list of key/value pairs with a combining function.
     *
     * @param f			combining function
     * @param seq		series of key/value pairs
     * @return			the required map
     */
    fun <K : Comparable<K>, V> fromSequenceWithKey(f: (K) -> (V) -> (V) -> V, vararg seq: Pair<K, V>): Map<K, V> {
        val insert: (Map<K, V>) -> (Pair<K, V>) -> Map<K, V> = {map -> {pair -> map.insertWithKey(pair.first, pair.second, f)}}
        var pairs: com.adt.kotlin.data.immutable.list.List<Pair<K, V>> = ListF.empty<Pair<K, V>>()
        for (pair: Pair<K, V> in seq) {
            pairs = ListF.cons(pair, pairs)
        }
        return pairs.foldLeft(empty<K, V>(), insert)
    }

    fun <K : Comparable<K>, V> fromSequenceWithKey(f: (K, V, V) -> V, vararg seq: Pair<K, V>): Map<K, V> = fromSequenceWithKey(C3(f), *seq)

    /**
     * Convert a variable-length list into a map.
     *
     * @param xs                    variable-length list
     * @return                      map of the given values
     */
    fun <K : Comparable<K>, V> fromKList(xs: List<Pair<K, V>>): Map<K, V> =
        xs.fold(empty()){map, pair -> map.insert(pair.first, pair.second)}

    /**
     * Convert a variable-length list into a map.
     *
     * @param xs                    variable-length list
     * @return                      map of the given values
     */
    fun <K : Comparable<K>, V> fromList(xs: com.adt.kotlin.data.immutable.list.List<Pair<K, V>>): Map<K, V> =
        xs.foldLeft(empty<K, V>()){map -> {pair -> map.insert(pair.first, pair.second)}}

    /**
     *  Build a map from a list of key/value pairs with a combining function.
     *
     * @param xs		list of key/value pairs
     * @param f			curried combining function
     * @return			the required map
     */
    fun <K : Comparable<K>, V> fromListWith(xs: com.adt.kotlin.data.immutable.list.List<Pair<K, V>>, f: (V) -> (V) -> V): Map<K, V> =
        fromListWithKey(xs){_ -> {v1 -> {v2 -> f(v1)(v2)}}}

    /**
     *  Build a map from a list of key/value pairs with a combining function.
     *
     * @param xs		list of key/value pairs
     * @param f			combining function
     * @return			the required map
     */
    fun <K : Comparable<K>, V> fromListWith(xs: com.adt.kotlin.data.immutable.list.List<Pair<K, V>>, f: (V, V) -> V): Map<K, V> = fromListWith(xs, C2(f))

    /**
     *  Build a map from a list of key/value pairs with a combining function.
     *
     * @param xs		list of key/value pairs
     * @param f			curried combining function
     * @return			the required map
     */
    fun <K : Comparable<K>, V> fromListWithKey(xs: com.adt.kotlin.data.immutable.list.List<Pair<K, V>>, f: (K) -> (V) -> (V) -> V): Map<K, V> {
        val insert: (Map<K, V>) -> (Pair<K, V>) -> Map<K, V> = {map -> {pair -> map.insertWithKey(pair.first, pair.second, f)}}
        return xs.foldLeft(empty<K, V>(), insert)
    }

    /**
     *  Build a map from a list of key/value pairs with a combining function.
     *
     * @param xs		list of key/value pairs
     * @param f			combining function
     * @return			the required map
     */
    fun <K : Comparable<K>, V> fromListWithKey(xs: com.adt.kotlin.data.immutable.list.List<Pair<K, V>>, f: (K, V, V) -> V): Map<K, V> = fromListWithKey(xs, C3(f))

    /**
     * Convert a map into an array list of key/value pairs.
     *
     * @param map                   the map to convert
     * @return                      the array list of key/value pairs
     */
    fun <K : Comparable<K>, V> toKList(map: Map<K, V>): KList<Pair<K, V>> {
        fun <K : Comparable<K>, V> recToList(map: Map<K, V>, acc: KList<Pair<K, V>>): KList<Pair<K, V>> {
            return when(map) {
                is Tip -> acc
                is Bin -> {
                    val leftMap: KList<Pair<K, V>> = recToList(map.left, acc)
                    val rightMap: KList<Pair<K, V>> = recToList(map.right, leftMap + Pair(map.key, map.value))
                    rightMap
                }
            }
        }

        return recToList(map, arrayListOf<Pair<K, V>>())
    }   // toKList

    /**
     * Convert a map into a list of key/value pairs.
     *
     * @param map                   the map to convert
     * @return                      the list of key/value pairs
     */
    fun <K : Comparable<K>, V> toList(map: Map<K, V>): com.adt.kotlin.data.immutable.list.List<Pair<K, V>> {
        fun recToKList(map: Map<K, V>): com.adt.kotlin.data.immutable.list.List<Pair<K, V>> {
            return when(map) {
                is Tip -> ListF.empty()
                is Bin -> {
                    val front: com.adt.kotlin.data.immutable.list.List<Pair<K, V>> = recToKList(map.left)
                    val rear: com.adt.kotlin.data.immutable.list.List<Pair<K, V>> = recToKList(map.right)
                    val middle: Pair<K, V> = Pair(map.key, map.value)
                    front.append(middle).append(rear)
                }
            }
        }   // recToKList

        return recToKList(map)
    }   // toList

    /**
     * Show the map. The map is presented in
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
    fun <K : Comparable<K>, V> drawMap(map: Map<K, V>): String =
        drawMap(ListF.empty(), map)

    internal fun <K : Comparable<K>, V> drawMap(bars: List<String>, map: Map<K, V>): String {
        return if (map.isEmpty())
            "${drawBars(bars)}|\n"
        else {
            @Suppress("UNCHECKED_CAST") val bin: Bin<K, V> = map as Bin<K, V>
            if (bin.left.isEmpty() && bin.right.isEmpty())
                "${drawBars(bars)}${bin.key}: ${bin.value}\n"
            else {
                val text5: String = drawMap(withEmpty(bars), bin.right)
                val text4: String = drawWide(bars)
                val text3: String = drawMap(withBar(bars), bin.left)
                val text2: String = drawWide(bars)
                val text1: String = "${drawBars(bars)}${bin.key}: ${bin.value}\n"
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



    // Functor extension functions:

    fun <K : Comparable<K>, V, W> lift(f: (V) -> W): (Map<K, V>) -> Map<K, W> =
        {map: Map<K, V> ->
            map.fmap(f)
        }   // lift



// ---------- implementation ------------------------------

    private val DELTA: Int = 4
    private val RATIO: Int = 2

    /**
     * Balance two maps with the key and value. The sizes of the trees should balance
     *   after decreasing the size of one of them (a rotation).
     */
    internal fun <K : Comparable<K>, V> balance(key: K, value: V, left: Map<K, V>, right: Map<K, V>): Map<K, V> {
        return if(left.size + right.size <= 1)
            bin(key, value, left, right)
        else if (right.size >= DELTA * left.size)
            rotateLeft(key, value, left, right)
        else if (left.size >= DELTA * right.size)
            rotateRight(key, value, left, right)
        else
            bin(key, value, left, right)
    }   // balance

    internal fun <K : Comparable<K>, V> balanceLeft(key: K, value: V, left: Map<K, V>, right: Map<K, V>): Map<K, V> {
        return when(right) {
            is Tip -> when(left) {
                is Tip -> bin(key, value, empty(), empty())
                is Bin -> {
                    val leftleft: Map<K, V> = left.left
                    val leftright: Map<K, V> = left.right
                    when(leftleft) {
                        is Tip -> when(leftright) {
                            is Tip -> bin(key, value, left, empty())
                            is Bin -> bin(leftright.key, leftright.value, bin(left.key, left.value, empty(), empty()), bin(key, value, empty(), empty()))
                        }
                        is Bin -> when(leftright) {
                            is Tip -> bin(left.key, left.value, leftleft, bin(key, value, empty(), empty()))
                            is Bin -> if(leftright.size < RATIO * leftleft.size)
                                bin(left.key, left.value, leftleft, bin(key, value, leftright, empty()))
                            else
                                bin(leftright.key, leftright.value, bin(left.key, left.value, leftleft, leftright.left), bin(key, value, leftright.right, empty()))
                        }
                    }
                }
            }
            is Bin -> when(left) {
                is Tip -> bin(key, value, empty(), right)
                is Bin -> if (left.size > DELTA * right.size) {
                    val leftleft: Map<K, V> = left.left
                    val leftright: Map<K, V> = left.right
                    if(!leftleft.isEmpty() && !leftright.isEmpty()) {
                        if (leftright.size < RATIO * leftleft.size)
                            bin(left.key, left.value, leftleft, bin(key, value, leftright, right))
                        else {
                            val leftRightBin: Bin<K, V> = leftright as Bin<K, V>
                            bin(leftRightBin.key, leftRightBin.value, bin(left.key, left.value, leftleft, leftright.left), bin(key, value, leftright.right, right))
                        }
                    } else
                        throw MapException("balanceLeft: failure")
                } else
                    bin(key, value, left, right)
            }
        }
    }   // balanceLeft

    internal fun <K : Comparable<K>, V> balanceRight(key: K, value: V, left: Map<K, V>, right: Map<K, V>): Map<K, V> {
        return when(left) {
            is Tip -> when(right) {
                is Tip -> bin(key, value, empty(), empty())
                is Bin -> {
                    val rightleft: Map<K, V> = right.left
                    val rightright: Map<K, V> = right.right
                    when(rightleft) {
                        is Tip -> when(rightright) {
                            is Tip -> bin(key, value, empty(), right)
                            is Bin -> bin(right.key, right.value, bin(key, value, empty(), empty()), rightright)
                        }
                        is Bin -> when(rightright) {
                            is Tip -> bin(rightleft.key, rightleft.value, bin(key, value, empty(), empty()), bin(right.key, right.value, empty(), empty()))
                            is Bin -> if (rightleft.size < RATIO * rightright.size)
                                bin(right.key, right.value, bin(key, value, empty(), rightleft.left), rightright)
                            else
                                bin(rightleft.key, rightleft.value, bin(key, value, empty(), rightleft.left), bin(right.key, right.value, rightleft.right, rightright))
                        }
                    }
                }
            }
            is Bin -> when(right) {
                is Tip -> bin(key, value, left, empty())
                is Bin -> if (right.size > DELTA * left.size) {
                    val rightleft: Map<K, V> = right.left
                    val rightright: Map<K, V> = right.right
                    if (!rightleft.isEmpty() && !rightright.isEmpty()) {
                        if (rightleft.size < RATIO * rightright.size)
                            bin(right.key, right.value, bin(key, value, left, rightleft), rightright)
                        else {
                            val rightleftBin: Bin<K, V> = rightleft as Bin<K, V>
                            bin(rightleftBin.key, rightleftBin.value, bin(key, value, left, rightleftBin.left), bin(right.key, right.value, rightleftBin.right, rightright))
                        }
                    } else
                        throw MapException("balanceRight: failure")
                } else
                    bin(key, value, left, right)
            }
        }
    }   // balanceRight

    internal fun <K : Comparable<K>, V> rotateLeft(key: K, value: V, left: Map<K, V>, right: Map<K, V>): Map<K, V> {
        return when(right) {
            is Tip -> throw MapException("rotateLeft: right is Tip")
            is Bin -> if (right.left.size < RATIO * right.right.size)
                singleLeft(key, value, left, right)
            else
                doubleLeft(key, value, left, right)
        }
    }   // rotateLeft

    internal fun <K : Comparable<K>, V> rotateRight(key: K, value: V, left: Map<K, V>, right: Map<K, V>): Map<K, V> {
        return when(left) {
            is Tip -> throw MapException("rotateRight: left is Tip")
            is Bin -> if (left.right.size < RATIO * left.left.size)
                singleRight(key, value, left, right)
            else
                doubleRight(key, value, left, right)
        }
    }   // rotateRight

    internal fun <K : Comparable<K>, V> singleLeft(key: K, value: V, left: Map<K, V>, right: Map<K, V>): Map<K, V> {
        return when(right) {
            is Tip -> throw MapException("singleLeft: right is Tip")
            is Bin -> bin(right.key, right.value, bin(key, value, left, right.left), right.right)
        }
    }   // singleLeft

    internal fun <K : Comparable<K>, V> singleRight(key: K, value: V, left: Map<K, V>, right: Map<K, V>): Map<K, V> {
        return when(left) {
            is Tip -> throw MapException("singleRight: left is Tip")
            is Bin -> bin(left.key, left.value, left.left, bin(key, value, left.right, right))
        }
    }   // singleRight

    internal fun <K : Comparable<K>, V> doubleLeft(key: K, value: V, left: Map<K, V>, right: Map<K, V>): Map<K, V> {
        return when(right) {
            is Tip -> throw MapException("doubleLeft: right is Tip")
            is Bin -> {
                val rightleft: Map<K, V> = right.left
                when(rightleft) {
                    is Tip -> throw MapException("doubleLeft: right.left is Tip")
                    is Bin -> bin(rightleft.key, rightleft.value, bin(key, value, left, rightleft.left), bin(right.key, right.value, rightleft.right, right.right))
                }
            }
        }
    }   // doubleLeft

    internal fun <K : Comparable<K>, V> doubleRight(key: K, value: V, left: Map<K, V>, right: Map<K, V>): Map<K, V> {
        return when(left) {
            is Tip -> throw MapException("doubleRight: left is Tip")
            is Bin -> {
                val leftright: Map<K, V> = left.right
                when(leftright) {
                    is Tip -> throw MapException("doubleRight: left.right is Tip")
                    is Bin -> bin(leftright.key, leftright.value, bin(left.key, left.value, left.left, leftright.left), bin(key, value, leftright.right, right))
                }
            }
        }
    }   // doubleRight

    internal fun <K : Comparable<K>, V> glue(left: Map<K, V>, right: Map<K, V>): Map<K, V> {
        return when(left) {
            is Tip -> right
            is Bin -> when(right) {
                is Tip -> left
                is Bin -> if (left.size > right.size) {
                    val dfm: Triple<K, V, Map<K, V>> = deleteFindMax(left)
                    balance(dfm.first, dfm.second, dfm.third, right)
                } else {
                    val dfm: Triple<K, V, Map<K, V>> = deleteFindMin(right)
                    balance(dfm.first, dfm.second, left, dfm.third)
                }
            }
        }
    }   // glue

    internal fun <K : Comparable<K>, V> deleteFindMin(map: Map<K, V>): Triple<K, V, Map<K, V>> {
        return when(map) {
            is Tip -> throw MapException("deleteFindMin: empty tree")
            is Bin -> if (map.left.isEmpty())
                Triple(map.key, map.value, map.right)
            else {
                val dfm: Triple<K, V, Map<K, V>> = deleteFindMin(map.left)
                Triple(dfm.first, dfm.second, balance(map.key, map.value, dfm.third, map.right))
            }
        }
    }   // deleteFindMin

    internal fun <K : Comparable<K>, V> deleteFindMax(map: Map<K, V>): Triple<K, V, Map<K, V>> {
        return when(map) {
            is Tip -> throw MapException("deleteFindMax: empty tree")
            is Bin -> if (map.right.isEmpty())
                Triple(map.key, map.value, map.left)
            else {
                val dfm: Triple<K, V, Map<K, V>> = deleteFindMax(map.right)
                Triple(dfm.first, dfm.second, balance(map.key, map.value, map.left, dfm.third))
            }
        }
    }   // deleteFindMax

    internal fun <K : Comparable<K>, V> join(key: K, value: V, left: Map<K, V>, right: Map<K, V>): Map<K, V> {
        return when(left) {
            is Tip -> insertMin(key, value, right)
            is Bin -> when(right) {
                is Tip -> insertMax(key, value, left)
                is Bin -> if (DELTA * left.size <= right.size)
                    balance(right.key, right.value, join(key, value, left, right.left), right.right)
                else if (DELTA * right.size <= left.size)
                    balance(left.key, left.value, left.left, join(key, value, left.right, right))
                else
                    bin(key, value, left, right)
            }
        }
    }   // join

    internal fun <K : Comparable<K>, V> insertMax(key: K, value: V, map: Map<K, V>): Map<K, V> {
        return when(map) {
            is Tip -> singleton(key, value)
            is Bin -> balance(map.key, map.value, map.left, insertMax(key, value, map.right))
        }
    }   // insertMax

    internal fun <K : Comparable<K>, V> insertMin(key: K, value: V, map: Map<K, V>): Map<K, V> {
        return when(map) {
            is Tip -> singleton(key, value)
            is Bin -> balance(map.key, map.value, insertMin(key, value, map.left), map.right)
        }
    }   // insertMin

    internal fun <K : Comparable<K>, V> merge(left: Map<K, V>, right: Map<K, V>): Map<K, V> {
        return when(left) {
            is Tip -> right
            is Bin -> when(right) {
                is Tip -> left
                is Bin -> if (DELTA * left.size <= right.size)
                    balance(right.key, right.value, merge(left, right.left), right.right)
                else if (DELTA * right.size <= left.size)
                    balance(left.key, left.value, left.left, merge(left.right, right))
                else
                    glue(left, right)
            }
        }
    }   // merge

    internal fun <K : Comparable<K>, V> hedgeUnion(cmpLo: (K) -> Int, cmpHi: (K) -> Int, left: Map<K, V>, right: Map<K, V>): Map<K, V> {
        return when(right) {
            is Tip -> left
            is Bin -> when(left) {
                is Tip -> join(right.key, right.value, filterGT(cmpLo, right.left), filterLT(cmpHi, right.right))
                is Bin -> {
                    val cmpT: (K) -> Int = {k -> left.key.compareTo(k)}
                    join(left.key, left.value, hedgeUnion(cmpLo, cmpT, left.left, trim(cmpLo, cmpT, right)), hedgeUnion(cmpT, cmpHi, left.right, trim(cmpT, cmpHi, right)))
                }
            }
        }
    }   // hedgeUnion

    internal fun <K : Comparable<K>, V> hedgeDifference(cmpLo: (K) -> Int, cmpHi: (K) -> Int, left: Map<K, V>, right: Map<K, V>): Map<K, V> {
        return when(left) {
            is Tip -> empty()
            is Bin -> when(right) {
                is Tip -> join(left.key, left.value, filterGT(cmpLo, left.left), filterLT(cmpHi, left.right))
                is Bin -> {
                    val cmpT: (K) -> Int = {k -> right.key.compareTo(k)}
                    merge(hedgeDifference(cmpLo, cmpT, trim(cmpLo, cmpT, left), right.left), hedgeDifference(cmpT, cmpHi, trim(cmpT, cmpHi, left), right.right))
                }
            }
        }
    }   // hedgeDifference

    internal fun <K : Comparable<K>, V> filterGT(cmp: (K) -> Int, map: Map<K, V>): Map<K, V> {
        return when(map) {
            is Tip -> empty()
            is Bin -> if (cmp(map.key) < 0)
                join(map.key, map.value, filterGT(cmp, map.left), map.right)
            else if (cmp(map.key) > 0)
                filterGT(cmp, map.right)
            else
                map.right
        }
    }   // filterGT

    internal fun <K : Comparable<K>, V> filterLT(cmp: (K) -> Int, map: Map<K, V>): Map<K, V> {
        return when(map) {
            is Tip -> empty()
            is Bin -> if (cmp(map.key) < 0)
                filterLT(cmp, map.left)
            else if (cmp(map.key) > 0)
                join(map.key, map.value, map.left, filterLT(cmp, map.right))
            else
                map.left
        }
    }   // filterLT

    internal fun <K : Comparable<K>, V> trim(cmpLo: (K) -> Int, cmpHi: (K) -> Int, map: Map<K, V>): Map<K, V> {
        return when(map) {
            is Tip -> empty()
            is Bin -> if (cmpLo(map.key) < 0) {
                if(cmpHi(map.key) > 0)
                    map
                else
                    trim(cmpLo, cmpHi, map.left)
            } else
                trim(cmpLo, cmpHi, map.right)
        }
    }   // trim

    internal fun <K : Comparable<K>, V> splitLookup(key: K, value: V, map: Map<K, V>): Triple<Map<K, V>, Option<Pair<K, V>>, Map<K, V>> {
        return when(map) {
            is Tip -> Triple(empty(), none(), empty())
            is Bin -> if(key.compareTo(map.key) < 0) {
                val split: Triple<Map<K, V>, Option<Pair<K, V>>, Map<K, V>> = splitLookup(key, value, map.left)
                Triple(split.first, split.second, join(map.key, map.value, split.third, map.right))
            } else if (key.compareTo(map.key) > 0) {
                val split: Triple<Map<K, V>, Option<Pair<K, V>>, Map<K, V>> = splitLookup(key, value, map.right)
                Triple(join(map.key, map.value, map.left, split.first), split.second, split.third)

            } else
                Triple(map.left, some(Pair(map.key, map.value)), map.right)
        }
    }   // splitLookup

    internal fun <K : Comparable<K>, V> splitMember(key: K, value: V, map: Map<K, V>): Triple<Map<K, V>, Boolean, Map<K, V>> {
        val split: Triple<Map<K, V>, Option<Pair<K, V>>, Map<K, V>> = splitLookup(key, value, map)
        return Triple(split.first, split.second.fold({false}, {true}), split.third)
    }   // splitMember

}   // MapF
