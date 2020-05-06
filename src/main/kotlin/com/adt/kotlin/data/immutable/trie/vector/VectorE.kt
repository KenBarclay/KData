package com.adt.kotlin.data.immutable.trie.vector

/**
 * The Vector is a persistent version of the classical vector data structure.
 *   The structure supports efficient, non-destructive operations. It is a port
 *   of the Haskell port from Clojure.
 *
 * The algebraic data type declaration is:
 *
 * datatype Node[A] = EmptyNode[A]
 *                  | RootNode[A] of Int * Int * Int * Int * List[A] * Array[Node[A]]
 *                  | InternalNode[A] of Array[Node[A]]
 *                  | DataNode[A] of Array[A]
 *
 * @param A                     the type of elements in the vector
 *
 * @author	                    Ken Barclay
 * @since                       December 2014
 */

import com.adt.kotlin.data.immutable.trie.vector.node.Node
import com.adt.kotlin.data.immutable.trie.vector.node.NodeF
import com.adt.kotlin.data.immutable.trie.vector.VectorF.liftA2
import com.adt.kotlin.data.immutable.trie.vector.VectorF.liftA3

import com.adt.kotlin.data.immutable.list.*
import com.adt.kotlin.data.immutable.list.List

import com.adt.kotlin.hkfp.typeclass.Monoid



// ---------- special vectors -----------------------------

/**
 * 'and' returns the conjunction of a container of booleans.
 *
 * Examples:
 *   [true, true, true, true].and() = true
 *   [true, true, false, true].and() = false
 *
 * @return                      true, if all the elements are true
 */
fun VectorIF<Boolean>.and(): Boolean =
        this.forAll{bool -> (bool == true)}

/**
 * 'and' returns the disjunction of a container of booleans.
 *
 * Examples:
 *   [false, false, true, false].or() = true
 *   [false, false, false, false].or() = false
 *
 * @return                      true, if any of the elements is true
 */
fun VectorIF<Boolean>.or(): Boolean =
        this.thereExists{bool -> (bool == true)}

/**
 * The sum function computes the sum of the integers in a vector.
 *
 * Examples:
 *   [1, 2, 3, 4].sum() = 10
 *   [].sum() = 0
 *
 * @return                      the sum of all the elements
 */
fun VectorIF<Int>.sum(): Int =
        this.foldLeft(0){n, m -> n + m}

/**
 * The sum function computes the sum of the doubles in a vector.
 *
 * Examples:
 *   [1.0, 2.0, 3.0, 4.0].sum() = 10.0
 *   [].sum() = 0.0
 *
 * @return                      the sum of all the elements
 */
fun VectorIF<Double>.sum(): Double =
        this.foldLeft(0.0){x, y -> x + y}

/**
 * The product function computes the product of the integers in a vector.
 *
 * Examples:
 *   [1, 2, 3, 4].product() = 24
 *   [].product() = 1
 *
 * @return                      the product of all the elements
 */
fun VectorIF<Int>.product(): Int =
        this.foldLeft(1){n, m -> n * m}

/**
 * The product function computes the product of the doubles in a vector.
 *
 * Examples:
 *   [1.0, 2.0, 3.0, 4.0].product() = 24.0
 *   [].product() = 1.0
 *
 * @return                      the product of all the elements
 */
fun VectorIF<Double>.product(): Double =
        this.foldLeft(1.0){x, y -> x * y}




// Functor extension functions:

/**
 * Apply the function to the content(s) of the Vector context.
 * Function map applies the function parameter to each item in this vector, delivering
 *   a new vector. The result vector has the same size as this vector.
 *
 * Examples:
 *   ////[1, 2, 3, 4].fmap{n -> n + 1} = [2, 3, 4, 5]
 *   ////[].fmap{n -> n + 1} = []
 */
fun <A, B> VectorIF<A>.fmap(f: (A) -> B): VectorIF<B> = this.map(f)

/**
 * An infix symbol for fmap.
 */
infix fun <A, B> ((A) -> B).dollar(v: VectorIF<A>): VectorIF<B> = v.fmap(this)

