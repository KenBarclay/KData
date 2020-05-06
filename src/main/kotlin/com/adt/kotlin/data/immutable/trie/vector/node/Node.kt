package com.adt.kotlin.data.immutable.trie.vector.node

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

import com.adt.kotlin.data.immutable.trie.vector.VectorException

import com.adt.kotlin.data.immutable.list.*
import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.List.Nil
import com.adt.kotlin.data.immutable.list.List.Cons
import com.adt.kotlin.data.immutable.list.ListF

import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.OptionF.none
import com.adt.kotlin.data.immutable.option.OptionF.some

import com.adt.kotlin.hkfp.fp.FunctionF



sealed class Node<A> {

    internal class EmptyNode<A> : Node<A>() {

        /**
         * Append a single element on to this vector.
         *
         * @param a                 new element
         * @return                  new vector with element at end
         */
        override fun append(a: A): Node<A> = NodeF.singleton(a)

        /**
         * Test whether this vector is empty.
         *
         * @return                  true if this vector is empty
         */
        override fun isEmpty(): Boolean = true

        /**
         * Obtains the length of this vector.
         *
         * @return                  number of elements in the vector
         */
        override fun length(): Int = 0

        /**
         * Present the trie as a graph revealing the subtrees.
         *
         * @return                  the trie as a graph
         */
        override fun toGraph(): String = "Empty"

    }   // EmptyNode



    internal class RootNode<A>(val size: Int, val shift: Int, val offset: Int, val capacity: Int, val tail: List<A>, val vecPtrs: Array<Node<A>>) : Node<A>() {

        /**
         * Append a single element on to this vector.
         *
         * @param a                 new element
         * @return                  new vector with element at end
         */
        override fun append(a: A): Node<A> {
            return if (capacity >= size) {
                val vecP: Node<A> = RootNode(1 + size, shift, offset, capacity, tail, vecPtrs)
                vecP.update(size - offset, a)
            } else if ((size and 0x1F) != 0)
                RootNode(1 + size, shift, offset, capacity, ListF.cons(a, tail), vecPtrs)
            else if ((size shr NodeF.shiftStep) > (1 shl shift))
                RootNode(1 + size, shift + NodeF.shiftStep, offset, capacity + NodeF.chunk, ListF.singleton(a), arrayOf(InternalNode(vecPtrs), newPath(shift, tail)))
            else
                RootNode(size + 1, shift, offset, capacity + NodeF.chunk, ListF.singleton(a), pushTail(size, tail)(shift)(vecPtrs))
        }   // append

        /**
         * Test whether this vector is empty.
         *
         * @return                  true if this vector is empty
         */
        override fun isEmpty(): Boolean = false

        /**
         * Obtains the length of this vector.
         *
         * @return                  number of elements in the vector
         */
        override fun length(): Int = size - offset

        /**
         * Present the trie as a graph revealing the subtrees.
         *
         * @return                  the trie as a graph
         */
        override fun toGraph(): String {
            val tails: String = tail.makeString(", ", "[", "]")
            val buffer: StringBuffer = vecPtrs.fold(StringBuffer()){buf, node -> buf.append("\n  ${node.toGraph()}")}
            return "Root(size: ${size} shift: ${shift} offset: ${offset} capacity: ${capacity}\ntail: ${tails};\nvecPtrs: ${buffer})"
        }



        // ---------- internal --------------------------------

        private fun pushTail(size: Int, tail: List<A>): (Int) -> (Array<Node<A>>) -> Array<Node<A>> {
            fun go(shift: Int, nodes: Array<Node<A>>): Array<Node<A>> {
                fun Array<Node<A>>.replace(index: Int, element: Node<A>): Array<Node<A>> =
                        Array(this.size, {idx: Int -> if (idx == index) element else this[idx]})

                fun Array<Node<A>>.append(node: Node<A>): Array<Node<A>> =
                        Array(1 + this.size, {idx: Int -> if (idx == this.size) node else this[idx]})

                fun fromList(list: List<A>): Array<Wrapper<A>> =
                        Array(list.size(), {idx: Int -> Wrapper(list[idx])})

                val subIndex: Int = (((size - 1) shr shift) and 0x1F)
                return if (shift == NodeF.shiftStep)
                    nodes.append(DataNode(fromList(tail.reverse())))
                else if (subIndex < nodes.size) {
                    val vec: Node<A> = nodes[subIndex]
                    val insert: Array<Node<A>> = if (vec is RootNode<A>)
                        go(shift - NodeF.shiftStep, vec.vecPtrs)
                    else if (vec is InternalNode<A>)
                        go(shift - NodeF.shiftStep, vec.vecPtrs)
                    else
                        throw VectorException("RootNode.pushTail: no RootNode or InternalNode")
                    nodes.replace(subIndex, InternalNode(insert))
                } else
                    nodes.append(newPath(shift - NodeF.shiftStep, tail))
            }   // go

            return {shift: Int -> {nodes: Array<Node<A>> -> go(shift, nodes)}}
        }

        private fun newPath(level: Int, tail: List<A>): Node<A> {
            fun fromList(list: List<A>): Array<Wrapper<A>> =
                    Array(list.size(), {idx: Int -> Wrapper(list[idx])})

            return if (level == 0)
                DataNode(fromList(tail.reverse()))
            else
                InternalNode(arrayOf(newPath(level - NodeF.shiftStep, tail)))
        }

    }   // RootNode



    internal class InternalNode<A>(val vecPtrs: Array<Node<A>>) : Node<A>() {

        /**
         * Append a single element on to this vector.
         *
         * @param a                 new element
         * @return                  new vector with element at end
         */
        override fun append(a: A): Node<A> = throw VectorException("InternalNode.append: internal nodes should not be exposed")

        /**
         * Test whether this vector is empty.
         *
         * @return                  true if this vector is empty
         */
        override fun isEmpty(): Boolean = false

        /**
         * Obtains the length of this vector.
         *
         * @return                  number of elements in the vector
         */
        override fun length(): Int = throw VectorException("InternalNode.size: internal nodes should not be exposed")

        /**
         * Present the trie as a graph revealing the subtrees.
         *
         * @return                  the trie as a graph
         */
        override fun toGraph(): String {
            val buffer: StringBuffer = vecPtrs.fold(StringBuffer()){buf, node -> buf.append("\n  ${node.toGraph()}")}
            return "Internal(${buffer.delete(0, 2).toString()})"
        }

    }   // InternalNode



    internal class Wrapper<A>(val wrapped: A) {
        override fun toString() = "${wrapped}"
    }

    internal class DataNode<A>(val data: Array<Wrapper<A>>) : Node<A>() {

