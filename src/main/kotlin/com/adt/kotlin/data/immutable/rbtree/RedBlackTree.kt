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

import com.adt.kotlin.data.immutable.option.Option
import com.adt.kotlin.data.immutable.option.OptionF
import com.adt.kotlin.data.immutable.option.bind

import com.adt.kotlin.hkfp.fp.FunctionF.C2
import com.adt.kotlin.hkfp.fp.FunctionF.C3



sealed class RedBlackTree<K : Comparable<K>, V> {



    class EmptyTree<K : Comparable<K>, V> : RedBlackTree<K, V>() {

        override fun toString(): String = "EmptyTree"

    }   // EmptyTree



    sealed class RBTree<K : Comparable<K>, V>(val key: K, val value: V, val left: RedBlackTree<K, V>, val right: RedBlackTree<K, V>) : RedBlackTree<K, V>() {



        class RedTree<K : Comparable<K>, V>(key: K, value: V, left: RedBlackTree<K, V>, right: RedBlackTree<K, V>) : RBTree<K, V>(key, value, left, right) {

            override val black: RBTree<K, V>
                get() {
                    return BlackTree(key, value, left, right)
                }

            override val red: RBTree<K, V>
                get() {
                    return this
                }

            override fun toString(): String = "RedTree(${key}, ${value}, ${left}, ${right})"

        }   //RedTree



        class BlackTree<K : Comparable<K>, V>(key: K, value: V, left: RedBlackTree<K, V>, right: RedBlackTree<K, V>) : RBTree<K, V>(key, value, left, right) {

            override val black: RBTree<K, V>
                get() {
                    return this
                }

            override val red: RBTree<K, V>
                get() {
                    return RedTree(key, value, left, right)
                }

            override fun toString(): String = "BlackTree(${key}, ${value}, ${left}, ${right})"

        }   // BlackTree



        abstract val black: RedBlackTree<K, V>
        abstract val red: RedBlackTree<K, V>

    }   // RBTree



    /**
     * Determine if the tree contains the given key.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.containsKey("Ken") = true
     *   <[Jessie: 22, John: 31, Ken: 25]>.containsKey("Irene") = false
     *   <[]>.containsKey("Ken") = false
     *
     * @param key               search key
     * @return                  true if the tree contains this key
     */
    fun containsKey(key: K): Boolean {
        val tree: RedBlackTree<K, V> = RedBlackTreeF.lookUpTree(this, key)
        return (tree !is EmptyTree)
    }   // containsKey

    /**
     * Determine if the tree contains the given key.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.containsKey{k -> (k == "Ken")} = true
     *   <[Jessie: 22, John: 31, Ken: 25]>.containsKey{k -> (k == "Irene")} = false
     *   <[]>.containsKey{k -> (k == "Ken")} = false
     *
     * @param predicate         search criteria
     * @return                  true if the tree contains this key
     */
    fun containsKey(predicate: (K) -> Boolean): Boolean {
        val tree: RedBlackTree<K, V> = RedBlackTreeF.lookUpTree(this, predicate)
        return (tree !is EmptyTree)
    }   // containsKey

    /**
     * Obtain the size of the tree.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.count = 3
     *   <[]>.count = 0
     *
     * @return                  the number of elements in the tree
     */
    val count: Int
        get() {
            return when(this) {
                is EmptyTree -> 0
                is RBTree -> 1 + RedBlackTreeF.treeCount(left) + RedBlackTreeF.treeCount(right)
            }
        }   // count

    /**
     * Delete the key/value from the tree. When the key is not a member
     *   of the tree, the original tree is returned.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.delete(Ken) = <[Jessie: 22, John: 31]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.delete(Irene) = <[Jessie: 22, John: 31, Ken: 25]>
     *   <[]>.delete(Ken) = <[]>
     *
     * @param key               key to be removed
     * @return                  new map without the given key
     */
    fun delete(key: K): RedBlackTree<K, V> = RedBlackTreeF.blacken(RedBlackTreeF.del(this, key))

