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

import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.ListF



sealed class SplayHeap<A : Comparable<A>> {



    class Empty<A : Comparable<A>> internal constructor() : SplayHeap<A>() {
        override fun toString(): String = "Empty"
    }



    class Heap<A : Comparable<A>> internal constructor(val value: A, val left: SplayHeap<A>, val right: SplayHeap<A>) : SplayHeap<A>() {
        override fun toString(): String = "Heap(value: $value, left: $left, right: $right)"
    }



    /**
     * Is the heap empty?
     *
     * @return                  true if this heap is empty, otherwise false
     */
    fun isEmpty(): Boolean = when (this) {
        is Empty -> true
        is Heap -> false
    }   // isEmpty

    /**
     * Find the size of this heap.
     *
     * @return                  the size (number of elements) of this heap
     */
    fun size(): Int = when (this) {
        is Empty -> 0
        is Heap -> 1 + this.left.size() + this.right.size()
    }   // size

    /**
     * Insert a new element into this SplayHeap. This procedure adds the new
     *   element at the root of the tree.
     *
     * @param a                 the new element ot insert
     * @return                  the new heap containing this new element
     */
    fun insert(a: A): SplayHeap<A> {
        val (small: SplayHeap<A>, big: SplayHeap<A>) = this.partition(a)
        return Heap(a, small, big)
    }   // insert

    /**
     * Merge this SplayHeap with the given SplayHeap. When this SplayHeap is
     *   not empty we find the smaller and bigger sets from the heap parameter
     *   and make a new node merging the smaller with left of this node and
     *   merging the bigger with the right of this node.
     *
     * @param heap              the heap to merge with this heap
     * @return                  the merge of this heap and the heap argument
     */
    fun merge(heap: SplayHeap<A>): SplayHeap<A> =
            when (this) {
                is Empty -> heap
                is Heap -> {
                    val value: A = this.value
                    val left: SplayHeap<A> = this.left
                    val right: SplayHeap<A> = this.right
                    val (small: SplayHeap<A>, big: SplayHeap<A>) = heap.partition(value)
                    Heap(value, small.merge(left), big.merge(right))
                }
            }   // merge

    /**
     * Return the smallest in the heap.
     *
     * @return                  the minimum value in this heap
     */
    fun findMinimum(): A =
            when (this) {
                is Empty -> throw SplayHeapException("findMinimum: empty heap")
                is Heap -> {
                    val value: A = this.value
                    val left: SplayHeap<A> = this.left
                    when (left) {
                        is Empty -> value
                        is Heap -> left.findMinimum()
                    }
                }
            }   // findMinimum

    /**
     * Remove the smallest element in the heap.
     *
     * @return                  the heap after the minimum has been removed
     */
    fun deleteMinimum(): SplayHeap<A> =
            when (this) {
                is Empty -> throw SplayHeapException("deleteMinimum: empty heap")
                is Heap -> {
                    val value: A = this.value
                    val left: SplayHeap<A> = this.left
                    val right: SplayHeap<A> = this.right
                    when (left) {
                        is Empty -> right
                        is Heap -> {
                            val leftValue: A = left.value
                            val leftLeft: SplayHeap<A> = left.left
                            val leftRight: SplayHeap<A> = left.right
                            when (leftLeft) {
                                is Empty -> Heap(value, leftRight, right)
                                is Heap -> Heap(leftValue, leftLeft.deleteMinimum(), Heap(value, leftRight, right))
                            }
                        }
                    }
                }
            }   // deleteMinimum

    /**
     * Create a List from this SplayHeap.
     *
     * @return                  the list of values from this heap
     */
    fun toList(): List<A> {
        fun inorder(heap: SplayHeap<A>, list: List<A>): List<A> {
            return when (heap) {
                is Empty -> list
                is Heap -> inorder(heap.left, ListF.cons(heap.value, inorder(heap.right, list)))
            }
        }   // inorder

        return inorder(this, ListF.empty())
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
            @Suppress("UNCHECKED_CAST") val otherSplayHeap: SplayHeap<A> = other as SplayHeap<A>
            val thisList: List<A> = this.toList().sort{a, b -> if (a < b) -1 else if (a > b) +1 else 0}
            val otherList: List<A> = otherSplayHeap.toList().sort{a, b -> if (a < b) -1 else if (a > b) +1 else 0}
            (thisList == otherList)
        }
    }   // equals



// ---------- implementation ------------------------------

    /**
     * Partition this PairingHeap into the smaller and bigger subtrees: ones
     *   containing all the elements smaller than or equal to the pivot and
     *   one containing all the elements greater than the pivot.
     */
    private fun partition(pivot: A): Pair<SplayHeap<A>, SplayHeap<A>> {
        return when (this) {
            is Empty -> Pair(Empty(), Empty())
            is Heap -> {
                val value: A = this.value
                val left: SplayHeap<A> = this.left
                val right: SplayHeap<A> = this.right
                if (value <= pivot) {
                    when (right) {
                        is Empty -> Pair(this, Empty())
                        is Heap -> {
                            val rightValue: A = right.value
                            val rightLeft: SplayHeap<A> = right.left
                            val rightRight: SplayHeap<A> = right.right
                            if (rightValue <= pivot) {
                                val (small: SplayHeap<A>, big: SplayHeap<A>) = rightRight.partition(pivot)
                                Pair(Heap(rightValue, Heap(value, left, rightLeft), small), big)
                            } else {
                                val (small: SplayHeap<A>, big: SplayHeap<A>) = rightLeft.partition(pivot)
                                Pair(Heap(value, left, small), Heap(rightValue, big, rightRight))
                            }
                        }
                    }
                } else {
                    when (left) {
                        is Empty -> Pair(Empty(), this)
                        is Heap -> {
                            val leftValue: A = left.value
                            val leftLeft: SplayHeap<A> = left.left
                            val leftRight: SplayHeap<A> = left.right
                            if (leftValue <= pivot) {
                                val (small: SplayHeap<A>, big: SplayHeap<A>) = leftRight.partition(pivot)
                                Pair(Heap(leftValue, leftLeft, small), Heap(value, big, right))
                            } else {
                                val (small: SplayHeap<A>, big: SplayHeap<A>) = leftLeft.partition(pivot)
                                Pair(small, Heap(leftValue, big, Heap(value, leftRight, right)))
                            }
                        }
                    }
                }
            }
        }
    }   // partition

}   // SplayHeap
