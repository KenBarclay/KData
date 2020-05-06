package com.adt.kotlin.data.immutable.heap.leftistheap

/**
 * A leftist heap is a data structure that provides efficient access to the
 *   minimum element. In a leftist heap the element at each node is no
 *   larger than the elements at its children. Under this ordering the minimum
 *   element is always at the root.
 *
 * Leftist heaps are heap-ordered binary trees that satisfy the leftist property:
 *   the rank of any left child is at least as large as the rank of its right
 *   sibling. The rank of a node is defined to be the length of its right spine:
 *   the rightmost path from the node to an empty node. Consequently, the right
 *   spine of any node is always the shortest path to an empty node.
 *
 * @param P                     the priority type
 * @param A                     the element type
 *
 * @author	                    Ken Barclay
 * @since                       September 2019
 */

import com.adt.kotlin.data.immutable.heap.leftistheap.LeftistHeap.Empty
import com.adt.kotlin.data.immutable.heap.leftistheap.LeftistHeap.Heap

import com.adt.kotlin.data.immutable.list.List



object LeftistHeapF {

    /**
     * Deliver an empty LeftistHeap.
     */
    fun <P : Comparable<P>, A> empty(): LeftistHeap<P, A> = Empty()

    /**
     * Factory constructor function.
     */
    fun <P : Comparable<P>, A> heap(rank: Int, size: Int, priority: P, value: A, left: LeftistHeap<P, A>, right: LeftistHeap<P, A>): LeftistHeap<P, A> =
        Heap(rank, size, priority,value, left, right)

    /**
     * Deliver a LeftistHeap containg a single value.
     */
    fun <P : Comparable<P>, A> singleton(priority: P, value: A): LeftistHeap<P, A> =
        Heap(1, 1, priority, value, empty(), empty())

    /**
     * Build a LeftistHeap from the List of priority-value pairs.
     */
    fun <P : Comparable<P>, A> from(pairs: List<Pair<P, A>>): LeftistHeap<P, A> {
        val sortedDescending: List<Pair<P, A>> = pairs.sort{pair1, pair2 -> if (pair2.first < pair1.first) -1 else if (pair2.first > pair1.first) +1 else 0}
        return fromDescsendingList(sortedDescending)
    }   // from

    fun <P : Comparable<P>, A> from(vararg seq: Pair<P, A>): LeftistHeap<P, A> {
        val seqCopy: Array<Pair<P, A>> = Array(seq.size){idx -> seq[idx]}
        seqCopy.sortWith(object: Comparator<Pair<P, A>> {
            override fun compare(pair1: Pair<P, A>, pair2: Pair<P, A>): Int =
                if (pair2.first < pair1.first) -1 else if (pair2.first > pair1.first) +1 else 0
        })
        return fromDescsendingArray(seqCopy)
    }   // from



// ---------- implementation ------------------------------

    /**
     * Create a LeftistHeap froma List providing its priority-value pairs in
     *   descending order.
     */
    private fun <P : Comparable<P>, A> fromDescsendingList(pairs: List<Pair<P, A>>): LeftistHeap<P, A> =
            pairs.foldLeft(empty()){heap -> {pair -> heap.insert(pair.first, pair.second)}}

    private fun <P : Comparable<P>, A> fromDescsendingArray(pairs: Array<Pair<P, A>>): LeftistHeap<P, A> =
        pairs.fold(empty()){heap, pair -> heap.insert(pair.first, pair.second)}

}   // LeftistHeapF
