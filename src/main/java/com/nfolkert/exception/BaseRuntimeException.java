package com.nfolkert.exception;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * This is the base exception for exceptions thrown by the application that should not be declared in method
 * signatures.
 * <p/>
 * Generally, you should use BaseException -- however, using BaseRuntimeException is better than using RuntimeException.
 * <p/>
 * Date: Dec 17, 2004
 * Time: 5:02:37 PM
 *
 * @author bbabcock
 */
public class BaseRuntimeException extends NestableRuntimeException {
    public BaseRuntimeException() {
    }

    public BaseRuntimeException(final String s) {
        super(s);
    }

    public BaseRuntimeException(final Throwable throwable) {
        super(throwable);
    }

    public BaseRuntimeException(final String s, final Throwable throwable) {
        super(s, throwable);
    }
}
