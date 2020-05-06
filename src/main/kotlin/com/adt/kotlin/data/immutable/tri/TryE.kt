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
import com.adt.kotlin.data.immutable.tri.TryF.liftA2
import com.adt.kotlin.data.immutable.tri.TryF.liftA3
import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3

import com.adt.kotlin.hkfp.fp.FunctionF.constant

import com.adt.kotlin.hkfp.typeclass.Monoid



// Contravariant extension functions:

/**
 * Return the value if this is a Success or the given default argument if this
 *   is a Failure.
 *
 * Examples:
 *   success(123).getOrElse(100) == 123
 *   failure(Exception("error")).getOrElse(100) == 100
 *
 * @param defaultValue      return value if this is a Failure
 * @return                  the value wrapped by this Success or the given default
 */
fun <A> Try<A>.getOrElse(defaultValue: A): A {
    return when (this) {
        is Failure -> defaultValue
        is Success -> this.value
    }
}   // getOrElse

/**
 * Apply the given function f if this is a Failure, otherwise returns this if
 *   this is a Success.
 *
 * Examples:
 *   success(123).recoverWith{_ -> success(456)} == success(123)
 *   failure(Exception("error")).recoverWith{_ -> success(456)} == success(456)
 */
fun <A> Try<A>.recoverWith(f: (Throwable) -> Try<A>): Try<A> =
        this.fold({th: Throwable -> f(th)}, {a: A -> Success(a)})



// Functor extension functions:

/**
 * Apply the function to the content(s) of the context.
 *
 * Examples:
 *   success(123).fmap(isOdd) == success(true)
 *   failure(Exception("error")).fmap(isOdd) == failure(Exception("error"))
 */
fun <A, B> Try<A>.fmap(f: (A) -> B): Try<B> = this.map(f)

/**
 * An infix symbol for fmap.
 */
infix fun <A, B> ((A) -> B).dollar(v: Try<A>): Try<B> = v.fmap(this)

/**
 * Replace all locations in the input with the given value.
 *
 * Examples:
 *   success(123).replaceAll(true) == success(true)
 *   failure(Exception("error")).replaceAll(true) == failure(Exception("error"))
 */
fun <A, B> Try<A>.replaceAll(b: B): Try<B> = this.fmap{_ -> b}

/**
 * Distribute the Validation<E, (A, B)> over the pair to get (Validation<E, A>, Validation<E, B>).
 *
 * Examples:
 *   success(("Ken", 123)).distribute() == (success("Ken"), success(123))
 *   failure(Exception("error")).distribute() == (failure(Exception("error")), failure(Exception("error")))
 */
fun <A, B> Try<Pair<A, B>>.distribute(): Pair<Try<A>, Try<B>> =
    Pair(this.fmap{pr -> pr.first}, this.fmap{pr -> pr.second})

/**
 * Inject a to the left of the b's in this try.
 *
 * Examples:
 *   success(123).injectLeft(true) == success((true, 123))
 *   failure(Exception("error")).injectLeft(true) == failure(Exception("error"))
 */
fun <A, B> Try<B>.injectLeft(a: A): Try<Pair<A, B>> = this.fmap{b: B -> Pair(a, b)}

/**
 * Inject b to the right of the a's in this try.
 *
 * Examples:
 *   success(123).injectRight(true) == success((123, true))
 *   failure(Exception("error")).injectRight(true) == failure(Exception("error"))
 */
fun <A, B> Try<A>.injectRight(b: B): Try<Pair<A, B>> = this.fmap{a: A -> Pair(a, b)}

/**
 * Twin all the a's in this try with itself.
 *
 * Examples:
 *   success(123).pair() == success((123, 123))
 *   failure(Exception("error")).pair() == failure(Exception("error"))
 */
fun <A> Try<A>.pair(): Try<Pair<A, A>> = this.fmap{a: A -> Pair(a, a)}

/**
 * Pair all the a's in this try with the result of the function application.
 *
 * Examples:
 *   success(123).product(isOdd) == success((123, true))
 *   failure(Exception("error")).product(isOdd) == failure(Exception("error"))
 */
fun <A, B> Try<A>.product(f: (A) -> B): Try<Pair<A, B>> = this.fmap{a: A -> Pair(a, f(a))}



// Applicative extension functions:

