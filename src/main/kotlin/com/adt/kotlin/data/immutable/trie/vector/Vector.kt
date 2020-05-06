package com.adt.kotlin.data.immutable.trie.vector

/**
 * The Vector is a persistent version of the classical vector data structure.
 *   The structure supports efficient, non-destructive operations. It is a port
 *   of the Haskell port from Clojure.
 *
 * The algebraic data type declaration is:
 *
 * datatype Node[A] = EmptyNode[A]
 *                  | RootNode[A] of Int * Int * Int * Int * List[A] * Array[Node[A]]
 *                  | InternalNode[A] of Array[Node[A]]
 *                  | DataNode[A] of Array[A]
 *
 * @param A                     the type of elements in the vector
 *
 * @author	                    Ken Barclay
 * @since                       December 2014
 */

import com.adt.kotlin.data.immutable.trie.vector.node.*



class Vector<A> internal constructor(root: Node<A>) : VectorAB<A>(root)
