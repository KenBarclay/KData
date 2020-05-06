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

import com.adt.kotlin.data.immutable.map.*
import com.adt.kotlin.data.immutable.map.Map
import com.adt.kotlin.data.immutable.map.Map.Tip
import com.adt.kotlin.data.immutable.map.Map.Bin

import com.adt.kotlin.data.immutable.list.List

import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.Option.None
import com.adt.kotlin.data.immutable.option.Option.Some

import com.adt.kotlin.hkfp.fp.FunctionF.C2



class MultiSet<A : Comparable<A>>(val elements: Map<A, Int>) {

    /**
     * Determine if this multiset contains the element determined by the predicate.
     *
     * Examples:
     *   {Jessie, John, Ken}.contains{name -> (name == John)} = true
     *   {Jessie, John, Ken}.contains{name -> (name == Irene)} = false
     *   {}.contains{name -> (name == John)} = false
     *
     * @param predicate         search predicate
     * @return                  true if search element is present, false otherwise
     */
    fun contains(predicate: (A) -> Boolean): Boolean = elements.contains(predicate)

    /**
     * Determine if the multiset contains the given element.
     *
     * Examples:
     *   {Jessie, John, Ken}.contains(John) = true
     *   {Jessie, John, Ken}.contains(Irene) = false
     *   {}.contains(John) = false
     *
     * @param a                 search element
     * @return                  true if the given element is in the tree
     */
    fun contains(a: A): Boolean = elements.contains(a)

    /**
     * Delete the value from the multiset. When the value is not a member
     *   of the multiset, the original multiset is returned.
     *
     * Examples:
     *   {Jessie, John, Ken}.delete(John) = {Jessie, Ken}
     *   {Jessie, John, Ken}.delete(Irene) = {Jessie, John, Ken}
     *
     * @param a                 existing element to remove
     * @result                  updated multiset
     */
    fun delete(a: A): MultiSet<A> = MultiSet(elements.delete(a))

    /**
     * Difference two multiset, ie all the elements in this multiset that are
     *   not present in the given multiset.
     *
     * Examples:
     *   {Jessie, John, Ken}.difference({Jessie, John, Ken}) = {}
     *   {Jessie, John, Ken}.difference({John, Ken}) = {Jessie}
     *   {Jessie, John, Ken}.difference({}) = {Jessie, John, Ken}
     *   {}.difference({Jessie, John, Ken}) = {}
     *
     * @param tree              existing multiset
     * @return                  the difference of this multiset and the given multiset
     */
    fun difference(set: MultiSet<A>): MultiSet<A> = MultiSet(elements.difference(set.elements))

    /**
     * Are two multisets equal?
     *
     * @param other             the other multiset
     * @return                  true if both multisets are the same; false otherwise
     */
    override fun equals(other: Any?): Boolean {
        return if (this === other)
            true
        else if (other == null || this::class.java != other::class.java)
            false
        else {
            @Suppress("UNCHECKED_CAST") val otherSet: MultiSet<A> = other as MultiSet<A>
            this.elements == otherSet.elements
        }
    }   // equals

    /**
     * Filter all elements that satisfy the predicate.
     *
     * Examples:
     *   {Jessie, John, Ken}.filter{name -> name.startsWith(J)} = {Jessie, John}
     *   {Jessie, John, Ken}.filter{name -> name.charAt(0) >= A} = {Jessie, John, Ken}
     *   {Jessie, John, Ken}.filter{name -> name.charAt(0) >= Z} = {}
     *   {}.filter{name -> name.startsWith(J)} = {}
     *
     * @param predicate         criteria
     * @return                  multiset comprising those elements from this multiset that match criteria
     */
    fun filter(predicate: (A) -> Boolean): MultiSet<A> = MultiSet(elements.filterWithKey{k -> {_ -> predicate(k)}})

    /**
     * The find function takes a predicate and returns the first element in
     *   the multiset matching the predicate, or none if there is no
     *   such element.
     *
     * Examples:
     *   {Jessie, John, Ken}.find{name -> name.startsWith(J)} = some(John)
     *   {Jessie, John, Ken}.find{name -> name.charAt(0) >= A} = some(John)
     *   {Jessie, John, Ken}.find{name -> name.charAt(0) >= Z} = none
     *   {}.find{name -> name.startsWith(J)} = none
     *
     * @param predicate         criteria
     * @return                  matching element, if found
     */
    fun find(predicate: (A) -> Boolean): Option<A> = elements.find(predicate)

