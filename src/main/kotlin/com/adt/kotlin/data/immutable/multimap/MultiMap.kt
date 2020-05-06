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

import com.adt.kotlin.data.immutable.map.Map
import com.adt.kotlin.data.immutable.map.*

import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.ListF.empty
import com.adt.kotlin.data.immutable.list.append

import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.Option.None
import com.adt.kotlin.data.immutable.option.Option.Some

import com.adt.kotlin.data.immutable.set.Set
import com.adt.kotlin.data.immutable.set.SetF

import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3



class MultiMap<K : Comparable<K>, V : Comparable<V>> internal constructor(val elements: Map<K, Set<V>>) {

    /**
     * Update the value at the key if present. If the key is not present
     *   then the original multimap is returned.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.adjust(Ken){v -> v + v} = <[Jessie: 22, John: 31, Ken: 50]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.adjust(Irene){v -> v + v} = <[Jessie: 22, John: 31, Ken: 25]>
     *
     * @param key     		    new key
     * @param f			        update function
     * @return        		    updated multimap
     */
    fun adjust(key: K, f: (Set<V>) -> Set<V>): MultiMap<K, V> = MultiMap(elements.adjust(key, f))

    /**
     * Update the value at the key if present. If the key is not present
     *   then the original multimap is returned.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.adjustWithKey(Ken){k -> {v -> v + v}} = <[Jessie: 22, John: 31, Ken: 50]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.adjustWithKey(Irene){k -> {v -> v + v}} = <[Jessie: 22, John: 31, Ken: 25]>
     *
     * @param key     		    new key
     * @param f			        curried update function
     * @return        		    updated multimap
     */
    fun adjustWithKey(key: K, f: (K) -> (Set<V>) -> Set<V>): MultiMap<K, V> = MultiMap(elements.adjustWithKey(key, f))

    fun adjustWithKey(key: K, f: (K, Set<V>) -> Set<V>): MultiMap<K, V> = this.adjustWithKey(key, C2(f))

    /**
     * Determine if this multimap contains the key determined by the predicate.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.contains{name -> (name == John)} = true
     *   {Jessie: 22, John: 31, Ken: 25}.contains{name -> (name == Irene)} = false
     *   {}.contains{name -> (name == John)} = false
     *
     * @param predicate         search predicate
     * @return                  true if search element is present, false otherwise
     */
    fun contains(predicate: (K) -> Boolean): Boolean = elements.contains(predicate)

    /**
     * Determine if the multimap contains the given key.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.containsKey("Ken") = true
     *   {Jessie: 22, John: 31, Ken: 25}.containsKey("Irene") = false
     *   {}.containsKey("Ken") = false
     *
     * @param key               search key
     * @return                  true if the multimap contains this key
     */
    fun contains(key: K): Boolean = elements.contains(key)

    /**
     * Delete the value from the multimap. When the value is not a member
     *   of the multimap, the original multimap is returned.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.delete(Ken) = {Jessie: 22, John: 31}
     *   {Jessie: 22, John: 31, Ken: 25}.delete(Irene) = {Jessie: 22, John: 31, Ken: 25}
     *   {}.delete(Ken) = {}
     *
     * @param key               key to be removed
     * @return                  new multimap without the given key
     */
    fun delete(key: K): MultiMap<K, V> = MultiMap(elements.delete(key))

    /**
     * The difference of two multimap. Returns elements from this not in the
     *   right multimap.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.difference({Irene: 25, Dawn: 31}) = {Jessie: 22, John: 31, Ken: 25}
     *   {Jessie: 22, John: 31, Ken: 25}.difference({John: 41, Ken: 35}) = {Jessie: 22}
     *   {Jessie: 22, John: 31, Ken: 25}.difference({}) = {Jessie: 22, John: 31, Ken: 25}
     *   {}.difference({Jessie: 22, John: 31, Ken: 25}) = {}
     *
     * @param map               existing multimap
     * @return                  the difference of this multimap and the given multimap
     */
    fun difference(map: MultiMap<K, V>): MultiMap<K, V> = MultiMap(elements.difference(map.elements))

    /**
     * Are two maps equal?
     *
     * @param other             the other map
     * @return                  true if both maps are the same; false otherwise
     */
    override fun equals(other: Any?): Boolean {
        return if (this === other)
            true
        else if (other == null || this::class.java != other::class.java)
            false
        else {
            @Suppress("UNCHECKED_CAST") val otherMap: MultiMap<K, V> = other as MultiMap<K, V>
            (this.elements == otherMap.elements)
        }
    }   // equals

