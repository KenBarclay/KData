package com.adt.kotlin.hkfp.fp

/**
 * A suite of extension functions over unary functions. The extensions
 *   are from the functor, applicative, etc categories.
 *
 * @author	                    Ken Barclay
 * @since                       November 2019
 */

import com.adt.kotlin.hkfp.fp.FunctionF.compose
import com.adt.kotlin.hkfp.fp.FunctionF.forwardCompose



/**
 * Function compose: apply function g to an input x and apply
 *   function f to the result as in f(g(x)).
 *
 * compose:: (B -> C) * (A -> B) -> (A -> C)
 *
 * @param f     		        result function
 * @param g     		        intermediate function
 * @return      		        composed functions
 */
infix fun <A, B, C> ((B) -> C).compose(g: (A) -> B): (A) -> C = compose(this, g)

infix fun <A, B, C> ((B) -> C).o(g: (A) -> B): (A) -> C = compose(this, g)

/**
 * Function composition: apply function f to an input x and apply
 *   function g to the result as in g(f(x)).
 *
 * forwardComp:: (A -> B) * (B -> C) -> (A -> C)
 *
 * @param	f		            intermediate function
 * @param	g		            result function
 * @result      		        composed functions
 */
infix fun <A, B, C> ((A) -> B).forwardCompose(g: (B) -> C): (A) -> C = forwardCompose(this, g)

infix fun <A, B, C> ((A) -> B).fc(g: (B) -> C): (A) -> C = forwardCompose(this, g)

infix fun <A, B, C> ((A) -> B).pipe(g: (B) -> C): (A) -> C = forwardCompose(this, g)



// Functor extension functions:

/**
 * Apply the function to the content(s) of the function context.
 *
 * Examples:
 *   ({m -> 100 + m}fmap{n -> 3 * n})(1) == 303
 *   ({m -> 100 + m} dollar {n -> 3 * n})(1) == 303
 */
fun <A, B, C> ((A) -> B).fmap(f: (B) -> C): (A) -> C =
    {a: A -> f(this(a))}

/**
 * An infix symbol for fmap.
 */
infix fun <A, B, C> ((A) -> B).dollar(f: (B) -> C): (A) -> C = this.fmap(f)

/**
 * Replace all locations in the input with the given value.
 *
 * Examples:
 *   {m -> 100 + m}.replaceAll(9)(1) == 9
 */
fun <A, B, C> ((A) -> B).replaceAll(c: C): (A) -> C = this.fmap{_ -> c}

/**
 * Distribute the FN<A, (B, C)> over the pair to get (FN<A, B>, Fn<A, C>).
 *
 * Examples:
 *   pair = {m -> Pair(m - 100, m + 100)}.distribute()
 *   pair.first(0) == -100
 *   pair.second(0) == 100
 */
fun <A, B, C> ((A) -> Pair<B, C>).distribute(): Pair<(A) -> B, (A) -> C> =
    Pair(this.fmap{pr -> pr.first}, this.fmap{pr -> pr.second})



// Applicative extension functions:

/**
 * Apply the function wrapped in the function context to the content of the
 *   value also wrapped in a function context.
 *
 * Examples:
 *   ({m -> 100 * m}.ap({n -> n + 3}.fmap{p -> {q -> p + q}}))(5) == 508
 *   ({m -> 100 * m} apply {n -> n + 3}.fmap{p -> {q -> p + q}})(5) == 508
 *   ({m -> 100 * m} apply {n -> n + 3} dollar {p -> {q -> p + q}})(5) == 508
 */
fun <A, B, C> ((A) -> B).ap(f: (A) -> (B) -> C): (A) -> C =
    {a: A -> f(a)(this(a))}

/**
 * An infix symbol for ap.
 */
infix fun <A, B, C> ((A) -> B).apply(f: (A) -> (B) -> C): (A) -> C = this.ap(f)

/**
 * The product of two applicatives.
 *
 * Examples:
 *   ({m -> 100 * m}.product2{n -> 3 + n})(1) == (100, 4)
 *   ({m -> 100 * m}.product2{n -> 3 + n})(2) == (200, 5)
 */
fun <A, B, C> ((A) -> B).product2(fac: (A) -> C): (A) -> Pair<B, C> =
    fac.ap(this.fmap{b: B -> {c: C -> Pair(b, c)}})

/**
 * The product of three applicatives.
 *
 * Examples:
 *   ({m -> 100 * m}.product3({n -> 3 + n}, {p -> -p}))(1) == (100, 4, -1)
 *   ({m -> 100 * m}.product3({n -> 3 + n}, {p -> -p}))(2) == (200, 5, -2)
 */
fun <A, B, C, D> ((A) -> B).product3(fac: (A) -> C, fad: (A) -> D): (A) -> Triple<B, C, D> {
    val fabc: (A) -> Pair<B, C> = this.product2(fac)
    return fad.product2(fabc).fmap{t2 -> Triple(t2.second.first, t2.second.second, t2.first)}
}   // product3

