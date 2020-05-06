package com.adt.kotlin.data.immutable.tri

/**
 * The Try type represents a computation that may return a successfully computed value
 *   or result in an exception. Instances of Try[A], are either an instance of Success[A]
 *   or Failure[A]. The code is modelled on the Scala Try.
 *
 * The algebraic data type declaration is:
 *
 * datatype Try[A] = Failure
 *                 | Success A
 *
 * @param A                     the type of element
 *
 * @author	                    Ken Barclay
 * @since                       October 2014
 */

import com.adt.kotlin.data.immutable.tri.Try.Failure
import com.adt.kotlin.data.immutable.tri.Try.Success
import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3


object TryF {

    /**
     * Factory functions to create the base instances.
     */
    fun <A> failure(throwable: Throwable): Try<A> = Failure(throwable)
    fun <A> success(value: A): Try<A> = Success(value)

    /**
     * Constructs a Try using the parameter. This function will ensure any non-fatal
     *   exception is caught and a Failure object is returned.
     *
     * @param exp               the (lazy) expression to wrap in a Try
     * @return                  a Failure if the expression raises an exception; otherwise its value wrapped in a Success
     */
    fun <A> `try`(exp: () -> A): Try<A> {
        return try {
            Success(exp())
        } catch(ex: Exception) {
            Failure(ex)
        }
    }   // try



    // Functor extension functions:

    /**
     * Lift a function into the Try context.
     */
    fun <A, B> lift(f: (A) -> B): (Try<A>) -> Try<B> =
        {ta: Try<A> -> ta.fmap(f)}



    // Applicative extension functions:

    /**
     * Lift a function to actions.
     */
    fun <A, B> liftA(f: (A) -> B): (Try<A>) -> Try<B> =
        {ta: Try<A> ->
            ta.ap(success(f))
        }   // liftA

    /**
     * Lift a binary function to actions.
     *
     * Some functors support an implementation of lift2 that is more efficient than the
     *   default one. In particular, if fmap is an expensive operation, it is likely
     *   better to use liftA2 than to fmap over the structure and then use ap.
     */
    fun <A, B, C> liftA2(f: (A) -> (B) -> C): (Try<A>) -> (Try<B>) -> Try<C> =
        {ta: Try<A> ->
            {tb: Try<B> ->
                tb.ap(ta.fmap(f))
            }
        }   // liftA2

    fun <A, B, C> liftA2(f: (A, B) -> C): (Try<A>) -> (Try<B>) -> Try<C> =
        liftA2(C2(f))

    /**
     * Lift a ternary function to actions.
     */
    fun <A, B, C, D> liftA3(f: (A) -> (B) -> (C) -> D): (Try<A>) -> (Try<B>) -> (Try<C>) -> Try<D> =
        {ta: Try<A> ->
            {tb: Try<B> ->
                {tc: Try<C> ->
                    tc.ap(tb.ap(ta.fmap(f)))
                }
            }
        }   // liftA3

    fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (Try<A>) -> (Try<B>) -> (Try<C>) -> Try<D> =
        liftA3(C3(f))



    // Foldable extension functions:

    /**
     * Lift a function to a monad.
     */
    fun <A, B> liftM(f: (A) -> B): (Try<A>) -> Try<B> =
        {ta: Try<A> ->
            ta.bind{a: A -> success(f(a))}
        }   // liftM

    /**
     * Lift a binary function to a monad.
     */
    fun <A, B, C> liftM2(f: (A) -> (B) -> C): (Try<A>) -> (Try<B>) -> Try<C> =
        {ta: Try<A> ->
            {tb: Try<B> ->
                ta.bind{a: A -> tb.bind{b: B -> success(f(a)(b))}}
            }
        }   // liftM2

    fun <A, B, C> liftM2(f: (A, B) -> C): (Try<A>) -> (Try<B>) -> Try<C> =
        liftM2(C2(f))

    /**
     * Lift a ternary function to a monad.
     */
    fun <A, B, C, D> liftM3(f: (A) -> (B) -> (C) -> D): (Try<A>) -> (Try<B>) -> (Try<C>) -> Try<D> =
        {ta: Try<A> ->
            {tb: Try<B> ->
                {tc: Try<C> ->
                    ta.bind{a: A -> tb.bind{b: B -> tc.bind{c: C -> success(f(a)(b)(c))}}}
                }
            }
        }   // liftM3

    fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (Try<A>) -> (Try<B>) -> (Try<C>) -> Try<D> =
        liftM3(C3(f))

}   // TryF