        /**
         * Append a single element on to this vector.
         *
         * @param a                 new element
         * @return                  new vector with element at end
         */
        override fun append(a: A): Node<A> = throw VectorException("DataNode.append: internal nodes should not be exposed")

        /**
         * Test whether this vector is empty.
         *
         * @return                  true if this vector is empty
         */
        override fun isEmpty(): Boolean = false

        /**
         * Obtains the length of this vector.
         *
         * @return                  number of elements in the vector
         */
        override fun length(): Int = throw VectorException("DataNode.size: data nodes should not be exposed")

        /**
         * Present the trie as a graph revealing the subtrees.
         *
         * @return                  the trie as a graph
         */
        override fun toGraph(): String {
            val buffer: StringBuffer = data.fold(StringBuffer()){buf, wrap -> buf.append(", ${wrap.wrapped}")}
            return "Data(${buffer.delete(0, 2).toString()})"
        }

    }   // DataNode



    /**
     * Append a single element on to this vector.
     *
     * @param a                 new element
     * @return                  new vector with element at end
     */
    abstract fun append(a: A): Node<A>

    /**
     * Test whether this vector is empty.
     *
     * @return                  true if this vector is empty
     */
    abstract fun isEmpty(): Boolean

    /**
     * Obtains the length of this vector.
     *
     * @return                  number of elements in the vector
     */
    abstract fun length(): Int

    /**
     * Present the trie as a graph revealing the subtrees.
     *
     * @return                  the trie as a graph
     */
    abstract fun toGraph(): String



    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param other             the other object
     * @return                  true if "equal", false otherwise
     */
    override fun equals(other: Any?): Boolean {
        return if (this === other)
            true
        else if (other == null || this::class.java != other::class.java)
            false
        else {
            @Suppress("UNCHECKED_CAST") val node: Node<A> = other as Node<A>
            (this.toList() == node.toList())
        }
    }   // equals

    /**
     * Convert this node to a list of key/value pairs
     *
     * @return                  list of key/value pairs
     */
    fun toList(): List<A> {
        fun recToList(node: Node<A>, buffer: ListBufferIF<A>): ListBufferIF<A> {
            return when(node) {
                is EmptyNode -> buffer
                is RootNode -> {
                    val buff: ListBufferIF<A> = node.vecPtrs.fold(buffer){buf, nod -> recToList(nod, buf)}
                    node.tail.foldRight(buff){a: A -> {buf: ListBufferIF<A> -> buf.append(a)}}
                }
                is InternalNode -> node.vecPtrs.fold(buffer){buf, nod -> recToList(nod, buf)}
                is DataNode -> node.data.fold(buffer){buf, wrap -> buf.append(wrap.wrapped)}
            }
        }   // recToList

        return recToList(this, ListBuffer()).toList()
    }



    /**
     * Return an iterator over elements of type A.
     */
    fun iterator(): Iterator<A> {
        val list: List<A> = this.foldRight(ListF.empty()){elem, list -> ListF.cons(elem, list)}
        return list.iterator()
    }

    /**
     * Append the given vector on to this vector, eg:
     *   concatenate([1, 2, 3], [4, 5]) = [1, 2, 3, 4, 5]
     *
     * @param xs                existing vector
     * @return                  new vector of appended elements
     */
    fun append(xs: Node<A>): Node<A> = this.concatenate(xs)

    /**
     * Append the given vector on to this vector, eg:
     *   concatenate([1, 2, 3], [4, 5]) = [1, 2, 3, 4, 5]
     *
     * @param xs                existing vector
     * @return                  new vector of appended elements
     */
    fun concatenate(xs: Node<A>): Node<A> =
            xs.foldLeft(this){node: Node<A>, a: A -> node.append(a)}

    /**
     * Determine if this vector contains the element determined by the predicate.
     *
     * @param predicate         search predicate
     * @return                  true if search element is present, false otherwise
     */
    fun contains(predicate: (A) -> Boolean): Boolean {
        fun Array<Wrapper<A>>.contains(predicate: (A) -> Boolean): Boolean {
            val wrapper: Wrapper<A>? = this.firstOrNull{wrap: Wrapper<A> -> predicate(wrap.wrapped)}
            return (wrapper != null)
        }

        fun Array<Node<A>>.contains(predicate: (A) -> Boolean): Boolean {
            val node: Node<A>? = this.firstOrNull{n: Node<A> -> n.contains(predicate)}
            return (node != null)
        }

        return when(this) {
            is EmptyNode -> false
            is RootNode -> {
                val found: Boolean = this.vecPtrs.contains(predicate)
                if (!found) this.tail.reverse().contains(predicate) else found
            }
            is InternalNode -> this.vecPtrs.contains(predicate)
            is DataNode -> this.data.contains(predicate)
        }
    }   // contains

    /**
     * Determine if this vector contains the given element.
     *
     * @param a                 search element
     * @return                  true if search element is present, false otherwise
     */
    fun contains(a: A): Boolean = this.contains{b: A -> (b == a)}

    /**
     * Count the number of times a value appears in this vector matching the criteria.
     *
     * @param predicate         the search criteria
     * @return                  the number of occurrences
     */
    fun count(predicate: (A) -> Boolean): Int {
        fun go(predicate: (A) -> Boolean, node: Node<A>): Int {
            fun Array<Wrapper<A>>.count(predicate: (A) -> Boolean): Int =
                    this.count{wrap: Wrapper<A> -> predicate(wrap.wrapped)}

            fun Array<Node<A>>.count(predicate: (A) -> Boolean): Int =
                    this.fold(0){cnt: Int, n: Node<A> -> cnt + n.count(predicate)}

            return when(node) {
                is EmptyNode -> 0
                is RootNode -> {
                    val cnt: Int = node.vecPtrs.count(predicate)
                    cnt + node.tail.count(predicate)
                }
                is InternalNode -> node.vecPtrs.count(predicate)
                is DataNode -> node.data.count(predicate)
            }
        }   // go

        return go(predicate, this)
    }   // count

    /**
     * Count the number of times the parameter appears in this vector.
     *
     * @param a                 the search value
     * @return                  the number of occurrences
     */
    fun count(a: A): Int = this.count{b: A -> (b == a)}

    /**
     * Return the element at the specified position in this vector.
     *   Throws a VectorException if the index is out of bounds.
     *
     * @param index             position in vector
     * @return                  the element at the specified position in the vector
     */
    operator fun get(index: Int): A =
            if (index < this.length()) this.getUnSafe(index, this) else throw VectorException("get: index out of bounds: ${index}")