/**
 * Replace all locations in the input with the given value.
 *
 * Examples:
 *   ////[1, 2, 3, 4].replaceAll(5) = [5, 5, 5, 5]
 *   ////[].replaceAll(5) = []
 */
fun <A, B> VectorIF<A>.replaceAll(b: B): VectorIF<B> = this.fmap{_ -> b}

/**
 * Distribute the List<(A, B)> over the pair to get (List<A>, List<B>).
 *
 * Examples:
 *   [(1, 2), (3, 4), (5, 6)].distribute() = ([1, 3, 5], [2, 4, 6])
 *   [].distribute() = ([], [])
 */
fun <A, B> VectorIF<Pair<A, B>>.distribute(): Pair<VectorIF<A>, VectorIF<B>> {
    fun recDistribute(node: Node<Pair<A, B>>, accs: Pair<Node<A>, Node<B>>): Pair<Node<A>, Node<B>> {
        return when(node) {
            is Node.EmptyNode -> accs
            is Node.RootNode -> {
                val axs: Pair<Node<A>, Node<B>> = node.tail.foldLeft(accs){acc, pr -> Pair(acc.first.append(pr.first), acc.second.append(pr.second))}
                node.vecPtrs.fold(axs){acc, nod -> recDistribute(nod, acc)}
            }
            is Node.InternalNode -> node.vecPtrs.fold(accs){ acc, nod -> recDistribute(nod, acc)}
            is Node.DataNode -> node.data.fold(accs){ acc, wrap -> Pair(acc.first.append(wrap.wrapped.first), acc.second.append(wrap.wrapped.second))}
        }
    }   // recDistribute

    val thisRoot: Node<Pair<A, B>> = this.root
    val pair: Pair<Node<A>, Node<B>> = recDistribute(thisRoot, Pair(NodeF.empty(), NodeF.empty()))
    return Pair(Vector(pair.first), Vector(pair.second))
}   // distribute

/**
 * Inject a to the left of the b's in this vector.
 */
fun <A, B> VectorIF<B>.injectLeft(a: A): VectorIF<Pair<A, B>> = this.fmap{b: B -> Pair(a, b)}

/**
 * Inject b to the right of the a's in this vector.
 */
fun <A, B> VectorIF<A>.injectRight(b: B): VectorIF<Pair<A, B>> = this.fmap{a: A -> Pair(a, b)}

/**
 * Twin all the a's in this vector with itself.
 */
fun <A> VectorIF<A>.pair(): VectorIF<Pair<A, A>> = this.fmap{a: A -> Pair(a, a)}

/**
 * Pair all the a's in this vector with the result of the function application.
 */
fun <A, B> VectorIF<A>.product(f: (A) -> B): VectorIF<Pair<A, B>> = this.fmap{a: A -> Pair(a, f(a))}



// Applicative extension functions:

/**
 * Apply the function wrapped in a context to the content of the
 *   value also wrapped in a matching context.
 *
 * Examples:
 *   [1, 2, 3, 4].ap([{n -> (n % 2 == 0)}]) = [false, true, false, true]
 *   [].ap([{n -> (n % 2 == 0)}]) = []
 */
fun <A, B> VectorIF<A>.ap(f: VectorIF<(A) -> B>): VectorIF<B> {
    val thisList: List<A> = this.toList()
    val fList: List<(A) -> B> = f.toList()
    val bList: List<B> = thisList.ap(fList)
    return VectorF.from(bList)
}   // ap

/**
 * An infix symbol for ap.
 */
infix fun <A, B> VectorIF<(A) -> B>.apply(v: VectorIF<A>): VectorIF<B> = v.ap(this)

/**
 * Sequence actions, discarding the value of the first argument.
 *
 * Examples:
 *   [0, 1, 2, 3].sDF(["Ken", "John", "Jessie", "Irene"]) = ["Ken", "Ken", "Ken", "Ken", "John", "John", "John", "John", "Jessie", "Jessie", "Jessie", "Jessie", "Irene", "Irene", "Irene", "Irene"]
 *   [5].sDF(["Ken", "John", "Jessie", "Irene"]) = ["Ken", "John", "Jessie", "Irene"]
 *   [].sDF(["Ken", "John", "Jessie", "Irene"]) = []
 */
