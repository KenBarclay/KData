package com.adt.kotlin.data.immutable.fingertree

/**
 * Determines how the elements of a tree are measured and how measures
 *   are summed. Consists of a monoid and a measuring function. Different
 *   instances of this class will result in different behaviours for the tree.
 *
 * @author	                    Ken Barclay
 * @since                       September 2019
 */

import com.adt.kotlin.data.immutable.fingertree.digit.Digit
import com.adt.kotlin.data.immutable.fingertree.node.Node
import com.adt.kotlin.hkfp.typeclass.Monoid



data class Measured<V, A> internal constructor(val monoid: Monoid<V>, val measuring: (A) -> V) {

    override fun toString(): String = "Measured(...)"

    /**
     * Measures a given element.
     *
     * @param a                 an element to measure
     * @return                  the element's measurement
     */
    fun measure(a: A): V = measuring(a)

    /**
     * Sums the given measurements with the monoid.
     *
     * @param v1                a measurement to add to another
     * @param v2                a measurement to add to another
     * @return                  the sum of the two measurements
     */
    fun sum(v1: V, v2: V): V = monoid.combine(v1, v2)

    /**
     * Returns the identity measurement for the monoid.
     *
     * @return                  the identity measurement for the monoid
     */
    fun empty(): V = monoid.empty

    /**
     * A measured instance for nodes.
     *
     * @return                  a measured instance for nodes
     */
    fun nodeMeasured(): Measured<V, Node<V, A>> = Measured(monoid, {node: Node<V, A> -> node.measure})

    /**
     * A measured instance for digits.
     *
     * @return                  a measured instance for digits
     */
    fun digitMeasured(): Measured<V, Digit<V, A>> = Measured(monoid, {digit: Digit<V, A> -> digit.measure()})

}   // Measured
