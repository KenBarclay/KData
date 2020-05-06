package com.adt.kotlin.data.immutable.either

/**
 * Either[A, B] = Left of A
 *              | Right of B
 *
 * This Either type is inspired by the Haskell Either data type. The Either type represents
 *   values with two possibilities: a value of type Either[A, B] is either Left[A] or Right[B].
 *
 * The Either type is sometimes used to represent a value which is either correct or an error;
 *   by convention, the Left constructor is used to hold an error value and the Right constructor
 *   is used to hold a correct value (mnemonic: "right" also means "correct").
 *
 * This Either type is right-biased, so functions such as map and bind apply only to the Right
 *   case. This right-bias makes this Either more convenient to use in a monadic context than
 *   the either/Either type avoiding the need for a right projection.
 *
 * @param A                     the type of Left elements
 * @param B                     the type of Right elements
 *
 * @author	                    Ken Barclay
 * @since	                    October 2019
 */

import com.adt.kotlin.data.immutable.either.Either.Left
import com.adt.kotlin.data.immutable.either.Either.Right

import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.List.Nil
import com.adt.kotlin.data.immutable.list.List.Cons
import com.adt.kotlin.data.immutable.list.ListBuffer
import com.adt.kotlin.data.immutable.list.ListBufferIF

import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3



object EitherF {

    /**
     * Factory functions to create the base instances.
     */
    fun <A, B> left(a: A): Either<A, B> = Left(a)
    fun <A, B> right(b: B): Either<A, B> = Right(b)

    /**
     * Extract from a list of either all the left elements. The elements
     *   are extracted in order.
     *
     * Examples:
     *   lefts([Left("ken"), Left("john"), Right(3), Right(7), Left("jessie)]) == ["ken", "john", "Jessie"]
     */
    fun <A, B> lefts(list: List<Either<A, B>>): List<A> {
        fun recLefts(list: List<Either<A, B>>, acc: ListBufferIF<A>): List<A> {
            return when (list) {
                is Nil -> acc.toList()
                is Cons -> {
                    val eab: Either<A, B> = list.head()
                    when (eab) {
                        is Left -> recLefts(list.tail(), acc.append(eab.value))
                        is Right -> recLefts(list.tail(), acc)
                    }
                }
            }
        }   // recLefts

        return recLefts(list, ListBuffer())
    }   // lefts

    /**
     * Extract from a list of either all the right elements. The elements
     *   are extracted in order.
     *
     * Examples:
     *   rights([Left("ken"), Left("john"), Right(3), Right(7), Left("jessie)]) == [3, 7]
     */
    fun <A, B> rights(list: List<Either<A, B>>): List<B> {
        fun recRights(list: List<Either<A, B>>, acc: ListBufferIF<B>): List<B> {
            return when (list) {
                is Nil -> acc.toList()
                is Cons -> {
                    val eab: Either<A, B> = list.head()
                    when (eab) {
                        is Left -> recRights(list.tail(), acc)
                        is Right -> recRights(list.tail(), acc.append(eab.value))
                    }
                }
            }
        }   // recRights

        return recRights(list, ListBuffer())
    }   // rights

    /**
     * Partition a list of either into two lists. All the left elements are
     *   extracted, in order, to the first component of the output. Similarly
     *   the right elements are extracted to the second component of the output.
     *
     * Examples:
     *   partition([Left("ken"), Left("john"), Right(3), Right(7), Left("jessie)]) == (["ken", "john", "Jessie"], [3, 7])
     */
    fun <A, B> partition(list: List<Either<A, B>>): Pair<List<A>, List<B>> {
        fun recPartition(list: List<Either<A, B>>, accLeft: ListBufferIF<A>, accRight: ListBufferIF<B>): Pair<List<A>, List<B>> {
            return when (list) {
                is Nil -> Pair(accLeft.toList(), accRight.toList())
                is Cons -> {
                    val eab: Either<A, B> = list.head()
                    when (eab) {
                        is Left -> recPartition(list.tail(), accLeft.append(eab.value), accRight)
                        is Right -> recPartition(list.tail(), accLeft, accRight.append(eab.value))
                    }
                }
            }
        }   // recPartition

        return recPartition(list, ListBuffer(), ListBuffer())
    }   // partition



    // Functor extension functions:

    /**
     * Lift a function into the Either context.
     */
    fun <A, B, C> lift(f: (B) -> C): (Either<A, B>) -> Either<A, C> =
        {eab: Either<A, B> -> eab.fmap(f)}



