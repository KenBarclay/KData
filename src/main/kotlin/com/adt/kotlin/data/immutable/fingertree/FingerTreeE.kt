package com.adt.kotlin.data.immutable.fingertree

// Functor extension functions:

/**
 * Apply the function to the content(s) of the finger tree context.
 */
fun <V, A, B> FingerTree<V, A>.fmap(measured: Measured<V, B>, f: (A) -> B): FingerTree<V, B> =
    this.map(measured, f)
