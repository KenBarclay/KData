package com.adt.kotlin.data.immutable.tri

/**
 * The Try type represents a computation that may return a successfully computed value
 *   or result in an exception. Instances of Try[A], are either an instance of Success[A]
 *   or Failure[A]. The code is modelled on the Scala Try.
 *
 * The algebraic data type declaration is:
 *
 * datatype Try[A] = Failure
 *                 | Success A
 *
 * @param A                     the type of element
 *
 * @author	                    Ken Barclay
 * @since                       October 2014
 */



sealed class Try<out A> {



    class Failure<out A> internal constructor(val throwable: Throwable) : Try<A>() {

        override val isFailure: Boolean = true
        override val isSuccess: Boolean = false

        /**
         * Convert this to a Failure if the predicate is not satisfied.
         *
         * @param predicate         test criteria
         * @return                  a Failure if this is one or this does not satisfy the predicate; this otherwise
         */
        override fun filter(predicate: (A) -> Boolean): Try<A> = this

        /**
         * Return the value if this is a Success or throws the exception if this is a Failure.
         *
         * Examples:
         *   success(123).get() == 123
         *   failure(Exception("error")) == error
         *
         * @return                  the value or an exception
         */
        override fun get(): A = throw throwable

        /**
         * Map the given function to the value from this Success or returns this if this is a Failure.
         *
         * Examples:
         *   success(123).map{n -> 1 + n} == success(124)
         *   failure(Exception("error")).map{n -> 1 + n} == failure(Exception("error"))
         *
         * @param f                 transformation function
         * @return                  a value of type B wrapped in a Try
         */
        override fun <B> map(f: (A) -> B): Try<B> = Failure(throwable)

        /**
         * Returns a string representation of the object.
         *
         * @return                  string representation
         */
        override fun toString(): String = "Failure(${throwable.message})"

    }   // Failure



    class Success<out A> internal constructor(val value: A) : Try<A>() {

        override val isFailure: Boolean = false
        override val isSuccess: Boolean = true

        /**
         * Convert this to a Failure if the predicate is not satisfied.
         *
         * @param predicate         test criteria
         * @return                  a Failure if this is one or this does not satisfy the predicate; this otherwise
         */
        override fun filter(predicate: (A) -> Boolean): Try<A> =
            if (predicate(value)) this else Failure(TryException("filter: predicate does not hold for ${value}"))

        /**
         * Return the value if this is a Success or throws the exception if this is a Failure.
         *
         * Examples:
         *   success(123).get() == 123
         *   failure(Exception("error")) == error
         *
         * @return                  the value or an exception
         */
        override fun get(): A = value

        /**
         * Map the given function to the value from this Success or returns this if this is a Failure.
         *
         * Examples:
         *   success(123).map{n -> 1 + n} == success(124)
         *   failure(Exception("error")).map{n -> 1 + n} == failure(Exception("error"))
         *
         * @param f                 transformation function
         * @return                  a value of type B wrapped in a Try
         */
        override fun <B> map(f: (A) -> B): Try<B> = Success(f(value))

        /**
         * Returns a string representation of the object.
         *
         * @return                  string representation
         */
        override fun toString(): String = "Success(${value})"

    }   // Success



    open val isFailure: Boolean = false
    open val isSuccess: Boolean = false

    /**
     * Convert this to a Failure if the predicate is not satisfied.
     *
     * @param predicate         test criteria
     * @return                  a Failure if this is one or this does not satisfy the predicate; this otherwise
     */
    abstract fun filter(predicate: (A) -> Boolean): Try<A>

    /**
     * Return the value if this is a Success or throws the exception if this is a Failure.
     *
     * Examples:
     *   success(123).get() == 123
     *   failure(Exception("error")) == error
     *
     * @return                  the value or an exception
     */
    abstract fun get(): A

    /**
     * Map the given function to the value from this Success or returns this if this is a Failure.
     *
     * Examples:
     *   success(123).map{n -> 1 + n} == success(124)
     *   failure(Exception("error")).map{n -> 1 + n} == failure(Exception("error"))
     *
     * @param f                 transformation function
     * @return                  a value of type B wrapped in a Try
     */
    abstract fun <B> map(f: (A) -> B): Try<B>

    /**
     * Applies failure if this is a Failure, else success if this is a Success.
     *
     * Examples:
     *   success(123).fold({_ -> false}, {n -> (n % 2 == 1)}) == true
     *   failure(Exception("error")).fold({_ -> false}, {n -> (n % 2 == 1)}) == false
     */
    fun <B> fold(failure: (Throwable) -> B, success: (A) -> B): B {
        return when (this) {
            is Failure -> failure(this.throwable)
            is Success -> success(this.value)
        }
    }   // fold

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
            @Suppress("UNCHECKED_CAST") val otherTry: Try<A> = other as Try<A>
            when (this) {
                is Failure -> when (otherTry) {
                    is Failure -> (this.throwable.message == otherTry.throwable.message)
                    is Success -> false
                }
                is Success -> when (otherTry) {
                    is Failure -> false
                    is Success -> (this.value == otherTry.value)
                }
            }
        }
    }   // equals

}   // Try
