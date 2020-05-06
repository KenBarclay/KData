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

import com.adt.kotlin.data.immutable.either.Either
import com.adt.kotlin.data.immutable.either.EitherF.left
import com.adt.kotlin.data.immutable.either.EitherF.right
import com.adt.kotlin.data.immutable.either.fmap
import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.ListF
import com.adt.kotlin.data.immutable.list.fmap
import com.adt.kotlin.data.immutable.nel.NonEmptyList
import com.adt.kotlin.data.immutable.nel.NonEmptyListF
import com.adt.kotlin.data.immutable.nel.fmap
import com.adt.kotlin.data.immutable.option.Option.None
import com.adt.kotlin.data.immutable.option.Option.Some
import com.adt.kotlin.data.immutable.option.OptionF.liftA2
import com.adt.kotlin.data.immutable.option.OptionF.liftA3
import com.adt.kotlin.data.immutable.option.OptionF.none
import com.adt.kotlin.data.immutable.option.OptionF.some
import com.adt.kotlin.data.immutable.stream.Stream
import com.adt.kotlin.data.immutable.stream.StreamF
import com.adt.kotlin.data.immutable.stream.fmap
import com.adt.kotlin.data.immutable.validation.Validation
import com.adt.kotlin.data.immutable.validation.ValidationF.success
import com.adt.kotlin.data.immutable.validation.fmap
import com.adt.kotlin.hkfp.fp.*
import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3

import com.adt.kotlin.hkfp.fp.FunctionF.constant
import com.adt.kotlin.hkfp.typeclass.Monoid



/**
 * Functions to support an applicative style of programming.
 *
 * Examples:
 *   {a: A -> ... B value} fmap opA ==> Option<B>
 *   {a: A -> {b: B -> ... C value}} fmap opA ==> Option<(B) -> C>
 *   {a: A -> {b: B -> ... C value}} fmap opA appliedOver opB ==> Option<C>
 */
infix fun <A, B> ((A) -> B).fmap(op: Option<A>): Option<B> =
    op.fmap(this)

infix fun <A, B> Option<(A) -> B>.appliedOver(op: Option<A>): Option<B> =
    op.ap(this)



// Contravariant extension functions:

/**
 * Test if this option is defined and contains the given element.
 *
 * Examples:
 *   none.contains(99) == false
 *   Some(5).contains(99) == false
 *   Some(99).contains(99) == true
 *
 * @param elem              the element to test
 * @return                  true if the option has an element that is equal to elem, false otherwise
 */
operator fun <A> Option<A>.contains(elem: A): Boolean = when(this) {
    is Option.None -> false
    is Option.Some -> (this.value == elem)
}   // contains

/**
 * Return the option's value if the option is nonempty, otherwise
 *   return the defaultValue.
 *
 * Examples:
 *   none.getOrElse(99) == 99
 *   Some(5).getOrElse(99) == 5
 *
 * @param defaultValue  	fall-back result
 * @return              	option's value or default
 */
fun <A> Option<A>.getOrElse(defaultValue: A): A = when(this) {
    is Option.None -> defaultValue
    is Option.Some -> this.value
}   // getOrElse

/**
 * Return this option if the option is nonempty, otherwise return another
 *   option provided lazily by default.
 *
 * Examples:
 *   none.orElse{-> Some(99)} == Some(99)
 *   Some(5).orElse{-> Some(99)} == Some(5)
 */
fun <A> Option<A>.orElse(defaultValue: () -> Option<A>): Option<A> =
    fold(defaultValue, {_: A -> this})



// Functor extension functions:

/**
 * Apply the function to the content(s) of the Option context.
 *
 * Examples:
 *   none.fmap{n -> (n % 2 == 0)} = none
 *   some(5).fmap{n -> (n % 2 == 0)} = some(false)
 *   some(6).fmap{n -> (n % 2 == 0)} = some(true)
 */
fun <A, B> Option<A>.fmap(f: (A) -> B): Option<B> = this.map(f)

/**
 * An infix symbol for fmap.
 */
infix fun <A, B> ((A) -> B).dollar(v: Option<A>): Option<B> = v.fmap(this)

/**
 * Replace all locations in the input with the given value.
 *
 * Examples:
 *   none.replaceAll(true) = none
 *   some(5).replaceAll(true) = some(true)
 *   some(6).replaceAll(true) = some(true)
 */
fun <A, B> Option<A>.replaceAll(b: B): Option<B> = this.fmap{_ -> b}

