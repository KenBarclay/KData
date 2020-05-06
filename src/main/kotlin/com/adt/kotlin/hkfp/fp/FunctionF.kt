package com.adt.kotlin.hkfp.fp

/**
 * A suite of function definitions that can be used in a functional style.
 *
 * In these definitions the parameters and the results are described
 *   as functions.
 *
 * The documentation for these function definitions specifies the parameters
 *   or the result as A or A -> B or A -> B -> C or A * B -> C. In all cases
 *   the A, B and C represent arbitrary types. The type A -> B is a function
 *   that has a single parameter of type A and which returns a result of type B.
 *   The type A -> B -> C is a function that has a single parameter of type A
 *   and which returns a function of type B -> C (right associative).
 *
 * The type denoted by <A> is a list of values each of type A. The type
 *   denoted by <<A>> is a list of values each of which is list <A>. The type
 *   denoted by [[A, B]] is a list each element of which is a sub-list
 *   of length 2 of types A and B (effectively a tuple).
 *
 * @author	                    Ken Barclay
 * @since                       August 2012
 */



object FunctionF {

// ---------- curry/uncurry functions ---------------------

    /**
     * Curry: transform an uncurried function of two parameters
     *   into its curried form. A synonym for C2.
     *
     * C:: (A * B -> C) -> A -> B -> C
     *
     * @param f     		        the uncurried binary function
     * @return        		        the curried form
     */
    fun <A, B, C> C(f: (A, B) -> C): (A) -> (B) -> C = {a: A -> {b: B -> f(a, b)}}

    /**
     * Curry: transform an uncurried function of two parameters
     *   into its curried form. A synonym for C.
     *
     * C2:: (A * B -> C) -> A -> B -> C
     *
     * @param   f     		        the uncurried binary function
     * @return        		        the curried form
     */
    fun <A, B, C> C2(f: (A, B) -> C): (A) -> (B) -> C = {a: A -> {b: B -> f(a, b)}}

    /**
     * Curry: transform an uncurried function of three parameters
     *   into its curried form.
     *
     * C3:: (A * B * C -> D) -> A -> B -> C -> D
     *
     * @param   f     		        the uncurried function of three parameters
     * @return        		        the curried form
     */
    fun <A, B, C, D> C3(f: (A, B, C) -> D): (A) -> (B) -> (C) -> D = {a: A -> {b: B -> {c: C -> f(a, b, c)}}}

    /**
     * Curry: transform an uncurried function of four parameters
     *   into its curried form.
     *
     * C4:: (A * B * C * D -> E) -> A -> B -> C -> D -> E
     *
     * @param   f     		        the uncurried function of four parameters
     * @return        		        the curried form
     */
    fun <A, B, C, D, E> C4(f: (A, B, C, D) -> E): (A) -> (B) -> (C) -> (D) -> E = {a: A -> {b: B -> {c: C -> {d: D -> f(a, b, c, d)}}}}




    /**
     * Uncurry: transform a curried function of two parameters
     *   into its uncurried form. The synonym of U2.
     *
     * U:: (A -> B -> C) -> (A * B -> C)
     *
     * @param f     		        the binary curried function
     * @return      		        the uncurried form
     */
    fun <A, B, C> U(f: (A) -> (B) -> C): (A, B) -> C {
        return {a: A, b: B -> f(a)(b)}
    }

    /**
     * Uncurry: transform a curried function of two parameters
     *   into its uncurried form. The synonym of U.
     *
     * U2:: (A -> B -> C) -> (A * B -> C)
     *
     * @param f     		        the binary curried function
     * @return      		        the uncurried form
     */
    fun <A, B, C> U2(f: (A) -> (B) -> C): (A, B) -> C {
        return {a: A, b: B -> f(a)(b)}
    }

    /**
     * Uncurry: transform a curried function of three parameters
     *   into its uncurried form.
     *
     * U3:: (A -> B -> C -> D) -> (A * B * C -> D)
     *
     * @param f     		        the curried function of three parameters
     * @return      		        the uncurried form
     */
    fun <A, B, C, D> U3(f: (A) -> (B) -> (C) -> D): (A, B, C) -> D {
        return {a: A, b: B, c: C -> f(a)(b)(c)}
    }

    /**
     * Uncurry: transform a curried function of four parameters
     *   into its uncurried form.
     *
     * U4:: (A -> B -> C -> D -> E) -> (A * B * C * D -> E)
     *
     * @param f     		        the curried function of four parameters
     * @return      		        the uncurried form
     */
    fun <A, B, C, D, E> U4(f: (A) -> (B) -> (C) -> (D) -> E): (A, B, C, D) -> E {
        return {a: A, b: B, c: C, d: D -> f(a)(b)(c)(d)}
    }



// ---------- function composition/flip -------------------

