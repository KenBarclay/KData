package com.adt.kotlin.data.immutable.lens

/**
 * Lenses provide getters and setters for immutable data.
 *
 * @author	                    Ken Barclay
 * @since                       November 2019
 */

import com.adt.kotlin.data.immutable.either.Either
import com.adt.kotlin.data.immutable.either.bimap
import com.adt.kotlin.data.immutable.either.bind
import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.bind



typealias Prism<S, A> = PPrism<S, S, A, A>

/**
 * A PPrism can be seen as a pair of functions:
 *   getOrModify: (S) -> Either<T, A>
 *   reverseGet: (B) -> T
 *
 * @param S                     the source of a PPrism
 * @param T                     the modified source of a PPrism
 * @param A                     the target of a PPrism
 * @param B                     the modified target of a PPrism
 */
abstract class PPrism<S, T, A, B> {

    /**
     * Get the target of a PPrism or return the original value while allowing
     *   the type to change if it does not match.
     */
    abstract fun getOrModify(s: S): Either<T, A>

    /**
     * Get the modified source of a PPrism.
     */
    abstract fun reverseGet(b: B): T

    /**
     * Get the target of a PPrism or nothing if there is no target.
     */
    abstract fun getOption(s: S): Option<A>

    /**
     * Modify polymorphically the target of a PPrism with a function.
     */
    fun modify(s: S, f: (A) -> B): T = getOrModify(s).fold({id -> id}, {a: A -> reverseGet(f(a))})

    /**
     * Modify polymorphically the target of a PPrism with a function.
     *   Return empty if the PPrism is not matching.
     */
    fun modifyOption(s: S, f: (A) -> B): Option<T> =
        getOption(s).map{a: A -> reverseGet(f(a))}

    /**
     * Set polymorphically the target of a PPrism with a value.
     */
    fun set(s: S, b: B): T = modify(s){_ -> b}

    /**
     * Set polymorphically the target of a PPrism with a value.
     *   Return empty if the PPrism is not matching.
     */
    fun setOption(s: S, b: B): Option<T> =
        modifyOption(s){_ -> b}

    /**
     * Check if there is no target.
     */
    fun isEmpty(s: S): Boolean = getOption(s).isEmpty()

    /**
     * Compose this PPrism with the parameter.
     */
    fun <C, D> compose(prism: PPrism<A, B, C, D>): PPrism<S, T, C, D> {
        val self: PPrism<S, T, A, B> = this
        return object: PPrism<S, T, C, D>() {
            override fun getOrModify(s: S): Either<T, C> =
                self.getOrModify(s).bind{a: A -> prism.getOrModify(a).bimap({b: B -> self.set(s, b)}, {id -> id})}
            override fun reverseGet(@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE") d: D): T = self.reverseGet(prism.reverseGet(d))
            override fun getOption(s: S): Option<C> = self.getOption(s).bind{a: A -> prism.getOption(a)}
        }
    }   // compose

    infix fun <C, D> o(prism: PPrism<A, B, C, D>): PPrism<S, T, C, D> = this.compose(prism)

}   // PPrism
