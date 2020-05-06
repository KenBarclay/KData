package com.adt.kotlin.data.immutable.fingertree

/**
 * Provides 2-3 finger trees, a functional representation of persistent
 *   sequences supporting access to the ends in amortized O(1) time.
 *   Concatenation and splitting time is O(log n) in the size of the
 *   smaller piece.
 *
 * A general purpose data structure that can serve as a sequence,
 *   priority queue, search tree, priority search queue and more.
 *
 * This class serves as a data structure construction kit, rather
 *   than a data structure in its own right. By supplying a monoid,
 *   a measurement function, insertion, deletion, and so forth,
 *   any purely functional data structure can be emulated.
 *
 * Based on "Finger trees: a simple general-purpose data structure",
 *   by Ralf Hinze and Ross Paterson.
 *
 * @param V                     the monoidal type with which to annotate nodes
 * @param A                     the type of the tree's elements
 *
 * @author	                    Ken Barclay
 * @since                       September 2019
 */

import com.adt.kotlin.data.immutable.fingertree.digit.*
import com.adt.kotlin.data.immutable.fingertree.node.Node
import com.adt.kotlin.data.immutable.fingertree.node.NodeF

import com.adt.kotlin.hkfp.fp.FunctionF



sealed class FingerTree<V, A>(val measured: Measured<V, A>) {

    /**
     * Indicates whether this tree is empty.
     *
     * @return                  true if this tree is the empty tree, otherwise false
     */
    open fun isEmpty(): Boolean = false

    /**
     * Indicates whether this tree is single.
     *
     * @return                  true if this tree is the single tree, otherwise false
     */
    open fun isSingle(): Boolean = false

    /**
     * Indicates whether this tree is deep.
     *
     * @return                  true if this tree is deep, otherwise false
     */
    open fun isDeep(): Boolean = false

    /**
     * Folds the tree to the left with the given function and the given initial element.
     *
     * @param e                 an initial element to apply to the fold
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the left
     */
    abstract fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B

    /**
     * Folds the tree to the left with the given function and the given initial element.
     *
     * @param e                 an initial element to apply to the fold
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the left
     */
    abstract fun <B> foldLeft(e: B, f: (B, A) -> B): B

    /**
     * Folds the tree to the right with the given function and the given initial element.
     *
     * @param e                 an initial element to apply to the fold
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    abstract fun <B> foldRight(e: B, f: (A) -> (B) -> B): B

    /**
     * Folds the tree to the right with the given function and the given initial element.
     *
     * @param e                 an initial element to apply to the fold
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    abstract fun <B> foldRight(e: B, f: (A, B) -> B): B

    /**
     * Folds the tree to the left with the given function.
     *
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    abstract fun reduceLeft(f: (A) -> (A) -> A): A

    /**
     * Folds the tree to the left with the given function.
     *
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    abstract fun reduceLeft(f: (A, A) -> A): A

    /**
     * Folds the tree to the right with the given function.
     *
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    abstract fun reduceRight(f: (A) -> (A) -> A): A

    /**
     * Folds the tree to the right with the given function.
     *
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    abstract fun reduceRight(f: (A, A) -> A): A

    /**
     * Maps the given function across this tree, measuring with the given Measured instance.
     *
     * @param f                 a function to map across the values of this tree
     * @param measured          a measuring with which to annotate the tree
     * @return                  a new tree with the same structure as this tree, with each element transformed by the given function,
     *                              and nodes annotated according to the given measuring
     */
    abstract fun <B> map(measured: Measured<V, B>, f: (A) -> B): FingerTree<V, B>

    /**
     * Returns the sum of this tree's annotations.
     *
     * @return                  the sum of this tree's annotations
     */
    abstract fun measure(): V

    /**
     * Adds the given element to this tree as the first element (synonym for addLeft).
     *
     * @param a                 the element to add to the front of this tree
     * @return                  a new tree with the given element at the front
     */
    abstract fun cons(b: A): FingerTree<V, A>

    /**
     * Adds the given element to this tree as the last element (synonym for addRight).
     *
     * @param a                 the element to add to the end of this tree
     * @return                  a new tree with the given element at the end
     */
    abstract fun snoc(b: A): FingerTree<V, A>

