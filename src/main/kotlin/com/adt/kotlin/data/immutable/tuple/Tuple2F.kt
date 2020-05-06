package com.adt.kotlin.data.immutable.tuple

/**
 * A Tuple2 represents a pair of values.
 *
 * @author	                    Ken Barclay
 * @since                       October 2019
 */

import com.adt.kotlin.hkfp.typeclass.Monoid



object Tuple2F {

    // Functor extension functions:

    /**
     * Lift a function into the Tuple2 context.
     */
    fun <A, B, C> lift(f: (B) -> C): (Tuple2<A, B>) -> Tuple2<A, C> =
        {tab: Tuple2<A, B> -> tab.fmap(f)}

    fun <A, B, C, D> liftA2(md: Monoid<A>, f: (B) -> (C) -> D): (Tuple2<A, B>) -> (Tuple2<A, C>) -> Tuple2<A, D> =
        {tab: Tuple2<A, B> ->
            {tac: Tuple2<A, C> ->
                tac.ap(md, tab.fmap(f))
            }
        }   // liftA2

    fun <A, B, C, D, E> liftA3(md: Monoid<A>, f: (B) -> (C) -> (D) -> E): (Tuple2<A, B>) -> (Tuple2<A, C>) -> (Tuple2<A, D>) -> Tuple2<A, E> =
        {tab: Tuple2<A, B> ->
            {tac: Tuple2<A, C> ->
                {tad: Tuple2<A, D> ->
                    tad.ap(md, tac.ap(md, tab.fmap(f)))
                }
            }
        }   // liftA3



    // Monad extension functions:

    /**
     * Promote a function to a monad.
     */
    fun <A, B, C> liftM(md: Monoid<A>, f: (B) -> C): (Tuple2<A, B>) -> Tuple2<A, C> =
        {tab: Tuple2<A, B> ->
            tab.bind(md){b -> Tuple2(md.empty, f(b))}
        }   // liftM

    /**
     * Promote a function to a monad, scanning the monadic arguments from left to right.
     */
    fun <A, B, C, D> liftM2(md: Monoid<A>, f: (B) -> (C) -> D): (Tuple2<A, B>) -> (Tuple2<A, C>) -> Tuple2<A, D> =
        {tab: Tuple2<A, B> ->
            {tac: Tuple2<A, C> ->
                tab.bind(md){b -> tac.bind(md){c -> Tuple2(md.empty, f(b)(c))}}
            }
        }   // liftM2

    /**
     * Promote a function to a monad, scanning the monadic arguments from left to right.
     */
    fun <A, B, C, D, E> liftM3(md: Monoid<A>, f: (B) -> (C) -> (D) -> E): (Tuple2<A, B>) -> (Tuple2<A, C>) -> (Tuple2<A, D>) -> Tuple2<A, E> =
        {tab: Tuple2<A, B> ->
            {tac: Tuple2<A, C> ->
                {tad: Tuple2<A, D> ->
                    tab.bind(md){b -> tac.bind(md){c -> tad.bind(md){d -> Tuple2(md.empty, f(b)(c)(d))}}}
                }
            }
        }   // liftM3

}   // Tuple2F
