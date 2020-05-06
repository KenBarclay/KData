package com.adt.kotlin.data.immutable.rbtree

/**
 * A class hierarchy defining an immutable red-black tree. The algebraic data
 *   type declaration is:
 *
 * datatype RedBlackTree[K, V] = EmptyTree
 *                             | RedTree of K * V * RedBlackTree[K, V] * RedBlackTree[K, V]
 *                             | BlackTree of K * V * RedBlackTree[K, V] * RedBlackTree[K, V]
 *
 * Red-black trees are binary search trees obeying two key invariants:
 *
 *   (1) Any path from a root node to a leaf node contains the same number
 *       of black nodes (balancing condition).
 *
 *   (2) Red nodes always have black children.
 *
 * @param K                     the type of key elements in the tree
 * @param V                     the type of value elements in the tree
 *
 * @author	                    Ken Barclay
 * @since                       April 2017
 */



class RedBlackTreeException(message: String) : Exception(message)
