package com.adt.kotlin.data.immutable.trie.hamt.node

/**
 * The HamtMap is a persistent version of the classical hash table data structure.
 *   The structure supports efficient, non-destructive operations.
 *
 * The algebraic data type declaration is:
 *
 * datatype Node[A, B] = EmptyNode[A, B]
 *                     | LeafNode[A, B] of Int * A * B
 *                     | ArrayNode[A, B] of Int * [Node[A, B]]  where [...] is an array
 *                     | BitmapIndexedNode[A, B] of Int * [Node{A, B]]
 *                     | HashCollisionNode[A, B] of Int * List[Pair[A, B]]
 *
 * This implementation is modelled after the Haskell version described in the talk
 *   Faster persistent data structures through hashing by Johan Tibell at:
 *   https://www.haskell.org/wikiupload/6/65/HIW2011-Talk-Tibell.pdf. The Haskell
 *   code follows the Clojure implementation by Rich Hickey.
 *
 * @param K                     the type of keys in the map
 * @param V                     the type of values in the map
 *
 * @author	                    Ken Barclay
 * @since                       December 2014
 */

import com.adt.kotlin.data.immutable.trie.hamt.MapException

import com.adt.kotlin.data.immutable.option.*
import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.Option.None
import com.adt.kotlin.data.immutable.option.Option.Some
import com.adt.kotlin.data.immutable.option.OptionF

import com.adt.kotlin.data.immutable.list.*
import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.ListF

import kotlin.collections.List as KList



sealed class Node<K : Comparable<K>, V> {



    internal class EmptyNode<K : Comparable<K>, V> : Node<K, V>() {

        override fun toGraph(): String = "Empty"

        /**
         * Is the structure empty.
         *
         * @return                  true if the structure is empty, false otherwise
         */
        override fun isEmpty(): Boolean = true

        /**
         * Is the structure a tip, as represented by an empty node and a leaf node.
         *
         * @return                  true if the structure is a tip, false otherwise
         */
        override fun isTip(): Boolean = true



        // ---------- implementation ------------------------------

        /**
         * Alter this node given the key and the value function.
         *
         * @param shift             the shift amount
         * @param h                 the hash value for the given key
         * @param k                 the key
         * @param f                 function converting the corresponding value
         * @return                  new node; either a LeafNode or an EmptyNode
         */
        override fun alterNode(shift: Int, h: Int, k: K, f: (Option<V>) -> Option<V>): Node<K, V> {
            ////println("EmptyNode.alterNode: shift: ${shift} h: ${h} k: ${k} node: ${this}")
            val leafNodeC: (Int, K) -> (V) -> LeafNode<K, V> = {hh: Int, a: K -> {b: V -> LeafNode(hh, a, b)}}
            return f(OptionF.none()).fold({ EmptyNode() }, leafNodeC(h, k))
        }

        /**
         * Look up the value for the given key in this node. Wrap the value in a Some
         *   if present, otherwise return a None.
         *
         * @param shift             the shift amount
         * @param h                 the hash value for the given key
         * @param k                 the key
         * @return                  the value corresponding to the given key, if present
         */
        override fun lookUpNode(shift: Int, h: Int, k: K): Option<V> = OptionF.none()

        /**
         * Apply the function to the key and its corresponding value as a replacement for the value.
         *
         * @param f                 the transformation function
         * @return                  node of same type with the replaced value
         */
        override fun <W> mapWithKey(f: (K) -> (V) -> W): Node<K, W> = EmptyNode()

        /**
         * Filter out those nodes that match the predicate.
         *
         * @param predicate         match criteria
         * @return                  matching node
         */
        override fun filterWithKey(predicate: (K) -> (V) -> Boolean): Node<K, V> = EmptyNode()

        /**
         * Determine the number of elements in this node.
         *
         * @return                  the number of elements
         */
        override fun numberOfElements(): Int = 0

        /**
         * Deliver a list of the key/value pairs in this node.
         *
         * @return                  the key/value pairs in this node
         */
        override fun toList(): List<Pair<K, V>> = ListF.empty()

    }   // EmptyNode



    internal class LeafNode<K : Comparable<K>, V>(val hash: Int, val key: K, val value: V) : Node<K, V>() {

        override fun toGraph(): String = "Leaf(${key}, ${value})"

        /**
         * Is the structure empty.
         *
         * @return                  true if the structure is empty, false otherwise
         */
        override fun isEmpty(): Boolean = false

        /**
         * Is the structure a tip, as represented by an empty node and a leaf node.
         *
         * @return                  true if the structure is a tip, false otherwise
         */
        override fun isTip(): Boolean = true



        // ---------- implementation ------------------------------