    /**
     * Find the index of the given value, or -1 if absent.
     *
     * Examples:
     *   [1, 2, 3, 4].indexOf{n -> (n == 1)} = 0
     *   [1, 2, 3, 4].indexOf{n -> (n == 3)} = 2
     *   [1, 2, 3, 4].indexOf{n -> (n == 5)} = -1
     *   [].indexOf{n -> (n == 2)} = -1
     *
     * @param predicate         the search predicate
     * @return                  the index position
     */
    fun indexOf(predicate: (A) -> Boolean): Int {
        fun recIndexOf(predicate: (A) -> Boolean, offset: Int, node: Node<A>): Pair<Int, Int> {  // index and offset
            return when(node) {
                is EmptyNode -> Pair(-1, offset)
                is RootNode -> {
                    var idx: Int = -1
                    var offs: Int = offset
                    for (ix: Int in node.vecPtrs.indices) {
                        if (idx < 0) {
                            val pr: Pair<Int, Int> = recIndexOf(predicate, offs, node.vecPtrs[ix])
                            idx = pr.first
                            offs = pr.second
                        }
                    }

                    if (idx < 0) {
                        val reversedTail: List<A> = node.tail.reverse()
                        for (ix: Int in 0..(reversedTail.size() - 1)) {
                            if (idx < 0) {
                                if (predicate(reversedTail[ix]))
                                    idx = ix + offs
                            }
                        }
                        Pair(idx, offs)
                    } else
                        Pair(idx, offs)
                }
                is InternalNode -> {
                    var idx: Int = -1
                    var offs: Int = offset
                    for (ix: Int in node.vecPtrs.indices) {
                        if (idx < 0) {
                            val pr: Pair<Int, Int> = recIndexOf(predicate, offs, node.vecPtrs[ix])
                            idx = pr.first
                            offs = pr.second
                        }
                    }
                    Pair(idx, offs)
                }
                is DataNode -> {
                    var idx: Int = -1
                    for (ix: Int in node.data.indices) {
                        if (idx < 0) {
                            if (predicate(node.data[ix].wrapped))
                                idx = ix + offset
                        }
                    }
                    Pair(idx, offset + NodeF.chunk)
                }
            }
        }   // recIndexOf

        return recIndexOf(predicate, 0, this).first
    }   // indexOf

    /**
     * Find the index of the given value, or -1 if absent.
     *
     * @param a                 the search value
     * @return                  the index position
     */
    fun indexOf(a: A): Int = this.indexOf{b -> (b == a)}

    /**
     * Obtains the length of this vector.
     *
     * @return                  number of elements in the vector
     */
    fun size(): Int = this.length()

    /**
     * Produce a string representation of a vector.
     *
     * @return                  string as <{ ... ]>
     */
    override fun toString(): String {
        return when(this) {
            is EmptyNode -> ""
            is RootNode -> {
                val sbuf: StringBuffer = this.vecPtrs.fold(StringBuffer()){sb: StringBuffer, node: Node<A> -> sb.append("${node}, ")}
                val sbuf2: StringBuffer = if (this.vecPtrs.count() == 0) sbuf else sbuf.delete(sbuf.length - 2, sbuf.length)
                val sbuf3: StringBuffer = this.tail.reverse().foldLeft(StringBuffer()){sb: StringBuffer, a: A -> sb.append("${a}, ")}
                val sbuf4: StringBuffer = if (this.tail.isEmpty()) sbuf3 else sbuf3.delete(sbuf3.length - 2, sbuf3.length)
                val sbuffer: StringBuffer = if (this.vecPtrs.count() == 0) sbuf4 else sbuf2.append(", ${sbuf4}")
                sbuffer.toString()
            }
            is InternalNode -> {
                val sbuf: StringBuffer = this.vecPtrs.fold(StringBuffer()){sb: StringBuffer, node: Node<A> -> sb.append("${node}, ")}
                val sbuffer: StringBuffer = if (this.vecPtrs.count() == 0) sbuf else sbuf.delete(sbuf.length - 2, sbuf.length)
                sbuffer.toString()
            }
            is DataNode -> {
                val sbuf: StringBuffer = this.data.fold(StringBuffer()){sb: StringBuffer, wrap: Wrapper<A> -> sb.append("${wrap.wrapped}, ")}
                val sbuffer: StringBuffer = if (this.data.count() == 0) sbuf else sbuf.delete(sbuf.length - 2, sbuf.length)
                sbuffer.toString()
            }
        }
    }   // toString

    /**
     * Update the element at the given index this vector.
     *
     * @param index             update position
     * @param a                 new element
     * @return                  new vector with element at specified position
     */
    fun update(index: Int, a: A): Node<A> {
        fun bulkUpdates(node: Node<A>, updates: List<Pair<Int, A>>): Node<A> {
            return updates.foldRight(node){pr: Pair<Int, A>, vnode: Node<A> -> vnode.replaceElement(pr.first, pr.second)}
        }

        return bulkUpdates(this, ListF.singleton(Pair(index, a)))
    }   // update

    /**
     * Sort the elements of this vector into ascending order and deliver
     *   the resulting vector. The elements are compared using the given
     *   comparator.
     *
     * Examples:
     *   [4, 3, 2, 1].sort{x, y -> if (x < y) -1 else if (x > y) +1 else 0} = [1, 2, 3, 4]
     *   [1, 2, 3, 4].sort{x, y -> if (x < y) -1 else if (x > y) +1 else 0} = [1, 2, 3, 4]
     *   [].sort{x, y -> if (x < y) -1 else if (x > y) +1 else 0} = []
     *   ["Ken", "John", "Jessie", "", ""].sort{str1, str2 -> str1.compareTo(str2)} = ["", "", "Jessie", "John", "Ken"]
     *
     * @param comparator        element comparison function
     * @return                  the sorted seq
     */
    fun sort(comparator: (A, A) -> Int): Node<A> {
        fun recSort(node: Node<A>, comparator: (A, A) -> Int): Node<A> {
            return if (node.size() <= 1)
                node
            else {
                val item: A = node[0]
                val equalItems: Node<A> = node.filter{x -> (comparator(x, item) == 0)}
                val smallerItems: Node<A> = node.filter{x -> (comparator(x, item) < 0)}
                val largerItems: Node<A> = node.filter{x -> (comparator(x, item) > 0)}
                recSort(smallerItems, comparator).append(equalItems).append(recSort(largerItems, comparator))
            }
        }   // recSort

        return recSort(this, comparator)
    }



// ---------- vector transformations ----------------------

