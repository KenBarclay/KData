package com.adt.kotlin.data.immutable.nel

/**
 * A singly-linked list that is guaranteed to be non-empty. A data type which
 *   represents a non empty list, with single element (hd) and optional
 *   structure (tl).
 *
 * The documentation uses the notation [x0 :| x1, x2, ...] to represent a
 *   list instance.
 *
 * @param A                     the (covariant) type of elements in the list
 *
 * @author	                    Ken Barclay
 * @since                       October 2019
 */

class NonEmptyListException(message: String) : Exception(message)
