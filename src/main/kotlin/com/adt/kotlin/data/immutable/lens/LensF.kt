package com.adt.kotlin.data.immutable.lens

import com.adt.kotlin.hkfp.fp.FunctionF.C2



object LensF {

    /**
     * Factory constructor functions for PLens.
     */
    fun <S, T, A, B> plens(fget: (S) -> A, fset: (S) -> (B) -> T): PLens<S, T, A, B> = object: PLens<S, T, A, B>() {
        override fun get(s: S): A = fget(s)
        override fun set(s: S, b: B): T = fset(s)(b)
        override fun modify(s: S, f: (A) -> B): T = fset(s)(f(fget(s)))
    }   // plens

    fun <S, T, A, B> plens(fget: (S) -> A, fset: (S, B) -> T): PLens<S, T, A, B> = plens(fget, C2(fset))

    /**
     * Factory constructor functions for Lens.
     */
    fun <S, A> lens(fget: (S) -> A, fset: (S) -> (A) -> S): Lens<S, A> = object: Lens<S, A>() {
        override fun get(s: S): A = fget(s)
        override fun set(s: S, @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE") a: A): S = fset(s)(a)
        override fun modify(s: S, f: (A) -> A): S = fset(s)(f(fget(s)))
    }   // lens

    fun <S, A> lens(fget: (S) -> A, fset: (S, A) -> S): Lens<S, A> = lens(fget, C2(fset))

}   // LensF