    /**
     * Function map applies the function parameter to each item in this vector, delivering
     *   a new vector.
     *
     * @param f                 pure function:: A -> B
     * @return                  new vector of transformed values
     */
    fun <B> map(f: (A) -> B): Node<B> {
        fun Array<Wrapper<A>>.map(g: (A) -> B): Array<Wrapper<B>> =
                Array(this.size, {idx: Int -> Wrapper(g(this[idx].wrapped))})

        fun Array<Node<A>>.map(g: (A) -> B): Array<Node<B>> =
                Array(this.size, {idx: Int -> this[idx].map(g)})

        return when(this) {
            is EmptyNode -> EmptyNode()
            is RootNode -> {
                val tailP: List<B> = this.tail.map(f)
                val vecP: Array<Node<B>> = this.vecPtrs.map(f)
                RootNode(this.size, this.shift, this.offset, this.capacity, tailP, vecP)
            }
            is InternalNode -> InternalNode(this.vecPtrs.map(f))
            is DataNode -> DataNode(this.data.map(f))
        }
    }   // map

    /**
     * Reverses the content of this vector into a new vector.
     *
     * @return                  new vector of elements reversed
     */
    fun reverse(): Node<A> = NodeF.from(this.foldLeft(ListF.empty()){xs: List<A>, a: A -> ListF.cons(a, xs)})



    /**
     * foldLeft is a higher-order function that folds a binary function into this
     *   vector of values.
     *
     * @param e                 initial value
     * @param f                 curried binary function:: B -> A -> B
     * @return                  folded result
     */
    fun <B> foldLeft(e: B, f: (B) -> (A) -> B): B {
        fun isNotSliced(node: RootNode<A>): Boolean = (node.offset == 0) && (node.capacity < node.size)
        fun go(triple: Triple<B, Int, Int>, node: Node<A>): Triple<B, Int, Int> {
            fun Array<Wrapper<A>>.boundedFoldLeft(g: (B) -> (A) -> B, start: Int, end: Int, b: B): B {
                fun recBoundedFoldLeft(array: Array<Wrapper<A>>, n: Int, i: Int, z: B): B =
                        if (i >= n)
                            z
                        else
                            recBoundedFoldLeft(array, n, 1 + i, g(z)(array[i].wrapped))

                return recBoundedFoldLeft(this, Math.min(end, this.size), Math.max(0, start), b)
            }   // recBoundedFoldLeft

            fun Array<Node<A>>.foldLeft(b: Triple<B, Int, Int>, g: (Triple<B, Int, Int>) -> (Node<A>) -> Triple<B, Int, Int>): Triple<B, Int, Int> {
                var res: Triple<B, Int, Int> = b
                for (idx: Int in this.indices)
                    res = g(res)(this[idx])
                return res
            }   // foldLeft

            val seed: B = triple.first
            val nskip: Int = triple.second
            val len: Int = triple.third

            return when(node) {
                is EmptyNode -> triple
                is RootNode -> {
                    val goC: (Triple<B, Int, Int>) -> (Node<A>) -> Triple<B, Int, Int> = {tripleBII: Triple<B, Int, Int> -> {node: Node<A> -> go(tripleBII, node)}}
                    val tripleP: Triple<B, Int, Int> = node.vecPtrs.foldLeft(Triple(seed, nskip, len - node.tail.size()), goC)
                    val flipf: (A) -> (B) -> B = FunctionF.flip(f)
                    Triple(node.tail.foldRight(tripleP.first, flipf), 0, 0)
                }
                is InternalNode -> {
                    val goC: (Triple<B, Int, Int>) -> (Node<A>) -> Triple<B, Int, Int> = {tripleBII: Triple<B, Int, Int> -> {node: Node<A> -> go(tripleBII, node)}}
                    node.vecPtrs.foldLeft(triple, goC)
                }
                is DataNode -> {
                    if (len <= 0)
                        Triple(seed, 0, 0)
                    else if (nskip == 0)
                        Triple(node.data.boundedFoldLeft(f, 0, Math.min(len, NodeF.chunk), seed), 0, len - node.data.size)
                    else if (nskip >= NodeF.chunk)
                        Triple(seed, nskip - NodeF.chunk, len)
                    else {
                        val end: Int = Math.min(NodeF.chunk, len + nskip)
                        val start: Int = nskip
                        val taken: Int = end - Math.max(0, start)
                        Triple(node.data.boundedFoldLeft(f, start, end, seed), 0, len - taken)
                    }
                }
            }
        }   // go

        fun sgo(seed: B, node: Node<A>): B {
            fun Array<Wrapper<A>>.foldLeft(b: B, g: (B) -> (A) -> B): B {
                var res: B = b
                for (idx: Int in this.indices)
                    res = g(res)(this[idx].wrapped)
                return res
            }   // foldLeft

            fun Array<Node<A>>.foldLeft(b: B, g: (B) -> (Node<A>) -> B): B {
                var res: B = b
                for (idx: Int in this.indices)
                    res = g(res)(this[idx])
                return res
            }   // foldLeft

            return when(node) {
                is EmptyNode -> seed
                is RootNode -> {
                    val sgoC: (B) -> (Node<A>) -> B = {b: B -> {n: Node<A> -> sgo(b, n)}}
                    val rseed: B = node.vecPtrs.foldLeft(seed, sgoC)
                    val flipf: (A) -> (B) -> B = FunctionF.flip(f)
                    node.tail.foldRight(rseed, flipf)
                }
                is InternalNode -> {
                    val sgoC: (B) -> (Node<A>) -> B = {b: B -> {n: Node<A> -> sgo(b, n)}}
                    node.vecPtrs.foldLeft(seed, sgoC)
                }
                is DataNode -> node.data.foldLeft(seed, f)
            }
        }   // sgo

        return when(this) {
            is RootNode -> {
                if (isNotSliced(this))
                    sgo(e, this)
                else
                    go(Triple(e, this.offset, this.size()), this).first
            }
            else -> sgo(e, this)
        }
    }   // foldLeft

    /**
     * foldLeft is a higher-order function that folds a binary function into this
     *   vector of values.
     *
     * @param e                 initial value
     * @param f                 binary function:: T * V -> T
     * @return                  folded result
     */
    fun <B> foldLeft(e: B, f: (B, A) -> B): B = this.foldLeft(e, FunctionF.C(f))

