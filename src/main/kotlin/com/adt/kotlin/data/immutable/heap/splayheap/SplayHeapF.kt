package com.adt.kotlin.data.immutable.heap.splayheap

/**
 * A splay heap is a self-balancing binary search tree with the additional
 *   property that recently accessed elements are quick to access again. All
 *   normal operations on a binary search tree are combined with one basic
 *   operation, called splaying. Splaying the tree for a certain element
 *   rearranges the tree so that the element is placed at the root of the tree.
 *   One way to do this with the basic search operation is to first perform a
 *   standard binary tree search for the element in question, and then use
 *   tree rotations in a specific fashion to bring the element to the top
 *
 * Author:	                    Ken Barclay
 * Date:	                    September 2019
 */

import com.adt.kotlin.data.immutable.heap.splayheap.SplayHeap.Empty
import com.adt.kotlin.data.immutable.heap.splayheap.SplayHeap.Heap

import com.adt.kotlin.data.immutable.list.List



object SplayHeapF {

    /**
     * Create an empty heap.
     */
    fun <A : Comparable<A>> empty(): SplayHeap<A> = Empty()

    /**
     * Factory constructor function.
     */
    fun <A : Comparable<A>> heap(value: A, left: SplayHeap<A>, right: SplayHeap<A>): SplayHeap<A> =
        Heap(value, left, right)

    /**
     * Create a heap containing a single element.
     */
    fun <A : Comparable<A>> singleton(a: A): SplayHeap<A> = Heap(a, SplayHeap.Empty(), SplayHeap.Empty())

    /**
     * Create a SplayHeap from a list of elements.
     */
    fun <A : Comparable<A>> from(list: List<A>): SplayHeap<A> =
            list.foldLeft(empty()){heap -> {a -> heap.insert(a)}}

    /**
     * Create a SplayHeap from a series of elements.
     */
    fun <A : Comparable<A>> from(vararg seq: A): SplayHeap<A> =
        seq.fold(empty()){heap, a -> heap.insert(a)}

    /**
     * Create a SplayHeap from a series of elements.
     */
    fun <A : Comparable<A>> of(vararg seq: A): SplayHeap<A> =
            seq.fold(empty()){heap, a -> heap.insert(a)}

}   // SplayHeapF