        /**
         * Alter this node given the key and the value function.
         *
         * @param shift             the shift amount
         * @param h                 the hash value for the given key
         * @param k                 the key
         * @param f                 function converting the corresponding value
         * @return                  new node; either EmptyNode, LeafNode, BitmapIndexedNode or HashCollisionNode
         */
        override fun alterNode(shift: Int, h: Int, k: K, f: (Option<V>) -> Option<V>): Node<K, V> {
            ////println("LeafNode.alterNode: shift: ${shift} h: ${h} k: ${k} node: ${this}")
            fun combineNodes(shift: Int, node1: Node<K, V>, node2: Node<K, V>): Node<K, V> {
                //println("combineNodes: shift: ${shift} node1: ${node1} node2: ${node2}")
                if (node1 is LeafNode<K, V> && node2 is LeafNode<K, V>) {
                    val hash1: Int = node1.hash
                    val hash2: Int = node2.hash
                    val subHash1: Int = NodeF.hashFragment(shift, hash1)
                    val subHash2: Int = NodeF.hashFragment(shift, hash2)
                    //println("    combineNodes: hash1: ${hash1} hash2: ${hash2} subHash1: ${subHash1} subHash2: ${subHash2}")
                    val nodeA: Node<K, V> = if (subHash1 < subHash2) node1 else node2
                    val nodeB: Node<K, V> = if (subHash1 < subHash2) node2 else node1
                    //println("    combineNodes: nodeA: ${nodeA} nodeB: ${nodeB}")
                    val bitmapP: Int = NodeF.toBitmap(subHash1) or NodeF.toBitmap(subHash2)
                    val subNodesP: Array<Node<K, V>> = if (subHash1 == subHash2) arrayOf(combineNodes(shift + NodeF.shiftStep, node1, node2)) else arrayOf(nodeA, nodeB)
                    //println("    combineNodes: subNodesP: ${subNodesP.size()} zeroth: ${subNodesP[0]}")
                    if (hash1 == hash2)
                        return HashCollisionNode(hash1, ListF.from(Pair(node2.key, node2.value), Pair(node1.key, node1.value)))
                    else
                        return BitmapIndexedNode(bitmapP, subNodesP)
                } else
                    throw MapException("combineNodes: one or both parameters are not LeafNode")
            }   // combineNodes

            if (k == key) {
                val leafNodeC: (Int, K) -> (V) -> LeafNode<K, V> = {hh: Int, kk: K -> {v: V -> LeafNode(hh, kk, v)}}
                return f(OptionF.some(value)).fold({ EmptyNode() }, leafNodeC(hash, key))
            } else {
                val nodeP: Node<K, V> = EmptyNode<K, V>().alterNode(shift, h, k, f)
                return if (nodeP.isEmpty()) this else combineNodes(shift, this, nodeP)
            }
        }

        /**
         * Look up the value for the given key in this node. Wrap the value in a Some
         *   if present, otherwise return a None.
         *
         * @param shift             the shift amount
         * @param h                 the hash value for the given key
         * @param k                 the key
         * @return                  the value corresponding to the given key, if present
         */
        override fun lookUpNode(shift: Int, h: Int, k: K): Option<V> =
                if (k == key) OptionF.some(value) else OptionF.none()

        /**
         * Apply the function to the key and its corresponding value as a replacement for the value.
         *
         * @param f                 the transformation function
         * @return                  node of same type with the replaced value
         */
        override fun <W> mapWithKey(f: (K) -> (V) -> W): Node<K, W> = LeafNode(hash, key, f(key)(value))

        /**
         * Filter out those nodes that match the predicate.
         *
         * @param predicate         match criteria
         * @return                  matching node
         */
        override fun filterWithKey(predicate: (K) -> (V) -> Boolean): Node<K, V> =
                if (predicate(key)(value)) this else EmptyNode()

        /**
         * Determine the number of elements in this node.
         *
         * @return                  the number of elements
         */
        override fun numberOfElements(): Int = 1

        /**
         * Deliver a list of the key/value pairs in this node.
         *
         * @return                  the key/value pairs in this node
         */
        override fun toList(): List<Pair<K, V>> = ListF.singleton(Pair(key, value))

    }   // LeafNode



    internal class ArrayNode<K : Comparable<K>, V>(val numChildren: Int, val subNodes: Array<Node<K, V>>) : Node<K, V>() {

        override fun toGraph(): String = "Array(${numChildren}, ${subNodes.joinToString(", ", "[", "]")})"

        /**
         * Is the structure empty.
         *
         * @return                  true if the structure is empty, false otherwise
         */
        override fun isEmpty(): Boolean = false

        /**
         * Is the structure a tip, as represented by an empty node and a leaf node.
         *
         * @return                  true if the structure is a tip, false otherwise
         */
        override fun isTip(): Boolean = false



        // ---------- implementation ------------------------------

