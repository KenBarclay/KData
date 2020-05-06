package com.adt.kotlin.data.immutable.heap.binomialheap

/**
 * A binomial heap is an elegant data structure for supporting ordered collections
 *   of values efficiently. The crucial property of a binomial heap is that
 *   operations such as these execute in a time that is proportional to the
 *   logarithm of the size of the collection in the worst case. It thus avoids
 *   the problems associated with some other branching data structures, e.g.
 *   binary trees, where an imbalance in the structure can cause it to behave
 *   more like a linked list.
 *
 * Author:	                    Ken Barclay
 * Date:	                    September 2019
 */

import com.adt.kotlin.data.immutable.heap.binomialheap.BinomialHeapF.singleton
import com.adt.kotlin.data.immutable.list.*
import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.List.Nil
import com.adt.kotlin.data.immutable.list.List.Cons



sealed class BinomialHeap<A : Comparable<A>> {



    class Empty<A : Comparable<A>> internal constructor() : BinomialHeap<A>() {
        override fun toString(): String = "Empty"
    }



    class Heap<A : Comparable<A>> internal constructor(val nodes: List<BinomialTree<A>>) : BinomialHeap<A>() {
        override fun toString(): String = "Heap(nodes: $nodes)"
    }



    /**
     * Is the heap empty?
     */
    fun isEmpty(): Boolean = when (this) {
        is Empty -> true
        is Heap -> false
    }   // isEmpty

    /**
     * Find the size of this heap.
     */
    fun size(): Int = when (this) {
        is Empty -> 0
        is Heap -> this.nodes.foldLeft(0){acc -> {hn -> acc + hn.size()}}
    }   // size

    /**
     * Find the head element (minimum) of this heap. Throws an exception
     *   if the heap is empty.
     */
    fun head(): A = when (this) {
        is Empty -> throw BinomialHeapException("head: empty node")
        is Heap -> minimum(this.nodes).value
    }   // head

    /**
     * Find the tail of this heap.
     */
    fun tail(): BinomialHeap<A> =
        when (this) {
            is Empty -> throw BinomialHeapException("tail: empty nodes")
            is Heap -> {
                val (_: BinomialTree<A>, ts: List<BinomialTree<A>>) = removeMinimumTree(this.nodes)
                Heap(ts)
            }
        }   // tail

    /**
     * Find the minimum of this heap.
     */
    fun findMinimum(): A =
        when (this) {
            is Empty -> throw BinomialHeapException("findMinimum: empty heap")
            is Heap -> {
                val (bt: BinomialTree<A>, _: List<BinomialTree<A>>) = removeMinimumTree(nodes)
                bt.value
            }
        }   // findMinimum

    /**
     * Remove the smallest element in the heap.
     */
    fun deleteMinimum(): BinomialHeap<A> =
        when (this) {
            is Empty -> throw BinomialHeapException("deleteMinimum: empty heap")
            is Heap -> {
                val (bt: BinomialTree<A>, ts: List<BinomialTree<A>>) = removeMinimumTree(nodes)
                Heap(merge(bt.nodes.reverse(), ts))
            }
        }   // deleteMinimum

    /**
     * Are two heaps equal?
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
            @Suppress("UNCHECKED_CAST") val otherBinomialHeap: BinomialHeap<A> = other as BinomialHeap<A>
            val thisList: List<A> = this.toList().sort{a, b -> a.compareTo(b)}
            val otherList: List<A> = otherBinomialHeap.toList().sort{a, b -> a.compareTo(b)}
            (thisList == otherList)
        }
    }   // equals

    /**
     * Insert a new element into the heap. Once the merging function
     *   has been defined it is easy to implement a function for
     *   inserting a new value into a heap: simply form a singleton
     *   heap comprising a singleton tree of rank 0, made using the
     *   given value, and merge this with the given heap.
     */
    fun insert(a: A): BinomialHeap<A> = this.merge(singleton(a))

    /**
     * Merge this BinomialHeap with the given BinomialHeap. Each node
     *   in the two original heaps occurs somewhere in the merged heap
     *   and the merged heap itself satisfies all the properties of a
     *   binomial heap. In particular, the minimum element in the merged
     *   heap is the smallest of the minimum elements in the original
     *   heaps and thus appears in the root node of one of the trees
     *   making up the merged heap.
     */
    fun merge(heap: BinomialHeap<A>): BinomialHeap<A> =
        when (this) {
            is Empty -> heap
            is Heap -> when (heap) {
                is Empty -> this
                is Heap -> Heap(merge(this.nodes, heap.nodes))
            }
        }   // merge

