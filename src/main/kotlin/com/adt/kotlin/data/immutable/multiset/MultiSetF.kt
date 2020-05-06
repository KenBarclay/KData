package com.adt.kotlin.data.immutable.multiset

/**
 * A class defining an immutable multiset that can contain multiple occurrences
 *   of the same value.
 *
 * @param A                     the type of elements in the multiset
 *
 * @author	                    Ken Barclay
 * @since                       July 2019
 */

import com.adt.kotlin.data.immutable.map.MapF

import com.adt.kotlin.data.immutable.list.List



object MultiSetF {

    /**
     * Create an empty multiset.
     */
    @Suppress("UNCHECKED_CAST")
    fun <A : Comparable<A>> empty(): MultiSet<A> = MultiSet(MapF.empty())

    /**
     * Create a set with a single element.
     */
    fun <A : Comparable<A>> singleton(a: A): MultiSet<A> = MultiSet(MapF.singleton(a, 1))

    /**
     * Create a multiset with one, two, etc values.
     */
    fun <A : Comparable<A>> of(a1: A): MultiSet<A> = singleton(a1)

    fun <A : Comparable<A>> of(a1: A, a2: A): MultiSet<A> = singleton(a1).insert(a2)

    fun <A : Comparable<A>> of(a1: A, a2: A, a3: A): MultiSet<A> = singleton(a1).insert(a2).insert(a3)

    fun <A : Comparable<A>> of(a1: A, a2: A, a3: A, a4: A): MultiSet<A> = singleton(a1).insert(a2).insert(a3).insert(a4)

    fun <A : Comparable<A>> of(a1: A, a2: A, a3: A, a4: A, a5: A): MultiSet<A> = singleton(a1).insert(a2).insert(a3).insert(a4).insert(a5)

    fun <A : Comparable<A>> of(vararg a: A): MultiSet<A> = from(*a)



    /**
     * Convert a variable-length parameter series into a multiset.
     *
     * Examples:
     *   fromSequence(Jessie, John, Ken) = {Jessie, John, Ken}
     *   fromSequence() = {}
     *
     * @param seq                   variable-length parameter series
     * @return                      set of the given values
     */
    fun <A : Comparable<A>> from(vararg seq: A): MultiSet<A> =
            seq.fold(empty()){s, a -> s.insert(a)}

    /**
     * Convert a variable-length immutable list into a multiset.
     *
     * Examples:
     *   fromList([Jessie, John, Ken]) = {Jessie, John, Ken}
     *   fromList([]) = {}
     *
     * @param ls                    variable-length list
     * @return                      multiset of the given values
     */
    fun <A : Comparable<A>> from(ls: List<A>): MultiSet<A> {
        return ls.foldRight(empty()) { a -> { set: MultiSet<A> -> set.insert(a) } }
    }

    /**
     * Convert an immutable multiset to an immutable list.
     *
     * @param set                   existing immutable multiset
     * @return                      an immutable list
     */
    fun <A : Comparable<A>> toList(set: MultiSet<A>): List<A> = set.elements.keyList()

}   // MultiSetF
