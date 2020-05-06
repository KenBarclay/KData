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

import com.adt.kotlin.data.immutable.rbtree.RedBlackTree.EmptyTree
import com.adt.kotlin.data.immutable.rbtree.RedBlackTree.RBTree



abstract class TreeIteratorAB<K : Comparable<K>, V, R>(val tree: RedBlackTree<K, V>) : Iterator<R> {

    abstract protected fun nextResult(tree: RedBlackTree<K, V>): R

    override fun hasNext(): Boolean = (nxt !is EmptyTree)

    override fun next(): R {
        return when(nxt) {
            is EmptyTree -> throw RedBlackTreeException("TreeIterator.next: next on empty iterator")
            is RBTree -> {
                val nxtTree: RBTree<K, V> = nxt as RBTree<K, V>
                nxt = findNext(nxtTree.right)
                nextResult(nxtTree)
            }
        }
    }



// ---------- implementation -----------------------------

    private fun log2(d: Double): Double = Math.log10(d)/Math.log10(2.0)

    private fun findNext(tree: RedBlackTree<K, V>): RedBlackTree<K, V> {
        return when(tree) {
            is EmptyTree -> popPath()
            is RBTree -> {
                if (tree.left is EmptyTree)
                    tree
                else {
                    pushPath(tree)
                    findNext(tree.left)
                }
            }
        }
    }

    private fun pushPath(tree: RedBlackTree<K, V>) {
        try {
            path[index] = tree
            index += 1
        } catch(ex: ArrayIndexOutOfBoundsException) {
            throw RedBlackTreeException("TreeIterator.pushPath: index out of bounds: ${index}")
        }
    }

    private fun popPath(): RedBlackTree<K, V> {
        return if (index == 0)
            EMPTYTREE
        else {
            index -= 1
            path[index]
        }
    }



// ---------- properties ----------------------------------

    private val EMPTYTREE: EmptyTree<K, V> = EmptyTree()
    private val pathSize: Int = Math.ceil(2 * log2(tree.count + 2.0) - 2).toInt()
    private var path: Array<RedBlackTree<K, V>> = Array(pathSize, {_: Int -> EMPTYTREE})
    private var index: Int = 0
    private var nxt: RedBlackTree<K, V> = findNext(tree)

}



class KeysIterator<K : Comparable<K>, V>(tree: RedBlackTree<K, V>) : TreeIteratorAB<K, V, K>(tree) {

    override protected fun nextResult(tree: RedBlackTree<K, V>): K =
        when(tree) {
            is EmptyTree -> throw RedBlackTreeException("KeysIterator.nextResult: empty tree")
            is RBTree -> tree.key
        }

}



class ValuesIterator<K : Comparable<K>, V>(tree: RedBlackTree<K, V>) : TreeIteratorAB<K, V, V>(tree) {

    override protected fun nextResult(tree: RedBlackTree<K, V>): V =
        when(tree) {
            is EmptyTree -> throw RedBlackTreeException("ValuesIterator.nextResult: empty tree")
            is RBTree -> tree.value
        }

}



class EntriesIterator<K : Comparable<K>, V>(tree: RedBlackTree<K, V>) : TreeIteratorAB<K, V, Pair<K, V>>(tree) {

    override protected fun nextResult(tree: RedBlackTree<K, V>): Pair<K, V> =
        when(tree) {
            is EmptyTree -> throw RedBlackTreeException("EntriesIterator.nextResult: empty tree")
            is RBTree -> Pair(tree.key, tree.value)
        }

}
