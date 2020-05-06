package com.adt.kotlin.data.immutable.option

/**
 * The Option type encapsulates an optional value.
 *
 * A value of type Option[A] either contains a value of type A (represented as Some A),
 *   or it is empty represented as None. Using Option is a good way to deal with errors
 *   without resorting to exceptions. The algebraic data type declaration is:
 *
 * datatype Option[A] = None
 *                    | Some A
 *
 * This Option type is inspired by the Haskell Maybe data type. The idiomatic way to
 *   employ an Option instance is as a monad using the functions map, inject, bind
 *   and filter. Given:
 *
 *   fun divide(num: Int, den: Int): Option<Int> ...
 *
 * then:
 *
 *   divide(a, c).bind{ac -> divide(b, c).bind{bc -> Some(Pair(ac, bc))}}
 *
 * finds the pair of divisions of a and b by c should c be an exact divisor.
 *
 * @author	                    Ken Barclay
 * @since                       October 2019
 */

import com.adt.kotlin.data.immutable.option.Option.None
import com.adt.kotlin.data.immutable.option.Option.Some
import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3


object OptionF {

    /**
     * Factory functions to create the base instances.
     */
    fun <A> none(): Option<A> = None

    fun <A> some(a: A): Option<A> = Some(a)

    /**
     * The option function takes an Option value, a default value, and a function.
     *   If the Option value is None, the function returns the default value.
     *   Otherwise, it applies the function to the value inside the Some and returns
     *   the result. See also the member function fold.
     *
     * @param op            	    option value
     * @param defaultValue  	    fall-back result
     * @param f           	        pure function:: A -> B
     * @return              	    function's value or default
     */
    fun <A, B> option(op: Option<A>, defaultValue: B, f: (A) -> B): B =
        if(op.isEmpty()) defaultValue else f(op.get())

    /**
     * Return an optional value that has a value of the given parameter, if the given predicate holds
     *   on that parameter, otherwise, returns no value.
     *
     * @param predicate         the predicate to test of the given parameter
     * @param a                 the parameter to test the predicate on and potentially
     *                              the value of the returned optional
     * @return                  an optional value that has a value of the given parameter,
     *                              if the given predicate holds on that parameter, otherwise,
     *                              returns no value
     */
    fun <A> iif(a: A, predicate: (A) -> Boolean): Option<A> =
        if (predicate(a)) some(a) else none()



// Functor extension functions:

    /**
     * Lift a function into the Option context.
     */
    fun <A, B> lift(f: (A) -> B): (Option<A>) -> Option<B> =
        {oa: Option<A> -> oa.fmap(f)}



// Applicative extension functions:

    /**
     * Lift a function to actions.
     */
    fun <A, B> liftA(f: (A) -> B): (Option<A>) -> Option<B> =
        {oa: Option<A> ->
            oa.ap(some(f))
        }   // liftA

    /**
     * Lift a binary function to actions.
     *
     * Some functors support an implementation of lift2 that is more efficient than the
     *   default one. In particular, if fmap is an expensive operation, it is likely
     *   better to use liftA2 than to fmap over the structure and then use ap.
     */
    fun <A, B, C> liftA2(f: (A) -> (B) -> C): (Option<A>) -> (Option<B>) -> Option<C> =
        {oa: Option<A> ->
            {ob: Option<B> ->
                ob.ap(oa.fmap(f))
            }
        }   // liftA2

    fun <A, B, C> liftA2(f: (A, B) -> C): (Option<A>) -> (Option<B>) -> Option<C> =
        liftA2(C2(f))

    /**
     * Lift a ternary function to actions.
     */
    fun <A, B, C, D> liftA3(f: (A) -> (B) -> (C) -> D): (Option<A>) -> (Option<B>) -> (Option<C>) -> Option<D> =
        {oa: Option<A> ->
            {ob: Option<B> ->
                {oc: Option<C> ->
                    oc.ap(ob.ap(oa.fmap(f)))
                }
            }
        }   // liftA3

    fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (Option<A>) -> (Option<B>) -> (Option<C>) -> Option<D> =
        liftA3(C3(f))



// Monad extension functions:

    fun <A, B> liftM(f: (A) -> B): (Option<A>) -> Option<B> =
        {oa: Option<A> ->
            oa.bind{a -> some(f(a))}
        }   // liftM

    /**
     * Lift a binary function to a monad.
     */
    fun <A, B, C> liftM2(f: (A) -> (B) -> C): (Option<A>) -> (Option<B>) -> Option<C> =
        {oa: Option<A> ->
            {ob: Option<B> ->
                oa.bind{a -> ob.bind{b -> some(f(a)(b))}}
            }
        }   // liftM2

    fun <A, B, C> liftM2(f: (A, B) -> C): (Option<A>) -> (Option<B>) -> Option<C> =
        liftM2(C2(f))

    /**
     * Lift a ternary function to a monad.
     */
    fun <A, B, C, D> liftM3(f: (A) -> (B) -> (C) -> D): (Option<A>) -> (Option<B>) -> (Option<C>) -> Option<D> =
        {oa: Option<A> ->
            {ob: Option<B> ->
                {oc: Option<C> ->
                    oa.bind{a -> ob.bind{b -> oc.bind{c -> some(f(a)(b)(c))}}}
                }
            }
        }   // liftM3

    fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (Option<A>) -> (Option<B>) -> (Option<C>) -> Option<D> =
        liftM3(C3(f))

    /**
     * Map the given function across the two Options.
     */
    fun <A, B, C> map2(oa: Option<A>, ob: Option<B>, f: (A) -> (B) -> C): Option<C> =
        oa.bind{a: A -> ob.map{b: B -> f(a)(b)}}

    fun <A, B, C> map2(oa: Option<A>, ob: Option<B>, f: (A, B) -> C): Option<C> =
        map2(oa, ob, C2(f))

    /**
     * Map the given function across the three Options.
     */
    fun <A, B, C, D> map3(oa: Option<A>, ob: Option<B>, oc: Option<C>, f: (A) -> (B) -> (C) -> D): Option<D> =
        oa.bind{a: A -> ob.bind{b: B -> oc.map{c: C -> f(a)(b)(c)}}}

    fun <A, B, C, D> map3(oa: Option<A>, ob: Option<B>, oc: Option<C>, f: (A, B, C) -> D): Option<D> =
        map3(oa, ob, oc, C3(f))

}   // OptionF