        /**
         * Alter this node given the key and the value function.
         *
         * @param shift             the shift amount
         * @param h                 the hash value for the given key
         * @param k                 the key
         * @param f                 function converting the corresponding value
         * @return                  new node; either ArrayNode or BitmapIndexedNode
         */
        override fun alterNode(shift: Int, h: Int, k: K, f: (Option<V>) -> Option<V>): Node<K, V> {
            ////println("ArrayNode.alterNode: shift: ${shift} h: ${h} k: ${k} node: ${this}")
            fun Array<Node<K, V>>.search(predicate: (Node<K, V>) -> Boolean): Array<Node<K, V>> {
                val result: KList<Node<K, V>> = this.filter{node: Node<K, V> -> predicate(node)}
                return Array(result.size){idx -> result[idx]}
            }   // search

            fun Array<Node<K, V>>.map(f: (Node<K, V>) -> Boolean): Array<Boolean> =
                    Array(this.size){idx -> f(this[idx])}

            fun <A : Comparable<A>, B> Array<Node<A, B>>.replace(index: Int, node: Node<A, B>): Array<Node<A, B>> =
                    Array(this.size){idx: Int -> if (idx == index) node else this[idx]}

            val foldRC: (Array<Boolean>) -> Int = {array: Array<Boolean> ->
                var bm: Int = 0
                for (idx: Int in array.size - 1 downTo 0)
                    bm = (bm shl 1) or (if (array[idx]) 1 else 0)
                bm
            }   // foldRC

            fun listArray(size: Int, array: Array<Node<K, V>>): Array<Node<K, V>> =
                    Array(size){idx: Int -> array[idx]}

            fun packArrayNode(subHash: Int, subN: Array<Node<K, V>>): Node<K, V> {
                val elemsP: Array<Node<K, V>> = Array(NodeF.chunk){idx -> if (idx == subHash) EmptyNode() else subN[idx]}
                val subNodesP: Array<Node<K, V>> = elemsP.search{node: Node<K, V> -> !node.isEmpty()}.copyOfRange(0, numChildren - 1)
                //val subNodesP: Array<Node<A, B>> = elemsP.search{node: Node<A, B> -> !node.isEmpty()}
                val bitmap: Int = foldRC(elemsP.map{node: Node<K, V> -> !node.isEmpty()})
                //val bitmap: Int = foldRC(elemsP.map{node: Node<A, B> -> node.isEmpty()})

                return BitmapIndexedNode(bitmap, subNodesP)
                /*****val elemsP: Array<Node<A, B>> = Array(NodeF.chunk, {idx -> if (idx == subHash) EmptyNode() else subN[idx]})
                val subNodesP: Array<Node<A, B>> = elemsP.search{node: Node<A, B> -> !node.isEmpty()}.copyOfRange(0, numChildren - 1)
                //val subNodesP: Array<Node<A, B>> = elemsP.search{node: Node<A, B> -> !node.isEmpty()}
                val bitmap: Int = foldRC(elemsP.map{node: Node<A, B> -> !node.isEmpty()})
                //val bitmap: Int = foldRC(elemsP.map{node: Node<A, B> -> node.isEmpty()})

                return BitmapIndexedNode(bitmap, subNodesP)
                 *****/
            }   // packArrayNode

            val subHash: Int = NodeF.hashFragment(shift, h)
            val child: Node<K, V> = subNodes[subHash]
            val childP: Node<K, V> = child.alterNode(shift + NodeF.shiftStep, h, k, f)
            val change: Change =
                    if (child.isEmpty()) {
                        if (childP.isEmpty()) Change.NIL else Change.ADDED
                    } else if (childP.isEmpty())
                        Change.REMOVED
                    else
                        Change.MODIFIED
            val numChildrenP: Int =
                    if (change == Change.REMOVED) numChildren - 1
                    else if (change == Change.MODIFIED) numChildren
                    else if (change == Change.NIL) numChildren
                    else numChildren + 1

            return if (numChildrenP < NodeF.arrayNodeMin)
                packArrayNode(subHash, subNodes)
            else
                ArrayNode(numChildrenP, subNodes.replace(subHash, childP))
        }

        /**
         * Look up the value for the given key in this node. Wrap the value in a Some
         *   if present, otherwise return a None.
         *
         * @param shift             the shift amount
         * @param h                 the hash value for the given key
         * @param k                 the key
         * @return                  the value corresponding to the given key, if present
         */
        override fun lookUpNode(shift: Int, h: Int, k: K): Option<V> {
            val subHash: Int = NodeF.hashFragment(shift, h)
            return subNodes[subHash].lookUpNode(shift + NodeF.shiftStep, h, k)
        }

        /**
         * Apply the function to the key and its corresponding value as a replacement for the value.
         *
         * @param f                 the transformation function
         * @return                  node of same type with the replaced value
         */
        override fun <W> mapWithKey(f: (K) -> (V) -> W): Node<K, W> {
            fun Array<Node<K, V>>.map(g: (K) -> (V) -> W): Array<Node<K, W>> =
                    Array(this.size){idx: Int -> this[idx].mapWithKey(g)}

            return ArrayNode(numChildren, subNodes.map(f))
        }

        /**
         * Filter out those nodes that match the predicate.
         *
         * @param predicate         match criteria
         * @return                  matching node
         */
        override fun filterWithKey(predicate: (K) -> (V) -> Boolean): Node<K, V> {
            fun Array<Node<K, V>>.map(f: (Node<K, V>) -> Node<K, V>): Array<Node<K, V>> =
                    Array(this.size, {idx -> f(this[idx])})

            fun Array<Node<K, V>>.zip(ints: Array<Int>): Array<Pair<Int, Node<K, V>>> =
                    Array(this.size, {idx: Int -> Pair(ints[idx], this[idx])})

            fun Array<Pair<Int, Node<K, V>>>.unzip(): Pair<Array<Int>, Array<Node<K, V>>> {
                val indices: Array<Int> = Array(this.size, {idx: Int -> this[idx].first})
                val nodes: Array<Node<K, V>> = Array(this.size, {idx: Int -> this[idx].second})
                return Pair(indices, nodes)
            }

            fun Array<Pair<Int, Node<K, V>>>.search(pred: (Pair<Int, Node<K, V>>) -> Boolean): Array<Pair<Int, Node<K, V>>> {
                val list: KList<Pair<Int, Node<K, V>>> = this.filter(pred)
                return Array(list.size, {idx: Int -> list.get(idx)})
            }

            val mapped: Array<Node<K, V>> = subNodes.map{node: Node<K, V> -> node.filterWithKey(predicate)}
            val zipped: Array<Pair<Int, Node<K, V>>> = mapped.zip(Array(32, {idx: Int -> idx}))
            val filtered: Array<Pair<Int, Node<K, V>>> = zipped.search{pr: Pair<Int, Node<K, V>> -> !pr.second.isEmpty()}
            val unzipped: Pair<Array<Int>, Array<Node<K, V>>> = filtered.unzip()
            val n: Int = filtered.size

            if (n == 0)
                return EmptyNode()
            else if (n == 1 && filtered[0].second.isTip())
                return filtered[0].second
            else {
                if (n <= NodeF.bmNodeMax)
                    return BitmapIndexedNode(NodeF.indicesToBitmap(unzipped.first), unzipped.second)
                else
                    return ArrayNode(n, mapped)
            }
        }