/**
 * Distribute the Option<(A, B)> over the pair to get (Option<A>, Option<B>).
 *
 * Examples:
 *   none.distribute() = (none, none)
 *   some(("Ken", 25)).distribute() = (some("Ken"), some(25))
 */
fun <A, B> Option<Pair<A, B>>.distribute(): Pair<Option<A>, Option<B>> =
    Pair(this.fmap{pr -> pr.first}, this.fmap{pr -> pr.second})

/**
 * Inject a to the left of the b's in this option.
 */
fun <A, B> Option<B>.injectLeft(a: A): Option<Pair<A, B>> = this.fmap{b: B -> Pair(a, b)}

/**
 * Inject b to the right of the a's in this option.
 */
fun <A, B> Option<A>.injectRight(b: B): Option<Pair<A, B>> = this.fmap{a: A -> Pair(a, b)}

/**
 * Twin all the a's in this option with itself.
 */
fun <A> Option<A>.pair(): Option<Pair<A, A>> = this.fmap{a: A -> Pair(a, a)}

/**
 * Pair all the a's in this option with the result of the function application.
 */
fun <A, B> Option<A>.product(f: (A) -> B): Option<Pair<A, B>> = this.fmap{a: A -> Pair(a, f(a))}



// Applicative extension functions:

/**
 * Apply the function wrapped in the Option context to the content of the
 *   value also wrapped in a Option context.
 *
 * Examples:
 *   none.ap(some{n -> (n % 2 == 0)}) = none
 *   some(5).ap(some{n -> (n % 2 == 0)}) = some(false)
 *   some(6).ap(some{n -> (n % 2 == 0)}) = some(true)
 */
fun <A, B> Option<A>.ap(f: Option<(A) -> B>): Option<B> =
    when (f) {
        is None -> None
        is Some -> {
            when (this) {
                is None -> None
                is Some -> some(f.value(this.value))
            }
        }
    }   // ap

/**
 * An infix symbol for ap.
 */
infix fun <A, B> Option<(A) -> B>.apply(v: Option<A>): Option<B> = v.ap(this)

/**
 * Sequence actions, discarding the value of the first argument.
 *
 * Examples:
 *   none.sDF(none) = none
 *   none.sDF(some("Ken")) = none
 *   some(5).sDF(some("Ken")) = some("Ken")
 *   some(6).sDF(none) = none
 */
fun <A, B> Option<A>.sDF(ob: Option<B>): Option<B> {
    fun constant(b: B): (A) -> B = {_: A -> b}
    return liftA2(::constant)(ob)(this)
}   // sDF

/**
 * Sequence actions, discarding the value of the second argument.
 *
 * Examples:
 *   none.sDS(none) = none
 *   none.sDF(some("Ken")) = none
 *   some(5).sDS(some("Ken")) = some(5)
 *   some(6).sDS(none) = none
 */
fun <A, B> Option<A>.sDS(ob: Option<B>): Option<A> {
    val const: (A) -> (B) -> A = ::constant
    return liftA2(const)(this)(ob)
}   // sDS

/**
 * The product of two applicatives.
 *
 * Examples:
 *   none.product2(none) = none
 *   none.product2(some("Ken")) = none
 *   some(5).product2(none) = none
 *   some(5).product2(some("Ken")) = some(Pair(5, "Ken")
 */
fun <A, B> Option<A>.product2(ob: Option<B>): Option<Pair<A, B>> =
    ob.ap(this.fmap{a: A -> {b: B -> Pair(a, b)}})

/**
 * The product of three applicatives.
 *
 * Examples:
 *   none.product3(none, none) = none
 *   etc
 *   some(5).product3(some("Ken"), some(false)) = some(Triple(5, "Ken", false))
 */
fun <A, B, C> Option<A>.product3(ob: Option<B>, oc: Option<C>): Option<Triple<A, B, C>> {
    val otab: Option<Pair<A, B>> = this.product2(ob)
    return oc.product2(otab).fmap{t2 -> Triple(t2.second.first, t2.second.second, t2.first)}
}   // product3

/**
 * fmap2 is a binary version of fmap.
 *
 * Examples:
 *   none.fmap2(none){m -> {n -> m + n}} = none
 *   none.fmap2(some(8)){m -> {n -> m + n}} = none
 *   some(5).fmap2(none){m -> {n -> m + n}} = none
 *   some(5).fmap2(some(8)){m -> {n -> m + n}} = some(13)
 */
