package com.adt.kotlin.data.immutable.tuple

/**
 * A Tuple2 represents a pair of values.
 *
 * @author	                    Ken Barclay
 * @since                       October 2018
 */

import com.adt.kotlin.data.immutable.tuple.Tuple2F.liftA2
import com.adt.kotlin.data.immutable.tuple.Tuple2F.liftA3
import com.adt.kotlin.hkfp.typeclass.Monoid



// Functor extension functions:

fun <A, B, C> Tuple2<A, B>.fmap(f: (B) -> C): Tuple2<A, C> = this.map(f)

/**
 * An infix symbol for fmap.
 */
infix fun <A, B, C> ((B) -> C).dollar(v: Tuple2<A, B>): Tuple2<A, C> = v.fmap(this)

fun <A, B, C> Tuple2<A, B>.replaceAll(c: C): Tuple2<A, C>  = this.fmap{_ -> c}

fun <A, B, C> Tuple2<A, Tuple2<B, C>>.distribute(): Tuple2<Tuple2<A, B>, Tuple2<A, C>> =
    Tuple2(this.fmap{pr -> pr.a}, this.fmap{pr -> pr.b})

/**
 * Inject c to the left of the b's in this tuple.
 */
fun <A, B, C> Tuple2<A, B>.injectL(c: C): Tuple2<A, Pair<C, B>> = this.fmap{b: B -> Pair(c, b)}

/**
 * Inject c to the right of the b's in this tuple.
 */
fun <A, B, C> Tuple2<A, B>.injectR(c: C): Tuple2<A, Pair<B, C>> = this.fmap{b: B -> Pair(b, c)}

/**
 * Twin all the b's in this tuple with itself.
 */
fun <A, B> Tuple2<A, B>.pair(): Tuple2<A, Pair<B, B>> = this.fmap{b: B -> Pair(b, b)}

/**
 * Pair all the b's in this tuple with the result of the function application.
 */
fun <A, B, C> Tuple2<A, B>.product(f: (B) -> C): Tuple2<A, Pair<B, C>> = this.fmap{b: B -> Pair(b, f(b))}



// Bifunctor extension functions:

fun <A, B, C, D> Tuple2<A, C>.bimap(f: (A) -> B, g: (C) -> D): Tuple2<B, D> =
    Tuple2(f(this.a), g(this.b))

fun <A, B, C> Tuple2<A, C>.first(f: (A) -> B): Tuple2<B, C> =
    this.bimap(f){c -> c}

fun <A, C, D> Tuple2<A, C>.second(g: (C) -> D): Tuple2<A, D> =
    this.bimap({a -> a}, g)



// Applicative extension functions:

/**
 * Apply the function wrapped in a context to the content of the
 *   value also wrapped in a matching context.
 */
fun <A, B, C> Tuple2<A, B>.ap(md: Monoid<A>, f: Tuple2<A, (B) -> C>): Tuple2<A, C> =
    Tuple2(md.combine(this.a, f.a), f.b(this.b))

/**
 * Sequence actions, discarding the value of the first argument.
 */
fun <A, B, C> Tuple2<A, B>.sDF(md: Monoid<A>, tac: Tuple2<A, C>): Tuple2<A, C> =
    tac.ap(md, this.replaceAll{c -> c})

/**
 * Sequence actions, discarding the value of the second argument.
 */
fun <A, B, C> Tuple2<A, B>.sDS(md: Monoid<A>, tac: Tuple2<A, C>): Tuple2<A, B> =
    liftA2(md){b: B -> {_: C -> b}}(this)(tac)

/**
 * The product of two applicatives.
 */
fun <A, B, C> Tuple2<A, B>.product2(md: Monoid<A>, tac: Tuple2<A, C>): Tuple2<A, Tuple2<B, C>> =
    tac.ap(md, this.fmap{b: B -> {c: C -> Tuple2(b, c)}})

/**
 * The product of three applicatives.
 */
fun <A, B, C, D> Tuple2<A, B>.product3(md: Monoid<A>, tac: Tuple2<A, C>, tad: Tuple2<A, D>): Tuple2<A, Tuple3<B, C, D>> {
    val tabc: Tuple2<A, Tuple2<B, C>> = this.product2(md, tac)
    return tad.product2(md, tabc).fmap{t2 -> Tuple3(t2.b.a, t2.b.b, t2.a)}
}   // product

/**
 * fmap2 is a binary version of fmap.
 */
fun <A, B, C, D> Tuple2<A, B>.fmap2(md: Monoid<A>, tac: Tuple2<A, C>, f: (B) -> (C) -> D): Tuple2<A, D> =
    liftA2(md, f)(this)(tac)

/**
 * fmap3 is a ternary version of fmap.
 */
fun <A, B, C, D, E> Tuple2<A, B>.fmap3(md: Monoid<A>, tac: Tuple2<A, C>, tad: Tuple2<A, D>, f: (B) -> (C) -> (D) -> E): Tuple2<A, E> =
    liftA3(md, f)(this)(tac)(tad)

/**
 * ap2 is a binary version of ap, defined in terms of ap.
 */
fun <A, B, C, D> Tuple2<A, B>.ap2(md: Monoid<A>, tac: Tuple2<A, C>, f: Tuple2<A, (B) -> (C) -> D>): Tuple2<A, D> =
    tac.ap(md, this.ap(md, f))

/**
 * ap3 is a ternary version of ap, defined in terms of ap.
 */
fun <A, B, C, D, E> Tuple2<A, B>.ap3(md: Monoid<A>, tac: Tuple2<A, C>, tad: Tuple2<A, D>, f: Tuple2<A, (B) -> (C) -> (D) -> E>): Tuple2<A, E> =
    tad.ap(md, tac.ap(md, this.ap(md, f)))



// Monad extension functions:

/**
 * Sequentially compose two actions, passing any value produced by the first
 *   as an argument to the second.
 */
fun <A, B, C> Tuple2<A, B>.bind(md: Monoid<A>, f: (B) -> Tuple2<A, C>): Tuple2<A, C> {
    val tac: Tuple2<A, C> = f(this.b)
    return Tuple2(md.combine(this.a, tac.a), tac.b)
}

/**
 * Sequentially compose two actions, passing any value produced by the first
 *   as an argument to the second.
 */
fun <A, B, C> Tuple2<A, B>.flatMap(md: Monoid<A>, f: (B) -> Tuple2<A, C>): Tuple2<A, C> =
    this.bind(md, f)

/**
 * Sequentially compose two actions, discarding any value produced by the first,
 *   like sequencing operators (such as the semicolon) in imperative languages.
 */
fun <A, B, C> Tuple2<A, B>.then(md: Monoid<A>, tac: Tuple2<A, C>): Tuple2<A, C> =
    this.bind(md){_ -> tac}
