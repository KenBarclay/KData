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

import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.ListF

import com.adt.kotlin.data.immutable.either.Either.Left
import com.adt.kotlin.data.immutable.either.Either.Right
import com.adt.kotlin.data.immutable.either.EitherF.left
import com.adt.kotlin.data.immutable.either.EitherF.right
import com.adt.kotlin.data.immutable.list.fmap
import com.adt.kotlin.data.immutable.nel.NonEmptyList
import com.adt.kotlin.data.immutable.nel.NonEmptyListF
import com.adt.kotlin.data.immutable.nel.fmap
import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.OptionF.some
import com.adt.kotlin.data.immutable.option.fmap
import com.adt.kotlin.data.immutable.stream.Stream
import com.adt.kotlin.data.immutable.stream.StreamF
import com.adt.kotlin.data.immutable.stream.fmap
import com.adt.kotlin.data.immutable.validation.Validation
import com.adt.kotlin.data.immutable.validation.ValidationF.success
import com.adt.kotlin.data.immutable.validation.fmap

import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3

import com.adt.kotlin.hkfp.fp.FunctionF.id
import com.adt.kotlin.hkfp.fp.fmap

import com.adt.kotlin.hkfp.typeclass.Monoid



/**
 * Functions to support an applicative style of programming.
 *
 * Examples:
 *   {b: B -> ... C value} fmap eiAB ==> Either<A, C>
 */
infix fun <A, B, C> ((B) -> C).fmap(ei: Either<A, B>): Either<A, C> =
    ei.fmap(this)

infix fun <A, B, C> Either<A, (B) -> C>.appliedOver(eiab: Either<A, B>): Either<A, C> =
    eiab.ap(this)



// Contravariant extension functions:

/**
 * Obtain the value of the Left (if it is one), otherwise return the
 *   default value.
 *
 * Examples:
 *   Left("Ken").getLeftOrElse("DEFAULT") = "Ken"
 *   Right(2).getLeftOrElse("DEFAULT") = "DEFAULT"
 */
fun <A, B> Either<A, B>.getLeftOrElse(defaultvalue: A): A {
    return when (this) {
        is Left -> this.value
        is Right -> defaultvalue
    }
}

/**
 * Obtain the value of the Right (if it is one), otherwise return the
 *   default value.
 *
 * Examples:
 *   Left("Ken").getRightOrElse(0) = 0
 *   Right(2).getRightOrElse(0) = 2
 */
fun <A, B> Either<A, B>.getRightOrElse(defaultvalue: B): B {
    return when (this) {
        is Left -> defaultvalue
        is Right -> this.value
    }
}



// Functor extension functions:

/**
 * Apply the function to the content(s) of the context.
 *
 * Examples:
 *   Left("Ken").fmap{n -> n + 1} = Left("Ken")
 *   Right(2).fmap{n -> n + 1} = Right(3)
 */
fun <A, B, C> Either<A, B>.fmap(f: (B) -> C): Either<A, C> = this.map(f)

/**
 * An infix symbol for fmap.
 */
infix fun <A, B, C> ((B) -> C).dollar(v: Either<A, B>): Either<A, C> = v.fmap(this)

/**
 * Replace all locations in the input with the given value.
 *
 * Examples:
 *   Left("Ken").replaceAll(99) = Left("Ken")
 *   Right(2).replaceAll(99) = Right(99)
 */
fun <A, B, C> Either<A, B>.replaceAll(c: C): Either<A, C> = this.fmap{_ -> c}

/**
 * Distribute the Either<A, (B, C)> over the pair to get (Either<A, B>, Either<A, C>).
 *
 * Examples:
 *   Left("Ken").distribute() = Pair(Left("Ken"), Left("Ken"))
 *   Right(Pair(5, false)).distribute() = Pair(Right(5), Right(false))
 */
fun <A, B, C> Either<A, Pair<B, C>>.distribute(): Pair<Either<A, B>, Either<A, C>> =
    Pair(this.fmap{pr -> pr.first}, this.fmap{pr -> pr.second})