fun <A, B, C> Option<A>.fmap2(ob: Option<B>, f: (A) -> (B) -> C): Option<C> =
    liftA2(f)(this)(ob)

fun <A, B, C> Option<A>.fmap2(ob: Option<B>, f: (A, B) -> C): Option<C> =
    this.fmap2(ob, C2(f))

/**
 * fmap3 is a ternary version of fmap.
 *
 * Examples:
 *   none.fmap3(none, none){m -> {n -> {o -> m + n + o}}} = none
 *   etc
 *   some(5).fmap3(some(8), some(9)){m -> {n -> {o -> m + n + o}}} = some(22)
 */
fun <A, B, C, D> Option<A>.fmap3(ob: Option<B>, oc: Option<C>, f: (A) -> (B) -> (C) -> D): Option<D> =
    liftA3(f)(this)(ob)(oc)

fun <A, B, C, D> Option<A>.fmap3(ob: Option<B>, oc: Option<C>, f: (A, B, C) -> D): Option<D> =
    this.fmap3(ob, oc, C3(f))

/**
 * ap2 is a binary version of ap, defined in terms of ap.
 *
 * Examples:
 *   none.ap2(none, some{s -> {n -> (s.length == n)}}) = none
 *   none.ap2(some(3), some{s -> {n -> (s.length == n)}}) = none
 *   some("Ken").ap2(none, some{s -> {n -> (s.length == n)}}) = none
 *   some("Ken").ap2(some(3), some{s -> {n -> (s.length == n)}}) = some(true)
 *   some("Ken").ap2(some(15), some{s -> {n -> (s.length == n)}}) = some(false)
 */
fun <A, B, C> Option<A>.ap2(ob: Option<B>, f: Option<(A) -> (B) -> C>): Option<C> =
    ob.ap(this.ap(f))

/**
 * ap3 is a ternary version of ap, defined in terms of ap.
 *
 * Examples:
 *   none.ap3(none, none, some{m -> {n -> {o -> m + n + o}}}) = none
 *   none.ap3(some(4), none, some{m -> {n -> {o -> m + n + o}}}) = none
 *   some(3).ap3(none, none, some{m -> {n -> {o -> m + n + o}}}) = none
 *   some(3).ap3(some(4), some(5), some{m -> {n -> {o -> m + n + o}}}) = some(12)
 */
fun <A, B, C, D> Option<A>.ap3(ob: Option<B>, oc: Option<C>, f: Option<(A) -> (B) -> (C) -> D>): Option<D> =
    oc.ap(ob.ap(this.ap(f)))



// Monad extension functions:

/**
 * Sequentially compose two actions, passing any value produced by the first
 *   as an argument to the second.
 *
 * Examples:
 *   none.bind{n -> some(n % 2 == 0)} = none
 *   some(5).bind{n -> some(n % 2 == 0)} = some(false)
 *   some(6).bind{n -> some(n % 2 == 0)} = some(true)
 */
fun <A, B> Option<A>.bind(f: (A) -> Option<B>): Option<B> =
    when (this) {
        is None -> None
        is Some -> f(this.value)
    }   // bind

/**
 * Sequentially compose two options, passing any value produced by the first
 *   as an argument to the second.
 */
fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> = bind(f)

/**
 * Sequentially compose two actions, discarding any value produced by the first,
 *   like sequencing operators (such as the semicolon) in imperative languages.
 *
 * Examples:
 *   none.then(none) == none
 *   none.then(some(5)) == none
 *   some(5).then(none) == none
 *   some(5).then(some(6)) == some(6)
 */
fun <A, B> Option<A>.then(ob: Option<B>): Option<B> = this.bind{_ -> ob}



// Foldable extension functions:

/**
 * Combine the content of this option using the given monoid.
 *   The value of this option is combined with the empty of the monoid.
 *
 * Examples:
 *   none.fold(intAddMonoid) = 0
 *   some(5).fold(intAddMonoid) = 5
 *   some(6).fold(intAddMonoid) = 6
 */
fun <A> Option<A>.fold(md: Monoid<A>): A {
    val self: Option<A> = this
    return md.run {
        self.foldLeft(empty){b -> {a -> combine(b, a)}}
    }   // fold
}

/**
 * Map each element of the structure to a monoid, and combine the results.
 *
 * Examples:
 *   none.foldMap(booleanConjMonoid){n -> (n % 2 == 0)} = true
 *   some(5).foldMap(booleanConjMonoid){n -> (n % 2 == 0)} = false
 *   some(6).foldMap(booleanConjMonoid){n -> (n % 2 == 0)} = true
 */
