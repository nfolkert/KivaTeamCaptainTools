package com.nfolkert.exception;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;


/**
 */
public class AssertExt {
    private AssertExt() {
    }

    public static Object notImplemented() {
        return notImplemented(null);
    }

    public static Object notImplemented(final String feature) {
        final String msg = feature == null ? "Not implemented" : (feature + " not implemented");
        throw new AssertionFailure(msg);
    }

    public static Object unexpectedValue(final String val) {
        throw new AssertionFailure("Unexpected value \"" + val + "\"");
    }

    public static void assertCond(final boolean test) {
        if (!test) {
            throw new AssertionFailure("Assertion Failure");
        }
    }

    public static void notNull(final Object o) {
        notNull(o, null);
    }

    public static void notNull(final Object o, final String objectName) {
        if (o == null) {
            final String errorMsg = (objectName == null) ? "Object cannot be null." : "\"" + objectName + "\" cannot be null.";
            throw new AssertionFailure(errorMsg);
        }
    }

    /**
     * @param test
     * @param msg  avoid wasting time building the message because this code is not going to be smart about short-circuit
     *             evaluation
     */
    public static void assertCond(final boolean test, final String msg) {
        if (!test) {
            throw new AssertionFailure(msg);
        }
    }

    public static void fail(final String msg)
    {
        throw new AssertionFailure(msg);
    }

    public static void unexpectedException(final Throwable e) {
        throw new AssertionFailure("Unexpected exception", e);
    }

    public static Object unexpectedCodePath(final String s) {
        throw new AssertionFailure("Unexpected code path: " + s);
    }

    /**
     * Asserts that the supplied arrays have equal objects, but disregards array
     * position.  The arguments are also checked for nullity.
     * @param expected an array
     * @param actual an array
     */
    public static void assertEqualNoOrder(final Object[] expected, final Object[] actual) {
        final String errorMessage = "Arrays are different.";
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || actual == null) {
            throw new AssertionFailure(errorMessage + " Expected: <" + String.valueOf(expected) + ">," +
                                       " Actual: <" + String.valueOf(actual) + ">.");
        }
        if (expected.length != actual.length) {
            throw new AssertionFailure(errorMessage + " Expected length is <" + expected.length + ">, " +
                                                      " Actual length is <" + actual.length + ">.");
        }

        final List source = toList(expected);
        final List target = toList(actual);

        for (Iterator sourceItr = source.iterator(); sourceItr.hasNext(); ) {
            final Object one = sourceItr.next();
            if (target.contains(one)) {
                sourceItr.remove();
            }
            else {
                throw new AssertionFailure(errorMessage + " Found object \"" + one.toString() + "\" in expected array but not actual array.");
            }
        }
    }


    /**
     * Converts the specified array into a List.  CANNOT use Arrays.asList() because I want a modifiable list back.
     * @param array the object array.
     * @return a modifiable list.
     */
    private static List toList(final Object[] array) {
        final ArrayList<Object> out = new ArrayList<Object>();
        if (array == null) {
            return out;
        }
        out.addAll(Arrays.asList(array));
        return out;
    }


    public static Object unreachable()
    {
        throw new AssertionFailure("Should be unreachable");
    }

    /**
     * make sure that a) arr is not null; and b) no element of arr is null.
     * @param arr
     * @param objLabel
     */
    public static void notNullElements(final Object[] arr, final String objLabel)
    {
        if ( arr == null )
            throw new IllegalArgumentException( "\"" + objLabel + "\" is null" );
        for (int i = 0; i < arr.length; i++)
            if (arr[i] == null)
                throw new IllegalArgumentException( "\"" + objLabel + "\"[" + i + "] is null");
    }
}
