package com.nfolkert.collections;

import java.util.Iterator;

/**
 * Defines an iterator that operates over a ordered collections.
 * <p>
 * This iterator allows both forward and reverse iteration through the collection.
 *
 * @since Commons Collections 3.0
 * @version $Revision: 1.4 $ $Date: 2004/02/18 01:15:42 $
 *
 * @author Stephen Colebourne
 */
public interface OrderedIterator<T> extends Iterator<T> {

    /**
     * Checks to see if there is a previous entry that can be iterated to.
     *
     * @return <code>true</code> if the iterator has a previous element
     */
    boolean hasPrevious();

    /**
     * Gets the previous element from the collection.
     *
     * @return the previous key in the iteration
     * @throws java.util.NoSuchElementException if the iteration is finished
     */
    T previous();
}