    /**
     * Filter all key/values that satisfy the predicate.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.filterWithKey{name -> {age -> name.startsWith(J) && age > 30}} = {John: 31}
     *
     * @param predicate     	curried predicate function on the key and value types
     * @return              	multimap of selected elements
     */
    fun filterWithKey(predicate: (K) -> (Set<V>) -> Boolean): MultiMap<K, V> = MultiMap(elements.filterWithKey(predicate))

    fun filterWithKey(predicate: (K, Set<V>) -> Boolean): MultiMap<K, V> = this.filterWithKey(C2(predicate))

    /**
     * The find function takes a predicate and returns the first key in
     *   the multimap matching the predicate, or none if there is no
     *   such element.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.find{name -> name.startsWith(J)} = some(John)
     *   {Jessie: 22, John: 31, Ken: 25}.find{name -> name.charAt(0) >= A} = some(John)
     *   {Jessie: 22, John: 31, Ken: 25}.find{name -> name.charAt(0) >= Z} = none
     *   {}.find{name -> name.startsWith(J)} = none
     *
     * @param predicate         criteria
     * @return                  matching element, if found
     */
    fun find(predicate: (K) -> Boolean): Option<K> = elements.find(predicate)

    /**
     * foldLeft is a higher-order function that folds a left associative binary
     *   function into the values of a multimap.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.foldLeft(0){res -> {age -> res + age}} = 78
     *   {}.foldLeft(0){res -> {age -> res + age}} = 0
     *   {Jessie: 22, John: 31, Ken: 25}.foldLeft([]){res -> {age -> res.append(age)}} = [22, 31, 25]
     *
     * @param e           	    initial value
     * @param f         		curried binary function
     * @return            	    folded result
     */
    fun <W> foldLeft(e: W, f: (W) -> (Set<V>) -> W): W = elements.foldLeft(e, f)

    fun <W> foldLeft(e: W, f: (W, Set<V>) -> W): W = this.foldLeft(e, C2(f))

    /**
     * foldLeftWithKey is a higher-order function that folds a left associative binary
     *   function into a multimap.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.foldLeftWithKey(0){res -> {name -> {age -> res + name.length() + age}}} = 91
     *   {}.foldLeftWithKey(0){res -> {name -> {age -> res + name.length() + age}}} = 0
     *
     * @param e           	    initial value
     * @param f         		curried binary function
     * @return            	    folded result
     */
    fun <W> foldLeftWithKey(e: W, f: (W) -> (K) -> (Set<V>) -> W): W = elements.foldLeftWithKey(e, f)

    fun <W> foldLeftWithKey(e: W, f: (W, K, Set<V>) -> W): W = this.foldLeftWithKey(e, C3(f))

    /**
     * foldRight is a higher-order function that folds a right associative binary
     *   function into the values of a multimap.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.foldRight(0){age -> {res -> res + age}} = 78
     *   {}.foldRight(0){age -> {res -> res + age}} = 0
     *   {Jessie: 22, John: 31, Ken: 25}.foldRight([]){age -> {res -> res.append(age)}} = [25, 31, 22]
     *
     * @param e           	    initial value
     * @param f         		curried binary function
     * @return            	    folded result
     */
    fun <W> foldRight(e: W, f: (Set<V>) -> (W) -> W) : W = elements.foldRight(e, f)

    fun <W> foldRight(e: W, f: (Set<V>, W) -> W) : W = this.foldRight(e, C2(f))

    /**
     * foldRightWithKey is a higher-order function that folds a right associative binary
     *   function into a multimap.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldRightWithKey(0){name -> {age -> {res -> res + name.length() + age}}} = 91
     *   <[]>.foldRightWithKey(0){name -> {age -> {res -> res + name.length() + age}}} = 0
     *
     * @param e           	    initial value
     * @param f         		curried binary function
     * @return            	    folded result
     */
    fun <W> foldRightWithKey(e: W, f: (K) -> (Set<V>) -> (W) -> W): W = elements.foldRightWithKey(e, f)

    fun <W> foldRightWithKey(e: W, f: (K, Set<V>, W) -> W): W = this.foldRightWithKey(e, C3(f))

