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

import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.OptionF
import com.adt.kotlin.data.immutable.option.OptionF.none
import com.adt.kotlin.data.immutable.option.OptionF.some

import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3



sealed class Map<K : Comparable<K>, out V>(val size: Int) {



    object Tip : Map<Nothing, Nothing>(0)



    class Bin<K : Comparable<K>, out V> internal constructor(size: Int, val key: K, val value: V, val left: Map<K, V>, val right: Map<K, V>) : Map<K, V>(size)



    /**
     * Determine if this map contains the key determined by the predicate.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.contains{name -> (name == John)} = true
     *   {Jessie: 22, John: 31, Ken: 25}.contains{name -> (name == Irene)} = false
     *   {}.contains{name -> (name == John)} = false
     *
     * @param predicate         search predicate
     * @return                  true if search element is present, false otherwise
     */
    fun contains(predicate: (K) -> Boolean): Boolean {
        fun recContains(predicate: (K) -> Boolean, map: Map<K, V>): Boolean {
            return when (map) {
                is Tip -> false
                is Bin -> {
                    if (predicate(map.key))
                        true
                    else if (recContains(predicate, map.left))
                        true
                    else
                        recContains(predicate, map.right)
                }
            }
        }   // recContains

        return recContains(predicate, this)
    }   // contains

    /**
     * Determine if the map contains the given key.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.contains("Ken") = true
     *   {Jessie: 22, John: 31, Ken: 25}.contains("Irene") = false
     *   {}.contains("Ken") = false
     *
     * @param key               search key
     * @return                  true if the map contains this key
     */
    fun contains(key: K): Boolean {
        tailrec
        fun recContains(key: K, map: Map<K, V>): Boolean {
            return when(map) {
                is Tip -> false
                is Bin -> {
                    if (key.compareTo(map.key) < 0)
                        recContains(key, map.left)
                    else if (key.compareTo(map.key) > 0)
                        recContains(key, map.right)
                    else
                        true
                }
            }
        }

        return recContains(key, this)
    }   // containsKey

    /**
     * Delete the value from the map. When the value is not a member
     *   of the map, the original map is returned.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.delete(Ken) = {Jessie: 22, John: 31}
     *   {Jessie: 22, John: 31, Ken: 25}.delete(Irene) = {Jessie: 22, John: 31, Ken: 25}
     *   {}.delete(Ken) = {}
     *
     * @param key               key to be removed
     * @return                  new map without the given key
     */
    fun delete(key: K): Map<K, V> {
        fun recDelete(key: K, map: Map<K, V>): Map<K, V> {
            return when(map) {
                is Tip -> MapF.empty()
                is Bin -> {
                    if (key.compareTo(map.key) < 0)
                        MapF.balance(map.key, map.value, recDelete(key, map.left), map.right)
                    else if (key.compareTo(map.key) > 0)
                        MapF.balance(map.key, map.value, map.left, recDelete(key, map.right))
                    else
                        MapF.glue(map.left, map.right)
                }
            }
        }   // recDelete

        return recDelete(key, this)
    }   // delete

    /**
     * Are two maps equal?
     *
     * @param other             the other map
     * @return                  true if both maps are the same; false otherwise
     */
    override fun equals(other: Any?): Boolean {
        return if (this === other)
            true
        else if (other == null || this::class.java != other::class.java)
            false
        else {
            @Suppress("UNCHECKED_CAST") val otherMap: Map<K, V> = other as Map<K, V>
            (this.size == otherMap.size) && (this.toAscendingList() == otherMap.toAscendingList())
        }
    }   // equals

    /**
     * Filter all values that satisfy the predicate.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.filter{v -> (v % 2 == 0)} = {Jessie: 22}
     *   {}.filter{v -> (v % 2 == 0)} = {}
     *
     * @param predicate     	predicate function on the value types
     * @return              	map of selected elements
     */
    fun filter(predicate: (V) -> Boolean): Map<K, V> {
        fun recFilter(predicate: (V) -> Boolean, map: Map<K, V>): Map<K, V> {
            return when(map) {
                is Tip -> MapF.empty()
                is Bin -> {
                    if (predicate(map.value))
                        MapF.join(map.key, map.value, recFilter(predicate, map.left), recFilter(predicate, map.right))
                    else
                        MapF.merge(recFilter(predicate, map.left), recFilter(predicate, map.right))
                }
            }
        }

        return recFilter(predicate, this)
    }   // filter

