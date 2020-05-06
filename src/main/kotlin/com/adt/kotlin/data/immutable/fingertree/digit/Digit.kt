package com.adt.kotlin.data.immutable.fingertree.digit

/**
 * A digit is a vector of 1-4 elements. Serves as a pointer to the
 *   prefix or suffix of a finger tree.
 *
 * @param V                     the monoidal type with which to annotate nodes
 * @param A                     the type of the tree's elements
 *
 * @author	                    Ken Barclay
 * @since                       September 2019
 */

import com.adt.kotlin.data.immutable.fingertree.MakeTree
import com.adt.kotlin.data.immutable.fingertree.Measured



sealed class Digit<V, A>(val measured: Measured<V, A>) {

    open fun isOne(): Boolean = false
    open fun isTwo(): Boolean = false
    open fun isThree(): Boolean = false
    open fun isFour(): Boolean = false

    /**
     * The leading element of the pack.
     *
     * @return                  the leading element
     */
    fun headDigit(): A = when (this) {
        is One -> this.a
        is Two -> this.a1
        is Three -> this.a1
        is Four -> this.a1
    }

    /**
     * The trailing element of the pack.
     *
     * @return                  the tail element
     */
    fun tailDigit(): Digit<V, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return when (this) {
            is One -> this
            is Two -> mk.one(this.a2)
            is Three -> mk.two(this.a2, this.a3)
            is Four -> mk.three(this.a2, this.a3, this.a4)
        }
    }   // tailDigit

    /**
     * Fold this digit to the left using the given function and the given initial value.
     *
     * @param e                 an initial value to apply at the leftmost end of the fold
     * @param f                 a function with which to fold this digit
     * @return                  the left reduction of this digit with the given function and the given initial value
     */
    abstract fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B

    /**
     * Fold this digit to the right using the given function and the given initial value.
     *
     * @param e                 an initial value to apply at the rightmost end of the fold
     * @param f                 a function with which to fold this digit
     * @return                  the right reduction of this digit with the given function and the given initial value
     */
    abstract fun <B> foldRight(e: B, f: (A) -> (B) -> B): B

    /**
     * Return the sum of the measurements of this digit according to the monoid.
     *
     * @return                  the sum of the measurements of this digit according to the monoid
     */
    fun measure(): V = this.foldLeft(measured.empty()){v: V -> {a: A -> measured.sum(v, measured.measure(a))}}

    /**
     * Fold this digit to the left using the given function.
     *
     * @param f                 a function with which to fold this digit
     * @return                  the left reduction of this digit with the given function
     */
    fun reduceLeft(f: (A) -> (A) -> A): A {
        return when(this) {
            is One -> this.a
            is Two -> f(this.a1)(this.a2)
            is Three -> f(f(this.a1)(this.a2))(this.a3)
            is Four -> f(f(f(this.a1)(this.a2))(this.a3))(this.a4)
        }
    }   // reduceLeft

    /**
     * Fold this digit to the right using the given function.
     *
     * @param f                 a function with which to fold this digit
     * @return                  the right reduction of this digit with the given function
     */
    fun reduceRight(f: (A) -> (A) -> A): A {
        return when (this) {
            is One -> this.a
            is Two -> f(this.a1)(this.a2)
            is Three -> f(this.a1)(f(this.a2)(this.a3))
            is Four -> f(this.a1)(f(this.a2)(f(this.a3)(this.a4)))
        }
    }   // reduceRight

    /**
     * Maps a function across the elements of this digit, measuring with the given measurement.
     *
     * @param f                 a function to map across the elements of this digit
     * @param measured          a measuring for the function's domain (destination type)
     * @return                  a new digit with the same structure as this digit, but with all elements transformed
     *                              with the given function and measured with the given measuring
     */
    fun <B> map(f: (A) -> B, measured: Measured<V, B>): Digit<V, B> {
        val mk: MakeTree<V, B> = MakeTree(measured)
        return when (this) {
            is One -> mk.one(f(this.a))
            is Two -> mk.two(f(this.a1), f(this.a2))
            is Three -> mk.three(f(this.a1), f(this.a2), f(this.a3))
            is Four -> mk.four(f(this.a1), f(this.a2), f(this.a3), f(this.a4))
        }
    }   // map

}   // Digit



class One<V, A> internal constructor(measured: Measured<V, A>, val a: A) : Digit<V, A>(measured) {