    /**
     * Insert a new key/value pair in the multimap. If the key is already present in
     *   the multimap, then the value is included.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.insert(Irene, 99) = {Jessie: 22, John: 31, Irene: 99, Ken: 25}
     *   {Jessie: 22, John: 31, Ken: 25}.insert(Ken, 99) = {Jessie: 22, John: 31, Ken: 99}
     *   {}.insert(Ken, 99) = {Ken: 99}
     *
     * @param key               new key
     * @param value             matching value
     * @return                  new multimap with new key/value pair
     */
    fun insert(key: K, value: V): MultiMap<K, V> {
        val op: Option<Set<V>> = elements.lookUpKey(key)
        return when (op) {
            is None -> MultiMap(elements.insert(key, SetF.singleton(value)))
            is Some -> MultiMap(elements.insert(key, op.value.insert(value)))
        }
    }   // insert

    fun insert(e: Pair<K, V>): MultiMap<K, V> = this.insert(e.first, e.second)

    /**
     * Insert a new key/value pair in the multimap. If the key is absent then the
     *   the key/value pair are added to the multimap. If the key does exist then
     *   the pair key/f(newValue)(oldValue) is inserted.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.insertWith(Ken, 100){newV -> {oldV -> oldV + newV}} = {Jessie: 22, John: 31, Ken: 125}
     *   {Jessie: 22, John: 31, Ken: 25}.insertWith(Irene, 100){newV -> {oldV -> oldV + newV}} = {Jessie: 22, John: 31, Irene: 100, Ken: 25}
     *
     * @param key     		    new key
     * @param value   		    new value associated with the key
     * @param f			        curried combining function
     * @return        		    updated multimap
     */
    fun insertWith(key: K, value: Set<V>, f: (Set<V>) -> (Set<V>) -> Set<V>): MultiMap<K, V> = MultiMap(elements.insertWith(key, value, f))

    /**
     * Insert a new key/value pair in the multimap. If the key is absent then the
     *   the key/value pair are added to the multimap. If the key does exist then
     *   the pair key/f(key)(newValue)(oldValue) is inserted.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.insertWithKey(Ken, 100){k -> {newV -> {oldV -> k.length + oldV + newV}}} = {Jessie: 22, John: 31, Ken: 128}
     *   {Jessie: 22, John: 31, Ken: 25}.insertWithKey(Irene, 100){k -> {newV -> {oldV -> k.length + oldV + newV}}} = {Jessie: 22, John: 31, Irene: 100, Ken: 128}
     *
     * @param key     		    new key
     * @param value   		    new value associated with the key
     * @param f			        curried combining function
     * @return        		    updated multimap
     */
    fun insertWithKey(key: K, value: Set<V>, f: (K) -> (Set<V>) -> (Set<V>) -> Set<V>): MultiMap<K, V> = MultiMap(elements.insertWithKey(key, value, f))

    fun insertWithKey(key: K, value: Set<V>, f: (K, Set<V>, Set<V>) -> Set<V>): MultiMap<K, V> = this.insertWithKey(key, value, C3(f))

    fun insertWith(key: K, value: Set<V>, f: (Set<V>, Set<V>) -> Set<V>): MultiMap<K, V> = this.insertWith(key, value, C2(f))

    /**
     * The intersection of two multimaps. Returns data from this for keys
     *   existing in both multimaps.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.intersection(<[Irene: 25, Dawn: 31]>) = {}
     *   {Jessie: 22, John: 31, Ken: 25}.intersection(<[John: 41, Ken: 35]>) = <[John: 31, Ken: 25]>
     *   {Jessie: 22, John: 31, Ken: 25}.intersection({}) = {}
     *   {}.intersection({Jessie: 22, John: 31, Ken: 25}) = {}
     *
     * @param map               existing multimap
     * @return                  the intersection of this multimap and the given multimap
     */
    fun intersection(map: MultiMap<K, V>): MultiMap<K, V> = MultiMap(elements.intersection(map.elements))

    /**
     * Test whether the multimap is empty.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.isEmpty() = false
     *   {}.isEmpty() = true
     *
     * @return                  true if the multimap contains zero elements
     */
    fun isEmpty(): Boolean = (size() == 0)

