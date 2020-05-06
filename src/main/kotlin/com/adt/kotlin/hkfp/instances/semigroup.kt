package com.adt.kotlin.hkfp.instances

/**
 * A class for semigroup (types with an associative binary operation)
 *   with various general-purpose instances. We see that only concrete
 *   types can be made instances of Semigroup, because the A in the
 *   type class definition doesn't take any type parameters.
 *
 * @author	                    Ken Barclay
 * @since                       October 2019
 */

import com.adt.kotlin.hkfp.typeclass.Monoid
import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.append
import com.adt.kotlin.data.immutable.nel.NonEmptyList
import com.adt.kotlin.data.immutable.nel.append
import com.adt.kotlin.hkfp.typeclass.Semigroup


/**
 * Semigroup that adds integers.
 */
val intAddSemigroup: Semigroup<Int> = object: Semigroup<Int> {
    override fun combine(a: Int, b: Int): Int = a + b
}

/**
 * Semigroup that multiplies integers.
 */
val intMulSemigroup: Semigroup<Int> = object: Semigroup<Int> {
    override fun combine(a: Int, b: Int): Int = a * b
}



/**
 * Semigroup that adds longs.
 */
val longAddSemigroup: Semigroup<Long> = object: Semigroup<Long> {
    override fun combine(a: Long, b: Long): Long = a + b
}

/**
 * Semigroup that multiplies longs.
 */
val longMulSemigroup: Semigroup<Long> = object: Semigroup<Long> {
    override fun combine(a: Long, b: Long): Long = a * b
}



/**
 * Semigroup that adds doubles.
 */
val doubleAddSemigroup: Semigroup<Double> = object: Semigroup<Double> {
    override fun combine(a: Double, b: Double): Double = a + b
}

/**
 * Semigroup that multiplies doubles.
 */
val doubleMulSemigroup: Semigroup<Double> = object: Semigroup<Double> {
    override fun combine(a: Double, b: Double): Double = a * b
}



/**
 * Semigroup that ands booleans.
 */
val booleanConjSemigroup: Semigroup<Boolean> = object: Semigroup<Boolean> {
    override fun combine(a: Boolean, b: Boolean): Boolean = a && b
}

/**
 * Semigroup that ors booleans.
 */
val booleanDisjSemigroup: Semigroup<Boolean> = object: Semigroup<Boolean> {
    override fun combine(a: Boolean, b: Boolean): Boolean = a || b
}



/**
 * Semigroup that concatenates strings.
 */
val stringSemigroup: Semigroup<String> = object: Semigroup<String> {
    override fun combine(a: String, b: String): String = a + b
}



/**
 * Semigroup that combines pairs.
 */
class PairSemigroup<A, B>(val ma: Monoid<A>, val mb: Monoid<B>) : Semigroup<Pair<A, B>> {

    override fun combine(a: Pair<A, B>, b: Pair<A, B>): Pair<A, B> {
        val first: A = ma.run { combine(a.first, b.first) }
        val second: B = mb.run{ combine(a.second, b.second) }
        return Pair(first, second)
    }   // combine

}   // PairSemigroup



/**
 * Semigroup over lists.
 */
class ListSemigroup<A> : Semigroup<List<A>> {

    override fun combine(a: List<A>, b: List<A>): List<A> = a.append(b)

}   // ListSemigroup



/**
 * Semigroup over non-empty lists.
 */
class NonEmptyListSemigroup<A> : Semigroup<NonEmptyList<A>> {

    override fun combine(a: NonEmptyList<A>, b: NonEmptyList<A>): NonEmptyList<A> = a.append(b)

}   // NonEmptyListSemigroup
