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

import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.ListF



sealed class Option<out A> {



    object None : Option<Nothing>() {

        /**
         * Return true if the option is None, false otherwise.
         *
         * Examples:
         *   none.isEmpty() = true
         *   some(5).isEmpty() = false
         *
         * @return    		        true, if the option is None, otherwise false
         */
        override fun isEmpty(): Boolean = true

        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param other             the other object
         * @return                  true if "equal", false otherwise
         */
        override fun equals(other: Any?): Boolean {
            return if (this === other)
                true
            else !(other == null || this::class.java != other::class.java)
        }   // equals

        /**
         * Return a string representation of the object.
         *
         * @return                  string representation
         */
        override fun toString(): String = "None"

    }   // None



    class Some<out A> internal constructor(val value: A) : Option<A>() {

        /**
         * Return true if the option is None, false otherwise.
         *
         * Examples:
         *   none.isEmpty() = true
         *   some(5).isEmpty() = false
         *
         * @return    		        true, if the option is None, otherwise false
         */
        override fun isEmpty(): Boolean = false

        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param other             the other object
         * @return                  true if "equal", false otherwise
         */
        override fun equals(other: Any?): Boolean {
            return if (this === other)
                true
            else if (other == null || this::class.java != other::class.java)
                false
            else {
                @Suppress("UNCHECKED_CAST") val otherSome: Some<A> = other as Some<A>
                (this.get() == otherSome.get())
            }
        }   // equals

        /**
         * Return a string representation of the object.
         *
         * @return                  string representation
         */
        override fun toString(): String = "Some(${value})"

    }   // Some



    /**
     * Return true if the option is None, false otherwise.
     *
     * Examples:
     *   none.isEmpty() = true
     *   some(5).isEmpty() = false
     *
     * @return    		        true, if the option is None, otherwise false
     */
    abstract fun isEmpty(): Boolean

    /**
     * Test if this option is defined and contains the given element.
     *
     * Examples:
     *   none.contains{n -> (n % 2 == 0)}       false
     *   some(5).contains{n -> (n % 2 == 0)}    false
     *   some(6).contains{n -> (n % 2 == 0)}    true
     */
    fun contains(predicate: (A) -> Boolean): Boolean {
        return when (this) {
            is None -> false
            is Some -> predicate(this.value)
        }
    }   // contains



    /**
     * Return true if this option is nonempty  and the predicate returns true
     *   when applied to its value. Otherwise, returns false.
     *
     * Examples:
     *   none.exists{n: Int -> (n % 2 == 0)} == false
     *   some(4).exists{n: Int -> (n % 2 == 0)} == true
     *   some(5).exists{n: Int -> (n % 2 == 0)} == false
     *
     * @param  predicate        the predicate to test
     * @return                  true if this option exists and the predicate returns true
     */
    fun exists(predicate: (A) -> Boolean): Boolean =
        (!this.isEmpty() && predicate(this.get()))

    /**
     * Return this option if it is nonempty and applying the predicate to
     *   this option's value returns true. Otherwise, return none.
     *
     * Examples:
     *   none.filter{n: Int -> (n % 2 == 0)} == none
     *   some(4).filter{n: Int -> (n % 2 == 0)} == some(4)
     *   some(5).filter{n: Int -> (n % 2 == 0)} == none
     *
     * @param  predicate        the predicate used for testing
     * @return                  this option if it is not empty and the predicate is true
     */
    fun filter(predicate: (A) -> Boolean): Option<A> =
        if (!this.isEmpty() && predicate(this.get())) this else None

    /**
     * Return this option if it is nonempty and applying the predicate to
     *   this option's value returns false. Otherwise, return none.
     *
     * Examples:
     *   none.filterNot{n: Int -> (n % 2 == 0)} == none
     *   some(4).filterNot{n: Int -> (n % 2 == 0)} == none
     *   some(5).filterNot{n: Int -> (n % 2 == 0)} == some(5)
     *
     * @param  predicate        the predicate used for testing
     * @return                  this option if it is not empty and the predicate is true
     */
    fun filterNot(predicate: (A) -> Boolean): Option<A> =
        filter{a: A -> !predicate(a)}