    /**
     * The difference of two trees. Returns elements from this not in the
     *   right tree.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.difference(<[Irene: 25, Dawn: 31]>) = <[Jessie: 22, John: 31, Ken: 25]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.difference(<[John: 41, Ken: 35]>) = <[Jessie: 22]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.difference(<[]>) = <[Jessie: 22, John: 31, Ken: 25]>
     *   <[]>.difference(<[Jessie: 22, John: 31, Ken: 25]>) = <[]>
     *
     * @param tree                  existing tree
     * @return                      the difference of this tree and the given tree
     */
    fun difference(tree: RedBlackTree<K, V>): RedBlackTree<K, V> =
        this.foldLeftWithKey(RedBlackTreeF.empty()){acc, key, value ->
            if (!tree.containsKey(key))
                acc.update(key, value, true)
            else
                acc
        }   // difference

    /**
     * Drop the smaller n elements from the tree. If n is negative, then return
     *   the original tree. If n exceeds or equals the number of elements in the
     *   tree, then return an empty tree.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.drop(1) = <[John: 31, Ken: 25]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.drop(4) = null
     *
     * @param n                 the number of elements to drop
     * @return                  the new tree
     */
    fun drop(n: Int): RedBlackTree<K, V> = RedBlackTreeF.blacken(RedBlackTreeF.drop(this, n))

    /**
     * Return an iterator over key/value elements of type Pair<K, V>.
     */
    fun entriesIterator(): EntriesIterator<K, V> = EntriesIterator(this)

    /**
     * Are two trees equal?
     *
     * @param other             the other tree
     * @return                  true if both trees are the same; false otherwise
     */
    override fun equals(other: Any?): Boolean {
        /***** TODO fun recEquals(pt: RedBlackTree<K, V>, qt: RedBlackTree<K, V>): Boolean {
        return when(pt) {
        is EmptyTree -> {
        when(qt) {
        is EmptyTree -> true
        is RBTree -> false
        }
        }
        is RBTree.RedTree -> {
        when(qt) {
        is EmptyTree -> false
        is RBTree.RedTree -> {
        if (pt.key == qt.key && pt.value == qt.value)
        recEquals(pt.left, qt.left) && recEquals(pt.right, qt.right)
        else
        false
        }
        is RBTree.BlackTree -> false
        }
        }
        is RBTree.BlackTree -> {
        when(qt) {
        is EmptyTree -> false
        is RBTree.RedTree -> false
        is RBTree.BlackTree -> {
        if (pt.key == qt.key && pt.value == qt.value)
        recEquals(pt.left, qt.left) && recEquals(pt.right, qt.right)
        else
        false
        }
        }
        }
        }
        }   //recEquals
         *****/

        return if (this === other)
            true
        else if (other == null || this::class.java != other::class.java)
            false
        else {
            @Suppress("UNCHECKED_CAST") val otherTree: RedBlackTree<K, V> = other as RedBlackTree<K, V>
            (RedBlackTreeF.toList(this) == RedBlackTreeF.toList(otherTree))
            //recEquals(this, otherTree)
        }
    }

    /**
     * foldLeft is a higher-order function that folds a left associative binary
     *   function into the values of a tree.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldLeft(0){res -> {age -> res + age}} = 78
     *   <[]>.foldLeft(0){res -> {age -> res + age}} = 0
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldLeft([]){res -> {age -> res.append(age)}} = [22, 31, 25]
     *
     * @param e           	    initial value
     * @param f         		curried binary function:: B -> V -> B
     * @return            	    folded result
     */
    fun <B> foldLeft(e: B, f: (B) -> (V) -> B): B {
        fun recFoldLeft(tree: RedBlackTree<K, V>, e: B, f: (B) -> (V) -> B): B {
            return when(tree) {
                is EmptyTree -> e
                is RBTree -> recFoldLeft(tree.right, f(recFoldLeft(tree.left, e, f))(tree.value), f)
            }
        }   // recFoldLeft

        return recFoldLeft(this, e, f)
    }   // foldLeft

