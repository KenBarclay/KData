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

import com.adt.kotlin.hkfp.typeclass.Monoid



// Functor extension functions:

fun <K : Comparable<K>, V, W> MapIF<K, V>.fmap(f: (V) -> W): MapIF<K, W> = this.map(f)

/**
 * An infix symbol for fmap.
 */
infix fun <K : Comparable<K>, V, W> ((V) -> W).dollar(v: MapIF<K, V>): MapIF<K, W> = v.fmap(this)

fun <K : Comparable<K>, V, W> MapIF<K, V>.replaceAll(c: W): MapIF<K, W> = this.fmap{_ -> c}

fun <K : Comparable<K>, V, W> MapIF<K, Pair<V, W>>.distribute(): Pair<MapIF<K, V>, MapIF<K, W>> =
    Pair(this.fmap{pr -> pr.first}, this.fmap{pr -> pr.second})

/**
 * Inject w to the left of the v's in this map.
 */
fun <K : Comparable<K>, V, W> MapIF<K, V>.injectLeft(w: W): MapIF<K, Pair<W, V>> = this.fmap{v: V -> Pair(w, v)}

/**
 * Inject w to the right of the v's in this map.
 */
fun <K : Comparable<K>, V, W> MapIF<K, V>.injectRight(w: W): MapIF<K, Pair<V, W>> = this.fmap{v: V -> Pair(v, w)}

/**
 * Twin all the v's in this map with itself.
 */
fun <K : Comparable<K>, V> MapIF<K, V>.pair(): MapIF<K, Pair<V, V>> = this.fmap{v: V -> Pair(v, v)}

/**
 * Pair all the v's in this list with the result of the function application.
 */
fun <K : Comparable<K>, V, W> MapIF<K, V>.product(f: (V) -> W): MapIF<K, Pair<V, W>> = this.fmap{v: V -> Pair(v, f(v))}



// Foldable extension functions:

/**
 * Combine the elements of a structure using a monoid.
 *
 * Examples:
 *   {}.fold(intAddMonoid) == 0
 *   {Ken: 1, John: 2, Jessie: 3}.fold(intAddMonoid) == 6
 */
fun <K : Comparable<K>, V> MapIF<K, V>.fold(md: Monoid<V>): V {
    val self: MapIF<K, V> = this
    return md.run{
        self.foldLeft(empty){b -> {a -> combine(b, a)}}
    }
}   // fold

fun <K : Comparable<K>, V, W> MapIF<K, V>.foldMap(md: Monoid<W>, f: (V) -> W): W =
    this.foldLeft(md.empty){b -> {a -> md.combine(b, f(a))}}