        /**
         * Determine the number of elements in this node.
         *
         * @return                  the number of elements
         */
        override fun numberOfElements(): Int = subNodes.fold(0){tot: Int, node: Node<K, V> -> tot + node.size()}

        /**
         * Deliver a list of the key/value pairs in this node.
         *
         * @return                  the key/value pairs in this node
         */
        override fun toList(): List<Pair<K, V>> =
                subNodes.fold(ListF.empty()){ls: List<Pair<K, V>>, node: Node<K, V> -> ls.append(node.toList())}

    }   // ArrayNode



    internal class BitmapIndexedNode<K : Comparable<K>, V>(val bitmap: Int, val subNodes: Array<Node<K, V>>) : Node<K, V>() {

        override fun toGraph(): String = "Bitmap(${bitmap}, ${subNodes.joinToString(", ", "[", "]")})"

        /**
         * Is the structure empty.
         *
         * @return                  true if the structure is empty, false otherwise
         */
        override fun isEmpty(): Boolean = false

        /**
         * Is the structure a tip, as represented by an empty node and a leaf node.
         *
         * @return                  true if the structure is a tip, false otherwise
         */
        override fun isTip(): Boolean = false



        // ---------- implementation ------------------------------

        /**
         * Alter this node given the key and the value function.
         *
         * @param shift             the shift amount
         * @param h                 the hash value for the given key
         * @param k                 the key
         * @param f                 function converting the corresponding value
         * @return                  new node; any of the other node types
         */
        override fun alterNode(shift: Int, h: Int, k: K, f: (Option<V>) -> Option<V>): Node<K, V> {
            ////println("BitmapIndexedNode.alterNode: shift: ${shift} h: ${h} k: ${k} subNodes size: ${subNodes.size} node: ${this}")

            fun splitAt(n: Int, array: Array<Node<K, V>>): Pair<Array<Node<K, V>>, Array<Node<K, V>>> {
                return if (n == 0)
                    Pair(arrayOf(), array.copyOf())
                else if (n >= array.size)
                    Pair(array.copyOf(), arrayOf())
                else
                    Pair(array.copyOfRange(0, n), array.copyOfRange(n, array.size))
            }   // splitAt

            fun Array<Node<K, V>>.prepend(node: Node<K, V>): Array<Node<K, V>> =
                    Array(1 + this.size, {idx -> if (idx == 0) node else this[idx - 1]})

            fun Array<Node<K, V>>.append(array: Array<Node<K, V>>): Array<Node<K, V>> =
                    Array(this.size + array.size, {idx -> if (idx < this.size) this[idx] else array[idx - this.size]})

            fun Array<Node<K, V>>.tail(): Array<Node<K, V>> =
                    this.copyOfRange(1, this.size)

            fun Array<Node<K, V>>.replace(index: Int, node: Node<K, V>): Array<Node<K, V>> =
                    Array(this.size, {idx: Int -> if (idx == index) node else this[idx]})

            fun Array<Node<K, V>>.replace(xs: List<Pair<Int, Node<K, V>>>): Array<Node<K, V>> =
                    Array(this.size, {idx: Int ->
                        val opt: Option<Pair<Int, Node<K, V>>> = xs.find{pr: Pair<Int, Node<K, V>> -> (pr.first == idx)}
                        if (opt.isDefined()) opt.get().second else this[idx]
                    })

            fun listArray(size: Int, array: Array<Node<K, V>>): Array<Node<K, V>> =
                    Array(size, {idx: Int -> array[idx]})

            fun expandBitmapNode(subHash: Int, nodeP: Node<K, V>, bm: Int, subN: Array<Node<K, V>>): Node<K, V> {
                val assocs: List<Pair<Int, Node<K, V>>> = NodeF.bitmapToIndices(bm).zip(ListF.fromArray(subN))
                val assocsP: List<Pair<Int, Node<K, V>>> = ListF.cons(Pair(subHash, nodeP), assocs)
                val blank: Array<Node<K, V>> = Array(32, {_ -> EmptyNode<K, V>()})
                val numChildren: Int = NodeF.bitCount32(bm) + 1
                return ArrayNode(numChildren, blank.replace(assocsP))
            }   // expandBitmapNode

            val subHash: Int = NodeF.hashFragment(shift, h)
            val index: Int = NodeF.fromBitmap(bitmap, subHash)
            val bit: Int = NodeF.toBitmap(subHash)
            val exists: Boolean = (bitmap and bit) != 0
            ////println("  BitmapIndexedNode.alterNode: subHash: ${subHash} index: ${index} bit: ${bit} exists: ${exists}")
            val child: Node<K, V> = if (exists) subNodes[index] else EmptyNode()
            val childP: Node<K, V> = child.alterNode(shift + NodeF.shiftStep, h, k, f)
            ////println("  BitmapIndexedNode.alterNode: subHash: ${subHash} index: ${index} bit: ${bit} exists: ${exists} child: ${child} childP: ${childP}")
            val change: Change =
                    if (exists) {
                        if (childP.isEmpty()) Change.REMOVED else Change.MODIFIED
                    } else if (childP.isEmpty())
                        Change.NIL
                    else
                        Change.ADDED
            val bound: Int = subNodes.size - 1
            val boundP: Int =
                    if (change == Change.REMOVED) bound - 1
                    else if (change == Change.MODIFIED) bound
                    else if (change == Change.NIL) bound
                    else bound + 1
            val split: Pair<Array<Node<K, V>>, Array<Node<K, V>>> = splitAt(index, subNodes)
            val subNodesP: Array<Node<K, V>> =
                    if (change == Change.REMOVED)
                        listArray(1 + boundP, split.first.append(split.second.tail()))
                    else if (change == Change.MODIFIED) subNodes.replace(index, childP)
                    else if (change == Change.NIL) subNodes
                    else
                        listArray(1 + boundP, split.first.append(split.second.prepend(childP)))
            /*****if (change == Change.REMOVED)
            split.first.append(split.second.tail())
            else if (change == Change.MODIFIED) subNodes.replace(index, childP)
            else if (change == Change.NIL) subNodes
            else
            split.first.append(split.second.prepend(childP))
             *****/
            val bitmapP: Int =
                    if (change == Change.REMOVED) bitmap and bit.inv()
                    else if (change == Change.MODIFIED) bitmap
                    else if (change == Change.NIL) bitmap
                    else bitmap or bit
            ////println("  BitmapIndexedNode.alterNode: change: ${change} bound: ${bound} boundP: ${boundP} split: (${split.first.joinToString(", ", "[", "]")}, ${split.second.joinToString(", ", "[", "]")}) subNodesP: ${subNodesP.joinToString(", ", "[", "]")} bitmapP: ${bitmapP}")

            return if (bitmapP == 0)
                EmptyNode()
            else if (boundP == 0 && subNodesP[0].isTip())
                subNodesP[0]
            else if (change == Change.ADDED && boundP > NodeF.bmNodeMax - 1)
                expandBitmapNode(subHash, childP, bitmap, subNodes)
            else
                BitmapIndexedNode(bitmapP, subNodesP)
        }