/**
 * Apply the function wrapped in a context to the content of the
 *   value also wrapped in a matching context.
 *
 * Examples:
 *   success(123).ap(success{n -> n + 1}) == success(124)
 *   failure(Exception("error")).ap(success{n -> n + 1}) == failure(Exception("error"))
 */
fun <A, B> Try<A>.ap(f: Try<(A) -> B>): Try<B> =
    when (f) {
        is Failure -> Failure(f.throwable)
        is Success -> this.map(f.value)
    }   // ap

/**
 * An infix symbol for ap.
 */
infix fun <A, B> Try<(A) -> B>.apply(v: Try<A>): Try<B> = v.ap(this)

/**
 * Sequence actions, discarding the value of the first argument.
 *
 * Examples:
 *   success(123).sDF(success(456)) == success(456)
 *   success(123).sDF(failure(Exception("error"))) == failure(Exception("error"))
 *   failure(Exception("error")).sDF(success(456)) == failure(Exception("error"))
 *   failure(Exception("error")).sDF(failure(Exception("bug"))) == failure(Exception("bug"))
 */
fun <A, B> Try<A>.sDF(tb: Try<B>): Try<B> {
    fun constant(b: B): (A) -> B = {_: A -> b}
    return liftA2(::constant)(tb)(this)
}   // sDF

/**
 * Sequence actions, discarding the value of the second argument.
 *
 * Examples:
 *   success(123).sDS(success(456)) == success(123)
 *   success(123).sDS(failure(Exception("bug"))) == failure(Exception("bug"))
 *   failure(Exception("error")).sDS(success(456)) == failure(Exception("error"))
 *   failure(Exception("error")).sDS(failure(Exception("bug"))) == failure(Exception("error"))
 */
fun <A, B> Try<A>.sDS(tb: Try<B>): Try<A> {
    fun constant(a: A): (B) -> A = {_: B -> a}
    return liftA2<A, B, A>(::constant)(this)(tb)
}

/**
 * The product of two applicatives.
 *
 * Examples:
 *   success(123).product2(success(456)) == success((123, 456))
 *   success(123).product2(failure(Exception("bug"))) == failure(Exception("bug"))
 *   failure(Exception("error")).product2(success(456)) == failure(Exception("error"))
 *   failure(Exception("error")).product2(failure(Exception("bug"))) == failure(Exception("error"))
 */
fun <A, B> Try<A>.product2(tb: Try<B>): Try<Pair<A, B>> =
    tb.ap(this.fmap{a: A -> {b: B -> Pair(a, b)}})

/**
 * The product of three applicatives.
 *
 * Examples:
 *
 */
fun <A, B, C> Try<A>.product3(tb: Try<B>, tc: Try<C>): Try<Triple<A, B, C>> {
    val ttab: Try<Pair<A, B>> = this.product2(tb)
    return tc.product2(ttab).fmap{t2 -> Triple(t2.second.first, t2.second.second, t2.first) }
}   // product3

/**
 * fmap2 is a binary version of fmap.
 *
 * Examples:
 *   success(123).fmap2(success(456)){m -> {n -> m + n}} == success(579)
 *   success(123).fmap2(failure(Exception("bug"))){m -> {n -> m + n}} == failure(Exception("bug"))
 *   failure(Exception("error")).fmap2(success(456)){m -> {n -> m + n}} == failure(Exception("error"))
 *   failure(Exception("error")).fmap2(failure(Exception("bug"))){m -> {n -> m + n}} == failure(Exception("error"))
 */
fun <A, B, C> Try<A>.fmap2(tb: Try<B>, f: (A) -> (B) -> C): Try<C> =
    liftA2(f)(this)(tb)

fun <A, B, C> Try<A>.fmap2(tb: Try<B>, f: (A, B) -> C): Try<C> =
    this.fmap2(tb, C2(f))

/**
 * fmap3 is a ternary version of fmap.
 *
 * Examples:
 *
 */
fun <A, B, C, D> Try<A>.fmap3(tb: Try<B>, tc: Try<C>, f: (A) -> (B) -> (C) -> D): Try<D> =
    liftA3(f)(this)(tb)(tc)

fun <A, B, C, D> Try<A>.fmap3(tb: Try<B>, tc: Try<C>, f: (A, B, C) -> D): Try<D> =
    this.fmap3(tb, tc, C3(f))

