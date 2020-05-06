package com.adt.kotlin.data.immutable.rosetree

/**
 * A rose tree is a tree structure with a variable and unbounded number of
 *   branches per node. Not every tree is some kind of search tree. Data
 *   structures are often designed to correspond to or capture aspects
 *   of a domain model. A rose tree can be used as a generic representation
 *   for an abstract syntax tree.
 *
 * @author	                    Ken Barclay
 * @since                       October 2018
 */

import com.adt.kotlin.data.immutable.rosetree.RoseTreeF.liftA2
import com.adt.kotlin.data.immutable.rosetree.RoseTreeF.liftA3

import com.adt.kotlin.hkfp.typeclass.Monoid

import com.adt.kotlin.data.immutable.list.*
import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3

import com.adt.kotlin.hkfp.fp.FunctionF.constant



// Functor extension functions:

fun <A, B> RoseTree<A>.fmap(f: (A) -> B): RoseTree<B> = this.map(f)

/**
 * An infix symbol for fmap.
 */
infix fun <A, B> ((A) -> B).dollar(v: RoseTree<A>): RoseTree<B> = v.fmap(this)

fun <A, B> RoseTree<A>.replaceAll(b: B): RoseTree<B> = this.fmap{_ -> b}

fun <A, B> RoseTree<Pair<A, B>>.distribute(): Pair<RoseTree<A>, RoseTree<B>> =
    Pair(this.fmap{pr -> pr.first}, this.fmap{pr -> pr.second})

/**
 * Inject a to the left of the b's in this tree.
 */
fun <A, B> RoseTree<B>.injectLeft(a: A): RoseTree<Pair<A, B>> = this.fmap{b: B -> Pair(a, b)}

/**
 * Inject b to the right of the a's in this tree.
 */
fun <A, B> RoseTree<A>.injectRight(b: B): RoseTree<Pair<A, B>> = this.fmap{a: A -> Pair(a, b)}

/**
 * Twin all the a's in this tree with itself.
 */
fun <A> RoseTree<A>.pair(): RoseTree<Pair<A, A>> = this.fmap{a: A -> Pair(a, a)}

/**
 * Pair all the a's in this tree with the result of the function application.
 */
fun <A, B> RoseTree<A>.product(f: (A) -> B): RoseTree<Pair<A, B>> = this.fmap{a: A -> Pair(a, f(a))}



// Applicative extension functions:

/**
 * Apply the function wrapped in a context to the content of the
 *   value also wrapped in a matching context.
 */
fun <A, B> RoseTree<A>.ap(f: RoseTree<(A) -> B>): RoseTree<B> {
    val thisLabel: A = this.label
    val thisSubForest: Forest<A> = this.subForest
    val g: (A) -> B = f.label
    val gs: Forest<(A) -> B> = f.subForest

    val apC: (RoseTree<A>) -> (RoseTree<(A) -> B>) -> RoseTree<B> = {rt -> {rtf -> rt.ap(rtf)}}
    val xsg: Forest<B> = thisSubForest.map(RoseTreeF.lift(g))
    val gsap: Forest<B> = gs.map(apC(this))
    return RoseTree(g(thisLabel), xsg.append(gsap))
}   // ap

/**
 * An infix symbol for ap.
 */
infix fun <A, B> RoseTree<(A) -> B>.apply(v: RoseTree<A>): RoseTree<B> = v.ap(this)

/**
 * Sequence actions, discarding the value of the first argument.
 */
fun <A, B> RoseTree<A>.sDF(rtb: RoseTree<B>): RoseTree<B> {
    fun constant(b: B): (A) -> B = {_: A -> b}
    return RoseTreeF.liftA2<B, A, B>(::constant)(rtb)(this)
}   // sDF

/**
 * Sequence actions, discarding the value of the second argument.
 */
fun <A, B> RoseTree<A>.sDS(rtb: RoseTree<B>): RoseTree<A> {
    fun constant(a: A): (B) -> A = {_: B -> a}
    return RoseTreeF.liftA2<A, B, A>(::constant)(this)(rtb)
}

/**
 * The product of two applicatives.
 */
fun <A, B> RoseTree<A>.product2(rtb: RoseTree<B>): RoseTree<Pair<A, B>> =
    rtb.ap(this.fmap{a: A -> {b: B -> Pair(a, b)}})

/**
 * The product of three applicatives.
 */
fun <A, B, C> RoseTree<A>.product3(rtb: RoseTree<B>, rtc: RoseTree<C>): RoseTree<Triple<A, B, C>> {
    val rtab: RoseTree<Pair<A, B>> = this.product2(rtb)
    return rtc.product2(rtab).fmap{t2 -> Triple(t2.second.first, t2.second.second, t2.first)}
}   // product3

/**
 * fmap2 is a binary version of fmap.
 */
fun <A, B, C> RoseTree<A>.fmap2(rtb: RoseTree<B>, f: (A) -> (B) -> C): RoseTree<C> =
    liftA2(f)(this)(rtb)

fun <A, B, C> RoseTree<A>.fmap2(rtb: RoseTree<B>, f: (A, B) -> C): RoseTree<C> =
    this.fmap2(rtb, C2(f))

/**
 * fmap3 is a ternary version of fmap.
 */
fun <A, B, C, D> RoseTree<A>.fmap3(rtb: RoseTree<B>, rtc: RoseTree<C>, f: (A) -> (B) -> (C) -> D): RoseTree<D> =
    liftA3(f)(this)(rtb)(rtc)

fun <A, B, C, D> RoseTree<A>.fmap3(rtb: RoseTree<B>, rtc: RoseTree<C>, f: (A, B, C) -> D): RoseTree<D> =
    this.fmap3(rtb, rtc, C3(f))

/**
 * ap2 is a binary version of ap, defined in terms of ap.
 */
fun <A, B, C> RoseTree<A>.ap2(rtb: RoseTree<B>, f: RoseTree<(A) -> (B) -> C>): RoseTree<C> =
    rtb.ap(this.ap(f))

/**
 * ap3 is a ternary version of ap, defined in terms of ap.
 */
fun <A, B, C, D> RoseTree<A>.ap3(rtb: RoseTree<B>, rtc: RoseTree<C>, f: RoseTree<(A) -> (B) -> (C) -> D>): RoseTree<D> =
    rtc.ap(rtb.ap(this.ap(f)))



// Monad extension functions:

fun <A, B> RoseTree<A>.bind(f: (A) -> RoseTree<B>): RoseTree<B> {
    val frt: RoseTree<B> = f(this.label)

    val bindC: ((A) -> RoseTree<B>) -> (RoseTree<A>) -> RoseTree<B> = {g -> {rt -> rt.bind(g)}}
    return RoseTree(frt.label, frt.subForest.append(this.subForest.map(bindC(f))))
}   // bind

fun <A, B> RoseTree<A>.flatMap(f: (A) -> RoseTree<B>): RoseTree<B> =
    this.bind(f)

fun <A, B> RoseTree<A>.then(rtb: RoseTree<B>): RoseTree<B> =
    this.bind{_ -> rtb}



// Foldable extension functions:

fun <A> RoseTree<A>.fold(md: Monoid<A>): A =
    this.foldLeft(md.empty){b -> {a -> md.combine(b, a)}}

fun <A, B> RoseTree<A>.foldMap(md: Monoid<B>, f: (A) -> B): B =
    this.foldLeft(md.empty){b -> {a -> md.combine(b, f(a))}}