        /**
         * Look up the value for the given key in this node. Wrap the value in a Some
         *   if present, otherwise return a None.
         *
         * @param shift             the shift amount
         * @param h                 the hash value for the given key
         * @param k                 the key
         * @return                  the value corresponding to the given key, if present
         */
        override fun lookUpNode(shift: Int, h: Int, k: K): Option<V> {
            val subHash: Int = NodeF.hashFragment(shift, h)
            val idx: Int = NodeF.fromBitmap(bitmap, subHash)
            val exists: Boolean = (bitmap and NodeF.toBitmap(subHash)) != 0
            return if (exists)
                subNodes[idx].lookUpNode(shift + NodeF.shiftStep, h, k)
            else
                OptionF.none()
        }

        /**
         * Apply the function to the key and its corresponding value as a replacement for the value.
         *
         * @param f                 the transformation function
         * @return                  node of same type with the replaced value
         */
        override fun <W> mapWithKey(f: (K) -> (V) -> W): Node<K, W> {
            fun Array<Node<K, V>>.map(g: (K) -> (V) -> W): Array<Node<K, W>> =
                    Array(this.size){idx: Int -> this[idx].mapWithKey(g)}

            return BitmapIndexedNode(bitmap, subNodes.map(f))
        }

        /**
         * Filter out those nodes that match the predicate.
         *
         * @param predicate         match criteria
         * @return                  matching node
         */
        override fun filterWithKey(predicate: (K) -> (V) -> Boolean): Node<K, V> {
            fun Array<Node<K, V>>.map(f: (Node<K, V>) -> Node<K, V>): Array<Node<K, V>> =
                    Array(this.size, {idx -> f(this[idx])})

            fun Array<Node<K, V>>.zip(ints: List<Int>): Array<Pair<Int, Node<K, V>>> =
                    Array(this.size, {idx: Int -> Pair(ints[idx], this[idx])})

            fun Array<Pair<Int, Node<K, V>>>.unzip(): Pair<Array<Int>, Array<Node<K, V>>> {
                val indices: Array<Int> = Array(this.size, {idx: Int -> this[idx].first})
                val nodes: Array<Node<K, V>> = Array(this.size, {idx: Int -> this[idx].second})
                return Pair(indices, nodes)
            }

            fun Array<Pair<Int, Node<K, V>>>.search(pred: (Pair<Int, Node<K, V>>) -> Boolean): Array<Pair<Int, Node<K, V>>> {
                val list: KList<Pair<Int, Node<K, V>>> = this.filter(pred)
                return Array(list.size, {idx: Int -> list.get(idx)})
            }

            val mapped: Array<Node<K, V>> = subNodes.map{node: Node<K, V> -> node.filterWithKey(predicate)}
            val zipped: Array<Pair<Int, Node<K, V>>> = mapped.zip(NodeF.bitmapToIndices(bitmap))
            val filtered: Array<Pair<Int, Node<K, V>>> = zipped.search{pr: Pair<Int, Node<K, V>> -> !pr.second.isEmpty()}
            val unzipped: Pair<Array<Int>, Array<Node<K, V>>> = filtered.unzip()
            val arrayInt: Array<Int> = unzipped.first
            val arrayNode: Array<Node<K, V>> = unzipped.second

            if (arrayNode.size == 0)
                return EmptyNode()
            else if (arrayNode.size == 1 && arrayNode[0].isTip()) {
                val node: Node<K, V> = arrayNode[0]
                return node
            } else {
                val bmap: Int = NodeF.indicesToBitmap(arrayInt)
                return BitmapIndexedNode(bmap, arrayNode)
            }
        }

