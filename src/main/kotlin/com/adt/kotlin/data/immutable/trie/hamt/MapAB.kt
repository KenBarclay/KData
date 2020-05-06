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
 * @param A                     the type of keys in the map
 * @param B                     the type of values in the map
 *
 * @author	                    Ken Barclay
 * @since                       December 2014
 */

import com.adt.kotlin.data.immutable.trie.hamt.node.Node

import com.adt.kotlin.data.immutable.option.Option

import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.hkfp.fp.FunctionF.C2



abstract class MapAB<K : Comparable<K>, V> internal constructor(val root: Node<K, V>) : MapIF<K, V> {

    /**
     * Determine if the map contains the given key.
     *
     * @param key               search key
     * @return                  true if the map contains this key
     */
    override fun contains(key: K): Boolean = root.containsKey(key)

    /**
     * Delete the key and its value from the map. If the key is not in the map
     *   then the original map is returned.
     *
     * @param key               look up key in the map
     * @return                  the updated map
     */
    override fun delete(key: K): MapIF<K, V> = Map(root.delete(key))

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
            (this.size() == otherMap.size()) && (this.toAscendingList() == otherMap.toAscendingList())
        }
    }   // equals

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
    override fun filter(predicate: (V) -> Boolean): MapIF<K, V> = Map(root.filter(predicate))

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
    override fun <W> foldLeft(e: W, f: (W) -> (V) -> W): W = root.foldLeft(e, f)

    override fun <W> foldLeft(e: W, f: (W, V) -> W): W = this.foldLeft(e, C2(f))

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
    override fun <W> foldRight(e: W, f: (V) -> (W) -> W): W = root.foldRight(e, f)

    override fun <W> foldRight(e: W, f: (V, W) -> W): W = this.foldRight(e, C2(f))

    /**
     * Insert a new key and the value into the map. If the key is already present,
     *   the associated value is replaced with the given value.
     *
     * @param key               new key
     * @param value             new associated value
     * @return                  updated map
     */
    override fun insert(key: K, value: V): MapIF<K, V> = Map(root.insert(key, value))

    override fun insert(pr: Pair<K, V>): MapIF<K, V> = insert(pr.first, pr.second)

    /**
     * Test whether the map is empty.
     *
     * @return                  true if the map contains zero elements
     */
    override fun isEmpty(): Boolean = root.isEmpty()

    /**
     * Returns a List view of the keys contained in this map.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.keyList() = [Jessie, John, Ken]
     *   <[]>.keyList() = []
     *
     * @return    		        the keys for this map
     */
    override fun keyList(): List<K> = root.keyList()

    /**
     * Obtain the size of the map.
     *
     * @return                  the number of elements in the map
     */
    override fun length(): Int = this.size()

    /**
     * Look up the value at the key in the map. Return the corresponding
     *   value if the key is present otherwise throws an exception.
     *
     * @param key               search key
     * @return                  corresponding value, if key is present
     */
    override fun lookUp(key: K): V = root.lookUp(key)

    /**
     * Look up the value at the key in the map.
     *
     * @param key               search key
     * @return                  corresponding value, if key is present
     */
    override fun lookUpKey(key: K): Option<V> = root.lookUpKey(key)

    /**
     * Look up the given key in the map. Return defaultValue if absent, otherwise
     *   return the corresponding value.
     *
     * @param key               search key
     * @param defaultValue      default value to use if key is absent
     * @return                  matching value or default if key is absent
     */
    override fun lookUpKeyWithDefault(key: K, defaultValue: V): V = root.lookUpKeyWithDefault(key, defaultValue)

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
    override fun <W> map(f: (V) -> W): MapIF<K, W> = Map(root.map(f))

    /**
     * Obtain the size of the map.
     *
     * @return                  the number of elements in the map
     */
    override fun size(): Int = root.size()

    /**
     * Convert the map to a list of key/value pairs where the keys are in ascending order.
     */
    override fun toAscendingList(): List<Pair<K, V>> = root.toAscendingList()

    /**
     * Present the map as a graph revealing the left and right subtrees.
     *
     * @return                  the map as a graph
     */
    override fun toGraph(): String = root.toGraph()

    /**
     * Convert this map to a list of key/value pairs
     *
     * @return                  list of key/value pairs
     */
    override fun toList(): List<Pair<K, V>> = root.toList()

    /**
     * Textual representation of a map.
     *
     * @return                  text representation including node sub-structures
     */
    override fun toString(): String {
        val content: String = root.toString()
        return "<{$content}>"
    }

    /**
     * Returns a List view of the values contained in this map.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.valueList() = [22, 31, 25]
     *   <[]>.valueList() = []
     *
     * @return    		        the values for this map
     */
    override fun valueList(): List<V> = root.valueList()

}   // MapAB
