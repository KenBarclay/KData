package com.adt.kotlin.hkfp.instances

/**
 * A class for monoids (types with an associative binary operation that has an identity)
 *   with various general-purpose instances. We see that only concrete types can be made
 *   instances of Monoid, because the A in the type class definition doesn't take any type
 *   parameters.
 *
 * @author	                    Ken Barclay
 * @since                       October 2019
 */

import com.adt.kotlin.hkfp.typeclass.Monoid
import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.ListF
import com.adt.kotlin.data.immutable.list.append
import com.adt.kotlin.data.immutable.nel.NonEmptyList


/**
 * Monoid that adds integers.
 */
val intAddMonoid: Monoid<Int> = object: Monoid<Int> {
    override val empty: Int = 0
    override fun combine(a: Int, b: Int): Int = a + b
}

/**
 * Monoid that multiplies integers.
 */
val intMulMonoid: Monoid<Int> = object: Monoid<Int> {
    override val empty: Int = 1
    override fun combine(a: Int, b: Int): Int = a * b
}



/**
 * Monoid that adds longs.
 */
val longAddMonoid: Monoid<Long> = object: Monoid<Long> {
    override val empty: Long = 0L
    override fun combine(a: Long, b: Long): Long = a + b
}

/**
 * Monoid that multiplies longs.
 */
val longMulMonoid: Monoid<Long> = object: Monoid<Long> {
    override val empty: Long = 1L
    override fun combine(a: Long, b: Long): Long = a * b
}



/**
 * Monoid that adds doubles.
 */
val doubleAddMonoid: Monoid<Double> = object: Monoid<Double> {
    override val empty: Double = 0.0
    override fun combine(a: Double, b: Double): Double = a + b
}

/**
 * Monoid that multiplies doubles.
 */
val doubleMulMonoid: Monoid<Double> = object: Monoid<Double> {
    override val empty: Double = 1.0
    override fun combine(a: Double, b: Double): Double = a * b
}



/**
 * Monoid that ands booleans.
 */
val booleanConjMonoid: Monoid<Boolean> = object: Monoid<Boolean> {
    override val empty: Boolean = true
    override fun combine(a: Boolean, b: Boolean): Boolean = a && b
}

/**
 * Monoid that ors booleans.
 */
val booleanDisjMonoid: Monoid<Boolean> = object: Monoid<Boolean> {
    override val empty: Boolean = false
    override fun combine(a: Boolean, b: Boolean): Boolean = a || b
}



/**
 * Monoid that concatenates strings.
 */
val stringMonoid: Monoid<String> = object: Monoid<String> {
    override val empty: String = ""
    override fun combine(a: String, b: String): String = a + b
}



/**
 * Monoid that combines pairs.
 */
class PairMonoid<A, B>(val ma: Monoid<A>, val mb: Monoid<B>) : Monoid<Pair<A, B>> {

    override val empty: Pair<A, B> = Pair(ma.run{ empty }, mb.run{ empty })
    override fun combine(a: Pair<A, B>, b: Pair<A, B>): Pair<A, B> {
        val first: A = ma.run { combine(a.first, b.first) }
        val second: B = mb.run{ combine(a.second, b.second) }
        return Pair(first, second)
    }   // combine

}   // PairMonoid


/**
 * Monoid over lists.
 */
class ListMonoid<A>(val ma: Monoid<A>) : Monoid<List<A>> {

    override val empty: List<A> = ListF.empty()
    override fun combine(a: List<A>, b: List<A>): List<A> = a.append(b)

}   // ListMonoid
