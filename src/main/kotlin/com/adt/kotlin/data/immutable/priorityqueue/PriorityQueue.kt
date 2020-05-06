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

import com.adt.kotlin.data.immutable.heap.binomialheap.BinomialHeap
import com.adt.kotlin.data.immutable.heap.binomialheap.BinomialHeapF.empty

import com.adt.kotlin.data.immutable.list.List



class PriorityQueue<A : Comparable<A>> internal constructor(val binomialHeap: BinomialHeap<A> = empty()) {

    /**
     * Is the queue empty?
     */
    fun isEmpty(): Boolean = binomialHeap.isEmpty()

    /**
     * Find the size of this queue.
     */
    fun size(): Int = binomialHeap.size()

    /**
     * Find the head element (minimum) of this heap. Throws an exception
     *   if the queue is empty.
     */
    fun head(): A = binomialHeap.head()

    /**
     * Find the tail of this queue.
     */
    fun tail(): PriorityQueue<A> = PriorityQueue(binomialHeap.tail())

    /**
     * Find the minimum of this queue.
     */
    fun findMinimum(): A = binomialHeap.findMinimum()

    /**
     * Remove the smallest element in the queue.
     */
    fun deleteMinimum(): PriorityQueue<A> = PriorityQueue(binomialHeap.deleteMinimum())

    /**
     * Are two lists equal?
     *
     * @param other             the other list
     * @return                  true if both lists are the same; false otherwise
     */
    override fun equals(other: Any?): Boolean {
        return if (this === other)
            true
        else if (other == null || this::class.java != other::class.java)
            false
        else {
            @Suppress("UNCHECKED_CAST") val otherPriorityQueue: PriorityQueue<A> = other as PriorityQueue<A>
            (this.binomialHeap == otherPriorityQueue.binomialHeap)
        }
    }   // equals

    /**
     * Insert a new element into the queue.
     */
    fun insert(a: A): PriorityQueue<A> = PriorityQueue(binomialHeap.insert(a))

    /**
     * Merge this BinomialHeap with the given BinomialHeap.
     */
    fun merge(queue: PriorityQueue<A>): PriorityQueue<A> = PriorityQueue(this.binomialHeap.merge(queue.binomialHeap))

    /**
     * Create a List from this PriorityQueue.
     */
    fun toList(): List<A> = this.binomialHeap.toList()

}   // PriorityQueue