fun <A, B> VectorIF<A>.sDF(vb: VectorIF<B>): VectorIF<B> {
    val thisList: List<A> = this.toList()
    val vbList: List<B> = vb.toList()
    val list: List<B> = thisList.sDF(vbList)
    return VectorF.from(list)
}   // sDF

/**
 * Sequence actions, discarding the value of the second argument.
 *
 * Examples:
 *   [0, 1, 2, 3].sDS(["Ken", "John", "Jessie", "Irene"]) = [0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3]
 *   [0, 1, 2, 3].sDS(["Ken"]) = [0, 1, 2, 3]
 *   [].sDS(["Ken"]) = []
 */
fun <A, B> VectorIF<A>.sDS(vb: VectorIF<B>): VectorIF<A> {
    val thisList: List<A> = this.toList()
    val vbList: List<B> = vb.toList()
    val list: List<A> = thisList.sDS(vbList)
    return VectorF.from(list)
}   // sDS

/**
 * The product of two applicatives.
 *
 * ["Ken", "John", "Jessie", "Irene"].product2([1, 2, 3]) = [("Ken", 1), ("Ken", 2), ("Ken", 3), ("John", 1), ...]
 * ["Ken", "John", "Jessie", "Irene"].product2([]) = []
 * [].product2([1, 2, 3]) = []
 */
fun <A, B> VectorIF<A>.product2(vb: VectorIF<B>): VectorIF<Pair<A, B>> {
    val thisList: List<A> = this.toList()
    val vbList: List<B> = vb.toList()
    val list: List<Pair<A, B>> = thisList.product2(vbList)
    return VectorF.from(list)
}   // product2

/**
 * The product of three applicatives.
 *
 * Examples:
 *   ["Ken", "John"].product3([1, 2], [false, true]) = [("Ken", 1, false), ("Ken", 1, true), ("John", 1, false), ...]
 *   ["Ken", "John"].product3([], [false, true]) = []
 *   ["Ken", "John"].product3([1, 2], []) = []
 *   [].product3([1, 2], [false, true]) = []
 */
fun <A, B, C> VectorIF<A>.product3(vb: VectorIF<B>, vc: VectorIF<C>): VectorIF<Triple<A, B, C>> {
    val thisList: List<A> = this.toList()
    val vbList: List<B> = vb.toList()
    val vcList: List<C> = vc.toList()
    val list: List<Triple<A, B, C>> = thisList.product3(vbList, vcList)
    return VectorF.from(list)
}   // product3

/**
 * fmap2 is a binary version of fmap.
 *
 * Examples:
 *   [1, 2, 3, 4].fmap2([5, 6, 7]){m -> {n -> m + n}} = [6, 7, 8, 7, 8, 9, 8, 9, 10, 9, 10, 11]
 *   [1, 2, 3, 4].fmap2([]){m -> {n -> m + n}} = []
 *   [].fmap2([5, 6, 7]){m -> {n -> m + n}} = []
 */
fun <A, B, C> VectorIF<A>.fmap2(vb: VectorIF<B>, f: (A) -> (B) -> C): VectorIF<C> =
    liftA2(f)(this)(vb)

/**
 * fmap3 is a ternary version of fmap.
 *
 * Examples:
 *   [1, 2].fmap3([3, 4], [5, 6]){m -> {n -> {o -> m + n + o}} = [9, 10, 10, 11, 10, 11, 11, 12]
 *   [1, 2].fmap3([], [5, 6]){m -> {n -> {o -> m + n + o}} = []
 *   [1, 2].fmap3([3, 4], []){m -> {n -> {o -> m + n + o}} = []
 *   [].fmap3([3, 4], [5, 6]){m -> {n -> {o -> m + n + o}} = []
 */