        /**
         * Determine the number of elements in this node.
         *
         * @return                  the number of elements
         */
        override fun numberOfElements(): Int = subNodes.fold(0){tot: Int, node: Node<K, V> -> tot + node.size()}

        /**
         * Deliver a list of the key/value pairs in this node.
         *
         * @return                  the key/value pairs in this node
         */
        override fun toList(): List<Pair<K, V>> =
                subNodes.fold(ListF.empty()){ls: List<Pair<K, V>>, node: Node<K, V> -> ls.append(node.toList())}

    }   // BitmapIndexedNode



    internal class HashCollisionNode<K : Comparable<K>, V>(val hash: Int, val pairs: List<Pair<K, V>>) : Node<K, V>() {

        override fun toGraph(): String = "HashCollision(${hash}, [${pairs.foldLeft(""){str, pair -> "${str}, (${pair.first}, ${pair.second})"}}])"

        /**
         * Is the structure empty.
         *
         * @return                  true if the structure is empty, false otherwise
         */
        override fun isEmpty(): Boolean = false

        /**
         * Is the structure a tip, as represented by an empty node and a leaf node.
         *
         * @return                  true if the structure is a tip, false otherwise
         */
        override fun isTip(): Boolean = true



        // ---------- implementation ------------------------------

        /**
         * Alter this node given the key and the value function.
         *
         * @param shift             the shift amount
         * @param h                 the hash value for the given key
         * @param k                 the key
         * @param f                 function converting the corresponding value
         * @return                  new node; either LeafNode or HashCollisionNode
         */
        override fun alterNode(shift: Int, h: Int, k: K, f: (Option<V>) -> Option<V>): Node<K, V> {
            ////println("HashCollisionNode.alterNode: shift: ${shift} h: ${h} k: ${k} node: ${this}")
            fun updateList(f: (Option<V>) -> Option<V>, key: K, ps: List<Pair<K, V>>): List<Pair<K, V>> {
                return if (ps.size() == 0)
                    f(OptionF.none()).fold({ ListF.empty<Pair<K, V>>() }, {v: V -> ListF.singleton(Pair(key, v))})
                else {
                    val pair: Pair<K, V> = ps.head()
                    if (key == pair.first)
                        f(OptionF.some(pair.second)).fold({ ps.tail() }, {v: V -> ListF.cons(Pair(pair.first, v), ps.tail())})
                    else
                        ListF.cons(ps.head(), updateList(f, key, ps.tail()))
                }
            }   // updateList

            val pairsP: List<Pair<K, V>> = updateList(f, k, pairs)
            return if (pairsP.size() == 0)
                throw MapException("HashCollisionNode.alterNode: empty pair list")
            else if (pairsP.size() == 1)
                LeafNode(this.hash, pairsP[0].first, pairsP[0].second)
            else
                HashCollisionNode(this.hash, pairsP)
        }

        /**
         * Look up the value for the given key in this node. Wrap the value in a Some
         *   if present, otherwise return a None.
         *
         * @param shift             the shift amount
         * @param h                 the hash value for the given key
         * @param k                 the key
         * @return                  the value corresponding to the given key, if present
         */
        override fun lookUpNode(shift: Int, h: Int, k: K): Option<V> {
            val op: Option<Pair<K, V>> = pairs.find{pr: Pair<K, V> -> (k == pr.first)}
            return op.fold({ OptionF.none() }, {pr: Pair<K, V> -> OptionF.some(pr.second)})
        }

        /**
         * Apply the function to the key and its corresponding value as a replacement for the value.
         *
         * @param f                 the transformation function
         * @return                  node of same type with the replaced value
         */
        override fun <W> mapWithKey(f: (K) -> (V) -> W): Node<K, W> =
                HashCollisionNode(hash, pairs.map{pr: Pair<K, V> -> Pair(pr.first, f(pr.first)(pr.second))})

        /**
         * Filter out those nodes that match the predicate.
         *
         * @param predicate         match criteria
         * @return                  matching node
         */
        override fun filterWithKey(predicate: (K) -> (V) -> Boolean): Node<K, V> {
            val pairsP: List<Pair<K, V>> = pairs.filter{pr: Pair<K, V> -> predicate(pr.first)(pr.second)}
            if (pairsP.size() == 0)
                return EmptyNode()
            else if (pairsP.size() == 1)
                return LeafNode(hash, pairsP[0].first, pairsP[0].second)
            else
                return HashCollisionNode(hash, pairsP)
        }

        /**
         * Determine the number of elements in this node.
         *
         * @return                  the number of elements
         */
        override fun numberOfElements(): Int = pairs.size()

        /**
         * Deliver a list of the key/value pairs in this node.
         *
         * @return                  the key/value pairs in this node
         */
        override fun toList(): List<Pair<K, V>> = pairs

    }   // HashCollisionNode



    /**
     * Is the structure empty.
     *
     * @return                  true if the structure is empty, false otherwise
     */
    abstract fun isEmpty(): Boolean

    /**
     * Is the structure a tip, as represented by an empty node and a leaf node.
     *
     * @return                  true if the structure is a tip, false otherwise
     */
    abstract fun isTip(): Boolean

    /**
     * Obtain the size of the map.
     *
     * @return                  the number of elements in the map
     */
    fun length(): Int = this.size()

    /**
     * Obtain the size of the map.
     *
     * @return                  the number of elements in the map
     */
    fun size(): Int = this.numberOfElements()

    /**
     * Present the map as a graph revealing the left and right subtrees.
     *
     * @return                  the map as a graph
     */
    abstract fun toGraph(): String