    /**
     * Partial application of the function f.
     *
     * @param a                 value to fix the first parameter
     * @param f                 curried function of two parameters
     * @return                  function of one argument
     */
    fun <A, B, C> partial(a: A, f: (A) -> (B) -> C): (B) -> C =
        {b: B -> f(a)(b)}

    fun <A, B, C> partial(a: A, f: (A, B) -> C): (B) -> C = partial(a, C2(f))



// ---------- function composition/flip -------------------

    /**
     * Function compose: apply function g to an input x and apply
     *   function f to the result as in f(g(x)).
     *
     * compose:: (B -> C) * (A -> B) -> (A -> C)
     *
     * @param f     		        result function
     * @param g     		        intermediate function
     * @return      		        composed functions
     */
    fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = {a: A -> f(g(a))}

    /**
     * Function composition: apply function f to an input x and apply
     *   function g to the result as in g(f(x)).
     *
     * forwardComp:: (A -> B) * (B -> C) -> (A -> C)
     *
     * @param	f		            intermediate function
     * @param	g		            result function
     * @result      		        composed functions
     */
    fun <A, B, C> forwardCompose(f: (A) -> B, g: (B) -> C): (A) -> C = {a: A -> g(f(a))}

    /**
     * flip(f) takes its first two arguments in the reverse order to f.
     *
     * flip:: (A -> B -> C) -> B -> A -> C
     *
     * @param f 			        binary curried function
     * @return    		            binary curried function that applies f to its reversed arguments
     */
    fun <A, B, C> flip(f: (A) -> (B) -> C): (B) -> (A) -> C = {b: B -> {a: A -> f(a)(b)}}

    /**
     * flip(f) takes its first two arguments in the reverse order to f.
     *
     * flip:: (A * B -> C) -> B * A -> C
     *
     * @param f 			        binary function
     * @return    		            binary function that applies f to its reversed arguments
     */
    fun <A, B, C> flip(f: (A, B) -> C): (B, A) -> C = {b: B, a: A -> f(a, b)}

    /**
     * Unary identity function: return the value of the argument.
     *
     * identity:: A -> A
     *
     * @param a   		            input value
     * @return    		            the input value unchanged
     */
    fun <A> identity(): (A) -> A = {a: A -> a}

    /**
     * Unary identity function: return the value of the argument.
     *   A synonym for identity.
     *
     * id:: A -> A
     *
     * @param a   		            input value
     * @return    		            the input value unchanged
     */
    fun <A> id(): (A) -> A = identity()

    /**
     * Specialised identities.
     */
    val idInt: (Int) -> Int = id()
    val idLong: (Long) -> Long = id()
    val idDouble: (Double) -> Double = id()
    val idBoolean: (Boolean) -> Boolean = id()
    val idString: (String) -> String = id()

    /**
     * Binary constant function: return the value of the first argument.
     *
     * constant:: A -> B -> A
     *
     * @param a   		            first argument
     * @return    		            value of first argument
     */
    fun <A, B> constant(a: A): (B) -> A = {_: B -> a}

    /**
     * Deliver a function that returns the complement of the predicate.
     *
     * @parameter predicate         the predicate function that is complemented
     * @return                      the complement of the given predicate
     */
    fun <A> negate(predicate: (A) -> Boolean): (A) -> Boolean = {a -> !predicate(a)}

    /**
     * Given a sequence of functions f0, f1, f2, ... return the function
     *   ... f2(f1(f0(x))) ... )
     *
     * @parameter fs                the sequence of functions
     * @return                      the function representing the sequencing
     */
    fun <A> chainLeft(fs: Array<(A) -> A>): (A) -> A = {a -> fs.fold(a){b, f -> f(b)}}

    /**
     * Given a sequence of functions f0, f1, f2, ... return the function
     *   f0(f1(f2( ... ))
     *
     * @parameter fs                the sequence of functions
     * @return                      the function representing the sequencing
     */
    fun <A> chainRight(fs: Array<(A) -> A>): (A) -> A = {a -> fs.foldRight(a){f, b -> f(b)}}



// ---------- categorical ---------------------------------

    // Functor extension functions:

    /**
     * Lift a function into the function context.
     */
    fun <A, B, C> lift(f: (B) -> C): ((A) -> B) -> (A) -> C =
        {g: (A) -> B -> g.fmap(f)}



    // Applicative extension functions:

    /**
     * Lift a function to actions.
     */
    fun <A, B, C> liftA(f: (B) -> C): ((A) -> B) -> (A) -> C =
        {g: (A) -> B -> g.fmap(f)}