    /**
     * Is this a proper submap? (ie. a submap but not equal).
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.isProperSubmapOf({Jessie: 22, John: 31, Ken: 25}) = false
     *   {}.isProperSubmapOf({Jessie: 22, John: 31, Ken: 25}) = true
     *   {Jessie: 22, John: 31, Ken: 25}.isProperSubmapOf({}) = false
     *   {}.isProperSubmapOf({}) = false
     *
     * @param map               existing multimap
     * @return                  true if this multimap is a proper submap of the given multimap
     */
    fun isProperSubmapOf(map: MultiMap<K, V>): Boolean = elements.isProperSubmapOf(map.elements)

    /**
     * Is this a sub-map of right?
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.isSubmapOf({Jessie: 22, John: 31, Ken: 25}) = true
     *   {}.isSubmapOf({Jessie: 22, John: 31, Ken: 25}) = true
     *   {Jessie: 22, John: 31, Ken: 25}.isSubmapOf({}) = false
     *   {}.isSubmapOf({}) = true
     *
     * @param map               existing multimap
     * @return                  true if this multimap is a submap of the given multimap
     */
    fun isSubmapOf(map: MultiMap<K, V>): Boolean = elements.isSubmapOf(map.elements)

    /**
     * Returns a List view of the keys contained in this multimap.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.keyList() = [Jessie, John, Ken]
     *   {.keyList() = []
     *
     * @return    		        the keys for this multimap
     */
    fun keyList(): List<K> = elements.keyList()

    /**
     * Obtains the size of a multimap.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.length() = 3
     *   {}.length() = 0
     *
     * @return                  the number of elements in the multimap
     */
    fun length(): Int = size()

    /**
     * Look up the given key in the multimap. Return value if present, otherwise
     *   throw an exception.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.lookUp("Ken") = 25
     *   {Jessie: 22, John: 31, Ken: 25}.lookUp("Irene") = exception
     *   {}.lookUp("Ken") = exception
     *
     * @param key               search key
     * @return                  matching value
     */
    fun lookUp(key: K): Set<V> = elements.lookUp(key)

    /**
     * Look up the given key in the multimap. Return None if absent, otherwise
     *   return the corresponding value wrapped in Some.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.lookUpKey("Ken") = Some(25)
     *   {Jessie: 22, John: 31, Ken: 25}.lookUpKey("Irene") = None
     *   {}.lookUpKey("Ken") = None
     *
     * @param key               search key
     * @return                  matching value or none if key is absent
     */
    fun lookUpKey(key: K): Option<Set<V>> = elements.lookUpKey(key)

    /**
     * Look up the given key in the multimap. Return defaultValue if absent, otherwise
     *   return the corresponding value.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.lookUpWithDefault("Ken", 99) = 25
     *   {Jessie: 22, John: 31, Ken: 25}.lookUpWithDefault("Irene", 99) = 99
     *   {}.lookUpWithDefault("Ken", 99) = 99
     *
     * @param key               search key
     * @param defaultValue      default value to use if key is absent
     * @return                  matching value or default if key is absent
     */
    fun lookUpWithDefault(key: K, defaultValue: Set<V>): Set<V> = elements.lookUpWithDefault(key, defaultValue)

    /**
     * Compose all the elements of this multimap as a string using the default separator, prefix, postfix, etc.
     *
     * @return                  the map content
     */
    fun makeString(): String = this.makeString(", ", "<[", "]>")

    /**
     * Compose all the elements of this multimap as a string using the separator, prefix, postfix, etc.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.makeString(", ", "<[", "]>", 2, "...") = <[Jessie: 22, John: 31, ...]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.makeString(", ", "<[", "]>", 2) = <[Jessie: 22, John: 31, ...]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.makeString(", ", "<[", "]>") = <[Jessie: 22, John: 31, Ken: 25]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.makeString() = <[Jessie: 22, John: 31, Ken: 25]>
     *   <[]>.makeString() = <[]>
     *
     * @param separator         the separator between each element
     * @param prefix            the leading content
     * @param postfix           the trailing content
     * @param limit             constrains the output to the fist limit elements
     * @param truncated         indicator that the output has been limited
     * @return                  the list content
     */
    fun makeString(separator: String = ", ", prefix: String = "", postfix: String = "", limit: Int = -1, truncated: String = "..."): String =
            elements.makeString(separator, prefix, postfix, limit, truncated)