    fun find(a: A): Option<A> = find{b -> (b == a)}

    /**
     * foldLeft is a higher-order function that folds a left associative binary
     *   function into the values of a multiset.
     *
     * Examples:
     *   {Jessie, John, Ken}.foldLeft(0){res -> {name -> res + name.length}} = 13
     *   {}.foldLeft(0){res -> {age -> res + age}} = 0
     *   {Jessie, John, Ken}.foldLeft([]){res -> {name -> res.append(name)}} = [Jessie, John, Ken]
     *
     * @param e           	    initial value
     * @param f         		curried binary function
     * @return            	    folded result
     */
    fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B = elements.foldLeftWithKey(e){b -> {a -> {_ -> f(b)(a)}}}

    fun <B> foldLeft(e: B, f: (B, A) -> B): B = this.foldLeft(e, C2(f))

    /**
     * foldRight is a higher-order function that folds a right associative binary
     *   function into the values of a multiset.
     *
     * Examples:
     *   ////<[Jessie: 22, John: 31, Ken: 25]>.foldRight(0){age -> {res -> res + age}} = 78
     *   ////<[]>.foldRight(0){age -> {res -> res + age}} = 0
     *   ////<[Jessie: 22, John: 31, Ken: 25]>.foldRight([]){age -> {res -> res.append(age)}} = [25, 31, 22]
     *
     * @param e           	    initial value
     * @param f         		curried binary function
     * @return            	    folded result
     */
    fun <B> foldRight(e: B, f: (A) -> (B) -> B) : B = elements.foldRightWithKey(e){a -> {_ -> {b -> f(a)(b)}}}

    fun <B> foldRight(e: B, f: (A, B) -> B) : B = this.foldRight(e, C2(f))

    /**
     * Insert a new value in the multiset.
     *
     * Examples:
     *   ////{Jessie, John, Ken}.insert(Irene) = {Irene, Jessie, John, Ken}
     *   ////{Jessie, John, Ken}.insert(John) = {Jessie, John, Ken}
     *
     * @param a                 new element to be added
     * @return                  updated multiset
     */
    fun insert(a: A): MultiSet<A> {
        val op: Option<Int> = elements.lookUpKey(a)
        val map: Map<A, Int> = when (op) {
            is None -> elements.insert(a, 1)
            is Some -> elements.insert(a, 1 + op.value)
        }

        return MultiSet(map)
    }   // insert

    /**
     * The intersection of two multisets, ie all the elements that are
     *   present in both multisets.
     *
     * Examples:
     *   {Jessie, John, Ken}.intersection({Jessie, John, Ken}) = {Jessie, John, Ken}
     *   {Jessie, John, Ken}.intersection({Jessie, John}) = {Jessie, John}
     *   {Jessie, John, Ken}.intersection({Dawn, Irene}) = {}
     *   {Jessie, John, Ken}.intersection({}) = {}
     *   {}.intersection({Jessie, John, Ken}) = {}
     *
     * @param set               existing multiset
     * @return                  the intersection of the two multisets
     */
    fun intersection(set: MultiSet<A>): MultiSet<A> = MultiSet(elements.intersection(set.elements))

    /**
     * Test whether the multiset is empty.
     *
     * Examples:
     *   {Jessie, John, Ken}.isEmpty() = false
     *   {}.isEmpty() = true
     *
     * @return                  true if the multiset contains no elements
     */
    fun isEmpty(): Boolean = elements.isEmpty()

    /**
     * Is this a proper subset of the given multiset? (ie. a subset but not equal).
     *
     * Examples:
     *   {Jessie, John, Ken}.isProperSubsetOf({Jessie, John, Ken}) = false
     *   {Jessie, John}.isProperSubsetOf({Jessie, John, Ken}) = true
     *   {Jessie, John, Ken}.isProperSubsetOf({John, Ken}) = false
     *   {}.isProperSubsetOf({Jessie, John, Ken}) = true
     *   {Jessie, John, Ken}.isProperSubsetOf({}) = false
     *   {}.isProperSubsetOf({}) = false
     *
     * @param set               existing multiset
     * @return                  true if this multiset is a proper subset of the given multiset
     */
    fun isProperSubsetOf(set: MultiSet<A>): Boolean = elements.isProperSubmapOf(set.elements)