fun <A, B> Option<A>.foldMap(md: Monoid<B>, f: (A) -> B): B {
    val self: Option<A> = this
    return md.run {
        self.foldLeft(empty){b -> {a -> combine(b, f(a))}}
    }
}   // foldMap

/**
 * foldLeft is a higher-order function that folds a binary function into this
 *   context.
 *
 * Examples:
 *   none.foldLeft(10){m -> {n -> m + n}} = 10
 *   some(5).foldLeft(10){m -> {n -> m + n}} = 15
 *   some(6).foldLeft(10){m -> {n -> m + n}} = 16
 *
 * @param e                 initial value
 * @param f                 curried binary function:: B -> A -> B
 * @return                  folded result
 */

fun <A, B> Option<A>.foldLeft(e: B, f: (B) -> (A) -> B): B =
    when (this) {
        is None -> e
        is Some -> f(e)(this.value)
    }   // foldLeft

fun <A, B> Option<A>.foldLeft(e: B, f: (B, A) -> B): B =
    this.foldLeft(e, C2(f))

/**
 * foldRight is a higher-order function that folds a binary function into this
 *   context.
 *
 * Examples:
 *   none.foldRight(10){m -> {n -> m + n}} = 10
 *   some(5).foldRight(10){m -> {n -> m + n}} = 15
 *   some(6).foldRight(10){m -> {n -> m + n}} = 16
 *
 * @param e                 initial value
 * @param f                 curried binary function:: A -> B -> B
 * @return                  folded result
 */
fun <A, B> Option<A>.foldRight(e: B, f: (A) -> (B) -> B): B =
    when (this) {
        is None -> e
        is Some -> f(this.value)(e)
    }   // foldRight

fun <A, B> Option<A>.foldRight(e: B, f: (A, B) -> B): B =
    this.foldRight(e, C2(f))




// Traversable extension functions:

fun <A, B, C> Option<A>.traverseEither(f: (A) -> Either<B, C>): Either<B, Option<C>> =
    when (this) {
        is None -> right(none())
        is Some -> f(this.value).fmap{c -> some(c)}
    }   // traverseEither

fun <A, B> Option<Either<A, B>>.sequenceEither(): Either<A, Option<B>> =
    this.traverseEither{eiab: Either<A, B> -> eiab}

fun <A, B, C> Option<A>.traverseFunction(f: (A) -> (B) -> C): (B) -> Option<C> {
    fun <X, Y> constant(y: Y): (X) -> Y = {_: X -> y}
    fun <X, Y, Z> ((X) -> Y).fmap(f: (Y) -> Z): (X) -> Z = {x: X -> f(this(x))}
    return when (this) {
        is None -> constant(none())
        is Some -> f(this.value).fmap{c: C -> some(c)}
    }
}   // traverseFunction

fun <A, B> Option<(A) -> B>.sequenceFunction(): (A) -> Option<B> =
    this.traverseFunction{f: (A) -> B -> f}

fun <A, B> Option<A>.traverseList(f: (A) -> List<B>): List<Option<B>> =
    when (this) {
        is None -> ListF.singleton(none())
        is Some -> f(this.value).fmap{b -> some(b)}
    }   // traverseList

fun <A> Option<List<A>>.sequenceList(): List<Option<A>> =
    this.traverseList{ls: List<A> -> ls}

fun <A, B> Option<A>.traverseNonEmptyList(f: (A) -> NonEmptyList<B>): NonEmptyList<Option<B>> =
    when (this) {
        is None -> NonEmptyListF.singleton(none())
        is Some -> f(this.value).fmap{b -> some(b)}
    }   // traverseNonEmptyList

fun <A> Option<NonEmptyList<A>>.sequenceNonEmptyList(): NonEmptyList<Option<A>> =
    this.traverseNonEmptyList{nel: NonEmptyList<A> -> nel}

fun <A, B> Option<A>.traverseOption(f: (A) -> Option<B>): Option<Option<B>> =
    when (this) {
        is None -> some(none())
        is Some -> f(this.value).fmap{b -> some(b)}
    }   // traverseOption

fun <A> Option<Option<A>>.sequenceOption(): Option<Option<A>> =
    this.traverseOption{op: Option<A> -> op}

