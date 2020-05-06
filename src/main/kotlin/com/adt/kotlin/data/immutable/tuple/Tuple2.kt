package com.adt.kotlin.data.immutable.tuple

/**
 * A Tuple2 represents a pair of values.
 *
 * @author	                    Ken Barclay
 * @since                       October 2019
 */



class Tuple2<out A, out B>(val a: A, val b: B) {

    operator fun component1(): A = a
    operator fun component2(): B = b

    /**
     * Apply the function to this context. Specifically, the function is applied
     *   to the second element.
     */
    fun <C> map(f: (B) -> C): Tuple2<A, C> = Tuple2(a, f(b))

    /**
     * Return a string representation of the object.
     *
     * @return                  string representation
     */
    override fun toString(): String = "Tuple2($a, $b)"

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
            @Suppress("UNCHECKED_CAST") val otherTuple2: Tuple2<A, B> = other as Tuple2<A, B>
            (this.a == otherTuple2.a) && (this.b == otherTuple2.b)
        }
    }   // equals

}   // Tuple2



infix fun <A, B> A.toT2(b: B): Tuple2<A, B> = Tuple2(this, b)
