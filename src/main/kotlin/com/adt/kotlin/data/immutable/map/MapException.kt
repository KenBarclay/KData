package com.adt.kotlin.data.immutable.map

/**
 * A class hierarchy defining an immutable map collection. The algebraic data
 *   type declaration is:
 *
 * datatype Map[K, V] = Tip
 *                    | Bin of K * V * Map[K, V] * Map[K, V]
 *
 * Maps are implemented as size balanced binary trees. This implementation
 *   mirrors the Haskell implementation in Data.Map that, in turn, is based
 *   on an efficient balanced binary tree referenced in the sources.
 *
 * This code duplicates much of the implementation for the immutable Set.
 *   Both are based on sized balanced binary trees and a generic Tree should
 *   be developed for both.
 *
 * The type denoted by Map[K, V] is a map of key/value pairs.
 *
 * The Map class is defined generically in terms of the type parameters K and V.
 *
 * @param K                     the type of keys in the map
 * @param V                     the type of values in the map
 *
 * @author	                    Ken Barclay
 * @since                       October 2019
 */



class MapException(message: String) : Exception(message)