    /**
     * foldLeft is a higher-order function that folds a left associative binary
     *   function into the values of a tree.
     *
     * Examples:
     *   .foldLeft(0){res, age -> res + age} = 78
     *   <[]>.foldLeft(0){res, age -> res + age} = 0
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldLeft([]){res, age -> res.append(age)} = [22, 31, 25]
     *
     * @param e           	    initial value
     * @param f         		binary function:: B * V -> B
     * @return            	    folded result
     */
    fun <B> foldLeft(e: B, f: (B, V) -> B): B = this.foldLeft(e, C2(f))

    /**
     * foldLeftWithKey is a higher-order function that folds a left associative binary
     *   function into a tree.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldLeftWithKey(0){res -> {name -> {age -> res + name.length() + age}}} = 91
     *   <[]>.foldLeftWithKey(0){res -> {name -> {age -> res + name.length() + age}}} = 0
     *
     * @param e           	    initial value
     * @param f         		curried binary function:: B -> K -> V -> B
     * @return            	    folded result
     */
    fun <B> foldLeftWithKey(e: B, f: (B) -> (K) -> (V) -> B): B {
        fun recFoldLeftWithKey(tree: RedBlackTree<K, V>, e: B, f: (B) -> (K) -> (V) -> B): B {
            return when(tree) {
                is EmptyTree -> e
                is RBTree -> recFoldLeftWithKey(tree.right, f(recFoldLeftWithKey(tree.left, e, f))(tree.key)(tree.value), f)
            }
        }   // recFoldLeftWithKey

        return recFoldLeftWithKey(this, e, f)
    }   // foldLeftWithKey

    /**
     * foldLeftWithKey is a higher-order function that folds a left associative binary
     *   function into a tree.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldLeftWithKey(0){res, name, age -> res + name.length() + age} = 91
     *   <[]>.foldLeftWithKey(0){res, name, age -> res + name.length() + age} = 0
     *
     * @param e           	    initial value
     * @param f         		binary function:: B * K * V -> B
     * @return            	    folded result
     */
    fun <B> foldLeftWithKey(e: B, f: (B, K, V) -> B): B = this.foldLeftWithKey(e, C3(f))

    /**
     * foldRight is a higher-order function that folds a right associative binary
     *   function into the values of a tree.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldRight(0){age -> {res -> res + age}} = 78
     *   <[]>.foldRight(0){age -> {res -> res + age}} = 0
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldRight([]){age -> {res -> res.append(age)}} = [25, 31, 22]
     *
     * @param e           	    initial value
     * @param f         		curried binary function:: V -> W -> W
     * @return            	    folded result
     */
    fun <B> foldRight(e: B, f: (V) -> (B) -> B) : B {
        fun recFoldRight(tree: RedBlackTree<K, V>, e: B, f: (V) -> (B) -> B) : B {
            return when(tree) {
                is EmptyTree -> e
                is RBTree -> recFoldRight(tree.left, f(tree.value)(recFoldRight(tree.right, e, f)), f)
            }
        }   // recFoldRight

        return recFoldRight(this, e, f)
    }   // foldRight

    /**
     * foldRight is a higher-order function that folds a right associative binary
     *   function into the values of a map.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldRight(0){age, res -> res + age} = 78
     *   <[]>.foldRight(0){age, res -> res + age} = 0
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldRight([]){age, res -> res.append(age)} = [25, 31, 22]
     *
     * @param e           	    initial value
     * @param f         		binary function:: V * W -> W
     * @return            	    folded result
     */
    fun <B> foldRight(e: B, f: (V, B) -> B) : B = this.foldRight(e, C2(f))

