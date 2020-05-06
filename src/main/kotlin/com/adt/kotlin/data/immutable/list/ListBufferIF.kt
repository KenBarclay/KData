package com.adt.kotlin.data.immutable.list

/**
 * A buffered implementation of a list.
 *
 * This is achieved by manipulating the list pointers. It supports constant time
 *   prepend and append operations.  Most other operations are linear.
 *
 * @author	                    Ken Barclay
 * @since                       October 2019
 */



interface ListBufferIF<A> {

    /**
     * Clear the buffer's content.
     */
    fun clear()

    /**
     * The current length of the buffer
     *
     * @return                   number of elements in the buffer
     */
    fun length(): Int

    /**
     * Convert this buffer to a list. The operation takes constant
     *   time.
     *
     * @return                  the buffer content as a list
     */
    fun toList(): List<A>

    /**
     * Determine if the buffer contains the given element.
     *
     * @param t                 search element
     * @return                  true if search element is present, false otherwise
     */
    fun contains(t: A): Boolean

    /**
     * Prepend a single element to this buffer. The operation takes
     *   constant time.
     *
     * @param t                 element to prepend
     * @return                  this buffer
     */
    fun prepend(t: A): ListBufferIF<A>

    /**
     * Prepend all the elements from the parameter to this buffer.
     *
     * @param xs                elements to append
     * @return                  this buffer
     */
    fun prepend(xs: List<A>): ListBufferIF<A>

    /**
     * Prepend the elements of this buffer to the given list.
     *
     * @param ts                existing list
     * @return                  new list with the concatenated elements of this buffer
     */
    fun prependTo(ts: List<A>): List<A>

    /**
     * Append a single element to this buffer. The operation takes
     *   constant time.
     *
     * @param t                 element to append
     * @return                  this buffer
     */
    fun append(t: A): ListBufferIF<A>

    /**
     * Append all the elements from the parameter to this buffer.
     *
     * @param xs                elements to append
     * @return                  this buffer
     */
    fun append(xs: List<A>): ListBufferIF<A>

}   // ListBufferIF
