package com.adt.kotlin.data.immutable.priorityqueue

/**
 * This class implements priority queues using a heap. A priority queue
 *   is an abstract data type which is like a regular queue or stack
 *   data structure, but where additionally each element has a priority
 *   associated with it. In a priority queue, an element with high priority
 *   is served before an element with low priority.
 *
 * Author:	                    Ken Barclay
 * Date:	                    October 2019
 */

import com.adt.kotlin.data.immutable.heap.binomialheap.BinomialHeapF

import com.adt.kotlin.data.immutable.list.List



object PriorityQueueF {

    /**
     * Create an empty queue.
     */
    fun <A : Comparable<A>> empty(): PriorityQueue<A> = PriorityQueue()

    /**
     * Create a queue containing a single element.
     */
    fun <A : Comparable<A>> singleton(a: A): PriorityQueue<A> =
        PriorityQueue(BinomialHeapF.singleton(a))

    /**
     * Create a queue from a list of elements.
     */
    fun <A : Comparable<A>> from(list: List<A>): PriorityQueue<A> =
        PriorityQueue(BinomialHeapF.from(list))

    fun <A : Comparable<A>> from(vararg seq: A): PriorityQueue<A> =
        PriorityQueue(BinomialHeapF.from(*seq))

    /**
     * Create a BinomialHeap from a sequence of elements.
     */
    fun <A : Comparable<A>> of(vararg seq: A): PriorityQueue<A> =
        PriorityQueue(BinomialHeapF.of(*seq))

    /**
     * Factory functions to create the base instances from a series of none or more elements.
     */
    fun <A : Comparable<A>> of(): PriorityQueue<A> = empty()

    fun <A : Comparable<A>> of(a1: A): PriorityQueue<A> = singleton(a1).merge(empty())

    fun <A : Comparable<A>> of(a1: A, a2: A): PriorityQueue<A> =
        singleton(a1).merge(singleton(a2).merge(empty()))

    fun <A : Comparable<A>> of(a1: A, a2: A, a3: A): PriorityQueue<A> =
        singleton(a1).merge(singleton(a2).merge(singleton(a3).merge(empty())))

    fun <A : Comparable<A>> of(a1: A, a2: A, a3: A, a4: A): PriorityQueue<A> =
        singleton(a1).merge(singleton(a2).merge(singleton(a3).merge(singleton(a4).merge(empty()))))

    fun <A : Comparable<A>> of(a1: A, a2: A, a3: A, a4: A, a5: A): PriorityQueue<A> =
        singleton(a1).merge(singleton(a2).merge(singleton(a3).merge(singleton(a4).merge(singleton(a5).merge(empty())))))

}   // PriorityQueueF