    /**
     * foldRight is a higher-order function that folds a binary function into this
     *   vector of values. Fold functions can be the implementation for many other
     *   functions.
     *
     * @param e                 initial value
     * @param f                 curried binary function:: A -> B -> B
     * @return                  folded result
     */
    fun <B> foldRight(e: B, f: (A) -> (B) -> B): B {
        fun isNotSliced(node: RootNode<A>): Boolean = (node.offset == 0) && (node.capacity < node.size)

        fun Array<Wrapper<A>>.boundedFoldRight(g: (A) -> (B) -> B, start: Int, end: Int, b: B): B {
            fun recBoundedFoldRight(array: Array<Wrapper<A>>, n: Int, i: Int, z: B): B =
                    if (i >= n)
                        z
                    else
                        g(this[i].wrapped)(recBoundedFoldRight(array, n, 1 + i, z))

            return recBoundedFoldRight(this, Math.min(end, this.size), Math.max(0, start), b)
        }   // boundedFoldRight

        fun Array<Node<A>>.foldRight(b: Triple<B, Int, Int>, g: (Node<A>) -> (Triple<B, Int, Int>) -> Triple<B, Int, Int>): Triple<B, Int, Int> {
            var res: Triple<B, Int, Int> = b
            for (idx: Int in this.size - 1 downTo 0)
                res = g(this[idx])(res)
            return res
        }   // foldRight

        fun go(triple: Triple<B, Int, Int>, node: Node<A>): Triple<B, Int, Int> {
            val seed: B = triple.first
            val nskip: Int = triple.second
            val len: Int = triple.third
            ////println("go: seed: ${seed} nskip: ${nskip} len: ${len}")

            return when(node) {
                is EmptyNode -> triple
                is RootNode -> {
                    val goC: (Node<A>) -> (Triple<B, Int, Int>) -> Triple<B, Int, Int> = {n: Node<A> -> {t: Triple<B, Int, Int> -> go(t, n)}}
                    val flipf: (B) -> (A) -> B = FunctionF.flip(f)
                    val tseed: B = node.tail.foldLeft(seed, flipf)
                    ////println("  RootNode: tseed: ${tseed}")
                    node.vecPtrs.foldRight(Triple(tseed, nskip, len - node.tail.size()), goC)
                }
                is InternalNode -> {
                    ////println("  InternalNode: ${node.toGraph()}")
                    val goC: (Node<A>) -> (Triple<B, Int, Int>) -> Triple<B, Int, Int> = {n: Node<A> -> {t: Triple<B, Int, Int> -> go(t, n)}}
                    node.vecPtrs.foldRight(triple, goC)
                }
                is DataNode -> {
                    ////println("  DataNode: ${node.toGraph()}")
                    if (len <= 0)
                        Triple(seed, 0, 0)
                    else if (nskip == 0)
                        Triple(node.data.boundedFoldRight(f, (NodeF.chunk - len), NodeF.chunk, seed), 0, len - node.data.size)
                    else if (nskip >= NodeF.chunk)
                        Triple(seed, nskip - NodeF.chunk, len)
                    else {
                        val end: Int = Math.min(Math.max(0, NodeF.chunk - nskip), NodeF.chunk)
                        val start: Int = NodeF.chunk - (len + nskip)
                        val taken: Int = end - Math.max(0, start)
                        Triple(node.data.boundedFoldRight(f, start, end, seed), 0, len - taken)
                    }
                }
            }
        }   // go

        fun sgo(seed: B, node: Node<A>): B {
            fun Array<Wrapper<A>>.foldRight(b: B, g: (A) -> (B) -> B): B {
                var res: B = b
                for (idx: Int in this.size - 1 downTo 0)
                    res = g(this[idx].wrapped)(res)
                return res
            }   // foldRight

            fun Array<Node<A>>.foldRight(b: B, g: (Node<A>) -> (B) -> B): B {
                var res: B = b
                for (idx: Int in this.size - 1 downTo 0)
                    res = g(this[idx])(res)
                return res
            }   // foldRight

            return when(node) {
                is EmptyNode -> seed
                is RootNode -> {
                    val sgoC: (Node<A>) -> (B) -> B = {n: Node<A> -> {b: B -> sgo(b, n)}}
                    val flipf: (B) -> (A) -> B = FunctionF.flip(f)
                    val tseed: B = node.tail.foldLeft(seed, flipf)
                    node.vecPtrs.foldRight(tseed, sgoC)
                }
                is InternalNode -> {
                    val sgoC: (Node<A>) -> (B) -> B = {n: Node<A> -> {b: B -> sgo(b, n)}}
                    node.vecPtrs.foldRight(seed, sgoC)
                }
                is DataNode -> node.data.foldRight(seed, f)
            }
        }   // sgo

        return when(this) {
            is RootNode -> {
                if (isNotSliced(this))
                    sgo(e, this)
                else
                    go(Triple(e, Math.max(0, this.capacity - this.size), this.size()), this).first
            }
            else -> sgo(e, this)
        }
    }   // foldRight


    /**
     * foldRight is a higher-order function that folds a binary function into this
     *   vector of values. Fold functions can be the implementation for many other
     *   functions.
     *
     * @param e                 initial value
     * @param f                 binary function:: A * B -> B
     * @return                  folded result
     */
    fun <B> foldRight(e: B, f: (A, B) -> B): B = this.foldRight(e, FunctionF.C(f))



// ---------- special folds -------------------------------

    /**
     * All the elements of this vector meet some criteria. If the vector is empty then
     *    true is returned.
     *
     * @param predicate         criteria
     * @return                  true if all elements match criteria
     */
    fun forAll(predicate: (A) -> Boolean): Boolean {
        return when(this) {
            is EmptyNode -> true
            is RootNode -> {
                val all: Boolean = this.vecPtrs.all{node: Node<A> -> node.forAll(predicate)}
                if (all)
                    this.tail.forAll(predicate)
                else
                    false
            }
            is InternalNode -> this.vecPtrs.all{node: Node<A> -> node.forAll(predicate)}
            is DataNode -> this.data.all{wrap: Wrapper<A> -> predicate(wrap.wrapped)}
        }
    }   // forAll

    /**
     * There exists at least one element of this vector that meets some criteria. If
     *   the vector is empty then false is returned.
     *
     * @param predicate         criteria
     * @return                  true if at least one element matches the criteria
     */
    fun thereExists(predicate: (A) -> Boolean): Boolean {
        return when(this) {
            is EmptyNode -> false
            is RootNode -> {
                val any: Boolean = this.vecPtrs.any{node: Node<A> -> node.thereExists(predicate)}
                if (any)
                    true
                else
                    this.tail.thereExists(predicate)
            }
            is InternalNode -> this.vecPtrs.any{node: Node<A> -> node.thereExists(predicate)}
            is DataNode -> this.data.any{wrap: Wrapper<A> -> predicate(wrap.wrapped)}
        }
    }   // thereExists

