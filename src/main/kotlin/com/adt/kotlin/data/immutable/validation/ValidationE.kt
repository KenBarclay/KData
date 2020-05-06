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

import com.adt.kotlin.data.immutable.validation.Validation.Failure
import com.adt.kotlin.data.immutable.validation.Validation.Success
import com.adt.kotlin.data.immutable.validation.ValidationF.liftA2
import com.adt.kotlin.data.immutable.validation.ValidationF.liftA3
import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3
import com.adt.kotlin.hkfp.instances.NonEmptyListSemigroup

import com.adt.kotlin.hkfp.typeclass.Monoid
import com.adt.kotlin.hkfp.typeclass.Semigroup



infix fun <E, A, B> ((A) -> B).fmap(vea: ValidationNel<E, A>): ValidationNel<E, B> =
    vea.fmap(this)

infix fun <E, A, B> ValidationNel<E, (A) -> B>.ap(vea: ValidationNel<E, A>): ValidationNel<E, B> =
    vea.ap(NonEmptyListSemigroup(), this)



// Contravariant extension functions:

/**
 * Converts this to a Failure if the predicate is not satisfied.
 *
 * @param predicate         test criteria
 * @return                  a Failure if this is one or this does not satisfy the predicate; this otherwise
 */
fun <E, A> Validation<E, A>.filter(predicate: (A) -> Boolean, md: Monoid<E>): Validation<E, A> {
    return when (this) {
        is Failure -> this
        is Success -> if (predicate(this.value)) this else Failure(md.empty)
    }
}   // filter

/**
 * Returns the value if this is a Success or the given default argument if this is a Failure.
 *
 * @param defaultValue      return value if this is a Failure
 * @return                  the value wrapped by this Success or the given default
 */
fun <E, A> Validation<E, A>.getOrElse(defaultValue: A): A {
    return when(this) {
        is Failure -> defaultValue
        is Success -> this.value
    }
}   // getOrElse



// Functor extension functions:

/**
 * Apply the function to the content(s) of the context.
 *
 * Examples:
 *   Failure("Ken") = Failure("Ken").fmap{n -> 2 * n}
 *   Success(50) = Success(25).fmap{n -> 2 * n}
 */
fun <E, A, B> Validation<E, A>.fmap(f: (A) -> B): Validation<E, B> = this.map(f)

/**
 * An infix symbol for fmap.
 */
infix fun <E, A, B> ((A) -> B).dollar(v: Validation<E, A>): Validation<E, B> = v.fmap(this)

/**
 * Replace all locations in the input with the given value.
 *
 * Examples:
 *   Failure("Ken") = Failure("Ken").replaceAll(true)
 *   Success(true) = Success(25).replaceAll(true)
 */
fun <E, A, B> Validation<E, A>.replaceAll(b: B): Validation<E, B> = this.fmap{_ -> b}

/**
 * Distribute the Validation<E, (A, B)> over the pair to get (Validation<E, A>, Validation<E, B>).
 *
 * Examples:
 *   Failure("Ken") = Failure("Ken").distribute().first
 *   Failure("Ken") = Failure("Ken").distribute().second
 *   Success(25) = Success(Pair(25, false)).distribute().first
 *   Success(false) = Success(Pair(25, false)).distribute().second
 */
fun <E, A, B> Validation<E, Pair<A, B>>.distribute(): Pair<Validation<E, A>, Validation<E, B>> =
    Pair(this.fmap{pr -> pr.first}, this.fmap{pr -> pr.second})

/**
 * Inject b to the left of the a's in this validation.
 */
fun <E, A, B> Validation<E, A>.injectLeft(b: B): Validation<E, Pair<B, A>> = this.fmap{a: A -> Pair(b, a)}

/**
 * Inject b to the right of the a's in this validation.
 */
fun <E, A, B> Validation<E, A>.injectRight(b: B): Validation<E, Pair<A, B>> = this.fmap{a: A -> Pair(a, b)}

/**
 * Twin all the a's in this validation with itself.
 */
fun <E, A> Validation<E, A>.pair(): Validation<E, Pair<A, A>> = this.fmap{a: A -> Pair(a, a)}

/**
 * Pair all the a's in this validation with the result of the function application.
 */
fun <E, A, B> Validation<E, A>.product(f: (A) -> B): Validation<E, Pair<A, B>> = this.fmap{a: A -> Pair(a, f(a))}



// Applicative extension functions:

/**
 * Apply the function wrapped in a context to the content of the
 *   value also wrapped in a matching context.
 *
 * Examples:
 *   failure("Ken") = failure("Ken").ap(stringSemigroup, success{n: Int -> (n % 2 == 0)})
 *   success(false) = success(25).ap(stringSemigroup, success{n: Int -> (n % 2 == 0)})
 */