/**
 * fmap2 is a binary version of fmap.
 *
 * Examples:
 *   ({m -> 100 * m}.famp2({n -> 3 + n}, {p -> {q -> p + q}}))(1) = 104
 *   ({m -> 100 * m}.famp2({n -> 3 + n}, {p -> {q -> p + q}}))(2) = 205
 */
fun <A, B, C, D> ((A) -> B).fmap2(fac: (A) -> C, f: (B) -> (C) -> D): (A) -> D =
    FunctionF.liftA2<A, B, C, D>(f)(this)(fac)

/**
 * fmap3 is a ternary version of fmap.
 *
 * Examples:
 *   ({m -> 100 * m}.fmap3({n -> 3 + n}, {p -> 10 + p}, {q -> {r -> {s -> q + r + s}}}))(1) == 115
 *   ({m -> 100 * m}.fmap3({n -> 3 + n}, {p -> 10 + p}, {q -> {r -> {s -> q + r + s}}}))(2) == 217
 */
fun <A, B, C, D, E> ((A) -> B).fmap3(fac: (A) -> C, fad: (A) -> D, f: (B) -> (C) -> (D) -> E): (A) -> E =
    FunctionF.liftA3<A, B, C, D, E>(f)(this)(fac)(fad)

/**
 * ap2 is a binary version of ap, defined in terms of ap.
 *
 * Examples:
 *   ({m -> 100 * m}.ap2({n -> 3 + n}, {q -> {r -> {s -> q + r + s}}}))(1) == 105
 *   ({m -> 100 * m}.ap2({n -> 3 + n}, {q -> {r -> {s -> q + r + s}}}))(2) == 207
 */
fun <A, B, C, D> ((A) -> B).ap2(fac: (A) -> C, f: (A) -> (B) -> (C) -> D): (A) -> D =
    fac.ap(this.ap(f))

/**
 * ap3 is a ternary version of ap, defined in terms of ap.
 *
 * Examples:
 *   ({m -> 100 * m}.ap3({n -> 3 + n}, {p -> 10 + p}, {q -> {r -> {s -> {t -> q + r + s + t}}}}))(1) == 116
 *   ({m -> 100 * m}.ap3({n -> 3 + n}, {p -> 10 + p}, {q -> {r -> {s -> {t -> q + r + s + t}}}}))(2) == 219
 */
fun <A, B, C, D, E> ((A) -> B).ap3(fac: (A) -> C, fad: (A) -> D, f: (A) -> (B) -> (C) -> (D) -> E): (A) -> E =
    fad.ap(fac.ap(this.ap(f)))



// Monad extension functions:

/**
 * Sequentially compose two actions, passing any value produced by the first
 *   as an argument to the second.
 *
 * Examples:
 *   ({m -> 100 * m}.bind{p -> {q -> p + q}})(1) == 101
 *   ({m -> 100 * m}.bind{p -> {q -> p + q}})(2) == 202
 */
fun <A, B, C> ((A) -> B).bind(f: (B) -> (A) -> C): (A) -> C =
    {a: A -> f(this(a))(a)}

fun <A, B, C> ((A) -> B).flatMap(f: (B) -> (A) -> C): (A) -> C = this.bind(f)

/**
 * Sequentially compose two actions, discarding any value produced by the first,
 *   like sequencing operators (such as the semicolon) in imperative languages.
 *
 * Examples:
 *   ({m -> 100 * m}.then{n -> 3 + n})(1) == 4
 *   ({m -> 100 * m}.then{n -> 3 + n})(2) == 5
 */
fun <A, B, C> ((A) -> B).then(fac: (A) -> C): (A) -> C =
    this.bind{_ -> fac}



// Profunctor extension functions:

/**
 * Map over both arguments at the same time.
 *
 * Examples:
 *   {m -> 100 * m}.dimap({n -> 3 + n}, {p -> 10 + p})(1) == 410
 *   {m -> 100 * m}.dimap({n -> 3 + n}, {p -> 10 + p})(2) == 510
 */
fun <A, B, C, D> ((A) -> B).dimap(f: (C) -> A, g: (B) -> D): (C) -> D =
    FunctionF.compose(FunctionF.compose(g, this), f)

/**
 * Map the first argument contravariantly.
 *
 * Examples:
 *
 *   ({m -> 100 * m}.lmap{n -> 3 + n})(1) == 400
 *   ({m -> 100 * m}.lmap{n -> 3 + n})(2) == 500
 */
fun <A, B, C> ((A) -> B).lmap(f: (C) -> A): (C) -> B =
    this.dimap(f, FunctionF.identity())

/**
 * Map the second argument covariantly.
 *
 * Examples:
 *   ({m -> 100 * m}.rmap{n -> 3 + n})(1) == 103
 *   ({m -> 100 * m}.rmap{n -> 3 + n})(2) == 203
 */
fun <A, B, C> ((A) -> B).rmap(g: (B) -> C): (A) -> C =
    this.dimap(FunctionF.identity(), g)