    /**
     * Appends one finger tree to another.
     *
     * @param tree              a finger tree to append to this one
     * @return                  a new finger tree which is a concatenation of this tree and the given tree
     */
    abstract fun append(tree: FingerTree<V, A>): FingerTree<V, A>

    /**
     * Add an element to the left end of a sequence (synonym for cons).
     *
     * @param a                 element to be added
     * @return                  new finger tree with the additional element
     */
    fun addLeft(a: A): FingerTree<V, A> = this.cons(a)

    /**
     * Add an element to the right end of a sequence (synonym for snoc).
     *
     * @param a                 element to be added
     * @return                  new finger tree with the additional element
     */
    fun addRight(a: A): FingerTree<V, A> = this.snoc(a)

    /**
     * Concatenate this finger tree with the given finger tree.
     *
     * @param tree              the trailing finger tree
     * @return                  merged trees
     */
    fun concatenate(tree: FingerTree<V, A>): FingerTree<V, A> {
        fun appendTree(tree1: FingerTree<V, A>, tree2: FingerTree<V, A>): FingerTree<V, A> {
            return when (tree1) {
                is Empty -> tree2
                is Single -> {
                    when (tree2) {
                        is Empty -> tree1
                        is Single -> tree2.addLeft(tree1.a)
                        is Deep -> tree2.addLeft(tree1.a)
                    }
                }
                is Deep -> {
                    when (tree2) {
                        is Empty -> tree1
                        is Single -> tree1.addRight(tree2.a)
                        is Deep -> {
                            val mk: MakeTree<V, A> = MakeTree(measured)
                            mk.deep(tree1.prefix, DeepF.addDigits0(measured, tree1.middle, tree1.suffix, tree2.prefix, tree2.middle), tree2.suffix)
                        }
                    }
                }
            }
        }

        return appendTree(this, tree)
    }   // concatenate

    /**
     * Split a sequence at a point where the predicate on the accumulated
     *   measure changes from false to true.
     *
     * @param predicate         the condition
     * @return                  pair of finger trees
     */
    fun split(predicate: (V) -> Boolean): Pair<FingerTree<V, A>, FingerTree<V, A>> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return when (this) {
            is Empty -> Pair(mk.empty(), mk.empty())
            is Single -> {
                if (predicate(this.measure())) {
                    val split: Split<FingerTree<V, A>, A> = FingerTreeF.splitTree(predicate, measured.empty(), this, measured)
                    Pair(split.t1, split.t2.addLeft(split.a))
                } else
                    Pair(this, mk.empty())
            }
            is Deep -> {
                if (predicate(this.measure())) {
                    val split: Split<FingerTree<V, A>, A> = FingerTreeF.splitTree(predicate, measured.empty(), this, measured)
                    Pair(split.t1, split.t2.addLeft(split.a))
                } else
                    Pair(this, mk.empty())
            }
        }
    }   // split

    /**
     * Take the elements until the condition is met.
     *
     * @param predicate         the condition
     * @return                  finger tree
     */
    fun takeUntil(predicate: (V) -> Boolean): FingerTree<V, A> = this.split(predicate).first

    /**
     * Remove the elements until the condition is met.
     *
     * @param predicate         the condition
     * @return                  finger tree
     */
    fun dropUntil(predicate: (V) -> Boolean): FingerTree<V, A> = this.split(predicate).second

}   // FingerTree



/**
 * The empty tree.
 *
 * @param V                     the monoidal type with which to annotate nodes
 * @param A                     the type of the tree's elements
 */
class Empty<V, A> internal constructor(measured: Measured<V, A>) : FingerTree<V, A>(measured) {

    override fun toString(): String = "Empty(${measured}, ${this.measure()})"

    /**
     * Indicates whether this tree is empty.
     *
     * @return                  true if this tree is the empty tree, otherwise false
     */
    override fun isEmpty(): Boolean = true

    /**
     * Folds the tree to the left with the given function and the given initial element.
     *
     * @param e                 an initial element to apply to the fold
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the left
     */
    override fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B = e