    /**
     * Determine if the map contains the given key.
     *
     * @param key               search key
     * @return                  true if the map contains this key
     */
    fun containsKey(key: K): Boolean =
            this.lookUpKey(key).fold({ false }, {_: V -> true})

    /**
     * Delete the key and its value from the map. If the key is not in the map
     *   then the original map is returned.
     *
     * @param key               look up key in the map
     * @return                  the updated map
     */
    fun delete(key: K): Node<K, V> {
        return this.alter(key){_: Option<V> -> OptionF.none()}
    }

    /**
     * Filter all values that satisfy the predicate.
     *
     * @param predicate         search criteria
     * @return                  resulting map
     */
    fun filter(predicate: (V) -> Boolean): Node<K, V> {
        return this.filterWithKey{_: K -> {v: V -> predicate(v)}}
    }

    /**
     * foldLeft is a higher-order function that folds a left associative binary
     *   function into the values of a map.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldLeft(0){res -> {age -> res + age}} = 78
     *   <[]>.foldLeft(0){res -> {age -> res + age}} = 0
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldLeft([]){res -> {age -> res.append(age)}} = [22, 31, 25]
     *
     * @param e           	    initial value
     * @param f         		curried binary function:: W -> V -> W
     * @return            	    folded result
     */
    fun <W> foldLeft(e: W, f: (W) -> (V) -> W): W {
        fun recFoldLeft(e: W, f: (W) -> (V) -> W, node: Node<K, V>): W {
            return when(node) {
                is EmptyNode -> e
                is LeafNode -> f(e)(node.value)
                is ArrayNode -> node.subNodes.fold(e){acc: W, nod: Node<K, V> -> recFoldLeft(acc, f, nod)}
                is BitmapIndexedNode -> node.subNodes.fold(e){acc: W, nod: Node<K, V> -> recFoldLeft(acc, f, nod)}
                is HashCollisionNode -> node.pairs.map{pair -> pair.second}.foldLeft(e, f)
            }
        }   // recFoldLeft

        return recFoldLeft(e, f, this)
    }

    /**
     * foldRight is a higher-order function that folds a right associative binary
     *   function into the values of a map.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldRight(0){age -> {res -> res + age}} = 78
     *   <[]>.foldRight(0){age -> {res -> res + age}} = 0
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldRight([]){age -> {res -> res.append(age)}} = [25, 31, 22]
     *
     * @param e           	    initial value
     * @param f         		curried binary function:: B -> C -> C
     * @return            	    folded result
     */
    fun <W> foldRight(e: W, f: (V) -> (W) -> W): W {
        fun recFoldRight(e: W, f: (V) -> (W) -> W, node: Node<K, V>): W {
            return when(node) {
                is EmptyNode -> e
                is LeafNode -> f(node.value)(e)
                is ArrayNode -> node.subNodes.foldRight(e){nod: Node<K, V>, acc: W -> recFoldRight(acc, f, nod)}
                is BitmapIndexedNode -> node.subNodes.foldRight(e){nod: Node<K, V>, acc: W -> recFoldRight(acc, f, nod)}
                is HashCollisionNode -> node.pairs.map{pair -> pair.second}.foldRight(e, f)
            }
        }   // recFoldRight

        return recFoldRight(e, f, this)
    }   // foldRight

    /**
     * Insert a new key and the value into the map. If the key is already present,
     *   the associated value is replaced with the given value.
     *
     * @param key               new key
     * @param value             new associated value
     * @return                  updated map
     */
    fun insert(key: K, value: V): Node<K, V> {
        val constC: (V) -> (V) -> V = {v1: V -> {_: V -> v1}}
        return this.insertWith(key, value, constC)
    }

    /**
     * Returns a List view of the keys contained in this map.
     *
     * @return    		        the keys for this map
     */
    fun keyList(): List<K> =
            this.toList().map{pr: Pair<K, V> -> pr.first}

    /**
     * Look up the value at the key in the map.
     *
     * @param key               search key
     * @return                  corresponding value, if key is present
     */
    fun lookUpKey(key: K): Option<V> =
            this.lookUpNode(0, key.hashCode(), key)

    /**
     * Look up the value at the key in the map. Return the corresponding
     *   value if the key is present otherwise throws an exception.
     *
     * @param key               search key
     * @return                  corresponding value, if key is present
     */
    fun lookUp(key: K): V {
        val op: Option<V> = this.lookUpKey(key)
        return if (op.isEmpty()) throw MapException("lookUp: key '${key}' not in the map") else op.get()
    }

    /**
     * Look up the given key in the map. Return defaultValue if absent, otherwise
     *   return the corresponding value.
     *
     * @param key               search key
     * @param defaultValue      default value to use if key is absent
     * @return                  matching value or default if key is absent
     */
    fun lookUpKeyWithDefault(key: K, defaultValue: V): V =
            this.lookUpKey(key).fold({ defaultValue }, {v: V -> v})

    /**
     * Map a function over all the values in the map.
     *
     * @param f                 mapping function
     * @return                  updated map
     */
    fun <W> map(f: (V) -> W): Node<K, W> {
        return this.mapWithKey{_: K -> {v: V -> f(v)}}
    }

