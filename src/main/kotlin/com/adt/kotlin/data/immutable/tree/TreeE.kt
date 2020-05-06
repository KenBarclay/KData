package com.adt.kotlin.data.immutable.tree

import com.adt.kotlin.data.immutable.list.fmap

/**
 * A class hierarchy defining an immutable tree collection. The algebraic data
 *   type declaration is:
 *
 * datatype Tree[A] = Tip
 *                 | Bin of A * Tree[A] * Tree[A]
 *
 * Trees are implemented as size balanced binary trees. This implementation
 *   mirrors the Haskell implementation in Data.Set that, in turn, is based
 *   on an efficient balanced binary tree referenced in the sources.
 *
 * The Tree class is defined generically in terms of the type parameter A.
 *
 * @author	                    Ken Barclay
 * @since                       October 2012
 */




// Functor extension functions:

/**
 * Apply the function to the content(s) of the Tree context.
 *
 * Examples:
 *
 */
fun <A : Comparable<A>, B : Comparable<B>> Tree<A>.fmap(f: (A) -> B): Tree<B> = this.map(f)

/**
 * An infix symbol for fmap.
 */
infix fun <A : Comparable<A>, B : Comparable<B>> ((A) -> B).dollar(v: Tree<A>): Tree<B> = v.fmap(this)

/**
 * Replace all locations in the input with the given value.
 *
 * Examples:
 *   [1, 2, 3, 4].replaceAll(5) = [5, 5, 5, 5]
 *   [].replaceAll(5) = []
 */
fun <A : Comparable<A>, B : Comparable<B>> Tree<A>.replaceAll(b: B): Tree<B> = this.fmap{_ -> b}
