package com.adt.kotlin.data.immutable.validation

/**
 * The Validation type represents a computation that may return a successfully computed value
 *   or result in a failure. Instances of Validation[E, A], are either an instance of Success[A]
 *   or Failure[E]. The code is modelled on the FunctionalJava Validation.
 *
 * datatype Validation[E, A] = Failure of E
 *                           | Success of A
 *
 * @param E                     the type of element in a failure
 * @param A                     the type of element in a success
 *
 * @author	                    Ken Barclay
 * @since                       December 2018
 */

import com.adt.kotlin.data.immutable.either.Either
import com.adt.kotlin.data.immutable.either.Either.Left
import com.adt.kotlin.data.immutable.either.Either.Right

import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.OptionF.none
import com.adt.kotlin.data.immutable.option.OptionF.some

import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.ListF
import com.adt.kotlin.data.immutable.nel.NonEmptyList


typealias ValidationNel<E, A> = Validation<NonEmptyList<E>, A>
typealias FailureNel<E, A> = Validation.Failure<NonEmptyList<E>, A>
typealias SuccessNel<E, A> = Validation.Success<NonEmptyList<E>, A>

sealed class Validation<out E, out A> {



    class Failure<out E, out A> internal constructor(val value: E) : Validation<E, A>() {

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
                @Suppress("UNCHECKED_CAST") val otherFailure: Failure<E, A> = other as Failure<E, A>
                (this.value == otherFailure.value)
            }
        }   // equals

        override fun toString(): String = "Failure($value)"

    }   // Failure



    class Success<out E, out A> internal constructor(val value: A) : Validation<E, A>() {

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
                @Suppress("UNCHECKED_CAST") val otherSuccess: Success<E, A> = other as Success<E, A>
                (this.value == otherSuccess.value)
            }
        }   // equals

        override fun toString(): String = "Success($value)"

    }   // Success



    /**
     * Return true if the Validation is a Failure.
     */
    fun isFailure(): Boolean {
        return when (this) {
            is Failure -> true
            is Success -> false
        }
    }   // isFailure

    /**
     * Return true if this Validation is a Success.
     */
    fun isSuccess(): Boolean = !isFailure()

    /**
     * Is this a Success and matching the given predicate.
     */
    fun exists(predicate: (A) -> Boolean): Boolean = this.fold({false}, {a -> predicate(a)})

    /**
     * Run the application of the first function if this is a Failure, otherwise the application of the
     *   second function if this is a Success.
     *
     * @param fail              the function to apply if this is a Failure
     * @param succ              the function to apply if this is a Success
     * @return                  application of the corresponding function
     */
    fun <B> fold(fail: (E) -> B, succ: (A) -> B): B {
        return when (this) {
            is Failure -> fail(this.value)
            is Success -> succ(this.value)
        }
    }   // fold

    /**
     * Map the given function on the success of this validation.
     *
     * @param f                 transformer function
     * @return                  new validation
     */
    fun <B> map(f: (A) -> B): Validation<E, B> {
        return when (this) {
            is Failure -> Failure(this.value)
            is Success -> Success(f(this.value))
        }
    }   // map

    /**
     * Flip the Failure/Success values.
     *
     * @return                  new validation
     */
    fun swap(): Validation<A, E> {
        return when(this) {
            is Failure -> Success(this.value)
            is Success -> Failure(this.value)
        }
    }   // swap

    /**
     * Return Success values wrapped in Some, and None for Failure values.
     */
    fun toOption(): Option<A> {
        return when(this) {
            is Failure -> none()
            is Success -> some(this.value)
        }
    }   // toOption

    /**
     * Convert the value to an Either<E, A>.
     */
    fun toEither(): Either<E, A> {
        return when(this) {
            is Failure -> Left(this.value)
            is Success -> Right(this.value)
        }
    }
    /**
     * Convert this value to a single element List if it is a Success,
     *   otherwise return an empty List.
     */
    fun toList(): List<A> {
        return when(this) {
            is Failure -> ListF.empty()
            is Success -> ListF.singleton(this.value)
        }
    }   // toList

}   // Validation
