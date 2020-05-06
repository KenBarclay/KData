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

import com.adt.kotlin.data.immutable.heap.leftistheap.LeftistHeapF.empty

import com.adt.kotlin.data.immutable.list.*
import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.ListF
import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.OptionF.none
import com.adt.kotlin.data.immutable.option.OptionF.some



sealed class LeftistHeap<P : Comparable<P>, A> {



    class Empty<P : Comparable<P>, A> internal constructor() : LeftistHeap<P, A>() {
        override fun toString(): String = "Empty"
    }

    class Heap<P : Comparable<P>, A> internal constructor(val rank: Int, val size: Int, val priority: P, val value: A, val left: LeftistHeap<P, A>, val right: LeftistHeap<P, A>) : LeftistHeap<P, A>() {
        override fun toString(): String = "Heap(rank: $rank, size: $size, priority: $priority, value: $value, left: $left, right: $right)"
    }



    /**
     * Is this heap empty?
     *
     * @return                  true if this heap is empty, false otherwise
     */
    fun isEmpty(): Boolean = when (this) {
        is Empty -> true
        is Heap -> false
    }   // isEmpty

    /**
     * Find the rank of this heap.
     *
     * @return                  the rank of this heap
     */
    fun rank(): Int = when (this) {
        is Empty -> 0
        is Heap -> this.rank
    }   // rank

    /**
     * Find the size of this heap.
     */
    fun size(): Int = when (this) {
        is Empty -> 0
        is Heap -> this.size
    }   // size

    /**
     * Return the smallest in the heap.
     *
     * @return                  the minimum value in this heap
     */
    fun findMinimum(): A {
        return when (this) {
            is Empty -> throw LeftistHeapException("findMinimum: empty heap")
            is Heap -> this.value
        }
    }   // findMinimum

    /**
     * Remove the smallest element in the heap.
     *
     * @return                  the heap after the minimum has been removed
     */
    fun deleteMinimum(): LeftistHeap<P, A> {
        return when (this) {
            is Empty -> throw LeftistHeapException("deleteMinimum: empty heap")
            is Heap -> this.left.merge(this.right)
        }
    }   // deleteMinimum

    /**
     * Merge this heap with the heap argument.
     *
     * @param heap              the other heap to merge this heap with
     * @return                  the merged heap
     */
    fun merge(heap: LeftistHeap<P, A>): LeftistHeap<P, A> {
        return when (this) {
            is Empty -> heap
            is Heap -> when (heap) {
                is Empty -> this
                is Heap -> {
                    val priorityThis: P = this.priority
                    val priorityHeap: P = heap.priority
                    if (priorityThis < priorityHeap)
                        make(priorityThis, this.value, this.left, this.right.merge(heap))
                    else
                        make(priorityHeap, heap.value, heap.left, heap.right.merge(this))
                }
            }
        }
    }   // merge

    /**
     * Insert a priority/value pair into this heap with the priority
     *   less than or equal to all other priorities in this heap. If
     *   condition is not me then an exception is thrown.
     *
     * @param priority          the priority for this new value
     * @param value             the new value
     * @return                  a new heap with the new element
     */
    fun insert(priority: P, value: A): LeftistHeap<P, A> {
        val check: Boolean = this.view().option(true){triple -> priority <= triple.first}
        return if (check)
            Heap(1, 1 + this.size(), priority, value, this, empty())
        else
            throw LeftistHeapException("insert: check fails")
    }   // cons

    /**
     * Partition this LeftistHeap into two LeftistHeaps. All priority-value pairs
     *   in the first return LeftistHeap satisfy the predicate. All those in the
     *   second returned LeftistHeap do not satisfy the predicate.
     *
     * @param predicate         the predicate used for the partitioning
     * @return                  the pair of heaps
     */
    fun partition(predicate: (P, A) -> Boolean): Pair<LeftistHeap<P, A>, LeftistHeap<P, A>> = when (this) {
        is Empty -> Pair(empty(), empty())
        is Heap -> {
            val priority: P = this.priority
            val value: A = this.value
            val (left1: LeftistHeap<P, A>, left2: LeftistHeap<P, A>) = this.left.partition(predicate)
            val (right1: LeftistHeap<P, A>, right2: LeftistHeap<P, A>) = this.right.partition(predicate)
            if (predicate(priority, value))
                Pair(make(priority, value, left1, right1), left2.merge(right2))
            else
                Pair(left1.merge(right1), make(priority, value, left2, right2))
        }
    }   // partition

