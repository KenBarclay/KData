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



class Map<K : Comparable<K>, V> internal constructor (root: Node<K, V>) : MapAB<K, V>(root)
