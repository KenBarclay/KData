package com.adt.kotlin.data.immutable.validation

/**
 * The Validation type represents a computation that may return a successfully computed value
 *   or result in a failure. Instances of Validation[E, A], are either an instance of Success[A]
 *   or Failure[E]. The code is modelled on the FunctionalJava Validation.
 *
 * datatype Validation[E, A] = Failure of E
 *                           | Success of A
 *
 * @param E                     the type of element in a failure
 * @param A                     the type of element in a success
 *
 * @author	                    Ken Barclay
 * @since                       December 2018
 */

import com.adt.kotlin.data.immutable.nel.NonEmptyList
import com.adt.kotlin.data.immutable.nel.NonEmptyListF
import com.adt.kotlin.data.immutable.validation.Validation.Failure
import com.adt.kotlin.data.immutable.validation.Validation.Success
import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3
import com.adt.kotlin.hkfp.instances.NonEmptyListSemigroup
import com.adt.kotlin.hkfp.typeclass.Monoid
import com.adt.kotlin.hkfp.typeclass.Semigroup


object ValidationF {

    /**
     * Factory constructor functions.
     */
    fun <E, A> failure(e: E): Failure<E, A> = Failure(e)
    fun <E, A> success(a: A): Success<E, A> = Success(a)

    fun <E, A> failureNel(e: E): ValidationNel<E, A> = FailureNel(NonEmptyListF.singleton(e))
    fun <E, A> failureNel(nel: NonEmptyList<E>): ValidationNel<E, A> = FailureNel(nel)
    fun <E, A> successNel(a: A): ValidationNel<E, A> = SuccessNel(a)



    // Functor extension functions:

    /**
     * Lift a function into the Validation context.
     */
    fun <E, A, B> lift(f: (A) -> B): (Validation<E, A>) -> Validation<E, B> =
        {vea: Validation<E, A> -> vea.fmap(f)}



    // Applicative extension functions:

    fun <E, A, B, C> liftA2(se: Semigroup<E>, f: (A) -> (B) -> C): (Validation<E, A>) -> (Validation<E, B>) -> Validation<E, C> =
        {vea: Validation<E, A> ->
            {veb: Validation<E, B> ->
                veb.ap(se, vea.fmap(f))
            }
        }   // liftA2

    fun <E, A, B, C> liftA2(se: Semigroup<E>, f: (A, B) -> C): (Validation<E, A>) -> (Validation<E, B>) -> Validation<E, C> =
        liftA2(se, C2(f))

    fun <E, A, B, C> liftA2(f: (A) -> (B) -> C): (ValidationNel<E, A>) -> (ValidationNel<E, B>) -> ValidationNel<E, C> =
        liftA2(NonEmptyListSemigroup(), f)

    fun <E, A, B, C> liftA2(f: (A, B) -> C): (ValidationNel<E, A>) -> (ValidationNel<E, B>) -> ValidationNel<E, C> =
        liftA2(C2(f))



    fun <E, A, B, C, D> liftA3(se: Semigroup<E>, f: (A) -> (B) -> (C) -> D): (Validation<E, A>) -> (Validation<E, B>) -> (Validation<E, C>) -> Validation<E, D> =
        {vea: Validation<E, A> ->
            {veb: Validation<E, B> ->
                {vec: Validation<E, C> ->
                    vec.ap(se, veb.ap(se, vea.fmap(f)))
                }
            }
        }   // liftA3

    fun <E, A, B, C, D> liftA3(se: Semigroup<E>, f: (A, B, C) -> D): (Validation<E, A>) -> (Validation<E, B>) -> (Validation<E, C>) -> Validation<E, D> =
        liftA3(se, C3(f))

    fun <E, A, B, C, D> liftA3(f: (A) -> (B) -> (C) -> D): (ValidationNel<E, A>) -> (ValidationNel<E, B>) -> (ValidationNel<E, C>) -> ValidationNel<E, D> =
        liftA3(NonEmptyListSemigroup(), f)

    fun <E, A, B, C, D> liftA3(f: (A, B, C) -> D): (ValidationNel<E, A>) -> (ValidationNel<E, B>) -> (ValidationNel<E, C>) -> ValidationNel<E, D> =
        liftA3(C3(f))



    // Monad extension functions:

    /**
     * Promote a function to a monad.
     */
    fun <E, A, B> liftM(me: Monoid<E>, f: (A) -> B): (Validation<E, A>) -> Validation<E, B> =
        {vea: Validation<E, A> ->
            vea.bind(me){a: A -> success<E, B>(f(a))}
        }   // liftM

    /**
     * Promote a function to a monad, scanning the monadic arguments from left to right.
     */
    fun <E, A, B, C> liftM2(me: Monoid<E>, f: (A) -> (B) -> C): (Validation<E, A>) -> (Validation<E, B>) -> Validation<E, C> =
        {vea: Validation<E, A> ->
            {veb: Validation<E, B> ->
                vea.bind(me){a: A -> veb.bind(me){b: B -> success<E, C>(f(a)(b))}}
            }
        }   // liftM2

    fun <E, A, B, C> liftM2(me: Monoid<E>, f: (A, B) -> C): (Validation<E, A>) -> (Validation<E, B>) -> Validation<E, C> =
        liftM2(me, C2(f))

    /**
     * Promote a function to a monad, scanning the monadic arguments from left to right.
     */
    fun <E, A, B, C, D> liftM3(me: Monoid<E>, f: (A) -> (B) -> (C) -> D): (Validation<E, A>) -> (Validation<E, B>) -> (Validation<E, C>) -> Validation<E, D> =
        {vea: Validation<E, A> ->
            {veb: Validation<E, B> ->
                {vec: Validation<E, C> ->
                    vea.bind(me){a: A -> veb.bind(me){b: B -> vec.bind(me){c: C -> success<E, D>(f(a)(b)(c))}}}
                }
            }
        }   // liftM3

    fun <E, A, B, C, D> liftM3(me: Monoid<E>, f: (A, B, C) -> D): (Validation<E, A>) -> (Validation<E, B>) -> (Validation<E, C>) -> Validation<E, D> =
        liftM3(me, C3(f))

    /**
     * Map the given function across the two Validations.
     */
    fun <A, B, C, D> map2(ma: Monoid<A>, vab: Validation<A, B>, vac: Validation<A, C>, f: (B) -> (C) -> D): Validation<A, D> =
        vab.bind(ma){b: B -> vac.map{c: C -> f(b)(c)}}

    fun <A, B, C, D> map2(ma: Monoid<A>, vab: Validation<A, B>, vac: Validation<A, C>, f: (B, C) -> D): Validation<A, D> =
        map2(ma, vab, vac, C2(f))

    /**
     * Map the given function across the three Validations.
     */
    fun <A, B, C, D, E> map3(ma: Monoid<A>, vab: Validation<A, B>, vac: Validation<A, C>, vad: Validation<A, D>, f: (B) -> (C) -> (D) -> E): Validation<A, E> =
        vab.bind(ma){b: B -> vac.bind(ma){c: C -> vad.map{d: D -> f(b)(c)(d)}}}

    fun <A, B, C, D, E> map3(ma: Monoid<A>, vab: Validation<A, B>, vac: Validation<A, C>, vad: Validation<A, D>, f: (B, C, D) -> E): Validation<A, E> =
        map3(ma, vab, vac, vad, C3(f))

}   // ValidationF
