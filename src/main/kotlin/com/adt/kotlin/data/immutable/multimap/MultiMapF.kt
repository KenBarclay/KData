package com.adt.kotlin.data.immutable.multimap

/**
 * A class defining an immutable multimap that can contain multiple occurrences
 *   of the same value for a given key.
 *
 * @param K                     the type of keys in the multimap
 * @param V                     the type of values in the multimap
 *
 * @author	                    Ken Barclay
 * @since                       July 2019
 */

import com.adt.kotlin.data.immutable.list.List

import com.adt.kotlin.data.immutable.map.MapF



object MultiMapF {

    /**
     * Factory constructor function.
     */
    fun <K : Comparable<K>, V : Comparable<V>> empty(): MultiMap<K, V> =
        MultiMap(MapF.empty())

    fun <K : Comparable<K>, V : Comparable<V>> of(k1: K, v1: V): MultiMap<K, V> =
        empty<K, V>().insert(k1, v1)

    fun <K : Comparable<K>, V : Comparable<V>> of(k1: K, v1: V, k2: K, v2: V): MultiMap<K, V> =
        empty<K, V>().insert(k1, v1).insert(k2, v2)

    fun <K : Comparable<K>, V : Comparable<V>> of(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V): MultiMap<K, V> =
        empty<K, V>().insert(k1, v1).insert(k2, v2).insert(k3, v3)

    fun <K : Comparable<K>, V : Comparable<V>> of(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V): MultiMap<K, V> =
        empty<K, V>().insert(k1, v1).insert(k2, v2).insert(k3, v3).insert(k4, v4)

    fun <K : Comparable<K>, V : Comparable<V>> of(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V, k5: K, v5: V): MultiMap<K, V> =
        empty<K, V>().insert(k1, v1).insert(k2, v2).insert(k3, v3).insert(k4, v4).insert(k5, v5)

    fun <K : Comparable<K>, V : Comparable<V>> of (vararg seq: Pair<K, V>): MultiMap<K, V> =
        seq.foldRight(empty()){pair: Pair<K, V>, map: MultiMap<K, V> -> map.insert(pair.first, pair.second)}



    fun <K: Comparable<K>, V : Comparable<V>> of(e1: Pair<K, V>): MultiMap<K, V> = empty<K, V>().insert(e1)

    fun <K: Comparable<K>, V : Comparable<V>> of(e1: Pair<K, V>, e2: Pair<K, V>): MultiMap<K, V> = empty<K, V>().insert(e1).insert(e2)

    fun <K: Comparable<K>, V : Comparable<V>> of(e1: Pair<K, V>, e2: Pair<K, V>, e3: Pair<K, V>): MultiMap<K, V> = empty<K, V>().insert(e1).insert(e2).insert(e3)

    fun <K: Comparable<K>, V : Comparable<V>> of(e1: Pair<K, V>, e2: Pair<K, V>, e3: Pair<K, V>, e4: Pair<K, V>): MultiMap<K, V> =
        empty<K, V>().insert(e1).insert(e2).insert(e3).insert(e4)

    fun <K: Comparable<K>, V : Comparable<V>> of(e1: Pair<K, V>, e2: Pair<K, V>, e3: Pair<K, V>, e4: Pair<K, V>, e5: Pair<K, V>): MultiMap<K, V> =
        empty<K, V>().insert(e1).insert(e2).insert(e3).insert(e4).insert(e5)



    fun <K : Comparable<K>, V : Comparable<V>> from(list: List<Pair<K, V>>): MultiMap<K, V> =
        list.foldRight(empty()){pair: Pair<K, V> -> {map: MultiMap<K, V> -> map.insert(pair.first, pair.second)}}

}   // MultiMapF