    /**
     * Filter all key/values that satisfy the predicate.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.filterWithKey{name -> {age -> name.startsWith(J) && age > 30}} = {John: 31}
     *
     * @param predicate     	curried predicate function on the key and value types
     * @return              	map of selected elements
     */
    fun filterWithKey(predicate: (K) -> (V) -> Boolean): Map<K, V> {
        fun recFilterWithKey(predicate: (K) -> (V) -> Boolean, map: Map<K, V>): Map<K, V> {
            return when(map) {
                is Tip -> MapF.empty()
                is Bin -> {
                    if (predicate(map.key)(map.value))
                        MapF.join(map.key, map.value, recFilterWithKey(predicate, map.left), recFilterWithKey(predicate, map.right))
                    else
                        MapF.merge(recFilterWithKey(predicate, map.left), recFilterWithKey(predicate, map.right))
                }
            }
        }

        return recFilterWithKey(predicate, this)
    }   // filterWithKey

    fun filterWithKey(predicate: (K, V) -> Boolean): Map<K, V> = this.filterWithKey(C2(predicate))

    /**
     * The find function takes a predicate and returns the first key in
     *   the map matching the predicate, or none if there is no
     *   such element.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.find{name -> name.startsWith(J)} = some(John)
     *   {Jessie: 22, John: 31, Ken: 25}.find{name -> name.charAt(0) >= A} = some(John)
     *   {Jessie: 22, John: 31, Ken: 25}.find{name -> name.charAt(0) >= Z} = none
     *   {}.find{name -> name.startsWith(J)} = none
     *
     * @param predicate         criteria
     * @return                  matching element, if found
     */
    fun find(predicate: (K) -> Boolean): Option<K> {
        fun recFind(predicate: (K) -> Boolean, map: Map<K, V>): Option<K> {
            return when (map) {
                is Tip -> none()
                is Bin -> {
                    if (predicate(map.key))
                        some(map.key)
                    else {
                        val leftFind: Option<K> = recFind(predicate, map.left)
                        if (leftFind.isDefined())
                            leftFind
                        else
                            recFind(predicate, map.right)
                    }
                }
            }
        }   // recFind

        return recFind(predicate, this)
    }   // find

    /**
     * foldLeft is a higher-order function that folds a left associative binary
     *   function into the values of a map.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.foldLeft(0){res -> {age -> res + age}} = 78
     *   {}.foldLeft(0){res -> {age -> res + age}} = 0
     *   {Jessie: 22, John: 31, Ken: 25}.foldLeft([]){res -> {age -> res.append(age)}} = [22, 31, 25]
     *
     * @param e           	    initial value
     * @param f         		curried binary function
     * @return            	    folded result
     */
    fun <W> foldLeft(e: W, f: (W) -> (V) -> W): W {
        fun <W> recFoldLeft(e: W, map: Map<K, V>, f: (W) -> (V) -> W): W {
            return when(map) {
                is Tip -> e
                is Bin -> recFoldLeft(f(recFoldLeft(e, map.left, f))(map.value), map.right, f)
            }
        }

        return recFoldLeft(e, this, f)
    }   // foldLeft

    fun <W> foldLeft(e: W, f: (W, V) -> W): W = this.foldLeft(e, C2(f))

    /**
     * foldLeftWithKey is a higher-order function that folds a left associative binary
     *   function into a map.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.foldLeftWithKey(0){res -> {name -> {age -> res + name.length() + age}}} = 91
     *   {}.foldLeftWithKey(0){res -> {name -> {age -> res + name.length() + age}}} = 0
     *
     * @param e           	    initial value
     * @param f         		curried binary function
     * @return            	    folded result
     */
    fun <W> foldLeftWithKey(e: W, f: (W) -> (K) -> (V) -> W): W {
        fun <W> recFoldLeftWithKey(e: W, map: Map<K, V>, f: (W) -> (K) -> (V) -> W): W {
            return when(map) {
                is Tip -> e
                is Bin -> recFoldLeftWithKey(f(recFoldLeftWithKey(e, map.left, f))(map.key)(map.value), map.right, f)
            }
        }

        return recFoldLeftWithKey(e, this, f)
    }   // foldLeftWithKey

    fun <W> foldLeftWithKey(e: W, f: (W, K, V) -> W): W = this.foldLeftWithKey(e, C3(f))