    /**
     * Folds the tree to the left with the given function and the given initial element.
     *
     * @param e                 an initial element to apply to the fold
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the left
     */
    override fun <B> foldLeft(e: B, f: (B, A) -> B): B = this.foldLeft(e, FunctionF.C(f))

    /**
     * Folds the tree to the right with the given function and the given initial element.
     *
     * @param e                 an initial element to apply to the fold
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    override fun <B> foldRight(e: B, f: (A) -> (B) -> B): B = e

    /**
     * Folds the tree to the right with the given function and the given initial element.
     *
     * @param e                 an initial element to apply to the fold
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    override fun <B> foldRight(e: B, f: (A, B) -> B): B = this.foldRight(e, FunctionF.C(f))

    /**
     * Folds the tree to the left with the given function.
     *
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    override fun reduceLeft(f: (A) -> (A) -> A): A = throw FingerTreeException("reduceLeft: empty tree")

    /**
     * Folds the tree to the left with the given function.
     *
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    override fun reduceLeft(f: (A, A) -> A): A = this.reduceLeft(FunctionF.C(f))

    /**
     * Folds the tree to the right with the given function.
     *
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    override fun reduceRight(f: (A) -> (A) -> A): A = throw FingerTreeException("reduceRight: empty tree")

    /**
     * Folds the tree to the right with the given function.
     *
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    override fun reduceRight(f: (A, A) -> A): A = this.reduceRight(FunctionF.C(f))

    /**
     * Maps the given function across this tree, measuring with the given Measured instance.
     *
     * @param f                 a function to map across the values of this tree
     * @param measured          a measuring with which to annotate the tree
     * @return                  a new tree with the same structure as this tree, with each element transformed by the given function,
     *                              and nodes annotated according to the given measuring
     */
    override fun <B> map(measured: Measured<V, B>, f: (A) -> B): FingerTree<V, B> {
        val mk: MakeTree<V, B> = MakeTree(measured)
        return mk.empty()
    }

    /**
     * Returns the sum of this tree's annotations.
     *
     * @return                  the sum of this tree's annotations
     */
    override fun measure(): V = measured.empty()

    /**
     * Adds the given element to this tree as the first element (synonym for addLeft).
     *
     * @param a                 the element to add to the front of this tree
     * @return                  a new tree with the given element at the front
     */
    override fun cons(b: A): FingerTree<V, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return mk.single(b)
    }

    /**
     * Adds the given element to this tree as the last element (synonym for addRight).
     *
     * @param a                 the element to add to the end of this tree
     * @return                  a new tree with the given element at the end
     */
    override fun snoc(b: A): FingerTree<V, A> = this.cons(b)

    /**
     * Appends one finger tree to another.
     *
     * @param tree              a finger tree to append to this one
     * @return                  a new finger tree which is a concatenation of this tree and the given tree
     */
    override fun append(tree: FingerTree<V, A>): FingerTree<V, A> = tree

}   // Empty



/**
 * A tree with a single element.
 *
 * @param V                     the monoidal type with which to annotate nodes
 * @param A                     the type of the tree's elements
 */
class Single<V, A> internal constructor(measured: Measured<V, A>, val a: A) : FingerTree<V, A>(measured) {

    override fun toString(): String = "Single(${measured}, ${this.measure()}, ${a})"

    /**
     * Indicates whether this tree is single.
     *
     * @return                  true if this tree is the single tree, otherwise false
     */
    override fun isSingle(): Boolean = true

    /**
     * Folds the tree to the left with the given function and the given initial element.
     *
     * @param e                 an initial element to apply to the fold
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the left
     */
    override fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B = f(e)(a)

    /**
     * Folds the tree to the left with the given function and the given initial element.
     *
     * @param e                 an initial element to apply to the fold
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the left
     */
    override fun <B> foldLeft(e: B, f: (B, A) -> B): B = this.foldLeft(e, FunctionF.C(f))