    // Applicative extension functions:

    /**
     * Lift a function to actions.
     */
    fun <A, B, C> liftA(f: (B) -> C): (Either<A, B>) -> Either<A, C> =
        {eab: Either<A, B> ->
            eab.ap(right(f))
        }   // liftA

    /**
     * Lift a binary function to actions.
     *
     * Some functors support an implementation of lift2 that is more efficient than the
     *   default one. In particular, if fmap is an expensive operation, it is likely
     *   better to use liftA2 than to fmap over the structure and then use ap.
     */
    fun <A, B, C, D> liftA2(f: (B) -> (C) -> D): (Either<A, B>) -> (Either<A, C>) -> Either<A, D> =
        {eab: Either<A, B> ->
            {eac: Either<A, C> ->
                eac.ap(eab.fmap(f))
            }
        }   // liftA2

    fun <A, B, C, D> liftA2(f: (B, C) -> D): (Either<A, B>) -> (Either<A, C>) -> Either<A, D> =
        liftA2(C2(f))

    /**
     * Lift a ternary function to actions.
     */
    fun <A, B, C, D, E> liftA3(f: (B) -> (C) -> (D) -> E): (Either<A, B>) -> (Either<A, C>) -> (Either<A, D>) -> Either<A, E> =
        {eab: Either<A, B> ->
            {eac: Either<A, C> ->
                {ead: Either<A, D> ->
                    ead.ap(eac.ap(eab.fmap(f)))
                }
            }
        }   // liftA3

    fun <A, B, C, D, E> liftA3(f: (B, C, D) -> E): (Either<A, B>) -> (Either<A, C>) -> (Either<A, D>) -> Either<A, E> =
        liftA3(C3(f))



    // Monad extension functions:

    /**
     * Lift a function to a monad.
     */
    fun <A, B, C> liftM(f: (B) -> C): (Either<A, B>) -> Either<A, C> =
        {eab: Either<A, B> ->
            eab.bind{b: B -> right<A, C>(f(b))}
        }   // liftM

    /**
     * Lift a binary function to a monad.
     */
    fun <A, B, C, D> liftM2(f: (B) -> (C) -> D): (Either<A, B>) -> (Either<A, C>) -> Either<A, D> =
        {eab: Either<A, B> ->
            {eac: Either<A, C> ->
                eab.bind{b: B -> eac.bind{c: C -> right<A, D>(f(b)(c))}}
            }
        }   // liftM2

    fun <A, B, C, D> liftM2(f: (B, C) -> D): (Either<A, B>) -> (Either<A, C>) -> Either<A, D> =
        liftM2(C2(f))

    /**
     * Lift a ternary function to a monad.
     */
    fun <A, B, C, D, E> liftM3(f: (B) -> (C) -> (D) -> E): (Either<A, B>) -> (Either<A, C>) -> (Either<A, D>) -> Either<A, E> =
        {eab: Either<A, B> ->
            {eac: Either<A, C> ->
                {ead: Either<A, D> ->
                    eab.bind{b: B -> eac.bind{c: C -> ead.bind{d: D -> right<A, E>(f(b)(c)(d))}}}
                }
            }
        }   // liftM3

    fun <A, B, C, D, E> liftM3(f: (B, C, D) -> E): (Either<A, B>) -> (Either<A, C>) -> (Either<A, D>) -> Either<A, E> =
        liftM3(C3(f))

    /**
     * Map the given function across the two Eithers.
     */
    fun <A, B, C, D> map2(eiab: Either<A, B>, eiac: Either<A, C>, f: (B) -> (C) -> D): Either<A, D> =
        eiab.bind{b: B -> eiac.map{c: C -> f(b)(c)}}

    fun <A, B, C, D> map2(eiab: Either<A, B>, eiac: Either<A, C>, f: (B, C) -> D): Either<A, D> =
        map2(eiab, eiac, C2(f))

    /**
     * Map the given function across the three Options.
     */
    fun <A, B, C, D, E> map3(eiab: Either<A, B>, eiac: Either<A, C>, eiad: Either<A, D>, f: (B) -> (C) -> (D) -> E): Either<A, E> =
        eiab.bind{b: B -> eiac.bind{c: C -> eiad.map{d: D -> f(b)(c)(d)}}}

    fun <A, B, C, D, E> map3(eiab: Either<A, B>, eiac: Either<A, C>, eiad: Either<A, D>, f: (B, C, D) -> E): Either<A, E> =
        map3(eiab, eiac, eiad, C3(f))

}   // EitherF
