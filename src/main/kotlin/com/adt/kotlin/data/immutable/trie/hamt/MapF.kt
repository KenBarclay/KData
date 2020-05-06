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
 * @author	                    Ken Barclay
 * @since                       December 2014
 */

import com.adt.kotlin.data.immutable.trie.hamt.node.Node.EmptyNode
import com.adt.kotlin.data.immutable.trie.hamt.node.Node.LeafNode

import com.adt.kotlin.data.immutable.list.List

import kotlin.collections.List as KList



object MapF {

    /**
     * Create an empty map.
     *
     * @return    		            an empty map
     */
    fun <K : Comparable<K>, V> empty(): MapIF<K, V> = Map(EmptyNode())

    /**
     * A map with a single element.
     *
     * @return                      a map containing the given key/value pair
     */
    fun <K : Comparable<K>, V> singleton(key: K, value: V): MapIF<K, V> = Map(LeafNode(key.hashCode(), key, value))



    fun <K : Comparable<K>, V> of(): MapIF<K, V> = empty()

    fun <K : Comparable<K>, V> of(k1: K, v1: V): MapIF<K, V> = empty<K, V>().insert(k1, v1)

    fun <K : Comparable<K>, V> of(k1: K, v1: V, k2: K, v2: V): MapIF<K, V> = empty<K, V>().insert(k1, v1).insert(k2, v2)

    fun <K : Comparable<K>, V> of(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V): MapIF<K, V> = empty<K, V>().insert(k1, v1).insert(k2, v2).insert(k3, v3)

    fun <K : Comparable<K>, V> of(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V): MapIF<K, V> =
            empty<K, V>().insert(k1, v1).insert(k2, v2).insert(k3, v3).insert(k4, v4)

    fun <K : Comparable<K>, V> of(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V, k5: K, v5: V): MapIF<K, V> =
            empty<K, V>().insert(k1, v1).insert(k2, v2).insert(k3, v3).insert(k4, v4).insert(k5, v5)



    fun <K: Comparable<K>, V> of(e1: Pair<K, V>): MapIF<K, V> = empty<K, V>().insert(e1)

    fun <K: Comparable<K>, V> of(e1: Pair<K, V>, e2: Pair<K, V>): MapIF<K, V> = empty<K, V>().insert(e1).insert(e2)

    fun <K: Comparable<K>, V> of(e1: Pair<K, V>, e2: Pair<K, V>, e3: Pair<K, V>): MapIF<K, V> = empty<K, V>().insert(e1).insert(e2).insert(e3)

    fun <K: Comparable<K>, V> of(e1: Pair<K, V>, e2: Pair<K, V>, e3: Pair<K, V>, e4: Pair<K, V>): MapIF<K, V> =
            empty<K, V>().insert(e1).insert(e2).insert(e3).insert(e4)

    fun <K: Comparable<K>, V> of(e1: Pair<K, V>, e2: Pair<K, V>, e3: Pair<K, V>, e4: Pair<K, V>, e5: Pair<K, V>): MapIF<K, V> =
            empty<K, V>().insert(e1).insert(e2).insert(e3).insert(e4).insert(e5)

    fun <K: Comparable<K>, V> of(vararg seq: Pair<K, V>): MapIF<K, V> = from(*seq)



    /**
     * Apply the block to each element in the map.
     *
     * @param block                 body of program block
     */
    fun <K: Comparable<K>, V> forEach(map: MapIF<K, V>, block: (K, V) -> Unit): Unit {
        for (pr: Pair<K, V> in map.toList().iterator())
            block(pr.first, pr.second)
    }

    /**
     * Convert a variable-length parameter series into a map.
     *
     * @param seq                   variable-length parameter series
     * @return                      map of the given values
     */
    fun <K: Comparable<K>, V> from(vararg seq: Pair<K, V>): MapIF<K, V> {
        var map: MapIF<K, V> = empty()
        for (pair: Pair<K, V> in seq) {
            map = map.insert(pair.first, pair.second)
        }
        return map
    }

    /**
     * Convert a java-based list into an immutable map.
     *
     * @param list                  java based list of elements
     * @return                      immutable map of the given values
     */
    fun <K: Comparable<K>, V> from(list: KList<Pair<K, V>>): MapIF<K, V> {
        return list.foldRight(empty()){pair: Pair<K, V>, map: MapIF<K, V> -> map.insert(pair.first, pair.second)}
    }

    /**
     * Convert an immutable list into an immutable map.
     *
     * @param list                  immutable list of elements
     * @return                      immutable map of the given values
     */
    fun <K: Comparable<K>, V> from(list: List<Pair<K, V>>): MapIF<K, V> {
        return list.foldRight(empty()) {pair: Pair<K, V> -> {map: MapIF<K, V> -> map.insert(pair.first, pair.second)}}
    }



    // Functor extension functions:

    /**
     * Lift a function into the List context.
     */
    fun <K: Comparable<K>, V, W> lift(f: (V) -> W): (MapIF<K, V>) -> MapIF<K, W> =
        {mkv: MapIF<K, V> ->
            mkv.fmap(f)
        }   // lift

}