    /**
     * foldRightWithKey is a higher-order function that folds a right associative binary
     *   function into a tree.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldRightWithKey(0){name -> {age -> {res -> res + name.length() + age}}} = 91
     *   <[]>.foldRightWithKey(0){name -> {age -> {res -> res + name.length() + age}}} = 0
     *
     * @param e           	    initial value
     * @param f         		curried binary function:: K -> V -> B -> B
     * @return            	    folded result
     */
    fun <B> foldRightWithKey(e: B, f: (K) -> (V) -> (B) -> B): B {
        fun recFoldRightWithKey(tree: RedBlackTree<K, V>, e: B, f: (K) -> (V) -> (B) -> B): B {
            return when(tree) {
                is EmptyTree -> e
                is RBTree -> recFoldRightWithKey(tree.left, f(tree.key)(tree.value)(recFoldRightWithKey(tree.right, e, f)), f)
            }
        }   // recFoldRightWithKey

        return recFoldRightWithKey(this, e, f)
    }   // foldRightWithKey

    /**
     * foldRightWithKey is a higher-order function that folds a right associative binary
     *   function into a tree.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.foldRightWithKey(0){name, age, res -> res + name.length() + age} = 91
     *   <[]>.foldRightWithKey(0){name, age, res -> res + name.length() + age} = 0
     *
     * @param e           	    initial value
     * @param f         		binary function:: K * V * B -> B
     * @return            	    folded result
     */
    fun <B> foldRightWithKey(e: B, f: (K, V, B) -> B): B = this.foldRightWithKey(e, C3(f))

    /**
     * Deliver the (sub-) tree starting at or after the given key. If the given key
     *   is later than the final element then empty tree is returned.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.from("John") = <[John: 31, Ken: 25]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.from("Kath") = <[Ken: 25]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.from("Stuart") = <[]>
     *
     * @param fromKey           the start key
     * @return                  the new sub-tree
     */
    fun from(fromKey: K): RedBlackTree<K, V> = RedBlackTreeF.blacken(RedBlackTreeF.from(this, fromKey))

    /**
     * Deliver a singleton tree containing the greatest element.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.greatest = <[Ken: 25]>
     */
    val greatest: RBTree<K, V>
        get() {
            fun recGreatest(tree: RedBlackTree<K, V>): RBTree<K, V> {
                return when(tree) {
                    is EmptyTree -> throw RedBlackTreeException("greatest: empty tree")
                    is RBTree -> if (tree.right !is EmptyTree<K, V>) recGreatest(tree.right) else tree
                }
            }   // recGreatest

            return recGreatest(this)
        }   // greatest

    /**
     * Insert a new key/value pair in the tree. If the key is absent then the
     *   the key/value pair are added to the tree. If the key does exist then
     *   the pair key/f(key)(newValue)(oldValue) is inserted.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.insertWithKey(Ken, 100){k -> {newV -> {oldV -> k.length + oldV + newV}}} = <[Jessie: 22, John: 31, Ken: 128]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.insertWithKey(Irene, 100){k -> {newV -> {oldV -> k.length + oldV + newV}}} = <[Jessie: 22, John: 31, Irene: 100, Ken: 25]>
     *
     * @param key     		        new key
     * @param value   		        new value associated with the key
     * @param f			            curried combining function
     * @return        		        updated tree
     */
    fun insertWithKey(key: K, value: V, f: (K) -> (V) -> (V) -> V): RedBlackTree<K, V> {
        fun recInsertWithKey(key: K, value: V, f: (K) -> (V) -> (V) -> V, tree: RedBlackTree<K, V>): RedBlackTree<K, V> {
            return when(tree) {
                is EmptyTree -> RedBlackTreeF.singleton(key, value)
                is RBTree -> {
                    if (key.compareTo(tree.key) < 0)
                        RedBlackTreeF.balanceLeft(RedBlackTreeF.isBlackTree(tree), tree.key, tree.value, recInsertWithKey(key, value, f, tree.left), tree.right)
                    else if (key.compareTo(tree.key) > 0)
                        RedBlackTreeF.balanceRight(RedBlackTreeF.isBlackTree(tree), tree.key, tree.value, tree.left, recInsertWithKey(key, value, f, tree.right))
                    else
                        RedBlackTreeF.makeTree(RedBlackTreeF.isBlackTree(tree), key, f(key)(value)(tree.value), tree.left, tree.right)
                }
            }
        }   // recInsertWithKey

        return recInsertWithKey(key, value, f, this)
    }   // insertWithKey