/**
 * Inject c to the left of the b's in this disjunction.
 *
 * Examples:
 *   Left(Ken).injectLeft(true) == Left(Ken)
 *   Right(2).injectLeft(true) == Right((true, 2))
 */
fun <A, B, C> Either<A, B>.injectLeft(c: C): Either<A, Pair<C, B>> = this.fmap{b: B -> Pair(c, b)}

/**
 * Inject c to the right of the b's in this disjunction.
 *
 * Examples:
 *   Left(Ken).injectRight(false) == Left(Ken)
 *   Right(2).injectRight(false) == Right((2, false))
 */
fun <A, B, C> Either<A, B>.injectRight(c: C): Either<A, Pair<B, C>> = this.fmap{b: B -> Pair(b, c)}

/**
 * Twin all the b's in this disjunction with itself.
 *
 * Examples:
 *   Left(Ken).pair() == Left(Ken)
 *   Right(2).pair() == Right((2, 2))
 */
fun <A, B> Either<A, B>.pair(): Either<A, Pair<B, B>> = this.fmap{b: B -> Pair(b, b)}

/**
 * Pair all the b's in this disjunction with the result of the function application.
 *
 * Examples:
 *   Left(Ken).product{n -> n + n} == Left(Ken)
 *   Right(2).product{n -> n + n} == Right((2, 4))
 */
fun <A, B, C> Either<A, B>.product(f: (B) -> C): Either<A, Pair<B, C>> = this.fmap{b: B -> Pair(b, f(b))}



// Bifunctor extension functions:

/**
 * Bifunctors are like functors, only they vary in two dimensions instead of one.
 */
fun <A, B, C, D> Either<A, B>.bimap(f: (A) -> C, g: (B) -> D): Either<C, D> = when (this) {
    is Left -> Left(f(this.value))
    is Right -> Right(g(this.value))
}   // bimap

/**
 * Map covariantly over the first argument.
 */
fun <A, B, C> Either<A, B>.first(f: (A) -> C): Either<C, B> =
    bimap(f, {b: B -> b})

/**
 * Map covariantly over the second argument.
 */
fun <A, B, C> Either<A, B>.second(g: (B) -> C): Either<A, C> =
    bimap({a: A -> a}, g)



// Applicative extension functions:

/**
 * Apply the function wrapped in a context to the content of the
 *   value also wrapped in a matching context.
 *
 * Examples:
 *   Left("Ken").ap(Right{n -> n + 1}) = Left("Ken")
 *   Right(2).ap(Right{n -> n + 1}) = Right(3)
 */
fun <A, B, C> Either<A, B>.ap(f: Either<A, (B) -> C>): Either<A, C> =
    when (f) {
        is Left -> Left(f.value)
        is Right -> this.fmap(f.value)
    }   // ap

/**
 * An infix symbol for ap.
 */
infix fun <A, B, C> Either<A, (B) -> C>.apply(v: Either<A, B>): Either<A, C> = v.ap(this)

/**
 * Sequence actions, discarding the value of the receiver.
 *   If the argument is a Right then a constant function is applied
 *   to the receiver. If the argument is a Left then it is returned.
 *
 * Examples:
 *   Left("Ken").sDF(Left("Ken")) = Left("Ken")
 *   Left("Ken").sDF(Right(2)) = Left("Ken")
 *   Right(2).sDF(Left("Ken")) = Left("Ken")
 *   Right(2).sDF(Right(2)) = Right(2)
 */
fun <A, B, C> Either<A, B>.sDF(eac: Either<A, C>): Either<A, C> {
    fun const(c: C): (B) -> C = {_: B -> c}
    return EitherF.liftA2<A, C, B, C>(::const)(eac)(this)
}   // sDF