    /**
     * Return a List of the lowest n priority-value pairs of this LeftistHeap
     *   in ascending order of priority, and this LeftistHeap with all those
     *   elements removed.
     *
     * @param n                 the number of the lowest priority pairs to be collected in a list
     * @return                  the pair comprising the list and the remaining heap
     */
    fun splitAt(n: Int): Pair<List<Pair<P, A>>, LeftistHeap<P, A>> =
        if (n > 0) {
            val view: Option<Triple<P, A, LeftistHeap<P, A>>> = this.view()
            when (view) {
                is Option.None -> Pair(ListF.empty(), empty())
                is Option.Some -> {
                    val (xs: List<Pair<P, A>>, heap: LeftistHeap<P, A>) = splitAt(n - 1)
                    val priority: P = view.value.first
                    val value: A = view.value.second
                    Pair(ListF.cons(Pair(priority, value), xs), heap)
                }
            }
        } else
            Pair(ListF.empty(), this)

    /**
     * Return the longest prefix of priority-value pairs from this LeftistHeap
     *   in ascending order of priority that satisfy the predicate and this
     *   LeftistHeap with those elements removed.
     *
     * @param predicate         the predicate used in the partitioning
     * @return                  the pair comprising the list and the remaining heap
     */
    fun span(predicate: (P, A) -> Boolean): Pair<List<Pair<P, A>>, LeftistHeap<P, A>> {
        val view: Option<Triple<P, A, LeftistHeap<P, A>>> = this.view()
        return when (view) {
            is Option.None -> Pair(ListF.empty(), empty())
            is Option.Some -> {
                val priority: P = view.value.first
                val value: A = view.value.second
                val hs: LeftistHeap<P, A> = view.value.third
                if (predicate(priority, value)) {
                    val (xs: List<Pair<P, A>>, heap: LeftistHeap<P, A>) = hs.span(predicate)
                    Pair(ListF.cons(Pair(priority, value), xs), heap)
                } else
                    Pair(ListF.empty(), this)
            }
        }
    }   // span

    /**
     * Return a List of the priority-value pairs from this LeftistHeap in no
     *   specific order.
     *
     * @return                  the list of priority/value pairs from this heap
     */
    fun toList(): List<Pair<P, A>> = when (this) {
        is Empty -> ListF.empty()
        is Heap -> {
            val left: LeftistHeap<P, A> = this.left
            val right: LeftistHeap<P, A> = this.right
            if (right.size() < left.size())
                ListF.cons(Pair(this.priority, this.value), right.toList().append(left.toList()))
            else
                ListF.cons(Pair(this.priority, this.value), left.toList().append(right.toList()))
        }
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
            @Suppress("UNCHECKED_CAST") val otherLeftistHeap: LeftistHeap<P, A> = other as LeftistHeap<P, A>
            val thisList: List<Pair<P, A>> = this.toList().sort{pair1, pair2 -> if (pair2.first < pair1.first) -1 else if (pair2.first > pair1.first) +1 else 0}
            val otherList: List<Pair<P, A>> = otherLeftistHeap.toList().sort{pair1, pair2 -> if (pair2.first < pair1.first) -1 else if (pair2.first > pair1.first) +1 else 0}
            (thisList == otherList)
        }
    }   // equals



// ---------- implementation ------------------------------

    /**
     * Build a LeftistHeap from a priority, a rank and two LeftistHeaps. The
     *   priority has to be less than or equal all priorities in both heap
     *   parameters.
     */
    private fun make(priority: P, value: A, left: LeftistHeap<P, A>, right: LeftistHeap<P, A>): LeftistHeap<P, A> {
        val rankLeft: Int = left.rank()
        val rankRight: Int = right.rank()
        val size: Int = 1 + left.size() + right.size()
        return if (rankLeft > rankRight)
            Heap(1 + rankRight, size, priority, value, left, right)
        else
            Heap(1 + rankLeft, size, priority, value, right, left)
    }   // make

    private fun view(): Option<Triple<P, A, LeftistHeap<P, A>>> = when(this) {
        is Empty -> none()
        is Heap -> some(Triple(this.priority, this.value, this.left.merge(this.right)))
    }   // view

}   // LeftistHeap
