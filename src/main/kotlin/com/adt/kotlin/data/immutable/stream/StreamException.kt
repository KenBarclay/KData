package com.adt.kotlin.data.immutable.stream

/**
 * A sequence of elements supporting sequential aggregate operations.
 *   To perform a computation, stream operations are composed into a
 *   stream pipeline. A stream pipeline consists of a source
 *   (which might be an array, a collection, a generator function, etc),
 *   zero or more intermediate operations (which transform a stream
 *   into another stream, such as filter), and a terminal operation
 *   (which produces a result or side-effect, such as count or forEach).
 *   Streams are lazy: computation on the source data is only performed
 *   when the terminal operation is initiated, and source elements are
 *   consumed only as needed.
 *
 * @param A                     the (covariant) type of elements in the stream
 *
 * @author	                    Ken Barclay
 * @since                       November 2019
 */



class StreamException(message: String) : Exception(message)
