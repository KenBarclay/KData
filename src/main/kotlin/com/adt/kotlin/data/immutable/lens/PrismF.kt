package com.adt.kotlin.data.immutable.lens

import com.adt.kotlin.data.immutable.either.Either
import com.adt.kotlin.data.immutable.option.Option

object PrismF {

    /**
     * Factory constructor function for PPrism.
     */
    fun <S, T, A, B> pprism(fgetOrModify: (S) -> Either<T, A>, freverseGet: (B) -> T): PPrism<S, T, A, B> = object: PPrism<S, T, A, B>() {
        override fun getOrModify(s: S): Either<T, A> = fgetOrModify(s)
        override fun reverseGet(b: B): T = freverseGet(b)
        override fun getOption(s: S): Option<A> = fgetOrModify(s).toOption()
    }   // pprism

    /**
     * Factory constructor function for Prism.
     */
    fun <S, A> prism(fgetOrModify: (S) -> Either<S, A>, freverseGet: (A) -> S): Prism<S, A> = object: Prism<S, A>() {
        override fun getOrModify(s: S): Either<S, A> = fgetOrModify(s)
        override fun reverseGet(@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE") a: A): S = freverseGet(a)
        override fun getOption(s: S): Option<A> = fgetOrModify(s).toOption()
    }   // prism

}   // PrismF