    /**
     * Map a function over all values in the multimap.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.map{v -> v + 1} = {Jessie: 23, John: 32, Ken: 26}
     *   {Jessie: 22, John: 31, Ken: 25}.map{v -> (v % 2 == 0)} = {Jessie: true, John: false, Ken: false}
     *
     * @param f     		    the function to apply to each value
     * @return      		    updated multimap
     */
    fun <W : Comparable<W>> map(f: (Set<V>) -> Set<W>): MultiMap<K, W> = MultiMap(elements.map(f))

    /**
     * Difference two multimaps (as an operator), ie all the elements in this multimap that are
     *   not present in the given multimap.
     *
     * Examples:
     *   {Jessie, John, Ken} - {Jessie, John, Ken} = {}
     *   {Jessie, John, Ken} - {John, Ken} = {Jessie}
     *   {Jessie, John, Ken} - {} = {Jessie, John, Ken}
     *   {} - {Jessie, John, Ken} = {}
     *
     * @param map               existing multimap
     * @return                  the difference of this multimap and the given multimap
     */
    operator fun minus(map: MultiMap<K, V>): MultiMap<K, V> = this.difference(map)

    /**
     * Partition the multimap into two multimaps, one with all values that satisfy
     *   the predicate and one with all values that don't satisfy the predicate.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.partition{age -> (age % 2 == 0)} = ({Jessie: 22}, {John: 31, Ken: 25})
     *
     * @param predicate     	predicate function on the value types
     * @return              	pair of multimaps partitioned by the predicate
     */
    fun partition(predicate: (Set<V>) -> Boolean): Pair<MultiMap<K, V>, MultiMap<K, V>> {
        val (left: Map<K, Set<V>>, right: Map<K, Set<V>>) = elements.partition(predicate)
        return Pair(MultiMap(left), MultiMap(right))
    }   // partition

    /**
     * Partition the multimap into two multimaps, one with all keys that satisfy
     *   the predicate and one with all keys that don't satisfy the predicate.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.partitionKey{name -> (name.startsWith("J"))} = ({Jessie: 22], John: 31}, {Ken: 25})
     *
     * @param predicate     	predicate function on the value types
     * @return              	pair of multimaps partitioned by the predicate
     */
    fun partitionKey(predicate: (K) -> Boolean): Pair<MultiMap<K, V>, MultiMap<K, V>> {
        val (left: Map<K, Set<V>>, right: Map<K, Set<V>>) = elements.partitionKey(predicate)
        return Pair(MultiMap(left), MultiMap(right))
    }   // partitionKey

    /**
     * The union of two multimaps (as an operator), ie all the elements from this multimap and
     *   from the given multimap.
     *
     * Examples:
     *   {Jessie, John, Ken} + {Dawn, Irene} = {Dawn, Irene, Jessie, John, Ken}
     *   {Jessie, John, Ken} + {Jessie, Irene} = {Irene, Jessie, John, Ken}
     *   {Jessie, John, Ken} + {} = {Jessie, John, Ken}
     *   {} + {Dawn, Irene} = {Dawn, Irene}
     *
     * @param map               existing multimap
     * @return                  the union of the two multimaps
     */
    operator fun plus(map: MultiMap<K, V>): MultiMap<K, V> = this.union(map)

    /**
     * Obtain the size of the multimap.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.size() = 3
     *   {}.size() = 0
     *
     * @return                  the number of elements in the multimap
     */
    fun size(): Int = elements.size()

    /**
     * The expression split k multimap is a pair (multimap1,multimap2) where multimap1 comprises
     *   the elements of multimap with keys less than k and multimap2 comprises the elements of
     *   multimap with keys greater than k.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.split(Judith) = ({Jessie: 22, John: 31}, {Ken: 25})
     *   {Jessie: 22, John: 31, Ken: 25}.split(John) = ({Jessie: 22}, {Ken: 25})
     *
     * @param key     		    partitioning key
     * @return        		    pair of multimaps partitioned by the key
     */
    fun split(key: K): Pair<MultiMap<K, V>, MultiMap<K, V>> {
        val (left: Map<K, Set<V>>, right: Map<K, Set<V>>) = elements.split(key)
        return Pair(MultiMap(left), MultiMap(right))
    }   // split