fun <A, B> Option<A>.traverseStream(f: (A) -> Stream<B>): Stream<Option<B>> =
    when (this) {
        is None -> StreamF.singleton(none())
        is Some -> f(this.value).fmap{b -> some(b)}
    }   // traverseStream

fun <A> Option<Stream<A>>.sequenceStream(): Stream<Option<A>> =
    this.traverseStream{str: Stream<A> -> str}

fun <A, E, B> Option<A>.traverseValidation(f: (A) -> Validation<E, B>): Validation<E, Option<B>> =
    when (this) {
        is None -> success(none())
        is Some -> f(this.value).fmap{b -> some(b)}
    }   // traverseValidation

fun <E, A> Option<Validation<E, A>>.sequenceValidation(): Validation<E, Option<A>> =
    this.traverseValidation{vea: Validation<E, A> -> vea}



/********** TODO
/**
 * Map each element of a structure to an action, evaluate these actions from left to right,
 *   and collect the results.
 *
 * Examples:
 *   none.traverse(optionApplicative()){n -> (n % 2 == 0)} = some(none)
 *   some(5).traverse(optionApplicative()){n -> (n % 2 == 0)} = some(false)
 *   some(6).traverse(optionApplicative()){n -> (n % 2 == 0)} = some(true)
 *
 *   none.traverse(listApplicative()){n -> [n % 2 == 0]} = [none]
 *   some(5).traverse(listApplicative()){n -> [n % 2 == 0]} = [some(false)]
 *   some(6).traverse(listApplicative()){n -> [n % 2 == 0]} = [some(true)]
 */
fun <G, A, B> Option<A>.traverse(ag: Applicative<G>, f: (A) -> Kind1<G, B>): Kind1<G, Option<B>> {
    val optionTraversable: OptionTraversable = optionTraversable()
    return optionTraversable.traverse(this, ag, f)
}   // traverse

/**
 * Evaluate each action in the structure from left to right, and and collect the results.
 *
 * Examples:
 *   none.sequenceA(optionApplicative()) = some(none)
 *   some(none).sequenceA(optionApplicative()) = none
 *   some(some(5)).sequenceA(optionApplicative()) = some(some(5))
 *
 *   some([5]).sequenceA(listApplicative()) = [some(5)]
 *   some([5, 6]).sequenceA(listApplicative()) = [some(5), some(6)]
 */
fun <G, A> Option<Kind1<G, A>>.sequenceA(ag: Applicative<G>): Kind1<G, Option<A>> {
    val optionTraversable: OptionTraversable = optionTraversable()
    return optionTraversable.sequenceA(this, ag)
}   // sequenceA

/**
 * Map each element of a structure to a monadic action, evaluate these actions from left to right,
 *   and collect the results.
 *
 * Examples:
 *   none.mapM(optionMonad()){n -> some(n % 2 == 0)} = some(none)
 *   some(5).mapM(optionMonad()){n -> some(n % 2 == 0)} = some(some(false))
 *   some(6).mapM(optionMonad()){n -> some(n % 2 == 0)} = some(some(true))
 *
 *   none.mapM(listMonad()){n -> [n % 2 == 0]} = [none]
 *   some(5).mapM(listMonad()){n -> [n % 2 == 0]} = [some(false)]
 *   some(6).mapM(listMonad()){n -> [n % 2 == 0]} = [some(true)]
 */
fun <M, A, B> Option<A>.mapM(md: Monad<M>, f: (A) -> Kind1<M, B>): Kind1<M, Option<B>> {
    val optionTraversable: OptionTraversable = optionTraversable()
    return optionTraversable.mapM(this, md, f)
}   // mapM

/**
 * Evaluate each monadic action in the structure from left to right, and collect the results.
 *
 * Examples:
 *   none.sequence(optionMonad()) = some(none)
 *   some(none).sequence(optionMonad()) = none
 *   some(some(5)).sequence(optionMonad()) = some(some(5))
 *
 *   some([5]]).sequence(listMonad()) = [some(5)]
 */
fun <M, A> Option<Kind1<M, A>>.sequence(md: Monad<M>): Kind1<M, Option<A>> {
    val optionTraversable: OptionTraversable = optionTraversable()
    return optionTraversable.sequence(this, md)
}   // sequence



// MonadPlus extension functions:

fun <A> Option<A>.mplus(op: Option<A>): Option<A> {
    val optionMonadPlus: OptionMonadPlus = optionMonadPlus()
    return optionMonadPlus.mplus(this, op)
}   // mplus
**********/