    /**
     * foldRight is a higher-order function that folds a right associative binary
     *   function into the values of a map.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.foldRight(0){age -> {res -> res + age}} = 78
     *   {}.foldRight(0){age -> {res -> res + age}} = 0
     *   {Jessie: 22, John: 31, Ken: 25}.foldRight([]){age -> {res -> res.append(age)}} = [25, 31, 22]
     *
     * @param e           	    initial value
     * @param f         		curried binary function:: V -> W -> W
     * @return            	    folded result
     */
    fun <W> foldRight(e: W, f: (V) -> (W) -> W) : W {
        fun <W> recFoldRight(e: W, map: Map<K, V>, f: (V) -> (W) -> W) : W {
            return when(map) {
                is Tip -> e
                is Bin -> recFoldRight(f(map.value)(recFoldRight(e, map.right, f)), map.left, f)
            }
        }

        return recFoldRight(e, this, f)
    }   // foldRight

    fun <W> foldRight(e: W, f: (V, W) -> W) : W = this.foldRight(e, C2(f))

    /**
     * foldRightWithKey is a higher-order function that folds a right associative binary
     *   function into a map.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldRightWithKey(0){name -> {age -> {res -> res + name.length() + age}}} = 91
     *   <[]>.foldRightWithKey(0){name -> {age -> {res -> res + name.length() + age}}} = 0
     *
     * @param e           	    initial value
     * @param f         		curried binary function:: K -> V -> W -> W
     * @return            	    folded result
     */
    fun <W> foldRightWithKey(e: W, f: (K) -> (V) -> (W) -> W): W {
        fun <W> recFoldRightWithKey(e: W, map: Map<K, V>, f: (K) -> (V) -> (W) -> W): W {
            return when(map) {
                is Tip -> e
                is Bin -> recFoldRightWithKey(f(map.key)(map.value)(recFoldRightWithKey(e, map.right, f)), map.left, f)
            }
        }

        return recFoldRightWithKey(e, this, f)
    }   // foldRightWithKey

    fun <W> foldRightWithKey(e: W, f: (K, V, W) -> W): W = this.foldRightWithKey(e, C3(f))

    /**
     * Test whether the map is empty.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.isEmpty() = false
     *   {}.isEmpty() = true
     *
     * @return                  true if the map contains zero elements
     */
    fun isEmpty(): Boolean = (size == 0)

    /**
     * Returns a List view of the keys contained in this map.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.keyList() = [Jessie, John, Ken]
     *   {.keyList() = []
     *
     * @return    		        the keys for this map
     */
    fun keyList(): List<K> {
        fun recKeyList(map: Map<K, V>): List<K> {
            return when(map) {
                is Tip -> ListF.empty()
                is Bin -> recKeyList(map.left).append(map.key).append(recKeyList(map.right))
            }
        }

        return recKeyList(this)
    }   // keyList

    /**
     * Obtains the size of a map.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.length() = 3
     *   {}.length() = 0
     *
     * @return                  the number of elements in the map
     */
    fun length(): Int = size

    /**
     * Look up the given key in the map. Return value if present, otherwise
     *   throw an exception.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.lookUp("Ken") = 25
     *   {Jessie: 22, John: 31, Ken: 25}.lookUp("Irene") = exception
     *   {}.lookUp("Ken") = exception
     *
     * @param key               search key
     * @return                  matching value
     */
    fun lookUp(key: K): V {
        tailrec
        fun recLookUp(key: K, map: Map<K, V>): V {
            return when(map) {
                is Tip -> throw MapException("Map.lookUp: absent key: ${key}")
                is Bin -> {
                    if (key.compareTo(map.key) < 0)
                        recLookUp(key, map.left)
                    else if (key.compareTo(map.key) > 0)
                        recLookUp(key, map.right)
                    else
                        map.value
                }
            }
        }

        return recLookUp(key, this)
    }   // lookUp

    /**
     * Look up the given key in the map. Return None if absent, otherwise
     *   return the corresponding value wrapped in Some.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.lookUpKey("Ken") = Some(25)
     *   {Jessie: 22, John: 31, Ken: 25}.lookUpKey("Irene") = None
     *   {}.lookUpKey("Ken") = None
     *
     * @param key               search key
     * @return                  matching value or none if key is absent
     */
    fun lookUpKey(key: K): Option<V> {
        tailrec
        fun recLookUpKey(key: K, map: Map<K, V>): Option<V> {
            return when(map) {
                is Tip -> none()
                is Bin -> {
                    if (key.compareTo(map.key) < 0)
                        recLookUpKey(key, map.left)
                    else if (key.compareTo(map.key) > 0)
                        recLookUpKey(key, map.right)
                    else
                        some(map.value)
                }
            }
        }

        return recLookUpKey(key, this)
    }   // lookUpKey