/**
 * Sequence actions, discarding the value of the second argument.
 *   If the receiver is a Right then a constant function is applied
 *   to the argument. If the receiver is a Left then it is returned.
 *
 * Examples:
 *   Left("Ken").sDS(Left("Ken")) = Left("Ken")
 *   Left("Ken").sDS(Right(2)) = Left("Ken")
 *   Right(2).sDS(Left("Ken")) = Left("Ken")
 *   Right(2).sDS(Right(2)) = Right(2)
 */
fun <A, B, C> Either<A, B>.sDS(eac: Either<A, C>): Either<A, B> {
    fun const(b: B): (C) -> B = {_: C -> b}
    return EitherF.liftA2<A, B, C, B>(::const)(this)(eac)
}   // sDS

/**
 * The product of two applicatives.
 *
 * Examples:
 *   Left("Ken").product2(Left("Ken")) = Left("Ken")
 *   Left("Ken").product2(Right(false)) = Left("Ken")
 *   Right(false).product2(Left("Ken")) = Left("Ken")
 *   Right(false).product2(Right(false)) = Right(Pair(false, false))
 */
fun <A, B, C> Either<A, B>.product2(eac: Either<A, C>): Either<A, Pair<B, C>> =
    eac.ap(this.fmap{b: B -> {c: C -> Pair(b, c)}})

/**
 * The product of three applicatives.
 *
 * Examples:
 *   Left("Ken").product3(Right(2), Right(false)) = Left("Ken")
 *   Right(2).product3(Left("Ken"), Right(false)) = Left("Ken")
 *   Right(false).product3(Left("Ken"), Right(2)) = Left("Ken")
 *   Right(false).product3(Right(false), Right(false)) = Right(Tuple3(false, false, false))
 */
fun <A, B, C, D> Either<A, B>.product3(eac: Either<A, C>, ead: Either<A, D>): Either<A, Triple<B, C, D>> {
    val eabc: Either<A, Pair<B, C>> = this.product2(eac)
    return ead.product2(eabc).fmap{t2 -> Triple(t2.second.first, t2.second.second, t2.first)}
}   // product3

/**
 * fmap2 is a binary version of fmap.
 *
 * Examples:
 *   Left("Ken").fmap2(Left("Ken")){m -> {n -> m + n}} = Left("Ken")
 *   Left("Ken").fmap2(Right(2)){m -> {n -> m + n}} = Left("Ken")
 *   Right(2).fmap2(Left("Ken")){m -> {n -> m + n}} = Left("Ken")
 *   Right(2).fmap2(Right(2)){m -> {n -> m + n}} = Right(4)
 */
fun <A, B, C, D> Either<A, B>.fmap2(eac: Either<A, C>, f: (B) -> (C) -> D): Either<A, D> =
    EitherF.liftA2<A, B, C, D>(f)(this)(eac)

fun <A, B, C, D> Either<A, B>.fmap2(eac: Either<A, C>, f: (B, C) -> D): Either<A, D> =
    this.fmap2(eac, C2(f))

/**
 * fmap3 is a ternary version of fmap.
 *
 * Examples:
 *   Left("Ken").fmap3(Left("Ken"), Left("Ken")){m -> {n -> {o -> m + n + o}}} = Left("Ken")
 *   Left("Ken").fmap3(Right(2), Right(false)){m -> {n -> {o -> m + n + if(o) 1 else 0}}} = Left("Ken")
 *   etc
 *   Right(2).fmap3(Right(2), Right(2)){m -> {n -> {o -> m + n + o}}} = Right(6)
 */
fun <A, B, C, D, E> Either<A, B>.fmap3(eac: Either<A, C>, ead: Either<A, D>, f: (B) -> (C) -> (D) -> E): Either<A, E> =
    EitherF.liftA3<A, B, C, D, E>(f)(this)(eac)(ead)

fun <A, B, C, D, E> Either<A, B>.fmap3(eac: Either<A, C>, ead: Either<A, D>, f: (B, C, D) -> E): Either<A, E> =
    this.fmap3(eac, ead, C3(f))