    /**
     * Folds the tree to the right with the given function and the given initial element.
     *
     * @param e                 an initial element to apply to the fold
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    override fun <B> foldRight(e: B, f: (A) -> (B) -> B): B = f(a)(e)

    /**
     * Folds the tree to the right with the given function and the given initial element.
     *
     * @param e                 an initial element to apply to the fold
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    override fun <B> foldRight(e: B, f: (A, B) -> B): B = this.foldRight(e, FunctionF.C(f))

    /**
     * Folds the tree to the left with the given function.
     *
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    override fun reduceLeft(f: (A) -> (A) -> A): A = a

    /**
     * Folds the tree to the left with the given function.
     *
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    override fun reduceLeft(f: (A, A) -> A): A = this.reduceLeft(FunctionF.C(f))

    /**
     * Folds the tree to the right with the given function.
     *
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    override fun reduceRight(f: (A) -> (A) -> A): A = a

    /**
     * Folds the tree to the right with the given function.
     *
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    override fun reduceRight(f: (A, A) -> A): A = this.reduceRight(FunctionF.C(f))

    /**
     * Maps the given function across this tree, measuring with the given Measured instance.
     *
     * @param f                 a function to map across the values of this tree
     * @param measured          a measuring with which to annotate the tree
     * @return                  a new tree with the same structure as this tree, with each element transformed by the given function,
     *                              and nodes annotated according to the given measuring
     */
    override fun <B> map(measured: Measured<V, B>, f: (A) -> B): FingerTree<V, B> {
        val mk: MakeTree<V, B> = MakeTree(measured)
        return mk.single(f(a))
    }

    /**
     * Returns the sum of this tree's annotations.
     *
     * @return                  the sum of this tree's annotations
     */
    override fun measure(): V = v

    /**
     * Adds the given element to this tree as the first element (synonym for addLeft).
     *
     * @param a                 the element to add to the front of this tree
     * @return                  a new tree with the given element at the front
     */
    override fun cons(b: A): FingerTree<V, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return DeepF.deep(measured, mk.one(b), Empty(measured.nodeMeasured()), mk.one(a))
    }

    /**
     * Adds the given element to this tree as the last element (synonym for addRight).
     *
     * @param a                 the element to add to the end of this tree
     * @return                  a new tree with the given element at the end
     */
    override fun snoc(b: A): FingerTree<V, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        val mkn: MakeTree<V, Node<V, A>> = MakeTree(measured.nodeMeasured())
        return mk.deep(mk.one(a), mkn.empty(), mk.one(b))
    }

    /**
     * Appends one finger tree to another.
     *
     * @param tree              a finger tree to append to this one
     * @return                  a new finger tree which is a concatenation of this tree and the given tree
     */
    override fun append(tree: FingerTree<V, A>): FingerTree<V, A> = tree.cons(a)



// ---------- properties ----------------------------------

    val v: V = measured.measure(a)

}   // Single



/**
 * A finger tree with 1-4-digits on the left and right, and a
 *   finger tree of 2-3-nodes in the middle.
 *
 * @param V                     the monoidal type with which to annotate nodes
 * @param A                     the type of the tree's elements
 */
class Deep<V, A> internal constructor(measured: Measured<V, A>, val v: V, val prefix: Digit<V, A>, val middle: FingerTree<V, Node<V, A>>, val suffix: Digit<V, A>) : FingerTree<V, A>(measured) {

    override fun toString(): String = "Deep(${measured}, ${v}, ${prefix}, ${middle}, ${suffix})"

    /**
     * Indicates whether this tree is deep.
     *
     * @return                  true if this tree is deep, otherwise false
     */
    override fun isDeep(): Boolean = true

    /**
     * Folds the tree to the left with the given function and the given initial element.
     *
     * @param e                 an initial element to apply to the fold
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the left
     */
    override fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B =
            suffix.foldLeft(middle.foldLeft(prefix.foldLeft(e, f), NodeF.foldLeft(f)), f)

    /**
     * Folds the tree to the left with the given function and the given initial element.
     *
     * @param e                 an initial element to apply to the fold
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the left
     */
    override fun <B> foldLeft(e: B, f: (B, A) -> B): B = this.foldLeft(e, FunctionF.C(f))

