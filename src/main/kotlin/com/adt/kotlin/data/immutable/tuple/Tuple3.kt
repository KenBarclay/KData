package com.adt.kotlin.data.immutable.tuple

/**
 * A Tuple3 represents a triple of values.
 *
 * @author	                    Ken Barclay
 * @since                       October 2019
 */



class Tuple3<out A, out B, out C>(val a: A, val b: B, val c: C) {

    /**
     * Return a string representation of the object.
     *
     * @return                  string representation
     */
    override fun toString(): String = "Tuple3($a, $b, $c)"

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
            @Suppress("UNCHECKED_CAST") val otherTuple3: Tuple3<A, B, C> = other as Tuple3<A, B, C>
            (this.a == otherTuple3.a) && (this.b == otherTuple3.b) && (this.c == otherTuple3.c)
        }
    }   // equals

}   // Tuple3
