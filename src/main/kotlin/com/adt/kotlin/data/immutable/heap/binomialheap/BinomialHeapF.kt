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

import com.adt.kotlin.data.immutable.heap.binomialheap.BinomialHeap.BinomialTree
import com.adt.kotlin.data.immutable.heap.binomialheap.BinomialHeap.Empty
import com.adt.kotlin.data.immutable.heap.binomialheap.BinomialHeap.Heap

import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.List.Nil
import com.adt.kotlin.data.immutable.list.List.Cons
import com.adt.kotlin.data.immutable.list.ListF



object BinomialHeapF {

    /**
     * Create an empty heap.
     */
    fun <A : Comparable<A>> empty(): BinomialHeap<A> = Empty()

    /**
     * Factory constructor function.
     */
    fun <A : Comparable<A>> heap(nodes: List<BinomialTree<A>>): BinomialHeap<A> =
        Heap(nodes)

    /**
     * Create a heap containing a single element.
     */
    fun <A : Comparable<A>> singleton(a: A): BinomialHeap<A> = Heap(ListF.singleton(BinomialTree(0, a, ListF.empty())))

    /**
     * Create a BinomialHeap from a list of elements.
     */
    fun <A : Comparable<A>> from(list: List<A>): BinomialHeap<A> =
        list.map{a -> singleton(a)}.foldLeft(empty()){bHeap: BinomialHeap<A> -> {hHeap: BinomialHeap<A> -> bHeap.merge(hHeap)}}

    fun <A : Comparable<A>> from(vararg seq: A): BinomialHeap<A> =
        seq.map{a -> singleton(a)}.fold(empty()){bHeap: BinomialHeap<A>, hHeap: BinomialHeap<A> -> bHeap.merge(hHeap)}

    /**
     * Create a BinomialHeap from a sequence of elements.
     */
    fun <A : Comparable<A>> of(vararg seq: A): BinomialHeap<A> =
        seq.map{a -> singleton(a)}.fold(empty()){bHeap: BinomialHeap<A>, hHeap: BinomialHeap<A> -> bHeap.merge(hHeap)}

    /**
     * Factory functions to create the base instances from a series of none or more elements.
     */
    fun <A : Comparable<A>> of(): BinomialHeap<A> = empty()

    fun <A : Comparable<A>> of(a1: A): BinomialHeap<A> = singleton(a1).merge(empty())

    fun <A : Comparable<A>> of(a1: A, a2: A): BinomialHeap<A> =
        singleton(a1).merge(singleton(a2).merge(empty()))

    fun <A : Comparable<A>> of(a1: A, a2: A, a3: A): BinomialHeap<A> =
        singleton(a1).merge(singleton(a2).merge(singleton(a3).merge(empty())))

    fun <A : Comparable<A>> of(a1: A, a2: A, a3: A, a4: A): BinomialHeap<A> =
        singleton(a1).merge(singleton(a2).merge(singleton(a3).merge(singleton(a4).merge(empty()))))

    fun <A : Comparable<A>> of(a1: A, a2: A, a3: A, a4: A, a5: A): BinomialHeap<A> =
        singleton(a1).merge(singleton(a2).merge(singleton(a3).merge(singleton(a4).merge(singleton(a5).merge(empty())))))

}   // BinomialHeapF
