package com.adt.kotlin.hkfp.typeclass

/**
 * A class for monoids (types with an associative binary operation that has an identity)
 *   with various general-purpose instances. We see that only concrete types can be made
 *   instances of Monoid, because the A in the type class definition doesn't take any type
 *   parameters.
 *
 * @author	                    Ken Barclay
 * @since                       October 2019
 */

// TODO import com.adt.kotlin.data.immutable.list.List



interface Monoid<A> : Semigroup<A> {

    val empty: A

    /**
     * Fold a list using the monoid.
     *
     * For most types, the default definition for concat will be sufficient.
     *   The function is included in the class definition so that an optimized
     *   version can be provided for specific types.
     */
    // TODO fun concat(list: List<A>): A = list.foldRight(empty, ::combine)

}   // Monoid
