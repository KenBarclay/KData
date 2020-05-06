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
 * @author	                    Ken Barclay
 * @since                       December 2014
 */

import com.adt.kotlin.data.immutable.trie.hamt.node.Node.EmptyNode

import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.ListF



object NodeF {

    /**
     * Convert an immutable list into an immutable map node.
     *
     * @param list                  immutable list of elements
     * @return                      immutable map node of the given values
     */
    fun <K: Comparable<K>, V> from(list: List<Pair<K, V>>): Node<K, V> {
        return list.foldRight(EmptyNode()) { pair: Pair<K, V> -> { node: Node<K, V> -> node.insert(pair.first, pair.second)}}
    }



    internal val shiftStep: Int = 5
    internal val chunk: Int = 32          // 2 ^ shiftStep
    internal val mask: Int = chunk - 1
    internal val bmNodeMax: Int = 16      // maximum size of a BitmapIndexedNode
    internal val arrayNodeMin: Int = 8    // minimum size of an ArrayNode


    /**
     * Mask off the lower order bits of the right shift of the hash value.
     *
     * @param shift             the amount of right shift to apply
     * @param hash              the value right shifted
     * @return                  the masked off right shifted amount
     */
    internal fun hashFragment(shift: Int, hash: Int): Int { return ((hash shr shift) and mask) }

    /**
     * Effectively compute pow(2, hash) with a left shift.
     *
     * @param hash              the left shift amount
     * @return                  the single bit left shifted by the given amount
     */
    internal fun toBitmap(hash: Int): Int = (1 shl hash)

    /**
     * Mask off the lower hash bits of the given bitmap then compute the number of
     *   one bits in the resulting value.
     *
     * @param bitmap            the integer from which to compute its one bit indices
     * @param hash              the interested lower hash bits
     * @return                  the number of one bits in the resulting integer
     */
    internal fun fromBitmap(bitmap: Int, hash: Int): Int {
        val mask: Int = toBitmap(hash) - 1
        return bitCount32(bitmap and mask)
    }

    /**
     * Compute a list of indices in ascending order in which the one bit is set in the
     *   given bitmap. If bitmap has the decimal value 22 (binary 10110) then the computed
     *   list is [1, 2, 4].
     *
     * @param bitmap            the integer from which to compute its one bit indices
     * @return                  the indices of the one bits
     */
    internal fun bitmapToIndices(bitmap: Int): List<Int> {
        fun loop(index: Int, bitmap: Int): List<Int> {
            return if (bitmap == 0)
                ListF.empty<Int>()
            else if (index == 32)
                ListF.empty<Int>()
            else if ((bitmap and 1) == 0)
                loop(1 + index, bitmap shr 1)
            else
                ListF.cons(index, loop(1 + index, bitmap shr 1))
        }

        return loop(0, bitmap)
    }

    /**
     * Convert the indices to their corresponding numeric value. For example, if the given
     *   array contains <1, 2, 4> then the effect of the fold is the compute:
     *   (((0 OR 1 << 1) OR 1 << 2) OR 1 << 4) and delivers the decimal value 22.
     *
     * @param indices           the one bit positions
     * @return                  the computed numeric value
     */
    internal fun indicesToBitmap(indices: Array<Int>): Int =
        indices.fold(0){bm: Int, idx: Int -> (bm or (1 shl idx))}

    /**
     * Find the number of one bits in the given integer.
     *
     * @param n                 the integer to count the one bits
     * @return                  the number of one bits in the given integer
     */
    internal fun bitCount32(n: Int): Int {
        return bitCount8[((n shr 24) and 0xFF)] +
                bitCount8[((n shr 16) and 0xFF)] +
                bitCount8[((n shr 8) and 0xFF)] +
                bitCount8[(n and 0xFF)]
    }



// ---------- properties ----------------------------------

    // the number of one bits in the representation for the values 0..255
    internal val bitCount8: IntArray = intArrayOf(
            0,  1,  1,  2,  1,  2,  2,  3,      //   0 ..   7
            1,  2,  2,  3,  2,  3,  3,  4,      //   8 ..  15
            1,  2,  2,  3,  2,  3,  3,  4,      //  16 ..  23
            2,  3,  3,  4,  3,  4,  4,  5,      //  24 ..  31
            1,  2,  2,  3,  2,  3,  3,  4,      //  32 ..  39
            2,  3,  3,  4,  3,  4,  4,  5,      //  40 ..  47
            2,  3,  3,  4,  3,  4,  4,  5,      //  48 ..  55
            3,  4,  4,  5,  4,  5,  5,  6,      //  56 ..  63
            1,  2,  2,  3,  2,  3,  3,  4,      //  64 ..  71
            2,  3,  3,  4,  3,  4,  4,  5,      //  72 ..  79
            2,  3,  3,  4,  3,  4,  4,  5,      //  80 ..  87
            3,  4,  4,  5,  4,  5,  5,  6,      //  88 ..  95
            2,  3,  3,  4,  3,  4,  4,  5,      //  96 .. 103
            3,  4,  4,  5,  4,  5,  5,  6,      // 104 .. 111
            3,  4,  4,  5,  4,  5,  5,  6,      // 112 .. 119
            4,  5,  5,  6,  5,  6,  6,  7,      // 120 .. 127
            1,  2,  2,  3,  2,  3,  3,  4,      // 128 .. 135
            2,  3,  3,  4,  3,  4,  4,  5,      // 136 .. 143
            2,  3,  3,  4,  3,  4,  4,  5,      // 144 .. 151
            3,  4,  4,  5,  4,  5,  5,  6,      // 152 .. 159
            2,  3,  3,  4,  3,  4,  4,  5,      // 160 .. 167
            3,  4,  4,  5,  4,  5,  5,  6,      // 168 .. 175
            3,  4,  4,  5,  4,  5,  5,  6,      // 176 .. 183
            4,  5,  5,  6,  5,  6,  6,  7,      // 184 .. 191
            2,  3,  3,  4,  3,  4,  4,  5,      // 192 .. 199
            3,  4,  4,  5,  4,  5,  5,  6,      // 200 .. 207
            3,  4,  4,  5,  4,  5,  5,  6,      // 208 .. 215
            4,  5,  5,  6,  5,  6,  6,  7,      // 216 .. 223
            3,  4,  4,  5,  4,  5,  5,  6,      // 224 .. 231
            4,  5,  5,  6,  5,  6,  6,  7,      // 232 .. 239
            4,  5,  5,  6,  5,  6,  6,  7,      // 240 .. 247
            5,  6,  6,  7,  6,  7,  7,  8       // 248 .. 255
    )

}   // NodeF
