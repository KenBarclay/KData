package com.adt.kotlin.data.immutable.lens

/**
 * Lenses provide getters and setters for immutable data.
 *
 * @author	                    Ken Barclay
 * @since                       November 2019
 */



typealias Lens<S, A> = PLens<S, S, A, A>

/**
 * A PLens can be seen as a pair of functions:
 *   get: (S) -> A meaning we can focus into an S and extract an A
 *   set: (B) -> (S) -> T meaning we can focus into an S and set a
 *     value B for a target A and obtain a modified source T
 *
 * @param S                     the source of a PLens
 * @param T                     the modified source of a PLens
 * @param A                     the focus of a PLens
 * @param B                     the modified focus of a PLens
 */
abstract class PLens<S, T, A, B> {

    abstract fun get(s: S): A
    abstract fun set(s: S, b: B): T

    /**
     * Modify the target of a PLens using the given function.
     */
    abstract fun modify(s: S, f: (A) -> B): T

    /**
     * Compose this PLens with the parameter.
     */
    fun <C, D> compose(plens: PLens<A, B, C, D>): PLens<S, T, C, D> {
        val self: PLens<S, T, A, B> = this
        return object: PLens<S, T, C, D>() {
            override fun get(s: S): C = plens.get(self.get(s))
            override fun set(s: S, @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE") d: D): T = self.modify(s){a: A-> plens.set(a, d)}
            override fun modify(s: S, f: (C) -> D): T = self.modify(s){a: A -> plens.modify(a, f)}
        }
    }   // compose

    infix fun <C, D> o(plens: PLens<A, B, C, D>): PLens<S, T, C, D> = this.compose(plens)

}   // PLens