    /**
     * Compose all the elements of this map as a string using the default separator, prefix, postfix, etc.
     *
     * @return                  the map content
     */
    fun makeString(): String = this.makeString(", ", "<[", "]>")

    /**
     * Compose all the elements of this map as a string using the separator, prefix, postfix, etc.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.makeString(", ", "<[", "]>", 2, "...") = <[Jessie: 22, John: 31, ...]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.makeString(", ", "<[", "]>", 2) = <[Jessie: 22, John: 31, ...]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.makeString(", ", "<[", "]>") = <[Jessie: 22, John: 31, Ken: 25]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.makeString() = <[Jessie: 22, John: 31, Ken: 25]>
     *   <[]>.makeString() = <[]>
     *
     * @param separator         the separator between each element
     * @param prefix            the leading content
     * @param postfix           the trailing content
     * @param limit             constrains the output to the fist limit elements
     * @param truncated         indicator that the output has been limited
     * @return                  the list content
     */
    fun makeString(separator: String = ", ", prefix: String = "", postfix: String = "", limit: Int = -1, truncated: String = "..."): String {
        var count: Int = 0
        fun recMakeString(map: Map<K, V>, buffer: StringBuffer): Int {
            return when(map) {
                is Tip -> count
                is Bin -> {
                    recMakeString(map.left, buffer)
                    if (count != 0)
                        buffer.append(separator)
                    if (limit < 0 || count < limit) {
                        buffer.append("${map.key}: ${map.value}")
                        count++
                    }
                    recMakeString(map.right, buffer)
                }
            }
        }   // recMakeString

        val buffer: StringBuffer = StringBuffer(prefix)
        val finalCount: Int = recMakeString(this, buffer)
        if (limit >= 0 && finalCount >= limit)
            buffer.append(truncated)
        buffer.append(postfix)
        return buffer.toString()
    }   // makeString

    /**
     * Map a function over all values in the map.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.map{v -> v + 1} = {Jessie: 23, John: 32, Ken: 26}
     *   {Jessie: 22, John: 31, Ken: 25}.map{v -> (v % 2 == 0)} = {Jessie: true, John: false, Ken: false}
     *
     * @param f     		    the function to apply to each value
     * @return      		    updated map
     */
    fun <W> map(f: (V) -> W): Map<K, W> {
        val mapList: List<Pair<K, V>> = MapF.toList(this)
        val mappedList: List<Pair<K, W>> = mapList.map{pr: Pair<K, V> -> Pair(pr.first, f(pr.second))}
        return MapF.fromList(mappedList)
    }   // map

    /**
     * Partition the map into two maps, one with all values that satisfy
     *   the predicate and one with all values that don't satisfy the predicate.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.partition{age -> (age % 2 == 0)} = ({Jessie: 22}, {John: 31, Ken: 25})
     *
     * @param predicate     	predicate function on the value types
     * @return              	pair of maps partitioned by the predicate
     */
    fun partition(predicate: (V) -> Boolean): Pair<Map<K, V>, Map<K, V>> {
        fun recPartition(predicate: (V) -> Boolean, map: Map<K, V>): Pair<Map<K, V>, Map<K, V>> {
            return when(map) {
                is Tip -> Pair(MapF.empty(), MapF.empty())
                is Bin -> {
                    if (predicate(map.value)) {
                        val leftPair: Pair<Map<K, V>, Map<K, V>> = recPartition(predicate, map.left)
                        val rightPair: Pair<Map<K, V>, Map<K, V>> = recPartition(predicate, map.right)
                        Pair(MapF.join(map.key, map.value, leftPair.first, rightPair.first), MapF.merge(leftPair.second, rightPair.second))
                    } else {
                        val leftPair: Pair<Map<K, V>, Map<K, V>> = recPartition(predicate, map.left)
                        val rightPair: Pair<Map<K, V>, Map<K, V>> = recPartition(predicate, map.right)
                        Pair(MapF.merge(leftPair.first, rightPair.first), MapF.join(map.key, map.value, leftPair.second, rightPair.second))
                    }
                }
            }
        }   // recPartition

        return recPartition(predicate, this)
    }   // partition

