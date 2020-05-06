package com.adt.kotlin.data.immutable.tuple

/**
 * A Tuple1 represents a single value.
 *
 * @author	                    Ken Barclay
 * @since                       October 2019
 */



class Tuple1<out A>(val a: A) {

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
            @Suppress("UNCHECKED_CAST") val otherTuple1: Tuple1<A> = other as Tuple1<A>
            (this.a == otherTuple1.a)
        }
    }   // equals

    /**
     * Apply the function to this context.
     */
    fun <B> map(f: (A) -> B): Tuple1<B> = Tuple1(f(a))

    /**
     * Return a string representation of the object.
     *
     * @return                  string representation
     */
    override fun toString(): String = "Tuple1($a)"

}   // Tuple1