    /**
     * Folds the tree to the right with the given function and the given initial element.
     *
     * @param e                 an initial element to apply to the fold
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    override fun <B> foldRight(e: B, f: (A) -> (B) -> B): B =
            prefix.foldRight(middle.foldRight(suffix.foldRight(e, f), FunctionF.flip(NodeF.foldRight(f))), f)

    /**
     * Folds the tree to the right with the given function and the given initial element.
     *
     * @param e                 an initial element to apply to the fold
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    override fun <B> foldRight(e: B, f: (A, B) -> B): B = this.foldRight(e, FunctionF.C(f))

    /**
     * Folds the tree to the left with the given function.
     *
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    override fun reduceLeft(f: (A) -> (A) -> A): A =
            suffix.foldLeft(middle.foldLeft(prefix.reduceLeft(f), NodeF.foldLeft(f)), f)

    /**
     * Folds the tree to the left with the given function.
     *
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    override fun reduceLeft(f: (A, A) -> A): A = this.reduceLeft(FunctionF.C(f))

    /**
     * Folds the tree to the right with the given function.
     *
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    override fun reduceRight(f: (A) -> (A) -> A): A =
            prefix.foldRight(middle.foldRight(suffix.reduceRight(f), FunctionF.flip(NodeF.foldRight(f))), f)

    /**
     * Folds the tree to the right with the given function.
     *
     * @param f                 a function with which to fold the tree
     * @return                  a reduction of this tree by applying the given function, associating to the right
     */
    override fun reduceRight(f: (A, A) -> A): A = this.reduceRight(FunctionF.C(f))

    /**
     * Maps the given function across this tree, measuring with the given Measured instance.
     *
     * @param f                 a function to map across the values of this tree
     * @param measured          a measuring with which to annotate the tree
     * @return                  a new tree with the same structure as this tree, with each element transformed by the given function,
     *                              and nodes annotated according to the given measuring
     */
    override fun <B> map(measured: Measured<V, B>, f: (A) -> B): FingerTree<V, B> {
        val mk: MakeTree<V, B> = MakeTree(measured)
        return mk.deep(prefix.map(f, measured), middle.map(measured.nodeMeasured(), NodeF.liftM(f, measured)), suffix.map(f, measured))
    }

    /**
     * Returns the sum of this tree's annotations.
     *
     * @return                  the sum of this tree's annotations
     */
    override fun measure(): V = v

    /**
     * Adds the given element to this tree as the first element (synonym for addLeft).
     *
     * @param a                 the element to add to the front of this tree
     * @return                  a new tree with the given element at the front
     */
    override fun cons(b: A): FingerTree<V, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return when (prefix) {
            is One -> mk.deep(mk.two(b, prefix.a), middle, suffix)
            is Two -> mk.deep(mk.three(b, prefix.a1, prefix.a2), middle, suffix)
            is Three -> mk.deep(mk.four(b, prefix.a1, prefix.a2, prefix.a3), middle, suffix)
            is Four -> mk.deep(mk.two(b, prefix.a1), middle.cons(mk.node3(prefix.a2, prefix.a3, prefix.a4)), suffix)
        }
    }   // cons

    /**
     * Adds the given element to this tree as the last element (synonym for addRight).
     *
     * @param a                 the element to add to the end of this tree
     * @return                  a new tree with the given element at the end
     */
    override fun snoc(b: A): FingerTree<V, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return when (suffix) {
            is One -> mk.deep(prefix, middle, mk.two(suffix.a, b))
            is Two -> mk.deep(prefix, middle, mk.three(suffix.a1, suffix.a2, b))
            is Three -> mk.deep(prefix, middle, mk.four(suffix.a1, suffix.a2, suffix.a3, b))
            is Four -> mk.deep(prefix, middle.snoc(mk.node3(suffix.a1, suffix.a2, suffix.a3)), mk.two(suffix.a4, b))
        }
    }   // snoc

    /**
     * Appends one finger tree to another.
     *
     * @param tree              a finger tree to append to this one
     * @return                  a new finger tree which is a concatenation of this tree and the given tree
     */
    override fun append(tree: FingerTree<V, A>): FingerTree<V, A> {
        val mk: MakeTree<V, A> = MakeTree(measured)
        return when (tree) {
            is Empty -> tree
            is Single -> tree.snoc(tree.a)
            is Deep -> mk.deep(prefix, DeepF.addDigits0(measured, middle, suffix, tree.prefix, tree.middle), tree.suffix)
        }
    }   // append

}   // Deep