    /**
     * The intersection of two multimaps (as an operator), ie all the elements that are
     *   present in both multimaps.
     *
     * Examples:
     *   {Jessie, John, Ken} * {Jessie, John, Ken} = {Jessie, John, Ken}
     *   {Jessie, John, Ken} * {Jessie, John} = {Jessie, John}
     *   {Jessie, John, Ken} * {Dawn, Irene} = {}
     *   {Jessie, John, Ken} * {} = {}
     *   {} * {Jessie, John, Ken} = {}
     *
     * @param map               existing multimap
     * @return                  the intersection of the two multimaps
     */
    operator fun times(map: MultiMap<K, V>): MultiMap<K, V> = this.intersection(map)

    /**
     * Textual representation of a multimap.
     *
     * @return                  text for a multimap: <[key1: value1, key2: value2, ...]>
     */
    override fun toString(): String = this.makeString()

    /**
     * The union of two multimaps. It delivers a left-biased union of this
     *   and right, ie it prefers this when duplicate keys are encountered.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.union({Irene: 25, Dawn: 31}) = {Dawn: 31, Irene: 25, Jessie: 22, John: 31, Ken: 25}
     *   {Jessie: 22, John: 31, Ken: 25}.union({Ken: 35, John: 41}) = {Jessie: 22, John: 31, Ken: 25}
     *   {Jessie: 22, John: 31, Ken: 25}.union({}) = {Jessie: 22, John: 31, Ken: 25}
     *   {}.union({Jessie: 22, John: 31, Ken: 25}) = {Jessie: 22, John: 31, Ken: 25}
     *
     * @param map               existing multimap
     * @return                  the union of this multimap and the given multimap
     */
    fun union(map: MultiMap<K, V>): MultiMap<K, V> = MultiMap(elements.union(map.elements))

    /**
     * Update the value at the key, if present. If f(v) is None then the
     *   element is deleted. If it is Some(v) then the key is bound to the
     *   new value v.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.update(Ken){v -> if (k == "Ken" && v == 25) some(100) else none} = {Jessie: 22, John: 31, Ken: 100}
     *   {Jessie: 22, John: 31, Ken: 25}.update(Ken){v -> if (k == "Ken" && v == 99) some(100) else none} = {Jessie: 22, John: 31}
     *   {Jessie: 22, John: 31, Ken: 25}.update(Irene){v -> if (k == "Ken" && v == 25) some(100) else none} = {Jessie: 22, John: 31, Ken: 100}
     *
     * @param key     		    new key
     * @param f			        update function
     * @return        		    updated multimap
     */
    fun update(key: K, f: (Set<V>) -> Option<Set<V>>): MultiMap<K, V> = this.updateWithKey(key){_ -> {v: Set<V> -> f(v)}}

    /**
     * Update the value at the key if present. If f(k)(v) is None then the
     *   element is deleted. If it is Some(v) then the key is bound to the
     *   new value v.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.updateWithKey(Ken){k -> {v -> if (k == "Ken" && v == 25) some(100) else none}} = {Jessie: 22, John: 31, Ken: 100}
     *   {Jessie: 22, John: 31, Ken: 25}.updateWithKey(Ken){k -> {v -> if (k == "Ken" && v == 99) some(100) else none}} = {Jessie: 22, John: 31}
     *   {Jessie: 22, John: 31, Ken: 25}.updateWithKey(Irene){k -> {v -> if (k == "Ken" && v == 25) some(100) else none}} = {Jessie: 22, John: 31, Ken: 100}
     *
     * @param key     		    new key
     * @param f			        curried update function
     * @return        		    updated multimap
     */
    fun updateWithKey(key: K, f: (K) -> (Set<V>) -> Option<Set<V>>): MultiMap<K, V> = MultiMap(elements.updateWithKey(key, f))

    fun updateWithKey(key: K, f: (K, Set<V>) -> Option<Set<V>>): MultiMap<K, V> = this.updateWithKey(key, C2(f))

    /**
     * Returns a List view of the values contained in this multimap.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.valueList() = [22, 31, 25]
     *   <[]>.valueList() = []
     *
     * @return    		the values for this multimap
     */
    fun valueList(): List<V> {
        val values: List<Set<V>> = elements.valueList()
        return values.foldLeft(empty()){list ->
            {set ->
                val setList: List<V> = set.foldLeft(empty()){vs -> {v -> vs.append(v)}}
                list.append(setList)
            }
        }
    }   // valueList

}   // MultiMap