    /**
     * Create a List from this BinomialHeap.
     */
    fun toList(): List<A> = when (this) {
        is Empty -> ListF.empty()
        is Heap -> this.nodes.foldLeft(ListF.empty()){list -> {bt -> list.append(bt.toList())}}
    }   // toList



    class BinomialTree<A : Comparable<A>>(val rank: Int, val value: A, val nodes: List<BinomialTree<A>>) : Comparable<BinomialTree<A>> {
        override fun compareTo(other: BinomialTree<A>): Int = value.compareTo(other.value)
        override fun toString(): String = "BinomialTree(rank: $rank, value: $value, nodes: $nodes)"

        fun link(tree: BinomialTree<A>): BinomialTree<A> =
            if (this.value <= tree.value)
                BinomialTree(1 + this.rank, this.value, ListF.cons(tree, this.nodes))
            else
                BinomialTree(1 + this.rank, tree.value, ListF.cons(this, tree.nodes))

        fun size(): Int = 1 + nodes.foldLeft(0){acc -> {bt -> acc + bt.size()}}

        fun toList(): List<A> {
            fun recToList(node: BinomialTree<A>): List<A> =
                ListF.cons(node.value, node.nodes.foldLeft(ListF.empty()){list -> {bt -> list.append(bt.toList())}})

            return recToList(this)
        }
    }   // BinomialTree



// ---------- implementation ------------------------------

    private fun minimum(nodes: List<BinomialTree<A>>): BinomialTree<A> =
        when (nodes) {
            is Nil -> throw BinomialHeapException("minimum: empty nodes")
            is Cons -> nodes.foldLeft(nodes[0]){min -> {hNode -> if (hNode < min) hNode else min}}
        }   // minimum

    private fun insertTree(node: BinomialTree<A>, nodes: List<BinomialTree<A>>): List<BinomialTree<A>> {
        fun recInsertTree(node: BinomialTree<A>, nodes: List<BinomialTree<A>>): List<BinomialTree<A>> =
            when (nodes) {
                is Nil -> ListF.singleton(node)
                is Cons -> {
                    val head: BinomialTree<A> = nodes.head()
                    val tail: List<BinomialTree<A>> = nodes.tail()
                    if (node.rank < head.rank)
                        ListF.cons(node, nodes)
                    else
                        recInsertTree(node.link(head), tail)
                }
            }   // recInsertTree

        return recInsertTree(node, nodes)
    }   // insertTree

    private fun insert(value: A, nodes: List<BinomialTree<A>>): List<BinomialTree<A>> =
        insertTree(BinomialTree(0, value, ListF.empty()), nodes)

    private fun merge(nodes1: List<BinomialTree<A>>, nodes2: List<BinomialTree<A>>): List<BinomialTree<A>> {
        fun recMerge(nodes1: List<BinomialTree<A>>, nodes2: List<BinomialTree<A>>): List<BinomialTree<A>> =
            when (nodes1) {
                is Nil -> nodes2
                is Cons -> when (nodes2) {
                    is Nil -> nodes1
                    is Cons -> {
                        val head1: BinomialTree<A> = nodes1.head()
                        val tail1: List<BinomialTree<A>> = nodes1.tail()
                        val head2: BinomialTree<A> = nodes2.head()
                        val tail2: List<BinomialTree<A>> = nodes2.tail()
                        if (head1.rank < head2.rank)
                            ListF.cons(head1, recMerge(tail1, nodes2))
                        else if(head2.rank < head1.rank)
                            ListF.cons(head2, recMerge(nodes1, tail2))
                        else
                            insertTree(head1.link(head2), recMerge(tail1, tail2))
                    }
                }
            }   // recMerge

        return recMerge(nodes1, nodes2)
    }   // merge

    private fun removeMinimumTree(nodes: List<BinomialTree<A>>): Pair<BinomialTree<A>, List<BinomialTree<A>>> {
        fun recRemoveMinimumTree(nodes: List<BinomialTree<A>>): Pair<BinomialTree<A>, List<BinomialTree<A>>> =
            when (nodes) {
                is Nil -> throw BinomialHeapException("removeMinimumTree: empty trees")
                is Cons -> {
                    if (nodes.size() == 1)
                        Pair(nodes.head(), ListF.empty())
                    else {
                        val head: BinomialTree<A> = nodes.head()
                        val tail: List<BinomialTree<A>> = nodes.tail()
                        val (bt: BinomialTree<A>, ts: List<BinomialTree<A>>) = recRemoveMinimumTree(tail)
                        if (head.value <= bt.value)
                            Pair(head, tail)
                        else
                            Pair(bt, ListF.cons(head, ts))
                    }
                }
            }   // recRemoveMinimumTree

        return recRemoveMinimumTree(nodes)
    }   // removeMinimumTree

}   // BinomialHeap
