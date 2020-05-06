package com.adt.kotlin.data.immutable.list

/**
 * A buffered implementation of a list.
 *
 * This is achieved by manipulating the list pointers. It supports constant time
 *   prepend and append operations.  Most other operations are linear.
 *
 * @author	                    Ken Barclay
 * @since                       October 2019
 */

import com.adt.kotlin.data.immutable.list.List.Nil
import com.adt.kotlin.data.immutable.list.List.Cons
import com.adt.kotlin.data.immutable.list.ListF.cons
import com.adt.kotlin.data.immutable.list.ListF.empty



class ListBuffer<A>() : ListBufferIF<A> {

    constructor(list: List<A>) : this() {
        if (list.isEmpty()){
            len = 0
            start = list
            lastCons = null
        } else {
            len = 1
            start = list
            var ptr: List<A> = list
            while (ptr is Cons && ptr.tl is Cons){
                ptr = ptr.tl
                len++
            }
            lastCons = ptr as Cons<A>
        }
    }   // constructor

    /**
     * Clear the buffer's content.
     */
    override fun clear() {
        start = Nil
        len = 0
    }

    /**
     * The current length of the buffer
     *
     * @return                   number of elements in the buffer
     */
    override fun length(): Int = len

    /**
     * Convert this buffer to a list. The operation takes constant
     *   time.
     *
     * @return                  the buffer content as a list
     */
    override fun toList(): List<A> = start

    /**
     * Determine if the buffer contains the given element.
     *
     * @param t                 search element
     * @return                  true if search element is present, false otherwise
     */
    override fun contains(t: A): Boolean {
        var found = false
        var cursor: List<A> = start
        while (!found && !cursor.isEmpty()) {
            if (t == cursor.head())
                found = true
            else
                cursor = cursor.tail()
        }

        return found
    }   // contains

    /**
     * Prepend a single element to this buffer. The operation takes
     *   constant time.
     *
     * @param t                 element to prepend
     * @return                  this buffer
     */
    override fun prepend(t: A): ListBufferIF<A> {
        val newStart: Cons<A> = Cons(t, start)
        if(start.isEmpty())
            lastCons = newStart
        start = newStart
        len++

        return this
    }   // prepend

    /**
     * Prepend all the elements from the parameter to this buffer.
     *
     * @param xs                elements to append
     * @return                  this buffer
     */
    override fun prepend(xs: List<A>): ListBufferIF<A> {
        var elements: List<A> = xs.reverse()
        len += elements.length()
        while (!elements.isEmpty()) {
            val newElem: Cons<A> = Cons(elements.head(), start)
            if (start.isEmpty())
                lastCons = newElem
            start = newElem
            elements = elements.tail()
        }

        return this
    }   // prepend

    /**
     * Prepend the elements of this buffer to the given list.
     *
     * @param ts                existing list
     * @return                  new list with the concatenated elements of this buffer
     */
    override fun prependTo(ts: List<A>): List<A> {
        if (start.isEmpty())
            return ts
        else if (ts.isEmpty()) {
            return this.toList()
        } else {
            val lastCons1: Cons<A> = lastCons!!
            lastCons1.tl = ts
            return this.toList()
        }
    }   // prependTo

    /**
     * Append a single element to this buffer. The operation takes
     *   constant time.
     *
     * @param t                 element to append
     * @return                  this buffer
     */
    override fun append(t: A): ListBufferIF<A> {
        if (start.isEmpty()) {
            lastCons = Cons(t, Nil)
            start = lastCons!!
        } else {
            val lastCons1: Cons<A> = lastCons!!
            lastCons = Cons(t, Nil)
            lastCons1.tl = lastCons!!
        }
        len++

        return this
    }   // append

    /**
     * Append all the elements from the parameter to this buffer.
     *
     * @param xs                elements to append
     * @return                  this buffer
     */
    override fun append(xs: List<A>): ListBufferIF<A> {
        if (!xs.isEmpty()) {
            val ys: List<A> = xs.foldRight(empty()){a -> {list: List<A> -> cons(a, list)}}
            if (start.isEmpty()) {
                start = ys
                lastCons = start as Cons<A>
                while (lastCons?.tl?.isEmpty() == false) {
                    lastCons = lastCons?.tl as Cons<A>
                }
            } else {
                lastCons?.tl = ys
                lastCons = ys as Cons<A>
                while (lastCons?.tl?.isEmpty() == false) {
                    lastCons = lastCons?.tl as Cons<A>
                }
            }
            len += ys.length()
        }

        return this
    }   // append



// ---------- properties ----------------------------------

    private var len: Int = 0                    // length of the buffer
    private var start: List<A> = Nil            // the list under construction
    private var lastCons: Cons<A>? = null       // the final Cons instance

}   // ListBuffer
