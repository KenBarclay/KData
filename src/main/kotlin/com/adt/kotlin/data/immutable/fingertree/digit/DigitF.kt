package com.adt.kotlin.data.immutable.fingertree.digit

/**
 * A digit is a vector of 1-4 elements. Serves as a pointer to the
 *   prefix or suffix of a finger tree.
 *
 * @author	                    Ken Barclay
 * @since                       September 2019
 */

import com.adt.kotlin.data.immutable.fingertree.*
import com.adt.kotlin.data.immutable.fingertree.node.Node
import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.OptionF



object DigitF {

    fun <V, A> consDigit(a: A, digit: Digit<V, A>, measured: Measured<V, A>): Digit<V, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return when (digit) {
            is One -> mk.two(a, digit.a)
            is Two -> mk.three(a, digit.a1, digit.a2)
            is Three -> mk.four(a, digit.a1, digit.a2, digit.a3)
            is Four -> throw FingerTreeException("consDigit: Four digit")
        }
    }   // consDigit

    fun <V, A> snocDigit(a: A, digit: Digit<V, A>, measured: Measured<V, A>): Digit<V, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return when (digit) {
            is One -> mk.two(digit.a, a)
            is Two -> mk.three(digit.a1, digit.a2, a)
            is Three -> mk.four(digit.a1, digit.a2, digit.a3, a)
            is Four -> throw FingerTreeException("snocDigit: Four digit")
        }
    }   // snocDigit

    fun <V, A> digitToTree(digit: Digit<V, A>, measured: Measured<V, A>): FingerTree<V, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        val mkn: MakeTree<V, Node<V, A>> = MakeTree(measured.nodeMeasured())
        return when (digit) {
            is One -> mk.single(digit.a)
            is Two -> mk.deep(mk.one(digit.a1), mkn.empty(), mk.one(digit.a2))
            is Three -> mk.deep(mk.two(digit.a1, digit.a2), mkn.empty(), mk.one(digit.a3))
            is Four -> mk.deep(mk.two(digit.a1, digit.a2), mkn.empty(), mk.two(digit.a3, digit.a4))
        }
    }   // digitToTree

    fun <V, A> splitDigit(predicate: (V) -> Boolean, v: V, digit: Digit<V, A>, measured: Measured<V, A>): Split<Option<Digit<V, A>>, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return when (digit) {
            is One -> {
                Split(OptionF.none(), digit.a, OptionF.none())
            }
            is Two -> {
                val va: V = measured.sum(v, measured.measure(digit.a1))
                if (predicate(va))
                    Split(OptionF.none(), digit.a1, OptionF.some<Digit<V, A>>(mk.one(digit.a2)))
                else
                    Split(OptionF.some<Digit<V, A>>(mk.one(digit.a1)), digit.a2, OptionF.none())
            }
            is Three -> {
                val va: V = measured.sum(v, measured.measure(digit.a1))
                val vab: V = measured.sum(va, measured.measure(digit.a2))
                if (predicate(va))
                    Split(OptionF.none(), digit.a1, OptionF.some<Digit<V, A>>(mk.two(digit.a2, digit.a3)))
                else if (predicate(vab))
                    Split(OptionF.some<Digit<V, A>>(mk.one(digit.a1)), digit.a2, OptionF.some<Digit<V, A>>(mk.one(digit.a3)))
                else
                    Split(OptionF.some<Digit<V, A>>(mk.two(digit.a1, digit.a2)), digit.a3, OptionF.none())
            }
            is Four -> {
                val va: V = measured.sum(v, measured.measure(digit.a1))
                val vab: V = measured.sum(va, measured.measure(digit.a2))
                val vabc: V = measured.sum(vab, measured.measure(digit.a3))
                if (predicate(va))
                    Split(OptionF.none(), digit.a1, OptionF.some<Digit<V, A>>(mk.three(digit.a2, digit.a3, digit.a4)))
                else if (predicate(vab))
                    Split(OptionF.some<Digit<V, A>>(mk.one(digit.a1)), digit.a2, OptionF.some<Digit<V, A>>(mk.two(digit.a3, digit.a4)))
                else if (predicate(vabc))
                    Split(OptionF.some<Digit<V, A>>(mk.two(digit.a1, digit.a2)), digit.a3, OptionF.some<Digit<V, A>>(mk.one(digit.a4)))
                else
                    Split(OptionF.some<Digit<V, A>>(mk.three(digit.a1, digit.a2, digit.a3)), digit.a4, OptionF.none())
            }
        }
    }   // splitDigit

}   // DigitF
