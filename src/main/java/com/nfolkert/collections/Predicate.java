package com.nfolkert.collections;

/**
 * Taken from the Jakarta commons-collection and augmented to have generics
 *
 *
 *
 * Defines a functor interface implemented by classes that perform a predicate
 * test on an object.
 * <p>
 * A <code>Predicate</code> is the object equivalent of an <code>if</code> statement.
 * It uses the input object to return a true or false value, and is often used in
 * validation or filtering.
 * <p>
 * Standard implementations of common predicates are provided by
 * {@link org.apache.commons.collections.PredicateUtils}. These include true, false, instanceof, equals, and,
 * or, not, method invokation and null testing.
 *
 * @since Commons Collections 1.0
 * @version $Revision: 1.11 $ $Date: 2004/04/14 20:08:57 $
 *
 * @author James Strachan
 * @author Stephen Colebourne
 */
public interface Predicate<T> {

    /**
     * Use the specified parameter to perform a test that returns true or false.
     *
     * @param object  the object to evaluate, should not be changed
     * @return true or false
     * @throws ClassCastException (runtime) if the input is the wrong class
     * @throws IllegalArgumentException (runtime) if the input is invalid
     * @throws org.apache.commons.collections.FunctorException (runtime) if the predicate encounters a problem
     */
    public boolean evaluate(T object);

}
