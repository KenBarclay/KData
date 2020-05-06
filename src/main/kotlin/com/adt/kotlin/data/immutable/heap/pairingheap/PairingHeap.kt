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

import com.adt.kotlin.data.immutable.list.*
import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.ListF



sealed class PairingHeap<A : Comparable<A>> {



    class Empty<A : Comparable<A>> internal constructor() : PairingHeap<A>() {
        override fun toString(): String = "Empty"
    }



    class Heap<A : Comparable<A>> internal constructor(val element: A, val subHeaps: List<PairingHeap<A>>) : PairingHeap<A>() {
        override fun toString(): String = "Heap(element: $element, subHeaps: $subHeaps)"
    }



    /**
     * Is this heap empty?
     */
    fun isEmpty(): Boolean = when (this) {
        is Empty -> true
        is Heap -> false
    }   // isEmpty

    /**
     * Find the size of this heap.
     */
    fun size(): Int {
        fun recSize(pairingHeap: PairingHeap<A>): Int {
            return when (pairingHeap) {
                is Empty -> 0
                is Heap -> 1 + pairingHeap.subHeaps.foldLeft(0){acc -> {heap -> acc + heap.size()}}
            }
        }   // recSize

        return recSize(this)
    }   // size

    /**
     * Return the root element of the heap.
     */
    fun findMinimum(): A = when (this) {
        is Empty -> throw PairingHeapException("findMinimum: empty heap")
        is Heap -> this.element
    }   // findMinimum

    /**
     * Merge this PairingHeap with the given PairingHeap. Make the heap with
     *   the larger root the leftmost child of the heap with the samller root.
     */
    fun merge(heap: PairingHeap<A>): PairingHeap<A> = when (this) {
        is Empty -> heap
        is Heap -> when (heap) {
            is Empty -> this
            is Heap -> if (this.element < heap.element)
                Heap(this.element, ListF.cons(heap, this.subHeaps))
            else
                Heap(heap.element, ListF.cons(this, heap.subHeaps))
        }
    }   // merge

    /**
     * Insert a new element into this PairingHeap.
     */
    fun insert(element: A): PairingHeap<A> = merge(Heap(element, ListF.empty()))

    /**
     * For a min-heap the minimum element is the root of the heap. To delete
     *   this element, delete the root node. If the root had two or more subtrees,
     *   these must be merged together into a single tree.
     */
    fun deleteMinimum(): PairingHeap<A> {
        fun mergePairs(heaps: List<PairingHeap<A>>): PairingHeap<A> {
            return if (heaps.size() == 0)
                Empty()
            else if (heaps.size() == 1)
                heaps.head()
            else {
                val h0: PairingHeap<A> = heaps[0]
                val h1: PairingHeap<A> = heaps[1]
                val hs: List<PairingHeap<A>> = heaps.drop(2)
                h0.merge(h1).merge(mergePairs(hs))
            }
        }   // mergePairs

        return when (this) {
            is Empty -> throw PairingHeapException("deleteMinimum: emply heap")
            is Heap -> mergePairs(this.subHeaps)
        }
    }   // deleteMinimum

    /**
     * Create a List from this PairingHeap.
     */
    fun toList(): List<A> {
        fun recToList(pairingHeap: PairingHeap<A>): List<A> {
            return when (pairingHeap) {
                is Empty -> ListF.empty()
                is Heap -> {
                    val subs: List<A> = pairingHeap.subHeaps.foldLeft(ListF.empty()){acc -> {heap -> acc.append(heap.toList())}}
                    ListF.cons(pairingHeap.element, subs)
                }
            }
        }   // recToList

        return recToList(this)
    }   // toList

    /**
     * Are two heaps equal?
     *
     * @param other             the other heap
     * @return                  true if both heaps are the same; false otherwise
     */
    override fun equals(other: Any?): Boolean {
        return if (this === other)
            true
        else if (other == null || this::class.java != other::class.java)
            false
        else {
            @Suppress("UNCHECKED_CAST") val otherPairingHeap: PairingHeap<A> = other as PairingHeap<A>
            val thisList: List<A> = this.toList().sort{a, b -> if (a < b) -1 else if (a > b) +1 else 0}
            val otherList: List<A> = otherPairingHeap.toList().sort{a, b -> if (a < b) -1 else if (a > b) +1 else 0}
            (thisList == otherList)
        }
    }   // equals

}   // PairingHeap