fun <E, A, B> Validation<E, A>.ap(se: Semigroup<E>, f: Validation<E, (A) -> B>): Validation<E, B> =
    when (this) {
        is Failure -> when (f) {
            is Failure -> Failure(se.combine(this.value, f.value))
            is Success -> Failure(this.value)
        }
        is Success -> when (f) {
            is Failure -> Failure(f.value)
            is Success -> Success(f.value(this.value))
        }
    }   // ap

////fun <E, A, B> ValidationNel<E, A>.ap(f: ValidationNel<E, (A) -> B>): ValidationNel<E, B> =
////    this.ap(NonEmptyListSemigroup(), f)

/**
 * Sequence actions, discarding the value of the receiver.
 */
fun <E, A, B> Validation<E, A>.sDF(se: Semigroup<E>, veb: Validation<E, B>): Validation<E, B> =
    veb.ap(se, this.replaceAll{b -> b})

fun <E, A, B> ValidationNel<E, A>.sDF(veb: ValidationNel<E, B>): ValidationNel<E, B> =
    this.sDF(NonEmptyListSemigroup(), veb)

/**
 * Sequence actions, discarding the value of the argument.
 */
fun <E, A, B> Validation<E, A>.sDS(se: Semigroup<E>, veb: Validation<E, B>): Validation<E, A> =
    liftA2(se){a: A -> {_: B -> a}}(this)(veb)

fun <E, A, B> ValidationNel<E, A>.sDS(veb: ValidationNel<E, B>): ValidationNel<E, A> =
    this.sDS(NonEmptyListSemigroup(), veb)

/**
 * The product of two applicatives.
 */
fun <E, A, B> Validation<E, A>.product2(se: Semigroup<E>, veb: Validation<E, B>): Validation<E, Pair<A, B>> =
    veb.ap(se, this.fmap{a -> {b: B -> Pair(a, b) }})

fun <E, A, B> ValidationNel<E, A>.product2(veb: ValidationNel<E, B>): ValidationNel<E, Pair<A, B>> =
    this.product2(NonEmptyListSemigroup(), veb)

/**
 * The product of three applicatives.
 */
fun <E, A, B, C> Validation<E, A>.product3(se: Semigroup<E>, veb: Validation<E, B>, vec: Validation<E, C>): Validation<E, Triple<A, B, C>> {
    val veab: Validation<E, Pair<A, B>> = this.product2(se, veb)
    return vec.product2(se, veab).fmap{t2 -> Triple(t2.second.first, t2.second.second, t2.first)}
}   // product3

fun <E, A, B, C> ValidationNel<E, A>.product3(veb: ValidationNel<E, B>, vec: ValidationNel<E, C>): ValidationNel<E, Triple<A, B, C>> =
    this.product3(NonEmptyListSemigroup(), veb, vec)

/**
 * fmap2 is a binary version of fmap.
 */
fun <E, A, B, C> Validation<E, A>.fmap2(se: Semigroup<E>, veb: Validation<E, B>, f: (A) -> (B) -> C): Validation<E, C> =
    liftA2(se, f)(this)(veb)

fun <E, A, B, C> Validation<E, A>.fmap2(se: Semigroup<E>, veb: Validation<E, B>, f: (A, B) -> C): Validation<E, C> =
    this.fmap2(se, veb, C2(f))

fun <E, A, B, C> ValidationNel<E, A>.fmap2(veb: ValidationNel<E, B>, f: (A) -> (B) -> C): ValidationNel<E, C> =
    this.fmap2(NonEmptyListSemigroup(), veb, f)

fun <E, A, B, C> ValidationNel<E, A>.fmap2(veb: ValidationNel<E, B>, f: (A, B) -> C): ValidationNel<E, C> =
    this.fmap2(veb, C2(f))

/**
 * fmap3 is a ternary version of fmap.
 */
fun <E, A, B, C, D> Validation<E, A>.fmap3(se: Semigroup<E>, veb: Validation<E, B>, vec: Validation<E, C>, f: (A) -> (B) -> (C) -> D): Validation<E, D> =
    liftA3(se, f)(this)(veb)(vec)

fun <E, A, B, C, D> Validation<E, A>.fmap3(se: Semigroup<E>, veb: Validation<E, B>, vec: Validation<E, C>, f: (A, B, C) -> D): Validation<E, D> =
    this.fmap3(se, veb, vec, C3(f))

fun <E, A, B, C, D> ValidationNel<E, A>.fmap3(veb: ValidationNel<E, B>, vec: ValidationNel<E, C>, f: (A) -> (B) -> (C) -> D): ValidationNel<E, D> =
    this.fmap3(NonEmptyListSemigroup(), veb, vec, f)