fun <A, B, C, D> VectorIF<A>.fmap3(vb: VectorIF<B>, vc: VectorIF<C>, f: (A) -> (B) -> (C) -> D): VectorIF<D> =
    liftA3(f)(this)(vb)(vc)

/**
 * ap2 is a binary version of ap, defined in terms of ap.
 *
 * Examples:
 *   [1, 2].ap2([3, 4], [{m -> {n -> m + n}}, {m -> {n -> m * n}}]) = [4, 5, 5, 6, 3, 4, 6, 8]
 *   [1, 2].ap2([], [{m -> {n -> m + n}}, {m -> {n -> m * n}}]) = []
 *   [].ap2([3, 4], [{m -> {n -> m + n}}, {m -> {n -> m * n}}]) = []
 */
fun <A, B, C> VectorIF<A>.ap2(vb: VectorIF<B>, f: VectorIF<(A) -> (B) -> C>): VectorIF<C> =
    vb.ap(this.ap(f))

/**
 * ap3 is a ternary version of ap, defined in terms of ap.
 *
 * Examples:
 *   [1, 2].ap3([3, 4], [5, 6], [{m -> {n -> {o -> m + n + o}}}]) = [9, 10, 10, 11, 10, 11, 11, 12]
 *   [1, 2].ap3([], [5, 6], [{m -> {n -> {o -> m + n + o}}}]) = []
 *   [1, 2].ap3([3, 4], [], [{m -> {n -> {o -> m + n + o}}}]) = []
 *   [].ap3([3, 4], [5, 6], [{m -> {n -> {o -> m + n + o}}}]) = []
 */
fun <A, B, C, D> VectorIF<A>.ap3(vb: VectorIF<B>, vc: VectorIF<C>, f: VectorIF<(A) -> (B) -> (C) -> D>): VectorIF<D> =
    vc.ap(vb.ap(this.ap(f)))



// Monad extension functions:

/**
 * Sequentially compose two actions, passing any value produced by the first
 *   as an argument to the second.
 *
 * Examples:
 *   [0, 1, 2, 3].bind{n -> [n, n + 1]} = [0, 1, 1, 2, 2, 3, 3, 4]
 *   [].bind{n -> [n, n + 1]} = []
 */
fun <A, B> VectorIF<A>.bind(f: (A) -> VectorIF<B>): VectorIF<B> {
    val thisList: List<A> = this.toList()
    val g: (A) -> List<B> = {a -> f(a).toList()}
    val bList: List<B> = thisList.bind(g)
    return VectorF.from(bList)
}   // bind

fun <A, B> VectorIF<A>.flatMap(f: (A) -> VectorIF<B>): VectorIF<B> = this.bind(f)

/**
 * Sequentially compose two actions, discarding any value produced by the first,
 *   like sequencing operators (such as the semicolon) in imperative languages.
 *
 * Examples:
 *   [0, 1, 2, 3].then(["Ken", "John"]) = ["Ken", "John", "Ken", "John", "Ken", "John", "Ken", "John"]
 *   [].then(["Ken", "John"]) = []
 */
fun <A, B> VectorIF<A>.then(vb: VectorIF<B>): VectorIF<B> = this.bind{_ -> vb}



// Foldable extension functions:

/**
 * Combine the elements of a structure using a monoid.
 *
 * Examples:
 *   [1, 2, 3, 4].fold(intAddMonoid) = 10
 *   [].fold(intAddMonoid) = 0
 */
fun <A> VectorIF<A>.fold(md: Monoid<A>): A =
    this.foldLeft(md.empty){b -> {a -> md.combine(b, a)}}

/**
 * Map each element of the structure to a monoid, and combine the results.
 *
 * Examples:
 *   [1, 2, 3, 4].foldMap(intAddMonoid){n -> n + 1} = 14
 *   [].foldMap(intAddMonoid){n -> n + 1} = 0
 */
fun <A, B> VectorIF<A>.foldMap(md: Monoid<B>, f: (A) -> B): B =
    this.foldLeft(md.empty){b -> {a -> md.combine(b, f(a))}}