    /**
     * Convert the map to a list of key/value pairs where the keys are in ascending order.
     */
    fun toAscendingList(): List<Pair<K, V>> {
        fun <K : Comparable<K>, V> List<Pair<K, V>>.insertOrdered(key: K, value: V): List<Pair<K, V>> {
            return when(this) {
                is List.Nil -> ListF.singleton(Pair(key, value))
                is List.Cons -> if (key <= this.hd.first) ListF.cons(Pair(key, value), this) else ListF.cons(this.hd, this.tl.insertOrdered(key, value))
            }
        }   // insertOrdered

        fun recToAscendingList(node: Node<K, V>, list: List<Pair<K, V>>): List<Pair<K, V>> {
            return when(node) {
                is EmptyNode -> list
                is LeafNode -> list.insertOrdered(node.key, node.value)
                is ArrayNode -> node.subNodes.fold(list){ac, nod -> recToAscendingList(nod, ac)}
                is BitmapIndexedNode -> node.subNodes.fold(list){ac, nod -> recToAscendingList(nod, ac)}
                is HashCollisionNode -> node.pairs.fold(list){ac, pr -> ac.insertOrdered(pr.first, pr.second)}
            }
        }   // recToAscendingList

        return recToAscendingList(this, ListF.empty())
    }   // toAscendingList

    /**
     * Textual representation of a map.
     *
     * @return                  text for a map: <[key1: value1, key2: value2, ...]>
     */
    override fun toString(): String {
        fun recToString(node: Node<K, V>, acc: String): String {
            return when(node) {
                is EmptyNode -> acc
                is LeafNode -> if (acc == "") "${node.key}: ${node.value}" else "$acc, ${node.key}: ${node.value}"
                is ArrayNode -> node.subNodes.fold(acc){ac, nod -> recToString(nod, ac)}
                is BitmapIndexedNode -> node.subNodes.fold(acc){ac, nod -> recToString(nod, ac)}
                is HashCollisionNode -> node.pairs.fold(acc){ac, pr -> "$ac, ${pr.first}: ${pr.second}"}
            }
        }   // recToString

        return recToString(this, "")
    }   // toString

    /**
     * Update the value at the key if the key is in the map. If the update function
     *   f applied to the value is None then the element is deleted. If the update
     *   function f applied to the value is Some then the key is bound to the value
     *   wrapped in the Some.
     *
     * @param key               look up key in the map
     * @param f                 update function
     * @return                  updated map
     */
    fun update(f: (V) -> Option<V>): (K) -> Node<K, V> {
        fun bindRC(g: (V) -> Option<V>): (Option<V>) -> Option<V> = {op: Option<V> -> op.bind(g)}
        fun alterC(g: (V) -> Option<V>): (K) -> Node<K, V> = {k: K -> this.alter(k, bindRC(g))}
        return alterC(f)
    }

    /**
     * Returns a List view of the values contained in this map.
     *
     * @return    		        the values for this map
     */
    fun valueList(): List<V> =
            this.toList().map{pr: Pair<K, V> -> pr.second}



// ---------- implementation ------------------------------

    /**
     * Alter the value associated with the given key if present in the map. This function
     *   can be used to implement insert, delete or update.
     *
     * @param key               lookup key in the map
     * @param f                 the function to apply to the existing value
     * @return                  updated map
     */
    fun alter(key: K, f: (Option<V>) -> Option<V>): Node<K, V> =
            this.alterNode(0, key.hashCode(), key, f)

    /**
     * Insert with a function, combining the new value and the old value. The
     *   function inserts the pair key/value into the map if the key is not present.
     *   If the key does exist, the function will insert the pair key/
     *   f(new value, old value).
     *
     * @param k                 new key
     * @param v                 new associated value
     * @param f                 binary function over the values
     * @return                  updated map
     */
    private fun insertWith(k: K, v: V, f: (V) -> (V) -> V): Node<K, V> {
        // function to apply to the existing value
        fun fnC(f: (V) -> (V) -> V, v: V): (Option<V>) -> Option<V> {
            return {op: Option<V> ->
                when(op) {
                    is None -> Some(v)
                    is Some -> Some(f(v)(op.get()))
                }
            }
            /***return {op: OptionIF<V> ->
            op.match(
            {none: None<V> -> Some(v)},
            {some: Some<V> -> Some(f(v)(some.get()))}
            )
            }
             ***/
        }   // fnC

        return this.alter(k, fnC(f, v))
    }

    /**
     * Alter this node given the key and the value function.
     *
     * @param shift             the shift amount
     * @param h                 the hash value for the given key
     * @param k                 the key
     * @param f                 function converting the corresponding value
     * @return                  new node
     */
    abstract fun alterNode(shift: Int, h: Int, k: K, f: (Option<V>) -> Option<V>): Node<K, V>

    /**
     * Look up the value for the given key in this node. Wrap the value in a Some
     *   if present, otherwise return a None.
     *
     * @param shift             the shift amount
     * @param h                 the hash value for the given key
     * @param k                 the key
     * @return                  the value corresponding to the given key, if present
     */
    abstract fun lookUpNode(shift: Int, h: Int, k: K): Option<V>

    /**
     * Apply the function to the key and its corresponding value as a replacement for the value.
     *
     * @param f                 the transformation function
     * @return                  node of same type with the replaced value
     */
    abstract fun <W> mapWithKey(f: (K) -> (V) -> W): Node<K, W>

    /**
     * Filter out those nodes that match the predicate.
     *
     * @param predicate         match criteria
     * @return                  matching node
     */
    abstract fun filterWithKey(predicate: (K) -> (V) -> Boolean): Node<K, V>

    /**
     * Determine the number of elements in this node.
     *
     * @return                  the number of elements
     */
    abstract fun numberOfElements(): Int

    /**
     * Deliver a list of the key/value pairs in this node.
     *
     * @return                  the key/value pairs in this node
     */
    abstract fun toList(): List<Pair<K, V>>

}   // Node
