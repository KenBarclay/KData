package com.adt.kotlin.data.immutable.trie.vector

/**
 * The Vector is a persistent version of the classical vector data structure.
 *   The structure supports efficient, non-destructive operations. It is a port
 *   of the Haskell port from Clojure.
 *
 * The algebraic data type declaration is:
 *
 * datatype Node[V] = EmptyNode[V]
 *                  | RootNode[K, V] of Int * Int * Int * Int * List[V] * Array[Node[V]]
 *                  | InternalNode[K, V] of Array[Node[V]]
 *                  | DataNode[K, V] of Array[V]
 *
 * @author	                    Ken Barclay
 * @since                       December 2014
 */



class VectorException(message: String) : Exception(message)