    /**
     * Is this a subset of the given multiset?, ie. are all the elements
     *   of this multiset also elements of the given multiset?
     *
     * Examples:
     *   {Jessie, John, Ken}.isSubsetOf({Jessie, John, Ken}) = true
     *   {Jessie, John}.isSubsetOf({Jessie, John, Ken}) = true
     *   {Jessie, John, Ken}.isSubsetOf({John, Ken}) = false
     *   {}.isSubsetOf({Jessie, John, Ken}) = true
     *   {Jessie, John, Ken}.isSubsetOf({}) = false
     *   {}.isSubsetOf({}) = true
     *
     * @param set               existing multiset
     * @return                  true if this multiset is a subset of the given multiset
     */
    fun isSubsetOf(set: MultiSet<A>): Boolean = elements.isSubmapOf(set.elements)

    /**
     * Obtains the size of a multiset.
     *
     * Examples:
     *   {Jessie, John, Ken}.length() = 3
     *   {}.length() = 0
     *
     * @return                  the number of elements in the multiset
     */
    fun length(): Int = elements.length()

    /**
     * Compose all the elements of this multiset as a string using the default separator, prefix, postfix, etc.
     *
     * @return                  the map content
     */
    fun makeString(): String = this.makeString(", ", "{", "}")

    /**
     * Compose all the elements of this multiset as a string using the separator, prefix, postfix, etc.
     *
     * Examples:
     *   {Jessie: 22, John: 31, Ken: 25}.makeString(", ", "<[", "]>", 2, "...") = <[Jessie: 22, John: 31, ...]>
     *   {Jessie: 22, John: 31, Ken: 25}.makeString(", ", "<[", "]>", 2) = <[Jessie: 22, John: 31, ...]>
     *   {Jessie: 22, John: 31, Ken: 25}.makeString(", ", "<[", "]>") = <[Jessie: 22, John: 31, Ken: 25]>
     *   {Jessie: 22, John: 31, Ken: 25}.makeString() = <[Jessie: 22, John: 31, Ken: 25]>
     *   {}.makeString() = <[]>
     *
     * @param separator         the separator between each element
     * @param prefix            the leading content
     * @param postfix           the trailing content
     * @param limit             constrains the output to the fist limit elements
     * @param truncated         indicator that the output has been limited
     * @return                  the list content
     */
    fun makeString(separator: String = ", ", prefix: String = "", postfix: String = "", limit: Int = -1, truncated: String = "..."): String {
        var count: Int = 0
        fun recMakeString(map: Map<A, Int>, buffer: StringBuffer): Int {
            return when(map) {
                is Tip -> count
                is Bin -> {
                    recMakeString(map.left, buffer)
                    if (count != 0)
                        buffer.append(separator)
                    if (limit < 0 || count < limit) {
                        buffer.append("${map.key}")
                        count++
                    }
                    recMakeString(map.right, buffer)
                }
            }
        }   // recMakeString

        val buffer: StringBuffer = StringBuffer(prefix)
        val finalCount: Int = recMakeString(elements, buffer)
        if (limit >= 0 && finalCount >= limit)
            buffer.append(truncated)
        buffer.append(postfix)
        return buffer.toString()
    }   // makeString

    /**
     * Function map applies the function parameter to each item in the multiset, delivering
     *   a new multiset.
     *
     * Examples:
     *   {Jessie, John, Ken}.map{name -> name.charAt(0)} = {J, K}
     *   {}.map{name -> charAt(0)} = {}
     *
     * @param f                 transformation function
     * @return                  set with the elements transformed
     */
    fun <B : Comparable<B>> map(f: (A) -> B): MultiSet<B> {
        val mapList: List<Pair<A, Int>> = MapF.toList(elements)
        val mappedList: List<Pair<B, Int>> = mapList.map{pr: Pair<A, Int> -> Pair(f(pr.first), pr.second)}
        return MultiSet(MapF.fromList(mappedList))
    }   // map

