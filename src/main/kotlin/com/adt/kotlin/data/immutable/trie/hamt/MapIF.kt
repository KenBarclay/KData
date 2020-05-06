package com.adt.kotlin.data.immutable.trie.hamt

/**
 * The HamtMap is a persistent version of the classical hash table data structure.
 *   The structure supports efficient, non-destructive operations.
 *
 * The algebraic data type declaration is:
 *
 * datatype Node[A, B] = EmptyNode[A, B]
 *                     | LeafNode[A, B] of Int * A * B
 *                     | ArrayNode[A, B] of Int * [Node[A, B]]  where [...] is an array
 *                     | BitmapIndexedNode[A, B] of Int * [Node{A, B]]
 *                     | HashCollisionNode[A, B] of Int * List[Pair[A, B]]
 *
 * This implementation is modelled after the Haskell version described in the talk
 *   Faster persistent data structures through hashing by Johan Tibell at:
 *   https://www.haskell.org/wikiupload/6/65/HIW2011-Talk-Tibell.pdf. The Haskell
 *   code follows the Clojure implementation by Rich Hickey.
 *
 * @param K                     the type of keys in the map
 * @param V                     the type of values in the map
 *
 * @author	                    Ken Barclay
 * @since                       December 2014
 */

import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.list.List



interface MapIF<K : Comparable<K>, V> {

    /**
     * Determine if the map contains the given key.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.containsKey(Ken) = true
     *   <[Jessie: 22, John: 31, Ken: 25]>.containsKey(Irene) = false
     *   <[]>.containsKey(Ken) = false
     *
     * @param key               search key
     * @return                  true if the map contains this key
     */
    fun contains(key: K): Boolean

    /**
     * Delete the key and its value from the map. If the key is not in the map
     *   then the original map is returned.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.delete(Ken) = <[Jessie: 22, John: 31]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.delete(Irene) = <[Jessie: 22, John: 31, Ken: 25]>
     *
     * @param key               look up key in the map
     * @return                  the updated map
     */
    fun delete(key: K): MapIF<K, V>

    /**
     * Filter all values that satisfy the predicate.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.filter{v -> (v % 2 == 0)} = <[Jessie: 22]>
     *   <[]>.filter{v -> (v % 2 == 0)} = <[]>
     *
     * @param predicate         search criteria
     * @return                  resulting map
     */
    fun filter(predicate: (V) -> Boolean): MapIF<K, V>

    /**
     * foldLeft is a higher-order function that folds a left associative binary
     *   function into the values of a map.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldLeft(0){res -> {age -> res + age}} = 78
     *   <[]>.foldLeft(0){res -> {age -> res + age}} = 0
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldLeft([]){res -> {age -> res.append(age)}} = [22, 31, 25]
     *
     * @param e           	    initial value
     * @param f         		curried binary function:: W -> V -> W
     * @return            	    folded result
     */
    fun <W> foldLeft(e: W, f: (W) -> (V) -> W): W

    fun <W> foldLeft(e: W, f: (W, V) -> W): W

    /**
     * foldRight is a higher-order function that folds a right associative binary
     *   function into the values of a map.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldRight(0){age -> {res -> res + age}} = 78
     *   <[]>.foldRight(0){age -> {res -> res + age}} = 0
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldRight([]){age -> {res -> res.append(age)}} = [25, 31, 22]
     *
     * @param e           	    initial value
     * @param f         		curried binary function:: B -> C -> C
     * @return            	    folded result
     */
    fun <W> foldRight(e: W, f: (V) -> (W) -> W): W

    fun <W> foldRight(e: W, f: (V, W) -> W): W

    /**
     * Insert a new key and the value into the map. If the key is already present,
     *   the associated value is replaced with the given value.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.insert(Irene, 30) = <[Irene: 30, Jessie: 22, John: 31, Ken: 25]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.insert(Ken, 30) = <[Jessie: 22, John: 31, Ken: 30]>
     *
     * @param key               new key
     * @param value             new associated value
     * @return                  updated map
     */
    fun insert(key: K, value: V): MapIF<K, V>

    fun insert(pr: Pair<K, V>): MapIF<K, V>

    /**
     * Test whether the map is empty.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.isEmpty() = false
     *   <[]>.isEmpty() = true
     *
     * @return                  true if the map contains zero elements
     */
    fun isEmpty(): Boolean

    /**
     * Returns a List view of the keys contained in this map.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.keyList() = [Jessie, John, Ken]
     *   <[]>.keyList() = []
     *
     * @return    		        the keys for this map
     */
    fun keyList(): List<K>

    /**
     * Obtain the size of the map.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.length() = 3
     *   <[]>.length() = 0
     *
     * @return                  the number of elements in the map
     */
    fun length(): Int

    /**
     * Look up the value at the key in the map. Return the corresponding
     *   value if the key is present otherwise throws an exception.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.lookUp(Ken) = 25
     *   <[Jessie: 22, John: 31, Ken: 25]>.lookUp(Irene) = exception
     *   <[]>.lookUp(Irene) = exception
     *
     * @param key               search key
     * @return                  corresponding value, if key is present
     */
    fun lookUp(key: K): V

    /**
     * Look up the value at the key in the map.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.lookUpKey(Ken) = Some(25)
     *   <[Jessie: 22, John: 31, Ken: 25]>.lookUpKey(Irene) = None
     *   <[]>.lookUpKey(Ken) = None
     *
     * @param key               search key
     * @return                  corresponding value, if key is present
     */
    fun lookUpKey(key: K): Option<V>

    /**
     * Look up the given key in the map. Return defaultValue if absent, otherwise
     *   return the corresponding value.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.lookUpKeyWithDefault(Ken, 99) = 25
     *   <[Jessie: 22, John: 31, Ken: 25]>.lookUpKeyWithDefault(Irene, 99) = 99
     *   <[]>.lookUpKeyWithDefault(Irene, 99) = 99
     *
     * @param key               search key
     * @param defaultValue      default value to use if key is absent
     * @return                  matching value or default if key is absent
     */
    fun lookUpKeyWithDefault(key: K, defaultValue: V): V

    /**
     * Map a function over all the values in the map.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.map{v -> v + 1} = <[Jessie: 23, John: 32, Ken: 26]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.map{v -> (v % 2 == 0)} = <[Jessie: true, John: false, Ken: false]>
     *
     * @param f                 mapping function
     * @return                  updated map
     */
    fun <W> map(f: (V) -> W): MapIF<K, W>

    /**
     * Obtain the size of the map.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.size() = 3
     *   <[]>.size() = 0
     *
     * @return                  the number of elements in the map
     */
    fun size(): Int

    /**
     * Convert the map to a list of key/value pairs where the keys are in ascending order.
     */
    fun toAscendingList(): List<Pair<K, V>>

    /**
     * Present the map as a graph revealing the left and right subtrees.
     *
     * @return                  the map as a graph
     */
    fun toGraph(): String

    /**
     * Convert this map to a list of key/value pairs
     *
     * @return                  list of key/value pairs
     */
    fun toList(): List<Pair<K, V>>

    /**
     * Returns a List view of the values contained in this map.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.valueList() = [22, 31, 25]
     *   <[]>.valueList() = []
     *
     * @return    		        the values for this map
     */
    fun valueList(): List<V>

}   // MapIF