    /**
     * Partition the map into two maps, one with all keys that satisfy
     *   the predicate and one with all keys that don't satisfy the predicate.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.partitionKey{name -> (name.startsWith("J"))} = ({Jessie: 22], John: 31}, {Ken: 25})
     *
     * @param predicate     	predicate function on the value types
     * @return              	pair of maps partitioned by the predicate
     */
    fun partitionKey(predicate: (K) -> Boolean): Pair<Map<K, V>, Map<K, V>> {
        fun recPartitionKey(predicate: (K) -> Boolean, map: Map<K, V>): Pair<Map<K, V>, Map<K, V>> {
            return when(map) {
                is Tip -> Pair(MapF.empty(), MapF.empty())
                is Bin -> {
                    if (predicate(map.key)) {
                        val leftPair: Pair<Map<K, V>, Map<K, V>> = recPartitionKey(predicate, map.left)
                        val rightPair: Pair<Map<K, V>, Map<K, V>> = recPartitionKey(predicate, map.right)
                        Pair(MapF.join(map.key, map.value, leftPair.first, rightPair.first), MapF.merge(leftPair.second, rightPair.second))
                    } else {
                        val leftPair: Pair<Map<K, V>, Map<K, V>> = recPartitionKey(predicate, map.left)
                        val rightPair: Pair<Map<K, V>, Map<K, V>> = recPartitionKey(predicate, map.right)
                        Pair(MapF.merge(leftPair.first, rightPair.first), MapF.join(map.key, map.value, leftPair.second, rightPair.second))
                    }
                }
            }
        }   // recPartitionKey

        return recPartitionKey(predicate, this)
    }   // partitionKey

    /**
     * Obtain the size of the map.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.size() = 3
     *   {}.size() = 0
     *
     * @return                  the number of elements in the map
     */
    fun size(): Int = size

    /**
     * The expression split k map is a pair (map1,map2) where map1 comprises
     *   the elements of map with keys less than k and map2 comprises the elements of
     *   map with keys greater than k.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.split(Judith) = ({Jessie: 22, John: 31}, {Ken: 25})
     *   {Jessie: 22, John: 31, Ken: 25}.split(John) = ({Jessie: 22}, {Ken: 25})
     *
     * @param key     		    partitioning key
     * @return        		    pair of maps partitioned by the key
     */
    fun split(key: K): Pair<Map<K, V>, Map<K, V>> {
        fun recSplit(key: K, map: Map<K, V>): Pair<Map<K, V>, Map<K, V>> {
            return when(map) {
                is Tip -> Pair(MapF.empty(), MapF.empty())
                is Bin -> {
                    if (key.compareTo(map.key) < 0) {
                        val leftSplit: Pair<Map<K, V>, Map<K, V>> = recSplit(key, map.left)
                        Pair(leftSplit.first, MapF.join(map.key, map.value, leftSplit.second, map.right))
                    } else if (key.compareTo(map.key) > 0) {
                        val rightSplit: Pair<Map<K, V>, Map<K, V>> = recSplit(key, map.right)
                        Pair(MapF.join(map.key, map.value, map.left, rightSplit.first), rightSplit.second)
                    } else
                        Pair(map.left, map.right)
                }
            }
        }

        return recSplit(key, this)
    }   // split

    /**
     * Convert the map to a list of key/value pairs where the keys are in ascending order.
     */
    fun toAscendingList(): List<Pair<K, V>> =
        foldRightWithKey(ListF.empty()){k, v, xs -> ListF.cons(Pair(k, v), xs)}

    /**
     * Present the map as a graph revealing the left and right subtrees.
     *
     * @return                  the map as a graph
     */
    fun toGraph(): String {
        fun recToGraph(map: Map<K, V>, spaces: String): String {
            return if(map.isEmpty())
                "${spaces}Tip"
            else {
                val binMap: Bin<K, V> = map as Bin<K, V>
                val binString: String = "${spaces}Bin: ${binMap.key} ${binMap.value}"
                val leftString: String = recToGraph(binMap.left, spaces + "  ")
                val rightString: String = recToGraph(binMap.right, spaces + "  ")
                "${binString}\n${leftString}\n${rightString}"
            }
        }

        return recToGraph(this, "")
    }   // toGraph

    /**
     * Textual representation of a map.
     *
     * @return                  text for a map: <[key1: value1, key2: value2, ...]>
     */
    override fun toString(): String = this.makeString(", ", "<[", "]>")

    /**
     * Returns a List view of the values contained in this map.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.valueList() = [22, 31, 25]
     *   <[]>.valueList() = []
     *
     * @return    		the values for this map
     */
    fun valueList(): List<V> {
        fun recValueList(map: Map<K, V>): List<V> {
            return when(map) {
                is Tip -> ListF.empty<V>()
                is Bin -> recValueList(map.left).append(map.value).append(recValueList(map.right))
            }
        }

        return recValueList(this)
    }   // valueList

}   // Map