/**
 * ap2 is a binary version of ap, defined in terms of ap.
 *
 * Examples:
 *   Left("Ken").ap2(Left("Ken"), Right{m -> {n -> m + n}}) = Left("Ken")
 *   Left("Ken").ap2(Right(2), Right{m -> {n -> m + n}}) = Left("Ken")
 *   etc
 *   Right(2).ap2(Right(2), Right{m -> {n -> m + n}}) = Right(4)
 */
fun <A, B, C, D> Either<A, B>.ap2(eac: Either<A, C>, f: Either<A, (B) -> (C) -> D>): Either<A, D> =
    eac.ap(this.ap(f))

/**
 * ap3 is a ternary version of ap, defined in terms of ap.
 *
 * Examples:
 *   Left("Ken").ap3(Left("Ken"), Left("Ken"), Right{m -> {n -> {o -> m + n + o}}}) = Left("Ken")
 *   Left("Ken").ap3(Left("Ken"), Right(2), Right{m -> {n -> {o -> m + n + o}}}) = Left("Ken")
 *   etc
 *   Right(2).ap3(Right(2), Right(2), Right{m -> {n -> {o -> m + n + o}}}) = Right(6)
 */
fun <A, B, C, D, E> Either<A, B>.ap3(eac: Either<A, C>, ead: Either<A, D>, f: Either<A, (B) -> (C) -> (D) -> E>): Either<A, E> =
    ead.ap(eac.ap(this.ap(f)))



// Monad extension functions:

/**
 * Sequentially compose two actions, passing any value produced by the first
 *   as an argument to the second.
 *
 * Examples:
 *   Left("Ken").bind{n -> Right(n + 1)} = Left("Ken")
 *   Right(2).bind{n -> Right(n + 1)} = Right(3)
 */
fun <A, B, C> Either<A, B>.bind(f: (B) -> Either<A, C>): Either<A, C> =
    when (this) {
        is Left -> Left(this.value)
        is Right -> f(this.value)
    }   // bind

/**
 * Sequentially compose two actions, passing any value produced by the first
 *   as an argument to the second.
 *
 * Examples:
 *   Left("Ken").flatMap{n -> Right(n + 1)} = Left("Ken")
 *   Right(2).flatMap{n -> Right(n + 1)} = Right(3)
 */
fun <A, B, C> Either<A, B>.flatMap(f: (B) -> Either<A, C>): Either<A, C> = this.bind(f)

/**
 * Sequentially compose two actions, discarding any value produced by the first,
 *   like sequencing operators (such as the semicolon) in imperative languages.
 *
 * Examples:
 *   Left("Ken").then(Left("Ken")) = Left("Ken")
 *   Left("Ken").then(Right(2)) = Left("Ken")
 *   Right(2).then(Left("Ken")) = Left("Ken")
 *   Right(2).then(Right(2)) = Right(2)
 */
fun <A, B, C> Either<A, B>.then(eac: Either<A, C>): Either<A, C> = this.bind{_ -> eac}



// Traversable extension functions:

fun <A, B, C, X> Either<A, B>.traverseEither(f: (B) -> Either<X, C>): Either<X, Either<A, C>> =
    when (this) {
        is Left -> right(Left(this.value))
        is Right -> f(this.value).fmap{c: C -> right<A, C>(c)}
    }   // traverseEither

fun <A, X, Y> Either<A, Either<X, Y>>.sequenceEither(): Either<X, Either<A, Y>> =
    this.traverseEither{eixy: Either<X, Y> -> eixy}

fun <A, B, C, D> Either<A, B>.traverseFunction(f: (B) -> (C) -> D): (C) -> Either<A, D> {
    fun <X, Y> constant(y: Y): (X) -> Y = {_: X -> y}
    fun <X, Y, Z> ((X) -> Y).fmap(f: (Y) -> Z): (X) -> Z = {x: X -> f(this(x))}
    return when (this) {
        is Left -> constant(Left(this.value))
        is Right -> f(this.value).fmap{d: D -> right<A, D>(d)}
    }
}   // traverseFunction

