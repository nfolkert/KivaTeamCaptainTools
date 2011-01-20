package com.nfolkert.exception;

import com.nfolkert.exception.BaseRuntimeException;

/**
 * A runtime exception that indicates an assertion failure.
 */
public class AssertionFailure extends BaseRuntimeException
{
    public AssertionFailure(final String message) {
        super(message);
    }

    public AssertionFailure(final String message, final Throwable cause) {
        super(message, cause);
    }
}
