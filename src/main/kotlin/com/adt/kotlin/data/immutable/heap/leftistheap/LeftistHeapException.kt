package com.adt.kotlin.data.immutable.heap.leftistheap

/**
 * A leftist heap is a data structure that provides efficient access to the
 *   minimum element. In a leftist heap the element at each node is no
 *   larger than the elements at its children. Under this ordering the minimum
 *   element is always at the root.
 *
 * Leftist heaps are heap-orderd binary trees that satisfy the leftist property:
 *   the rank of any left child is at least as large as the rank of its right
 *   sibling. The rank of a node is defined to be the length of its right spine:
 *   the rightmost path from the node to an empty node. Consequently, the right
 *   spine of any node is always the shortest path to an empty node.
 *
 * @param P                     the priority
 * @param A                     the element type
 *
 * @author	                    Ken Barclay
 * @since                       September 2019
 */



class LeftistHeapException(message: String) : Exception(message)
