package com.adt.kotlin.data.immutable.list

/**
 * A class hierarchy defining an immutable list collection. The algebraic data
 *   type declaration is:
 *
 * datatype List[A] = Nil
 *                  | Cons of A * List[A]
 *
 * The implementation mimics functional Lists as found in Haskell. The
 *   member functions and the extension functions mostly use primitive
 *   recursion over the List value constructors. Local tail recursive
 *   functions are commonly used.
 *
 * The documentation uses the notation [...] to represent a list instance.
 *
 * @param A                     the (covariant) type of elements in the list
 *
 * @author	                    Ken Barclay
 * @since                       October 2019
 */



class ListIterator<A>(ls: List<A>) : Iterator<A> {

    override fun hasNext(): Boolean = !list.isEmpty()

    override fun next(): A {
        val head: A = list.head()
        list = list.tail()
        return head
    }

// ---------- properties ----------------------------------

    var list: List<A> = ls

}   // ListIterator