    /**
     * There exists only one element of this vector that meets some criteria. If the
     *   vector is empty then false is returned.
     *
     * @param predicate         criteria
     * @return                  true if only one element matches the criteria
     */
    fun thereExistsUnique(predicate: (A) -> Boolean): Boolean =
            (this.count(predicate) == 1)



// ---------- building vectors ----------------------------

    /**
     * scanLeft is similar to foldLeft, but returns a vector of successively
     *   reduced values from the left.
     *
     * Examples:
     *   [4, 2, 4].scanLeft(64){m -> {n -> m / y}} = [64, 16, 8, 2]
     *   [].scanLeft(3){m -> {n -> m / y}} = [3]
     *   [1, 2, 3, 4].scanLeft(5){m -> {n -> if (m > n) m else n}} = [5, 5, 5, 5, 5]
     *   [1, 2, 3, 4, 5, 6, 7].scanLeft(5){m -> {n -> if (m > n) m else n}} = [5, 5, 5, 5, 5, 5, 6, 7]
     *
     * @param f                 curried binary function
     * @param e                 initial value
     * @return                  new vector
     */
    fun <B> scanLeft(e: B, f: (B) -> (A) -> B): Node<B> {
        var b: B = e
        var node: Node<B> = NodeF.singleton((e))
        val iterator: Iterator<A> = this.iterator()
        while (iterator.hasNext()) {
            val elem: A = iterator.next()
            b = f(b)(elem)
            node = node.append(b)
        }

        return node
    }   // scanLeft

    /**
     * scanRight is the right-to-left dual of scanLeft.
     *
     * Examples:
     *   [1, 2, 3, 4].scanRight(5){m -> {n -> m + n}} = [15, 14, 12, 9, 5]
     *   [8, 12, 24, 4].scanRight(2){m -> {n -> m / n}} = [8, 1, 12, 2, 2]
     *   [].scanRight(3){m -> {n -> m / n}} = [3]
     *   [3, 6, 12, 4, 55, 11].scanRight(18){m -> {n -> if (m > n) m else n}} = [55, 55, 55, 55, 55, 18, 18]
     *
     * @param e                 initial value
     * @param f                 curried binary function
     * @return                  new vector
     */
    fun <B> scanRight(e: B, f: (A) -> (B) -> B): Node<B> {
        val list: List<A> = this.foldRight(ListF.empty()){elem, list -> ListF.cons(elem, list)}
        val scannedList: List<B> = list.scanRight(e, f)
        return NodeF.from(scannedList)
    }   // scanRight



// ---------- extracting sublists -------------------------

    /**
     * Return a new vector containing the first n elements from this vector. If n
     *    exceeds the size of this vector, then a copy is returned. If n is
     *    negative or zero, then an empty vector is delivered.
     *
     * @param n                 number of elements to extract
     * @return                  new vector of first n elements
     */
    fun take(n: Int): Node<A> = this.slice(0, n)

    /**
     * Drop the first n elements from this vector and return a vector containing the
     *   remainder. If n is negative or zero then this vector is returned. If n is
     *   negative or zero, then an empty vector is delivered.
     *
     * @param n                 number of elements to skip
     * @return                  new vector of remaining elements
     */
    fun drop(n: Int): Node<A> = this.slice(n, this.size() - n)

    /**
     * Return a slice of this vector of the given length starting at the given start
     *   position. A slice of negative or zero length is the empty vector.
     *
     * @param start             start index for the slice
     * @param length            length of the slice
     * @return                  slice of this vector
     */
    fun slice(start: Int, length: Int): Node<A> {
        ////println("slice: start: ${start} length: ${length}")
        return when(this) {
            is EmptyNode -> EmptyNode<A>()
            is RootNode -> {
                ////println("  slice: RootNode(size: ${this.size} shift: ${this.shift} offset: ${this.offset} capacity: ${this.capacity})")
                val toff: Int = NodeF.tailOffset(this)
                val len: Int = Math.max(0, Math.min(length, this.size - start))
                ////println("  slice: toff: ${toff} len: ${len}")
                ////println("  slice: tail: ${tail}")

                if (len <= 0)
                    EmptyNode<A>()
                else if (toff < start) {
                    val tailP: List<A> = this.tail.reverse().drop(start - toff).take(length).reverse()
                    RootNode(tailP.size(), this.shift, 0, 0, tailP, arrayOf<Node<A>>())
                } else if (start < 0) {
                    val elementsRetained: Int = Math.min(len + start, this.size)
                    RootNode(elementsRetained, this.shift, this.offset, this.capacity, this.tail.drop(this.size - elementsRetained), this.vecPtrs)
                } else {
                    val newOff: Int = this.offset + start
                    val newSize = Math.min(newOff + len, this.size)
                    val nTake: Int = Math.max(0, start - this.capacity)
                    val tailP: List<A> = this.tail.drop(this.size - newSize)
                    ////println("  slice: newOff: ${newOff} newSize: ${newSize} nTake: ${nTake}")
                    ////println("  slice: tailP: ${tailP}")
                    RootNode(newSize, this.shift, newOff, this.capacity, tailP.take(tailP.size() - nTake), this.vecPtrs)
                }
            }
            is InternalNode -> throw VectorException("InternalNode.slice: internal nodes should not be exposed")
            is DataNode -> throw VectorException("DataNode.slice: internal nodes should not be exposed")
        }
    }   // slice

    /**
     * Delivers a tuple where first element is prefix of this vector of length n and
     *   second element is the remainder of the vector.
     *
     * @param n                 number of elements into first result vector
     * @return                  pair of two new vectors
     */
    fun splitAt(n: Int): Pair<Node<A>, Node<A>> = Pair(this.take(n), this.drop(n))

    /**
     * Function takeWhile takes the leading elements from this vector that matches
     *   some predicate.
     *
     * @param predicate         criteria
     * @return                  new vector of leading elements matching criteria
     */
    fun takeWhile(predicate: (A) -> Boolean): Node<A> {
        val elements: List<A> = this.foldLeft(ListF.empty()){vs: List<A>, a: A -> vs.append(a)}
        return elements.takeWhile(predicate).foldLeft(NodeF.empty()){node: Node<A>, a: A -> node.append(a)}
    }

    /**
     * Function dropWhile removes the leading elements from this vector that matches
     *   some predicate.
     *
     * @param predicate         criteria
     * @return                  new vector of remaining elements
     */
    fun dropWhile(predicate: (A) -> Boolean): Node<A> {
        val elements: List<A> = this.foldLeft(ListF.empty()){vs: List<A>, a: A -> vs.append(a)}
        return elements.dropWhile(predicate).foldLeft(NodeF.empty()){node: Node<A>, a: A -> node.append(a)}
    }

