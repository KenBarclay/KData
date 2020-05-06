package com.adt.kotlin.data.immutable.heap.pairingheap

/**
 * The pairing heap is a type of heap data structure with relatively simple
 *   implementation and excellent practical amortized performance. The pairing
 *   heap are heap-ordered multi-way trees. Pairing heaps maintain a min-heap
 *   property that all parent nodes always have a smaller value than their children.
 *
 * Author:	                    Ken Barclay
 * Date:	                    September 2019
 */

import com.adt.kotlin.data.immutable.heap.pairingheap.PairingHeap.Empty
import com.adt.kotlin.data.immutable.heap.pairingheap.PairingHeap.Heap

import com.adt.kotlin.data.immutable.list.List



object PairingHeapF {

    /**
     * Delever an empty PairingHeap.
     */
    fun <A : Comparable<A>> empty(): PairingHeap<A> = Empty()

    fun <A : Comparable<A>> heap(element: A, subHeaps: List<PairingHeap<A>>): PairingHeap<A> =
        Heap(element, subHeaps)

    /**
     * Deliver a PairingHeap with one element.
     */
    fun <A : Comparable<A>> singleton(a: A): PairingHeap<A> = Empty<A>().insert(a)

    /**
     * Create a PairingHeap from a List of values.
     */
    fun <A : Comparable<A>> from(list: List<A>): PairingHeap<A> =
            list.foldLeft(empty()){heap -> {a -> heap.insert(a)}}

    /**
     * Create a PairingHeap from a series of values.
     */
    fun <A : Comparable<A>> of(vararg seq: A): PairingHeap<A> =
            seq.fold(empty()){heap, a -> heap.insert(a)}

}   // PairingHeapF