    /**
     * The intersection of two trees. Returns data from this for keys
     *   existing in both trees.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.intersection(<[Irene: 25, Dawn: 31]>) = <[]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.intersection(<[John: 41, Ken: 35]>) = <[John: 31, Ken: 25]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.intersection(<[]>) = <[]>
     *   <[]>.intersection(<[]>) = <[Jessie: 22, John: 31, Ken: 25]>
     *
     * @param tree                  existing tree
     * @return                      the intersection of this tree and the given tree
     */
    fun intersection(tree: RedBlackTree<K, V>): RedBlackTree<K, V> =
        this.foldLeftWithKey(RedBlackTreeF.empty<K, V>()){acc, key, value ->
            if (tree.containsKey(key))
                acc.update(key, value, true)
            else
                acc
        }   // intersection

    /**
     * Obtain the length of this tree.
     *
     * Examples:
     *   [1, 2, 3, 4].length() == 4
     *   [].length() = 0
     *
     * @return                  number of elements in the tree
     */
    fun length(): Int = count

    /**
     * Look up the given key in the tree. Return value if present, otherwise
     *   throw an exception.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.lookUp("Ken") = 25
     *   <[Jessie: 22, John: 31, Ken: 25]>.lookUp("Irene") = exception
     *   <[]>.lookUp("Ken") = exception
     *
     * @param key               search key
     * @return                  matching value
     */
    fun lookUp(key: K): V {
        val tree: RedBlackTree<K, V> = RedBlackTreeF.lookUpTree(this, key)
        return when(tree) {
            is EmptyTree -> throw RedBlackTreeException("RedBlackTree.lookUp: absent key: ${key}")
            is RBTree -> tree.value
        }
    }   // lookUp

    /**
     * Look up the given key in the tree. Return None if absent, otherwise
     *   return the corresponding value wrapped in Some.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.lookUpKey("Ken") = Some(25)
     *   <[Jessie: 22, John: 31, Ken: 25]>.lookUpKey("Irene") = None
     *   <[]>.lookUpKey("Ken") = None
     *
     * @param key               search key
     * @return                  matching value or none if key is absent
     */
    fun lookUpKey(key: K): Option<V> {
        val tree: RedBlackTree<K, V> = RedBlackTreeF.lookUpTree(this, key)
        return when(tree) {
            is EmptyTree -> OptionF.none()
            is RBTree -> OptionF.some(tree.value)
        }
    }   // lookUpKey

    /**
     * Difference two trees (as an operator), ie all the elements in this tree that are
     *   not present in the given tree.
     *
     * Examples:
     *   {Jessie, John, Ken} - {Jessie, John, Ken} = {}
     *   {Jessie, John, Ken} - {John, Ken} = {Jessie}
     *   {Jessie, John, Ken} - {} = {Jessie, John, Ken}
     *   {} - {Jessie, John, Ken} = {}
     *
     * @param tree              existing tree
     * @return                  the difference of this tree and the given tree
     */
    operator fun minus(map: RedBlackTree<K, V>): RedBlackTree<K, V> = this.difference(map)

    /**
     * Return an iterator over key elements of type K.
     */
    fun keysIterator(): KeysIterator<K, V> = KeysIterator(this)