    /**
     * Difference two multisets (as an operator), ie all the elements in this multiset that are
     *   not present in the given multiset.
     *
     * Examples:
     *   {Jessie, John, Ken} - {Jessie, John, Ken} = {}
     *   {Jessie, John, Ken} - {John, Ken} = {Jessie}
     *   {Jessie, John, Ken} - {} = {Jessie, John, Ken}
     *   {} - {Jessie, John, Ken} = {}
     *
     * @param set               existing multiset
     * @return                  the difference of this multiset and the given multiset
     */
    operator fun minus(set: MultiSet<A>): MultiSet<A> = this.difference(set)

    /**
     * Partition the multiset into two multisets, one with all elements that satisfy
     *   the predicate and one with all elements that don't satisfy the predicate.
     *
     * Examples:
     *   {Jessie, John, Ken}.partition{name -> name.startsWith(J)} = ({Jessie, John}, {Ken})
     *
     * @param predicate         criteria
     * @return                  pair of multisets
     */
    fun partition(predicate: (A) -> Boolean): Pair<MultiSet<A>, MultiSet<A>> {
        val (leftMap: Map<A, Int>, rightMap: Map<A, Int>) = elements.partitionKey(predicate)
        return Pair(MultiSet(leftMap), MultiSet(rightMap))
    }   // partition

    /**
     * The union of two multisets (as an operator), ie all the elements from this multiset and
     *   from the given multiset.
     *
     * Examples:
     *   {Jessie, John, Ken} + {Dawn, Irene} = {Dawn, Irene, Jessie, John, Ken}
     *   {Jessie, John, Ken} + {Jessie, Irene} = {Irene, Jessie, John, Ken}
     *   {Jessie, John, Ken} + {} = {Jessie, John, Ken}
     *   {} + {Dawn, Irene} = {Dawn, Irene}
     *
     * @param set               existing multiset
     * @return                  the union of the two multisets
     */
    operator fun plus(set: MultiSet<A>): MultiSet<A> = this.union(set)

    /**
     * Obtain the size of the multiset, a synonym for length.
     *
     * Examples:
     *   {Jessie, John, Ken}.size() = 3
     *   {}.size() = 0
     *
     * @return                  the number of elements in the multiset
     */
    fun size(): Int = length()

    /**
     * The expression split x multiset is a pair (multiset1, multiset2) where multiset1 comprises
     *   the elements of multiset less than x and multiset2 comprises the elements of
     *   multiset greater than x.
     *
     * Examples:
     *   {Jessie, John, Ken}.split(John) = ({Jessie}, {Ken})
     *   {Jessie, John, Ken}.split(Linda) = ({Jessie, John, Ken}, {})
     *
     * @param a                 the pivot element
     * @return                  pair of multisets
     */
    fun split(a: A): Pair<MultiSet<A>, MultiSet<A>> {
        val (leftMap: Map<A, Int>, rightMap: Map<A, Int>) = elements.split(a)
        return Pair(MultiSet(leftMap), MultiSet(rightMap))
    }   // split

    /**
     * The intersection of two multisets (as an operator), ie all the elements that are
     *   present in both multisets.
     *
     * Examples:
     *   {Jessie, John, Ken} * {Jessie, John, Ken} = {Jessie, John, Ken}
     *   {Jessie, John, Ken} * {Jessie, John} = {Jessie, John}
     *   {Jessie, John, Ken} * {Dawn, Irene} = {}
     *   {Jessie, John, Ken} * {} = {}
     *   {} * {Jessie, John, Ken} = {}
     *
     * @param set               existing multiset
     * @return                  the intersection of the two multisets
     */
    operator fun times(set: MultiSet<A>): MultiSet<A> = this.intersection(set)

    /**
     * Textual representation of a multiset.
     *
     * @return                  text for a multiset: {value1, value2, ...}
     */
    override fun toString(): String = this.makeString()

    /**
     * The union of two sets, ie all the elements from this set and
     *   from the given set.
     *
     * Examples:
     *   {Jessie, John, Ken}.union({Dawn, Irene}) = {Dawn, Irene, Jessie, John, Ken}
     *   {Jessie, John, Ken}.union({Jessie, Irene}) = {Irene, Jessie, John, Ken}
     *   {Jessie, John, Ken}.union({}) = {Jessie, John, Ken}
     *   {}.union({Dawn, Irene}) = {Dawn, Irene}
     *
     * @param tree              existing set
     * @return                  the union of the two sets
     */
    fun union(set: MultiSet<A>): MultiSet<A> = MultiSet(elements.union(set.elements))

}   // MultiSet
