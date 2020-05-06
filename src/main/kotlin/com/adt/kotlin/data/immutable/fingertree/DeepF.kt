package com.adt.kotlin.data.immutable.fingertree

/**
 * A finger tree with 1-4-digits on the left and right, and a
 *   finger tree of 2-3-nodes in the middle.
 *
 * @author	                    Ken Barclay
 * @since                       March 2014
 */

import com.adt.kotlin.data.immutable.fingertree.digit.*
import com.adt.kotlin.data.immutable.fingertree.node.*



object DeepF {

    /**
     * Smart constructor.
     */
    fun <V, A> deep(measured: Measured<V, A>, prefix: Digit<V, A>, middle: FingerTree<V, Node<V, A>>, suffix: Digit<V, A>): Deep<V, A> =
            Deep(measured, measured.sum(prefix.measure(), measured.sum(middle.measure(), suffix.measure())), prefix, middle, suffix)



    fun <V, A> addDigits0(measured: Measured<V, A>, tree1: FingerTree<V, Node<V, A>>, digit1: Digit<V, A>, digit2: Digit<V, A>, tree2: FingerTree<V, Node<V, A>>): FingerTree<V, Node<V, A>> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return when (digit1) {
            is One -> {
                when (digit2) {
                    is One -> append1(measured, tree1, mk.node2(digit1.a, digit2.a), tree2)
                    is Two -> append1(measured, tree1, mk.node3(digit1.a, digit2.a1, digit2.a2), tree2)
                    is Three -> append2(measured, tree1, mk.node2(digit1.a, digit2.a1), mk.node2(digit2.a2, digit2.a3), tree2)
                    is Four -> append2(measured, tree1, mk.node3(digit1.a, digit2.a1, digit2.a2), mk.node2(digit2.a3, digit2.a4), tree2)
                }
            }
            is Two -> {
                when (digit2) {
                    is One -> append1(measured, tree1, mk.node3(digit1.a1, digit1.a2, digit2.a), tree2)
                    is Two -> append2(measured, tree1, mk.node2(digit1.a1, digit1.a2), mk.node2(digit2.a1, digit2.a2), tree2)
                    is Three -> append2(measured, tree1, mk.node3(digit1.a1, digit1.a2, digit2.a1), mk.node2(digit2.a2, digit2.a3), tree2)
                    is Four -> append2(measured, tree1, mk.node3(digit1.a1, digit1.a2, digit2.a1), mk.node3(digit2.a2, digit2.a3, digit2.a4), tree2)
                }
            }
            is Three -> {
                when (digit2) {
                    is One -> append2(measured, tree1, mk.node2(digit1.a1, digit1.a2), mk.node2(digit1.a3, digit2.a), tree2)
                    is Two -> append2(measured, tree1, mk.node3(digit1.a1, digit1.a2, digit1.a3), mk.node2(digit2.a1, digit2.a2), tree2)
                    is Three -> append2(measured, tree1, mk.node3(digit1.a1, digit1.a2, digit1.a3), mk.node3(digit2.a1, digit2.a2, digit2.a3), tree2)
                    is Four -> append3(measured, tree1, mk.node3(digit1.a1, digit1.a2, digit1.a3), mk.node2(digit2.a1, digit2.a2), mk.node2(digit2.a3, digit2.a4), tree2)
                }
            }
            is Four -> {
                when (digit2) {
                    is One -> append2(measured, tree1, mk.node3(digit1.a1, digit1.a2, digit1.a3), mk.node2(digit1.a4, digit2.a), tree2)
                    is Two -> append2(measured, tree1, mk.node3(digit1.a1, digit1.a2, digit1.a3), mk.node3(digit1.a4, digit2.a1, digit2.a2), tree2)
                    is Three -> append3(measured, tree1, mk.node3(digit1.a1, digit1.a2, digit1.a3), mk.node2(digit1.a4, digit2.a1), mk.node2(digit2.a2, digit2.a3), tree2)
                    is Four -> append3(measured, tree1, mk.node3(digit1.a1, digit1.a2, digit1.a3), mk.node3(digit1.a4, digit2.a1, digit2.a2), mk.node2(digit2.a3, digit2.a4), tree2)
                }
            }
        }
    }   // addDigits0

    fun <V, A> append1(measured: Measured<V, A>, tree1: FingerTree<V, Node<V, A>>, node: Node<V, A>, tree2: FingerTree<V, Node<V, A>>): FingerTree<V, Node<V, A>> {
        return when (tree1) {
            is Empty -> tree2.cons(node)
            is Single -> tree2.cons(node).cons(tree1.a)
            is Deep -> {
                when (tree2) {
                    is Empty -> tree1.snoc(node)
                    is Single -> tree1.snoc(node).snoc(tree2.a)
                    is Deep -> {
                        val nodeMeasured: Measured<V, Node<V, A>> = measured.nodeMeasured()
                        val mkn: MakeTree<V, Node<V, A>> = MakeTree(nodeMeasured)
                        mkn.deep(tree1.prefix, addDigits1(nodeMeasured, tree1.middle, tree1.suffix, node, tree2.prefix, tree2.middle), tree2.suffix)
                    }
                }
            }
        }
    }   // append1

    fun <V, A> addDigits1(measured: Measured<V, Node<V, A>>, tree1: FingerTree<V, Node<V, Node<V, A>>>, digit1: Digit<V, Node<V, A>>, node: Node<V, A>, digit2: Digit<V, Node<V, A>>, tree2: FingerTree<V, Node<V, Node<V, A>>>): FingerTree<V, Node<V, Node<V, A>>> {
        val mk: MakeTree<V, Node<V, A>> = MakeTree(measured)
        return when (digit1) {
            is One -> {
                when (digit2) {
                    is One -> append1(measured, tree1, mk.node3(digit1.a, node, digit2.a), tree2)
                    is Two -> append2(measured, tree1, mk.node2(digit1.a, node), mk.node2(digit2.a1, digit2.a2), tree2)
                    is Three -> append2(measured, tree1, mk.node3(digit1.a, node, digit2.a1), mk.node2(digit2.a2, digit2.a3), tree2)
                    is Four -> append2(measured, tree1, mk.node3(digit1.a, node, digit2.a1), mk.node3(digit2.a2, digit2.a3, digit2.a4), tree2)
                }
            }
            is Two -> {
                when (digit2) {
                    is One -> append2(measured, tree1, mk.node2(digit1.a1, digit1.a2), mk.node2(node, digit2.a), tree2)
                    is Two -> append2(measured, tree1, mk.node3(digit1.a1, digit1.a2, node), mk.node2(digit2.a1, digit2.a2), tree2)
                    is Three -> append2(measured, tree1, mk.node3(digit1.a1, digit1.a2, node), mk.node3(digit2.a1, digit2.a2, digit2.a3), tree2)
                    is Four -> append3(measured, tree1, mk.node3(digit1.a1, digit1.a2, node), mk.node2(digit2.a1, digit2.a2), mk.node2(digit2.a3, digit2.a4), tree2)
                }
            }
            is Three -> {
                when (digit2) {
                    is One -> append2(measured, tree1, mk.node3(digit1.a1, digit1.a2, digit1.a3), mk.node2(node, digit2.a), tree2)
                    is Two -> append2(measured, tree1, mk.node3(digit1.a1, digit1.a2, digit1.a3), mk.node3(node, digit2.a1, digit2.a2), tree2)
                    is Three -> append3(measured, tree1, mk.node3(digit1.a1, digit1.a2, digit1.a3), mk.node2(node, digit2.a1), mk.node2(digit2.a2, digit2.a3), tree2)
                    is Four -> append3(measured, tree1, mk.node3(digit1.a1, digit1.a2, digit1.a3), mk.node3(node, digit2.a1, digit2.a2), mk.node2(digit2.a3, digit2.a4), tree2)
                }
            }
            is Four -> {
                when (digit2) {
                    is One -> append2(measured, tree1, mk.node3(digit1.a1, digit1.a2, digit1.a3), mk.node3(digit1.a4, node, digit2.a), tree2)
                    is Two -> append3(measured, tree1, mk.node3(digit1.a1, digit1.a2, digit1.a3), mk.node2(digit1.a4, node), mk.node2(digit2.a1, digit2.a2), tree2)
                    is Three -> append3(measured, tree1, mk.node3(digit1.a1, digit1.a2, digit1.a3), mk.node3(digit1.a4, node, digit2.a1), mk.node2(digit2.a2, digit2.a3), tree2)
                    is Four -> append3(measured, tree1, mk.node3(digit1.a1, digit1.a2, digit1.a3), mk.node3(digit1.a4, node, digit2.a1), mk.node3(digit2.a2, digit2.a3, digit2.a4), tree2)
                }
            }
        }
    }   // addDigits1

    fun <V, A> append2(measured: Measured<V, A>, tree1: FingerTree<V, Node<V, A>>, node1: Node<V, A>, node2: Node<V, A>, tree2: FingerTree<V, Node<V, A>>): FingerTree<V, Node<V, A>> {
        return when (tree1) {
            is Empty -> tree2.cons(node2).cons(node1)
            is Single -> tree2.cons(node2).cons(node1).cons(tree1.a)
            is Deep -> {
                when (tree2) {
                    is Empty -> tree1.snoc(node1).snoc(node2)
                    is Single -> tree1.snoc(node1).snoc(node2).snoc(tree2.a)
                    is Deep -> {
                        val nodeMeasured: Measured<V, Node<V, A>> = measured.nodeMeasured()
                        val mkn: MakeTree<V, Node<V, A>> = MakeTree(nodeMeasured)
                        mkn.deep(tree1.prefix, addDigits2(nodeMeasured, tree1.middle, tree1.suffix, node1, node2, tree2.prefix, tree2.middle), tree2.suffix)
                    }
                }
            }
        }
    }   // append2

    fun <V, A> addDigits2(measured: Measured<V, Node<V, A>>, tree1: FingerTree<V, Node<V, Node<V, A>>>, suffix: Digit<V, Node<V, A>>, node1: Node<V, A>, node2: Node<V, A>, prefix: Digit<V, Node<V, A>>, tree2: FingerTree<V, Node<V, Node<V, A>>>): FingerTree<V, Node<V, Node<V, A>>> {
        val mk: MakeTree<V, Node<V, A>> = MakeTree(measured)
        return when (suffix) {
            is One -> {
                when (prefix) {
                    is One -> append2(measured, tree1, mk.node2(suffix.a, node1), mk.node2(node2, prefix.a), tree2)
                    is Two -> append2(measured, tree1, mk.node3(suffix.a, node1, node2), mk.node2(prefix.a1, prefix.a2), tree2)
                    is Three -> append2(measured, tree1, mk.node3(suffix.a, node1, node2), mk.node3(prefix.a1, prefix.a2, prefix.a3), tree2)
                    is Four -> append3(measured, tree1, mk.node3(suffix.a, node1, node2), mk.node2(prefix.a1, prefix.a2), mk.node2(prefix.a3, prefix.a4), tree2)
                }
            }
            is Two -> {
                when (prefix) {
                    is One -> append2(measured, tree1, mk.node3(suffix.a1, suffix.a2, node1), mk.node2(node2, prefix.a), tree2)
                    is Two -> append2(measured, tree1, mk.node3(suffix.a1, suffix.a2, node1), mk.node3(node2, prefix.a1, prefix.a2), tree2)
                    is Three -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, node1), mk.node2(node2, prefix.a1), mk.node2(prefix.a2, prefix.a3), tree2)
                    is Four -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, node1), mk.node3(node2, prefix.a1, prefix.a2), mk.node2(prefix.a3, prefix.a4), tree2)
                }
            }
            is Three -> {
                when (prefix) {
                    is One -> append2(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(node1, node2, prefix.a), tree2)
                    is Two -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node2(node1, node2), mk.node2(prefix.a1, prefix.a2), tree2)
                    is Three -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(node1, node2, prefix.a1), mk.node2(prefix.a2, prefix.a3), tree2)
                    is Four -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(node1, node2, prefix.a1), mk.node3(prefix.a2, prefix.a3, prefix.a4), tree2)
                }
            }
            is Four -> {
                when (prefix) {
                    is One -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node2(suffix.a4, node1), mk.node2(node2, prefix.a), tree2)
                    is Two -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(suffix.a4, node1, node2), mk.node2(prefix.a1, prefix.a2), tree2)
                    is Three -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(suffix.a4, node1, node2), mk.node3(prefix.a1, prefix.a2, prefix.a3), tree2)
                    is Four -> append4(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(suffix.a4, node1, node2), mk.node2(prefix.a1, prefix.a2), mk.node2(prefix.a3, prefix.a4), tree2)
                }
            }
        }
    }   // addDigits2

    fun <V, A> append3(measured: Measured<V, A>, tree1: FingerTree<V, Node<V, A>>, node1: Node<V, A>, node2: Node<V, A>, node3: Node<V, A>, tree2: FingerTree<V, Node<V, A>>): FingerTree<V, Node<V, A>> {
        return when (tree1) {
            is Empty -> tree2.cons(node3).cons(node2).cons(node1)
            is Single -> tree2.cons(node3).cons(node2).cons(node1).cons(tree1.a)
            is Deep -> {
                when (tree2) {
                    is Empty -> tree1.snoc(node1).snoc(node2).snoc(node3)
                    is Single -> tree1.snoc(node1).snoc(node2).snoc(node3).snoc(tree2.a)
                    is Deep -> {
                        val nodeMeasured: Measured<V, Node<V, A>> = measured.nodeMeasured()
                        val mkn: MakeTree<V, Node<V, A>> = MakeTree(nodeMeasured)
                        mkn.deep(tree1.prefix, addDigits3(nodeMeasured, tree1.middle, tree1.suffix, node1, node2, node3, tree2.prefix, tree2.middle), tree2.suffix)
                    }
                }
            }
        }
    }   // append3

    fun <V, A> addDigits3(measured: Measured<V, Node<V, A>>, tree1: FingerTree<V, Node<V, Node<V, A>>>, suffix: Digit<V, Node<V, A>>, node1: Node<V, A>, node2: Node<V, A>, node3: Node<V, A>, prefix: Digit<V, Node<V, A>>, tree2: FingerTree<V, Node<V, Node<V, A>>>): FingerTree<V, Node<V, Node<V, A>>> {
        val mk: MakeTree<V, Node<V, A>> = MakeTree(measured)
        return when (suffix) {
            is One -> {
                when (prefix) {
                    is One -> append2(measured, tree1, mk.node3(suffix.a, node1, node2), mk.node2(node3, prefix.a), tree2)
                    is Two -> append2(measured, tree1, mk.node3(suffix.a, node1, node2), mk.node3(node3, prefix.a1, prefix.a2), tree2)
                    is Three -> append3(measured, tree1, mk.node3(suffix.a, node1, node2), mk.node2(node3, prefix.a1), mk.node2(prefix.a2, prefix.a3), tree2)
                    is Four -> append3(measured, tree1, mk.node3(suffix.a, node1, node2), mk.node3(node3, prefix.a1, prefix.a2), mk.node2(prefix.a3, prefix.a4), tree2)
                }
            }
            is Two -> {
                when (prefix) {
                    is One -> append2(measured, tree1, mk.node3(suffix.a1, suffix.a2, node1), mk.node3(node2, node3, prefix.a), tree2)
                    is Two -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, node1), mk.node2(node2, node3), mk.node2(prefix.a1, prefix.a2), tree2)
                    is Three -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, node1), mk.node3(node2, node3, prefix.a1), mk.node2(prefix.a2, prefix.a3), tree2)
                    is Four -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, node1), mk.node3(node2, node3, prefix.a1), mk.node3(prefix.a2, prefix.a3, prefix.a4), tree2)
                }
            }
            is Three -> {
                when (prefix) {
                    is One -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node2(node1, node2), mk.node2(node3, prefix.a), tree2)
                    is Two -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(node1, node2, node3), mk.node2(prefix.a1, prefix.a2), tree2)
                    is Three -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(node1, node2, node3), mk.node3(prefix.a1, prefix.a2, prefix.a3), tree2)
                    is Four -> append4(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(node1, node2, node3), mk.node2(prefix.a1, prefix.a2), mk.node2(prefix.a3, prefix.a4), tree2)
                }
            }
            is Four -> {
                when (prefix) {
                    is One -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(suffix.a4, node1, node2), mk.node2(node3, prefix.a), tree2)
                    is Two -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(suffix.a4, node1, node2), mk.node3(node3, prefix.a1, prefix.a2), tree2)
                    is Three -> append4(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(suffix.a4, node1, node2), mk.node2(node3, prefix.a1), mk.node2(prefix.a2, prefix.a3), tree2)
                    is Four -> append4(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(suffix.a4, node1, node2), mk.node3(node3, prefix.a1, prefix.a2), mk.node2(prefix.a3, prefix.a4), tree2)
                }
            }
        }
    }   // addDigits3

    fun <V, A> append4(measured: Measured<V, A>, tree1: FingerTree<V, Node<V, A>>, node1: Node<V, A>, node2: Node<V, A>, node3: Node<V, A>, node4: Node<V, A>, tree2: FingerTree<V, Node<V, A>>): FingerTree<V, Node<V, A>> {
        return when (tree1) {
            is Empty -> tree2.cons(node4).cons(node3).cons(node2).cons(node1)
            is Single -> tree2.cons(node4).cons(node3).cons(node2).cons(node1).cons(tree1.a)
            is Deep -> {
                when (tree2) {
                    is Empty -> tree1.snoc(node1).snoc(node2).snoc(node3).snoc(node4)
                    is Single -> tree1.snoc(node1).snoc(node2).snoc(node3).snoc(node4).snoc(tree2.a)
                    is Deep -> {
                        val nodeMeasured: Measured<V, Node<V, A>> = measured.nodeMeasured()
                        val mkn: MakeTree<V, Node<V, A>> = MakeTree(nodeMeasured)
                        mkn.deep(tree1.prefix, addDigits4(nodeMeasured, tree1.middle, tree1.suffix, node1, node2, node3, node4, tree2.prefix, tree2.middle), tree2.suffix)
                    }
                }
            }
        }
    }   // append4

    fun <V, A> addDigits4(measured: Measured<V, Node<V, A>>, tree1: FingerTree<V, Node<V, Node<V, A>>>, suffix: Digit<V, Node<V, A>>, node1: Node<V, A>, node2: Node<V, A>, node3: Node<V, A>, node4: Node<V, A>, prefix: Digit<V, Node<V, A>>, tree2: FingerTree<V, Node<V, Node<V, A>>>): FingerTree<V, Node<V, Node<V, A>>> {
        val mk: MakeTree<V, Node<V, A>> = MakeTree(measured)
        return when (suffix) {
            is One -> {
                when (prefix) {
                    is One -> append2(measured, tree1, mk.node3(suffix.a, node1, node2), mk.node3(node3, node4, prefix.a), tree2)
                    is Two -> append3(measured, tree1, mk.node3(suffix.a, node1, node2), mk.node2(node3, node4), mk.node2(prefix.a1, prefix.a2), tree2)
                    is Three -> append3(measured, tree1, mk.node3(suffix.a, node1, node2), mk.node3(node3, node4, prefix.a1), mk.node2(prefix.a2, prefix.a3), tree2)
                    is Four -> append3(measured, tree1, mk.node3(suffix.a, node1, node2), mk.node3(node3, node4, prefix.a1), mk.node3(prefix.a2, prefix.a3, prefix.a4), tree2)
                }
            }
            is Two -> {
                when (prefix) {
                    is One -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, node1), mk.node2(node2, node3), mk.node2(node4, prefix.a), tree2)
                    is Two -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, node1), mk.node3(node2, node3, node4), mk.node2(prefix.a1, prefix.a2), tree2)
                    is Three -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, node1), mk.node3(node2, node3, node4), mk.node3(prefix.a1, prefix.a2, prefix.a3), tree2)
                    is Four -> append4(measured, tree1, mk.node3(suffix.a1, suffix.a2, node1), mk.node3(node2, node3, node4), mk.node2(prefix.a1, prefix.a2), mk.node2(prefix.a3, prefix.a4), tree2)
                }
            }
            is Three -> {
                when (prefix) {
                    is One -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(node1, node2, node3), mk.node2(node4, prefix.a), tree2)
                    is Two -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(node1, node2, node3), mk.node3(node4, prefix.a1, prefix.a2), tree2)
                    is Three -> append4(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(node1, node2, node3), mk.node2(node4, prefix.a1), mk.node2(prefix.a2, prefix.a3), tree2)
                    is Four -> append4(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(node1, node2, node3), mk.node3(node4, prefix.a1, prefix.a2), mk.node2(prefix.a3, prefix.a4), tree2)
                }
            }
            is Four -> {
                when (prefix) {
                    is One -> append3(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(suffix.a4, node1, node2), mk.node3(node3, node4, prefix.a), tree2)
                    is Two -> append4(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(suffix.a4, node1, node2), mk.node2(node3, node4), mk.node2(prefix.a1, prefix.a2), tree2)
                    is Three -> append4(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(suffix.a4, node1, node2), mk.node3(node3, node4, prefix.a1), mk.node2(prefix.a2, prefix.a3), tree2)
                    is Four -> append4(measured, tree1, mk.node3(suffix.a1, suffix.a2, suffix.a3), mk.node3(suffix.a4, node1, node2), mk.node3(node3, node4, prefix.a1), mk.node3(prefix.a2, prefix.a3, prefix.a4), tree2)
                }
            }
        }
    }   // addDigits4

}   // DeepF
