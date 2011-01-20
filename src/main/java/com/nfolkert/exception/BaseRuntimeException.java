package com.nfolkert.exception;

import org.apache.commons.lang.exception.NestableRuntimeException;

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