    /**
     * Deliver the (sub-) tree extending from to as far as but not including the until key.
     *   If the given until key is before or the same as the from key then the empty tree is returned.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.range("Jessie", "Ken") = <[Jessie: 22, John: 31]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.range("Jessie", "Pat") = <[Jessie: 22, John: 31, Ken: 25]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.range("Dave", "Ken") = <[Jessie: 22, John: 31]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.range("Jessie", "Dave") = <[]>
     *
     * @param fromKey           the smaller key from which to start extraction
     * @param untilKey          the larger key in which to stop extraction (exclusive)
     * @return                  new tree
     */
    fun range(fromKey: K, untilKey: K): RedBlackTree<K, V> = RedBlackTreeF.blacken(RedBlackTreeF.range(this, fromKey, untilKey))

    /**
     * Obtain the length of this tree.
     *
     * Examples:
     *   [1, 2, 3, 4].size() == 4
     *   [].size() == 0
     *
     * @return                  number of elements in the tree
     */
    fun size(): Int = this.length()

    /**
     * Deliver a singleton tree containing the smallest element.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.smallest = <[Jessie: 22]>
     */
    val smallest: RBTree<K, V>
        get() {
            fun recSmallest(tree: RedBlackTree<K, V>): RBTree<K, V> {
                return when(tree) {
                    is EmptyTree -> throw RedBlackTreeException("smallest: empty tree")
                    is RBTree -> if (tree.left !is EmptyTree<K, V>) recSmallest(tree.left) else tree
                }
            }   // recSmallest

            return recSmallest((this))
        }   // smallest

    /**
     * Deliver the tree with the elements visited pre-order starting at from (inclusive)
     *   to until (exclusive). If until exceeds the size of the tree then all the elements
     *   from to the end are returned. If from is negative then all the elements to until
     *   are included. If until is the same or less than from then null is returned.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.slice(0, 2) = <[Jessie: 22, John: 31]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.slice(1, 2) = <[John: 31]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.slice(1, 0) = null
     *
     * @param from              the start index (inclusive)
     * @param until             the end index (exclusive)
     * @return                  the new tree
     */
    fun slice(from: Int, until: Int): RedBlackTree<K, V> = RedBlackTreeF.blacken(RedBlackTreeF.slice(this, from, until))

    /**
     * Deliver the tree with the first n elements visited pre-order. If n is zero
     *   or less then deliver the empty tree.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.take(3) = <[Jessie: 22, John: 31, Ken: 25]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.take(2) = <[Jessie: 22, John: 31]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.take(0) = <[]>
     *
     * @param n                 the number of elements to extract
     * @return                  the new tree
     */
    fun take(n: Int): RedBlackTree<K, V> = RedBlackTreeF.blacken(RedBlackTreeF.take(this, n))

    /**
     * Deliver the (sub-) tree extending as far as and including the given key.
     *   If the given key is before the first element then the empty tree is returned.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.to("John") = <[Jessie: 22, John: 31]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.to("Kath") = <[Jessie: 22, John: 31]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.to("Dave") = <[]>
     *
     * @param toKey             the end key (inclusive)
     * @return                  the new tree
     */
    fun to(toKey: K): RedBlackTree<K, V> = RedBlackTreeF.blacken(RedBlackTreeF.to(this, toKey))

    /**
     * The union of two trees. It delivers a left-biased union of this
     *   and right, ie it prefers this when duplicate keys are encountered.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.union(<[Irene: 25, Dawn: 31]>) = <[Dawn: 31, Irene: 25, Jessie: 22, John: 31, Ken: 25]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.union(<[Ken: 35, John: 41]>) = <[Jessie: 22, John: 31, Ken: 25]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.union(<[]>) = <[Jessie: 22, John: 31, Ken: 25]>
     *   <[]>.union(<[Jessie: 22, John: 31, Ken: 25]>) = <[Jessie: 22, John: 31, Ken: 25]>
     *
     * @param tree                  existing tree
     * @return                      the union of this tree and the given tree
     */
    fun union(tree: RedBlackTree<K, V>): RedBlackTree<K, V> =
        this.foldLeftWithKey(tree){acc, key, value -> acc.update(key, value)}