/**
 * ap2 is a binary version of ap, defined in terms of ap.
 *
 * Examples:
 *   success(123).ap2(success(456), success{m -> {n -> m + n}}) == success(579)
 *   success(123).ap2(failure(Exception("bug")), success{m -> {n -> m + n}}) == failure(Exception("bug"))
 *   failure(Exception("error")).ap2(success(456), success{m -> {n -> m + n}}) == failure(Exception("error"))
 *   failure(Exception("error")).ap2(failure(Exception("bug")), success{m -> {n -> m + n}})== failure(Exception("error"))
 */
fun <A, B, C> Try<A>.ap2(tb: Try<B>, f: Try<(A) -> (B) -> C>): Try<C> =
    tb.ap(this.ap(f))

/**
 * ap3 is a ternary version of ap, defined in terms of ap.
 *
 * Examples:
 *
 */
fun <A, B, C, D> Try<A>.ap3(tb: Try<B>, tc: Try<C>, f: Try<(A) -> (B) -> (C) -> D>): Try<D> =
    tc.ap(tb.ap(this.ap(f)))



// Monad extension functions:

/**
 * Sequentially compose two actions, passing any value produced by the first
 *   as an argument to the second.
 *
 * Examples:
 *   success(123).bind{n -> success(n % 2 == 1)} == success(true)
 *   failure(Exception("error")).bind{n -> success(n % 2 == 1)} == failure(Exception("error"))
 */
fun <A, B> Try<A>.bind(f: (A) -> Try<B>): Try<B> =
    when (this) {
        is Failure -> Failure(this.throwable)
        is Success -> try {
            f(this.value)
        } catch(ex: Exception) {
            Failure<B>(ex)
        }
    }   // bind

fun <A, B> Try<A>.flatMap(f: (A) -> Try<B>): Try<B> = this.bind(f)

/**
 * Sequentially compose two actions, discarding any value produced by the first,
 *   like sequencing operators (such as the semicolon) in imperative languages.
 *
 * Examples:
 *   success(123).then(success(456)) == success(456)
 *   success(123).then(failure(Exception("bug"))) == failure(Exception("bug"))
 *   failure(Exception("error")).then(success(456)) == failure(Exception("error"))
 *   failure(Exception("error")).then(failure(Exception("bug"))) == failure(Exception("error"))
 */
fun <A, B> Try<A>.then(tb: Try<B>): Try<B> = this.bind{_ -> tb}



// Foldable extension functions:

/**
 * Combine the elements of a structure using a monoid.
 *
 * Examples:
 *   success(123).fold(intAddMonoid) == 123
 *   failure(Exception("error")).fold(intAddMonoid) == 0
 */
fun <A> Try<A>.fold(md: Monoid<A>): A =
    this.foldLeft(md.empty){b -> {a -> md.combine(b, a)}}

/**
 * Map each element of the structure to a monoid, and combine the results.
 *
 * Examples:
 *   success(123).foldmap(intAddMonoid){n -> n + 1} == 124
 *   failure(Exception("error")).foldmap(intAddMonoid){n -> n + 1} == 0
 */
fun <A, B> Try<A>.foldMap(md: Monoid<B>, f: (A) -> B): B =
    this.foldLeft(md.empty){b -> {a -> md.combine(b, f(a))}}

/**
 * foldLeft is a higher-order function that folds a binary function into this
 *   context.
 *
 * Examples:
 *   success(123).foldLeft(10){m -> {n -> m + n}} = 133
 *   failure(Exception("error")).foldLeft(0){m -> {n -> m + n}} = 10
 */
fun <A, B> Try<A>.foldLeft(e: B, f: (B) -> (A) -> B): B =
    when (this) {
        is Failure -> e
        is Success -> f(e)(this.value)
    }   // foldLeft

fun <A, B> Try<A>.foldLeft(e: B, f: (B, A) -> B): B =
    this.foldLeft(e, C2(f))

/**
 * foldRight is a higher-order function that folds a binary function into this
 *   context.
 *
 * Examples:
 *   success(123).foldRight(10){m -> {n -> m + n}} = 133
 *   failure(Exception("error")).foldRight(0){m -> {n -> m + n}} = 10
 */
fun <A, B> Try<A>.foldRight(e: B, f: (A) -> (B) -> B): B =
    when (this) {
        is Failure -> e
        is Success -> f(this.value)(e)
    }   // foldRight

fun <A, B> Try<A>.foldRight(e: B, f: (A, B) -> B): B =
    this.foldRight(e, C2(f))