    /**
     * span applied to a predicate and a vector xs, returns a tuple where
     *   the first element is longest prefix (possibly empty) of xs of elements
     *   that satisfy predicate and second element is the remainder of the list,
     *   eg: span({ x -> (x < 3) }, [1, 2, 3, 4, 1, 2, 3, 4]) == ([1, 2], [3, 4 ,1, 2, 3, 4])
     *       span({ x -> (x < 9) }, [1, 2, 3]) == ([1, 2, 3], [])
     *       span({ x -> (x < 0) }, [1, 2, 3]) == ([], [1,2,3])
     *
     * @param predicate         criteria
     * @return                  pair of two new vectors
     */
    fun span(predicate: (A) -> Boolean): Pair<Node<A>, Node<A>> {
        val elements: List<A> = this.foldLeft(ListF.empty()){vs: List<A>, a: A -> vs.append(a)}
        val pair: Pair<List<A>, List<A>> = elements.span(predicate)
        val v1: Node<A> = pair.first.foldLeft(NodeF.empty()){node: Node<A>, a: A -> node.append(a)}
        val v2: Node<A> = pair.second.foldLeft(NodeF.empty()){node: Node<A>, a: A -> node.append(a)}
        return Pair(v1, v2)
    }   // span



// ---------- predicates ----------------------------------

    /**
     * The isPrefixOf function returns true iff this vector is a prefix of the parameter vector.
     *
     * @param xs                existing vector
     * @return                  true if this vector is prefix of given vector
     */
    fun isPrefixOf(xs: Node<A>): Boolean {
        val elements: List<A> = this.foldLeft(ListF.empty()){vs: List<A>, a: A -> vs.append(a)}
        val xsElements: List<A> = xs.foldLeft(ListF.empty()){vs: List<A>, a: A -> vs.append(a)}
        return elements.isPrefixOf(xsElements)
    }

    /**
     * The isSuffixOf function returns true iff the this vector is a suffix of the second.
     *
     * @param xs                existing vector
     * @return                  true if this vector is suffix of given vector
     */
    fun isSuffixOf(xs: Node<A>): Boolean {
        val elements: List<A> = this.foldLeft(ListF.empty()){vs: List<A>, a: A -> vs.append(a)}
        val xsElements: List<A> = xs.foldLeft(ListF.empty()){vs: List<A>, a: A -> vs.append(a)}
        return elements.isSuffixOf(xsElements)
    }

    /**
     * The isInfixOf function returns true iff the this vector is a constituent of the argument.
     *
     * @param xs                existing vector
     * @return                  true if this vector is constituent of second vector
     */
    fun isInfixOf(xs: Node<A>): Boolean {
        val elements: List<A> = this.foldLeft(ListF.empty()){vs: List<A>, a: A -> vs.append(a)}
        val xsElements: List<A> = xs.foldLeft(ListF.empty()){vs: List<A>, a: A -> vs.append(a)}
        return elements.isInfixOf(xsElements)
    }



// ---------- searching with a predicate ------------------

    /**
     * The find function takes a predicate and returns the first
     *   element in the vector matching the predicate, or None if there is no
     *   such element.
     *
     * @param predicate         criteria
     * @return                  matching element, if found
     */
    fun find(predicate: (A) -> Boolean): Option<A> {
        return when(this) {
            is EmptyNode -> none()
            is RootNode -> {
                var found: Option<A> = none()
                for (index: Int in this.vecPtrs.indices) {
                    if (found.isEmpty()) {
                        val opt: Option<A> = this.vecPtrs[index].find(predicate)
                        found = opt
                    }
                }

                if (found.isEmpty()) {
                    this.tail.reverse().find(predicate)
                } else
                    found
            }
            is InternalNode -> {
                var found: Option<A> = none()
                for (index: Int in this.vecPtrs.indices) {
                    if (found.isEmpty()) {
                        val opt: Option<A> = this.vecPtrs[index].find(predicate)
                        found = opt
                    }
                }
                found
            }
            is DataNode -> {
                var found: Option<A> = none()
                for (index: Int in this.data.indices) {
                    if (found.isEmpty()) {
                        if (predicate(this.data[index].wrapped))
                            found = some(this.data[index].wrapped)
                    }
                }
                found
            }
        }
    }   // find

    /**
     * Function filter selects the items from this vector that match the criteria specified
     *   by the function parameter. This is known as a predicate function, and
     *   delivers a boolean result.
     *
     * @param predicate         criteria
     * @return                  new vector of matching elements
     */
    fun filter(predicate: (A) -> Boolean): Node<A> {
        val goC: (Node<A>) -> (A) -> Node<A> = {acc: Node<A> -> {a: A -> if (predicate(a)) acc.append(a) else acc}}

        return this.foldLeft(NodeF.empty(), goC)
    }

    /**
     * The partition function takes a predicate and returns the pair
     *   of vector of elements which do and do not satisfy the predicate.
     *
     * @param predicate         criteria
     * @return                  pair of new vectors
     */
    fun partition(predicate: (A) -> Boolean): Pair<Node<A>, Node<A>> {
        val goC: (Pair<Node<A>, Node<A>>) -> (A) -> Pair<Node<A>, Node<A>> = {pr: Pair<Node<A>, Node<A>> ->
            {a: A -> if (predicate(a)) Pair(pr.first.append(a), pr.second) else Pair(pr.first, pr.second.append(a))}
        }

        return this.foldLeft(Pair(NodeF.empty(), NodeF.empty()), goC)
    }



// ---------- zipping -------------------------------------

    /**
     * zip returns a list of corresponding pairs from this vector and the argument vector.
     *   If one input vector is shorter, excess elements of the longer vector are discarded.
     *
     * @param xs                existing vector
     * @return                  new vector of pairs
     */
    fun <B> zip(xs: Node<B>): Node<Pair<A, B>> {
        val elements: List<A> = this.foldLeft(ListF.empty()){vs: List<A>, a: A -> vs.append(a)}
        val xsElements: List<B> = xs.foldLeft(ListF.empty()){vs: List<B>, b: B -> vs.append(b)}
        val zipped: List<Pair<A, B>> = elements.zip(xsElements)
        return zipped.foldLeft(NodeF.empty()){node: Node<Pair<A, B>>, pr: Pair<A, B> -> node.append(pr)}
    }

