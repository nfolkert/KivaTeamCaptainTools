package com.nfolkert.collections;

import java.io.Serializable;

/**
 * Taken from the Jakarta commons-collection and augmented to have generics
 *
 * Predicate implementation that always returns true.
 *
 * @since Commons Collections 3.0
 * @version $Revision: 1.6 $ $Date: 2004/05/16 11:16:01 $
 *
 * @author Stephen Colebourne
 */
public final class TruePredicate<T> implements Predicate<T>, Serializable {

    /** Serial version UID */
    static final long serialVersionUID = 3374767158756189740L;

    /** Singleton predicate instance */
    public static final Predicate INSTANCE = new TruePredicate();

    /**
     * Factory returning the singleton instance.
     *
     * @return the singleton instance
     * @since Commons Collections 3.1
     */
    public static <T> Predicate<T> getInstance() {
        return INSTANCE;
    }

    /**
     * Restricted constructor.
     */
    private TruePredicate() {
        super();
    }

    /**
     * Evaluates the predicate returning true always.
     *
     * @param object  the input object
     * @return true always
     */
    public boolean evaluate(T object) {
        return true;
    }

}