    /**
     * Lift a binary function to actions.
     */
    fun <A, B, C, D> liftA2(f: (B) -> (C) -> D): ((A) -> B) -> ((A) -> C) -> (A) -> D =
        {fab: (A) -> B ->
            {fac: (A) -> C ->
                fac.ap(fab.fmap(f))
            }
        }   // liftA2

    /**
     * Lift a ternary function to actions.
     */
    fun <A, B, C, D, E> liftA3(f: (B) -> (C) -> (D) -> E): ((A) -> B) -> ((A) -> C) -> ((A) -> D) -> (A) -> E =
        {fab: (A) -> B ->
            {fac: (A) -> C ->
                {fad: (A) -> D ->
                    fad.ap(fac.ap(fab.fmap(f)))
                }
            }
        }   // liftA3

    /**
     * Execute a binary function.
     */
    fun <A, B, C, D> mapA2(fab: (A) -> B, fac: (A) -> C, f: (B) -> (C) -> D): (A) -> D =
        liftA2<A, B, C, D>(f)(fab)(fac)

    /**
     * Execute a ternary function.
     */
    fun <A, B, C, D, E> mapA3(fab: (A) -> B, fac: (A) -> C, fad: (A) -> D, f: (B) -> (C) -> (D) -> E): (A) -> E =
        liftA3<A, B, C, D, E>(f)(fab)(fac)(fad)



    // Monad extension functions:

    /**
     * Lift a function to a monad.
     */
    fun <A, B, C> liftM(f: (B) -> C): ((A) -> B) -> ((A) -> C) =
        {fab: (A) -> B ->
            fab.bind{b: B -> {_: A -> f(b)}}
        }   // liftM

    /**
     * Lift a binary function to a monad.
     */
    fun <A, B, C, D> liftM2(f: (B) -> (C) -> D): ((A) -> B) -> ((A) -> C) -> ((A) -> D) =
        {fab: (A) -> B ->
            {fac: (A) -> C ->
                fab.bind{b: B -> fac.bind{c: C -> {_: A -> f(b)(c)}}}
            }
        }   // liftM2

    /**
     * Lift a ternary function to a monad.
     */
    fun <A, B, C, D, E> liftM3(f: (B) -> (C) -> (D) -> E): ((A) -> B) -> ((A) -> C) -> ((A) -> D) -> ((A) -> E) =
        {fab: (A) -> B ->
            {fac: (A) -> C ->
                {fad: (A) -> D ->
                    fab.bind{b: B -> fac.bind{c: C -> fad.bind{d: D -> {_: A -> f(b)(c)(d)}}}}
                }
            }
        }   // liftM3



// ---------- numerics ------------------------------------

    /**
     * Compute m raised to the power n.
     *
     * @param m                 numerator
     * @param n                 the power to raise the numerator
     * @result                  m raised to the power n
     */
    fun pow(m: Int, n: Int): Int {
        tailrec
        fun recPow(m: Int, n: Int, acc: Int): Int {
            return if (n == 0)
                acc
            else
                recPow(m, n - 1, m * acc)
        }

        return recPow(m, n, 1)
    }

    /**
     * Is the integer parameter even-valued?
     *
     * @param n                     integer parameter
     * @return                      true if the parameter is even-valued
     */
    public val isEven: (Int) -> Boolean = {n: Int -> (n % 2 == 0)}

    /**
     * Is the integer parameter not even-valued?
     *
     * @param n                     integer parameter
     * @return                      true if the parameter is not even-valued
     */
    public val isOdd: (Int) -> Boolean = {n: Int -> (n % 2 != 0)}

    /**
     * Numeric arithmetic
     */
    public val intAdd: (Int) -> (Int) -> Int = {m -> {n -> m + n}}
    public val intMul: (Int) -> (Int) -> Int = {m -> {n -> m * n}}
    public val doubleAdd: (Double) -> (Double) -> Double = {m -> {n -> m + n}}
    public val doubleMul: (Double) -> (Double) -> Double = {m -> {n -> m * n}}

    fun compare(a: Int, b: Int): Int =
        if (a < b) -1 else if (a > b) +1 else 0
    fun compare(a: Long, b: Long): Int =
        if (a < b) -1 else if (a > b) +1 else 0
    fun compare(a: Float, b: Float): Int =
        if (a < b) -1 else if (a > b) +1 else 0
    fun compare(a: Double, b: Double): Int =
        if (a < b) -1 else if (a > b) +1 else 0
    fun compare(a: String, b: String): Int =
        if (a < b) -1 else if (a > b) +1 else 0

}   // FunctionF