    /**
     * Deliver the (sub-) tree extending as far as but not including the given key.
     *   If the given key is before or the same as the first element then the empty tree is returned.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.until("Ken") = <[Jessie: 22, John: 31]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.until("Jessie") = <[]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.until("Dave") = <[]>
     *
     * @param untilKey          the end key (exclusive)
     * @return                  the new tree
     */
    fun until(untilKey: K): RedBlackTree<K, V> = RedBlackTreeF.blacken(RedBlackTreeF.until(this, untilKey))

    /**
     * Update the value at the key, if present and the overwrite is true.
     *   Include the key/value pair if the key is originally absent irrespective
     *   of overwrite.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.update(Ken, 99, false) = <[Jessie: 22, John: 31, Ken: 25]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.update(Ken, 99, true) = <[Jessie: 22, John: 31, Ken: 99]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.update(Pat, 99, false) = <[Jessie: 22, John: 31, Ken: 25, Pat, 99]>
     *   <[Jessie: 22, John: 31, Ken: 25]>.update(Pat, 99, true) = <[Jessie: 22, John: 31, Ken: 25, Pat, 99]>
     *
     * @param key     		    new key
     * @param value             new value
     * @param overwite          make changes or not
     * @return        		    updated tree
     */
    fun update(key: K, value: V, overwrite: Boolean = true): RedBlackTree<K, V> = RedBlackTreeF.blacken(RedBlackTreeF.upd(this, key, value, overwrite))

    /**
     * Return an iterator over value elements of type V.
     */
    fun valuesIterator(): ValuesIterator<K, V> = ValuesIterator(this)

    /**
     * Verify the black-black depth are all the same.
     *
     * @return                  Some(depth) on success, None otherwise
     */
    fun verifyDepth(): Option<Int> {
        fun recVerifyDepth(tree: RedBlackTree<K, V>): Option<Int> {
            return when(tree) {
                is EmptyTree -> OptionF.some(1)
                is RBTree -> {
                    val vdL: Option<Int> = recVerifyDepth(tree.left)
                    val vdR: Option<Int> = recVerifyDepth(tree.right)
                    val inc: Option<Int> = if (tree is RBTree.RedTree) OptionF.some(0) else OptionF.some(1)
                    if (vdL == vdR) inc.bind{m -> vdL.bind{n -> OptionF.some(m + n)}} else OptionF.none()
                }
            }
        }   // recVerifyBlackDepth

        return recVerifyDepth(this)
    }   // verifyDepth

    /**
     * Verify no red-red pattern in a node and node's parent.
     *
     * @return                  true if verified, false otherwise
     */
    fun verifyNoRedRed(): Boolean {
        fun recVerifyNoRedRed(tree: RedBlackTree<K, V>): Boolean {
            return when(tree) {
                is EmptyTree -> true
                is RBTree.RedTree -> (tree.left !is RBTree.RedTree) && (tree.right !is RBTree.RedTree) && recVerifyNoRedRed(tree.left) && recVerifyNoRedRed(tree.right)
                is RBTree.BlackTree -> recVerifyNoRedRed(tree.left) && recVerifyNoRedRed(tree.right)
            }
        }   // recVerifyNoRedRed

        return recVerifyNoRedRed(this)
    }   // verifyNoRedRed



















    /**
     * Deliver a singleton tree containing the smallest element.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.smallest = <[Jessie: 22]>
     *
    val smallest: RedBlackTree<K, V>?
    get() {
    var result: RedBlackTree<K, V>? = this
    while(result!!.left != null)
    result = result.left
    return result
    }
     ***/

    /**
     * Deliver a singleton tree containing the greatest element.
     *
     * Examples:
     *   <[Jessie: 22, John: 31, Ken: 25]>.smallest = <[Ken: 25]>
     *
    val greatest: RedBlackTree<K, V>?
    get() {
    var result: RedBlackTree<K, V>? = this
    while (result!!.right != null)
    result = result.right
    return result
    }
     ***/

}   // RedBlackTree