    /**
     * zipWith generalises zip by zipping with the function given as the final argument,
     *   instead of a tupling function. For example, zipWith (+) is applied to two vectors
     *   to produce the vectors of corresponding sums.
     *
     * @param xs                existing list
     * @param f                 curried binary function
     * @return                  new list of function results
     */
    fun <B, C> zipWith(xs: Node<B>, f: (A) -> (B) -> C): Node<C> {
        val elements: List<A> = this.foldLeft(ListF.empty()){vs: List<A>, a: A -> vs.append(a)}
        val xsElements: List<B> = xs.foldLeft(ListF.empty()){vs: List<B>, b: B -> vs.append(b)}
        val zipped: List<C> = elements.zipWith(xsElements, f)
        return zipped.foldLeft(NodeF.empty()){node: Node<C>, c: C -> node.append(c)}
    }

    /**
     * zipWith generalises zip by zipping with the function given as the first argument,
     *   instead of a tupling function. For example, zipWith (+) is applied to two vectors
     *   to produce the vectors of corresponding sums.
     *
     * @param xs                existing list
     * @param f                 binary function
     * @return                  new list of function results
     */
    fun <B, C> zipWith(xs: Node<B>, f: (A, B) -> C): Node<C> = this.zipWith(xs, FunctionF.C(f))

    /**
     * Zips this vector with the index of its element as a pair.
     *
     * @return                  a new vector with the same length as this vector
     */
    fun zipWithIndex(): Node<Pair<A, Int>> {
        return if (this.isEmpty())
            NodeF.empty()
        else {
            val elements: List<A> = this.foldLeft(ListF.empty()){vs: List<A>, a: A -> vs.append(a)}
            val indices: List<Int> = ListF.range(0, elements.size())
            val zipped: List<Pair<A, Int>> = elements.zip(indices)
            zipped.foldLeft(NodeF.empty()){node: Node<Pair<A, Int>>, pr: Pair<A, Int> -> node.append(pr)}
        }
    }



// ---------- implementation ------------------------------

    private fun getUnSafe(index: Int, node: Node<A>): A {
        if (node is RootNode<A>) {
            val idx: Int = index + node.offset
            fun recGetUnSafe(level: Int, vec: Node<A>): A {
                return if (level == 0 && vec is DataNode<A>)
                    vec.data[(idx and 0x1F)].wrapped
                else if (vec is RootNode<A>) {
                    val nextIdx: Int = ((idx shr level) and 0x1F)
                    val vecP: Array<Node<A>> = vec.vecPtrs
                    recGetUnSafe(level - NodeF.shiftStep, vecP[nextIdx])
                } else if (vec is InternalNode<A>) {
                    val nextIdx: Int = ((idx shr level) and 0x1F)
                    val vecP: Array<Node<A>> = vec.vecPtrs
                    recGetUnSafe(level - NodeF.shiftStep, vecP[nextIdx])
                } else
                    throw VectorException("recGetUnSafe: not a root or data or internal node")
            }   // recGetUnSafe

            return if (idx >= tailOffset(node) && node.capacity < node.size)
                node.tail.reverse().get((idx and 0x1F))
            else
                recGetUnSafe(node.shift, node)
        } else
            throw VectorException("getUnSafe: not a root node")
    }

    private fun tailOffset(node: Node<A>): Int {
        return if (node is EmptyNode)
            0
        else if (node is RootNode<A>) {
            val len: Int = node.size
            if (len < 32)
                0
            else
                (len - 1) shr NodeF.shiftStep shl NodeF.shiftStep
        } else
            throw VectorException("tailOffset: internal nodes should not be exposed")
    }

    /**
     * Replace the element at the given index with the given value.
     *
     * @param index             replace index position
     * @param v                 replacement value
     * @return                  updated vector
     */
    private fun replaceElement(index: Int, a: A): Node<A> {
        fun Array<Wrapper<A>>.replace(index: Int, element: Wrapper<A>): Array<Wrapper<A>> =
                Array(this.size, {idx: Int -> if (idx == index) element else this[idx]})

        fun Array<Node<A>>.replace(index: Int, node: Node<A>): Array<Node<A>> =
                Array(this.size, {idx: Int -> if (idx == index) node else this[idx]})

        ////println("replaceElement: index: ${index}, a: ${a}; node: ${this.toGraph()}")
        return when(this) {
            is EmptyNode -> EmptyNode()
            is RootNode -> {
                val idx: Int = index + this.offset
                val toff: Int = tailOffset(this)

                ////println("  replaceElement Root: offset: ${this.offset}, size: ${this.size}, capacity: ${this.capacity}, idx: ${idx}, toff: ${toff}}")
                fun go(level: Int, nodes: Array<Node<A>>): Array<Node<A>> {
                    val vidx: Int = ((idx shr level) and 0x1F)
                    val nodeP: Node<A> = nodes[vidx]
                    return if (level == NodeF.shiftStep) {
                        when(nodeP) {
                            is DataNode -> {
                                val dnode: DataNode<A> = DataNode(nodeP.data.replace((idx and 0x1F), Wrapper(a)))
                                nodes.replace(vidx, dnode)
                            }
                            else -> throw VectorException("RootNode.replaceElement: not a DataNode")
                        }
                    } else {
                        when(nodeP) {
                            is RootNode -> {
                                val rnodes: Array<Node<A>> = go(level - NodeF.shiftStep, nodeP.vecPtrs)
                                nodes.replace(vidx, InternalNode(rnodes))
                            }
                            is InternalNode -> {
                                val rnodes: Array<Node<A>> = go(level - NodeF.shiftStep, nodeP.vecPtrs)
                                nodes.replace(vidx, InternalNode(rnodes))
                            }
                            else -> throw VectorException("RootNode.replaceElement: not a RootNode or InternalNode")
                        }
                    }
                }   // go

                if (this.size <= idx || idx < 0)
                    this
                else if (idx >= toff && this.capacity < this.size) {
                    when(this.tail) {
                        is Nil -> RootNode(this.size, this.shift, this.offset, this.capacity, ListF.singleton(a), this.vecPtrs)
                        is Cons -> {
                            val tidx: Int = this.size - 1 - idx
                            val pair: Pair<List<A>, List<A>> = this.tail.splitAt(tidx)
                            RootNode(this.size, this.shift, this.offset, this.capacity, pair.first.append(ListF.cons(a, pair.second.tail())), this.vecPtrs)
                        }
                    }
                } else
                    RootNode(this.size, this.shift, this.offset, this.capacity, this.tail, go(this.shift, this.vecPtrs))
            }
            is InternalNode -> throw VectorException("InternalNode.replaceElement: internal nodes should not be exposed")
            is DataNode -> throw VectorException("DataNode.replaceElement: internal nodes should not be exposed")
        }
    }   // replaceElement

}   // Node