    /**
     * Return the result of applying the given function to this option's value
     *   if the option is nonempty.  Otherwise, return the default value.
     * This is equivalent to option.map(f).getOrElse(defaultValue)
     *
     * Examples:
     *   none.fold(false){n: Int -> (n % 2 == 0)} == false
     *   some(4).fold(false){n: Int -> (n % 2 == 0)} == true
     *   some(5).fold(false){n: Int -> (n % 2 == 0)} == false
     *
     * @param defaultValue      the expression to evaluate if empty
     * @param f                 the function to apply if nonempty
     * @return                  function result or the default
     */
    fun <B> fold(none: () -> B, some: (A) -> B): B {
        return when (this) {
            is None -> none()
            is Some -> some(this.value)
        }
    }   // fold

    /**
     * Return true if this option is empty or the predicate returns true when
     *   applied to this option's nonempty value.
     *
     * Examples:
     *   none.forAll{n: Int -> (n % 2 == 0)} == true
     *   some(4).forAll{n: Int -> (n % 2 == 0)} == true
     *   some(5).forAll{n: Int -> (n % 2 == 0)} == false
     *
     * @param predicate         the predicate to test
     * @return                  true if this option is empty or the predicate returns true when
     *                              applied to this option's nonempty value
     */
    fun forAll(predicate: (A) -> Boolean): Boolean =
        (this.isEmpty() || predicate(this.get()))

    /**
     * Return the option's value.
     *   Throws OptionException if the option is empty.
     *
     * Examples:
     *   none.get() throws OptionException
     *   some(5).get() == 5
     *
     * @throws                  OptionException if this is a None
     * @return    		        option's value
     */
    fun get(): A {
        return when (this) {
            is None -> throw OptionException("None: get")
            is Some -> this.value
        }
    }   // get

    /**
     * Return true if the option is an instance of Some, false otherwise.
     *
     * Examples:
     *   none.isDefined() == false
     *   some(5).isDefined() == true
     *
     * @return    		        true, if the option is Some, false otherwise
     */
    fun isDefined(): Boolean = !isEmpty()

    /**
     * Return true if the option is an instance of Some, false otherwise.
     *   This is a synonym for isDefined.
     *
     * Examples:
     *   none.isNonEmpty() == false
     *   some(5).isNonEmpty() == true
     *
     * @return    		        true, if the option is Some, false otherwise
     */
    fun isNonEmpty(): Boolean = isDefined()

    /**
     * If this option is defined then apply the given function to the value
     *   of this option and wrap in a Some. Otherwise return none.
     *
     * Examples:
     *   none.map{n: Int -> (n % 2 == 0)} == none
     *   some(4).map{n: Int -> (n % 2 == 0)} == some(true)
     *   some(5).map{n: Int -> (n % 2 == 0)} == some(false)
     *
     * @param f   		        function:: A -> B
     * @return      		    wrapped result of function application
     */
    fun <B> map(f: (A) -> B): Option<B> {
        return when (this) {
            is None -> None
            is Some -> Some(f(this.value))
        }
    }   // map

    /**
     * The option function takes a default value and a function. If the value is None,
     *   the function returns the default value. Otherwise, it applies the function to
     *   the value inside the Some and returns the result.
     */
    fun <B> option(defaultValue: B, f: (A) -> B): B {
        return when (this) {
            is None -> defaultValue
            is Some -> f(this.value)
        }
    }   // option

    /**
     * Convert an option value into a boolean value. If this value is None then
     *   false. If this value is Some then return true.
     */
    fun toBoolean(): Boolean =
        when (this) {
            is None -> false
            is Some -> true
        }

    /**
     * Convert an option value into a list. If this value is None then
     *   the empty list. If this value is Some then a singleton list.
     */
    fun toList(): List<A> =
        when (this) {
            is None -> ListF.empty()
            is Some -> ListF.singleton(this.value)
        }

}   // Option