    override fun toString(): String = "One(${measured}, ${this.measure()}, ${a})"

    override fun isOne(): Boolean = true

    /**
     * Folds this digit to the left using the given function and the given initial value.
     *
     * @param e                 an initial value to apply at the leftmost end of the fold
     * @param f                 a function with which to fold this digit
     * @return                  the left reduction of this digit with the given function and the given initial value
     */
    override fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B = f(e)(a)

    /**
     * Folds this digit to the right using the given function and the given initial value.
     *
     * @param e                 an initial value to apply at the rightmost end of the fold
     * @param f                 a function with which to fold this digit
     * @return                  the right reduction of this digit with the given function and the given initial value
     */
    override fun <B> foldRight(e: B, f: (A) -> (B) -> B): B = f(a)(e)

}   // One



class Two<V, A> internal constructor(measured: Measured<V, A>, val a1: A, val a2: A) : Digit<V, A>(measured) {

    override fun toString(): String = "Two(${measured}, ${this.measure()}, ${a1}, ${a2})"

    override fun isTwo(): Boolean = true

    /**
     * Folds this digit to the left using the given function and the given initial value.
     *
     * @param e                 an initial value to apply at the leftmost end of the fold
     * @param f                 a function with which to fold this digit
     * @return                  the left reduction of this digit with the given function and the given initial value
     */
    override fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B {
        fun g(b: B, a: A): B = f(b)(a)
        return g(g(e, a1), a2)
    }

    /**
     * Folds this digit to the right using the given function and the given initial value.
     *
     * @param e                 an initial value to apply at the rightmost end of the fold
     * @param f                 a function with which to fold this digit
     * @return                  the right reduction of this digit with the given function and the given initial value
     */
    override fun <B> foldRight(e: B, f: (A) -> (B) -> B): B {
        fun g(a: A, b: B): B = f(a)(b)
        return g(a1, g(a2, e))
    }

}   // Two



class Three<V, A> internal constructor(measured: Measured<V, A>, val a1: A, val a2: A, val a3: A) : Digit<V, A>(measured) {

    override fun toString(): String = "Three(${measured}, ${this.measure()}, ${a1}, ${a2}, ${a3})"

    override fun isThree(): Boolean = true

    /**
     * Folds this digit to the left using the given function and the given initial value.
     *
     * @param e                 an initial value to apply at the leftmost end of the fold
     * @param f                 a function with which to fold this digit
     * @return                  the left reduction of this digit with the given function and the given initial value
     */
    override fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B {
        fun g(b: B, a: A): B = f(b)(a)
        return g(g(g(e, a1), a2), a3)
    }

    /**
     * Folds this digit to the right using the given function and the given initial value.
     *
     * @param e                 an initial value to apply at the rightmost end of the fold
     * @param f                 a function with which to fold this digit
     * @return                  the right reduction of this digit with the given function and the given initial value
     */
    override fun <B> foldRight(e: B, f: (A) -> (B) -> B): B {
        fun g(a: A, b: B): B = f(a)(b)
        return g(a1, g(a2, g(a3, e)))
    }

}   // Three



class Four<V, A> internal constructor(measured: Measured<V, A>, val a1: A, val a2: A, val a3: A, val a4: A) : Digit<V, A>(measured) {

    override fun toString(): String = "Four(${measured}, ${this.measure()}, ${a1}, ${a2}, ${a3}, ${a4})"

    override fun isFour(): Boolean = true

    /**
     * Folds this digit to the left using the given function and the given initial value.
     *
     * @param e                 an initial value to apply at the leftmost end of the fold
     * @param f                 a function with which to fold this digit
     * @return                  the left reduction of this digit with the given function and the given initial value
     */
    override fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B {
        fun g(b: B, a: A): B = f(b)(a)
        return g(g(g(g(e, a1), a2), a3), a4)
    }

    /**
     * Folds this digit to the right using the given function and the given initial value.
     *
     * @param e                 an initial value to apply at the rightmost end of the fold
     * @param f                 a function with which to fold this digit
     * @return                  the right reduction of this digit with the given function and the given initial value
     */
    override fun <B> foldRight(e: B, f: (A) -> (B) -> B): B {
        fun g(a: A, b: B): B = f(a)(b)
        return g(a1, g(a2, g(a3, g(a4, e))))
    }

}   // Four