fun <E, A, B, C, D> ValidationNel<E, A>.fmap3(veb: ValidationNel<E, B>, vec: ValidationNel<E, C>, f: (A, B, C) -> D): ValidationNel<E, D> =
    this.fmap3(veb, vec, C3(f))

/**
 * ap2 is a binary version of ap, defined in terms of ap.
 */
fun <E, A, B, C> Validation<E, A>.ap2(se: Semigroup<E>, veb: Validation<E, B>, f: Validation<E, (A) -> (B) -> C>): Validation<E, C> =
    veb.ap(se, this.ap(se, f))

fun <E, A, B, C> ValidationNel<E, A>.ap2(veb: ValidationNel<E, B>, f: ValidationNel<E, (A) -> (B) -> C>): ValidationNel<E, C> =
    this.ap2(NonEmptyListSemigroup(), veb, f)

/**
 * ap3 is a ternary version of ap, defined in terms of ap.
 */
fun <E, A, B, C, D> Validation<E, A>.ap3(se: Semigroup<E>, veb: Validation<E, B>, vec: Validation<E, C>, f: Validation<E, (A) -> (B) -> (C) -> D>): Validation<E, D> =
    vec.ap(se, veb.ap(se, this.ap(se, f)))

fun <E, A, B, C, D> ValidationNel<E, A>.ap3(veb: ValidationNel<E, B>, vec: ValidationNel<E, C>, f: ValidationNel<E, (A) -> (B) -> (C) -> D>): ValidationNel<E, D> =
    this.ap3(NonEmptyListSemigroup(), veb, vec, f)



// Monad extension functions:

/**
 * Sequentially compose two actions, passing any value produced by the first
 *   as an argument to the second.
 *
 * Examples:
 *   failure("") = failure("Ken").bind(stringMonoid){n -> success(n % 2 == 0)}
 *   success(false) = success(25).bind(stringMonoid){n -> success(n % 2 == 0)}
 */
fun <E, A, B> Validation<E, A>.bind(me: Monoid<E>, f: (A) -> Validation<E, B>): Validation<E, B> =
    when (this) {
        is Failure -> Failure(me.empty)
        is Success -> f(this.value)
    }   // bind

fun <E, A, B> Validation<E, A>.flatMap(me: Monoid<E>, f: (A) -> Validation<E, B>): Validation<E, B> =
    this.bind(me, f)

/**
 * Sequentially compose two actions, discarding any value produced by the first,
 *   like sequencing operators (such as the semicolon) in imperative languages.
 *
 * Examples:
 *   failure("") = failure("Ken").then(stringMonoid, success(25))
 *   failure("Ken") = success(25).then(stringMonoid, failure("Ken"))
 *   success(25) = success(25).then(stringMonoid, success(25))
 */
fun <E, A, B> Validation<E, A>.then(me: Monoid<E>, veb: Validation<E, B>): Validation<E, B> =
    this.bind(me){_ -> veb}



// Foldable extension functions:

/**
 * Map each element of the structure to a monoid, and combine the results.
 *
 * Examples:
 *   failure("Ken").foldMap(intAddMonoid){n -> 2 * n} = 0
 *   success(25).foldMap(intAddMonoid){n -> 2 * n} = 50
 */
fun <E, A, B> Validation<E, A>.foldMap(md: Monoid<B>, f: (A) -> B): B =
    when (this) {
        is Failure -> md.empty
        is Success -> f(this.value)
    }   // foldMap

/**
 * foldLeft is a higher-order function that folds a binary function into this
 *   context.
 *
 * Examples:
 *   failure("Ken").foldLeft(0){m -> {n -> m + n}} = 0
 *   success(25).foldLeft(10){m -> {n -> m + n}} = 35
 */
fun <E, A, B> Validation<E, A>.foldLeft(e: B, f: (B) -> (A) -> B): B =
    when (this) {
        is Failure -> e
        is Success -> f(e)(this.value)
    }   // foldLeft

fun <E, A, B> Validation<E, A>.foldLeft(e: B, f: (B, A) -> B): B =
    this.foldLeft(e, C2(f))

/**
 * foldRight is a higher-order function that folds a binary function into this
 *   context.
 *
 * Examples:
 *   failure("Ken").foldRight(2){m -> {n -> m * n}} = 2
 *   success(25).foldRight(2){m -> {n -> m * n}} = 50
 */
fun <E, A, B> Validation<E, A>.foldRight(e: B, f: (A) -> (B) -> B): B =
    when (this) {
        is Failure -> e
        is Success -> f(this.value)(e)
    }   // foldRight

fun <E, A, B> Validation<E, A>.foldRight(e: B, f: (A, B) -> B): B =
    this.foldRight(e, C2(f))