fun <A, B, C, D> Either<A, (B) -> C>.sequenceFunction(): (B) -> Either<A, C> =
    this.traverseFunction{f: (B) -> C -> f}

fun <A, B, C> Either<A, B>.traverseList(f: (B) -> List<C>): List<Either<A, C>> =
    when (this) {
        is Left -> ListF.singleton(left(this.value))
        is Right -> f(this.value).fmap{c: C -> right<A, C>(c)}
    }   // traverseList

fun <A, B> Either<A, List<B>>.sequenceList(): List<Either<A, B>> =
    this.traverseList{ls: List<B> -> ls}

fun <A, B, C> Either<A, B>.traverseNonEmptyList(f: (B) -> NonEmptyList<C>): NonEmptyList<Either<A, C>> =
    when (this) {
        is Left -> NonEmptyListF.singleton(left(this.value))
        is Right -> f(this.value).fmap{c: C -> right<A, C>(c)}
    }   // traverseNonEmptyList

fun <A, B> Either<A, NonEmptyList<B>>.sequenceNonEmptyList(): NonEmptyList<Either<A, B>> =
    this.traverseNonEmptyList{ls: NonEmptyList<B> -> ls}

fun <A, B, C> Either<A, B>.traverseOption(f: (B) -> Option<C>): Option<Either<A, C>> =
    when (this) {
        is Left -> some(Left(this.value))
        is Right -> f(this.value).fmap{c: C -> right<A, C>(c)}
    }   // traverseOption

fun <A, B> Either<A, Option<B>>.sequenceOption(): Option<Either<A, B>> =
    this.traverseOption{op: Option<B> -> op}

fun <A, B, C> Either<A, B>.traverseStream(f: (B) -> Stream<C>): Stream<Either<A, C>> =
    when (this) {
        is Left -> StreamF.singleton(left(this.value))
        is Right -> f(this.value).fmap{c: C -> right<A, C>(c)}
    }   // traverseStream

fun <A, B> Either<A, Stream<B>>.sequenceStream(): Stream<Either<A, B>> =
    this.traverseStream{str: Stream<B> -> str}

fun <A, B, C, X> Either<A, B>.traverseValidation(f: (B) -> Validation<X, C>): Validation<X, Either<A, C>> =
    when (this) {
        is Left -> success(Left(this.value))
        is Right -> f(this.value).fmap{c: C -> right<A, C>(c)}
    }   // traverseValidation

fun <A, X, Y> Either<A, Validation<X, Y>>.sequenceValidation(): Validation<X, Either<A, Y>> =
    this.traverseValidation{vxy: Validation<X, Y> -> vxy}




// Foldable extension functions:

fun <A, B> Either<A, B>.fold(md: Monoid<B>): B =
    this.foldLeft(md.empty){a: B -> {b: B -> md.combine(a, b)}}

fun <A, B, C> Either<A, B>.foldMap(md: Monoid<C>, f: (B) -> C): C =
    when (this) {
        is Left -> md.empty
        is Right -> f(this.value)
    }   // foldMap

fun <A, B, C> Either<A, B>.foldLeft(e: C, f: (C) -> (B) -> C): C =
    when (this) {
        is Left -> e
        is Right -> f(e)(this.value)
    }   // foldLeft

fun <A, B, C> Either<A, B>.foldLeft(e: C, f: (C, B) -> C): C =
    this.foldLeft(e, C2(f))

fun <A, B, C> Either<A, B>.foldRight(e: C, f: (B) -> (C) -> C): C =
    when (this) {
        is Left -> e
        is Right -> f(this.value)(e)
    }   // foldRight

fun <A, B, C> Either<A, B>.foldRight(e: C, f: (B, C) -> C): C =
    this.foldRight(e, C2(f))
